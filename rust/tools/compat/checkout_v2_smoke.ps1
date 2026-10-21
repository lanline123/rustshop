$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:8181"
$token = (Invoke-RestMethod -Method Post -Uri "$base/api/user/login/signin" -ContentType "application/json" -Body '{"username":"testbuyer","password":"admin123"}').data.token
$headers = @{ Authorization = "Bearer $token" }

function Clear-Cart {
    Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/clear" -Headers $headers | Out-Null
}
function Add-Product($id, $qty) {
    $body = @{ productId = $id; quantity = $qty } | ConvertTo-Json -Compress
    Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/addToCart" -Headers $headers -ContentType "application/json" -Body $body | Out-Null
}
function Check($obj) {
    $json = $obj | ConvertTo-Json -Compress -Depth 8
    return Invoke-RestMethod -Method Post -Uri "$base/api/order/check/index" -Headers $headers -ContentType "application/json" -Body $json
}

# A. shipping template: product 136 (35 CNY, template 900001 weight 2.5kg => 2 + 2*1 = 4)
Clear-Cart
Add-Product 136 1
$r = Check @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 0 }
Write-Host ("A shipping: product={0} fee={1} total={2} storeFee={3}" -f $r.data.total.productAmount, $r.data.total.shippingFee, $r.data.total.totalAmount, ($r.data.total.storeShippingFee | ConvertTo-Json -Compress))

# B. points + coupon: product 4 (273 CNY), usePoint=100, coupon 900001 (10 CNY)
Clear-Cart
Add-Product 4 1
$r = Check @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 100; useBalance = 0; useCredit = 0; useCouponIds = @(900001); selectUserCouponIds = @(900001) }
$b = $r.data
Write-Host ("B points/coupon: product={0} coupon={1} points={2} availablePoints={3} usedPoint={4} total={5} unpaid={6}" -f $b.total.productAmount, $b.total.couponAmount, $b.total.pointsAmount, $b.availablePoints, $b.item.usePoint, $b.total.totalAmount, $b.total.unpaidAmount)

# C. shop-scope coupon 900002 (shop 1) should be disabled for shop 0 cart
$r2 = Check @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 0 }
$disableIds = @($r2.data.couponList.disableCoupons | ForEach-Object { $_.couponId })
Write-Host ("C coupon scope: enable={0} disable={1} disabledIds={2} shopCouponDisabled={3}" -f @($r2.data.couponList.enableCoupons).Count, $disableIds.Count, ($disableIds -join ','), ($disableIds -contains 900002))

# D. credit: useCredit=1, credit info + creditAmount
$r3 = Check @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 1; useCouponIds = @(900001); selectUserCouponIds = @(900001) }
Write-Host ("D credit: canUse={0} available={1} creditAmount={2} unpaid={3}" -f $r3.data.creditPaymentInfo.canUseCredit, $r3.data.creditPaymentInfo.availableAmount, $r3.data.total.creditAmount, $r3.data.total.unpaidAmount)

# E. presale: product 264 (deposit presale, fixed deposit 5)
Clear-Cart
Add-Product 264 1
$r4 = Check @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 0 }
Write-Host ("E presale: saleMode={0} deposit={1} final={2} productAmount={3}" -f $r4.data.productPresale.saleMode, $r4.data.productPresale.depositAmount, $r4.data.productPresale.finalPaymentAmount, $r4.data.productPresale.productAmount)

# F. submit: product 4 + coupon 900001 + 100 points
Clear-Cart
Add-Product 4 1
$submitBody = @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 100; useBalance = 0; useCredit = 0; useCouponIds = @(900001); selectUserCouponIds = @(900001); buyerNote = "checkout v2" } | ConvertTo-Json -Compress -Depth 5
$order = Invoke-RestMethod -Method Post -Uri "$base/api/order/check/submit" -Headers $headers -ContentType "application/json" -Body $submitBody
Write-Host ("F order: id={0} total={1} online={2}" -f $order.data.orderId, $order.data.totalAmount, $order.data.onlineAmount)
Write-Host "V2 SMOKE DONE"
