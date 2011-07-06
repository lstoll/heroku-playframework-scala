package play.scalasupport

import play._
import play.test._
import play.vfs.{VirtualFile => VFile}
import play.exceptions._
import play.classloading.ApplicationClasses.ApplicationClass
import play.scalasupport.compiler._
import play.classloading.HotswapAgent
import play.templates._

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap

import java.util.{List => JList, Map => JMap}
import java.net.URLClassLoader
import java.io.{File, PrintStream, ByteArrayOutputStream}

import org.scalatest.{Suite, Assertions}
import org.scalatest.tools.ScalaTestRunner

class ScalaPlugin extends PlayPlugin {

    override def onLoad {
        CustomGroovy()
        play.data.binding.Binder.register(classOf[play.db.anorm.Pk[_]], new PkBinder())
        play.data.binding.Binder.register(classOf[Option[_]], new OptionBinder())
        onConfigurationRead()

        // Now scala is activated
        Logger.info("Scala support is active")
    }

    override def onConfigurationRead() {
        Play.configuration.put("play.bytecodeCache", "false")
    }

    override def overrideTemplateSource(template: BaseTemplate, source: String) = {
        if(template.isInstanceOf[GroovyTemplate]) {
            template.source.replace("?.", "?.safeNull()?.")
        } else {
            null
        }
    }

    override def addTemplateExtensions(): JList[String] = {
        List("play.templates.TemplateScalaExtensions")
    }

    override def willBeValidated(o: Any) = {
        o match {
            case Some(v) => v.asInstanceOf[AnyRef]
            case _ => null
        }
    }

    override def bind(name:String,
                      clazz:Class[_],
                      t:java.lang.reflect.Type,
                      annotations:Array[java.lang.annotation.Annotation],
                      params: java.util.Map[String, Array[String]]) = {
        clazz match {
            case c if c == classOf[Option[_]] => {
                val parameterClass = t.asInstanceOf[java.lang.reflect.ParameterizedType].getActualTypeArguments()(0)
                val result = play.data.binding.Binder.bind(name, parameterClass.asInstanceOf[Class[_]], parameterClass, annotations, params)
                Option(result)
            }
            case c if c == classOf[play.db.anorm.Pk[_]] => {
                val parameterClass = t.asInstanceOf[java.lang.reflect.ParameterizedType].getActualTypeArguments()(0)
                val result = play.data.binding.Binder.bind(name, parameterClass.asInstanceOf[Class[_]], parameterClass, annotations, params)
                play.db.anorm.Id(result)
            }
            case c if c == classOf[play.db.anorm.Id[_]] => {
                val parameterClass = t.asInstanceOf[java.lang.reflect.ParameterizedType].getActualTypeArguments()(0)
                val result = play.data.binding.Binder.bind(name, parameterClass.asInstanceOf[Class[_]], parameterClass, annotations, params)
                play.db.anorm.Id(result)
            }
            case _ => null
       }
    }

    override def unBind(o:Any, name:String) = {
        o match {
            case play.db.anorm.Id(id) => Map(name -> id).asInstanceOf[Map[String,AnyRef]]
            case play.db.anorm.NotAssigned => null
            case Some(v) => Map(name -> v).asInstanceOf[Map[String,AnyRef]]
            case None => null
            case _ => null
        }
    }

    override def runTest(testClass: Class[BaseTest]) = {
        testClass match {
            case suite if classOf[Suite] isAssignableFrom testClass => ScalaTestRunner runSuiteClass suite.asInstanceOf[Class[Suite]]
            case junit if classOf[Assertions] isAssignableFrom testClass => ScalaTestRunner runJunitClass junit
            case _ => null
        }
    }

    override def compileSources = {
        update() match {
            case Right(compiled) => updateInternalApplicationClasses(compiled)
            case Left(err) => throw compilationException(err)
        }

        weHaveCompiled
    }

    override def detectClassesChange = {
        update() match {
            case Right((added,removed)) => {
                updateInternalApplicationClasses((added,removed))

                if(added.length + removed.length > 0) {
                    reload()
                }
            }
            case Left(err) => throw compilationException(err)
        }

        weHaveCompiled
    }

    def compilationException(compilationError: CompilationError) = {
        val CompilationError(_, message, source, line, marker) = compilationError
        if(source.isDefined) {
            if(source.get.getParentFile == ScalaTemplateCompiler.generatedDirectory) {
                val generatedSource = GeneratedSource(source.get)
                val originalPos = generatedSource.toSourcePosition(marker.get)
                new CompilationException(VFile.open(generatedSource.source.get), message, originalPos._1, originalPos._2 - 1, originalPos._2 - 1)
            } else {
                new CompilationException(VFile.open(source.get), message, line.getOrElse(-1), marker.getOrElse(0) - 1, marker.getOrElse(0) - 1)
            }
        } else {
            new CompilationException(message)
        }
    }

    def updateInternalApplicationClasses(compiled:(List[ClassDefinition], List[ClassDefinition])) {
        val (added,removed) = compiled

        // Removed deleted classes
        removed.foreach { c =>
            Play.classes.remove(c.name)
        }

        // Add new classes 
        added.foreach { c =>
            val appClass = new ApplicationClass
            appClass.name = c.name
            appClass.javaFile = VFile.open(c.source.get)
            appClass.refresh()
            appClass.compiled(c.code)
            Play.classes.add(appClass)
        }
    }

    def reload() = error("Reload needed")

    def weHaveCompiled = true

    // ----- Compiler interface

    val compiler = new PlayScalaCompiler(
        Play.applicationPath, 
        new File(Play.modules("scala").getRealFile, "lib"), 
        System.getProperty("java.class.path").split(System.getProperty("path.separator")).map(new File(_)).toList, 
        Play.tmpDir
    )

    def sources:Map[File,Long] = {
        import play.vfs.VirtualFile
        currentSources.empty ++ (for(p <- (Play.javaPath ++ Seq(VirtualFile.open(ScalaTemplateCompiler.generatedDirectory)))) 
            yield PlayScalaCompiler.scanFiles(p.getRealFile)).flatten.map(f => (f,f.lastModified))
    }

    def templates:Seq[File] = {
        (for(p <- Play.javaPath) 
            yield PlayScalaCompiler.scanFiles(p.getRealFile, """^[^.].*[.]scala[.](html|txt)$""".r)).flatten
    }

    def generated:Seq[GeneratedSource] = {
        ScalaTemplateCompiler.generatedDirectory.listFiles.map { f =>
            GeneratedSource(f)
        }
    }

    var currentSources = Map[File,Long]()

    def update() = {

        // Sync generated
        generated.foreach(_.sync())

        // Generate templates
        templates.foreach(ScalaTemplateCompiler.compile)

        val newSources = sources
        if(currentSources != newSources) {
            compiler.update(newSources.keySet.toList).right.map( r => {currentSources = newSources; r} )
        } else {
            Right(Nil,Nil)
        }
    }

}

