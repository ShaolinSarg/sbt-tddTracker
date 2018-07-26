package sarginson.sbt

import java.text.SimpleDateFormat

import play.api.libs.json.{JsValue, Json}
import sarginson.sbt.domain.TestSnapshot
import sbt.{TestEvent, TestResult, TestsListener}
import sbt.testing.Status.{Error, Failure}

trait TddTestReporterTested extends TestsListener {
  val httpClient: HttpClient

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  var failureCount: Int = 0
  var failingTestIds: List[String] = Nil


  def generateTestSnapshot(t: TestSnapshot): JsValue = {

    Json.obj(
      "timestamp" -> dateFormatter.format(t.timestamp),
      "failingTestCount" -> t.failingTestCount,
      "failingTestNames" -> t.failingTestNames)
  }

  def sendTestSnapshot(t: TestSnapshot): Int = {
    val requestBody: JsValue = generateTestSnapshot(t)

    httpClient.doPost(Json.stringify(requestBody)).code
  }


  override def testEvent(event: TestEvent): Unit = {
    def isFailingTest(event: sbt.testing.Event): Boolean = (event.status == Error) || (event.status == Failure)
    def failingCount:Int = event.detail.count(isFailingTest)

    failingTestIds = failingTestIds ++ event.detail.filter(isFailingTest).map(t => t.fullyQualifiedName)
    failureCount = failureCount + failingCount
  }


  override def doComplete(finalResult: TestResult): Unit = {
    if (TddTestReporterPlugin.sessId.isDefined) {

      val currDateTime = new java.util.Date

      println("")
      println("~~~~~ Reporting TDD metrics ~~~~~")
      println(s"~ Session is: ${TddTestReporterPlugin.sessId}")
      println(s"~ Current date/time: ${dateFormatter.format(currDateTime)}")
      println(s"~ Failing test count: $failureCount")
      println(s"~ Failing test IDs: ${failingTestIds.mkString(", ")}")
      println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
      println("")

      sendTestSnapshot(TestSnapshot(
        currDateTime,
        failureCount,
        failingTestIds))

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


  override def doInit(): Unit = {
    failureCount = 0
    failingTestIds = Nil
  }

  override def startGroup(name: String): Unit = ()
  override def endGroup(name: String, t: Throwable): Unit = ()
  override def endGroup(name: String, result: TestResult): Unit = ()

}

object ScalajTestReporter extends TddTestReporterTested {
  override val httpClient = ScalajHttpClient
}
