#!/bin/bash

ENV=$1
SUPPLIER=$2

if [ -z "$ENV" ]
then
  echo "No environment specified, please supply environment (dev, preprod or prod) in the 1st argument."
  exit
fi

if [ -z "$SUPPLIER" ]
then
  echo "No supplier specified, please supply serco or geo in the 2nd argument."
  exit
fi

ACCESS_KEY_ID=$(kubectl get secret calculate-journey-variable-payments-bucket -n calculate-journey-variable-payments-$ENV -o json | jq -r ".data | map_values(@base64d).access_key_id")
SECRET_ACCESS_KEY=$(kubectl get secret calculate-journey-variable-payments-bucket -n calculate-journey-variable-payments-$ENV -o json | jq -r ".data | map_values(@base64d).secret_access_key")
BUCKET=$(kubectl get secret calculate-journey-variable-payments-bucket -n calculate-journey-variable-payments-$ENV -o json | jq -r ".data | map_values(@base64d).bucket_name")

export AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID
export AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY
export AWS_DEFAULT_REGION=eu-west-2

if [ "$SUPPLIER" = "serco" ]
then
  aws s3api put-object --bucket $BUCKET --key serco_prices.xlsx --body ./serco-prices.xlsx
  echo "Uploaded Serco spreadsheet serco-prices.xlsx to '$ENV'."
elif [ "$SUPPLIER" = "geo" ]
then
  aws s3api put-object --bucket $BUCKET --key geoamey_prices.xlsx --body ./geoamey-prices.xlsx
  echo "Uploaded GEOamey spreadsheet geoamey-prices.xlsx to '$ENV'."
else
  echo "Unknown spreadsheet option '$SUPPLIER'."
fi
