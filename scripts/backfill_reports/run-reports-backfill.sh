#!/bin/bash

# This script runs the reports backfill in a given namespace for the given from and to dates.

# The following command line tools are required to run this script:
#  kubectl

ENV=$1
DATE_FROM=$2
DATE_TO=$3

if [ -z "$ENV" ]
then
  echo "No environment specified, please supply the environment in the first argument, dev, preprod or prod."
  exit
fi

if [ -z "$DATE_FROM" ]
then
  echo "No date from specified, please supply the date from in the second argument, e.g 2022-08-15."
  exit
fi

if [ -z "$DATE_TO" ]
then
  echo "No date to specified, please supply the date to in the second argument, e.g 2022-08-15."
  exit
else

  NAMESPACE=calculate-journey-variable-payments-$ENV

  read -r -p "HAVE YOU TAKEN A SNAPSHOT OF THE $ENV RDS ? (yes/no) " yn

  case $yn in
	  yes ) ;;
	  no ) echo exiting...;
		  exit;;
	  * ) echo invalid response;
		  exit 1;;
  esac

  kubectl get pods -n "$NAMESPACE"

  read -r -p "PLEASE ENTER THE POD NAME YOU WISH TO RUN THE BACKFILL REPORTS ON: " POD

  if [ -z "$POD" ]
  then
    echo "No pod specified, exiting."
    exit
  fi

  echo Using namespace: "$NAMESPACE"
  echo Using pod: "$POD"
  echo Using date from: "$DATE_FROM"
  echo Using date to: "$DATE_TO"

  read -r -p "ARE YOU SURE YOU WANT TO CONTINUE ? (yes/no) " yn

  case $yn in
	  yes ) echo Running backfill reports ... ;;
	  no ) echo exiting...;
		  exit;;
	  * ) echo invalid response;
		  exit 1;;
  esac

  kubectl -n "$NAMESPACE" exec "$POD" -- java -Xmx2048m -jar app.jar --spring.main.web-application-type=none --report-import --from="$DATE_FROM" --to="$DATE_TO"
fi
