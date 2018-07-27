package sarginson.sbt

import play.api.libs.json.{JsValue, Json}
import sarginson.sbt.connectors.TDDUris
import scalaj.http.{Http, HttpResponse}


trait HttpClient {
  def doPost(payload: JsValue, server: TDDUris): HttpResponse[String]
  def doGet(endPoint: TDDUris): HttpResponse[String]
}

object ScalajHttpClient extends HttpClient {
  override def doPost(payload: JsValue, endPoint: TDDUris): HttpResponse[String] =
    Http(endPoint.uri)
      .headers("content-type" -> "application/json")
      .postData(Json.stringify(payload))
      .asString


  override def doGet(endPoint: TDDUris): HttpResponse[String] =
    Http(endPoint.uri)
      .headers("content-type" -> "application/json")
      .asString
}
