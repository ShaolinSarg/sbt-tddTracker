package sarginson.sbt

import java.text.SimpleDateFormat
import java.util.Date

import sbt.Keys.{baseDirectory, testListeners}
import sbt.testing.Status.{Error, Failure}
import sbt.{AllRequirements, AutoPlugin, TestEvent, TestResult, TestsListener, inputKey}

import scalaj.http.Http

object TddTestReporterPlugin extends AutoPlugin {

  var sessId: Option[Int] = None
  val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  object autoImport {
    val tddStart = inputKey[Unit]("Starts a tdd session by getting a tdd session id")
    val tddDetails = inputKey[Unit]("Returns the stats for the specified tdd session")
    val tddEnd = inputKey[Unit]("Ends a tdd session by getting removing the tdd session id")
  }

  override lazy val projectSettings = super.projectSettings ++ Seq(
    testListeners += new TddTestReporter,

    autoImport.tddStart := {
      if (sessId.isDefined) {
        println("")
        println("~~~~~~~~~~~~ TDD metrics ~~~~~~~~~~~~")
        println(s"Session already started with ID: ${sessId.get}")
        println("run `tddEnd` to close the current session")
        println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        println("")
      } else {

        val postData: String = s"""{"timestamp" : "${formatter.format(new Date)}",""" +
          """"watchedFiles" : ".scala"}"""

        val resp = Http("http://localhost:3000/session")
          .headers("content-type" -> "application/json")
          .postData(postData)
          .asString

        sessId = resp.header("Location").map(
          _.split("/")
            .last
            .toInt)
      }
    },

    autoImport.tddDetails := {
      val resp = Http(s"http://localhost:3000/session/${TddTestReporterPlugin.sessId}/stats").postForm(Seq(
        "timestamp" -> formatter.format(new Date))).asString

      println("")
      println("~~~~~ Reporting TDD metrics ~~~~~")
      println(resp.body)
      println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
      println("")
    },

    autoImport.tddEnd := {
      sessId = None
    }
  )

  override val trigger = AllRequirements
}

class TddTestReporter extends TestsListener {

  var failureCount: Int = 0
  var failingTestIds: Set[String] = Set()
  var runtime: Long = 0

  override def doInit(): Unit = {}

  override def doComplete(finalResult: TestResult): Unit = {

    if (TddTestReporterPlugin.sessId.isDefined) {
      println("")
      println("~~~~~ Reporting TDD metrics ~~~~~")
      println(s"~ Session is: ${TddTestReporterPlugin.sessId}")
      println(s"~ Current date/time: ${new java.util.Date}")
      println(s"~ Failing test count: $failureCount")
      println(s"~ Failing test IDs: " + failingTestIds.mkString(", "))
      println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
      println("")

      Http(s"http://localhost:3000/session/${TddTestReporterPlugin.sessId}/snapshot").postForm(Seq(
        "timestamp" -> TddTestReporterPlugin.formatter.format(new Date),
        "failingTestCount" -> failureCount.toString,
        "failingTestNames" -> failingTestIds.mkString(", "))).asString

    } else {
      println("")
      println("~~~~~ TDD metrics ~~~~~")
      println("~ No TDD session started")
      println("~ run `tddStart` to begin")
      println("~ run `tddDetails` to see session statistics")
      println("~ run `tddEnd` to stop a session")
      println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
      println("")

    }
  }

  override def testEvent(event: TestEvent): Unit = {
    def isFailingTest(event: sbt.testing.Event): Boolean = (event.status == Error) || (event.status == Failure)
    def failingCount:Int = event.detail.count(isFailingTest)

    failingTestIds = failingTestIds ++ event.detail.filter(isFailingTest).map(t => t.fullyQualifiedName)
    failureCount = failureCount + failingCount
  }

  override def startGroup(name: String): Unit = ()
  override def endGroup(name: String, t: Throwable): Unit = ()
  override def endGroup(name: String, result: TestResult): Unit = ()

}