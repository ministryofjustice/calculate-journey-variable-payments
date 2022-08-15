#!/bin/sh

# This script creates a cron job for the daily reports ingress into the payment service.

# The following command line tools are required to run this script:
#  kubectl

ENV=$1

if [ -z "$ENV" ]
then
  echo "No environment specified. Please supply required environment argument, dev, preprod or prod."
else
  cp import-reports.yaml imports-reports-$ENV.yaml

  # sed command formatted for Mac OS (does not work on linux)
  if ! sed -i '' 's/-placeholder/-'$ENV'/g' imports-reports-$ENV.yaml;
  then
    echo Attempting to running Linix sed
    sed -i 's/-placeholder/-'$ENV'/g' imports-reports-$ENV.yaml
  fi

  kubectl apply -f imports-reports-$ENV.yaml --namespace calculate-journey-variable-payments-$ENV
  rm imports-reports-$ENV.yaml
  kubectl get cronjobs -n calculate-journey-variable-payments-$ENV
fi