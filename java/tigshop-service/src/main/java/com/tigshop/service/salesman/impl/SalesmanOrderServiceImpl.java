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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderConfigDTO;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.salesman.SalesmanOrderStatus;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.model.finance.RefundLog;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanGroup;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.model.shop.OrderConfig;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.query.salesman.CustomerTransactionPageQuery;
import com.tigshop.bean.query.salesman.SalesmanOrderPageQuery;
import com.tigshop.bean.vo.finance.OrderInvoiceVO;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.salesman.*;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.bean.vo.user.UserBaseVO;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.salesman.SalesmanOrderMapper;
import com.tigshop.mapper.shop.OrderConfigMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RefundLogService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderItemService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.salesman.*;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * 分销内容管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SalesmanOrderServiceImpl extends BaseServiceImpl<SalesmanOrderMapper, SalesmanOrder> implements SalesmanOrderService {

    @Autowired
    private ObjectProvider<OrderService> orderService;

    @Autowired
    private ObjectProvider<UserService> userService;

    @Autowired
    private  ObjectProvider<SalesmanService> salesmanService;

    private final AftersalesService aftersalesService;

    private final RefundLogService refundLogService;

    private final SalesmanTmpOrderService salesmanTmpOrderService;



    private final SalesmanGroupService salesmanGroupService;

    private final OrderItemService orderItemService;

    private final SalesmanConfigService salesmanConfigService;



    private final SalesmanProductService salesmanProductService;

    private final UserMapper userMapper;

    private final ShopMapper shopMapper;

    private final OrderConfigMapper orderConfigMapper;

    private final ConfigService configService;

    private final AftersalesMapper aftersalesMapper;

    private final HttpServletResponse response;

    private final ApplicationContext applicationContext;

    @Override
    public Page<SalesmanOrderVO> list(SalesmanOrderPageQuery pageQuery, HttpServletResponse response) {
        Integer shopId = HeaderUtils.getShopId();
        List<Integer> orderIds = null;
        
        if (StringUtils.isNotEmpty(pageQuery.getOrderSn())) {
            List<Order> orderList = orderService.getIfAvailable().lambdaQuery().like(Order::getOrderSn, pageQuery.getOrderSn()).list();
            orderIds = orderList.stream().map(Order::getOrderId).distinct().toList();
        }

        // 分页
        Page<SalesmanOrder> page = buildSortOrder(pageQuery);

        // 构造查询构造器
        LambdaQueryWrapper<SalesmanOrder> querySaleWrapper = new LambdaQueryWrapper<>();
        querySaleWrapper.eq(pageQuery.getSalesmanId() != null, SalesmanOrder::getSalesmanId, pageQuery.getSalesmanId());
        querySaleWrapper.in(orderIds != null, SalesmanOrder::getOrderId, orderIds);
        querySaleWrapper.eq(pageQuery.getStatus() != null && pageQuery.getStatus() != -1, SalesmanOrder::getStatus, pageQuery.getStatus());

        // 查询店铺关联订单
        List<Order> shopOrders = orderService.getIfAvailable().lambdaQuery().eq(shopId != null && shopId > 0, Order::getShopId, shopId).list();
        if (CollUtil.isEmpty(shopOrders)) {
            return new Page<>();
        }
        List<Integer> shopOrderIds = shopOrders.stream().map(Order::getOrderId).distinct().toList();
        querySaleWrapper.in(SalesmanOrder::getOrderId, shopOrderIds);


        if (pageQuery.getTimeType() != null && pageQuery.getTimeType() != -1) {
            //根据下单筛选时间类型
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            if (pageQuery.getTimeType() == 1) {
                if (StringUtils.isNotEmpty(pageQuery.getOrderTimeStart())) {
                    orderWrapper.ge(Order::getAddTime, StringUtils.dateToTimestampExample(pageQuery.getOrderTimeStart() + " 00:00:00"));
                }
                if (StringUtils.isNotEmpty(pageQuery.getOrderTimeEnd())) {
                    orderWrapper.le(Order::getAddTime, StringUtils.dateToTimestampExample(pageQuery.getOrderTimeEnd() + " 23:59:59"));
                }
                List<Order> orderList = orderService.getIfAvailable().list(orderWrapper);
                if (CollUtil.isEmpty(orderList)) {
                    return new Page<>();
                } else {
                    List<Integer> orderDateIds = orderList.stream().map(Order::getOrderId).distinct().toList();
                    querySaleWrapper.in(SalesmanOrder::getOrderId, orderDateIds);
                }
            } else {
                LambdaQueryWrapper<RefundLog> refundLogWrapper = new LambdaQueryWrapper<>();
                if (StringUtils.isNotEmpty(pageQuery.getOrderTimeStart())) {
                    refundLogWrapper.ge(RefundLog::getAddTime, StringUtils.dateToTimestampExample(pageQuery.getOrderTimeStart() + " 00:00:00"));
                }
                if (StringUtils.isNotEmpty(pageQuery.getOrderTimeEnd())) {
                    refundLogWrapper.le(RefundLog::getAddTime, StringUtils.dateToTimestampExample(pageQuery.getOrderTimeEnd() + " 23:59:59"));
                }
                List<RefundLog> refundLogList = refundLogService.list(refundLogWrapper);
                if (CollUtil.isEmpty(refundLogList)) {
                    return new Page<>();
                } else {
                    List<Integer> refundLogIds = refundLogList.stream().map(RefundLog::getOrderId).distinct().toList();
                    querySaleWrapper.in(SalesmanOrder::getOrderId, refundLogIds);
                }
            }
        } else {
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            if (StringUtils.isNotEmpty(pageQuery.getOrderTimeStart())) {
                orderWrapper.ge(Order::getAddTime, StringUtils.dateToTimestampExample(pageQuery.getOrderTimeStart() + " 00:00:00"));
            }
            if (StringUtils.isNotEmpty(pageQuery.getOrderTimeEnd())) {
                orderWrapper.le(Order::getAddTime, StringUtils.dateToTimestampExample(pageQuery.getOrderTimeEnd() + " 23:59:59"));
            }
            List<Order> orderList = orderService.getIfAvailable().list(orderWrapper);
            if (CollUtil.isEmpty(orderList)) {
                return new Page<>();
            } else {
                List<Integer> orderDateIds = orderList.stream().map(Order::getOrderId).distinct().toList();
                querySaleWrapper.in(SalesmanOrder::getOrderId, orderDateIds);
            }
        }

        // 执行查询
        Page<SalesmanOrder> queryPage = this.page(page, querySaleWrapper);

        // 获取查询结果
        List<SalesmanOrder> queryPageRecords = queryPage.getRecords();
        if (queryPageRecords.isEmpty()) {
            return new Page<>();
        }

        List<SalesmanOrderVO> salesmanOrderVOList = queryPageRecords.stream().map(salesmanOrder -> {
            SalesmanOrderVO salesmanOrderVO = new SalesmanOrderVO();
            BeanUtils.copyProperties(salesmanOrder, salesmanOrderVO);
            salesmanOrderVO.setStatusText(SalesmanOrderStatus.getByCode(salesmanOrder.getStatus()));
            // 修改add_time
            salesmanOrderVO.setAddTime(TigUtils.handelTime(salesmanOrder.getAddTime()));

            SalesmanOrderVO.SalesmanSettingVO salesmanSettingVO = JSONUtil.toBean(salesmanOrder.getSalesmanSettlementData(), SalesmanOrderVO.SalesmanSettingVO.class);
            salesmanOrderVO.setSalesmanSettlementData(salesmanSettingVO);

            if (salesmanOrder.getSettlementTime() != null) {
                salesmanOrderVO.setSettlementTime(TigUtils.handelTime(salesmanOrder.getSettlementTime()));
            }
            //查询分销员信息
            LambdaQueryWrapper<Salesman> salesmanLambdaQueryWrapper = new LambdaQueryWrapper<>();
            salesmanLambdaQueryWrapper.eq(Salesman::getSalesmanId, salesmanOrder.getSalesmanId());
            Salesman salesman = salesmanTmpOrderService.getOne(salesmanLambdaQueryWrapper);
            SalesmanStatisticalVO salesmanStatisticalVO = new SalesmanStatisticalVO();
            BeanUtils.copyProperties(salesman, salesmanStatisticalVO);
            if (StringUtils.isNotEmpty(String.valueOf(salesman.getAddTime()))) {
                salesmanStatisticalVO.setAddTime(TigUtils.handelTime(salesman.getAddTime()));
            }

            //查询用户信息
            User user = userService.getIfAvailable().getById(salesman.getUserId());
            SalesmanStatisticalVO.SalesmanInfo salesmanInfo = new SalesmanStatisticalVO.SalesmanInfo();
            BeanUtils.copyProperties(user, salesmanInfo);
            if (user.getDistributionRegisterTime() > 0) {
                salesmanInfo.setDistributionRegisterTime(TigUtils.handelTime((long) user.getDistributionRegisterTime()));
            } else {
                salesmanInfo.setDistributionRegisterTime("");
            }
            salesmanStatisticalVO.setBaseUserInfo(salesmanInfo);
            //查询分组信息
            if (salesman.getGroupId() > 0) {
                SalesmanGroup salesmanGroup = salesmanGroupService.getById(salesman.getGroupId());
                if (salesmanGroup != null) {
                    SalesmanStatisticalVO.SalesmanGroupInfo salesmanGroupInfo = new SalesmanStatisticalVO.SalesmanGroupInfo();
                    BeanUtils.copyProperties(salesmanGroup, salesmanGroupInfo);
                    salesmanStatisticalVO.setGroupInfo(salesmanGroupInfo);
                }
            }
            salesmanOrderVO.setSalesman(salesmanStatisticalVO);

            //组合收益
            SalesmanOrderVO.SalesmanProductDataVO salesmanProductDataVO = JSONUtil.toBean(salesmanOrder.getSalesmanProductData(),
                    SalesmanOrderVO.SalesmanProductDataVO.class);

            salesmanOrderVO.setSalesmanProductData(salesmanProductDataVO);

            String unit = getString(pageQuery, salesmanProductDataVO);

            if (salesmanProductDataVO.getCommissionData() != null) {
                // 口径与下单计算 amount 保持一致：
                //   - 本人一档（orderSaleType != 2）取 rate
                //   - 上级一档（orderSaleType == 2）取"被邀请人(下级)等级"的 downSalesmanRate
                // 匹配 level 时优先用快照里保存的 salesman.level（下单时的等级），
                // 快照缺失（老数据）才回退到当前分销员的实时 level。
                SalesmanJsonVO snapshotSalesman = salesmanProductDataVO.getSalesman();
                Integer matchLevel = (snapshotSalesman != null && snapshotSalesman.getLevel() != null)
                        ? snapshotSalesman.getLevel()
                        : salesmanStatisticalVO.getLevel();
                boolean isParentRecord = snapshotSalesman != null
                        && snapshotSalesman.getOrderSaleType() != null
                        && snapshotSalesman.getOrderSaleType() == 2;
                salesmanProductDataVO.getCommissionData().forEach(
                        commissionData -> commissionData.getLevelArr().forEach(
                                levelData -> {
                                    if (matchLevel != null && matchLevel.equals(levelData.getLevel())) {
                                        String rateStr = isParentRecord
                                                ? levelData.getDownSalesmanRate()
                                                : levelData.getRate();
                                        salesmanOrderVO.setProfitComposition(unit + rateStr);
                                    }
                                }));
            }

            //查询订单信息
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderId, salesmanOrder.getOrderId());
            Order order = orderService.getIfAvailable().getOne(orderWrapper);
            salesmanOrderVO.setUserOrder(order);

            Aftersales aftersales = aftersalesService.lambdaQuery()
                    .eq(Aftersales::getOrderId, salesmanOrder.getOrderId())
                    .last("limit 1")
                    .one();
            if (aftersales != null) {
                salesmanOrderVO.setRefundTime(TigUtils.handelTime(aftersales.getAddTime()));
            }

            //设计user_order_info 信息
            SalesmanOrderVO.OrderUserInfoVO orderUserInfoVO = new SalesmanOrderVO.OrderUserInfoVO();
            BeanUtils.copyProperties(order, orderUserInfoVO);
            SalesmanOrderVO.UserInfoVO userInfoVO = new SalesmanOrderVO.UserInfoVO();
            User customer = userService.getIfAvailable().getById(orderUserInfoVO.getUserId());
            if (customer != null) {
                BeanUtils.copyProperties(customer, userInfoVO);
                orderUserInfoVO.setUser(userInfoVO);
            }
            orderUserInfoVO.setAddTime(TigUtils.handelTime(order.getAddTime() * 1000L));

            orderUserInfoVO.setPayTime(TigUtils.handelTime(order.getAddTime() * 1000L));
            salesmanOrderVO.setOrderUserInfo(orderUserInfoVO);

            //查询订单item信息
            LambdaQueryWrapper<OrderItem> orderItemWrapper = new LambdaQueryWrapper<>();
            orderItemWrapper.eq(OrderItem::getItemId, salesmanOrder.getItemId());
            OrderItem orderItem = orderItemService.getOne(orderItemWrapper);

            SalesmanOrderVO.UserOrderItemVO userOrderItemVO = new SalesmanOrderVO.UserOrderItemVO();
            if (orderItem != null) {
                BeanUtils.copyProperties(orderItem, userOrderItemVO);
                userOrderItemVO.setTotalProductMoney(orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity())));
            }
            salesmanOrderVO.setUserOrderItem(userOrderItemVO);
            return salesmanOrderVO;
        }).toList();

        if (pageQuery.getIsExport() != null && pageQuery.getIsExport() > 0) {
            if (pageQuery.getRange() > 0) {
                exportOrder(salesmanOrderVOList, response);
            } else {
                exportProduct(salesmanOrderVOList, response);
            }
        }

        return PageUtil.convertPage(queryPage, salesmanOrderVOList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addSalesmanOrder(List<OrderItem> orderItemList, Map<String, Integer> salesmanOrder) {
        for (OrderItem item : orderItemList) {
            if (salesmanOrder.containsKey(item.getProductId() + "_" + item.getSkuId())) {
                // 当前分销员添加分销金额
                applicationContext.getBean(getClass()).addNewOrderItem(item, salesmanOrder.get(item.getProductId() + "_" + item.getSkuId()), false);
                // 父级分销员添加分销金额
                applicationContext.getBean(getClass()).addNewOrderItem(item, salesmanOrder.get(item.getProductId() + "_" + item.getSkuId()), true);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addNewOrderItem(OrderItem item, Integer salesmanId, boolean isParent) {
        if (isParent) {
            // 判断是否开启二级分销
            Integer saleType = salesmanConfigService.detail("salesmanConfig").getSaleType();
            if (saleType == 1) {
                return;
            }
            Salesman salesman = salesmanService.getIfAvailable().getById(salesmanId);
            if (salesman == null) {
                return;
            }
            // 判断分销员是否有父级
            if (salesman.getPid() == null) {
                return;
            }
            Salesman parent = salesmanService.getIfAvailable().getById(salesman.getPid());
            if (parent == null) {
                return;
            }
        }

        SalesmanProductVO salesmanProductVO = salesmanProductService.detailByProductId(item.getProductId());
        if (salesmanProductVO == null || salesmanProductVO.getIsJoin() != 1) {
            return;
        }
        SalesmanVO salesmanVO = salesmanService.getIfAvailable().getSalesman(salesmanId);
        if (salesmanVO == null) {
            return;
        }
        BigDecimal amount = BigDecimal.ZERO;
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        if (salesmanProductVO.getCommissionType() == 1) {
            BigDecimal rate = BigDecimal.ZERO;
            for (SalesmanConfigVO.Level levelVO : salesmanConfig.getLevel()) {
                if (levelVO.getId().equals(salesmanVO.getLevel())) {
                    try {
                        if (isParent) {
                            rate = new BigDecimal(levelVO.getDownSalesmanRate());
                        } else {
                            rate = new BigDecimal(levelVO.getRate());
                        }
                    } catch (Exception e) {
                        log.error("获取分销金额失败", e);
                    }
                    break;
                }
            }
            amount = amount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())).multiply(rate).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));

            // 使用默认比例时，将分销配置中的比例数据填充到commissionData中，确保订单保存的数据与实际计算使用的比例一致
            if (salesmanConfig.getLevel() != null && !salesmanConfig.getLevel().isEmpty()) {
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
        } else if (salesmanProductVO.getCommissionType() == 2) {
            for (SalesmanProductVO.CommissionData commissionDataTableVO : salesmanProductVO.getCommissionData()) {
                for (SalesmanProductVO.CommissionData.LevelData levelArrVO : commissionDataTableVO.getLevelArr()) {
                    if (levelArrVO.getLevel().equals(salesmanVO.getLevel())) {
                        BigDecimal rate = BigDecimal.ZERO;
                        try {
                            if (isParent) {
                                rate = new BigDecimal(levelArrVO.getDownSalesmanRate());
                            } else {
                                rate = new BigDecimal(levelArrVO.getRate());
                            }
                        } catch (Exception e) {
                            log.error("获取分销金额失败", e);
                        }
                        amount = amount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())).multiply(rate).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                        break;
                    }
                }
            }

        } else {
            for (SalesmanProductVO.CommissionData commissionDataTableVO : salesmanProductVO.getCommissionData()) {
                for (SalesmanProductVO.CommissionData.LevelData levelArrVO : commissionDataTableVO.getLevelArr()) {
                    if (levelArrVO.getLevel().equals(salesmanVO.getLevel())) {
                        BigDecimal rate = BigDecimal.ZERO;
                        try {
                            if (isParent) {
                                rate = new BigDecimal(levelArrVO.getDownSalesmanRate());
                            } else {
                                rate = new BigDecimal(levelArrVO.getRate());
                            }
                        } catch (Exception e) {
                            log.error("获取分销金额失败", e);
                        }
                        amount = amount.add(rate);
                        break;
                    }
                }
            }
        }

        if (amount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            amount = BigDecimal.ZERO;
        }

        if (amount.equals(BigDecimal.ZERO)) {
            return;
        }

        SalesmanOrder salesmanOrder = new SalesmanOrder();

        // 将 OrderItem 的属性复制到 SalesmanOrder 对象中
        BeanUtils.copyProperties(item, salesmanOrder);

        // 设置分销员ID
        if (isParent) {
            salesmanOrder.setSalesmanId(salesmanVO.getPid());
        } else {
            salesmanOrder.setSalesmanId(salesmanId);
        }
        Salesman salesman = salesmanService.getIfAvailable().getById(salesmanId);
        SalesmanJsonVO salesmanJsonVO = new SalesmanJsonVO();
        BeanUtils.copyProperties(salesman, salesmanJsonVO);
        if (isParent) {
            salesmanJsonVO.setOrderSaleType(2);
        }
        salesmanProductVO.setSalesman(salesmanJsonVO);
        salesmanOrder.setSalesmanProductData(JsonUtil.toJson(salesmanProductVO));

        SalesmanConfigVO salesmanSettlement = salesmanConfigService.detail("salesmanSettlement");
        if (salesmanSettlement != null) {
            SalesmanOrderVO.SalesmanSettingVO salesmanSettingVO = new SalesmanOrderVO.SalesmanSettingVO();
            BeanUtils.copyProperties(salesmanSettlement, salesmanSettingVO);
            salesmanOrder.setSalesmanSettlementData(JsonUtil.toJson(salesmanSettlement));
        }

        salesmanOrder.setAddTime(StringUtils.getCurrentTime());
        salesmanOrder.setOrderAmount(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        salesmanOrder.setAmount(amount);
        // 保存 SalesmanOrder 对象到数据库
        this.save(salesmanOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addSalesmanOrderForWechatBiz(List<OrderItem> orderItemList, Integer buyerUserId) {
        if (buyerUserId == null || CollUtil.isEmpty(orderItemList)) {
            return;
        }
        // 微商模式佣金按买家的上级链归属：买家须是分销员且有上级
        Salesman buyer = salesmanService.getIfAvailable().lambdaQuery()
                .eq(Salesman::getUserId, buyerUserId)
                .last("limit 1")
                .one();
        if (buyer == null) {
            return;
        }
        if (buyer.getPid() == null || buyer.getPid() <= 0) {
            return;
        }
        Salesman directUpline = salesmanService.getIfAvailable().getById(buyer.getPid());
        if (directUpline == null) {
            return;
        }

        // 二级上级：仅开启二级分销（saleType != 1）时结算
        Salesman secondUpline = null;
        Integer saleType = salesmanConfigService.detail("salesmanConfig").getSaleType();
        if (saleType != null && saleType != 1 && directUpline.getPid() != null && directUpline.getPid() > 0) {
            secondUpline = salesmanService.getIfAvailable().getById(directUpline.getPid());
        }

        for (OrderItem item : orderItemList) {
            addWechatBizOrderItem(item, directUpline, directUpline, false);
            if (secondUpline != null) {
                addWechatBizOrderItem(item, secondUpline, directUpline, true);
            }
        }
    }

    /**
     * 微商模式单条佣金记录。
     *
     * @param beneficiary  佣金受益人（直接上级或上上级）
     * @param directUpline 买家的直接上级（费率查表与快照均按其等级）
     * @param useDownRate  true 取 downSalesmanRate（二级佣金），false 取 rate（一级佣金）
     */
    private void addWechatBizOrderItem(OrderItem item, Salesman beneficiary, Salesman directUpline, boolean useDownRate) {
        SalesmanProductVO salesmanProductVO = salesmanProductService.detailByProductId(item.getProductId());
        if (salesmanProductVO == null || salesmanProductVO.getIsJoin() != 1) {
            return;
        }
        Integer lookupLevel = directUpline.getLevel();
        BigDecimal amount = BigDecimal.ZERO;
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        if (salesmanProductVO.getCommissionType() == 1) {
            BigDecimal rate = BigDecimal.ZERO;
            for (SalesmanConfigVO.Level levelVO : salesmanConfig.getLevel()) {
                if (levelVO.getId().equals(lookupLevel)) {
                    try {
                        rate = new BigDecimal(useDownRate ? levelVO.getDownSalesmanRate() : levelVO.getRate());
                    } catch (Exception e) {
                        log.error("获取分销金额失败", e);
                    }
                    break;
                }
            }
            amount = amount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())).multiply(rate).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));

            // 使用默认比例时，将分销配置中的比例数据填充到commissionData中，确保订单保存的数据与实际计算使用的比例一致
            if (salesmanConfig.getLevel() != null && !salesmanConfig.getLevel().isEmpty()) {
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
        } else if (salesmanProductVO.getCommissionType() == 2) {
            for (SalesmanProductVO.CommissionData commissionDataTableVO : salesmanProductVO.getCommissionData()) {
                for (SalesmanProductVO.CommissionData.LevelData levelArrVO : commissionDataTableVO.getLevelArr()) {
                    if (levelArrVO.getLevel().equals(lookupLevel)) {
                        BigDecimal rate = BigDecimal.ZERO;
                        try {
                            rate = new BigDecimal(useDownRate ? levelArrVO.getDownSalesmanRate() : levelArrVO.getRate());
                        } catch (Exception e) {
                            log.error("获取分销金额失败", e);
                        }
                        amount = amount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())).multiply(rate).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                        break;
                    }
                }
            }
        } else {
            for (SalesmanProductVO.CommissionData commissionDataTableVO : salesmanProductVO.getCommissionData()) {
                for (SalesmanProductVO.CommissionData.LevelData levelArrVO : commissionDataTableVO.getLevelArr()) {
                    if (levelArrVO.getLevel().equals(lookupLevel)) {
                        BigDecimal rate = BigDecimal.ZERO;
                        try {
                            rate = new BigDecimal(useDownRate ? levelArrVO.getDownSalesmanRate() : levelArrVO.getRate());
                        } catch (Exception e) {
                            log.error("获取分销金额失败", e);
                        }
                        amount = amount.add(rate);
                        break;
                    }
                }
            }
        }

        if (amount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return;
        }

        SalesmanOrder salesmanOrder = new SalesmanOrder();

        // 将 OrderItem 的属性复制到 SalesmanOrder 对象中
        BeanUtils.copyProperties(item, salesmanOrder);

        // 佣金归属受益人
        salesmanOrder.setSalesmanId(beneficiary.getSalesmanId());

        // 快照按直接上级保存（列表展示按快照 level 查 rate / downSalesmanRate）
        SalesmanJsonVO salesmanJsonVO = new SalesmanJsonVO();
        BeanUtils.copyProperties(directUpline, salesmanJsonVO);
        if (useDownRate) {
            salesmanJsonVO.setOrderSaleType(2);
        }
        salesmanProductVO.setSalesman(salesmanJsonVO);
        salesmanOrder.setSalesmanProductData(JsonUtil.toJson(salesmanProductVO));

        SalesmanConfigVO salesmanSettlement = salesmanConfigService.detail("salesmanSettlement");
        if (salesmanSettlement != null) {
            salesmanOrder.setSalesmanSettlementData(JsonUtil.toJson(salesmanSettlement));
        }

        salesmanOrder.setAddTime(StringUtils.getCurrentTime());
        salesmanOrder.setOrderAmount(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        salesmanOrder.setAmount(amount);
        this.save(salesmanOrder);
    }

    @Override
    public Page<CustomerTransactionOrderVO> listCustomerTransaction(CustomerTransactionPageQuery pageQuery) {
        Page<Object> page = Page.of(pageQuery.getPage(), pageQuery.getSize());
        List<CustomerTransactionVO> customerTransactionList = getBaseMapper().listCustomerTransaction(page, pageQuery);
        if (CollUtil.isEmpty(customerTransactionList)) {
            if (pageQuery.getIsExport() != null && pageQuery.getIsExport() > 0) {
                //导出数据
                export(List.of(), response);
            }
            return PageUtil.convertPage(page, List.of());
        }

        customerTransactionList.forEach(customerTransactionVO -> {
            Order order = orderService.getIfAvailable().getById(customerTransactionVO.getOrderId());
            BeanUtils.copyProperties(order, customerTransactionVO);
        });

        List<Integer> userIds = customerTransactionList.stream().map(CustomerTransactionVO::getUserId).distinct().toList();
        Map<Integer, User> userMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userIds)) {
            List<User> userList = userMapper.selectBatchIds(userIds);
            userMap = userList.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
        }

        // 调用方法获取会员信息
        List<Integer> orderIds = customerTransactionList.stream().map(CustomerTransactionVO::getOrderId).toList();
        List<OrderItemVO> itemListNew = orderService.getIfAvailable().getOrderItemList(orderIds);

        // 插入售后数据
        Map<Integer, List<OrderItemVO>> itemMap = itemListNew.stream().collect(Collectors.groupingBy(OrderItemVO::getOrderId));
        // 取出店铺Id
        List<Integer> shopIds = customerTransactionList.stream().map(CustomerTransactionVO::getShopId).distinct().toList();
        List<Shop> shopList = shopMapper.selectBatchIds(shopIds);
        Map<Integer, Shop> shopMap = shopList.stream().collect(Collectors.toMap(Shop::getShopId, Function.identity()));

        // 查询店铺结算配置
        List<OrderConfig> orderConfigs = orderConfigMapper.selectList(
                new LambdaQueryWrapper<OrderConfig>()
                        .in(OrderConfig::getShopId, shopIds)
        );
        Map<Integer, OrderConfig> orderConfigMap = orderConfigs.stream().collect(Collectors.toMap(OrderConfig::getShopId, Function.identity()));

        // 转换为VO
        Map<Integer, User> finalUserMap = userMap;
        String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
        String autoDeliveryDays = configService.getConfigVal(SettingsEnum.AUTO_DELIVERY_DAYS);
        List<CustomerTransactionOrderVO> orderVOList = customerTransactionList.stream()
                .map(order -> {
                    CustomerTransactionOrderVO orderVO = new CustomerTransactionOrderVO();
                    BeanUtils.copyProperties(order, orderVO);
                    orderVO.setSalesmanName(userMapper.selectById(salesmanService.getIfAvailable().getById(order.getSalesmanId()).getUserId()).getNickname());
                    orderVO.setSalesmanId(order.getSalesmanId());
                    orderVO.setRegionIds(JsonUtil.jsonToList(order.getRegionIds(), Integer.class));
                    orderVO.setRegionNames(JsonUtil.jsonToList(order.getRegionNames(), String.class));
                    boolean isCycleMainOrder = false;
                    if (OrderType.isReservationOrder(order.getOrderType()) && !isCycleMainOrder) {
                        orderVO.setOrderStatusName(OrderReservationStatusEnum.getStatusName(order.getOrderStatus()));
                    } else if (Objects.equals(order.getShippingTypeId(), OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
                        orderVO.setOrderStatusName(OrderSameCityEnum.getStatusName(order.getOrderStatus()));
                    } else {
                        orderVO.setOrderStatusName(OrderStatusEnum.getStatusName(order.getOrderStatus()));
                    }
                    orderVO.setPayStatusName(PaymentStatus.getStatusName(order.getPayStatus()));
                    orderVO.setShippingStatusName(ShippingStatusEnum.getStatusName(order.getShippingStatus()));
                    orderVO.setInvoiceData(JSON.parseObject(order.getInvoiceData(), OrderInvoiceVO.class));
                    if (orderVO.getRegionNames() != null) {
                        orderVO.setUserAddress(String.join(" ", orderVO.getRegionNames()) + " " + order.getAddress());
                    }
                    UserAddressVO addressData = JsonUtil.fromJson(order.getAddressData(), UserAddressVO.class);
                    orderVO.setAddressData(addressData);

                    DateTime addDate = DateUtil.date(order.getAddTime() * 1000);
                    orderVO.setAddTime(DateUtil.format(addDate, DATE_FORMAT));

                    if (order.getPayTime() > 0) {
                        DateTime payDate = DateUtil.date(order.getPayTime() * 1000);
                        orderVO.setPayTime(DateUtil.format(payDate, DATE_FORMAT));
                    }

                    if (order.getShippingTime() > 0) {
                        DateTime shippingDate = DateUtil.date(order.getShippingTime() * 1000);
                        orderVO.setShippingTime(DateUtil.format(shippingDate, DATE_FORMAT));
                    }

                    if (order.getReceivedTime() > 0) {
                        DateTime receivedDate = DateUtil.date(order.getReceivedTime() * 1000);
                        orderVO.setReceivedTime(DateUtil.format(receivedDate, DATE_FORMAT));
                    }

                    orderVO.setItems(itemMap.get(order.getOrderId()));
                    UserBaseVO userBaseVO = new UserBaseVO();
                    User user = finalUserMap.get(order.getUserId());
                    if (user != null) {
                        BeanUtils.copyProperties(user, userBaseVO);
                    }
                    orderVO.setUser(userBaseVO);
                    orderVO.setAvailableActions(orderService.getIfAvailable().availableActions(orderVO));

                    if (order.getShopId() != null && order.getShopId() > 0) {
                        OrderVO.ShopVO shopVO = new OrderVO.ShopVO();
                        Shop shop = shopMap.get(order.getShopId());
                        if (shop != null) {
                            BeanUtils.copyProperties(shop, shopVO);
                            shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
                            orderVO.setShop(shopVO);
                            orderVO.setShopName(shopVO.getShopTitle());
                            orderVO.setShopId(shopVO.getShopId());
                        }
                    }

                    // 判断是否可以申请售后；1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）2. 全局设置（买家申请售后限制）
                    boolean toAftersales = orderVO.getAvailableActions().getToAftersales();
                    orderVO.setToAftersalses(toAftersales);
                    if (toAftersales) {
                        // 1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）
                        if (order.getShopId() != null && order.getShopId() > 0) {
                            OrderConfig orderConfig = orderConfigMap.get(order.getShopId());
                            if (orderConfig != null) {
                                OrderConfigDTO orderConfigData = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDTO.class);
                                if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && 0 == orderConfigData.getDateType()) {
                                    orderVO.setToAftersalses(false);
                                }
                            }
                        }
                        // 2. 全局设置（买家申请售后限制）
                        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()) {
                            if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                                long receivedTime = order.getReceivedTime() * 1000;
                                BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                                LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                                boolean toAftersalses = limitDateTime.isAfter(LocalDateTime.now());
                                orderVO.setToAftersalses(toAftersalses);
                            }
                        }
                    }
                    orderVO.getAvailableActions().setToAftersales(orderVO.getToAftersalses());

                    // 自动发货天数
                    Long aftersalesCount = aftersalesMapper.selectCount(
                            Wrappers.lambdaQuery(Aftersales.class)
                                    .eq(Aftersales::getOrderId, order.getOrderId())
                                    .eq(Aftersales::getStatus, AftersalesStatusEnum.IN_REVIEW.getCode())
                    );
                    if (aftersalesCount == 0 && OrderStatusEnum.PROCESSING.getCode() == order.getOrderStatus() && StrUtil.isNotBlank(autoDeliveryDays)) {
                        long shippingTimeMillis = order.getShippingTime() * 1000;
                        long elapsedMillis = Instant.now().toEpochMilli() - shippingTimeMillis;

                        long elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis);

                        BigDecimal result = new BigDecimal(autoDeliveryDays).subtract(new BigDecimal(elapsedDays)).setScale(0, RoundingMode.UP);

                        orderVO.setAutoDeliveryDays(result);
                    }

                    return orderVO;
                }).toList();
        if (pageQuery.getIsExport() != null && pageQuery.getIsExport() > 0) {
            //导出数据
            export(orderVOList, response);
        }
        return PageUtil.convertPage(page, orderVOList);
    }

    //导出数据
    private void export(List<CustomerTransactionOrderVO> customerTransactionOrderVOS, HttpServletResponse response) {
        List<CustomerTransactionOrderExportVO> exportVO = new ArrayList<>();
        if (customerTransactionOrderVOS != null) {
            for (CustomerTransactionOrderVO transactionOrderVO : customerTransactionOrderVOS) {
                for (OrderItemVO item : transactionOrderVO.getItems()) {
                    CustomerTransactionOrderExportVO orderExportVO = new CustomerTransactionOrderExportVO();
                    orderExportVO.setOrderSn(transactionOrderVO.getOrderSn());
                    orderExportVO.setAddTime(transactionOrderVO.getAddTime());
                    orderExportVO.setProductName(item.getProductName() + " x" + item.getQuantity());
                    orderExportVO.setReceivedTime(transactionOrderVO.getReceivedTime());
                    orderExportVO.setSalesmanName(transactionOrderVO.getSalesmanName());
                    orderExportVO.setUsername(transactionOrderVO.getUser().getUsername());
                    orderExportVO.setUserMobile(transactionOrderVO.getAddressData().getMobile());
                    orderExportVO.setUserAddress(transactionOrderVO.getUserAddress());
                    orderExportVO.setPayTypeStr(transactionOrderVO.getPayTypeId() == 1 ? "在线支付" : transactionOrderVO.getPayTypeId() == 2 ? "货到付款" : "线下支付");
                    orderExportVO.setOrderStatusName(transactionOrderVO.getOrderStatusName());
                    orderExportVO.setBalance(transactionOrderVO.getBalance());
                    exportVO.add(orderExportVO);
                }
            }
        }
        // 使用合并策略导出
        try {
            ExcelWriterSheetBuilder writerSheetBuilder = EasyExcel.write(response.getOutputStream(), CustomerTransactionOrderExportVO.class)
                    .autoCloseStream(true)
                    .registerWriteHandler(new OrderMergeStrategy(exportVO))
                    .sheet("客户成交订单导出");
            writerSheetBuilder.doWrite(exportVO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取unit
    private static String getString(SalesmanOrderPageQuery listDTO, SalesmanOrderVO.SalesmanProductDataVO salesmanProductDataVO) {
        String unit;
        //类型-比例集合
        List<Integer> commissionType = new ArrayList<>() {
            {
                add(1);
                add(2);
            }
        };
        if (listDTO.getIsExport() != null && listDTO.getIsExport() > 0) {
            if (commissionType.contains(salesmanProductDataVO.getCommissionType())) {
                unit = "比例";
            } else {
                unit = "金额";
            }
        } else {
            unit = "";
        }
        return unit;
    }

    //获取订单维度结算报表
    public void exportOrder(List<SalesmanOrderVO> salesmanOrderVOList, HttpServletResponse response) {
        List<SalesmanOrderExportOrderVO> exportVO = new ArrayList<>();
        for (SalesmanOrderVO salesmanOrderVO : salesmanOrderVOList) {
            SalesmanOrderExportOrderVO salesmanOrderExportOrderVO = new SalesmanOrderExportOrderVO();
            salesmanOrderExportOrderVO.setAddTime(salesmanOrderVO.getOrderUserInfo().getAddTime());
            salesmanOrderExportOrderVO.setPayTime(salesmanOrderVO.getOrderUserInfo().getPayTime());
            salesmanOrderExportOrderVO.setDistributionRegisterTime(salesmanOrderVO.getSalesman().
                    getBaseUserInfo().getDistributionRegisterTime());
            salesmanOrderExportOrderVO.setCustomerNickname(salesmanOrderVO.getOrderUserInfo().getUser().getNickname());
            salesmanOrderExportOrderVO.setCustomerMobile(salesmanOrderVO.getOrderUserInfo().getUser().getMobile());
            salesmanOrderExportOrderVO.setShopTitle("自营");
            salesmanOrderExportOrderVO.setGroupName(salesmanOrderVO.getSalesman().getGroupInfo().getGroupName());
            salesmanOrderExportOrderVO.setOrderSn(salesmanOrderVO.getOrderUserInfo().getOrderSn());
            salesmanOrderExportOrderVO.setTotalProductMoney(salesmanOrderVO.getUserOrderItem().getTotalProductMoney());
            if (salesmanOrderVO.getProfitComposition().contains("比例")) {
                salesmanOrderExportOrderVO.setProfitComposition(salesmanOrderVO.getProfitComposition() + "%");
            } else {
                salesmanOrderExportOrderVO.setProfitComposition(salesmanOrderVO.getProfitComposition());
            }
            salesmanOrderExportOrderVO.setAmount(salesmanOrderVO.getAmount());
            salesmanOrderExportOrderVO.setStatus(salesmanOrderVO.getStatusText());
            salesmanOrderExportOrderVO.setSettlementTime(salesmanOrderVO.getSettlementTime());
            if (salesmanOrderVO.getSalesmanSettlementData().getSettlementType() == 1) {
                salesmanOrderExportOrderVO.setSettlementType("自动结算");
            } else {
                salesmanOrderExportOrderVO.setSettlementType("人工结算");
            }
            salesmanOrderExportOrderVO.setOrderSource(salesmanOrderVO.getOrderUserInfo().getOrderSource());
            salesmanOrderExportOrderVO.setOrderStatus(OrderStatusEnum.getStatusName(salesmanOrderVO.getOrderUserInfo().getOrderStatus()));
            //查询退款
            QueryWrapper<RefundLog> refundLogQueryWrapper = new QueryWrapper<>();
            refundLogQueryWrapper.eq("order_id", salesmanOrderVO.getOrderId());
            refundLogQueryWrapper.eq("user_id", salesmanOrderVO.getOrderUserInfo().getUserId());
            refundLogQueryWrapper.select("sum(refund_amount) as refund_amount");
            RefundLog refundLog = refundLogService.getOne(refundLogQueryWrapper);
            if (refundLog != null) {
                salesmanOrderExportOrderVO.setRefundAmount(refundLog.getRefundAmount());
            } else {
                salesmanOrderExportOrderVO.setRefundAmount(new BigDecimal(0));
            }
            LambdaQueryWrapper<RefundLog> refundLogQueryWrapperTime = new LambdaQueryWrapper<>();
            refundLogQueryWrapperTime.eq(RefundLog::getOrderId, salesmanOrderVO.getOrderId());
            refundLogQueryWrapperTime.eq(RefundLog::getUserId, salesmanOrderVO.getOrderUserInfo().getUserId());
            refundLogQueryWrapperTime.orderByDesc(RefundLog::getLogId);
            RefundLog refundLogTime = refundLogService.getOne(refundLogQueryWrapperTime);
            if (refundLogTime != null) {
                Date dateOrderPay = new Date(refundLogTime.getAddTime() * 1000L);
                SimpleDateFormat sdfOrderPay = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                salesmanOrderExportOrderVO.setRefundTime(sdfOrderPay.format(dateOrderPay));
            } else {
                salesmanOrderExportOrderVO.setRefundTime("");
            }
            exportVO.add(salesmanOrderExportOrderVO);
        }
        ExcelUtils<SalesmanOrderExportOrderVO> excelUtils = new ExcelUtils<>(exportVO, SalesmanOrderExportOrderVO.class);
        excelUtils.exportExcelWeb(response);
    }

    //获取商品维度统计报表
    public void exportProduct(List<SalesmanOrderVO> salesmanOrderVOList, HttpServletResponse response) {
        List<SalesmanOrderExportProductVO> exportVO = new ArrayList<>();
        for (SalesmanOrderVO salesmanOrderVO : salesmanOrderVOList) {
            SalesmanOrderExportProductVO salesmanOrderExportProductVO = new SalesmanOrderExportProductVO();
            salesmanOrderExportProductVO.setAddTime(salesmanOrderVO.getOrderUserInfo().getAddTime());
            salesmanOrderExportProductVO.setPayTime(salesmanOrderVO.getOrderUserInfo().getPayTime());
            salesmanOrderExportProductVO.setDistributionRegisterTime(salesmanOrderVO.getSalesman().
                    getBaseUserInfo().getDistributionRegisterTime());
            salesmanOrderExportProductVO.setCustomerNickname(salesmanOrderVO.getOrderUserInfo().getUser().getNickname());
            salesmanOrderExportProductVO.setCustomerMobile(salesmanOrderVO.getOrderUserInfo().getUser().getMobile());
            salesmanOrderExportProductVO.setShopTitle("自营");
            salesmanOrderExportProductVO.setGroupName(salesmanOrderVO.getSalesman().getGroupInfo().getGroupName());
            salesmanOrderExportProductVO.setOrderSn(salesmanOrderVO.getOrderUserInfo().getOrderSn());
            salesmanOrderExportProductVO.setTotalProductMoney(salesmanOrderVO.getUserOrderItem().getTotalProductMoney());
            if (salesmanOrderVO.getProfitComposition().contains("比例")) {
                salesmanOrderExportProductVO.setProfitComposition(salesmanOrderVO.getProfitComposition() + "%");
            } else {
                salesmanOrderExportProductVO.setProfitComposition(salesmanOrderVO.getProfitComposition());
            }
            salesmanOrderExportProductVO.setProductSn(salesmanOrderVO.getUserOrderItem().getProductSn());
            salesmanOrderExportProductVO.setProductName(salesmanOrderVO.getUserOrderItem().getProductName());
            salesmanOrderExportProductVO.setPrice(salesmanOrderVO.getUserOrderItem().getPrice());
            salesmanOrderExportProductVO.setQuantity(salesmanOrderVO.getUserOrderItem().getQuantity());
            if (salesmanOrderVO.getSalesmanProductData().getIsJoin() > 0) {
                salesmanOrderExportProductVO.setIsJoin("参与");
            } else {
                salesmanOrderExportProductVO.setIsJoin("不参与");
            }
            exportVO.add(salesmanOrderExportProductVO);
        }
        ExcelUtils<SalesmanOrderExportProductVO> excelUtils = new ExcelUtils<>(exportVO, SalesmanOrderExportProductVO.class);
        excelUtils.exportExcelWeb(response);
    }

    /**
     * 拆分分销订单（用于订单拆分场景，如部分发货、店铺拆单）
     * <p>
     * 当订单拆分时，同一个原 item_id 可能会被拆成多个新 item_id（如部分发货场景：3件发2件留1件）。
     * 此方法会按比例将原分销订单的佣金拆分到各个新订单中，确保总佣金不变。
     * </p>
     *
     * @param originalOrderVO      原订单信息
     * @param oldItemIds           原订单项ID列表
     * @param newOrderItemsMap     新订单ID -> 新订单项列表的映射
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void splitSalesmanOrders(OrderVO originalOrderVO, List<Integer> oldItemIds, Map<Integer, List<OrderItem>> newOrderItemsMap) {
        if (CollUtil.isEmpty(oldItemIds) || CollUtil.isEmpty(newOrderItemsMap)) {
            return;
        }

        // 1. 查询原订单的所有分销订单记录（按原订单项ID）
        List<SalesmanOrder> oldSalesmanOrders = this.lambdaQuery()
                .eq(SalesmanOrder::getOrderId, originalOrderVO.getOrderId())
                .in(SalesmanOrder::getItemId, oldItemIds)
                .list();

        if (CollUtil.isEmpty(oldSalesmanOrders)) {
            return;
        }

        // 2. 按原 itemId 和 productId 分组原分销订单记录
        // 一个 itemId 可能对应多条 salesman_order（本人+上级各一条）
        Map<Integer, List<SalesmanOrder>> oldSalesmanOrdersByItemId = oldSalesmanOrders.stream()
                .collect(Collectors.groupingBy(SalesmanOrder::getItemId));

        // 3. 计算原订单总金额（用于比例计算）
        BigDecimal originalTotalAmount = originalOrderVO.getTotalAmount();
        if (originalTotalAmount == null || originalTotalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            originalTotalAmount = originalOrderVO.getProductAmount();
        }

        // 4. 遍历每个新订单及其订单项，创建新的分销订单记录
        List<SalesmanOrder> newSalesmanOrders = new ArrayList<>();

        for (Map.Entry<Integer, List<OrderItem>> entry : newOrderItemsMap.entrySet()) {
            Integer newOrderId = entry.getKey();
            List<OrderItem> newOrderItems = entry.getValue();

            if (CollUtil.isEmpty(newOrderItems)) {
                continue;
            }

            // 计算新订单的总金额（用于比例计算）
            BigDecimal newOrderTotalAmount = BigDecimal.ZERO;
            for (OrderItem item : newOrderItems) {
                newOrderTotalAmount = newOrderTotalAmount.add(
                        item.getPrice().multiply(new BigDecimal(item.getQuantity()))
                );
            }

            // 计算金额比例
            BigDecimal ratio = BigDecimal.ZERO;
            if (originalTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
                ratio = newOrderTotalAmount.divide(originalTotalAmount, 6, RoundingMode.HALF_UP);
            }

            // 遍历新订单的每个订单项
            for (OrderItem newItem : newOrderItems) {
                // 找到该商品对应的原分销订单记录
                // 通过 productId 匹配，因为拆分后 itemId 已经变了
                List<SalesmanOrder> matchedOldOrders = findMatchedSalesmanOrders(
                        oldSalesmanOrdersByItemId, newItem.getProductId(), oldItemIds, originalOrderVO.getItems()
                );

                if (CollUtil.isEmpty(matchedOldOrders)) {
                    continue;
                }

                // 为每条匹配的原分销订单创建新记录
                for (SalesmanOrder oldSalesmanOrder : matchedOldOrders) {
                    SalesmanOrder newSalesmanOrder = new SalesmanOrder();
                    BeanUtils.copyProperties(oldSalesmanOrder, newSalesmanOrder);

                    // 重置ID，让数据库自动生成
                    newSalesmanOrder.setSalesmanOrderId(null);

                    // 更新订单和订单项关联
                    newSalesmanOrder.setOrderId(newOrderId);
                    newSalesmanOrder.setItemId(newItem.getItemId());

                    // 计算拆分后的订单商品金额
                    BigDecimal newOrderAmount = newItem.getPrice().multiply(new BigDecimal(newItem.getQuantity()));
                    newSalesmanOrder.setOrderAmount(newOrderAmount);

                    // 按比例拆分佣金金额
                    // 使用订单项金额比例：newItemAmount / oldItemAmount * oldAmount
                    BigDecimal oldOrderAmount = oldSalesmanOrder.getOrderAmount();
                    if (oldOrderAmount != null && oldOrderAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal itemRatio = newOrderAmount.divide(oldOrderAmount, 6, RoundingMode.HALF_UP);
                        BigDecimal newAmount = oldSalesmanOrder.getAmount()
                                .multiply(itemRatio)
                                .setScale(2, RoundingMode.HALF_UP);
                        newSalesmanOrder.setAmount(newAmount);
                    } else {
                        // 如果原订单金额为0，则按订单比例计算
                        BigDecimal newAmount = oldSalesmanOrder.getAmount()
                                .multiply(ratio)
                                .setScale(2, RoundingMode.HALF_UP);
                        newSalesmanOrder.setAmount(newAmount);
                    }

                    // 更新时间
                    newSalesmanOrder.setAddTime(StringUtils.getCurrentTime());

                    newSalesmanOrders.add(newSalesmanOrder);
                }
            }
        }

        // 5. 批量保存新的分销订单记录
        if (CollUtil.isNotEmpty(newSalesmanOrders)) {
            this.saveBatch(newSalesmanOrders);
        }

        // 6. 删除原分销订单记录
        List<Integer> oldSalesmanOrderIds = oldSalesmanOrders.stream()
                .map(SalesmanOrder::getSalesmanOrderId)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(oldSalesmanOrderIds)) {
            this.removeByIds(oldSalesmanOrderIds);
        }

        log.info("分销订单拆分完成：原订单ID={}，原分销订单数={}，新分销订单数={}",
                originalOrderVO.getOrderId(), oldSalesmanOrderIds.size(), newSalesmanOrders.size());
    }

    /**
     * 根据商品ID找到匹配的原分销订单记录
     *
     * @param oldSalesmanOrdersByItemId 按原订单项ID分组的分销订单
     * @param productId                 要匹配的商品ID
     * @param oldItemIds                原订单项ID列表
     * @param originalItems             原订单项列表
     * @return 匹配的分销订单列表
     */
    private List<SalesmanOrder> findMatchedSalesmanOrders(
            Map<Integer, List<SalesmanOrder>> oldSalesmanOrdersByItemId,
            Integer productId,
            List<Integer> oldItemIds,
            List<OrderItemVO> originalItems) {

        List<SalesmanOrder> result = new ArrayList<>();

        // 遍历原订单项，找到商品ID匹配的订单项
        if (CollUtil.isNotEmpty(originalItems)) {
            for (OrderItemVO originalItem : originalItems) {
                if (originalItem.getProductId().equals(productId) && oldItemIds.contains(originalItem.getItemId())) {
                    List<SalesmanOrder> orders = oldSalesmanOrdersByItemId.get(originalItem.getItemId());
                    if (CollUtil.isNotEmpty(orders)) {
                        result.addAll(orders);
                    }
                }
            }
        }

        // 如果通过原订单项找不到，则直接通过分销订单的productId匹配
        if (CollUtil.isEmpty(result)) {
            for (Map.Entry<Integer, List<SalesmanOrder>> entry : oldSalesmanOrdersByItemId.entrySet()) {
                for (SalesmanOrder order : entry.getValue()) {
                    if (order.getProductId() != null && order.getProductId().equals(productId)) {
                        result.add(order);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public BigDecimal getTotalCommissionByOrderId(Integer orderId) {
        List<SalesmanOrder> salesmanOrders = this.lambdaQuery()
                .eq(SalesmanOrder::getOrderId, orderId)
                .ne(SalesmanOrder::getStatus, SalesmanOrderStatus.INVALID.getCode())
                .list();
        if (CollUtil.isEmpty(salesmanOrders)) {
            return BigDecimal.ZERO;
        }
        return salesmanOrders.stream()
                .map(SalesmanOrder::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualSettle(List<Integer> salesmanOrderIds) {
        if (CollUtil.isEmpty(salesmanOrderIds)) {
            return;
        }
        List<SalesmanOrder> orders = this.listByIds(salesmanOrderIds);
        if (CollUtil.isEmpty(orders)) {
            return;
        }
        // 仅结算"待结算"记录，按所属订单去重后复用订单结算逻辑。
        // 传 settlementType=1：与自动结算资金流转完全一致（标记已结算 + 累加业绩额 + 发放佣金到佣金账户 + 触发升级）。
        List<Integer> orderIds = orders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() == SalesmanOrderStatus.UNSETTLED.getCode())
                .map(SalesmanOrder::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        for (Integer orderId : orderIds) {
            orderService.getIfAvailable().settlement(orderId, 1);
        }
    }
}
