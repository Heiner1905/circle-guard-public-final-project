# CircleGuard Infrastructure

Terraform modular infrastructure for CircleGuard. Azure is the primary cloud; AWS modules are added in US-02 as a multi-cloud bonus.

```
infra/
├── backend-azure/        # One-shot bootstrap of the remote tfstate Storage Account
├── modules/
│   ├── azure-network/    # VNet, AKS subnet, NSG (80/443 inbound)
│   ├── azure-aks/        # AKS 1.30 cluster (OIDC, Workload Identity, Azure CNI)
│   ├── azure-acr/        # Container Registry + AcrPull role assignment
│   └── k8s-middleware/   # PostgreSQL, Neo4j, Kafka, Redis, OpenLDAP via Helm
└── environments/         # dev / stage / prod compositions (added in US-03)
```

## Bootstrap order (manual, runs once)

```bash
cd infra/backend-azure
terraform init
terraform apply
```

This creates rg-circleguard-tfstate + a Storage Account that holds the remote
state for every environment. After that, each environment in environments/
reads/writes its own <env>.terraform.tfstate blob in the tfstate container.
