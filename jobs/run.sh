#!/bin/sh
kubectl apply -f cronjob-import-reports-dev.yaml --namespace calculate-journey-variable-payments-dev

kubectl create job import-report-feed-try01 --from=cronjob/import-report-feed --namespace calculate-journey-variable-payments-dev