package sarginson.sbt

import sarginson.sbt.connectors.TDDUris
import scalaj.http.{Http, HttpResponse}


trait HttpClient {
  def doPost(payload: String, server: TDDUris): HttpResponse[String]
  def doGet(endPoint: TDDUris): HttpResponse[String]
}

object ScalajHttpClient extends HttpClient {
  override def doPost(payload: String, endPoint: TDDUris): HttpResponse[String] =
    Http(endPoint.uri)
      .headers("content-type" -> "application/json")
      .postData(payload)
      //          .postData(Json.stringify(data))
      .asString


  override def doGet(endPoint: TDDUris): HttpResponse[String] =
    Http(endPoint.uri)
      .headers("content-type" -> "application/json")
      .asString
}
