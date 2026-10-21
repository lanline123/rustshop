// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.product.impl;

import com.tigshop.common.exception.GlobalException;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.product.ProductStockDTO;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.product.*;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.bean.query.product.ProductSkuListQuery;
import com.tigshop.bean.vo.product.ProductStockPageVO;
import com.tigshop.bean.vo.product.ProductStockResultVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SkuValueTranslateUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.product.ECardGroupMapper;
import com.tigshop.mapper.product.ProductInventoryLogMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.product.ProductSkuMapper;
import com.tigshop.mapper.vendor.product.VendorProductSkuMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ECardService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.product.ProductType.*;
import static com.tigshop.common.constant.product.ProductConstants.PRODUCT_STOCK_MAX_LIMIT;

/**
 * 商品属性服务接口实现
 *
 * @author Jayce
 * @create 2024年11月21日 13:34
 */
@RequiredArgsConstructor
@Service
public class ProductSkuServiceImpl extends BaseServiceImpl<ProductSkuMapper, ProductSku> implements ProductSkuService {

    private final ProductMapper productMapper;
    private final ECardService eCardService;
    private final ProductInventoryLogMapper productInventoryLogMapper;
    private final VendorProductSkuMapper vendorProductSkuMapper;
    private final TranslatePackage translatePackage;
    private final ConfigService configService;
    private final TigshopProperties tigshopProperties;
    private final ECardGroupMapper eCardGroupMapper;
    private final ProductSkuMapper productSkuMapper;

    @Override
    public List<ProductSkuDTO> getProductSkusByPid(Integer productId) {
        return getProductSkusByPid(productId, false);
    }

