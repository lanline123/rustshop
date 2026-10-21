$ErrorActionPreference = "Continue"

function Login($user) {
    $body = '{"username":"' + $user + '","password":"admin123","loginType":"password","captchaUid":"","verifyToken":null}'
    Set-Content -Path "D:\tigshop-mysql\rbac_login.json" -Value $body -NoNewline
    $raw = curl.exe -s -X POST "http://127.0.0.1:8080/adminapi/login/signin" -H "Content-Type: application/json" --data-binary "@D:\tigshop-mysql\rbac_login.json"
    $obj = $raw | ConvertFrom-Json
    if ($obj.code -ne 0) { Write-Host "login $user failed: $raw"; return $null }
    return $obj.data.token
}

function Show($label, $token, $path) {
    $code = curl.exe -s -o "D:\tigshop-mysql\rbac_resp.json" -w "%{http_code}" -H "Authorization: Bearer $token" ("http://127.0.0.1:8080" + $path)
    $body = Get-Content "D:\tigshop-mysql\rbac_resp.json" -Raw -ErrorAction SilentlyContinue
    if ($body.Length -gt 120) { $body = $body.Substring(0, 120) }
    Write-Host "$label -> HTTP $code $body"
}

$tAdmin = Login "admin"
Write-Host "=== admin (auth_list all) ==="
Show "product/list" $tAdmin "/adminapi/product/product/list?page=1&size=2"
Show "order/list  " $tAdmin "/adminapi/order/order/list?page=1&size=2"

$tOp = Login "rbac_op"
Write-Host "=== rbac_op (only orderExport) ==="
Show "product/list(need productManage/decorate)" $tOp "/adminapi/product/product/list?page=1&size=2"
Show "order/list(no perm required)          " $tOp "/adminapi/order/order/list?page=1&size=2"

$tOp2 = Login "rbac_op2"
Write-Host "=== rbac_op2 (only productManage) ==="
Show "product/list" $tOp2 "/adminapi/product/product/list?page=1&size=2"
