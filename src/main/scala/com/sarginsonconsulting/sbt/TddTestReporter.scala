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
  }

  override lazy val projectSettings = super.projectSettings ++ Seq(
    testListeners += new TddTestReporter,

    autoImport.tddStart := {
      val resp = Http("http://localhost:3000/session").postForm(Seq("timestamp" -> formatter.format(new Date))).asString 
      println(resp)
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
   
    println(TddTestReporterPlugin.sessId)
    println("")
    println("@@@@@@ Passed details to TDD metric engine @@@@@@")
    println(s"Current date/time: ${new java.util.Date}")
    println(s"Failing test count: $failureCount")
    println(s"Failing test IDs: " + failingTestIds.mkString(", "))
    println("")
    Http("http://localhost:3000/session/111/snapshot").postForm(Seq("timestamp" -> "2016-12-04 12:35:26", "failingTestCount" -> "3", "failingTestNames" -> failingTestIds.mkString(", "))).asString

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
