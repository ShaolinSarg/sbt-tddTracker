package sarginson.sbt.connectors

sealed trait TDDUris {
  val baseUri: String = "http://localhost:3000"
  val uri: String
}

case object SessionsUri extends TDDUris {
  override val uri: String = baseUri + "/sessions"
}

case class SnapshotsUri(sessionId: Int) extends TDDUris {
  override val uri: String = baseUri + s"/sessions/$sessionId/snapshots"
}

case class StatsUri(sessionId: Int) extends TDDUris {
  override val uri: String = baseUri + s"/sessions/$sessionId/stats"
}