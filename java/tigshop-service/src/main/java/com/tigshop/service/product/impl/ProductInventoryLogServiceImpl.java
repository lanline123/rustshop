package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.product.ProductStockDetailDTO;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductInventoryLog;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.query.product.ProductInventoryLogPageQuery;
import com.tigshop.bean.vo.product.ProductInventoryLogVO;
import com.tigshop.bean.vo.product.ProductStockDetailVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SkuValueTranslateUtils;
import com.tigshop.mapper.product.ProductInventoryLogMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductInventoryLogService;
import com.tigshop.service.product.ProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 库存日志服务实现类
 *
 * @author kidd
 * @create 2025/7/3
 */
@Service
@RequiredArgsConstructor
public class ProductInventoryLogServiceImpl extends BaseServiceImpl<ProductInventoryLogMapper, ProductInventoryLog> implements ProductInventoryLogService {

    private final ProductMapper productMapper;
    private final ProductSkuService productSkuService;
    private final TranslatePackage translatePackage;


    @Override
    public Page<ProductInventoryLogVO> list(ProductInventoryLogPageQuery pageQuery) {
        pageQuery.init();

        Page<ProductInventoryLogVO> page = buildSortOrder(pageQuery);

        Page<ProductInventoryLogVO> productInventoryLogVOPage = this.baseMapper.productInventoryLog(page, pageQuery);

        if (CollUtil.isEmpty(productInventoryLogVOPage.getRecords())) {
            return page;
        }

        List<ProductInventoryLogVO> records = page.getRecords();
        for (ProductInventoryLogVO record : records) {
            Integer specId = record.getSpecId();
            Integer productId = record.getProductId();
            if (Objects.nonNull(specId) && specId > 0) {
                ProductSkuDTO detail = productSkuService.getDetail(specId, null);
                if(Objects.nonNull(detail)){
                    List<ProductSkuDTO.SkuData> skuData = detail.getSkuData();
                    if (skuData != null && skuData.size() > 0) {
                        // 将skuData列表中属性名和属性值,合并为 属性名:属性值,属性名:属性值 的格式
                        StringBuilder skuDataStr = new StringBuilder();
                        for (ProductSkuDTO.SkuData skuDatum : skuData) {
                            String name = translatePackage.translate(skuDatum.getName(), DataType.SKU.getCode());
                            String value = translatePackage.translate(skuDatum.getValue(), DataType.SKU.getCode());
                            skuDataStr.append(name).append(":").append(value).append(",");
                        }
                        // 去掉最后一个逗号
                        if (skuDataStr.length() > 0) {
                            skuDataStr.deleteCharAt(skuDataStr.length() - 1);
                        }
                        record.setSkuValue(skuDataStr.toString().trim());
                        record.setSn(detail.getSkuSn());
                    }
                }
            } else if(productId!=null) {
                //商品
                Product product = productMapper.selectById(productId);
                Optional.ofNullable(product).ifPresent(productVo -> record.setSn(productVo.getProductSn()));
            }
            if (StrUtil.isNotBlank(record.getProductName())) {
                record.setProductName(translatePackage.translate(record.getProductName(), DataType.PRODUCT.getCode()));
            }
        }
        return page;
    }

    @Override
    public Page<ProductStockDetailVO> getDetail(ProductStockDetailDTO productStockDetailDTO) {

        Page<ProductInventoryLog> page = new Page<>(productStockDetailDTO.getPage(), productStockDetailDTO.getSize());
        Page<ProductInventoryLog> logPage = this.page(page, Wrappers.lambdaQuery(ProductInventoryLog.class)
                .eq(ProductInventoryLog::getProductId, productStockDetailDTO.getProductId())
                .eq(ProductInventoryLog::getShopId, HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId())
                .eq(ProductInventoryLog::getSpecId, productStockDetailDTO.getSkuId() == null ? 0 : productStockDetailDTO.getSkuId())
                .orderByDesc(ProductInventoryLog::getAddTime));
        if (CollUtil.isEmpty(logPage.getRecords())) {
            return null;
        }
        ArrayList<ProductStockDetailVO> productStockDetailVOS = new ArrayList<>();
        for (ProductInventoryLog productInventoryLog : logPage.getRecords()) {
            ProductStockDetailVO productStockDetailVO = new ProductStockDetailVO();
            Product product = productMapper.selectById(productInventoryLog.getProductId());
            String productName = product != null ? product.getProductName() : "";
            if (StrUtil.isNotBlank(productName)) {
                productName = translatePackage.translate(productName, DataType.PRODUCT.getCode());
            }
            productStockDetailVO.setProductName(productName);
            productStockDetailVO.setQuantity(productInventoryLog.getChangeNumber());
            productStockDetailVO.setDesc(productInventoryLog.getDesc());
            String picThumb = product != null ? product.getPicThumb() : "";
            if (StrUtil.isNotBlank(picThumb)) {
                picThumb = translatePackage.translate(picThumb, DataType.IMAGE_TITLE.getCode());
            }
            productStockDetailVO.setProductPicThumb(picThumb);
            productStockDetailVO.setInOrOut(productInventoryLog.getType());
            if (productStockDetailDTO.getSkuId() != null) {
                ProductSku productSku = productSkuService.getById(productStockDetailDTO.getSkuId());
                String skuValue = productSku != null ? productSku.getSkuValue() : "";
                productStockDetailVO.setProductSkuName(SkuValueTranslateUtils.translateSkuValue(skuValue, translatePackage));
            }
            productStockDetailVOS.add(productStockDetailVO);
        }
        Page<ProductStockDetailVO> productStockDetailVOPage = new Page<>(productStockDetailDTO.getPage(), productStockDetailDTO.getSize(), logPage.getTotal());
        productStockDetailVOPage.setRecords(productStockDetailVOS);
        return productStockDetailVOPage;
    }
}
