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

*Note: to log into the application (via the redirect to the HMPPS auth service) your user will need the PECS_JPC role assigned. A hmpps-auth in-memory user has been set up with this role to help with this 'jpc_user'.*

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
### Running imports of locations, prices and reporting data

Start by running the following from the command line to take you into the Spring shell.

```bash
export $(cat .env | xargs) # Only run this if you want to set or update the current local environment

java -jar app.jar --spring.shell.interactive.enabled=true --spring.main.web-application-type=none
```
Once in the Spring shell the following commands for importing data are available:
```
import-locations
```
```
import-prices --supplier SERCO/GEOAMEY
```
```
import-reports --from YYYY-MM-DD --to YYYY-MM-DD
```
Typing help in the shell will also list the available commands.  TAB autocomplete is also available.

To exit from the shell simply type: exit

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
