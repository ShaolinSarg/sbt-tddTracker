package sarginson.sbt

import java.text.SimpleDateFormat
import java.util.Date

import sarginson.sbt.connectors._
import sarginson.sbt.domain.TestSnapshot
import sbt.testing.Status.{Error, Failure}
import sbt.{TestEvent, TestResult, TestsListener}
import scalaj.http.HttpResponse


trait TddTestReporter extends TestsListener {
  val httpClient: HttpClient

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  var failureCount: Int = 0
  var failingTestIds: List[String] = Nil

  def generateTddSession(): String = {
//    JsObject(
//      "timestamp" -> JsString(dateFormatter.format(new Date)),
//      "watchedFiles" -> JsArray(JsArray(JsString(".scala")))
//    )

    s"""{"timestamp":"${dateFormatter.format(new Date)}","watchedFiles":[".scala"]}"""
  }

  def generateTestSnapshot(t: TestSnapshot): String = {
//    JsObject(
//      "timestamp" -> JsString(dateFormatter.format(t.timestamp)),
//      "failingTestCount" -> JsNumber(t.failingTestCount),
//      "failingTestNames" -> JsArray(t.failingTestNames.toVector.map(JsString(_))))

    s"""{"timestamp":"${dateFormatter.format(t.timestamp)}","failingTestCount":${t.failingTestCount},"failingTestNames":[${t.failingTestNames.mkString("\"", "\",\"","\"")}]}"""
  }

  def sendTestSnapshot(t: TestSnapshot): Int = {
    val requestBody: String = generateTestSnapshot(t)

    val resp: Int = httpClient.doPost(requestBody, SnapshotsUri(TddTestReporterPlugin.sessId.get)).code
    resp
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
      println("~ run `tddStatistics` to see session statistics")
      println("~ run `tddEnd` to stop a session")
      println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
      println("")
    }
  }

  def startTDDSession(): Option[Int] = {

    httpClient.doPost(generateTddSession(), SessionsUri)
      .header("Location")
      .map(
        _.split("/")
          .last
          .toInt)
  }

  def statisticsForSession(sessionId: Int): String = {
    val resp:HttpResponse[String] = httpClient.doGet(StatsUri(sessionId))
    resp.body
  }

  override def doInit(): Unit = {
    failureCount = 0
    failingTestIds = Nil
  }

  override def startGroup(name: String): Unit = ()
  override def endGroup(name: String, t: Throwable): Unit = ()
  override def endGroup(name: String, result: TestResult): Unit = ()

}

object ScalajTestReporter extends TddTestReporter {
  override val httpClient = ScalajHttpClient
}
