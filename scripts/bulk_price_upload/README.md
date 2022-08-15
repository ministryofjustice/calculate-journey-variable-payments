# Bulk price uploads for supplier journey prices

The bulk price uploads feature exists to assist the commercial team with adding large volumes of journey prices to the service.
This is not a feature we use very often but it does have its place.

The journey price data is sent to us from the commercial team via email in an Excel spreadsheet format. Upon receipt these
are steps for bulk prices uploads with a possible third option:

1. Take a RDS snapshot of the database using the **take-manual-rds-snapshot.sh** script where the journey prices are going to be loaded.
2. Using the shell script **upload-journey-prices-spreadsheet-to-s3.sh** upload the spreadsheet(s) to the appropriate environments (dev, preprod, prod) AWS S3 bucket. It is from here where the prices are fed into the service.
3. Use the script **run-bulk-price-upload.sh** to load the prices from step one above into the system.
4. (optional) Run a bulk price update via the frontend. Technically this can be done by the end users however you may wish
   to do this for testing purpose in preprod for example.
5. (optional) delete the backup RDS snapshot.

**IMPORTANT:**

- IT IS ADVISABLE TO DO A DRY RUN IN THE PRE-PRODUCTION ENVIRONMENT BEFORE DOING THIS IN THE PRODUCTION ENVIRONMENT.
- CARE SHOULD BE TAKEN WHEN RUNNING ANY COMMANDS IN PRODUCTION.
- A JOURNEY PRICE WILL ONLY ADDED IF IT IS NOT ALREADY PRICED, EXISTING JOURNEY PRICES ARE NOT UPDATED.

### How to upload journey price spreadsheets to S3 in preparation for running the price-import using the script in this folder.

_Note the script relies on naming of the spreadsheets to upload to be serco-prices.xlsx and geoamey-prices.xlsx and for 
the files to be in the same directory where the script is executed from. There are some examples already included in the folder._

Example taking a RDS snapshot

_List the snapshots before_

```bash
$ ./list-rds-snapshots.sh dev
```

_Take the snapshot and make a note of the snapshot identifier_

```bash
$ ./take-manual-rds-snapshot.sh dev
```

_Check the snapshot has been created and is in "Status" : "available"_

```bash
$ ./list-rds-snapshots.sh dev
```

Serco example on development environment from a local terminal session

```bash
$ ./upload-journey-prices-spreadsheet-to-s3.sh dev serco
```

GEOAmey example on development environment from a local terminal session

```bash
$ ./upload-journey-prices-spreadsheet-to-s3.sh dev geo
```

### How to run the actual bulk price upload part

**IMPORTANT:**
- Make sure the year parameter is set correctly. This is the contractual effective year for the prices e.g. for Sept 2021 to Aug 2022 the year would be 2021.

It can be helpful to run this SQL before and after the actual import. This is a simple way of seeing how many prices have been added bar reading the logs.
You will need to port forward onto the DB to do this though.

```sql
select effective_year, count(*) from prices group by effective_year order by effective_year
```

Serco example

```bash
$ ./run-bulk-price-upload.sh dev s 2021
```

GEOAmey example

```bash
$ ./run-bulk-price-upload.sh dev g 2021
```

When the prices are imported you will be be given some feedback in the logs as to the success rate e.g. how many
prices were added and any errors.