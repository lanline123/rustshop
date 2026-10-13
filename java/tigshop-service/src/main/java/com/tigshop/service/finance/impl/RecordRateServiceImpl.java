package com.tigshop.service.finance.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.enums.finance.RateSourceEnum;
import com.tigshop.bean.enums.finance.RuleTargetTypeEnum;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleMatchVO;
import com.tigshop.mapper.finance.RecordRateMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.CategoryFeeRuleMatchService;
import com.tigshop.service.finance.RecordRateService;
import com.tigshop.service.setting.ConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.setting.SettingsEnum.SUPPLIER_GENERAL_SERVICE_FEE_RATE;

/**
 * 费率记录接口实现
 * <p>
 * 订单(recordType=ORDER)：按 OrderItem 品类聚合 + 类目费率规则匹配 + 加权服务费率，明细写入 category_rate_details。
 * 提现等：仅全局配置 + shopType 区分门店，不写 category_rate_details；不读取 shop/vendor 表费率覆盖（D1'）。
 *
 * @author Tigshop项目组
 */
@Service
@RequiredArgsConstructor
public class RecordRateServiceImpl extends BaseServiceImpl<RecordRateMapper, RecordRate> implements RecordRateService {

    private final ShopMapper shopMapper;
    private final ConfigService configService;
    private final CategoryFeeRuleMatchService categoryFeeRuleMatchService;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRecordRate(int recordId, int recordType, int shopId, int vendorId) {
        if (recordType == StatementType.ORDER.getCode()) {
            saveOrderRecordRate(recordId, shopId, vendorId);
            return;
        }
        saveNonOrderRecordRate(recordId, recordType, shopId, vendorId);
    }

    /**
     * 行金额口径：OrderItem.price × quantity（与订单行小计一致，不含全单级分摊优惠）。
     */
    private void saveOrderRecordRate(int orderId, int shopId, int vendorId) {
        RecordRate recordRate = new RecordRate();
        recordRate.setRecordId(orderId);
        recordRate.setRecordType(StatementType.ORDER.getCode());
        recordRate.setShopId(shopId);
        recordRate.setVendorId(vendorId);

        BigDecimal storeGeneralSvc = new BigDecimal(configService.getConfigVal(SettingsEnum.STORE_GENERAL_SERVICE_FEE_RATE));
        BigDecimal storefrontGeneralSvc = new BigDecimal(configService.getConfigVal(SettingsEnum.STOREFRONT_GENERAL_SERVICE_FEE_RATE));

        String shopWithdrawVal = configService.getConfigVal(SettingsEnum.STORE_WITHDRAWAL_FEE_RATE);
        BigDecimal shopWithdraw = new BigDecimal(shopWithdrawVal);
        String shopfrontWithdrawVal = configService.getConfigVal(SettingsEnum.STOREFRONT_WITHDRAWAL_FEE_RATE);
        BigDecimal storefrontWithdraw = new BigDecimal(shopfrontWithdrawVal);

        boolean storefrontShop = false;
        if (shopId > 0) {
            Shop shop = shopMapper.selectById(shopId);
            if (shop != null && shop.getShopType() != null && shop.getShopType() == ShopTypeEnum.STORE.getCode()) {
                storefrontShop = true;
                shopWithdraw = storefrontWithdraw;
            }
        }

        BigDecimal defaultShopSvc = storefrontShop ? storefrontGeneralSvc : storeGeneralSvc;
        BigDecimal defaultVendorSvc = new BigDecimal(configService.getConfigVal(SUPPLIER_GENERAL_SERVICE_FEE_RATE));

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
        Map<Integer, BigDecimal> categoryAmounts = new HashMap<>();
        Set<Integer> productIds = items.stream()
                .map(OrderItem::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, Product> products = Collections.emptyMap();
        if (!productIds.isEmpty()) {
            List<Product> plist = productMapper.selectBatchIds(productIds);
            products = plist.stream().collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));
        }
        for (OrderItem item : items) {
            if (item.getPrice() == null || item.getQuantity() == null) {
                continue;
            }
            BigDecimal line = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            Product p = products.get(item.getProductId());
            if (p == null || p.getCategoryId() == null || p.getCategoryId() <= 0) {
                continue;
            }
            categoryAmounts.merge(p.getCategoryId(), line, BigDecimal::add);
        }

        BigDecimal totalAmt = categoryAmounts.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedShopNum = BigDecimal.ZERO;
        BigDecimal weightedVendorNum = BigDecimal.ZERO;
        List<CategoryRateDetailJson> details = new ArrayList<>();

