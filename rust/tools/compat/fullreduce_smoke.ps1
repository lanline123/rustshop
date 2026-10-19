$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:8181"
$token = (Invoke-RestMethod -Method Post -Uri "$base/api/user/login/signin" -ContentType "application/json" -Body '{"username":"testbuyer","password":"admin123"}').data.token
$h = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/clear" -Headers $h | Out-Null
Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/addToCart" -Headers $h -ContentType "application/json" -Body '{"productId":8,"quantity":1}' | Out-Null
$check = @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 0; useCouponIds = @(); selectUserCouponIds = @() } | ConvertTo-Json -Compress -Depth 5
$r = Invoke-RestMethod -Method Post -Uri "$base/api/order/check/index" -Headers $h -ContentType "application/json" -Body $check
Write-Host ("A checkout: product={0} fullReduceDiscount={1} price={2} (expect 387 / 103 / 387 (含演示数据已有满50减10))" -f $r.data.total.productAmount, $r.data.total.discountDiscountAmount, $r.data.cartList[0].carts[0].price)
$submit = @{ flowType = 1; addressId = 900001; payTypeId = 1; usePoint = 0; useBalance = 0; useCredit = 0; useCouponIds = @(); selectUserCouponIds = @(); buyerNote = "fullreduce" } | ConvertTo-Json -Compress -Depth 5
$order = Invoke-RestMethod -Method Post -Uri "$base/api/order/check/submit" -Headers $h -ContentType "application/json" -Body $submit
Write-Host ("B order: id={0} total={1} (expect 387)" -f $order.data.orderId, $order.data.totalAmount)
Write-Host "FULLREDUCE SMOKE DONE"
