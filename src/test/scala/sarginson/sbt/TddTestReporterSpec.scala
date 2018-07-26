package sarginson.sbt

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.Matchers.anyString
import play.api.libs.json.Json
import sarginson.sbt.domain.TestSnapshot
import sbt.TestEvent
import sbt.protocol.testing.TestResult
import sbt.testing._
import scalaj.http.HttpResponse

class TddTestReporterSpec extends WordSpec
    with Matchers
    with MockitoSugar {

  def createSUT = new TddTestReporterTested {
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
      when(sut.httpClient.doPost(anyString))
        .thenReturn(HttpResponse[String]("", 200, Map.empty))

      sut.sendTestSnapshot(snapshot) shouldBe 200

      verify(sut.httpClient, times(1)).doPost(anyString)
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

        when(sut.httpClient.doPost(anyString))
          .thenReturn(HttpResponse[String]("", 200, Map.empty))

        sut.doComplete(TestResult.Failed)

        verify(sut.httpClient, never).doPost(anyString)
      }
    }
    "send test results" when {
      "a session id exists within the plugin" in {
        val sut = createSUT

        TddTestReporterPlugin.sessId = Some(23)

        when(sut.httpClient.doPost(anyString))
          .thenReturn(HttpResponse[String]("", 200, Map.empty))

        sut.doComplete(TestResult.Failed)

        verify(sut.httpClient, times(1)).doPost(anyString)
      }
    }
  }
}