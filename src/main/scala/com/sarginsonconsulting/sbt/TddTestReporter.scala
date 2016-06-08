package com.sarginsonconsulting.sbt


import sbt._
import Keys._
import sbt.testing.Status.{Failure, Error, Success}


object TddTestReporterPlugin extends AutoPlugin {

    object autoImport {}

    override lazy val projectSettings = Seq(
        testListeners += new TddTestReporter
    )

    override val trigger = AllRequirements
}


class TddTestReporter extends TestsListener {

  var failureCount: Int = 0
  var failingTestIds: Set[String] = Set()
  var runtime: Long = 0

  override def doInit(): Unit = ()

  override def doComplete(finalResult: TestResult.Value): Unit = {
    println("")
    println("@@@@@@ This is where I would pass the following to the TDD metric engine @@@@@@")
    
    println(s"Current date/time: ${new java.util.Date}")
    println(s"Failing test count: $failureCount")
    println(s"Failing test IDs: " + failingTestIds.mkString(", "))
    //println(s"Test run duration: $runtime")
    println("")
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
