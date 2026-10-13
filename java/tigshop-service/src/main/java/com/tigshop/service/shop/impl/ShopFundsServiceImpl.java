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

package com.tigshop.service.shop.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.ShopFundsCreateDTO;
import com.tigshop.bean.dto.shop.ShopFundsListDTO;
import com.tigshop.bean.dto.shop.ShopFundsUpdateDTO;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.model.shop.ShopFunds;
import com.tigshop.bean.vo.shop.ShopFundsVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.shop.ShopFundsMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.shop.ShopFundsService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店铺表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ShopFundsServiceImpl extends BaseServiceImpl<ShopFundsMapper, ShopFunds> implements ShopFundsService {
    @Resource
    ObjectProvider<OrderService> orderService;
    @Resource
    TigshopProperties tigshopProperties;
    /**
     * 未结算订单金额排序字段。该值为 Java 计算的派生值（非 shop 表字段），
     * 无法通过 SQL 列排序，需走内存排序分支。
     */
    private static final String SORT_FIELD_UN_SETTLEMENT = "unSettlementOrder";

    @Override
    public Page<ShopFundsVO> list(ShopFundsListDTO listDTO) {
        // 构造查询构造器（公共筛选条件）
        LambdaQueryWrapper<ShopFunds> queryWrapper = buildListQueryWrapper(listDTO);

        // 未结算订单金额为派生值，无法用 SQL 列排序，单独走内存排序分支
        if (ObjectUtil.equals(listDTO.getSortField(), SORT_FIELD_UN_SETTLEMENT)) {
            return listOrderByUnSettlement(listDTO, queryWrapper);
        }

        // 分页
        Page<ShopFunds> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        // 执行查询
        Page<ShopFunds> shopFundsPage = this.page(page, queryWrapper);
        // 转换为VO
        List<ShopFundsVO> shopFundsVOList = shopFundsPage.getRecords().stream()
                .map(shopFunds -> convertToVO(shopFunds, null))
                .toList();
        return PageUtil.convertPage(shopFundsPage, shopFundsVOList);
    }

    /**
     * 构造列表公共筛选条件（与排序方式无关）。
     */
    private LambdaQueryWrapper<ShopFunds> buildListQueryWrapper(ShopFundsListDTO listDTO) {
        LambdaQueryWrapper<ShopFunds> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(ShopFunds::getShopType, ShopTypeEnum.PICKUP.getCode());
        // 过滤已归档的门店（is_delete 为 null 或 0 表示未删除）
        queryWrapper.apply("(is_delete IS NULL OR is_delete = 0)");
        // 状态筛选
        Integer status = listDTO.getStatus();
        if (status != null && status > 0) {
            queryWrapper.eq(ShopFunds::getStatus, status);
        }
        // 店铺类型
        if (listDTO.getShopType() != null) {
            queryWrapper.eq(ShopFunds::getShopType, listDTO.getShopType());
        }
        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(ShopFunds::getShopTitle, keyword);
        }
        return queryWrapper;
    }

    /**
     * 按未结算订单金额排序。该值由 {@code OrderService#getOrderUnSettlementAmount} 逐店铺计算，
     * 与列表展示使用同一方法，保证排序值与展示值完全一致。
     */
    private Page<ShopFundsVO> listOrderByUnSettlement(ShopFundsListDTO listDTO, LambdaQueryWrapper<ShopFunds> queryWrapper) {
        // 查出全部匹配店铺（不分页），以便按派生值整体排序
        List<ShopFunds> allShopFunds = this.list(queryWrapper);
        // 逐个计算未结算订单金额（与列表展示同一方法）
        Map<Integer, BigDecimal> unSettlementMap = new HashMap<>(allShopFunds.size());
        for (ShopFunds shopFunds : allShopFunds) {
            unSettlementMap.put(shopFunds.getShopId(),
                    orderService.getIfAvailable().getOrderUnSettlementAmount(shopFunds.getShopId()));
        }
        // 排序：asc 升序，其余/空按 desc（与 buildSortOrder 默认一致）
        Comparator<ShopFunds> comparator = Comparator.comparing(
                shopFunds -> unSettlementMap.getOrDefault(shopFunds.getShopId(), BigDecimal.ZERO));
        if (!"asc".equalsIgnoreCase(listDTO.getSortOrder())) {
            comparator = comparator.reversed();
        }
        allShopFunds.sort(comparator);
        // 内存分页
        long total = allShopFunds.size();
        int fromIndex = (listDTO.getPage() - 1) * listDTO.getSize();
        List<ShopFundsVO> records;
        if (fromIndex >= allShopFunds.size() || fromIndex < 0) {
            records = List.of();
        } else {
            int toIndex = Math.min(fromIndex + listDTO.getSize(), allShopFunds.size());
            records = allShopFunds.subList(fromIndex, toIndex).stream()
                    .map(shopFunds -> convertToVO(shopFunds, unSettlementMap.get(shopFunds.getShopId())))
                    .toList();
        }
        Page<ShopFundsVO> resultPage = new Page<>(listDTO.getPage(), listDTO.getSize(), total);
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * ShopFunds 转 VO。
     *
     * @param unSettlementOrder 预计算的未结算订单金额；为 null 时实时计算（与原逻辑一致）
     */
    private ShopFundsVO convertToVO(ShopFunds shopFunds, BigDecimal unSettlementOrder) {
        ShopFundsVO shopFundsVO = new ShopFundsVO();
        BeanUtils.copyProperties(shopFunds, shopFundsVO);
        long timestampInSeconds = shopFunds.getAddTime();
        Date date = new Date(timestampInSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        shopFundsVO.setAddTime(sdf.format(date));
        shopFundsVO.setStatusText(ShopType.getTypeName(shopFunds.getStatus()));
        BigDecimal sumOrderUnSettlementAmount = unSettlementOrder != null
                ? unSettlementOrder
                : orderService.getIfAvailable().getOrderUnSettlementAmount(shopFunds.getShopId());
        shopFundsVO.setUnSettlementOrder(sumOrderUnSettlementAmount);
        return shopFundsVO;
    }

    @Override
    public ShopFundsVO detail(Integer id) {
        if (id != null) {
            ShopFunds shopFunds = this.getById(id);
            ShopFundsVO shopFundsVO = new ShopFundsVO();
            BeanUtils.copyProperties(shopFunds, shopFundsVO);
            return shopFundsVO;
        }
        return null;
    }

    @Override
    public ShopFunds getShopByShopId(Integer shopId) {
        LambdaQueryWrapper<ShopFunds> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopFunds::getShopId, shopId);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean create(ShopFundsCreateDTO createDTO) {
        if (createDTO != null) {
            ShopFunds shopFunds = new ShopFunds();
            BeanUtils.copyProperties(createDTO, shopFunds);
            return this.save(shopFunds);
        }
        return false;
    }

    @Override
    public boolean update(ShopFundsUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ShopFunds shopFunds = new ShopFunds();
            BeanUtils.copyProperties(updateDTO, shopFunds);
            return this.updateById(shopFunds);
        }
        return false;
    }

    @Override
    public Long getCountByMerchantId(Integer merchantId) {
        QueryWrapper<ShopFunds> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", merchantId);
        queryWrapper.ne("shop_type", ShopTypeEnum.PICKUP.getCode());
        queryWrapper.eq(tigshopProperties.getIsO2o() == 1, "shop_type", ShopTypeEnum.STORE.getCode());
        queryWrapper.eq(tigshopProperties.getIsO2o() == 0, "shop_type", ShopTypeEnum.SHOP.getCode());
        queryWrapper.and(w -> w.isNull("is_delete").or().eq("is_delete", 0));
        return this.count(queryWrapper);
    }
}
