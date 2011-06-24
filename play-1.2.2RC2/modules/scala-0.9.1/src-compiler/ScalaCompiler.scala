package play.scalasupport.compiler

import _root_.xsbt.boot.{AppID, Provider, Launcher, IvyOptions, Locks, AppConfiguration}
import _root_.sbt.{Compiler => SbtCompiler, Level, CompileOrder, Logger => SbtLoggerAPI}

import java.io.{File, FileOutputStream}

case class CompilationError(severity: xsbti.Severity, message: String, source: Option[File], line: Option[Int], marker: Option[Int])
case class ClassDefinition(name: String, code: Array[Byte], source: Option[File])

object PlayScalaCompiler {
    
    def scanFiles(path: File, regex: scala.util.matching.Regex = "^[^.].*[.](scala|java)$".r ):Seq[File] = {
        if(path.isDirectory) {
            path.listFiles.toSeq.collect({
                case f if f.isFile && regex.unapplySeq(f.getName).isDefined => Seq(f.getAbsoluteFile)
                case f if f.isDirectory => scanFiles(f, regex)
            }).flatten
        } else {
            Nil
        }
    } 
    
}

class PlayScalaCompiler(app: File, libs: File, classpath: List[File], output: File, order: CompileOrder.Value = CompileOrder.Mixed, skipResult: Boolean = false) {
    
    // Enable deeper Analysis logging -->
    // System.setProperty("xsbt.inc.debug", "true")
    
    val sbt = new Launch
    val scalaProvider = sbt.getScala("2.8.1")
    val appProvider = scalaProvider.app(AppID("play", "application", "1.0", "", Array(), false, Array()))
    val appConfig = new AppConfiguration(Array(), app, appProvider)
    val compilers = SbtCompiler.compilers(appConfig, SbtLogger)
    
    @scala.annotation.tailrec private def classFile2className(f:File, suffix:String = ""):String = {
        (f, f.getName) match {
            case (f, name) if f.isFile => classFile2className(f.getParentFile, name.split('.').head)
            case (_, "classes") => suffix
            case (f, name) => classFile2className(f.getParentFile, name + "." + suffix)
        }   
    }
    
    // Keep the state of successive compilations
    var previousProducts = Map[String,(File,Long)]()   
    
    // Public API ->
    // Call update() with the list of source files of your application,
    // then you get Either(compilationError, (updatedClasses,removedClasses))    
    def update(sources:List[File]):Either[CompilationError,(List[ClassDefinition], List[ClassDefinition])] = {        
        try {
            val inputs = SbtCompiler.inputs(classpath, sources, output, Nil/*Seq("-verbose")*/, Seq("-g"), 1, order)(compilers, SbtLogger)        
            
            val result = SbtCompiler(inputs, SbtLogger)
            val (stamps,relations) = result.stamps -> result.relations

            val newProducts = Map( stamps.allProducts.toSeq.map( classFile =>
                (classFile2className(classFile) -> (classFile, classFile.lastModified))
            ):_* )
            
            val byteCode = (className:String) => {
                val classFile = new File(output, "classes/" + className.replace(".", "/") + ".class")
                val is = new java.io.FileInputStream(classFile)
                val code = new Array[Byte](classFile.length.toInt)
                is.read(code)
                is.close()
                code
            }
            
            if(skipResult) {
                
                Right(Nil,Nil)
                
            } else {
                
                // Compute the result of the compilation
                
                val removed = (previousProducts.keySet &~ newProducts.keySet).map( className => {
                    ClassDefinition(className, Array[Byte](), None)
                }).toList.sortBy(_.name)

                val added = (newProducts.keySet &~ previousProducts.keySet).map( className => {
                    ClassDefinition(className, byteCode(className), relations.produced(newProducts(className)._1).headOption)
                })

                val recompiled = (newProducts.keySet & previousProducts.keySet).filter( className => {
                    newProducts(className)._2 > previousProducts(className)._2
                }).map( className => {
                    ClassDefinition(className, byteCode(className), relations.produced(newProducts(className)._1).headOption)
                })

                val updated = (added ++ recompiled).toList.sortBy(_.name)

                // Now change the internal state for the next compilation
                previousProducts = newProducts

                Right((updated, removed))
                
            }
            
        } catch {
            case cf:xsbti.CompileFailed => {
                Left(
                    cf.problems.headOption.map( problem =>
                        CompilationError(
                            problem.severity, 
                            problem.message, 
                            Option(problem.position).collect {case p if p.sourceFile.isDefined => p.sourceFile.get},
                            Option(problem.position).collect {case p if p.line.isDefined => p.line.get.intValue},
                            Option(problem.position).collect {case p if p.offset.isDefined => p.offset.get.intValue}
                        )
                    ).getOrElse(
                        CompilationError(xsbti.Severity.Error, cf.toString, None, None, None)
                    )
                )
            }
            case e:java.lang.reflect.InvocationTargetException => error(e.getTargetException.getMessage)
        }
    }
    
