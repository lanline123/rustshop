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

package com.tigshop.service.salesman.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.salesman.SalesmanProductAnalysisDTO;
import com.tigshop.bean.enums.salesman.SalesmanConfigTypeEnum;
import com.tigshop.bean.enums.salesman.SalesmanProductCommissionTypeEnum;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductGallery;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanConfig;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.model.salesman.SalesmanProduct;
import com.tigshop.bean.param.salesman.SalesmanProductEditParam;
import com.tigshop.bean.param.salesman.SalesmanProductSaveParam;
import com.tigshop.bean.query.salesman.SalesmanProductPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanConfigVO;
import com.tigshop.bean.vo.salesman.SalesmanProductDetailVO;
import com.tigshop.bean.vo.salesman.SalesmanProductListVO;
import com.tigshop.bean.vo.salesman.SalesmanProductVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.product.ProductGalleryMapper;
import com.tigshop.mapper.salesman.SalesmanConfigMapper;
import com.tigshop.mapper.salesman.SalesmanMapper;
import com.tigshop.mapper.salesman.SalesmanOrderMapper;
import com.tigshop.mapper.salesman.SalesmanProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.salesman.SalesmanConfigService;
import com.tigshop.service.salesman.SalesmanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 分销商品服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class SalesmanProductServiceImpl extends BaseServiceImpl<SalesmanProductMapper, SalesmanProduct> implements SalesmanProductService {

    private final ProductService productService;

    private final SalesmanConfigService configService;

    private final SalesmanOrderMapper salesmanOrderMapper;

    private final ProductGalleryMapper productGalleryMapper;

    private final SalesmanMapper salesmanMapper;

    private final SalesmanConfigMapper salesmanConfigMapper;

    private final com.tigshop.common.core.TranslatePackage translatePackage;

    @Override
    public Page<SalesmanProductListVO> list(SalesmanProductPageQuery pageQuery) {

        // 分页
        Page<SalesmanProductListVO> salesmanProductPage = productService.salesmanProducts(pageQuery);

        List<SalesmanProductListVO> records = salesmanProductPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return salesmanProductPage;
        }

        records.forEach(salesmanProduct ->
                salesmanProduct.setPaidContent(JsonUtil.checkJsonType(salesmanProduct.getPaidContentJson())));

        salesmanProductPage.getRecords().forEach(item -> {
            item.setSalesmanProduct(detailByProductId(item.getProductId()));
            List<ProductGallery> productGalleries = productGalleryMapper.selectList(
                    Wrappers.lambdaQuery(ProductGallery.class)
                            .eq(ProductGallery::getProductId, item.getProductId())
                            .orderByAsc(ProductGallery::getSortOrder)
            );
            item.setPics(productGalleries.stream().map(image -> {
                SalesmanProductListVO.ProductImage img = new SalesmanProductListVO.ProductImage();
                BeanUtils.copyProperties(image, img);
                return img;
            }).toList());
        });
        return salesmanProductPage;
    }

    @Override
    public SalesmanProductVO detailByProductId(Integer id) {
        SalesmanProduct salesmanProduct = this.lambdaQuery().eq(SalesmanProduct::getProductId, id).one();
        if (salesmanProduct == null) {
            return null;
        }

        SalesmanProductVO salesmanProductVO = new SalesmanProductVO();
        BeanUtils.copyProperties(salesmanProduct, salesmanProductVO);

        // 如果是默认佣金比例，采用公共分销模式数据
        if (salesmanProduct.getCommissionType() != null && salesmanProduct.getCommissionType() == 1) {
            SalesmanConfigVO salesmanConfig = configService.detail("salesmanConfig");
            if (salesmanConfig != null && salesmanConfig.getLevel() != null && !salesmanConfig.getLevel().isEmpty()) {
                List<SalesmanProductVO.CommissionData> commissionDataList = new ArrayList<>();
                SalesmanProductVO.CommissionData commissionData = new SalesmanProductVO.CommissionData();
                List<SalesmanProductVO.CommissionData.LevelData> levelDataList = new ArrayList<>();
                for (SalesmanConfigVO.Level configLevel : salesmanConfig.getLevel()) {
                    SalesmanProductVO.CommissionData.LevelData levelData = new SalesmanProductVO.CommissionData.LevelData();
                    levelData.setLevel(configLevel.getId());
                    levelData.setRate(configLevel.getRate());
                    levelData.setDownSalesmanRate(configLevel.getDownSalesmanRate());
                    levelDataList.add(levelData);
                }
                commissionData.setLevelArr(levelDataList);
                commissionDataList.add(commissionData);
                salesmanProductVO.setCommissionData(commissionDataList);
            }
        } else if (StringUtils.isNotEmpty(salesmanProduct.getCommissionData())) {
            salesmanProductVO.setCommissionData(JSONUtil.toList(salesmanProduct.getCommissionData(), SalesmanProductVO.CommissionData.class));
        }

        salesmanProductVO.setUpdateTime(TigUtils.handelTime(salesmanProduct.getUpdateTime()));
        salesmanProductVO.setProductCommission(getProductCommissionAttr(salesmanProductVO));
        return salesmanProductVO;
    }

    @Override
    public void create(SalesmanProductSaveParam param) {
        SalesmanProduct salesmanProduct = param.createSalesmanProduct();
        this.save(salesmanProduct);
    }

    @Override
    public void update(SalesmanProductEditParam param) {
        SalesmanProduct salesmanProduct = this.getById(param.getSalesmanProductId());
        Assert.notNull(salesmanProduct, () -> new GlobalException("未知的分销商品"));

        SalesmanProduct updateSalesmanProduct = param.updateSalesmanProduct(salesmanProduct);

        this.updateById(updateSalesmanProduct);
    }

    @Override
    public Page<Map<String, Object>> analysis(SalesmanProductAnalysisDTO analysisDTO) {
        Page<SalesmanProduct> salesmanProductPage = this.lambdaQuery()
                .eq(SalesmanProduct::getShopId, getShopId())
                .eq(SalesmanProduct::getIsJoin, 1)
                .page(new Page<>(analysisDTO.getPage(), analysisDTO.getSize()));

        List<SalesmanProduct> salesmanProductList = salesmanProductPage.getRecords();

        analysisDTO.handleSearchTime();

        // 把日期格式的start转为时间戳
        Long startTime = analysisDTO.getSearchStartTime();
        Long endTime = analysisDTO.getSearchEndTime();
        List<Map<String, Object>> result = new ArrayList<>();

        for (SalesmanProduct item : salesmanProductList) {
            Product product = productService.getOne(Wrappers.lambdaQuery(Product.class).eq(Product::getProductId, item.getProductId()));
            if (product == null) {
                continue;
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("productId", item.getProductId());
            // Map 返回绕过 @JsonTranslate，手动翻译商品名(2)/缩略图(7)
            resultMap.put("productName", product.getProductName() != null
                    ? translatePackage.translate(product.getProductName(), com.tigshop.common.enums.DataType.PRODUCT.getCode()) : "");
            resultMap.put("productSn", product.getProductSn() != null ? product.getProductSn() : "");
            resultMap.put("picThumb", product.getPicThumb() != null
                    ? translatePackage.translate(product.getPicThumb(), com.tigshop.common.enums.DataType.IMAGE_TITLE.getCode()) : "");
            resultMap.put("totalProductMoney", 0);
            resultMap.put("commissionExpenses", 0);
            List<SalesmanOrder> salesmanOrders = salesmanOrderMapper.selectList(
                    Wrappers.lambdaQuery(SalesmanOrder.class)
                            .eq(SalesmanOrder::getProductId, item.getProductId())
                            .ge(SalesmanOrder::getAddTime, startTime)
                            .le(SalesmanOrder::getAddTime, endTime)
            );
            if (salesmanOrders != null && !salesmanOrders.isEmpty()) {
                BigDecimal totalProductMoney = BigDecimal.ZERO;
                BigDecimal commissionExpenses = BigDecimal.ZERO;
                for (SalesmanOrder order : salesmanOrders) {
                    // 成交金额
                    BigDecimal price = order.getOrderAmount();
                    totalProductMoney = totalProductMoney.add(price);
                    // 支出佣金
                    if (order.getStatus() == 1) {
                        commissionExpenses = commissionExpenses.add(order.getAmount());
                    }
                }
                resultMap.put("totalProductMoney", totalProductMoney);
                resultMap.put("commissionExpenses", commissionExpenses);
            }
            result.add(resultMap);
        }

        // 排序
        if (analysisDTO.getSortField() != null && analysisDTO.getSortOrder() != null) {
            if (Arrays.asList("totalProductMoney", "commissionExpenses").contains(analysisDTO.getSortField())) {
                result.sort((map1, map2) -> {
                    double value1 = ((Number) map1.get(analysisDTO.getSortField())).doubleValue();
                    double value2 = ((Number) map2.get(analysisDTO.getSortField())).doubleValue();
                    return "desc".equalsIgnoreCase(analysisDTO.getSortOrder()) ? Double.compare(value2, value1) : Double.compare(value1, value2);
                });
            }
        }

        Page<Map<String, Object>> resultPage = new Page<>();
        BeanUtil.copyProperties(salesmanProductPage, resultPage);
        resultPage.setRecords(result);

        return resultPage;
    }

    @Override
    public SalesmanProductDetailVO detailClient(Integer productId) {
        // 1. 查询商品信息
        Product product = productService.getById(productId);
        if (product == null) {
            return null;
        }

        // 2. 查询商品图片信息
        List<ProductGallery> productGalleries = productGalleryMapper.selectList(
                Wrappers.lambdaQuery(ProductGallery.class)
                        .eq(ProductGallery::getProductId, productId)
                        .orderByAsc(ProductGallery::getSortOrder)
        );

        // 3. 查询分销商品信息
        SalesmanProduct salesmanProduct = this.lambdaQuery().eq(SalesmanProduct::getProductId, productId).one();

        // 4. 查询最终推广佣金
        // 4.1. 查询分销员信息
        Integer userId = SecurityUtils.getCurrentUserId();
        Salesman salesman = salesmanMapper.selectOne(
                Wrappers.lambdaQuery(Salesman.class).eq(Salesman::getUserId, userId)
        );
        // 4.2. 查询分销员推广比例
        SalesmanConfig salesmanConfig = salesmanConfigMapper.selectOne(
                Wrappers.lambdaQuery(SalesmanConfig.class)
                        .eq(SalesmanConfig::getCode, SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode())
        );
        SalesmanConfigVO salesmanConfigVO = JSON.parseObject(salesmanConfig.getData(), SalesmanConfigVO.class);
        List<SalesmanConfigVO.Level> level = salesmanConfigVO.getLevel();
        String rate = level.stream()
                .filter(item -> item.getId().equals(salesman.getLevel()))
                .findFirst()
                .orElseThrow(() -> new GlobalException("未找到匹配的等级"))
                .getRate();

        // 4.3. 计算最终推广佣金
        String finalCommissionPrice = "";
        if (SalesmanProductCommissionTypeEnum.DEFAULT_RATE.getCode() == salesmanProduct.getCommissionType()) {
            BigDecimal ratePercent = new BigDecimal(rate).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            finalCommissionPrice = product.getProductPrice().multiply(ratePercent).setScale(2, RoundingMode.HALF_UP).toString();
        }
        if (SalesmanProductCommissionTypeEnum.CUSTOM_RATE.getCode() == salesmanProduct.getCommissionType()) {
            List<SalesmanProductVO.CommissionData> commissionData = JSONArray.parseArray(salesmanProduct.getCommissionData(), SalesmanProductVO.CommissionData.class);
            for (SalesmanProductVO.CommissionData dataItem : commissionData) {
                String customRate = dataItem.getLevelArr().stream()
                        .filter(item -> item.getLevel().equals(salesman.getLevel()))
                        .findFirst()
                        .orElseThrow(() -> new GlobalException("未找到匹配的等级"))
                        .getRate();

                BigDecimal ratePercent = new BigDecimal(customRate).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                finalCommissionPrice = product.getProductPrice().multiply(ratePercent).setScale(2, RoundingMode.HALF_UP).toString();
            }
        }
        if (SalesmanProductCommissionTypeEnum.CUSTOM_PRICE.getCode() == salesmanProduct.getCommissionType()) {
            List<SalesmanProductVO.CommissionData> commissionData = JSONArray.parseArray(salesmanProduct.getCommissionData(), SalesmanProductVO.CommissionData.class);
            for (SalesmanProductVO.CommissionData dataItem : commissionData) {

                finalCommissionPrice = dataItem.getLevelArr().stream()
                        .filter(item -> item.getLevel().equals(salesman.getLevel()))
                        .findFirst()
                        .orElseThrow(() -> new GlobalException("未找到匹配的等级"))
                        .getRate();
            }
        }

        return new SalesmanProductDetailVO(product, productGalleries, salesmanProduct, finalCommissionPrice);
    }

    @Override
    public SalesmanProductVO detail(Integer id) {
        if (id != null) {
            SalesmanProduct salesmanProduct = this.getById(id);
            if (salesmanProduct == null) {
                return null;
            }
            SalesmanProductVO salesmanProductVO = new SalesmanProductVO();
            BeanUtils.copyProperties(salesmanProduct, salesmanProductVO);
            if (StringUtils.isNotEmpty(salesmanProduct.getCommissionData())) {
                salesmanProductVO.setCommissionData(JSONUtil.toList(salesmanProduct.getCommissionData(), SalesmanProductVO.CommissionData.class));
            }
            Date updateTime = DateUtil.date(salesmanProduct.getUpdateTime() * 1000);
            salesmanProductVO.setUpdateTime(DateUtil.format(updateTime, "yyyy-MM-dd HH:mm:ss"));
            salesmanProductVO.setProductCommission(getProductCommissionAttr(salesmanProductVO));
            return salesmanProductVO;
        }
        return null;
    }

    // 组织佣金数据规则的文案字符串
    public SalesmanProductVO.ProductCommission getProductCommissionAttr(SalesmanProductVO data) {
        SalesmanProductVO.ProductCommission commissionInfo = new SalesmanProductVO.ProductCommission();
        StringBuilder productCommission = new StringBuilder();
        StringBuilder subCommission = new StringBuilder();

        SalesmanConfigVO salesmanConfig = configService.detail("salesmanConfig");

        if (data.getIsJoin() != null && data.getIsJoin() == 1 && data.getCommissionType() == 1 && data.getCommissionData() == null) {
            if (salesmanConfig != null && !salesmanConfig.getLevel().isEmpty()) {
                List<SalesmanConfigVO.Level> levels = salesmanConfig.getLevel();
                for (SalesmanConfigVO.Level level : levels) {
                    productCommission.append(level.getName()).append("佣金:").append(level.getRate()).append("%;");
                }
            }
        } else {
            if (data.getIsJoin() != null && data.getIsJoin() == 1 && data.getCommissionData() != null) {
                List<SalesmanProductVO.CommissionData> commissionData = data.getCommissionData();
                if (salesmanConfig != null && !commissionData.isEmpty() && salesmanConfig.getLevel() != null) {
                    Map<Integer, String> levelNames = new HashMap<>();
                    for (SalesmanConfigVO.Level level : salesmanConfig.getLevel()) {
                        levelNames.put(level.getId(), level.getName());
                    }
                    for (SalesmanProductVO.CommissionData item : commissionData) {
                        if (item.getLevelArr() != null) {
                            List<SalesmanProductVO.CommissionData.LevelData> levelArr = item.getLevelArr();
                            for (SalesmanProductVO.CommissionData.LevelData val : levelArr) {
                                String levelName = levelNames.getOrDefault(val.getLevel(),
                                        "未配置等级（ID：" + val.getLevel() + "）");
                                int commissionType = data.getCommissionType();
                                if (commissionType == 1 || commissionType == 2) {
                                    productCommission.append(levelName).append("佣金:").append(val.getRate()).append("%;");
                                    if (salesmanConfig.getSaleType() != null && salesmanConfig.getSaleType() != 1) {
                                        subCommission.append(levelName).append("佣金: ").append(val.getDownSalesmanRate()).append("%;");
                                    }
                                } else {
                                    productCommission.append(levelName).append("佣金:").append(val.getRate()).append(";");
                                    if (salesmanConfig.getSaleType() != 1) {
                                        subCommission.append(levelName).append("佣金: ").append(val.getDownSalesmanRate()).append(";");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        commissionInfo.setProductCommission(productCommission.toString());
        commissionInfo.setSubCommission(subCommission.toString());
        return commissionInfo;
    }
}
