#!/usr/bin/env bash

export AWS_DEFAULT_REGION=eu-west-2

aws --endpoint-url=http://localhost:4572 s3 mb s3://jpc
aws --endpoint-url=http://localhost:4572 s3 mb s3://basm

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/schedule_34_locations_sample.xlsx s3://jpc/
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/geoamey_sample.xlsx s3://jpc/
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/serco_sample.xlsx s3://jpc/

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/01/2020-12-01-events.jsonl s3://basm/2020/12/01/2020-12-01-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/01/2020-12-01-journeys.jsonl s3://basm/2020/12/01/2020-12-01-journeys.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/01/2020-12-01-moves.jsonl s3://basm/2020/12/01/2020-12-01-moves.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/01/2020-12-01-people.jsonl s3://basm/2020/12/01/2020-12-01-people.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/01/2020-12-01-profiles.jsonl s3://basm/2020/12/01/2020-12-01-profiles.jsonl

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/02/2020-12-02-events.jsonl s3://basm/2020/12/02/2020-12-02-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/02/2020-12-02-journeys.jsonl s3://basm/2020/12/02/2020-12-02-journeys.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/02/2020-12-02-moves.jsonl s3://basm/2020/12/02/2020-12-02-moves.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/02/2020-12-02-people.jsonl s3://basm/2020/12/02/2020-12-02-people.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/02/2020-12-02-profiles.jsonl s3://basm/2020/12/02/2020-12-02-profiles.jsonl

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/03/2020-12-03-events.jsonl s3://basm/2020/12/03/2020-12-03-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/03/2020-12-03-journeys.jsonl s3://basm/2020/12/03/2020-12-03-journeys.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/03/2020-12-03-moves.jsonl s3://basm/2020/12/03/2020-12-03-moves.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/03/2020-12-03-people.jsonl s3://basm/2020/12/03/2020-12-03-people.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/03/2020-12-03-profiles.jsonl s3://basm/2020/12/03/2020-12-03-profiles.jsonl

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/04/2020-12-04-events.jsonl s3://basm/2020/12/04/2020-12-04-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/04/2020-12-04-journeys.jsonl s3://basm/2020/12/04/2020-12-04-journeys.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/04/2020-12-04-moves.jsonl s3://basm/2020/12/04/2020-12-04-moves.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/04/2020-12-04-people.jsonl s3://basm/2020/12/04/2020-12-04-people.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/04/2020-12-04-profiles.jsonl s3://basm/2020/12/04/2020-12-04-profiles.jsonl

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/05/2020-12-05-events.jsonl s3://basm/2020/12/05/2020-12-05-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/05/2020-12-05-journeys.jsonl s3://basm/2020/12/05/2020-12-05-journeys.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/05/2020-12-05-moves.jsonl s3://basm/2020/12/05/2020-12-05-moves.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/05/2020-12-05-people.jsonl s3://basm/2020/12/05/2020-12-05-people.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/05/2020-12-05-profiles.jsonl s3://basm/2020/12/05/2020-12-05-profiles.jsonl

aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/06/2020-12-06-events.jsonl s3://basm/2020/12/06/2020-12-06-events.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/06/2020-12-06-journeys.jsonl s3://basm/2020/12/06/2020-12-06-journeys.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/06/2020-12-06-moves.jsonl s3://basm/2020/12/06/2020-12-06-moves.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/06/2020-12-06-people.jsonl s3://basm/2020/12/06/2020-12-06-people.jsonl
aws --endpoint-url=http://localhost:4572 s3 cp /docker-entrypoint-initaws.d/2020/12/06/2020-12-06-profiles.jsonl s3://basm/2020/12/06/2020-12-06-profiles.jsonl

echo "Buckets and seed data configured for schedule 34 locations and suppliers."