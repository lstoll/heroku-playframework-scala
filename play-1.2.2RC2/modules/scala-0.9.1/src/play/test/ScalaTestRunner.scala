package org.scalatest.tools

import org.scalatest._
import org.scalatest.events._
import org.scalatest.tools._
import org.scalatest.junit._

import play.Play
import play.test.BaseTest
import play.test.TestEngine.{TestResults, TestResult}

object ScalaTestRunner {

    def runSuiteClass(suiteClass: Class[Suite]) = {
        try {
            val suite = suiteClass.newInstance
            run(suite)
        } catch {
            case e => val results = new TestResults
                      val result = new TestResult
                      result.name = "Test creation"
                      result.time = 0
                      result.error = e.getMessage
                      
                      val tmpout = new java.io.StringWriter
                      e.printStackTrace(new java.io.PrintWriter(tmpout))
                      result.trace = tmpout.toString

                      import util.control.Breaks._

                      breakable { for(se <- e.getStackTrace) {
                          if(se.getClassName.equals(suiteClass.getName) || se.getClassName.startsWith(suiteClass.getName+"$")) {
                              result.sourceInfos = "In " + Play.classes.getApplicationClass(suiteClass.getName).javaFile.relativePath() + ", line " + se.getLineNumber
                              result.sourceCode = Play.classes.getApplicationClass(suiteClass.getName).javaSource.split("\n")(se.getLineNumber-1)
                              result.sourceFile = Play.classes.getApplicationClass(suiteClass.getName).javaFile.relativePath
                              result.sourceLine = se.getLineNumber
                              break
                          }
                      } }
                      
                      result.passed = false
                      results.passed = false
                      results add result
                      results
                        
        }
    }
    
    def runJunitClass(junitClass: Class[BaseTest]) = {
        val suite = new JUnitWrapperSuite(junitClass.getName, play.Play.classloader)
        run(suite)
    }
    
    def run(suite: Suite) = {
        val reporter = new PlayReporter
        val dispatch = new DispatchReporter(List(reporter), System.out)
        val runner = new SuiteRunner(suite, dispatch, NoStop, new Filter(None, Set[String]()), Map[String,Any](), None, new Tracker(new Ordinal(1)))
        runner.run()
        dispatch.dispatchDisposeAndWaitUntilDone()
        reporter.results
    }

}

class PlayReporter extends Reporter {

    val results = new TestResults

    def apply(event : Event) {
        event match {

            case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
                val result = new TestResult
                result.name = testName
                result.time = duration.getOrElse(0)
                results add result

            case TestPending(ordinal, suiteName, suiteClassName, testName, formatter, payload, threadName, timeStamp) =>
                val result = new TestResult
                result.name = testName
                result.time = -1
                results add result

            case TestFailed(ordinal, message, suiteName, suiteClassName, testName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
                val result = new TestResult
                result.name = testName
                result.time = duration.getOrElse(0)
                result.passed = false
                results.passed = false
                
                throwable match {
                    case Some(e) => result.error = e.getMessage
                    case None => result.error = message
                }
                
                throwable match {
                    case Some(e: StackDepth) => val se = e.getStackTrace()(e.failedCodeStackDepth)
                                               result.sourceInfos = "In " + e.failedCodeFileNameAndLineNumberString.getOrElse("(unknow)").replace(":", ", ")
                                               result.sourceFile = Play.classes.getApplicationClass(se.getClassName).javaFile.relativePath
                                               result.sourceLine = se.getLineNumber
                                               result.sourceCode = Play.classes.getApplicationClass(se.getClassName).javaSource.split("\n")(se.getLineNumber-1)
                    
                    case Some(e) => val tmpout = new java.io.StringWriter
                                    e.printStackTrace(new java.io.PrintWriter(tmpout))
                                    result.trace = tmpout.toString
                                    
                                    import util.control.Breaks._
                                    
                                    breakable { for(se <- e.getStackTrace) {
                                        if(se.getClassName.equals(suiteClassName.get) || se.getClassName.startsWith(suiteClassName.get+"$")) {
                                            result.sourceInfos = "In " + Play.classes.getApplicationClass(suiteClassName.get).javaFile.relativePath() + ", line " + se.getLineNumber
                                            result.sourceCode = Play.classes.getApplicationClass(suiteClassName.get).javaSource.split("\n")(se.getLineNumber-1)
                                            result.sourceFile = Play.classes.getApplicationClass(suiteClassName.get).javaFile.relativePath
                                            result.sourceLine = se.getLineNumber
                                            break
                                        }
                                    } }
                    
                    case _ => // No source informations?
                }
                
                results add result

            case _ => 

        }
    }

}

object NoStop extends Stopper {

    override def apply = false

}