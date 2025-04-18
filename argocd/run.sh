#!/bin/bash

# Port forward to ArgoCD server
echo "Port-forwarding to ArgoCD server..."
kubectl port-forward svc/argocd-server -n argocd 9000:443


# Wait for port-forward to establish
sleep 5
echo "Port-forwarding to ArgoCD established on port https://localhost:9000"

# Apply petclinic-dev.yaml and petclinic-staging.yaml
echo "Applying petclinic-dev.yaml..."
kubectl apply -f petclinic-dev.yaml

# echo "Applying petclinic-staging.yaml..."
# kubectl apply -f petclinic-staging.yaml

# Optional: Wait for Kubernetes resources to be created before closing the port-forward
echo "Waiting for resources to be applied..."
sleep 5



echo "Script completed."