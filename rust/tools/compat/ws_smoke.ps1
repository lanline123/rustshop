$ErrorActionPreference = "Stop"

function Get-Token($url, $body) {
    $resp = Invoke-RestMethod -Method Post -Uri $url -ContentType "application/json" -Body $body
    return $resp.data.token
}

Write-Host "=== login ==="
$adminToken = Get-Token "http://127.0.0.1:8080/adminapi/login/signin" '{"username":"admin","password":"admin123","loginType":"password","captchaUid":"","verifyToken":null}'
$memberToken = Get-Token "http://127.0.0.1:8181/api/user/login/signin" '{"username":"testbuyer","password":"admin123"}'
Write-Host "tokens ok"

function Receive-Text($ws, $buf) {
    $seg = New-Object System.ArraySegment[byte] -ArgumentList @(,$buf)
    $r = $ws.ReceiveAsync($seg, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
    return [System.Text.Encoding]::UTF8.GetString($buf, 0, $r.Count)
}

function Test-Ws($url, $label, $expectWelcome) {
    Write-Host "=== $label ==="
    $ws = [System.Net.WebSockets.ClientWebSocket]::new()
    $buf = New-Object byte[] 8192
    $ws.ConnectAsync([Uri]$url, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
    $msg = Receive-Text $ws $buf
    Write-Host "recv: $msg"
    if ($expectWelcome) {
        if (-not $msg.Contains("userId")) { throw "welcome mismatch: $msg" }
        $bytes = [System.Text.Encoding]::UTF8.GetBytes('{"type":"heartBeat","data":{}}')
        $seg = New-Object System.ArraySegment[byte] -ArgumentList @(,$bytes)
        $ws.SendAsync($seg, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult() | Out-Null
        $echo = Receive-Text $ws $buf
        Write-Host "echo: $echo"
        if ($echo -ne '{"type":"heartBeat","data":{}}') { throw "heartbeat echo mismatch: $echo" }
    }
    $ws.Dispose()
    Write-Host "OK"
}

Test-Ws "ws://127.0.0.1:8080/ws?token=$adminToken&platform=admin" "admin ws (8080)" $true
Test-Ws "ws://127.0.0.1:8181/ws?token=$memberToken&platform=pc" "member ws (8181)" $true
Test-Ws "ws://127.0.0.1:8080/ws?token=bad-token&platform=admin" "invalid token" $false
Write-Host "WS SMOKE OK"
