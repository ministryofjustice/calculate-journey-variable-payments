# Calculate Journey Variable Payments

[![CircleCI](https://circleci.com/gh/ministryofjustice/calculate-journey-variable-payments/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/calculate-journey-variable-payments)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/calculate-journey-variable-payments/status)](https://quay.io/repository/hmpps/calculate-journey-variable-payments)
## Prerequisites

- [JDK 17](https://openjdk.java.net/projects/jdk/17/)
- [Docker and Docker compose](https://docs.docker.com/get-docker/)
- [Intellij IDEA](https://www.jetbrains.com/idea/)
  - [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin
- Chromedriver for running the integration tests

## Building

To build (append `-x test` to build without running tests):
```bash
$ ./gradlew clean build
```

## Running the integration tests locally from the command line

_Note: you will need several terminal sessions for this._

Start up the dependencies followed by the application and then running the integration tests:

Run docker-compose.  This will start up the dependent containers e.g. Postgres, Auth etc:
```bash
$ docker-compose down --remove-orphans
$ docker-compose up
```

Start the application:
```bash
$ SPRING_PROFILES_ACTIVE=dev SERVER_SERVLET_SESSION_COOKIE_SECURE=false ./gradlew bootRun
```

Run the integration tests:
```bash
$ ./gradlew clean testPlayWrightIntegration
```

## Running the containerised service and its dependent services locally

You can run the latest version of the application using with Docker compose (note this runs the app in a container, you may need to rebuild it. See section on integration tests for an example) :

```bash
$ docker-compose down --remove-orphans
$ docker-compose -f docker-compose.yml -f docker-compose-cjvp.yml up
```

## Running dependent services (containers) locally when developing:

You can run the dependent services locally using with Docker compose (note this runs the app in a container, so you may need to rebuild it, see section on integration tests for an example) :

```bash
$ docker-compose up
```

*Note: to log into the application (via the redirect to the HMPPS auth service) your user will need the PECS_JPC role assigned. A hmpps-auth in-memory user has been set up with this role to help with this 'jpc_user'.*

The above command will launch:

- [Localstack](https://github.com/localstack/localstack) (which is used to mock Amazon S3)
- Postgres

Next, head over to Intellij, locate `src/main/kotlin/JpcApplication.kt`, right click it and in `More Run/Debug`, click `Modify Run Configuration...`.

In the window that pops up, open the `EnvFile` tab, check the box next to `Enable EnvFile` and at the bottom of the box below, click the plus symbol and then .env file and add `.env` (on macOS, this file will be hidden, `cmd + shift + .` will make it appear) and then click `OK`.

You can run the application from Intelli with `Run > Run`.

If you prefer to run the app from the command line, you can do so from the root of the project with:

```bash
export $(cat .env | xargs)  # If you want to set or update the current shell environment
./gradlew bootRun
```

### Data for pricing journeys/moves

Pricing data makes its way into the service via two mechanisms. Via a daily CRON job (which at time of writing runs in 
the early hours of the morning) and via a manual process (with the view the manual process will be going away). The 
CRON job is configured in the helm config of this project via the **CRON_IMPORT_REPORTS** environment variable in the
helm config [here](helm_deploy).

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
   important to understand the reports being pulled are based on the previous day. If there are any missing files for
   the day it runs then the next time the import runs it will run from the point at which there were any missing files in an attempt
   to recover, this can be disabled via the environment variable **IMPORT_REPORTS_BACKDATE_ENABLED** in the Helm config
   [here](helm_deploy).

## How-to
- [Manually import supplier journey prices in bulk](scripts/bulk_price_upload/README.md).
- [Manually import reporting data](scripts/backfill_reports/README.md).
- [Manually process historic moves](scripts/process_historic_moves/README.md).
- [Apply K8s job for the daily reporting feed](k8_jobs/README.md).

### Automatic mapping of Schedule 34 locations (from BaSM)

Periodically new locations are added to BaSM. In an effort to keep in sync with BaSM and reduce the amount of work
the users have to do with mapping new locations, a CRON job runs daily to retrieve the previous days locations added to
BaSM and automatically add them to CJVP (if there are any).  If a location already exists in CJVP it will simply be
ignored, nothing is overwritten, only new locations will be added.

The CRON job is configured in the helm config of this project via the **CRON_AUTOMATIC_LOCATION_MAPPING** environment variable [here](https://github.com/ministryofjustice/calculate-journey-variable-payments/tree/main/helm_deploy).

### Feature toggles/flags

The following feature toggles/flags are in place for the application in the Helm config [here](helm_deploy):

- FEATURE_FLAG_INCLUDE_RECONCILIATION_MOVES 
  > Setting this to "true" includes an extra tab on the spreadsheet for moves that have been
categorised but cannot be priced. This is because whilst they have a move type they are missing some key information 
which prevents them from being priced e.g. date(s). This would only be enabled (in prod) if we are trying to reconcile 
moves with the supplier, it is not used by the end users per se.

### Common gradle tasks 
To list project dependencies, run:
```bash
$ ./gradlew dependencies
```

To check for dependency updates, run:
```bash
$ ./gradlew dependencyUpdates --warning-mode all
```

To run an OWASP dependency check, run:
```bash
$ ./gradlew clean dependencyCheckAnalyze --info
```

To upgrade the gradle wrapper version, run:
```bash
$ ./gradlew wrapper --gradle-version=<VERSION>
```
To automatically update project dependencies, run:

_Note: this should be used with caution, there may be genuine reasons certain dependencies are at a particular versions._

```bash
$ ./gradlew useLatestVersions
```
