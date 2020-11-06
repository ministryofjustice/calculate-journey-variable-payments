# Calculate Journey Variable Payments

[![CircleCI](https://circleci.com/gh/ministryofjustice/calculate-journey-variable-payments/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/calculate-journey-variable-payments)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/calculate-journey-variable-payments/status)](https://quay.io/repository/hmpps/calculate-journey-variable-payments)
## Prerequisites

- [Docker and Docker compose](https://docs.docker.com/get-docker/)
- [Intellij IDEA](https://www.jetbrains.com/idea/)

## Building

To build (append `-x test` to build without running tests):
```
./gradlew clean build
```

## Running locally

You can run the dependencies for the projects with Docker compose:

```bash
docker-compose up
```

The command will launch:

- [Localstack](https://github.com/localstack/localstack) (which is used to mock Amazon S3)
- Postgres

Next, head over to Intellij and set the environment variables from .env into the Run Configuration `Run > Edit configurations`:

![Configure the Spring Profile in Intellij](assets/environment_variables.png)

You can run the application from Intelli with `Run > Run`.

If you prefer to run the app from the command line, you can do so from the root of the project with:

```bash
export $(cat .env | xargs)  # If you want to set or update the current shell environment
./gradlew bootRun '
```
### Running imports from the commandline

Importing locations and prices:

```bash
export $(cat .env | xargs)  # If you want to set or update the current shell environment

java -jar app.jar --spring.main.web-application-type=none --import-locations-and-prices
```

Importing supplier reports for given dates:

```bash
export $(cat .env | xargs)  # If you want to set or update the current shell environment

java -jar app.jar --spring.main.web-application-type=none --import-supplier-reports=2020-10-01,2020-10-02
```


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
