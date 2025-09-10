# 🎺 Poziv cleanup skripte pre pokretanja
$cleanupScriptPath = "$PSScriptRoot\cleanup_ports.ps1"
if (Test-Path $cleanupScriptPath) {
    Write-Host "`n🧹 Cleaning up existing port-forward processes..."
    & $cleanupScriptPath
} else {
    Write-Host "`n⚠️ Cleanup script not found at: $cleanupScriptPath"
}

Write-Host "`n🔄 Starting port-forward for backend services..."

# Servisi i portovi
$services = @{
    "api-gateway"       = 8080
    "auth-service"      = 8081
    "cart-service"      = 8082
    "customer-service"  = 8083
    "order-service"     = 9002
    "product-service"   = 9001
    "eureka-server"     = 8761
    "grafana"           = 3001
    "prometheus"        = 9090
    "nats"              = 4222
    "angular-ecommerce" = 4200
    "postgres"          = 5432
}

# Broj pokušaja po servisu
$maxAttempts = 5
$waitSeconds = 3

foreach ($svc in $services.Keys) {
    $port = $services[$svc]
    $attempt = 0

    while ($attempt -lt $maxAttempts) {
        $status = kubectl get pod -l app=$svc -o jsonpath='{.items[0].status.phase}' 2>$null

        if ($status -eq "Running") {
            Write-Host "✅ $svc → pod running → forwarding localhost:$port"
            Start-Process powershell -WindowStyle Hidden -ArgumentList @(
                "-Command", "kubectl port-forward svc/$svc ${port}:${port}"
            )
            break
        }
        else {
            Write-Host "⏳ $svc → attempt $($attempt+1) → status: '$status', retrying in ${waitSeconds}s..."
            Start-Sleep -Seconds $waitSeconds
            $attempt++
        }
    }

    if ($attempt -eq $maxAttempts) {
        Write-Host "❌ $svc → pod not ready after $maxAttempts attempts, skipping..."
    }
}

Write-Host "`n🎷 All port-forward commands initiated. Access services like: http://localhost:8080"