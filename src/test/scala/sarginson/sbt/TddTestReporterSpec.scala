package sarginson.sbt

import org.mockito
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.Matchers.any
import play.api.libs.json.{JsValue, Json}
import sarginson.sbt.connectors.{SessionsUri, SnapshotsUri, StatsUri}
import sarginson.sbt.domain.TestSnapshot
import sbt.TestEvent
import sbt.protocol.testing.TestResult
import sbt.testing._
import scalaj.http.HttpResponse

class TddTestReporterSpec extends WordSpec
    with Matchers
    with MockitoSugar {

  def createSUT = new TddTestReporter {
    override val httpClient = mock[HttpClient]
  }

  val snapshot = TestSnapshot(createSUT.dateFormatter.parse("2018-08-10 23:30:20"), 4, List("testSpec.scala", "testSpec2.scala"))

  class testTestEvent(name: String, s: Status) extends Event {
    override def fullyQualifiedName(): String = name
    override def fingerprint(): Fingerprint = ???
    override def selector(): Selector = ???
    override def status(): Status = s
    override def throwable(): OptionalThrowable = ???
    override def duration(): Long = ???
  }


  "generate the correct test run payload" in {
    val jsonPayload = """{"timestamp":"2018-08-10 23:30:20","failingTestCount":4,"failingTestNames":["testSpec.scala","testSpec2.scala"]}"""

    Json.stringify(createSUT.generateTestSnapshot(snapshot)) shouldBe jsonPayload
  }

  "sendTestSnapshot" should {
    "send the test run details to the server" in {

      val sut = createSUT

      TddTestReporterPlugin.sessId = Some(23)

      when(sut.httpClient.doPost(any[JsValue], mockito.Matchers.eq(SnapshotsUri(23))))
        .thenReturn(HttpResponse[String]("", 200, Map.empty))

      sut.sendTestSnapshot(snapshot) shouldBe 200
    }
  }
  "testEvent" should {
    "update failed test counts" in {
      val sut = createSUT

      sut.failureCount = 0
      sut.failingTestIds = Nil

      val e1: Event = new testTestEvent("spec1", Status.Failure)
      val e2: Event = new testTestEvent("spec2", Status.Error)
      val e3: Event = new testTestEvent("spec3", Status.Success)
      sut.testEvent(TestEvent(Seq(e1, e2, e3)))

      sut.failureCount shouldBe 2
      sut.failingTestIds should contain ("spec1")
      sut.failingTestIds should contain ("spec2")
      sut.failingTestIds should not contain "spec3"
    }
  }
  "onComplete" should {
    "not send test results" when {
      "no sesson id exists within the plugin" in {
        val sut = createSUT

        TddTestReporterPlugin.sessId = None

        when(sut.httpClient.doPost(any[JsValue], mockito.Matchers.eq(SnapshotsUri(23))))
          .thenReturn(HttpResponse[String]("", 200, Map.empty))

        sut.doComplete(TestResult.Failed)

        verify(sut.httpClient, never)
                  .doPost(any[JsValue], mockito.Matchers.eq(SnapshotsUri(23)))
      }
    }
    "send test results" when {
      "a session id exists within the plugin" in {
        val sut = createSUT

        TddTestReporterPlugin.sessId = Some(23)

        when(sut.httpClient.doPost(any[JsValue], mockito.Matchers.eq(SnapshotsUri(23))))
          .thenReturn(HttpResponse[String]("", 200, Map.empty))

        sut.doComplete(TestResult.Failed)

        verify(sut.httpClient, times(1))
                  .doPost(any[JsValue], mockito.Matchers.eq(SnapshotsUri(23)))
      }
    }
  }
  "doInit" should {
    "reset mutable variables" in {
      val sut = createSUT

      sut.failingTestIds = List("this", "that")
      sut.failureCount = 3

      sut.failingTestIds.size shouldBe 2
      sut.failureCount shouldBe 3

      sut.doInit()

      sut.failingTestIds.size shouldBe 0
      sut.failureCount shouldBe 0
    }
  }

  "startTDDSession" should {
    "retrieve a session id from the server" in {
      val sut = createSUT

      when(sut.httpClient.doPost(any[JsValue], mockito.Matchers.eq(SessionsUri)))
        .thenReturn(HttpResponse[String]("", 200, Map("Location" -> IndexedSeq("http://localhost:2999/sessions/4"))))

      val resp = sut.startTDDSession()

      resp shouldBe Some(4)
    }
  }

  "tddStatistics" should {
    "request the statistics for the given session" in {
      val sut = createSUT

      when(sut.httpClient.doGet(mockito.Matchers.eq(StatsUri(23))))
        .thenReturn(HttpResponse[String]("this is the statistics", 200, Map.empty))

      val resp = sut.statisticsForSession(23)

      resp shouldBe "this is the statistics"

    }
  }
}