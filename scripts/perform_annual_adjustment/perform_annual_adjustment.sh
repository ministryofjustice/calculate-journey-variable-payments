#!/bin/bash

# This script runs the process historic moves command in a given namespace for the given supplier and from and to dates.

# The following command line tools are required to run this script:
#  kubectl

ENV=$1
SUPPLIER=$2
YEAR=$3
INFLATIONARY=$4
VOLUMETRIC=$5


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

if [ -z "$YEAR" ]
then
  echo "No year specified, please supply the contractual year to apply adjustments to."
  exit
fi

if [ -z "$INFLATIONARY" ]
then
  echo "No inflationary rate specified, e.g 0.67"
  exit
fi

if [ -z "$VOLUMETRIC" ]
then
  echo "No volumetric multiplier specified, using 0.0 to bypass"
  VOLUMETRIC="0.0"
fi

  case $SUPPLIER in
	  s ) CHOSEN_SUPPLIER=SERCO;;
	  g ) CHOSEN_SUPPLIER=GEOAMEY;;
	  * ) echo Only s for SERCO and g for GEOAMEY are valid supplier values;
		  exit 1;;
  esac

  read -r -p "PLEASE ENTER DETAIL TEXT FOR ADJUSTMENT: " DETAIL

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

  read -r -p "PLEASE ENTER THE POD NAME YOU WISH TO RUN THE ANNUAL ADJUSTMENTS ON: " POD

  if [ -z "$POD" ]
  then
    echo "No pod specified, exiting."
    exit
  fi

  echo Using namespace: "$NAMESPACE"
  echo Using pod: "$POD"
  echo Using supplier: "$CHOSEN_SUPPLIER"
  echo Using Inflationary multiplier: "$INFLATIONARY"
  echo Using Volumetric multiplier: "$VOLUMETRIC"
  echo Using Detail: "$DETAIL"

  read -r -p "ARE YOU SURE YOU WANT TO CONTINUE ? (yes/no) " yn

  case $yn in
	  yes ) echo Running Annual price adjustment ... ;;
	  no ) echo exiting...;
		  exit;;
	  * ) echo invalid response;
		  exit 1;;
  esac


if [ -z "$VOLUMETRIC" ]
then
    kubectl -n "$NAMESPACE" exec "$POD" -- java -Xmx2048m -jar app.jar --spring.main.web-application-type=none --price-adjust --supplier="$CHOSEN_SUPPLIER" --year="$YEAR" --inflationary="$INFLATIONARY" --details="$DETAIL"
else
    kubectl -n "$NAMESPACE" exec "$POD" -- java -Xmx2048m -jar app.jar --spring.main.web-application-type=none --price-adjust --supplier="$CHOSEN_SUPPLIER" --year="$YEAR" --volumetric="$VOLUMETRIC" --inflationary="$INFLATIONARY" --details="$DETAIL"
fi
