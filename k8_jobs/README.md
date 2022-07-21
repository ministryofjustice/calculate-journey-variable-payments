# Kubernetes jobs for the Payment Service

## Applying the job configurations:

The jobs are applied to the specific namespace and environment depending on the supplied environment parameter upon running the relevant scripts:

For dev:
```bash
$ ./apply-import-reports-job.sh dev
```
For preprod:
```bash
$ ./apply-import-reports-job.sh preprod
```
For prod:
```bash
$ ./apply-import-reports-job.sh prod
```

Upon on running one of the above the relevant cron job will be created.

Some useful kubectl commands.

List cron jobs in a given namespace:
```bash
$ kubectl get cronjobs -n <namespace>
```

Describe an existing cronjob:
```bash
$ kubectl get cronjob <job_name> -o yaml -n <namespace>
```

Delete an existing cronjob:
```bash
$ kubectl delete cronjob <job_name> -n <namespace>
```
