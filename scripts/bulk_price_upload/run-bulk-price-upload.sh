#!/bin/bash

# Bulk price upload script
# Uses the journey price spreadsheet previously uploaded to S3
# Required: kubectl

ENV=$1
SUPPLIER=$2
EFFECTIVE_YEAR=$3
ACTION=$4

# -----------------------------
# Validate inputs
# -----------------------------
if [ -z "$ENV" ]; then
  echo "❌ No environment specified. Supply dev, preprod, or prod as the 1st argument."
  exit 1
fi

if [ -z "$SUPPLIER" ]; then
  echo "❌ No supplier specified. Supply 'serco' or 'geo' as the 2nd argument."
  exit 1
fi

if [ -z "$EFFECTIVE_YEAR" ]; then
  echo "❌ No effective year specified. Supply the effective year as the 3rd argument."
  exit 1
fi

if [ -z "$ACTION" ]; then
  echo "❌ No action specified. Supply 'WARN' or 'ERROR' as the 4th argument."
  echo "INFO: ACTION controls how duplicate prices are handled:"
  echo "  ERROR - Stops the import and throws an exception when duplicates are found."
  echo "  WARN  - Logs a warning and overwrites existing prices with the new ones."
  exit 1
fi

# -----------------------------
# Validate supplier
# -----------------------------
case $SUPPLIER in
  serco ) CHOSEN_SUPPLIER="SERCO";;
  geo   ) CHOSEN_SUPPLIER="GEOAMEY";;
  * )
    echo "❌ Invalid supplier. Only 'serco' or 'geo' are allowed."
    exit 1
    ;;
esac

# -----------------------------
# Validate action
# -----------------------------
case $ACTION in
  WARN|ERROR ) ;;
  * )
    echo "❌ Invalid action. Only 'WARN' or 'ERROR' are allowed."
    echo "INFO: ACTION controls how duplicate prices are handled:"
    echo "  ERROR - Stops the import and throws an exception when duplicates are found."
    echo "  WARN  - Logs a warning and overwrites existing prices with the new ones."
    exit 1
    ;;
esac

NAMESPACE="calculate-journey-variable-payments-$ENV"

# -----------------------------
# Inform user
# -----------------------------
echo "--------------------------------------------"
echo "Environment: $ENV"
echo "Supplier: $CHOSEN_SUPPLIER"
echo "Effective Year: $EFFECTIVE_YEAR"
echo "Action on duplicates: $ACTION"
echo "Namespace: $NAMESPACE"
echo "This script will use the journey price spreadsheet previously uploaded to the S3 bucket for $CHOSEN_SUPPLIER."
echo "--------------------------------------------"

# -----------------------------
# Confirmation before proceeding
# -----------------------------
read -r -p "HAVE YOU TAKEN A SNAPSHOT OF THE $ENV DATABASE AND UPLOADED THE JOURNEY PRICE SPREADSHEET FOR $CHOSEN_SUPPLIER? (yes/no) " yn
case $yn in
  yes ) ;;
  no ) echo "Exiting..."; exit 0;;
  * ) echo "Invalid response. Exiting."; exit 1;;
esac

# -----------------------------
# List pods and explain selection
# -----------------------------
echo "⚠️ Listing all pods in namespace $NAMESPACE (excluding service pods):"
kubectl get pods -n "$NAMESPACE" | grep -v service-pod

echo "ℹ️ Multiple pods may be listed because the app runs on multiple pods for load balancing."
echo "It does not matter which pod you choose for the import,"
echo "but selecting one is useful for tracking logs."

read -r -p "PLEASE ENTER THE POD NAME YOU WISH TO RUN THE BULK PRICE UPLOAD ON: " POD
if [ -z "$POD" ]; then
  echo "❌ No pod specified. Exiting."
  exit 1
fi

# -----------------------------
# Final confirmation
# -----------------------------
echo "--------------------------------------------"
echo "You are about to run the bulk price upload with the following parameters:"
echo "Namespace: $NAMESPACE"
echo "Pod: $POD"
echo "Supplier: $CHOSEN_SUPPLIER"
echo "Effective Year: $EFFECTIVE_YEAR"
echo "Action on duplicates: $ACTION"
echo "The uploaded spreadsheet in S3 will be used for this import."
echo "⚠️ Ensure you have already taken a DB snapshot."
echo "--------------------------------------------"

read -r -p "ARE YOU SURE YOU WANT TO CONTINUE? (yes/no) " yn
case $yn in
  yes ) echo "Running bulk price upload...";;
  no ) echo "Exiting..."; exit 0;;
  * ) echo "Invalid response. Exiting."; exit 1;;
esac

# -----------------------------
# Execute bulk price upload
# -----------------------------
kubectl -n "$NAMESPACE" exec "$POD" -- java -jar app.jar \
  --spring.main.web-application-type=none \
  --price-import \
  --supplier="$CHOSEN_SUPPLIER" \
  --year="$EFFECTIVE_YEAR" \
  --action="$ACTION" \
  | tee price-upload.log

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
  echo "✅ Bulk price upload completed successfully."
else
  echo "❌ Bulk price upload FAILED. Please check the pod logs for details."
  exit $EXIT_CODE
fi
