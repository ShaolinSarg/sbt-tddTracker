package sarginson.sbt.domain

import java.util.Date

case class TestSnapshot(
  timestamp: Date,
  failingTestCount: Int,
  failingTestNames: List[String])
