TDD Tracker
===========

An sbt plugin that sends test data to the [TDD metric project](https://github.com/ShaolinSarg/tdd-trainer) when writing code using Scala and SBT.

Use the collated statistics to help you focus on making your TDD feedback cycle as tight as possible.


SBT input tasks
---------------

The following 3 STB input tasks are all you need to start, stop and get the metrics for a TDD coding session

| task       | description |
| ---------- | ----------- |
| tddStart   | Start a tdd session |
| tddDetails | Returns the metrics for the current tdd session |
| tddEnd     | Ends the current tdd session |


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