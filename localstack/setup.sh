#!/usr/bin/env bash

export AWS_ACCESS_KEY_ID=foo
export AWS_SECRET_ACCESS_KEY=bar
export AWS_DEFAULT_REGION=eu-west-2

aws --endpoint-url=http://localhost:4572 s3 mb s3://locations
aws --endpoint-url=http://localhost:4572 s3 mb s3://serco
aws --endpoint-url=http://localhost:4572 s3 mb s3://geoamey

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/schedule_34_locations_sample.xlsx s3://locations/
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/geoamey_sample.xlsx s3://geoamey/
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/serco_sample.xlsx s3://serco/

echo "Buckets and seed data configured for schedule 34 locations and suppliers."