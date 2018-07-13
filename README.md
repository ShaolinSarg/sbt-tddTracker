TDD Tracker
===========

An sbt plugin that sends test data to the [TDD metric project](https://github.com/ShaolinSarg/tdd-trainer) when writing code using Scala and SBT.

Use the collated statistics to help you focus on making your TDD feedback cycle as tight as possible.


Installing the plugin
---------------------
sbt> publishLocal

~/.sbt/1.0/plugins/plugins.sbt

`addSbtPlugin("sarginson" % "sbttddmetricstracker" % "0.1.0")`


SBT input tasks
---------------

The following 3 STB input tasks are all you need to start, stop and get the metrics for a TDD coding session

| task       | description |
| ---------- | ----------- |
| tddStart   | Start a tdd session |
| tddDetails | Returns the metrics for the current tdd session |
| tddEnd     | Ends the current tdd session |

> N.B. Once the TDD session is started, you need to run the tests.  I would recommend using the `~testQuick` sbt task to continually run your tests automatically on save so the timings of the RGR cycles are as accurate as possible.

tddStart
--------
Starts a new TDD session by sending the following details to the TDD metric project:
* Start time
* project base directory
* file type of the files to watch

tddDetails
----------
Prints out the TDD metrics for the current session in the SBT terminal.

tddEnd
----------
Ends the current TDD session by removing the stored TDD session ID from the SBT session value