        if (totalAmt.compareTo(BigDecimal.ZERO) > 0) {
            Map<Integer, CategoryFeeRuleMatchVO> shopMatches = shopId > 0
                    ? categoryFeeRuleMatchService.batchMatch(RuleTargetTypeEnum.SHOP.getCode(), shopId, categoryAmounts.keySet())
                    : Collections.emptyMap();
            Map<Integer, CategoryFeeRuleMatchVO> vendorMatches = vendorId > 0
                    ? categoryFeeRuleMatchService.batchMatch(RuleTargetTypeEnum.VENDOR.getCode(), vendorId, categoryAmounts.keySet())
                    : Collections.emptyMap();

            for (Map.Entry<Integer, BigDecimal> e : categoryAmounts.entrySet()) {
                Integer catId = e.getKey();
                BigDecimal amt = e.getValue();

                BigDecimal shopRate = defaultShopSvc;
                String shopSrc = RateSourceEnum.GLOBAL_DEFAULT.getValue();
                Integer matchedShopRuleId = null;
                if (shopId > 0) {
                    CategoryFeeRuleMatchVO sm = shopMatches.get(catId);
                    if (sm != null && Boolean.TRUE.equals(sm.getMatched()) && sm.getServiceFeeRate() != null) {
                        shopRate = sm.getServiceFeeRate();
                        shopSrc = sm.getRateSource() != null ? sm.getRateSource() : shopSrc;
                        matchedShopRuleId = sm.getRuleId();
                    }
                }

                BigDecimal vendorRate = defaultVendorSvc;
                String vendorSrc = RateSourceEnum.GLOBAL_DEFAULT.getValue();
                Integer matchedVendorRuleId = null;
                if (vendorId > 0) {
                    CategoryFeeRuleMatchVO vm = vendorMatches.get(catId);
                    if (vm != null && Boolean.TRUE.equals(vm.getMatched()) && vm.getServiceFeeRate() != null) {
                        vendorRate = vm.getServiceFeeRate();
                        vendorSrc = vm.getRateSource() != null ? vm.getRateSource() : vendorSrc;
                        matchedVendorRuleId = vm.getRuleId();
                    }
                }

                weightedShopNum = weightedShopNum.add(amt.multiply(shopRate));
                weightedVendorNum = weightedVendorNum.add(amt.multiply(vendorRate));

                CategoryRateDetailJson d = new CategoryRateDetailJson();
                d.setCategoryId(catId);
                d.setAmount(amt);
                d.setShopServiceFeeRate(shopRate);
                d.setSupplierServiceFeeRate(vendorRate);
                d.setMatchedShopRuleId(matchedShopRuleId);
                d.setMatchedSupplierRuleId(matchedVendorRuleId);
                d.setShopRateSource(shopSrc);
                d.setSupplierRateSource(vendorSrc);
                details.add(d);
            }
        }

        BigDecimal finalShopSvc = totalAmt.compareTo(BigDecimal.ZERO) > 0
                ? weightedShopNum.divide(totalAmt, 4, RoundingMode.HALF_UP)
                : defaultShopSvc;
        BigDecimal finalVendorSvc = totalAmt.compareTo(BigDecimal.ZERO) > 0
                ? weightedVendorNum.divide(totalAmt, 4, RoundingMode.HALF_UP)
                : defaultVendorSvc;

        recordRate.setShopServiceFee(finalShopSvc);
        recordRate.setShopWithdrawalFee(shopWithdraw);
        recordRate.setStorefrontServiceFee(storefrontGeneralSvc);
        recordRate.setStorefrontWithdrawalFee(storefrontWithdraw);
        recordRate.setSupplierServiceFee(finalVendorSvc);
        recordRate.setSupplierWithdrawalFee(new BigDecimal(configService.getConfigVal(SettingsEnum.SUPPLIER_WITHDRAWAL_FEE_RATE)));
        recordRate.setCategoryRateDetails(details.isEmpty() ? null : JSONUtil.toJsonStr(details));
        recordRate.setGmtCreate(DateUtil.now());
        this.save(recordRate);
    }

    private void saveNonOrderRecordRate(int recordId, int recordType, int shopId, int vendorId) {
        RecordRate recordRate = new RecordRate();
        recordRate.setRecordId(recordId);
        recordRate.setRecordType(recordType);
        recordRate.setShopId(shopId);
        recordRate.setVendorId(vendorId);

        BigDecimal shopServiceFeeRate = new BigDecimal(configService.getConfigVal(SettingsEnum.STORE_GENERAL_SERVICE_FEE_RATE));
        BigDecimal feeRate = new BigDecimal(configService.getConfigVal(SettingsEnum.STORE_WITHDRAWAL_FEE_RATE));

        BigDecimal shopfrontServiceFeeRate = new BigDecimal(configService.getConfigVal(SettingsEnum.STOREFRONT_GENERAL_SERVICE_FEE_RATE));
        BigDecimal shopfrontFeeRate = new BigDecimal(configService.getConfigVal(SettingsEnum.STOREFRONT_WITHDRAWAL_FEE_RATE));

        if (shopId > 0) {
            Shop shop = shopMapper.selectById(shopId);
            if (shop != null && shop.getShopType() != null && shop.getShopType() == ShopTypeEnum.STORE.getCode()) {
                shopServiceFeeRate = shopfrontServiceFeeRate;
                feeRate = shopfrontFeeRate;
            }
        }
        recordRate.setShopServiceFee(shopServiceFeeRate);
        recordRate.setShopWithdrawalFee(feeRate);
        recordRate.setStorefrontServiceFee(shopfrontServiceFeeRate);
        recordRate.setStorefrontWithdrawalFee(shopfrontFeeRate);

        BigDecimal vendorServiceFeeRate = new BigDecimal(configService.getConfigVal(SUPPLIER_GENERAL_SERVICE_FEE_RATE));
        BigDecimal vendorFeeRate = new BigDecimal(configService.getConfigVal(SettingsEnum.SUPPLIER_WITHDRAWAL_FEE_RATE));
        recordRate.setSupplierServiceFee(vendorServiceFeeRate);
        recordRate.setSupplierWithdrawalFee(vendorFeeRate);

        recordRate.setCategoryRateDetails(null);
        recordRate.setGmtCreate(DateUtil.now());
        this.save(recordRate);
    }

    @Data
    private static class CategoryRateDetailJson {
        private Integer categoryId;
        private BigDecimal amount;
        private BigDecimal shopServiceFeeRate;
        private BigDecimal supplierServiceFeeRate;
        private Integer matchedShopRuleId;
        private Integer matchedSupplierRuleId;
        private String shopRateSource;
        private String supplierRateSource;
    }
}
