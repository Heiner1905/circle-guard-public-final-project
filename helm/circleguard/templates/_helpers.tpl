{{- define "circleguard.name" -}}
{{- .Chart.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "circleguard.labels" -}}
app.kubernetes.io/name: {{ include "circleguard.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
part-of: circleguard
{{- end -}}

{{- define "circleguard.serviceLabels" -}}
app.kubernetes.io/instance: {{ .root.Release.Name }}
app.kubernetes.io/version: {{ .root.Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .root.Release.Service }}
helm.sh/chart: {{ .root.Chart.Name }}-{{ .root.Chart.Version | replace "+" "_" }}
part-of: circleguard
app.kubernetes.io/component: {{ .serviceName }}
app.kubernetes.io/name: {{ .service.fullName }}
{{- end -}}

{{- define "circleguard.image" -}}
{{- $registry := .root.Values.global.imageRegistry -}}
{{- $repository := default (printf "%s/%s" $registry .service.fullName) .service.image.repository -}}
{{- printf "%s:%s" $repository .service.image.tag -}}
{{- end -}}
