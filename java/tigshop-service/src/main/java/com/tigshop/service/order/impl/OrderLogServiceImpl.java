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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderLogCreateDTO;
import com.tigshop.bean.dto.order.OrderLogListDTO;
import com.tigshop.bean.dto.order.OrderLogUpdateDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderLog;
import com.tigshop.bean.model.order.OrderSpitLog;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.order.OrderLogVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.order.OrderLogMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.OrderLogService;
import com.tigshop.service.order.OrderSpitLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * 订单商品服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class OrderLogServiceImpl extends BaseServiceImpl<OrderLogMapper, OrderLog> implements OrderLogService {

    private static final String AUTO_APPROVED_AFTERSALES_LOG = "售后自动审核通过";

    @Resource
    OrderSpitLogService orderSpitLogService;

    @Resource
    AdminUserService adminUserService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OrderMapper orderMapper;

    @Override
    public Page<OrderLogVO> list(OrderLogListDTO listDTO) {
        // 分页
        Page<OrderLog> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<OrderLog> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        Integer orderId = listDTO.getOrderId();
        if (orderId != null) {
            List<Integer> orderIdArr = new ArrayList<>();
            Map<Integer, Long> parentSplitTimeMap = new LinkedHashMap<>();
            this.getParentOrderIds(orderId, orderIdArr, parentSplitTimeMap);
            if (CollUtil.isNotEmpty(orderIdArr)) {
                if (parentSplitTimeMap.isEmpty()) {
                    queryWrapper.in(OrderLog::getOrderId, orderIdArr);
                } else {
                    queryWrapper.and(wrapper -> {
                        wrapper.eq(OrderLog::getOrderId, orderId);
                        for (Map.Entry<Integer, Long> entry : parentSplitTimeMap.entrySet()) {
                            wrapper.or(w -> w
                                    .eq(OrderLog::getOrderId, entry.getKey())
                                    .lt(OrderLog::getLogTime, entry.getValue())
                            );
                        }
                    });
                }
            }
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(OrderLog::getDescription, keyword);
        }
        // 执行查询
        Page<OrderLog> orderLogPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<OrderLog> orderLogPageRecords = orderLogPage.getRecords();
        // 转换为VO
        List<OrderLogVO> orderLogVOList = orderLogPageRecords.stream()
                .map(orderLog -> {
                    OrderLogVO orderLogVO = new OrderLogVO();
                    BeanUtils.copyProperties(orderLog, orderLogVO);

                    DateTime logDate = DateUtil.date(orderLog.getLogTime() * 1000);
                    orderLogVO.setLogTime(DateUtil.format(logDate, DATE_FORMAT));

                    if (orderLog.getAdminId() != null && orderLog.getAdminId() > 0) {
                        AdminUser adminUser = adminUserService.getById(orderLog.getAdminId());
                        if (adminUser != null) {
                            orderLogVO.setOperator(adminUser.getUsername());
                        }
                    } else {
                        User user = userMapper.selectById(orderLog.getUserId());
                        if (user != null) {
                            orderLogVO.setOperator(user.getUsername());
                        }
                    }
                    if (StringUtils.isEmpty(orderLogVO.getOperator()) && AUTO_APPROVED_AFTERSALES_LOG.equals(orderLog.getDescription())) {
                        AdminUser systemAdminUser = this.getSystemAdminUser();
                        if (systemAdminUser != null) {
                            orderLogVO.setOperator(systemAdminUser.getUsername());
                        }
                    }
                    return orderLogVO;
                }).toList();
        return PageUtil.convertPage(orderLogPage, orderLogVOList);
    }

    private void getParentOrderIds(Integer orderId, List<Integer> orderIdArr, Map<Integer, Long> parentSplitTimeMap) {
        orderIdArr.add(orderId);
        OrderSpitLog orderSpitLog = orderSpitLogService.getByOrderId(orderId);
        if (orderSpitLog != null && orderSpitLog.getParentOrderId() != null && !orderIdArr.contains(orderSpitLog.getParentOrderId())) {
            parentSplitTimeMap.put(orderSpitLog.getParentOrderId(), orderSpitLog.getSplitTime());
            this.getParentOrderIds(orderSpitLog.getParentOrderId(), orderIdArr, parentSplitTimeMap);
        }
    }

    @Override
    public OrderLogVO detail(Integer id) {
        if (id != null) {
            OrderLog orderLog = this.getById(id);
            OrderLogVO orderLogVO = new OrderLogVO();
            BeanUtils.copyProperties(orderLog, orderLogVO);
            return orderLogVO;
        }
        return new OrderLogVO();
    }

    @Override
    public boolean create(OrderLogCreateDTO createDTO) {
        if (createDTO != null) {
            OrderLog orderLog = new OrderLog();
            BeanUtils.copyProperties(createDTO, orderLog);
            orderLog.setLogTime(StringUtils.getCurrentTime());
            if (createDTO.getAdminId() != null || createDTO.getUserId() != null) {
                orderLog.setAdminId(createDTO.getAdminId());
                orderLog.setUserId(createDTO.getUserId());
            } else if (SecurityUtils.isAdminUser()) {
                orderLog.setAdminId(SecurityUtils.getCurrentAdminId());
            } else {
                orderLog.setUserId(SecurityUtils.getCurrentUserId());
            }
            boolean saved = this.save(orderLog);
            // 提交时拆单的父单：支付前父单产生的操作日志同步写入各未取消子单
            syncParentLogToChildren(orderLog);
            return saved;
        }
        return false;
    }

    /**
     * 提交时拆单场景：父订单（isStoreSplited=1 且无父订单）在支付成功(含)之前产生的操作日志，
     * 同步写一份到每个未取消子订单，使子订单展示完整历史。
     * 支付之后操作直接落在子订单上，父订单失效不再展示，无需同步。
     */
    private void syncParentLogToChildren(OrderLog parentLog) {
        if (parentLog == null || parentLog.getOrderId() == null) {
            return;
        }
        // 拆分标记类日志只属于父单，对子单是噪音，不同步
        String desc = parentLog.getDescription();
        if (desc != null && desc.contains("订单商品来自不同店铺") && desc.contains("拆分")) {
            return;
        }
        Order order = orderMapper.selectById(parentLog.getOrderId());
        if (order == null) {
            return;
        }
        boolean isSplitParent = order.getIsStoreSplited() != null && order.getIsStoreSplited() == 1
                && (order.getParentOrderId() == null || order.getParentOrderId() == 0);
        if (!isSplitParent) {
            return;
        }
        List<Order> children = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getParentOrderId, order.getOrderId())
                .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode()));
        if (CollUtil.isEmpty(children)) {
            return;
        }
        for (Order child : children) {
            OrderLog childLog = new OrderLog();
            childLog.setOrderId(child.getOrderId());
            childLog.setOrderSn(child.getOrderSn());
            childLog.setShopId(child.getShopId());
            childLog.setDescription(parentLog.getDescription());
            childLog.setLogTime(parentLog.getLogTime());
            childLog.setAdminId(parentLog.getAdminId());
            childLog.setUserId(parentLog.getUserId());
            this.save(childLog);
        }
    }

    @Override
    public void quickCreate(String description, Integer orderId, String orderSn) {
        Order order = orderMapper.selectById(orderId);
        OrderLogCreateDTO orderLogCreateDTO = new OrderLogCreateDTO();
        orderLogCreateDTO.setOrderId(orderId);
        orderLogCreateDTO.setDescription(description);
        orderLogCreateDTO.setOrderSn(orderSn);
        orderLogCreateDTO.setShopId(order.getShopId());
        this.create(orderLogCreateDTO);
    }

    @Override
    public void quickCreateBySystemAdmin(String description, Integer orderId, String orderSn) {
        Order order = orderMapper.selectById(orderId);
        OrderLogCreateDTO orderLogCreateDTO = new OrderLogCreateDTO();
        orderLogCreateDTO.setOrderId(orderId);
        orderLogCreateDTO.setDescription(description);
        orderLogCreateDTO.setOrderSn(orderSn);
        orderLogCreateDTO.setShopId(order.getShopId());
        AdminUser systemAdminUser = this.getSystemAdminUser();
        if (systemAdminUser != null) {
            orderLogCreateDTO.setAdminId(systemAdminUser.getAdminId());
        }
        this.create(orderLogCreateDTO);
    }

    private AdminUser getSystemAdminUser() {
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getAdminType, AdminTypeEnum.ADMIN.getCode())
                .orderByAsc(AdminUser::getAdminId)
                .last("limit 1");
        return adminUserService.getOne(queryWrapper, false);
    }

    @Override
    public boolean update(OrderLogUpdateDTO updateDTO) {
        if (updateDTO != null) {
            OrderLog orderLog = new OrderLog();
            BeanUtils.copyProperties(updateDTO, orderLog);
            return this.updateById(orderLog);
        }
        return false;
    }
}
