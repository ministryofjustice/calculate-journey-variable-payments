#!/bin/bash

# This script deletes manual RDS snapshots and should be used after we happy the bulk price upload was successful.

# USE WITH EXTREME CARE, THIS OPERATION IS CANNOT BE REVERSED.

# The following command line tools are required to run this script:
#  aws cli
#  kubectl
#  jq

ENV=$1
SNAPSHOT_IDENTIFIER=$2

if [ -z "$ENV" ]
then
  echo "No environment specified, please supply environment in the first argument, dev, preprod or prod." >&2
  exit
fi

if [ -z "$SNAPSHOT_IDENTIFIER" ]
then
  echo "No snapshot identifier specified, please supply snapshot identifier in the second argument." >&2
  exit
else
  if [[ $SNAPSHOT_IDENTIFIER == "rds"* ]] ; then
    echo "Cannot delete automatic snapshots" >&2
    exit
  fi

  NAMESPACE=calculate-journey-variable-payments-$ENV

  echo Using namespace "$NAMESPACE" >&2

  DATABASE=$(kubectl -n "$NAMESPACE" get secrets rds-instance-"$NAMESPACE" -o json | jq -r ".data | map_values(@base64d).database_host" | cut -d . -f 1)

  echo Using database instance "$DATABASE" >&2

  read -r -p "ARE YOU SURE YOU WANT TO DELETE SNAPSHOT $SNAPSHOT_IDENTIFIER ON $NAMESPACE (THIS CANNOT BE REVERSED) ? (yes/no) " yn

  case $yn in
	  yes ) echo Deleting snapshot "$SNAPSHOT_IDENTIFIER";;
	  no ) echo exiting...;
		  exit;;
	  * ) echo invalid response;
		  exit 1;;
  esac
  SVCPOD=$(kubectl get po -n "$NAMESPACE" | grep service-pod | awk '{ print $1 }')
  echo "Using service pod: $SVCPOD" >&2

  RESULT=$(
    kubectl exec -i "$SVCPOD" -n "$NAMESPACE" -- \
      aws rds delete-db-snapshot --db-snapshot-identifier "$SNAPSHOT_IDENTIFIER"
  )

  STATUS=$(echo "$RESULT" | jq -r '.DBSnapshot.Status')
  ID=$(echo "$RESULT" | jq -r '.DBSnapshot.DBSnapshotIdentifier')

  if [ "$STATUS" = "deleted" ]; then
    echo "✅ Snapshot \"$ID\" has been successfully deleted."
  else
    echo "❌ Snapshot \"$ID\" status: $STATUS"
    exit 1
  fi

fi

