package com.tigshop.service.salesman.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.vo.salesman.OverviewCoreSummaryVO;
import com.tigshop.bean.vo.salesman.OverviewCoreTrendVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.salesman.OverviewService;
import com.tigshop.service.salesman.SalesmanOrderService;
import com.tigshop.service.salesman.SalesmanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author kidd
 * @since 2025/6/23
 */
@RequiredArgsConstructor
@Service
public class OverviewServiceImpl implements OverviewService {

    private final SalesmanService salesmanService;

    private final SalesmanOrderService salesmanOrderService;

    private final OrderService orderService;

    @Override
    public OverviewCoreTrendVO coreTrend(Integer dateType, String startEndTime) {
        List<String> times = StrUtil.split(startEndTime, StrUtil.COMMA);
        // 获取时间范围
        String[] startEndDate = new String[2];
        if (times.size() == 2) {
            String startTime = times.getFirst().isEmpty() ? times.getLast() : times.getFirst();
            String endTime = times.getLast().isEmpty() ? times.getFirst() : times.getLast();
            startEndDate = TigUtils.getRangeDate(dateType, startTime, endTime);
        }
        if (times.size() == 1) {
            startEndDate = TigUtils.getRangeDate(dateType, times.getFirst(), times.getFirst());
        }
        String rawStartDate = startEndDate[0];
        String rawEndDate = startEndDate[1];

        // 获取横轴数据
        List<String> horizontalAxis = getHorizontalAxis(dateType, rawStartDate, rawEndDate);
        Integer shopId = HeaderUtils.getShopId();

        // 查询分销员数据
        long startDate = StringUtils.dateToTimestampExample(normalizeDateTime(rawStartDate, true));
        long endDate = StringUtils.dateToTimestampExample(normalizeDateTime(rawEndDate, false));
        List<Salesman> trendData = salesmanService.lambdaQuery()
                .eq(shopId != null && shopId > 0, Salesman::getShopId, shopId)
                .list();
        if (trendData.isEmpty()) {
            return OverviewCoreTrendVO.builder()
                    .horizontalAxis(horizontalAxis)
                    .longitudinalAxis(new ArrayList<>())
                    .build();
        }
        List<SalesmanOrder> salesmanOrderList = salesmanOrderService.lambdaQuery()
                .in(SalesmanOrder::getSalesmanId, trendData.stream().map(Salesman::getSalesmanId).collect(Collectors.toList()))
                .ge(SalesmanOrder::getAddTime, startDate)
                .le(SalesmanOrder::getAddTime, endDate)
                .list();

        // trendData转Map<String, Object>
        List<Map<String, Object>> trendDataList = salesmanOrderList.stream()
                .map(salesmanOrder -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("addTime", salesmanOrder.getAddTime());
                    map.put("saleAmount", salesmanOrder.getOrderAmount());
                    return map;
                }).toList();

        // 获取纵轴数据
        List<JSONObject> data = trendDataList.stream().map(JSONObject::new).toList();
        List<BigDecimal> longitudinalAxis = getLongitudinalAxis(horizontalAxis, data, dateType, 8);

