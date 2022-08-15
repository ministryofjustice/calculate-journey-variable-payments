# Backfill reports

Backfilling reports is something hopefully you will not need to do. However, should need arise the process is scripted.

1. Take a RDS snapshot of the database using the **take-manual-rds-snapshot.sh** script where the journey prices are going to be loaded.
2. Use the shell script **run-reports-backfill.sh** to begin the backfill.
3(optional) delete the backup RDS snapshot.

**IMPORTANT:**

- IT IS ADVISABLE TO DO A DRY RUN IN THE PRE-PRODUCTION ENVIRONMENT BEFORE DOING THIS IN THE PRODUCTION ENVIRONMENT.
- WHEN A BACKFILL DOES PLACE YOU MUST ALWAYS BACKFILL TO YESTERDAYS DATE OTHERWISE YOU MAY BE MISSING KEY DATA/CHANGES. THIS SCRIPT DOES NOT ENFORCE THAT!
- DEPENDING ON HOW FAR YOU GO BACK IT CAN TAKE A LONG TIME, IDEALLY RUN OUT OF HOURS ON PROD.

### How to backfill reports

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

_Start the backfill from a local terminal session by running the following_

```bash
$ ./run-reports-backfill.sh <dev/preprod/prod> <DATE_FROM> <DATE_TO> 
```
