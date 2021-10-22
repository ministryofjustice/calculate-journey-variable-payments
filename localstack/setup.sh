#!/usr/bin/env bash

export AWS_ACCESS_KEY_ID=foo
export AWS_SECRET_ACCESS_KEY=bar
export AWS_DEFAULT_REGION=eu-west-2

aws --endpoint-url=http://localhost:4572 s3 mb s3://jpc
aws --endpoint-url=http://localhost:4572 s3 mb s3://basm

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/schedule_34_locations_sample.xlsx s3://jpc/
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/geoamey_sample.xlsx s3://jpc/
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/serco_sample.xlsx s3://jpc/

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2021/10/21/2021-10-21-events.jsonl s3://basm/2021/10/21/2021-10-21-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2021/10/21/2021-10-21-moves.jsonl s3://basm/2021/10/21/2021-10-21-moves.jsonl

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2021/10/22/2021-10-22-events.jsonl s3://basm/2021/10/22/2021-10-22-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2021/10/22/2021-10-22-moves.jsonl s3://basm/2021/10/22/2021-10-22-moves.jsonl

echo "Buckets and seed data configured for schedule 34 locations and suppliers."