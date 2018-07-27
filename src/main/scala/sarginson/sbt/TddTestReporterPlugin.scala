package sarginson.sbt

import java.text.SimpleDateFormat

import sbt.Keys.testListeners
import sbt.{AllRequirements, AutoPlugin, inputKey}

object TddTestReporterPlugin extends AutoPlugin {

  var sessId: Option[Int] = None
  val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val tddReporter: TddTestReporter = ScalajTestReporter

  object autoImport {
    val tddStart = inputKey[Unit]("Starts a tdd session by getting a tdd session id")
    val tddStatistics = inputKey[Unit]("Returns the statistics for the specified tdd session")
    val tddEnd = inputKey[Unit]("Ends a tdd session by getting removing the tdd session id")
  }

  override lazy val projectSettings =
    super.projectSettings ++ Seq(testListeners += tddReporter,

    autoImport.tddStart := {
      if (sessId.isDefined) {
        println("")
        println("~~~~~~~~~~~~ TDD metrics ~~~~~~~~~~~~")
        println(s"Session already started with ID: ${sessId.get}")
        println("run `tddEnd` to close the current session")
        println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        println("")

      } else {
        sessId = tddReporter.startTDDSession()
      }
    },

    autoImport.tddStatistics := {
      sessId.foreach { id =>
        val stats = tddReporter.statisticsForSession(id)

        println("")
        println("~~~~~ Reporting TDD metrics ~~~~~")
        println(stats)
        println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        println("")
      }
    },

    autoImport.tddEnd := { sessId = None }

  )

  override val trigger = AllRequirements
}