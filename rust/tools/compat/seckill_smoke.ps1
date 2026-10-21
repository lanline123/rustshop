$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:8181"
$token = (Invoke-RestMethod -Method Post -Uri "$base/api/user/login/signin" -ContentType "application/json" -Body '{"username":"testbuyer","password":"admin123"}').data.token
$h = @{ Authorization = "Bearer $token" }
function Clear-Cart { Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/clear" -Headers $h | Out-Null }
function Add-Product($id, $qty) {
    Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/addToCart" -Headers $h -ContentType "application/json" -Body (@{ productId = $id; quantity = $qty } | ConvertTo-Json -Compress) | Out-Null
}

Clear-Cart
Add-Product 6 1
$check = @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 0; useCouponIds = @(); selectUserCouponIds = @() } | ConvertTo-Json -Compress -Depth 5
$r = Invoke-RestMethod -Method Post -Uri "$base/api/order/check/index" -Headers $h -ContentType "application/json" -Body $check
Write-Host ("A checkout: productAmount={0} seckillDiscount={1} total={2} price={3} (expect 50 / 28 / 50 / 50)" -f $r.data.total.productAmount, $r.data.total.discountSeckillAmount, $r.data.total.totalAmount, $r.data.cartList[0].carts[0].price)

$submit = @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 0; useCouponIds = @(); selectUserCouponIds = @(); buyerNote = "seckill" } | ConvertTo-Json -Compress -Depth 5
$order = Invoke-RestMethod -Method Post -Uri "$base/api/order/check/submit" -Headers $h -ContentType "application/json" -Body $submit
Write-Host ("B submit: id={0} total={1} (expect 50)" -f $order.data.orderId, $order.data.totalAmount)

Clear-Cart
Add-Product 6 1
try {
    Invoke-RestMethod -Method Post -Uri "$base/api/order/check/submit" -Headers $h -ContentType "application/json" -Body $submit | Out-Null
    Write-Host "C limit: FAILED (second order allowed)"
} catch {
    Write-Host ("C limit: rejected -> {0}" -f $_.ErrorDetails.Message)
}
Write-Host "SECKILL SMOKE DONE"
