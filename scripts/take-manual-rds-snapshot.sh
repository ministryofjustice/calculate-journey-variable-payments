#!/bin/bash

# This script takes a manual RDS snapshot prior to bulk price uploads.
# It avoids using AWS CLI locally by running AWS inside the service pod.
#
# Requirements:
#   kubectl
#   jq
#
# AWS CLI and credentials must be available inside the service pod.

ENV=$1

if [ -z "$ENV" ]; then
  echo "No environment specified. Provide: dev, preprod, or prod."
  exit 1
fi

NAMESPACE="calculate-journey-variable-payments-$ENV"

echo "Using namespace: $NAMESPACE"

# Extract DB instance name from Kubernetes secret
DATABASE=$(kubectl -n "$NAMESPACE" get secret "rds-instance-$NAMESPACE" -o json \
  | jq -r '.data | map_values(@base64d).database_host' \
  | cut -d . -f 1)

echo "Using database instance: $DATABASE"

# Find the pod that includes AWS CLI (the service pod)
SVCPOD=$(kubectl get po -n "$NAMESPACE" | grep service-pod | awk '{print $1}')

if [ -z "$SVCPOD" ]; then
  echo "ERROR: Could not find a service pod in namespace $NAMESPACE"
  exit 1
fi

echo "Using service pod: $SVCPOD"

DATE=$(date +"%Y%m%d-%H%M%S")
SNAPSHOT_IDENTIFIER="manual-rds-$DATABASE-$DATE"

echo "Creating snapshot: $SNAPSHOT_IDENTIFIER"

# Run AWS inside the pod instead of locally
kubectl exec -n "$NAMESPACE" "$SVCPOD" -- \
  aws rds create-db-snapshot \
    --db-instance-identifier "$DATABASE" \
    --db-snapshot-identifier "$SNAPSHOT_IDENTIFIER"
