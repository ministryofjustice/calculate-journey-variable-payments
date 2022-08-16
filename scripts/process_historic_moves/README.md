# Process historic moves

The primary reason this exists is to cater for changes to the logic around the processing of moves and the ability to apply 
that logic change to historic moves in the service without the need to pull them down from S3 again i.e. it will run
against what we already have in the database.

**IMPORTANT:**

- DO A DRY RUN IN THE PRE-PRODUCTION ENVIRONMENT BEFORE DOING THIS IN THE PRODUCTION ENVIRONMENT.
- WHEN A BACKFILL DOES PLACE YOU MUST ALWAYS BACKFILL TO YESTERDAYS DATE OTHERWISE YOU MAY BE MISSING KEY DATA/CHANGES. THIS SCRIPT DOES NOT ENFORCE THAT!
- DEPENDING ON HOW FAR YOU GO BACK IT CAN TAKE A LONG TIME, IDEALLY RUN OUT OF HOURS ON PROD.

### How to process historic moves

Do the following in a terminal window:

_List the RDS snapshots before you start_

```bash
$ ./list-rds-snapshots.sh <dev/preprod/prod>
```

_Take a snapshot and make a note of the snapshot identifier_

```bash
$ ./take-manual-rds-snapshot.sh <dev/preprod/prod>
```

_Check the snapshot has been created and is in "Status" : "available"_

```bash
$ ./list-rds-snapshots.sh <dev/preprod/prod>
```

_Start the process historic moves. Note the to date should be to yesterdays date otherwise they may be gaps in the data_

```bash
$ ./run-process-historic-moves.sh <dev/preprod/prod> <s/g> <date_from> <date_to> 
```

_If you are happy all has worked then you can delete the snapshot taken at the start of the process_

```bash
$ ./delete-rds-snapshot.sh <dev/preprod/prod> <snapshot_identifier>
```
