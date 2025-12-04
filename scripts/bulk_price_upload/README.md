# Bulk price uploads for supplier journey prices

This folder contains scripts to upload journey price spreadsheets to S3 and run a bulk import inside a Kubernetes pod. The import reads prices from spreadsheets stored in the supplier-specific S3 bucket and writes them into the service database via the running application.

## Prerequisites

- Access to the target Kubernetes cluster and namespace
- `kubectl` installed and configured
- Spreadsheets named `serco-prices.xlsx` and/or `geoamey-prices.xlsx`
- A manual database snapshot taken before import
- The `*-prices.xlsx` file must be provided by the supplier and its format must match the example `*-prices.xlsx` in this folder. The order of columns is important; the 2nd, 3rd, and 4th columns are used by the import.
- Price imports can only take place in the current effective year or the previous effective year. For example, in 2025 you may update 2025 or 2024; attempting 2023 will fail.

## Overview of steps

0. Validate the spreadsheet and year:
   - Make sure the provided file is formatted correctly (matches the example and column order).
   - Make sure the effective year is allowed (current year or previous year only).
1. Take a manual RDS snapshot using `take-manual-rds-snapshot.sh`.
2. Upload the spreadsheet(s) to S3 using `upload-journey-prices-spreadsheet-to-s3.sh`.
3. Run the bulk price import using `run-bulk-price-upload.sh`.
4. Review and action the import logs (see example below).
5. Optionally validate via the frontend.
6. Optionally delete the backup RDS snapshot.

## Important notes

- Do a dry run in pre-production before production.
- Exercise caution when running in production.
- Set the effective year correctly (e.g., contract Sept 2025–Aug 2026 uses `2025`).
- The import runs inside an application pod; AWS credentials are provided via the pod environment/service account.

## Scripts

- `list-rds-snapshots.sh`: Lists manual DB snapshots for an environment.
- `take-manual-rds-snapshot.sh`: Creates a manual DB snapshot in the target environment.
- `upload-journey-prices-spreadsheet-to-s3.sh`: Uploads price spreadsheets to S3.
- `run-bulk-price-upload.sh`: Executes the price import inside a selected pod.
- `delete-rds-snapshot.sh`: Deletes a manual DB snapshot.

## Upload spreadsheets to S3

The uploader expects:
- Files named `serco-prices.xlsx` and/or `geoamey-prices.xlsx`
- Files to be present in the current working directory

Examples:

```bash
# List snapshots before uploading
./list-rds-snapshots.sh dev

# Take a snapshot
./take-manual-rds-snapshot.sh dev

# Verify snapshot status is "available"
./list-rds-snapshots.sh dev

# Upload spreadsheets
./upload-journey-prices-spreadsheet-to-s3.sh dev serco
./upload-journey-prices-spreadsheet-to-s3.sh dev geo
```

## Running the bulk price import

The script validates inputs, lists pods, prompts you to choose one, and runs the import inside that pod.

Examples:

```bash
# Serco (warn on duplicates, overwrite existing prices)
./run-bulk-price-upload.sh dev serco 2025 WARN

# GEOAmey (error on duplicates, stop import)
./run-bulk-price-upload.sh dev geo 2025 ERROR
```

After running, the logs MUST be reviewed and actioned. Confirm the inserted/updated/skipped/error counts are expected, and re-run or investigate if not.

Example log line:

```
2025-12-04 11:28:15.033  INFO 1170 --- [           main] u.g.j.d.h.p.j.s.pricing.PriceImporter    : GEOAMEY PRICES INSERTED: 2. PRICES UPDATED: 0. PRICES SKIPPED: 0. TOTAL ERRORS: 0 |
```

SQL to check counts before/after:

```sql
select effective_year, count(*) from prices group by effective_year order by effective_year;
```

## Action parameter: handling duplicates (WARN vs ERROR)

- `ERROR`: Stops the import immediately when a duplicate price is detected and throws an exception. No existing prices are overwritten. Use this to strictly prevent changes to already priced journeys.
- `WARN`: Continues the import, logs a warning, and overwrites any existing price with the new one from the spreadsheet. Use this to refresh or correct already priced journeys.

## Troubleshooting

- NullPointerException like `getOptionValues(...) must not be null`: Ensure supplier, year, and action are provided and you selected a valid pod.
- Import fails: Check the selected pod’s logs, verify spreadsheets exist in S3 for the chosen supplier and environment, and that the effective year is correct.
- Pod selection: Multiple pods may appear due to load balancing; any application pod is acceptable.

## Clean up

If you created a manual snapshot for safety, you can delete it after validating the import:

```bash
./delete-rds-snapshot.sh dev SNAPSHOT_IDENTIFIER
```
