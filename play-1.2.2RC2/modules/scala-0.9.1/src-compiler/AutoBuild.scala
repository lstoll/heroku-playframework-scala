package play.scalasupport.build

import _root_.sbt.{CompileOrder}
import play.scalasupport.compiler._
import scala.annotation.tailrec
import java.io.{File}
import play.libs.{Files}

object ModuleBuilder {
    
    def main(args:Array[String]) {
        
        val isIncremental = System.getProperty("incremental") != null
        val classpath = System.getProperty("java.class.path").split(System.getProperty("path.separator")).map(new File(_)).toList        
        val compiler = new PlayScalaCompiler(new File("."), new File("lib"), classpath, new File("tmp"), skipResult = true)
        
        println("~")
        println("~ Using Play Scala auto compiler")
        println("~")
        
        def jar() {
            Files.delete(new File("lib/play-scala.jar"))
            Files.zip(new File("tmp/classes"), new File("lib/play-scala.jar"))
        }
        
        @tailrec 
        def compile() {
            
            val start = System.currentTimeMillis
            
            val message = compiler.update(PlayScalaCompiler.scanFiles(new File("src")).toList) match {
                case Left(CompilationError(severity, message, source, line, marker)) => severity + " Compilation failed: " + message + (source.map( f => " @ " + f + " line " + line.get).getOrElse(""))
                case Right(_) => "Successfully compiled in " + ((System.currentTimeMillis - start)/1000)  + "s. !"
            }
            
            println("~")
            println("~ " + message)
            println("~")

            isIncremental match {
                case true => {
                    
                    jar()
                    println("~ Updated lib/play-scala.jar")
                    println("~")
                    println("~ Press Enter to recompile (or Ctrl+C to quit)...")
                    println("~")

                    // Just wait for something from the command line
                    System.in.read()
                    
                    compile()
                }
                case false => {
                    
                    println("~ Now you can build the module incrementally using:")
                    println("~     ant incremental")
                    println("~")
                    
                }
            }
            
        }
        
        compile()
        
    }
    
}