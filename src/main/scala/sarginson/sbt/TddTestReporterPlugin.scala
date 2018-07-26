package sarginson.sbt

import java.text.SimpleDateFormat
import java.util.Date

import scalaj.http.HttpResponse

import sbt.Keys.testListeners
import sbt.{AllRequirements, AutoPlugin, inputKey}
import scalaj.http.Http

object TddTestReporterPlugin extends AutoPlugin {

  var sessId: Option[Int] = None
  val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val tddReporter = ScalajTestReporter 

  object autoImport {
    val tddStart = inputKey[Unit]("Starts a tdd session by getting a tdd session id")
    val tddDetails = inputKey[Unit]("Returns the stats for the specified tdd session")
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

//        val data: JsValue = Json.obj("timestamp" -> formatter.format(new Date),
//                                     "watchedFiles" -> ".scala")


        val data: String = s"""{"timestamp" : "${formatter.format(new Date)}",
                             | "watchedFiles" : ".scala"}""".stripMargin

        val resp: HttpResponse[String] = Http("http://localhost:3000/sessions")
          .headers("content-type" -> "application/json")
          .postData(data)
//          .postData(Json.stringify(data))
          .asString

        sessId = resp.header("Location").map(
          _.split("/")
            .last
            .toInt)
      }
    },

    autoImport.tddDetails := {
      val resp = Http(s"http://localhost:3000/sessions/${TddTestReporterPlugin.sessId}/stats").postForm(Seq(
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
