#!/bin/bash

# This script runs the bulk price upload in a given namespace for a supplier and effective year.

# The following command line tools are required to run this script:
#  kubectl

ENV=$1
SUPPLIER=$2
EFFECTIVE_YEAR=$3

if [ -z "$ENV" ]
then
  echo "No environment specified, please supply the environment in the first argument, dev, preprod or prod."
  exit
fi

if [ -z "$SUPPLIER" ]
then
  echo "No supplier specified, please supply the supplier in the second argument, s or g."
  exit
fi

if [ -z "$EFFECTIVE_YEAR" ]
then
  echo "No effective specified, please supply the effective year in the third argument."
  exit
else

  case $SUPPLIER in
	  s ) CHOSEN_SUPPLIER=SERCO;;
	  g ) CHOSEN_SUPPLIER=GEOAMEY;;
	  * ) echo Only s for SERCO and g for GEOAMEY are valid supplier values;
		  exit 1;;
  esac

  NAMESPACE=calculate-journey-variable-payments-$ENV

  kubectl get pods -n "$NAMESPACE"

  read -r -p "PLEASE ENTER THE POD NAME YOU WISH TO RUN THE BULK PRICE UPLOAD ON: " POD

  if [ -z "$POD" ]
  then
    echo "No pod specified, exiting."
    exit
  fi

  echo Using namespace: "$NAMESPACE"
  echo Using pod: "$POD"
  echo Using supplier: "$CHOSEN_SUPPLIER"
  echo Using effective year: "$EFFECTIVE_YEAR"

  read -r -p "ARE YOU SURE YOU WANT TO CONTINUE ? (yes/no) " yn

  case $yn in
	  yes ) echo Running bulk price upload ... ;;
	  no ) echo exiting...;
		  exit;;
	  * ) echo invalid response;
		  exit 1;;
  esac

  kubectl -n "$NAMESPACE" exec "$POD" -- java -jar app.jar --spring.main.web-application-type=none --price-import --supplier="$CHOSEN_SUPPLIER" --year="$EFFECTIVE_YEAR"
fi
