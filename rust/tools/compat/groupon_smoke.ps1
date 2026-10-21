$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:8181"
$token = (Invoke-RestMethod -Method Post -Uri "$base/api/user/login/signin" -ContentType "application/json" -Body '{"username":"testbuyer","password":"admin123"}').data.token
$h = @{ Authorization = "Bearer $token" }
function Clear-Cart { Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/clear" -Headers $h | Out-Null }
function Add-Groupon($productId, $promoId, $itemId, $recordId) {
    $groupon = @{ grouponPromotionId = $promoId; grouponPromotionItemId = $itemId; groupNumKey = 1; grouponPromotionRecordId = $recordId }
    $body = @{ productId = $productId; quantity = 1; grouponData = $groupon } | ConvertTo-Json -Compress -Depth 5
    Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/addToCart" -Headers $h -ContentType "application/json" -Body $body | Out-Null
}
function Submit {
    $body = @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 0; useCouponIds = @(); selectUserCouponIds = @(); buyerNote = "groupon" } | ConvertTo-Json -Compress -Depth 5
    return Invoke-RestMethod -Method Post -Uri "$base/api/order/check/submit" -Headers $h -ContentType "application/json" -Body $body
}

# A. 开团
Clear-Cart
Add-Groupon 136 900001 900001 0
$check = @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 0; useCouponIds = @(); selectUserCouponIds = @() } | ConvertTo-Json -Compress -Depth 5
$r = Invoke-RestMethod -Method Post -Uri "$base/api/order/check/index" -Headers $h -ContentType "application/json" -Body $check
Write-Host ("A checkout: product={0} promotionDiscount={1} price={2} (expect 10/25/10)" -f $r.data.total.productAmount, $r.data.total.discountProductPromotionAmount, $r.data.cartList[0].carts[0].price)
$o1 = Submit
$rid = $o1.data.grouponPromotionRecordId
Write-Host ("B open: order={0} total={1} recordId={2}" -f $o1.data.orderId, $o1.data.totalAmount, $rid)
if (-not $rid -or $rid -eq 0) { throw "open group record id missing" }

# B. 参团（同一用户自参团用于状态机验证）
Clear-Cart
Add-Groupon 136 900001 900001 $rid
$o2 = Submit
Write-Host ("C join: order={0} total={1} recordId={2}" -f $o2.data.orderId, $o2.data.totalAmount, $o2.data.grouponPromotionRecordId)

# C. 过期团
Clear-Cart
Add-Groupon 6 900002 900002 0
$o3 = Submit
Write-Host ("D expire-open: order={0} recordId={1}" -f $o3.data.orderId, $o3.data.grouponPromotionRecordId)
Start-Sleep -Seconds 10
Write-Host "GROUPON SMOKE DONE"
