terraform {
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.25"
    }
  }
}

provider "kubernetes" {
  config_path = "~/.kube/config"
}

# Primer za jedan servis (dodaješ dalje sve po istom šablonu)
resource "kubernetes_manifest" "eureka_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/eureka-deployment.yaml"))
}

resource "kubernetes_manifest" "eureka_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/eureka-service.yaml"))
}

resource "kubernetes_manifest" "prometheus_configmap" {
  manifest = yamldecode(file("${path.module}/yaml/configs/prometheus-configmap.yaml"))
}

resource "kubernetes_manifest" "prometheus_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/prometheus-deployment.yaml"))
}

resource "kubernetes_manifest" "prometheus_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/prometheus-service.yaml"))
}

resource "kubernetes_manifest" "grafana_configmap" {
  manifest = yamldecode(file("${path.module}/yaml/configs/grafana-configmap.yaml"))
}

resource "kubernetes_manifest" "grafana_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/grafana-deployment.yaml"))
}

resource "kubernetes_manifest" "grafana_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/grafana-service.yaml"))
}

resource "kubernetes_manifest" "nats_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/nats-deployment.yaml"))
}

resource "kubernetes_manifest" "nats_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/nats-service.yaml"))
}

resource "kubernetes_manifest" "postgres_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/postgres-deployment.yaml"))
}

resource "kubernetes_manifest" "postgres_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/postgres-service.yaml"))
}

resource "kubernetes_manifest" "auth_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/auth-service-deployment.yaml"))
}

resource "kubernetes_manifest" "auth_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/auth-service.yaml"))
}

resource "kubernetes_manifest" "customer_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/customer-service-deployment.yaml"))
}

resource "kubernetes_manifest" "customer_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/customer-service.yaml"))
}

resource "kubernetes_manifest" "cart_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/cart-service-deployment.yaml"))
}

resource "kubernetes_manifest" "cart_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/cart-service.yaml"))
}

resource "kubernetes_manifest" "apigw_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/api-gateway-deployment.yaml"))
}

resource "kubernetes_manifest" "apigw_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/api-gateway-service.yaml"))
}

resource "kubernetes_manifest" "order_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/order-service-deployment.yaml"))
}

resource "kubernetes_manifest" "order_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/order-service.yaml"))
}

resource "kubernetes_manifest" "product_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/product-service-deployment.yaml"))
}

resource "kubernetes_manifest" "product_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/product-service.yaml"))
}

resource "kubernetes_manifest" "angular_deployment" {
  manifest = yamldecode(file("${path.module}/yaml/deployments/angular-ecommerce-deployment.yaml"))
}

resource "kubernetes_manifest" "angular_service" {
  manifest = yamldecode(file("${path.module}/yaml/services/angular-ecommerce-service.yaml"))
}

resource "kubernetes_manifest" "service_reader_role" {
  manifest = yamldecode(file("${path.module}/yaml/service-role.yaml"))
}

resource "kubernetes_manifest" "service_reader_binding" {
  manifest = yamldecode(file("${path.module}/yaml/service-binding.yaml"))
  depends_on = [kubernetes_manifest.service_reader_role]
}













# ... dodaješ ostale
