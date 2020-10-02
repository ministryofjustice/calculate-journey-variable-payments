    {{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: SPRING_PROFILES_ACTIVE
    value: "logstash"

  - name: APP_DB_URL
    valueFrom:
      secretKeyRef:
        name: "{{ .Values.db.secret_name }}"
        key: url

  - name: AWS_DEFAULT_REGION
    value: "eu-west-2"

  - name: JPC_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}-bucket
        key: ACCESS_KEY_ID

  - name: JPC_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}-bucket
        key: SECRET_ACCESS_KEY

  - name: JPC_BUCKET_NAME
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}-bucket
        key: BUCKET_NAME

  - name: BASM_BUCKET_NAME
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: BASM_BUCKET_NAME

  - name: BASM_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: BASM_AWS_ACCESS_KEY_ID

  - name: BASM_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: BASM_AWS_SECRET_ACCESS_KEY

{{- end -}}
