SppPublicReportClient
=====================

Java client for parsing the public Southwest Power Pool (SPP) .csv reports.

For now, I've now set up a Maven repository on Github that can be used to reference these libraries. To add these libraries as a dependency to your project using maven:

```xml
<repositories>
	<repository>
		<id>SppPublicReportClient-mvn-repo</id>
		<url>https://raw.github.com/r24mille/SppPublicReportClient/mvn-repo/</url>
		<snapshots>
			<enabled>true</enabled>
			<updatePolicy>always</updatePolicy>
		</snapshots>
	</repository>
</repositories>
<dependencies>
	<dependency>
		<groupId>name.reidmiller</groupId>
		<artifactId>spp-public-report-client</artifactId>
		<version>0.0.1-SNAPSHOT</version>
	</dependency>
</dependencies>
```

This open source project was started by Reid Miller, a graduate student at the University of Waterloo. The libraries have not been reviewed or approved by SPP. I'm just working with public data and sharing my work.
