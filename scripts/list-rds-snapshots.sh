#!/bin/bash

# This script lists all RDS snapshots within a given namespace environment.
# Requires:
#   aws cli
#   kubectl
#   jq

ENV=$1

if [ -z "$ENV" ]; then
  echo "No environment specified, please supply environment in the first argument: dev, preprod or prod." >&2
  exit 1
fi

NAMESPACE="calculate-journey-variable-payments-$ENV"

echo "Using namespace: $NAMESPACE" >&2

DATABASE=$(kubectl -n "$NAMESPACE" get secrets "rds-instance-$NAMESPACE" -o json \
  | jq -r '.data | map_values(@base64d).database_host' \
  | cut -d . -f 1)

echo "Using database instance: $DATABASE" >&2

SVCPOD=$(kubectl get po -n "$NAMESPACE" | grep service-pod | awk '{ print $1 }')

echo "Using service pod: $SVCPOD" >&2

# Run AWS CLI inside the pod; output JSON → piped to jq for clean formatted output
kubectl exec -i "$SVCPOD" -n "$NAMESPACE" -- \
  aws rds describe-db-snapshots \
    --db-instance-identifier "$DATABASE" \
    --output json \
  | jq -r '.DBSnapshots[] | "\(.DBSnapshotIdentifier)  \(.SnapshotCreateTime)  \(.SnapshotType)"'
