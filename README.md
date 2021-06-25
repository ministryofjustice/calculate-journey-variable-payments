# Calculate Journey Variable Payments

[![CircleCI](https://circleci.com/gh/ministryofjustice/calculate-journey-variable-payments/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/calculate-journey-variable-payments)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/calculate-journey-variable-payments/status)](https://quay.io/repository/hmpps/calculate-journey-variable-payments)
## Prerequisites

- [Docker and Docker compose](https://docs.docker.com/get-docker/)
- [Intellij IDEA](https://www.jetbrains.com/idea/)
  - [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin

## Building

To build (append `-x test` to build without running tests):
```
./gradlew clean build
```

## Running the integration tests

Rebuild the docker image locally (perhaps after changes to the project), run:
```bash
docker build -t quay.io/hmpps/calculate-journey-variable-payments:latest .
```

Start up the application and its dependencies:
```bash
docker-compose up
```

In a separate terminal window:
```bash
./gradlew clean testIntegration
```

## Running locally

You can run the latest version of the application using with Docker compose (note this runs the app in a container so you may need to rebuild it, see section on integration tests for an example) :

```bash
docker-compose up
```

*Note: to log into the application (via the redirect to the HMPPS auth service) your user will need the PECS_JPC role assigned. A hmpps-auth in-memory user has been set up with this role to help with this 'jpc_user'.*

The command will launch:

- [Localstack](https://github.com/localstack/localstack) (which is used to mock Amazon S3)
- Postgres

Next, head over to Intellij, locate `src/main/kotlin/JpcApplication.kt`, right click it and in `More Run/Debug`, click `Modify Run Configuration...`.

In the window that pops up, open the `EnvFile` tab, check the box next to `Enable EnvFile` and at the bottom of the box below, click the plus symbol and then .env file and add `.env` (on macOS, this file will be hidden, `cmd + shift + .` will make it appear) and then click `OK`.

You can run the application from Intelli with `Run > Run`.

If you prefer to run the app from the command line, you can do so from the root of the project with:

```bash
export $(cat .env | xargs)  # If you want to set or update the current shell environment
./gradlew bootRun '
```

### Data for pricing journeys/moves

Pricing data makes its way into the service via two mechanisms. Via a daily CRON job (which at time of writing runs in 
the early hours of the morning) and via a manual process (with the view the manual process will be going away). The 
CRON job is configured in the helm config of this project via the **CRON_IMPORT_REPORTS** environment variable.

The data itself falls into three distinct types:

1. Schedule 34 location data - this represents the allowed contracted locations for moves within Book a Secure Move, in 
   Excel spreadsheet format. Upon receipt of this spreadsheet it is (manually) uploaded to a secure S3 bucket.
2. Supplier pricing data - this represents the agreed prices with the suppliers for moves 'from' and 'to' the schedule 
   34 locations i.e. it is dependent on the Schedule 34 locations, in Excel spreadsheet format. There is a separate 
   pricing spreadsheet for each supplier. Upon receipt of a supplier price spreadsheet it is (manually) uploaded to a 
   secure S3 bucket.
3. Move reporting data - these are JSON files representing all the of the supplier moves, stored in a secure S3 bucket. 
   These files are uploaded automatically to S3 by an independent process not managed by this application.  The files 
   are pulled into the application via the daily early morning CRON job to be used for calculating journey prices. It is
   important to understand that the reports (are always) being pulled based on the previous day.

### Manually importing supplier prices and reporting data

**IMPORTANT:**

- EXTREME CARE SHOULD BE TAKEN WHEN RUNNING PRICING IMPORTS IN PRODUCTION, ANY EXISTING PRICES WILL BE REMOVED!!
- TO MANUALLY IMPORT DATA IN PRODUCTION YOU WILL NEED TO GO DIRECTLY ONTO ONE OF THE KUBE PODS.
- MANUALLY IMPORTING REPORTING DATA IS MAINLY TO SUPPORT LOADING OF BACK-FILLED SUPPLIER REPORTING DATA. IT CAN BE SLOW, 
  IF YOU HAVE TO IMPORT A LOT OF DATA. ALLOW PLENTY OF TIME AND ALSO CONSIDER TIME OF EXECUTION I.E OUT OF HOURS.

Start by running the following from the command line to take you into the Spring shell.

```bash
export $(cat .env | xargs) # Only run this if you want to set or update the current local environment

java -jar app.jar --spring.shell.interactive.enabled=true --spring.main.web-application-type=none
```
Once in the Spring shell the following commands for importing and generating pricing data are available:
```
# Import the supplier price spreadsheet that has been uploaded to S3

import-prices --supplier SERCO/GEOAMEY
```
```
# Import the reporting data for the supplied dates from S3

import-reports --from YYYY-MM-DD --to YYYY-MM-DD
```
```
# Bulk price updates for the following effective year

add-next-years-prices --supplier SERCO/GEOAMEY --multiplier 1.12
```
Typing help in the shell will also list the available commands.  TAB autocomplete is also available.

To exit from the shell simply type: exit

### Automatic mapping of Schedule 34 locations (from BaSM)

Periodically new locations are added to BaSM. In an effort to keep in sync with BaSM and reduce the amount of work
the users have to do with mapping new locations, a CRON job runs daily to retrieve the previous days locations added to
BaSM and automatically add them to CJVP (if there are any).  If a location already exists in CJVP it will simply be
ignored, nothing is overwritten, only new locations will be added.

The CRON job is configured in the helm config of this project via the **CRON_AUTOMATIC_LOCATION_MAPPING** environment variable.

### Common gradle tasks 
To list project dependencies, run:
```
./gradlew dependencies
```

To check for dependency updates, run:
```
./gradlew dependencyUpdates --warning-mode all
```

To run an OWASP dependency check, run:
```
./gradlew clean dependencyCheckAnalyze --info
```

To upgrade the gradle wrapper version, run:
```
./gradlew wrapper --gradle-version=<VERSION>
```
To automatically update project dependencies, run:
```
./gradlew useLatestVersions
```
