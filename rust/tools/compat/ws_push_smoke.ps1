$ErrorActionPreference = "Stop"

function Get-Token($url, $body) {
    $resp = Invoke-RestMethod -Method Post -Uri $url -ContentType "application/json" -Body $body
    return $resp.data.token
}

$memberToken = Get-Token "http://127.0.0.1:8181/api/user/login/signin" '{"username":"testbuyer","password":"admin123"}'
Write-Host "member token ok"

$wsUrl = 'ws://127.0.0.1:8181/ws?token=' + $memberToken + '&platform=pc'
$ws = [System.Net.WebSockets.ClientWebSocket]::new()
$buf = New-Object byte[] 8192
$ws.ConnectAsync([Uri]$wsUrl, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
$seg = New-Object System.ArraySegment[byte] -ArgumentList @(,$buf)
$r = $ws.ReceiveAsync($seg, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
$welcome = [System.Text.Encoding]::UTF8.GetString($buf, 0, $r.Count)
Write-Host "welcome: $welcome"

$headers = @{ Authorization = "Bearer $memberToken" }
$pushUrl = 'http://127.0.0.1:8181/api/socket/send?targetUserId=false::1&message=hello-from-server'
$resp = Invoke-RestMethod -Method Get -Uri $pushUrl -Headers $headers
Write-Host "send-api: $($resp.data)"

$r2 = $ws.ReceiveAsync($seg, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
$pushed = [System.Text.Encoding]::UTF8.GetString($buf, 0, $r2.Count)
Write-Host "pushed: $pushed"
if ($pushed -ne "hello-from-server") { throw "push mismatch: $pushed" }

$offlineUrl = 'http://127.0.0.1:8181/api/socket/send?targetUserId=false::99999&message=x'
$offline = Invoke-RestMethod -Method Get -Uri $offlineUrl -Headers $headers
Write-Host "offline-send: $($offline.data)"
if (-not $offline.data.Contains("99999")) { throw "offline response mismatch" }

$ws.Dispose()
Write-Host "WS PUSH SMOKE OK"
