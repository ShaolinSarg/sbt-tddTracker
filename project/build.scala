import sbt._
import sbt.testing.Status.{Failure, Error, Success}

class TddTestListner extends TestsListener {

  var successCount: Int = 0
  var failureCount: Int = 0

  override def doInit(): Unit = ()

  override def doComplete(finalResult: TestResult.Value): Unit = {
    println("@@@@@@ THIS WORKS @@@@@@")
    println(s"there were $successCount successful tests")
    println(s"there were $failureCount failing tests")
  }

  override def testEvent(event: TestEvent): Unit = {
    successCount = successCount + event.detail.count(t => t.status()==Success)
    failureCount = failureCount + event.detail.count(t => (t.status() == Error) || (t.status()==Failure))
  }


  override def startGroup(name: String): Unit = ()
  override def endGroup(name: String, t: Throwable): Unit = ()
  override def endGroup(name: String, result: TestResult.Value): Unit = ()

}
