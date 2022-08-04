# Bulk price uploads for supplier journey prices

The bulk price uploads feature exists to assist the commercial team with adding large volumes of journey prices to the service.
This is not a feature we use very often but it does have its place.

The journey price data is sent to us from the commercial team via email in an Excel spreadsheet format. Upon receipt there
are two main steps for bulk prices uploads with a possible third option:

1. Using the shell script **upload_journey_prices_spreadsheet_to_s3.sh** upload the spreadsheet(s) to the appropriate environments (dev, preprod, prod) AWS S3 bucket. It is from here where the prices are fed into the service.
2. Go onto one of the services pods and run the **price-import** command to load the prices from step one above into the system.
3. (optional) Run a bulk price update via the frontend. Technically this can be done by the end users however you may wish
   to do this for testing purpose in preprod for example.

**IMPORTANT:**

- IT IS ADVISABLE TO DO A DRY RUN IN THE PRE-PRODUCTION ENVIRONMENT BEFORE DOING THIS IN THE PRODUCTION ENVIRONMENT.
- CARE SHOULD BE TAKEN WHEN RUNNING ANY COMMANDS IN PRODUCTION.
- A JOURNEY PRICE WILL ONLY ADDED IF IT IS NOT ALREADY PRICED, EXISTING JOURNEY PRICES ARE NOT UPDATED.
- TO ACTUALLY IMPORT THE DATA YOU WILL NEED TO GO ONTO ONE OF THE RUNNING SERVICE PODS AND RUN A SHELL COMMAND.

### How to upload journey price spreadsheets to S3 in preparation for running the price-import using the script in this folder.

_Note the script relies on naming of the spreadsheets to upload to be serco-prices.xlsx and geoamey-prices.xlsx and for 
the files to be in the same directory where the script is executed from. There are some examples already included in the folder._

Serco example on development environment from a local terminal session

```bash
$ ./upload_journey_prices_spreadsheet_to_s3.sh dev serco
```

GEOAmey example on development environment from a local terminal session

```bash
$ ./upload_journey_prices_spreadsheet_to_s3.sh dev geo
```

### How to run the **price-import** command from a pod

**IMPORTANT:**
- Make sure the year parameter is set correctly. This is the contractual effective year for the prices e.g. for Sept 2021 to Aug 2022 the year would be 2021.

Serco example

```bash
$ java -jar app.jar --spring.main.web-application-type=none --price-import --supplier=SERCO --year=2021
```

GEOAmey example

```bash
$ java -jar app.jar --spring.main.web-application-type=none --price-import --supplier=GEOAMEY --year=2021
```

When the prices are imported you will be be given some feedback in the logs as to the success rate e.g. how many
prices were added and any errors.