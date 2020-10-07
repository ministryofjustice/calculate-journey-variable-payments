#!/usr/bin/env bash

export AWS_ACCESS_KEY_ID=foo
export AWS_SECRET_ACCESS_KEY=bar
export AWS_DEFAULT_REGION=eu-west-2

aws --endpoint-url=http://localhost:4572 s3 mb s3://jpc
aws --endpoint-url=http://localhost:4572 s3 mb s3://basm

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/schedule_34_locations_sample.xlsx s3://jpc/
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/geoamey_sample.xlsx s3://jpc/
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/serco_sample.xlsx s3://jpc/

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/01/2020-09-01-events.jsonl s3://basm/2020/09/01/2020-09-01-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/01/2020-09-01-journeys.jsonl s3://basm/2020/09/01/2020-09-01-journeys.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/01/2020-09-01-moves.jsonl s3://basm/2020/09/01/2020-09-01-moves.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/01/2020-09-01-people.jsonl s3://basm/2020/09/01/2020-09-01-people.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/01/2020-09-01-profiles.jsonl s3://basm/2020/09/01/2020-09-01-profiles.jsonl

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/02/2020-09-02-events.jsonl s3://basm/2020/09/02/2020-09-02-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/02/2020-09-02-journeys.jsonl s3://basm/2020/09/02/2020-09-02-journeys.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/02/2020-09-02-moves.jsonl s3://basm/2020/09/02/2020-09-02-moves.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/02/2020-09-02-people.jsonl s3://basm/2020/09/02/2020-09-02-people.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/02/2020-09-02-profiles.jsonl s3://basm/2020/09/02/2020-09-02-profiles.jsonl

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/03/2020-09-03-events.jsonl s3://basm/2020/09/03/2020-09-03-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/03/2020-09-03-journeys.jsonl s3://basm/2020/09/03/2020-09-03-journeys.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/03/2020-09-03-moves.jsonl s3://basm/2020/09/03/2020-09-03-moves.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/03/2020-09-03-people.jsonl s3://basm/2020/09/03/2020-09-03-people.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/09/03/2020-09-03-profiles.jsonl s3://basm/2020/09/03/2020-09-03-profiles.jsonl

echo "Buckets and seed data configured for schedule 34 locations and suppliers."