
# customs-exports-internal-frontend

This service is a frontend service for Exports Movements UI, for HMRC employees.
Its responsibility is to allow internal users submit Movements and Consolidations for their Export Declarations.

## Prerequisites
This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at a [JRE](https://www.java.com/en/download/) to run and a JDK for development.

This service uses MongoDB.

This service depends on other services. The easiest way to set up required microservices is to use Service Manager and profiles from [service-manager-config](https://github.com/hmrc/service-manager-config/) repository:

```bash
sm2 --start CDS_EXPORTS_INTERNAL_ALL
```

### Running the application
In order to run the application you need to have SBT installed. Then, it is enough to start the service with:

`sbt run`

### Testing the application
This repository contains unit tests for the service. In order to run them, simply execute:

`sbt test`

### Logging in
This UI is authenticated via the stride-auth stub. To login you will need to enter the following details on the stride-auth login form:
* Stride Identity Provider Login Stub URL Local = [Local IMS Stride Login](http://localhost:6799/customs-exports-internal/consignment-query)
* Stride Identity Provider Login Stub URL QA = [QA IMS Stride Login](https://admin.qa.tax.service.gov.uk/customs-exports-internal/consignment-query)
* Stride Identity Provider Login Stub URL Staging = [Staging IMS Stride Login](https://admin.staging.tax.service.gov.uk/customs-exports-internal/consignment-query)
* PID = 1234 
* Roles = write:customs-inventory-linking-exports

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

## ILE Query

A flow diagram for ILE Query is available on [Confluence](https://confluence.tools.tax.service.gov.uk/x/0wm9Mw).
