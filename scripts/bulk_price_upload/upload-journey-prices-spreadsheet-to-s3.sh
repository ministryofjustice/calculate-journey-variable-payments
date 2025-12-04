#!/bin/bash

# Required command line tools:
#  aws cli
#  kubectl
#  jq

ENV=$1
SUPPLIER=$2

# -----------------------------
# Validate inputs
# -----------------------------
if [ -z "$ENV" ]; then
  echo "No environment specified. Supply environment (dev, preprod, prod) as the 1st argument."
  exit 1
fi

if [ -z "$SUPPLIER" ]; then
  echo "No supplier specified. Supply 'serco' or 'geo' as the 2nd argument."
  exit 1
fi

# -----------------------------
# Set supplier file
# -----------------------------
case $SUPPLIER in
  serco ) FILE_NAME="serco-prices.xlsx"; ;;
  geo   ) FILE_NAME="geoamey-prices.xlsx"; ;;
  * )
    echo "Invalid supplier: $SUPPLIER"
    exit 1
    ;;
esac

# Generate S3_KEY by replacing '-' with '_'
S3_KEY="${FILE_NAME//-/_}"

# -----------------------------
# Confirmation
# -----------------------------
read -r -p "UPLOADING JOURNEY PRICES TO S3 IN $ENV FOR SUPPLIER $SUPPLIER. File to upload: $FILE_NAME. ARE YOU SURE? (yes/no) " yn

case $yn in
  yes ) ;;
  no )
    echo "Exiting..."
    exit 0
    ;;
  * )
    echo "Invalid response"
    exit 1
    ;;
esac

# -----------------------------
# Fetch bucket name
# -----------------------------
BUCKET=$(kubectl get secret calculate-journey-variable-payments-bucket \
  -n calculate-journey-variable-payments-$ENV \
  -o json | jq -r ".data | map_values(@base64d).bucket_name")

if [ -z "$BUCKET" ]; then
  echo "ERROR: Could not retrieve S3 bucket name."
  exit 1
fi

# -----------------------------
# Locate service pod
# -----------------------------
svcpod=$(kubectl get po -n calculate-journey-variable-payments-$ENV \
  | grep service-pod | awk '{ print $1 }')

if [ -z "$svcpod" ]; then
  echo "ERROR: No service-pod found in namespace calculate-journey-variable-payments-$ENV"
  exit 1
fi

# -----------------------------
# Upload to S3
# -----------------------------
if [ "$SUPPLIER" = "serco" ]; then
  # Direct upload from local file
  aws s3api put-object \
    --bucket "$BUCKET" \
    --key "$S3_KEY" \
    --body "./$FILE_NAME" \
    >/dev/null 2>&1

  echo "✅ Successfully uploaded $FILE_NAME to S3 bucket '$BUCKET' as '$S3_KEY'."

else
  # For geo (or any supplier requiring pod copy + upload)
  kubectl cp "./$FILE_NAME" \
    "${svcpod}:/tmp/$FILE_NAME" \
    -n calculate-journey-variable-payments-$ENV

  kubectl exec -it "$svcpod" -n calculate-journey-variable-payments-$ENV -- \
    aws s3api put-object \
      --bucket "$BUCKET" \
      --key "$S3_KEY" \
      --body "/tmp/$FILE_NAME" \
      >/dev/null 2>&1

  echo "✅ Successfully uploaded $FILE_NAME to S3 bucket '$BUCKET' as '$S3_KEY'."
fi
