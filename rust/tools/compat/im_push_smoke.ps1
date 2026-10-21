$ErrorActionPreference = "Stop"

function Get-Token($url, $body) {
    $resp = Invoke-RestMethod -Method Post -Uri $url -ContentType "application/json" -Body $body
    return $resp.data.token
}

function Receive-Text($ws, $buf, $timeoutMs = 8000) {
    $seg = New-Object System.ArraySegment[byte] -ArgumentList @(,$buf)
    $task = $ws.ReceiveAsync($seg, [System.Threading.CancellationToken]::None)
    if (-not $task.Wait($timeoutMs)) { return $null }
    $r = $task.Result
    return [System.Text.Encoding]::UTF8.GetString($buf, 0, $r.Count)
}

function Send-Ws($ws, $text) {
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($text)
    $seg = New-Object System.ArraySegment[byte] -ArgumentList @(,$bytes)
    $ws.SendAsync($seg, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult() | Out-Null
}

$adminToken = Get-Token "http://127.0.0.1:8080/adminapi/login/signin" '{"username":"admin","password":"admin123","loginType":"password","captchaUid":"","verifyToken":null}'
$memberToken = Get-Token "http://127.0.0.1:8181/api/user/login/signin" '{"username":"testbuyer","password":"admin123"}'
Write-Host "tokens ok"

$adminWs = [System.Net.WebSockets.ClientWebSocket]::new()
$adminWs.ConnectAsync([Uri]"ws://127.0.0.1:8080/ws?token=$adminToken&platform=admin", [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
$buf = New-Object byte[] 16384
Write-Host "admin ws: $(Receive-Text $adminWs $buf)"

$memberWs = [System.Net.WebSockets.ClientWebSocket]::new()
$memberWs.ConnectAsync([Uri]"ws://127.0.0.1:8181/ws?token=$memberToken&platform=pc", [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
Write-Host "member ws: $(Receive-Text $memberWs $buf)"

$headers = @{ Authorization = "Bearer $memberToken" }
$sendBody = '{"shopId":0,"content":{"messageType":"text","content":"hello from member"}}'
$send = Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8181/im/conversation/message/send" -Headers $headers -ContentType "application/json" -Body $sendBody
$conversationId = $send.data.conversationId
Write-Host "member sent, conversationId=$conversationId"

$adminRecv = Receive-Text $adminWs $buf
Write-Host "admin recv: $adminRecv"
if (-not $adminRecv -or -not $adminRecv.Contains('"type":"message"')) { throw "admin did not receive member message" }

$adminHeaders = @{ Authorization = "Bearer $adminToken"; "x-shop-id" = "0" }
$replyBody = '{"conversationId":' + $conversationId + ',"shopId":0,"content":{"messageType":"text","content":"hello from admin"}}'
$reply = Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8080/im/conversation/message/send" -Headers $adminHeaders -ContentType "application/json" -Body $replyBody
Write-Host "admin replied, messageId=$($reply.data.id)"

$memberRecv = Receive-Text $memberWs $buf
Write-Host "member recv: $memberRecv"
if (-not $memberRecv -or -not $memberRecv.Contains("hello from admin")) { throw "member did not receive admin reply" }

$adminWs.Dispose()
$memberWs.Dispose()
Write-Host "IM PUSH SMOKE OK"
