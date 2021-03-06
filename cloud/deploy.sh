#!/bin/bash
set -euxo pipefail

for template_file in *.yaml.template
do
  cat $template_file | sed -e 's/PROJECT_NAME/'"$PROJECT_NAME"'/g' > "${template_file/.template/}"
done

kubectl apply -f postgres.yaml
kubectl apply -f rabbit.yaml
kubectl apply -f mail.yaml
sleep 10s
kubectl apply -f database-schema.yaml
sleep 10s
kubectl apply -f worker.yaml
kubectl apply -f server.yaml
kubectl apply -f front.yaml
kubectl apply -f updater.yaml
kubectl create configmap nginx-frontend-conf --from-file=frontend.conf
kubectl apply -f frontend.yaml
