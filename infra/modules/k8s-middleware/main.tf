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
  repository = "oci://registry-1.docker.io/bitnamicharts"
  chart      = "postgresql"
  version    = "18.7.3"

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
      resources = {
        requests = {
          cpu    = "50m"
          memory = "256Mi"
        }
        limits = {
          cpu    = "500m"
          memory = "512Mi"
        }
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
      resources = {
        requests = {
          cpu    = "500m"
          memory = "2Gi"
        }
        limits = {
          cpu    = "1000m"
          memory = "4Gi"
        }
      }
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
  repository = "oci://registry-1.docker.io/bitnamicharts"
  chart      = "kafka"
  version    = "29.3.6"

  values = [yamlencode({
    image = {
      registry   = "docker.io"
      repository = "bitnamilegacy/kafka"
      tag        = "3.7.1-debian-12-r0"
    }
    volumePermissions = {
      enabled = false
    }
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
      replicaCount = 1
      persistence = {
        size = var.persistence_size
      }
      resources = {
        requests = {
          cpu    = "100m"
          memory = "512Mi"
        }
        limits = {
          cpu    = "500m"
          memory = "768Mi"
        }
      }
    }
    broker = {
      replicaCount = 0
    }
    zookeeper = {
      enabled = false
    }
  })]
}

resource "helm_release" "redis" {
  name       = "redis"
  namespace  = kubernetes_namespace.middleware.metadata[0].name
  repository = "oci://registry-1.docker.io/bitnamicharts" # Cambiado
  chart      = "redis"
  version    = "27.0.8"

  values = [yamlencode({
    auth = {
      password = var.redis_password
    }
    master = {
      persistence = {
        size = var.persistence_size
      }
      resources = {
        requests = {
          cpu    = "50m"
          memory = "256Mi"
        }
      }
    }
    replica = {
      replicaCount = 0
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
    "ltb-passwd" = {
      enabled = false
    }
    "phpldapadmin" = {
      enabled = true
    }
    resources = {
      requests = {
        cpu    = "25m"
        memory = "128Mi"
      }
      limits = {
        cpu    = "200m"
        memory = "256Mi"
      }
    }
  })]
}