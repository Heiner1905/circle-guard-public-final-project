terraform {
  required_version = ">= 1.5.0"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.31"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.14"
    }
  }
}

resource "kubernetes_namespace" "middleware" {
  metadata {
    name = var.namespace
    labels = {
      "app.kubernetes.io/part-of" = "circleguard"
      "managed-by"                = "terraform"
    }
  }
}

resource "helm_release" "postgresql" {
  name       = "postgresql"
  namespace  = kubernetes_namespace.middleware.metadata[0].name
  repository = "https://charts.bitnami.com/bitnami"
  chart      = "postgresql"
  version    = "15.5.20"

  values = [yamlencode({
    auth = {
      username         = var.postgres_username
      password         = var.postgres_password
      database         = var.postgres_database
      postgresPassword = var.postgres_password
    }
    primary = {
      persistence = {
        size = var.persistence_size
      }
    }
  })]
}

resource "helm_release" "neo4j" {
  name       = "neo4j"
  namespace  = kubernetes_namespace.middleware.metadata[0].name
  repository = "https://helm.neo4j.com/neo4j"
  chart      = "neo4j"
  version    = "5.26.0"

  values = [yamlencode({
    neo4j = {
      name                   = "circleguard-neo4j"
      password               = var.neo4j_password
      edition                = "community"
      acceptLicenseAgreement = "yes"
    }
    volumes = {
      data = {
        mode = "defaultStorageClass"
        defaultStorageClass = {
          requests = {
            storage = var.persistence_size
          }
        }
      }
    }
  })]
}

resource "helm_release" "kafka" {
  name       = "kafka"
  namespace  = kubernetes_namespace.middleware.metadata[0].name
  repository = "https://charts.bitnami.com/bitnami"
  chart      = "kafka"
  version    = "30.0.4"

  values = [yamlencode({
    listeners = {
      client = {
        protocol = "PLAINTEXT"
      }
      controller = {
        protocol = "PLAINTEXT"
      }
      interbroker = {
        protocol = "PLAINTEXT"
      }
    }
    controller = {
      persistence = {
        size = var.persistence_size
      }
    }
  })]
}

resource "helm_release" "redis" {
  name       = "redis"
  namespace  = kubernetes_namespace.middleware.metadata[0].name
  repository = "https://charts.bitnami.com/bitnami"
  chart      = "redis"
  version    = "20.0.3"

  values = [yamlencode({
    auth = {
      password = var.redis_password
    }
    master = {
      persistence = {
        size = var.persistence_size
      }
    }
    replica = {
      replicaCount = 1
      persistence = {
        size = var.persistence_size
      }
    }
  })]
}

resource "helm_release" "openldap" {
  name       = "openldap"
  namespace  = kubernetes_namespace.middleware.metadata[0].name
  repository = "https://jp-gouin.github.io/helm-openldap"
  chart      = "openldap-stack-ha"
  version    = "4.3.0"

  values = [yamlencode({
    global = {
      adminUser     = var.ldap_admin_user
      adminPassword = var.ldap_admin_password
      ldapDomain    = var.ldap_domain
    }
    replicaCount = 1
    persistence = {
      size = var.persistence_size
    }
  })]
}
