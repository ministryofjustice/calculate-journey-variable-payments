#!/bin/bash

# This script lists all RDS snapshots within a given namespace environment.

# The following command line tools are required to run this script:
#  aws cli
#  kubectl
#  jq

ENV=$1

if [ -z "$ENV" ]
then
  echo "No environment specified, please supply environment in the first argument, dev, preprod or prod."
else
  NAMESPACE=calculate-journey-variable-payments-$ENV

  echo using namespace "$NAMESPACE"

  DATABASE=$(kubectl -n "$NAMESPACE" get secrets rds-instance-"$NAMESPACE" -o json | jq -r ".data | map_values(@base64d).database_host" | cut -d . -f 1)

  echo using database instance ="$DATABASE"

  ACCESS_KEY_ID=$(kubectl get secret rds-instance-"$NAMESPACE" -n calculate-journey-variable-payments-$ENV -o json | jq -r ".data | map_values(@base64d).access_key_id")
  SECRET_ACCESS_KEY=$(kubectl get secret rds-instance-"$NAMESPACE" -n calculate-journey-variable-payments-$ENV -o json | jq -r ".data | map_values(@base64d).secret_access_key")

  export AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID
  export AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY
  export AWS_DEFAULT_REGION=eu-west-2

  aws rds describe-db-snapshots --db-instance-identifier "$DATABASE"
fi
