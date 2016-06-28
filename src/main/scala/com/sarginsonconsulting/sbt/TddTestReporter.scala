package com.sarginsonconsulting.sbt


import sbt._
import Keys._
import sbt.testing.Status.{Failure, Error}
import scalaj.http._
import java.util.Date
import java.text.SimpleDateFormat

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
        println("Session already started with ID: ${sessId.get}")
        println("run `tddEnd` to close the current session")
        println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        println("")
      } else {
        val resp = Http("http://localhost:3000/session").postForm(Seq(
          "timestamp" -> formatter.format(new Date),
          "projectBase" -> baseDirectory.value.getAbsolutePath,
          "watchedFiles" -> ".scala")).asString
        
        sessId = resp.body.split(",").head.split(":").drop(1).headOption.map(_.toInt)
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

  override def doComplete(finalResult: TestResult.Value): Unit = {

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
    def isFailingTest(event: sbt.testing.Event): Boolean = (event.status == Error) || (event.status==Failure)
    def failingCount:Int = event.detail.count(isFailingTest)

    failingTestIds = failingTestIds ++ event.detail.filter(isFailingTest).map(t => t.fullyQualifiedName)
    failureCount = failureCount + failingCount
    //runtime = event.detail.map(t => t.duration).reduce(_ + _)
  }

  override def startGroup(name: String): Unit = ()
  override def endGroup(name: String, t: Throwable): Unit = ()
  override def endGroup(name: String, result: TestResult.Value): Unit = ()

}
