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

package com.tigshop.service.order.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.AftersalesItemCreateDTO;
import com.tigshop.bean.dto.order.AftersalesItemListDTO;
import com.tigshop.bean.dto.order.AftersalesItemUpdateDTO;
import com.tigshop.bean.enums.order.AftersalesTypeEnum;
import com.tigshop.bean.enums.order.ShippingStatusEnum;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.order.OrderReservation;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.vo.order.AftersalesItemVO;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.mapper.order.AftersalesItemMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.promotion.GiftCardGroupsMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.AftersalesItemService;
import com.tigshop.service.order.OrderItemService;
import com.tigshop.service.order.OrderReservationService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.product.StockWarningService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static com.tigshop.bean.enums.product.ProductType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 售后商品服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AftersalesItemServiceImpl extends BaseServiceImpl<AftersalesItemMapper, AftersalesItem> implements AftersalesItemService {

    private final OrderItemService orderItemService;

    private final ProductService productService;

    private final ProductSkuService productSkuService;
    private final StockWarningService stockWarningService;
    private final GiftCardGroupsMapper giftCardGroupsMapper;


    private final AftersalesMapper aftersalesMapper;

    private final OrderMapper orderMapper;

    private final TigshopProperties tigshopProperties;

    private final TranslatePackage translatePackage;


    private final ConfigService configService;

    private final OrderReservationService orderReservationService;

    @Override
    public ListResVO<AftersalesItemVO, AftersalesItemListDTO> list(AftersalesItemListDTO listDTO) {
        // 分页
        Page<AftersalesItem> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<AftersalesItem> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        /*Integer isShow = listDTO.getIsShow();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq("is_show", isShow);
        }*/

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like("aftersales_item", keyword);
        }*/
        // 执行查询
        Page<AftersalesItem> aftersalesItemPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<AftersalesItem> aftersalesItemPageRecords = aftersalesItemPage.getRecords();
        // 转换为VO
        List<AftersalesItemVO> aftersalesItemVOList = aftersalesItemPageRecords.stream()
                .map(aftersalesItem -> {
                    AftersalesItemVO aftersalesItemVO = new AftersalesItemVO();
                    BeanUtils.copyProperties(aftersalesItem, aftersalesItemVO);
                    return aftersalesItemVO;
                }).toList();
        return new ListResVO<>(aftersalesItemVOList, listDTO, aftersalesItemPage.getTotal());
    }

    @Override
    public AftersalesItemVO detail(Integer id) {
        if (id != null) {
            AftersalesItem aftersalesItem = this.getById(id);
            AftersalesItemVO aftersalesItemVO = new AftersalesItemVO();
            BeanUtils.copyProperties(aftersalesItem, aftersalesItemVO);
            return aftersalesItemVO;
        }
        return new AftersalesItemVO();
    }

    @Override
    public boolean create(AftersalesItemCreateDTO createDTO) {
        if (createDTO != null) {
            AftersalesItem aftersalesItem = new AftersalesItem();
            BeanUtils.copyProperties(createDTO, aftersalesItem);
            return this.save(aftersalesItem);
        }
        return false;
    }

    @Override
    public boolean update(AftersalesItemUpdateDTO updateDTO) {
        if (updateDTO != null) {
            AftersalesItem AftersalesItem = new AftersalesItem();
            BeanUtils.copyProperties(updateDTO, AftersalesItem);
            return this.updateById(AftersalesItem);
        }
        return false;
    }

    @Override
    public List<AftersalesItemVO> getItemByIds(List<Integer> ids, boolean isClientRequest) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Integer requestVendorId = isClientRequest || RequestContextHolder.getRequestAttributes() == null
                ? null : getVendorId();
        LambdaQueryWrapper<AftersalesItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AftersalesItem::getAftersaleId, ids);
        if (isClientRequest) {
            queryWrapper.in(AftersalesItem::getAftersaleId, ids);
        }
        List<AftersalesItem> aftersalesItems = this.list(queryWrapper);
        List<AftersalesItemVO> aftersalesItemVOS = aftersalesItems.stream().map(aftersalesItem -> {
            AftersalesItemVO aftersalesItemVO = new AftersalesItemVO();
            OrderItem orderItem = orderItemService.getById(aftersalesItem.getOrderItemId());

            BeanUtils.copyProperties(aftersalesItem, aftersalesItemVO);

            if (orderItem != null) {
                aftersalesItemVO.setQuantity(orderItem.getQuantity());
                aftersalesItemVO.setProductName(translatePackage.translate(orderItem.getProductName(), DataType.PRODUCT.getCode()));
                aftersalesItemVO.setProductSn(orderItem.getProductSn());
                aftersalesItemVO.setProductId(orderItem.getProductId());
                aftersalesItemVO.setPicThumb(orderItem.getPicThumb());
                aftersalesItemVO.setOrderSn(orderItem.getOrderSn());
                aftersalesItemVO.setOrderId(orderItem.getOrderId());
                aftersalesItemVO.setExtraSkuData(orderItem.getExtraSkuData());
                // 解析规格数据（与 OrderItemServiceImpl 一致），并拼接 skuValue 便于列表展示
                if (!Objects.equals(orderItem.getSkuData(), "[]")) {
                    List<ProductSkuDTO.SkuData> skuData = JSON.parseArray(orderItem.getSkuData(), ProductSkuDTO.SkuData.class);
                    aftersalesItemVO.setSkuData(skuData != null ? skuData : new ArrayList<>());
                } else {
                    aftersalesItemVO.setSkuData(new ArrayList<>());
                }
                aftersalesItemVO.setSkuValue(aftersalesItemVO.getSkuData().stream()
                        .map(sku -> translatePackage.translate(sku.getName(), DataType.SKU.getCode()) + ":"
                                + translatePackage.translate(sku.getValue(), DataType.SKU.getCode()))
                        .collect(Collectors.joining("|")));
                Order order = orderMapper.selectById(orderItem.getOrderId());
                // 独立返回供货价快照；price 仍供实际退款计算使用，不得改为供货价。
                if (requestVendorId != null && requestVendorId > 0 && order != null
                        && Objects.equals(requestVendorId, order.getVendorId())
                        && orderItem.getVendorProductSupplyPrice() != null) {
                    aftersalesItemVO.setSupplyPrice(orderItem.getVendorProductSupplyPrice()
                            .setScale(2, RoundingMode.HALF_UP));
                }
                BigDecimal orderTaxRate = order != null && order.getTaxRate() != null ? order.getTaxRate() : BigDecimal.ZERO;
                BigDecimal unitTaxAmount = orderItem.getPrice() != null && orderItem.getPrice().compareTo(BigDecimal.ZERO) > 0 && orderTaxRate.compareTo(BigDecimal.ZERO) > 0
                        ? orderItem.getPrice().multiply(orderTaxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.CEILING)
                        : BigDecimal.ZERO;
                BigDecimal itemPrice = orderItem.getPrice() != null ? orderItem.getPrice() : BigDecimal.ZERO;
                aftersalesItemVO.setTaxRate(orderTaxRate);
                aftersalesItemVO.setTaxRateAmount(unitTaxAmount);
                // price 回显为“商品单价 + 单件税费”，用于前端售后退款计算
                aftersalesItemVO.setPrice(itemPrice.add(unitTaxAmount));
                BigDecimal aftersalesTaxAmount = aftersalesItemVO.getNumber() != null
                        ? unitTaxAmount.multiply(BigDecimal.valueOf(aftersalesItemVO.getNumber()))
                        : BigDecimal.ZERO;
                aftersalesItemVO.setTaxTotalAmount(aftersalesTaxAmount);
            } else {
                aftersalesItemVO.setTaxRate(BigDecimal.ZERO);
                aftersalesItemVO.setTaxRateAmount(BigDecimal.ZERO);
                aftersalesItemVO.setTaxTotalAmount(BigDecimal.ZERO);
            }
            return aftersalesItemVO;
        }).toList();
        return aftersalesItemVOS;
    }

    @Override
    public void returnStock(Integer aftersaleId) {
        List<AftersalesItem> aftersalesItems = this.lambdaQuery().eq(AftersalesItem::getAftersaleId, aftersaleId).list();
        Map<Integer, Integer> aftersalesItemMap = aftersalesItems.stream().collect(Collectors.toMap(AftersalesItem::getOrderItemId, AftersalesItem::getNumber));
        List<Integer> orderItemIds = aftersalesItems.stream().map(AftersalesItem::getOrderItemId).toList();

        Aftersales aftersales = aftersalesMapper.selectById(aftersaleId);
        boolean isOnlyRefund = aftersales != null && aftersales.getAftersaleType() == AftersalesTypeEnum.PAYRETURN.getCode();
        Order order = aftersales != null ? orderMapper.selectById(aftersales.getOrderId()) : null;
        boolean isShipped = order != null && order.getShippingStatus() != ShippingStatusEnum.PENDING.getCode();
        boolean isCardMainOrder = false;
        final boolean finalIsCardMainOrder = isCardMainOrder;
        List<OrderItem> orderItems = orderItemService.lambdaQuery().in(OrderItem::getItemId, orderItemIds).list();

        orderItems.forEach(orderItem -> {
            if (isOnlyRefund && isShipped) {
                return;
            }
            Integer num = aftersalesItemMap.get(orderItem.getItemId());
            Product product = productService.getById(orderItem.getProductId());
            if (product == null) {
                return;
            }

            boolean isReservationItem = orderItem.getProductType() == PRODUCT_TYPE_RESERVATION_HOME.getCode()
                    || orderItem.getProductType() == PRODUCT_TYPE_RESERVATION_STORE.getCode();

                if (orderItem.getSkuId() > 0) {
                    handleSkuStockInc(orderItem, num);
                } else {
                    handleProductStockInc(orderItem, num);
                }

            //礼品卡
            GiftCardGroups giftCardGroups = giftCardGroupsMapper.selectById(product.getGiftCardGroupsId());
            if (giftCardGroups != null) {
                giftCardGroups.setAvailabilityStock(giftCardGroups.getAvailabilityStock() + num);
                giftCardGroupsMapper.updateById(giftCardGroups);
            }

            //库存已返还，重新判定库存预警状态；预约商品走库存日历，不适用常规库存预警
            if (!isReservationItem && (orderItem.getVendorId() == null || orderItem.getVendorId() <= 0)) {
                stockWarningService.evaluate(orderItem.getShopId(), orderItem.getProductId(), orderItem.getSkuId());
            }
        });

    }

    @Override
    public List<AftersalesItem> validAftersalesItems(List<Integer> orderItemIds) {
        return this.baseMapper.validAftersalesItems(orderItemIds);
    }

    /**
     * 处理 SKU 商品库存增加
     */
    private void handleSkuStockInc(OrderItem orderItem, Integer num) {
            incStockSkuAndProduct(orderItem, num);
    }

    /**
     * 处理普通商品库存增加
     */
    private void handleProductStockInc(OrderItem orderItem, Integer num) {
            incStockProductOnly(orderItem, num);
    }


    /**
     * 增加 SKU + 商品库存
     */
    private void incStockSkuAndProduct(OrderItem orderItem, Integer num) {
        productSkuService.incStock(orderItem.getSkuId(), num, null);
        productService.incStock(orderItem.getProductId(), num, null,
                orderItem.getSkuId() != null && orderItem.getSkuId() > 0 ? false : true);
    }

    /**
     * 增加 商品库存
     */
    private void incStockProductOnly(OrderItem orderItem, Integer num) {
        productService.incStock(orderItem.getProductId(), num, null);
    }
}
