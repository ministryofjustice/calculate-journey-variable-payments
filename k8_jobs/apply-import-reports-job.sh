#!/bin/sh

ENV=$1

if [ -z "$ENV" ]
then
  echo "No environment specified. Please supply required environment argument, dev, preprod or prod."
else
  cp import-reports.yaml imports-reports-$ENV.yaml
  sed -i 's/-placeholder/-'$ENV'/g' imports-reports-$ENV.yaml
  kubectl config set-context --current --namespace=calculate-journey-variable-payments-$ENV
  kubectl apply -f imports-reports-$ENV.yaml --namespace calculate-journey-variable-payments-$ENV
  rm imports-reports-$ENV.yaml
  kubectl get cronjobs -n calculate-journey-variable-payments-$ENV
fi
