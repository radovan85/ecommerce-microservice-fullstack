Write-Host "`n🧹 Stopping kubectl port-forward processes from known service ports..."

# Servisi i portovi — isti kao u start skripti
$services = @{
    "api-gateway"       = 8080
    "auth-service"      = 8081
    "cart-service"      = 8082
    "customer-service"  = 8083
    "order-service"     = 9000
    "product-service"   = 9001
    "eureka-server"     = 8761
    "grafana"           = 3001
    "prometheus"        = 9090
    "nats"              = 4222
    "angular-ecommerce" = 4200
    "postgres"          = 5432
}

$killed = @()

foreach ($svc in $services.Keys) {
    $port = $services[$svc]
    $lines = netstat -aon | findstr ":$port"

    foreach ($line in $lines) {
        if ($line -match "\s+(\d+)$") {
            $procId = $Matches[1]
            $task = tasklist | findstr " $procId"

            if ($task -match "kubectl") {
                Write-Host "🛑 Killing kubectl port-forward: $svc on port $port (PID $procId)..."
                try {
                    taskkill /PID $procId /F | Out-Null
                    $killed += "${svc}:${port}"
                } catch {
                    Write-Host "⚠️ Failed to kill PID $procId"
                }
            }
        }
    }
}

if ($killed.Count -gt 0) {
    Write-Host "`n✅ Terminated: $($killed -join ', ')"
} else {
    Write-Host "`n👌 No matching kubectl port-forward processes found."
}

Write-Host "`n🎺 Port-forward shutdown complete. You can now restart cleanly."
