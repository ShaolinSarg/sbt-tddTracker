package sarginson.sbt

import scalaj.http.{ Http, HttpResponse }

trait HttpClient {
  def doPost(payload: String): HttpResponse[String]
}

object ScalajHttpClient extends HttpClient {
  override def doPost(payload: String): HttpResponse[String] = {
    Http("http://localhost:3000/sessions/300/snapshots")
      .headers("content-type" -> "application/json")
      .postData(payload)
      //          .postData(Json.stringify(data))
      .asString
  }
}
