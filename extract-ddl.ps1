$content = Get-Content 'java\database\pure.sql'
$tables = @('time_discount','time_discount_item','seckill','seckill_item','points_exchange','product_gift','sign_in_setting','recharge_setting','groupon_promotion','groupon_promotion_item','groupon_promotion_record')
foreach ($t in $tables) {
    $needle = 'CREATE TABLE `' + $t + '` ('
    $found = $false
    for ($i=0; $i -lt $content.Count; $i++) {
        if ($content[$i].Trim() -eq $needle) {
            Write-Output ("===== {0} (line {1}) =====" -f $t, ($i+1))
            for ($j=$i; $j -lt [Math]::Min($i+45, $content.Count); $j++) {
                Write-Output $content[$j]
                if ($content[$j] -match '^\)') { break }
            }
            $found = $true
            break
        }
    }
    if (-not $found) { Write-Output ("===== {0} NOT FOUND =====" -f $t) }
}
