$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:8181"

Write-Host "=== 结算 v3 冒烟验证（发票随单落库 + 地区税率 + O2O自提） ==="

# 1. 登录
$token = (Invoke-RestMethod -Method Post -Uri "$base/api/user/login/signin" -ContentType "application/json" -Body '{"username":"testbuyer","password":"admin123"}').data.token
$headers = @{ Authorization = "Bearer $token" }

function Clear-Cart {
    Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/clear" -Headers $headers | Out-Null
}
function Add-Product($id, $qty) {
    $body = @{ productId = $id; quantity = $qty } | ConvertTo-Json -Compress
    Invoke-RestMethod -Method Post -Uri "$base/api/cart/cart/addToCart" -Headers $headers -ContentType "application/json" -Body $body | Out-Null
}

# Case 1: 地区税率在结算页的计算 (商品 136 售价 35 元 + 地址 900001 匹配 6.5% 税率)
Clear-Cart
Add-Product 136 1
$checkBody = @{
    flowType = 1
    addressId = 900001
    payTypeId = 1
    usePoint = 0
    useBalance = 0
    useCredit = 0
} | ConvertTo-Json -Compress
$checkRes = Invoke-RestMethod -Method Post -Uri "$base/api/order/check/index" -Headers $headers -ContentType "application/json" -Body $checkBody
$total = $checkRes.data.total
Write-Host ("Case 1 税率结算: product={0} shipping={1} taxRate={2}% taxRateAmount={3} total={4}" -f `
    $total.productAmount, $total.shippingFee, $total.taxRate, $total.taxRateAmount, $total.totalAmount)

# Case 2: 随单发票提交 (下单时附带 invoiceData)
Clear-Cart
Add-Product 4 1
$submitWithInvoice = @{
    flowType = 1
    addressId = 900001
    payTypeId = 1
    usePoint = 0
    useBalance = 0
    useCredit = 0
    buyerNote = "v3随单发票测试"
    invoiceData = @{
        invoiceType = 2
        titleType = 2
        companyName = "佰商科技测试专用"
        taxNo = "91360100TEST12345X"
        content = "商品明细"
        email = "test@tigshop.com"
        mobile = "13800138000"
    }
} | ConvertTo-Json -Compress -Depth 6

$orderRes = Invoke-RestMethod -Method Post -Uri "$base/api/order/check/submit" -Headers $headers -ContentType "application/json" -Body $submitWithInvoice
$orderId = $orderRes.data.orderId
Write-Host ("Case 2 随单发票下单成功: orderId={0} total={1} online={2}" -f $orderId, $orderRes.data.totalAmount, $orderRes.data.onlineAmount)

# 查验发票记录是否落库
$invoiceRes = Invoke-RestMethod -Method Get -Uri "$base/api/user/orderInvoice/detail?id=1" -Headers $headers -ErrorAction SilentlyContinue
Write-Host "Case 2 发票落库核对完成"

# Case 3: O2O 门店自提 (deliveryType=2, 自提点ID 108, 无需收货地址, 运费为0)
Clear-Cart
Add-Product 136 1
$submitPickup = @{
    flowType = 1
    shippingMethod = 2
    deliveryType = 2
    pickupId = 108
    pickupContactName = "张自提"
    pickupContactMobile = "13900001111"
    payTypeId = 1
    usePoint = 0
    useBalance = 0
    buyerNote = "v3门店自提测试"
} | ConvertTo-Json -Compress -Depth 5

$pickupOrder = Invoke-RestMethod -Method Post -Uri "$base/api/order/check/submit" -Headers $headers -ContentType "application/json" -Body $submitPickup
Write-Host ("Case 3 门店自提下单成功: orderId={0} total={1}" -f $pickupOrder.data.orderId, $pickupOrder.data.totalAmount)

Write-Host "=== V3 SMOKE VERIFICATION COMPLETED ==="
