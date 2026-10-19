$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:8181"
$token = (Invoke-RestMethod -Method Post -Uri "$base/api/user/login/signin" -ContentType "application/json" -Body '{"username":"testbuyer","password":"admin123"}').data.token
$h = @{ Authorization = "Bearer $token" }
$adminToken = (Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8080/adminapi/login/signin" -ContentType "application/json" -Body '{"username":"admin","password":"admin123","loginType":"password","captchaUid":"","verifyToken":null}').data.token
$ah = @{ Authorization = "Bearer $adminToken" }

function Clear-Cart { Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/clear" -Headers $h | Out-Null }
function Add-Product($id, $qty) {
    Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/addToCart" -Headers $h -ContentType "application/json" -Body (@{ productId = $id; quantity = $qty } | ConvertTo-Json -Compress) | Out-Null
}

# 1) 提交定金预售订单（商品 264：32 元，固定定金 5 元）
Clear-Cart
Add-Product 264 1
$submit = @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 0; useCouponIds = @(); selectUserCouponIds = @(); buyerNote = "presale deposit" } | ConvertTo-Json -Compress -Depth 5
$order = Invoke-RestMethod -Method Post -Uri "$base/api/order/check/submit" -Headers $h -ContentType "application/json" -Body $submit
$oid = $order.data.orderId
Write-Host ("1 submit: id={0} total={1} online={2} (expect total 32, online 5)" -f $oid, $order.data.totalAmount, $order.data.onlineAmount)

# 2) 定金离线支付单 + 后台确认
$pay1 = Invoke-RestMethod -Uri "$base/api/order/pay/create?id=$oid&type=offline" -Headers $h
Write-Host ("2 deposit paylog amount={0} (expect 5)" -f $pay1.data.orderAmount)
Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8080/adminapi/order/order/setPaid" -Headers $ah -ContentType "application/json" -Body (@{ id = $oid; description = "deposit paid" } | ConvertTo-Json -Compress) | Out-Null
Write-Host "3 deposit confirmed"

# 3) 尾款离线支付单 + 后台确认
$pay2 = Invoke-RestMethod -Uri "$base/api/order/pay/create?id=$oid&type=offline" -Headers $h
Write-Host ("4 final paylog amount={0} (expect 27)" -f $pay2.data.orderAmount)
Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8080/adminapi/order/order/setPaid" -Headers $ah -ContentType "application/json" -Body (@{ id = $oid; description = "final paid" } | ConvertTo-Json -Compress) | Out-Null
Write-Host "5 final confirmed"
Write-Host "PRESALE SMOKE DONE order=$oid"
