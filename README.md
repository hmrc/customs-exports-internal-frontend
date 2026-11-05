# customs-exports-internal-frontend

## About
This service provides the frontend for the Exports Movements UI, an internal application used by HMRC employees.

It allows internal users to submit export movements and consolidations associated with their export declarations.

| Key                             | Value                                                                                                        | 
|---------------------------------|--------------------------------------------------------------------------------------------------------------|
| Digital service                 | CDS Exports                                                                                                  |
| Local port                      | `6799`                                                                                                       |
| Base path                       | `/customs-exports-internal`                                                                                  |
| Back-end                        | [customs-declare-exports-movements](https://github.com/hmrc/customs-declare-exports-movements) (port `6797`) |
| Acceptance tests                | [movements-internal-ui-acceptance-tests](https://github.com/hmrc/movements-internal-ui-acceptance-tests)     |
| Performance tests               | [cds-movements-performance-tests](https://github.com/hmrc/cds-movements-performance-tests)                   |
| Stubs used in Local and Staging | [customs-inventory-linking-exports-stub](https://github.com/hmrc/customs-inventory-linking-exports-stub)     |

## How to Run This Service

### Prerequisites
Use [Service Manager](#service-manager-profiles) to start the services required by CDS Exports.

From the terminal, run:

```bash
sm2 --start CDS_EXPORTS_INTERNAL_ALL
```

### Running the Service Locally
To run this service from source (for example, to test your own changes), stop the instance started by Service Manager and run it with sbt.

From the root of this repository, run:

```bash
sm2 --stop CUSTOMS_EXPORTS_INTERNAL_FRONTEND
sbt run
```
The service starts on port `6799`. 

To access the service, navigate to http://localhost:6799/customs-exports-internal/consignment-query, and log in using the [enrolment details](#enrolment-required).

## How to Test This Service

### Local

#### Unit Tests

```bash
sbt test
```
#### Pre-Push Check
There is a script called `precheck.sh` that runs all tests, examines their coverage and checks if all the files are properly formatted.
It is good practice to run it just before pushing to GitHub.

```bash
./precheck.sh
```

#### Acceptance Tests (Smoke and Regression)
The acceptance tests for this service are in [movements-internal-ui-acceptance-tests](https://github.com/hmrc/movements-internal-ui-acceptance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/movements-internal-ui-acceptance-tests#how-to-run-the-tests).

#### Performance tests
The performance tests for this service are in [cds-movements-performance-tests](https://github.com/hmrc/cds-movements-performance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/cds-movements-performance-tests#how-to-run-the-tests).

### Manual Testing in QA and Staging
After a successful [deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-exports-internal-frontend-pipeline/), perform manual verification in QA and/or Staging using the [manual testing instructions](https://github.com/hmrc/movements-internal-ui-acceptance-tests#manual-testing-in-qa-and-staging).

## Service Catalogue
- [customs-exports-internal-frontend in the MDTP Catalogue](https://catalogue.tax.service.gov.uk/repositories/customs-exports-internal-frontend)

## Jenkins Pipeline
Front End Build and Deployment Pipeline
-  [Build pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-exports-internal-frontend/)
-  [Deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-exports-internal-frontend-pipeline/)

The acceptance test Jenkins jobs (smoke and regression) are listed in the [movements-internal-ui-acceptance-tests README](https://github.com/hmrc/movements-internal-ui-acceptance-tests#jenkins-builds).

The performance test Jenkins jobs are listed in the [cds-movements-performance-tests README](https://github.com/hmrc/cds-movements-performance-tests#jenkins-builds).

## Service Manager Profiles
These profiles are defined in [service-manager-config](https://github.com/hmrc/service-manager-config).

| Profile                              | Use                                                                                                                 | Services started                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|--------------------------------------|---------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CDS_EXPORTS_INTERNAL_ALL`           | Running and developing this service locally                                                                         | `CUSTOMS_EXPORTS_INTERNAL_FRONTEND`, `CUSTOMS_DECLARE_EXPORTS_MOVEMENTS`, `AUTH`, `AUTH_LOGIN_API`, `CENTRALISED_AUTHORISATION_SERVER`, `AUTH_LOGIN_STUB`, `STRIDE_IDP_STUB`, `STRIDE_AUTH`, `STRIDE_AUTH_FRONTEND`, `IDENTITY_VERIFICATION`, `API_SUBSCRIPTION_FIELDS`, `CUSTOMS_API_HODS_STUBS`, `CUSTOMS_INVENTORY_LINKING_EXPORTS`, `CUSTOMS_INVENTORY_LINKING_EXPORTS_STUB`, `CUSTOMS_NOTIFICATION`, `CUSTOMS_NOTIFICATIONS_RECEIVER_STUB`, `CUSTOMS_NOTIFICATION_GATEWAY`, `API_NOTIFICATION_QUEUE`, `USER_DETAILS` |
| `CDS_EXPORTS_INTERNAL_MOVEMENTS_ATS` | Running the [acceptance tests](https://github.com/hmrc/movements-internal-ui-acceptance-tests#how-to-run-the-tests) | `CUSTOMS_EXPORTS_INTERNAL_FRONTEND`, `CUSTOMS_DECLARE_EXPORTS_MOVEMENTS`, `AUTH`, `BAS_GATEWAY`, `BAS_GATEWAY_FRONTEND`, `STRIDE_IDP_STUB`, `STRIDE_AUTH`, `STRIDE_AUTH_FRONTEND`, `CUSTOMS_INVENTORY_LINKING_EXPORTS_STUB`, `USER_DETAILS`,                                                                                                                                                                                                                                                                              |
| `CDS_EXPORTS_ALL`                    | All CDS Exports services (declarations, movements and internal)                                                     | See [service-manager-config](https://github.com/hmrc/service-manager-config/blob/main/profiles.json)                                                                                                                                                                                                                                                                                                                                                                                                                      |


## Endpoints

| Method   | Path                             | Purpose                                                                | Sample request (query / form body)                                                                                                                                                                                                                                           | Response                                                             |
|----------|----------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| GET      | `/`                              | Application entry point; displays the root page                        | –                                                                                                                                                                                                                                                                            | `303` → `/consignment-query`                                         |
| GET      | `/consignment-query`             | Display the ILE consignment query form                                 | –                                                                                                                                                                                                                                                                            | `200` (page rendered)                                                |
| GET      | `/find-cds-consignment`          | Display the form to find a CDS consignment                             | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| GET      | `/find-cds-consignment/:ucr`     | Retrieve consignment information using a UCR                           | `/find-cds-consignment/GB123456789012-ABC123`                                                                                                                                                                                                                                | `200`                                                                |
| POST     | `/find-cds-consignment`          | Submit a CDS consignment search                                        | `ucr=3GB986007773125-INVOICE123`                                                                                                                                                                                                                                             | `303` → `/find-cds-consignment/:ucr` (invalid form: `400`)           |
| GET      | `/choice  `                      | Display the declaration/action choice page                             | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/choice  `                      | Submit the selected declaration/action                                 | `choice=arrival`(or `departure`, `retrospectiveArrival`, `associateUCR`, `disassociateUCR`, `shutMUCR`)                                                                                                                                                                      | `303`                                                                |
| GET      | `/manage-mucr`                   | Display the manage MUCR page                                           | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/manage-mucr`                   | Submit a manage MUCR request                                           | `choice=associateThisToAnotherMucr` or `choice=associateAnotherUcrToThis`                                                                                                                                                                                                    | `303` → `/mucr-options` or `/associate-ucr` (invalid form: `400`)    |
| GET      | `/mucr-options`                  | Display available MUCR options                                         | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/mucr-options`                  | Submit the selected MUCR option                                        | Create: `createOrAdd=create&newMucr=GB123456789012-ABC123&existingMucr=` <br> Add: `createOrAdd=add&newMucr=&existingMucr=GB123456789012-ABC123`                                                                                                                             | `303` → `/associate-ucr-summary` (invalid form: `400`)               |
| GET      | `/associate-ucr`                 | Display the Associate UCR page                                         | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/associate-ucr`                 | Submit a request to associate a UCR with a MUCR                        | DUCR: `kind=ducr&ducr=3GB986007773125-INVOICE123` <br> MUCR: `kind=mucr&mucr=GB123456789012-ABC123`                                                                                                                                                                          | `303` → `/associate-ucr-summary` (invalid form: `400`)               |
| GET      | `/associate-ucr-summary`         | Display a summary of the UCR association                               | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/associate-ucr-summary`         | Submit the UCR association                                             | DUCR: `kind=ducr&ducr=3GB986007773125-INVOICE123` <br> MUCR: `kind=mucr&mucr=GB123456789012-ABC123`                                                                                                                                                                          | `303` → `/movement-confirmation`                                     |
| GET      | `/disassociate-ucr-summary`      | Display a summary of the UCR disassociation                            | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/disassociate-ucr-summary`      | Submit the UCR disassociation                                          | DUCR: `kind=ducr&ducr=3GB986007773125-INVOICE123` <br> MUCR: `kind=mucr&mucr=GB123456789012-ABC123`                                                                                                                                                                          | `303` → `/movement-confirmation`                                     |
| GET      | `/specific-date-and-time`        | Display the page for entering a specific date and time                 | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/specific-date-and-time`        | Save the specific date and time                                        | `choice=userDateTime` or `choice=currentDateTime`                                                                                                                                                                                                                            | `303` → `/movement-details` or `/location` (invalid form: `400`)     |
| GET      | `/movement-details`              | Display the movement details page                                      | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/movement-details`              | Save movement details                                                  | Arrival: `dateOfArrival.day=15&dateOfArrival.month=10&dateOfArrival.year=2026&timeOfArrival.hour=14&timeOfArrival.minute=30` <br> Departure: `dateOfDeparture.day=15&dateOfDeparture.month=10&dateOfDeparture.year=2026&timeOfDeparture.hour=14&timeOfDeparture.minute=30`   | `303` → `/location` (invalid form: `400`)                            |
| GET      | `/location`                      | Display the goods location page                                        | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/location`                      | Save the goods location                                                | `code=GBAU1234U123456`                                                                                                                                                                                                                                                       | `303` → `/summary` or `/goods-departed` (invalid form: `400`)        |
| GET      | `/goods-departed`                | Display the goods departed page                                        | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/goods-departed`                | Save the good departed information                                     | `departureLocation=outOfTheUk` or `departureLocation=backIntoTheUk`                                                                                                                                                                                                          | `303` → `/transport` or `/summary` (invalid form: `400`)             |
| GET      | `/transport`                     | Display the transport details page                                     | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/transport`                     | Save transport details                                                 | `modeOfTransport=3&nationality=GB&transportId=AB12345`                                                                                                                                                                                                                       | `303` → `/summary`                                                   |
| GET      | `/summary`                       | Display the arrival/departure movement summary                         | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/summary`                       | Submit the movement request                                            | –                                                                                                                                                                                                                                                                            | `303` → `/movement-confirmation` (invalid form: `400`)               |
| GET      | `/movement-confirmation`         | Display confirmation of the movement request                           | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| GET      | `/shut-mucr-summary`             | Display the summary for shutting a MUCR                                | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| POST     | `/shut-mucr-summary`             | Confirm or submit the MUCR shut request                                | `mucr=GB123456789012-ABC123`                                                                                                                                                                                                                                                 | `303` → `/movement-confirmation`                                     |
| GET      | `/submissions`                   | Display submitted requests or submissions                              | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| GET      | `/notifications/:conversationId` | List notifications associated with a conversation                      | `/notifications/abc123`                                                                                                                                                                                                                                                      | `200`                                                                |
| GET      | `/sign-out`                      | Sign out the user, using the specified sign-out reason                 | `?signOutReason=UserAction` (or `SessionTimeout`)                                                                                                                                                                                                                            | `303` → `bas-gateway` sign-out                                       |
| GET      | `/we-signed-you-out`             | Display the session-timeout sign-out page                              | –                                                                                                                                                                                                                                                                            | `200`                                                                |
| GET      | `/you-have-signed-out`           | Display the user-initiated sign-out confirmation page                  | –                                                                                                                                                                                                                                                                            | `200`                                                                |

## Enrolment Required

This UI is authenticated via the stride-auth stub. To log in you will need to enter the following details in the correct environment:

| Environment | Service URL                                                                                                        |
|-------------|--------------------------------------------------------------------------------------------------------------------|
| Local       | [Open the service in Local](http://localhost:6799/customs-exports-internal/consignment-query)                      |
| QA          | [Open the service in QA](https://admin.qa.tax.service.gov.uk/customs-exports-internal/consignment-query)           |
| Staging     | [Open the service in Staging](https://admin.staging.tax.service.gov.uk/customs-exports-internal/consignment-query) |

Enter the required details as shown below and select Submit to continue.

| Field     | Value                                        |
|-----------|----------------------------------------------|
| PID       | `1234`                                       |
| Roles     | `write:customs-inventory-linking-exports`    |

> **Note:** Before testing in QA, make sure the EORINumber being used has a valid CDS subscription.

## Developer Notes
A flow diagram for ILE Query is available on [Confluence](https://confluence.tools.tax.service.gov.uk/x/0wm9Mw).

### Scalafmt
The code is formatted with [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html#sbt), using the rules in `.scalafmt.conf`.

Check that all project files are formatted as expected:

```bash
sbt scalafmtCheckAll scalafmtSbtCheck
```

Format `*.sbt` and `project/*.scala` files:

```bash
sbt scalafmtSbt
```

Format all project files:

```bash
sbt scalafmtAll
```

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").