    // ~~
    // ~~ Internal SBT usage
    // ~~
    
    // SBT logger
    object SbtLogger extends SbtLoggerAPI {
        
        def trace(t: => Throwable) = {
            //println("TRACE: " + t)
        }
        
        def success(message: => String) = {
            //println("SUCCESS: " + message)
        }
        
        def log(level: Level.Value, message: => String) = {
            level match {
                case Level.Error => println(message)
                case _ => //println("!!! " + message)
            }
        }

    }

    class Launch(val bootDirectory: File = app, val ivyOptions: IvyOptions = null) extends xsbti.Launcher {  
        
        lazy val tmpDirectory = new File(bootDirectory, "tmp")

        def getScala(version: String): xsbti.ScalaProvider = getScala(version, "")
        def getScala(version: String, reason: String): xsbti.ScalaProvider = new ScalaProvider(version)

        lazy val topLoader = classOf[String].getClassLoader
        val updateLockFile = new File(tmpDirectory, "boot.lock")

        def globalLock: xsbti.GlobalLock = Locks
        
        // We don't use SBT dependencies management… so will allow a bunch of null here
        def ivyHome = null

        class ScalaProvider(val version: String) extends xsbti.ScalaProvider with Provider {
            
            def launcher = Launch.this
            def parentLoader = topLoader

            lazy val configuration = null // Hope that's not used
            lazy val libDirectory = libs
            lazy val scalaHome = libs
            
            def compilerJar = new File(scalaHome, "scala-compiler.jar")
            def libraryJar = new File(scalaHome, "scala-library.jar")
            def baseDirectories = List(scalaHome)
            def testLoadClasses = Nil
            def target = null
            def failLabel = "Play Scala " + version
            def lockFile = new File(tmpDirectory, "scala.lock")
            def extraClasspath = Array()
            override def app(id: xsbti.ApplicationID): xsbti.AppProvider = new AppProvider(id)

            class AppProvider(val id: xsbti.ApplicationID) extends xsbti.AppProvider with Provider {

                def scalaProvider: xsbti.ScalaProvider = ScalaProvider.this
                def configuration = ScalaProvider.this.configuration

                lazy val appHome = bootDirectory

                def parentLoader = ScalaProvider.this.loader
                def baseDirectories = List(appHome)
                def testLoadClasses = Nil
                def target = null
                def failLabel = "Application"
                def lockFile = new File(tmpDirectory, "app.lock")
                def mainClasspath = Array()
                def extraClasspath = Array()

                lazy val mainClass = null
                def newMain() = null

                lazy val components = new xsbti.ComponentProvider {

                    def component(componentID: String) = {
                        componentID match {
                            case "compiler-interface-bin_2.8.1.final" => Array(new File(libs, "../dlib/compiler-interface-bin-0.9.5.jar"))
                            case _ => Array[File]()
                        }
                    }

                    def defineComponent(componentID: String, components: Array[File]) {
                        throw new Error("CALLED defineComponent with " + componentID)
                    }

                    def lockFile = new File(tmpDirectory, "components.lock")

                }
            }
        }

    }
    
}