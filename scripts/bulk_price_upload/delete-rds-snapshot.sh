#!/bin/bash

ENV=$1
SNAPSHOT_IDENTIFIER=$2

if [ -z "$ENV" ]
then
  echo "No environment specified, please supply environment in the first argument, dev, preprod or prod."
  exit
fi

if [ -z "$SNAPSHOT_IDENTIFIER" ]
then
  echo "No snapshot identifier specified, please supply snapshot identifier in the second argument."
  exit
else

  NAMESPACE=calculate-journey-variable-payments-$ENV

  echo Using namespace "$NAMESPACE"

  DATABASE=$(kubectl -n "$NAMESPACE" get secrets rds-instance-"$NAMESPACE" -o json | jq -r ".data | map_values(@base64d).database_host" | cut -d . -f 1)

  echo Using database instance "$DATABASE"

  read -r -p "ARE YOU SURE YOU WANT TO DELETE SNAPSHOT $SNAPSHOT_IDENTIFIER ON $NAMESPACE (THIS CANNOT BE REVERSED) ? (yes/no) " yn

  case $yn in
	  yes ) echo Deleting snapshot "$SNAPSHOT_IDENTIFIER";;
	  no ) echo exiting...;
		  exit;;
	  * ) echo invalid response;
		  exit 1;;
  esac

  ACCESS_KEY_ID=$(kubectl get secret rds-instance-"$NAMESPACE" -n calculate-journey-variable-payments-$ENV -o json | jq -r ".data | map_values(@base64d).access_key_id")
  SECRET_ACCESS_KEY=$(kubectl get secret rds-instance-"$NAMESPACE" -n calculate-journey-variable-payments-$ENV -o json | jq -r ".data | map_values(@base64d).secret_access_key")

  export AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID
  export AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY
  export AWS_DEFAULT_REGION=eu-west-2

  aws rds delete-db-snapshot --db-snapshot-identifier "$SNAPSHOT_IDENTIFIER"
fi