        return OverviewCoreTrendVO.builder()
                .horizontalAxis(horizontalAxis)
                .longitudinalAxis(longitudinalAxis)
                .build();
    }

    private static String normalizeDateTime(String date, boolean isStart) {
        if (date.length() == 10) {
            return isStart ? date + " 00:00:00" : date + " 23:59:59";
        }
        return date;
    }

    @Override
    public OverviewCoreSummaryVO coreSummary(Integer summaryType) {
        Integer shopId = HeaderUtils.getShopId();
        boolean isToday = summaryType != null && summaryType == 0;
        long startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long endOfToday = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1;

        // ========= 1. 新增分销员数（始终按分销员注册时间口径；今日则限定当天） =========
        QueryWrapper<Salesman> newSalesmanWrapper = new QueryWrapper<>();
        if (isToday) {
            newSalesmanWrapper.ge("add_time", startOfToday).le("add_time", endOfToday);
        }
        if (shopId != null && shopId > 0) {
            newSalesmanWrapper.eq("shop_id", shopId);
        }
        long newSalesmanCount = salesmanService.count(newSalesmanWrapper);

        BigDecimal salesmanAmount;
        long customNum = 0;
        BigDecimal salesmanCommission;

        if (isToday) {
            // ========= 2. 今日口径：销售额/客户数/佣金按分销订单自身时间统计，不受分销员注册时间限制 =========
            // 店铺范围内的分销员（shopSalesmanIds == null 表示平台/全部，不按分销员维度限制）
            List<Integer> shopSalesmanIds = null;
            if (shopId != null && shopId > 0) {
                shopSalesmanIds = salesmanService.lambdaQuery()
                        .eq(Salesman::getShopId, shopId)
                        .list()
                        .stream()
                        .map(Salesman::getSalesmanId)
                        .collect(Collectors.toList());
            }

            if (shopSalesmanIds != null && shopSalesmanIds.isEmpty()) {
                // 当前店铺没有任何分销员，相关指标均为 0
                salesmanAmount = BigDecimal.ZERO;
                salesmanCommission = BigDecimal.ZERO;
            } else {
                // 今日销售额：今天生成的分销订单 order_amount 之和
                QueryWrapper<SalesmanOrder> amountWrapper = new QueryWrapper<>();
                amountWrapper.ge("add_time", startOfToday).le("add_time", endOfToday);
                if (shopSalesmanIds != null) {
                    amountWrapper.in("salesman_id", shopSalesmanIds);
                }
                salesmanAmount = Optional.ofNullable(
                        salesmanOrderService.getObj(
                                amountWrapper.select("SUM(order_amount)"),
                                obj -> (BigDecimal) obj
                        )
                ).orElse(BigDecimal.ZERO);

                // 今日成交客户数：今天生成的分销订单所关联订单的去重客户
                QueryWrapper<SalesmanOrder> custOrderWrapper = new QueryWrapper<>();
                custOrderWrapper.ge("add_time", startOfToday).le("add_time", endOfToday);
                if (shopSalesmanIds != null) {
                    custOrderWrapper.in("salesman_id", shopSalesmanIds);
                }
                List<SalesmanOrder> todaySalesmanOrders = salesmanOrderService.list(custOrderWrapper);
                if (!todaySalesmanOrders.isEmpty()) {
                    List<Order> list = orderService.list(
                            new LambdaQueryWrapper<Order>()
                                    .in(Order::getOrderId, todaySalesmanOrders.stream().map(SalesmanOrder::getOrderId).collect(Collectors.toSet()))
                                    .groupBy(Order::getUserId));
                    customNum = list.size();
                }

                // 今日支出佣金：今天结算的已结算佣金 amount 之和
                QueryWrapper<SalesmanOrder> commissionWrapper = new QueryWrapper<>();
                commissionWrapper.eq("status", 1)
                        .ge("settlement_time", startOfToday)
                        .le("settlement_time", endOfToday);
                if (shopSalesmanIds != null) {
                    commissionWrapper.in("salesman_id", shopSalesmanIds);
                }
                salesmanCommission = Optional.ofNullable(
                        salesmanOrderService.getObj(
                                commissionWrapper.select("SUM(amount)"),
                                obj -> (BigDecimal) obj
                        )
                ).orElse(BigDecimal.ZERO);
            }
        } else {
            // ========= 3. 累计口径：保持原有行为不变 =========
            QueryWrapper<Salesman> salesmanWrapper = new QueryWrapper<>();
            if (shopId != null && shopId > 0) {
                salesmanWrapper.eq("shop_id", shopId);
            }
            List<Salesman> salesmanList = salesmanService.list(salesmanWrapper);
            List<Integer> salesmanIds = salesmanList.stream()
                    .map(Salesman::getSalesmanId)
                    .collect(Collectors.toList());
            // 销售额（SUM(sale_amount)）
            salesmanAmount = Optional.ofNullable(
                    salesmanService.getObj(
                            salesmanWrapper.select("SUM(sale_amount)"),
                            obj -> (BigDecimal) obj
                    )
            ).orElse(BigDecimal.ZERO);
            // 成交客户数
            if (!salesmanIds.isEmpty()) {
                List<SalesmanOrder> salesmanOrders = salesmanOrderService.list(new LambdaQueryWrapper<SalesmanOrder>()
                        .in(SalesmanOrder::getSalesmanId, salesmanIds));

                if (!salesmanOrders.isEmpty()) {
                    List<Order> list = orderService.list(
                            new LambdaQueryWrapper<Order>()
                                    .in(Order::getOrderId, salesmanOrders.stream().map(SalesmanOrder::getOrderId).collect(Collectors.toSet()))
                                    .groupBy(Order::getUserId));
                    customNum = list.size();
                }
            }
            // 已结算佣金
            QueryWrapper<SalesmanOrder> commissionWrapper = new QueryWrapper<>();
            commissionWrapper.eq("status", 1);
            if (!salesmanIds.isEmpty()) {
                commissionWrapper.in("salesman_id", salesmanIds);
            } else {
                commissionWrapper.eq("salesman_id", -1);
            }
            salesmanCommission = Optional.ofNullable(
                    salesmanOrderService.getObj(
                            commissionWrapper.select("SUM(amount)"),
                            obj -> (BigDecimal) obj
                    )
            ).orElse(BigDecimal.ZERO);
        }

        // ========= 4. 封装结果 =========
        return OverviewCoreSummaryVO.builder()
                .newSalesmanCount(newSalesmanCount)
                .salesmanAmount(salesmanAmount)
                .customNum(customNum)
                .salesmanCommission(salesmanCommission)
                .build();
    }

    @Override
    public List<String> getHorizontalAxis(int dateType, String startDate, String endDate) {
        List<String> horizontalAxis = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            switch (dateType) {
                case 1:
                    // 年  -- 显示月份
                    horizontalAxis = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
                    break;
                case 2:
                    // 月 -- 显示日期
                    Date start = dateFormat.parse(startDate);
                    calendar.setTime(start);
                    int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    for (int i = 1; i <= daysInMonth; i++) {
                        horizontalAxis.add(String.format("%02d", i));
                    }
                    break;
                case 3:
                    // 日 -- 显示24小时
                    for (int i = 1; i <= 24; i++) {
                        horizontalAxis.add(String.format("%02d", i));
                    }
                    break;
                default:
                    // 自定义 -- 时间数组
                    Date current = dateFormat.parse(startDate);
                    Date end = dateFormat.parse(endDate);

                    while (!current.after(end)) {
                        horizontalAxis.add(dateFormat.format(current));
                        calendar.setTime(current);
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        current = calendar.getTime();
                    }
                    break;
            }
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }

        return horizontalAxis;
    }

    @Override
    public List<BigDecimal> getLongitudinalAxis(List<String> horizontalAxis, List<JSONObject> data, int dateType, int fromType) {
        LinkedHashMap<String, BigDecimal> rangeData = IntStream.range(0, horizontalAxis.size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> (dateType >= 1 && dateType <= 3) ? String.format("%02d", i + 1) : horizontalAxis.get(i),
                        i -> BigDecimal.ZERO,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        Map<Integer, SimpleDateFormat> dateFormatMap = new HashMap<>() {
            {
                put(1, new SimpleDateFormat("MM"));
                put(2, new SimpleDateFormat("dd"));
                put(3, new SimpleDateFormat("HH"));
            }
        };
        SimpleDateFormat dateFormat = dateFormatMap.getOrDefault(dateType, new SimpleDateFormat("yyyy-MM-dd"));

        for (JSONObject item : data) {
            Date regTime = null;
            BigDecimal valueToAdd = BigDecimal.ZERO;
            String range;

            switch (fromType) {
                case 1:
                    // 新增会员统计
                    regTime = new Date(Long.parseLong(item.getString("regTime")) * 1000L);
                    valueToAdd = BigDecimal.ONE;
                    break;
                case 2:
                    // 访问统计/访客统计
                    Object visitPeriod = item.get("period");
                    regTime = (visitPeriod instanceof Date) ? (Date) visitPeriod : DateUtil.parse(visitPeriod.toString());
                    valueToAdd = getBigDecimalValue(item.get("accessCount"));
                    break;
                case 3:
                    // 订单统计
                    Object orderPeriod = item.get("period");
                    regTime = (orderPeriod instanceof Date) ? (Date) orderPeriod : DateUtil.parse(orderPeriod.toString());
                    valueToAdd = getBigDecimalValue(item.get("orderCount"));
                    break;
                case 4:
                    // 订单金额统计
                    regTime = DateUtil.date(Long.parseLong(item.getString("payTime")) * 1000);
                    valueToAdd = getBigDecimalValue(item.get("totalAmount"));
                    break;
                case 5:
                    // 订单数统计
                    regTime = DateUtil.date(Long.parseLong(item.getString("payTime")) * 1000);
                    valueToAdd = BigDecimal.ONE;
                    break;
                case 6:
                    // 退款金额统计
                    regTime = DateUtil.date(Long.parseLong(item.getString("addTime")) * 1000);
                    valueToAdd = getBigDecimalValue(item.get("refundAmount"));
                    break;
                case 7:
                    // 面板 - 订单金额
                    regTime = DateUtil.parse(item.getString("period"));
                    valueToAdd = getBigDecimalValue(item.get("orderAmount"));
                    break;
                case 8:
                    // 分销员销售额趋势
                    regTime = DateUtil.date(Long.parseLong(item.getString("addTime")) * 1000);
                    valueToAdd = getBigDecimalValue(item.get("saleAmount"));
                    break;
                default:
                    continue;
            }

            if (regTime != null) {
                range = dateFormat.format(regTime);
                rangeData.merge(range, valueToAdd, BigDecimal::add);
            }
        }

        return new ArrayList<>(rangeData.values());
    }

    private BigDecimal getBigDecimalValue(Object value) {
        try {
            return new BigDecimal(value != null ? value.toString() : "0");
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }


}