    @Override
    public List<ProductSkuDTO> getProductSkusByPid(Integer productId, boolean includeDisabled) {
        LambdaQueryWrapper<ProductSku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductSku::getProductId, productId)
                .eq(!includeDisabled, ProductSku::getIsEnabled, Constants.YES);
        List<ProductSku> list = this.list(queryWrapper);
        return enrichSkuDTOList(list);
    }

    @Override
    public Page<ProductSkuDTO> getProductSkusPage(ProductSkuListQuery query) {
        int current = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int pageSize = (query.getSize() == null || query.getSize() < 1) ? 50 : Math.min(query.getSize(), 500);

        LambdaQueryWrapper<ProductSku> wrapper = new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, query.getId());

        String keyword = StrUtil.trimToNull(query.getKeyword());
        if (keyword != null) {
            wrapper.and(w -> w.like(ProductSku::getSkuSn, keyword)
                    .or().like(ProductSku::getSkuTsn, keyword)
                    .or().like(ProductSku::getSkuValue, keyword));
        }
        if (query.getStockMin() != null) {
            wrapper.ge(ProductSku::getSkuStock, query.getStockMin());
        }
        if (query.getStockMax() != null) {
            wrapper.le(ProductSku::getSkuStock, query.getStockMax());
        }
        applySkuSort(wrapper, query.getSortField(), query.getSortOrder());

        Page<ProductSku> skuPage = this.page(new Page<>(current, pageSize), wrapper);
        List<ProductSkuDTO> records = enrichSkuDTOList(skuPage.getRecords());
        return PageUtil.convertPage(skuPage, records);
    }

    @Override
    public void checkSkuEnabled(Integer productId, Integer skuId) {
        if (skuId == null || skuId <= 0) {
            return;
        }
        boolean exists = this.lambdaQuery()
                .eq(ProductSku::getSkuId, skuId)
                .eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getIsEnabled, Constants.YES)
                .exists();
        if (!exists) {
            throw new GlobalException("商品规格已禁用，请重新选择");
        }
    }

    /**
     * 按白名单应用排序，防止 SQL 注入；非法字段回退 skuId。
     */
    private void applySkuSort(LambdaQueryWrapper<ProductSku> wrapper, String sortField, String sortOrder) {
        boolean asc = !"desc".equalsIgnoreCase(sortOrder);
        String field = sortField == null ? "" : sortField.trim();
        switch (field) {
            case "skuStock" -> wrapper.orderBy(true, asc, ProductSku::getSkuStock);
            case "skuPrice" -> wrapper.orderBy(true, asc, ProductSku::getSkuPrice);
            case "skuSn" -> wrapper.orderBy(true, asc, ProductSku::getSkuSn);
            default -> wrapper.orderBy(true, asc, ProductSku::getSkuId);
        }
    }

    /**
     * 将 ProductSku 列表富化为 ProductSkuDTO：供应商价格/库存覆盖 + skuValue 翻译。
     * 供 {@link #getProductSkusByPid} 与 {@link #getProductSkusPageByPid} 复用。
     */
    private List<ProductSkuDTO> enrichSkuDTOList(List<ProductSku> list) {
        List<ProductSkuDTO> result = list.stream().map(this::convertToDTO).toList();

        List<ProductSkuDTO> skus = result.stream().filter(sku -> sku.getVendorProductSkuId() != null && sku.getVendorProductSkuId() > 0).toList();
        if (CollUtil.isNotEmpty(skus)) {
            List<Integer> vendorProductIds = skus.stream().map(ProductSkuDTO::getVendorProductSkuId).toList();
            List<VendorProductSku> vendorProductSkus = vendorProductSkuMapper.selectBatchIds(vendorProductIds);
            Map<Long, VendorProductSku> vendorProductSkuMap = vendorProductSkus.stream().collect(Collectors.toMap(VendorProductSku::getId, Function.identity()));

            result.stream()
                    .filter(sku -> sku.getVendorProductSkuId() != null && sku.getVendorProductSkuId() > 0)
                    .forEach(sku -> {
                        VendorProductSku vendorProductSku = vendorProductSkuMap.get(sku.getVendorProductSkuId().longValue());
                        if (vendorProductSku != null) {
                            sku.setSupplyPrice(vendorProductSku.getSupplyPrice());
                            sku.setSkuStock(vendorProductSku.getSkuStock());
                        }
                    });
        }

        // 规格串翻译：颜色:红|尺码:L → 逐段 dataType=SKU(8)
        result.forEach(productSkuDTO ->
                productSkuDTO.setSkuValue(SkuValueTranslateUtils.translateSkuValue(productSkuDTO.getSkuValue(), translatePackage)));
        return result;
    }

    @Override
    public List<ProductSkuDTO> getProductSkusByProductIds(List<Integer> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ProductSku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductSku::getProductId, productIds)
                .eq(ProductSku::getIsEnabled, Constants.YES);
        List<ProductSku> list = this.list(queryWrapper);
        List<ProductSkuDTO> result = list.stream().map(this::convertToDTO).toList();

        List<ProductSkuDTO> vendorProductSkus = result.stream().filter(sku -> sku.getVendorProductSkuId() != null && sku.getVendorProductSkuId() > 0).toList();
        if (CollUtil.isNotEmpty(vendorProductSkus)) {
            List<Integer> vendorProductIds = vendorProductSkus.stream().map(ProductSkuDTO::getVendorProductSkuId).toList();
            List<VendorProductSku> skus = vendorProductSkuMapper.selectBatchIds(vendorProductIds);
            Map<Long, VendorProductSku> vendorProductSkuMap = skus.stream().collect(Collectors.toMap(VendorProductSku::getId, Function.identity()));

            result.stream()
                    .filter(sku -> sku.getVendorProductSkuId() != null && sku.getVendorProductSkuId() > 0)
                    .forEach(sku -> {
                        VendorProductSku vendorProductSku = vendorProductSkuMap.get(sku.getVendorProductSkuId().longValue());
                        if (vendorProductSku != null) {
                            sku.setSkuStock(vendorProductSku.getSkuStock());
                        }
                    });
        }

        // 列表接口未走 enrichSkuDTOList，需单独翻译 skuValue
        result.forEach(productSkuDTO ->
                productSkuDTO.setSkuValue(SkuValueTranslateUtils.translateSkuValue(productSkuDTO.getSkuValue(), translatePackage)));
        return result;
    }

    @Override
    public boolean checkProductStock(int quantity, int productId, int skuId) {
        return getProductStock(productId, skuId) >= quantity;
    }

    @Override
    public int getProductStock(int productId, int skuId) {
        //todo 秒杀库存
        if (skuId == 0) {
            //直接返回商品的库存
            LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
            productQueryWrapper.eq(Product::getProductId, productId);
            Product product = productMapper.selectOne(productQueryWrapper);
            if (product == null) {
                return 0;
            }
            if (ProductType.isReservationProduct(product.getProductType())) {
                return PRODUCT_STOCK_MAX_LIMIT;
            }
            if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                product.setProductStock((int) eCardService.count(new LambdaQueryWrapper<ECard>()
                        .eq(ECard::getGroupId, product.getCardGroupId())));
            }
            return product.getProductStock();

        }
        // 构建通用查询条件
        LambdaQueryWrapper<ProductSku> queryWrapper = new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getIsEnabled, Constants.YES);
        queryWrapper.eq(ProductSku::getSkuId, skuId);

        ProductSku sku = this.getOne(queryWrapper);
        return sku != null ? sku.getSkuStock() : 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decStock(Integer skuId, Integer quantity, String stockDesc) {
        // 1. 查询商品属性
        ProductSku productSku = this.getById(skuId);
        // 无 SKU 行（如单规格 sku_id=0 兜底，或 SKU 已删除）时跳过 SKU 级扣减，
        // 与 incStock 判空口径一致；商品级扣减由 productService.decStock 负责，避免 NPE
        if (productSku == null) {
            return;
        }

        Product product = productMapper.selectById(productSku.getProductId());
        if (quantity <= 0) {
            return;
        }
        // 2. 扣减商品库存（原子操作，WHERE sku_stock >= quantity 防超卖）
        boolean success = this.lambdaUpdate()
                .eq(ProductSku::getSkuId, skuId)
                .ge(ProductSku::getSkuStock, quantity)
                .setDecrBy(ProductSku::getSkuStock, quantity)
                .update();
        if (!success) {
            throw new GlobalException(product.getProductName() + "库存不足");
        }
        int number = productSku.getSkuStock() - quantity;

        String desc = "下单扣减库存";
        if (StringUtils.isNotEmpty(stockDesc)) {
            desc = stockDesc;
        }

        // 3. 增加库存扣减日志
        ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                .productId(productSku.getProductId())
                .specId(skuId)
                .number(number)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(productSku.getSkuStock())
                .type(2)
                .changeNumber(quantity)
                .desc(desc)
                .shopId(product.getShopId())
                .build();
        productInventoryLogMapper.insert(productInventoryLog);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incStock(Integer skuId, Integer quantity, String stockDesc) {
        // 1. 查询商品属性
        ProductSku productSku = this.getById(skuId);
        // 如果商品规格已被删除
        if (productSku == null) {
            return;
        }
        // 实际变化量为0时，无需增加库存和记录日志
        if (quantity == null || quantity <= 0) {
            return;
        }

        String desc = "取消订单返还";
        if (StringUtils.isNotEmpty(stockDesc)) {
            desc = stockDesc;
        }
        // 2. 增加商品库存
        this.lambdaUpdate().eq(ProductSku::getSkuId, skuId).setIncrBy(ProductSku::getSkuStock, quantity).update();

        // 3. 增加库存扣减日志
        Product product = productMapper.selectById(productSku.getProductId());
        int number = productSku.getSkuStock() + quantity;
        ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                .productId(productSku.getProductId())
                .specId(skuId)
                .number(number)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(productSku.getSkuStock())
                .type(1)
                .changeNumber(quantity)
                .desc(desc)
                .shopId(product.getShopId())
                .build();
        productInventoryLogMapper.insert(productInventoryLog);
    }

    @Override
    public ProductSku getProductSkusBySn(String goodsSn) {
        LambdaQueryWrapper<ProductSku> queryWrapper = new LambdaQueryWrapper<ProductSku>()
                .and(wrapper -> wrapper.eq(ProductSku::getSkuSn, goodsSn).or().eq(ProductSku::getSkuTsn, goodsSn))
                .eq(ProductSku::getIsEnabled, Constants.YES)
                .select(ProductSku::getSkuId, ProductSku::getProductId);
        List<ProductSku> productSkus = this.list(queryWrapper);
        if (!productSkus.isEmpty()) {
            return productSkus.getFirst();
        }
        return null;
    }

    /**
     * 转换为DTO
     *
     * @param productSku 商品属性实体
     * @return 商品属性DTO
     */
    @Override
    public ProductSkuDTO convertToDTO(ProductSku productSku) {
        if (productSku == null) {
            return null;
        }
        ProductSkuDTO dto = new ProductSkuDTO();
        BeanUtils.copyProperties(productSku, dto);
        dto.setSkuPrice(productSku.getSkuPrice().toString());
        if (Objects.equals(dto.getCostPrice(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))) {
            dto.setCostPrice(null);
        }
        String skuDataStr = productSku.getSkuData();
        if (StrUtil.isNotEmpty(skuDataStr)) {
            JSONArray array = JsonUtil.fromJson(skuDataStr, JSONArray.class);
            List<ProductSkuDTO.SkuData> list = array.toList(ProductSkuDTO.SkuData.class);
            dto.setSkuData(list);
        }
        return dto;
    }

    @Override
    public ProductSkuDTO getDetail(int skuId, Long storeProductId) {
        ProductSku sku = this.getById(skuId);
        ProductSkuDTO productSkuDTO = convertToDTO(sku);
        return productSkuDTO;
    }

    @Override
    public Page<ProductStockPageVO> getProductStockRecordPage(ProductStockDTO productStockDTO) {
        return this.getProductStockPage(productStockDTO);
    }


    @Override
    public Page<ProductStockPageVO> getProductStockPage(ProductStockDTO productStockDTO) {

        Page<ProductStockResultVO> page = new Page<>(productStockDTO.getPage(), productStockDTO.getSize());
        ArrayList<ProductStockPageVO> productStockPageVOS = new ArrayList<>();

        Page<ProductStockResultVO> productStockPage = this.baseMapper.getProductStockPage(page, productStockDTO);
        for (ProductStockResultVO record : productStockPage.getRecords()) {
            ProductStockPageVO productStockPageVO = new ProductStockPageVO();
            productStockPageVO.setCurrentProductStock(record.getProductStock());
            productStockPageVO.setProductName(record.getProductName());
            productStockPageVO.setProductUnit(record.getProductUnit());
            productStockPageVO.setProductCost(record.getProductCost());
            productStockPageVO.setProductId(record.getProductId());
            productStockPageVO.setProductPicThumb(record.getProductPicThumb());
            productStockPageVO.setProductSn(record.getProductSn());
            productStockPageVO.setProductTsn(record.getProductTsn());
            if (record.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                if (record.getCardGroupId() != null) {
                    ECardGroup eCardGroup = eCardGroupMapper.selectOne(Wrappers.lambdaQuery(ECardGroup.class).eq(ECardGroup::getGroupId, record.getCardGroupId()).eq(ECardGroup::getIsUse, Constants.YES).last("limit 1"));
                    if (eCardGroup != null) {
                        long eCardCount = eCardService.count(Wrappers.lambdaQuery(ECard.class)
                                .eq(ECard::getGroupId, record.getCardGroupId())
                                .eq(ECard::getIsUse, 0));
                        // 使用Math.min避免long转int溢出
                        productStockPageVO.setCurrentProductStock((int) Math.min(eCardCount, Integer.MAX_VALUE));
                    } else {
                        productStockPageVO.setCurrentProductStock(0);
                    }
                }
            } else if (record.getProductType() == PRODUCT_TYPE_RESERVATION_HOME.getCode() || record.getProductType() == PRODUCT_TYPE_RESERVATION_STORE.getCode()) {
                productStockPageVO.setCurrentProductStock(null);
            }
            if (record.getSkuId() != null) {
                productStockPageVO.setProductSkuId(record.getSkuId());
                productStockPageVO.setCurrentProductStock(record.getProductSkuStock());
                productStockPageVO.setProductSkuName(record.getProductSkuName());
                productStockPageVO.setProductCost(record.getProductSkuCost());
                if (Objects.equals(record.getProductSkuCost(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))) {
                    productStockPageVO.setProductCost(null);
                }
            }
            productStockPageVOS.add(productStockPageVO);
        }
        productStockPageVOS.forEach(this::translateStockPageVO);
        Page<ProductStockPageVO> resultPage = new Page<>(productStockPage.getCurrent(), productStockPage.getSize(), productStockPage.getTotal());
        resultPage.setRecords(productStockPageVOS);
        return resultPage;
    }

    /**
     * 库存分页 VO 翻译：Mapper 直出 raw 字段，绕过 {@code @JsonTranslate}。
     */
    private void translateStockPageVO(ProductStockPageVO vo) {
        if (vo == null) {
            return;
        }
        if (StrUtil.isNotBlank(vo.getProductName())) {
            vo.setProductName(translatePackage.translate(vo.getProductName(), DataType.PRODUCT.getCode()));
        }
        if (StrUtil.isNotBlank(vo.getProductSkuName())) {
            vo.setProductSkuName(SkuValueTranslateUtils.translateSkuValue(vo.getProductSkuName(), translatePackage));
        }
        if (StrUtil.isNotBlank(vo.getProductPicThumb())) {
            vo.setProductPicThumb(translatePackage.translate(vo.getProductPicThumb(), DataType.IMAGE_TITLE.getCode()));
        }
    }


}
