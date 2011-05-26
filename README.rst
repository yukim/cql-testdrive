About
============

The experimental webapp using Apache Cassandra(http://cassandra.apache.org) CQL JDBC driver with Jetty and Commons DBCP.

However, current version(v0.8.0) of CQL JDBC driver does not work because of UnsupportedOperationException thrown by the driver.

How to run
============

Install the latest version of Maven(http://maven.apache.org). Then type::

  mvn jetty:run

