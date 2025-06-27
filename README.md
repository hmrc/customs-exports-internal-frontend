
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
* Stride Identity Provider Login Stub URL QA = [QA IMS Stride Login](https://admin.qa.tax.service.gov.uk/stride-idp-stub/auth-request?SAMLRequest=fVHRboIwFP2Vpu8FBAXWCIaMmZnosiDsYW8FqjaDlvUW4%2F5%2BDCVxL7719p5z7znnLleXtkFnrkEoGeGZ5WDEZaVqIY8RLvI1CfEqXgJrG7ejSW9OMuPfPQeDBqIEeu1EuNeSKgYCqGQtB2oquk92W%2BpaDu20MqpSDUYJANdmWPWsJPQt13uuz6LiRbaN8MmYDqhts7oV0jLsYsG1ax3V2eq%2FbDBa1NxmgwqiOXTDDI5ROogRkpnRwDTjANap1dXEZPUB7AZsjDZphHdp%2Fk5qf%2BZ5ZeiTsnyak%2Fk84MPL5yTwHP%2BpLF13Ec4GOEDPNxIMkybCruMuiOMTd5HPAup61PGtIAw%2FMfqYEhz84ltedCTr%2B6Ae58SmdDBaK90y8xj%2B9yNqchihlEsjzA%2BOi%2ByNjgb3ebZJX0hS5K9L%2B15QfCv%2F3zP%2BBQ%3D%3D&RelayState=successURL%3D%252Fcustoms-exports-internal%252Fnotifications%252Ffe576898-2f76-4f91-bcbd-f6093b9c86a8%26failureURL%3D%252Fstride%252Ffailure)
* PID = 1234 
* Roles = write:customs-inventory-linking-exports

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

## ILE Query

A flow diagram for ILE Query is available on [Confluence](https://confluence.tools.tax.service.gov.uk/x/0wm9Mw).
