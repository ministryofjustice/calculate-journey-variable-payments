#!/bin/bash

# Annual price adjustment script
# Applies inflationary and volumetric adjustments for a supplier and contractual year.
# Required: kubectl

ENV=$1
SUPPLIER=$2
YEAR=$3
INFLATIONARY=$4
VOLUMETRIC=$5

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

if [ -z "$YEAR" ]; then
  echo "❌ No contractual year specified. Supply the contractual year (e.g., 2025) as the 3rd argument."
  exit 1
fi

if [ -z "$INFLATIONARY" ]; then
  echo "❌ No inflationary rate specified. Supply a decimal multiplier (e.g., 0.0753595287) as the 4th argument."
  exit 1
fi

# Volumetric is optional; default to 0.0 (bypass)
if [ -z "$VOLUMETRIC" ]; then
  echo "ℹ️ No volumetric multiplier specified. Defaulting to 0.0 (bypass)."
  VOLUMETRIC="0.0"
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

NAMESPACE="calculate-journey-variable-payments-$ENV"

# -----------------------------
# Effective year constraint (current or previous year only)
# -----------------------------
CURRENT_YEAR=$(date +%Y)
PREVIOUS_YEAR=$((CURRENT_YEAR - 1))
if ! [[ "$YEAR" =~ ^[0-9]{4}$ ]]; then
  echo "❌ YEAR must be a 4-digit number (e.g., 2025)."
  exit 1
fi
if [ "$YEAR" -ne "$CURRENT_YEAR" ] && [ "$YEAR" -ne "$PREVIOUS_YEAR" ]; then
  echo "❌ Price adjustments can only be applied to the current ($CURRENT_YEAR) or previous ($PREVIOUS_YEAR) contractual year."
  exit 1
fi

# -----------------------------
# Collect details text for the adjustment
# -----------------------------
read -r -p "PLEASE ENTER DETAIL TEXT FOR ADJUSTMENT: " DETAIL
if [ -z "$DETAIL" ]; then
  echo "❌ Detail text is required to describe this adjustment (e.g., 'CY4/5 applied to CY6 per PECS instruction')."
  exit 1
fi

# -----------------------------
# List pods and explain selection
# -----------------------------
echo "⚠️ Listing all pods in namespace $NAMESPACE (excluding service pods):"
kubectl get pods -n "$NAMESPACE" | grep -v service-pod

echo "ℹ️ Multiple pods may be listed because the app runs on multiple pods for load balancing."
echo "It does not matter which pod you choose for the adjustment,"
echo "but selecting one is useful for tracking logs."

read -r -p "PLEASE ENTER THE POD NAME YOU WISH TO RUN THE ANNUAL ADJUSTMENT ON: " POD
if [ -z "$POD" ]; then
  echo "❌ No pod specified. Exiting."
  exit 1
fi

# -----------------------------
# Final confirmation
# -----------------------------
echo "--------------------------------------------"
echo "You are about to run the annual price adjustment with the following parameters:"
echo "Namespace: $NAMESPACE"
echo "Pod: $POD"
echo "Supplier: $CHOSEN_SUPPLIER"
echo "Year: $YEAR"
echo "Inflationary multiplier: $INFLATIONARY"
echo "Volumetric multiplier: $VOLUMETRIC"
echo "Detail: $DETAIL"
echo "⚠️ Ensure you have already taken a DB snapshot."
echo "--------------------------------------------"

read -r -p "ARE YOU SURE YOU WANT TO CONTINUE? (yes/no) " yn
case $yn in
  yes ) echo "Running annual price adjustment...";;
  no ) echo "Exiting..."; exit 0;;
  * ) echo "Invalid response. Exiting."; exit 1;;
esac

# -----------------------------
# Execute annual adjustment
# -----------------------------
  kubectl -n "$NAMESPACE" exec "$POD" -- java -Xmx2048m -jar app.jar \
  --spring.main.web-application-type=none \
  --price-adjust \
  --supplier="$CHOSEN_SUPPLIER" \
  --year="$YEAR" \
  --inflationary="$INFLATIONARY" \
  --details="$DETAIL" \
  --volumetric="$VOLUMETRIC" \
  | tee adjustment.log

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
  echo "✅ Annual price adjustment completed successfully."
else
  echo "❌ Annual price adjustment FAILED. Please check the pod logs for details."
  exit $EXIT_CODE
fi
