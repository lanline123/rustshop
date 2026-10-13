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

package com.tigshop.service.finance.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.enums.finance.OrderInvoiceStatusEnum;
import com.tigshop.bean.enums.finance.OrderInvoiceTitle;
import com.tigshop.bean.enums.finance.OrderInvoiceType;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.OrderInvoice;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.finance.orderinvoice.OrderInvoiceEditParam;
import com.tigshop.bean.param.finance.orderinvoice.OrderInvoiceSaveParam;
import com.tigshop.bean.query.finance.orderinvoice.OrderInvoicePageQuery;
import com.tigshop.bean.vo.finance.OrderInvoiceDetailVO;
import com.tigshop.bean.vo.finance.OrderInvoiceVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.finance.OrderInvoiceMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.OrderInvoiceService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.finance.OrderInvoiceConstants.ORDER_INVOICE_ALREADY_APPLIED;
import static com.tigshop.common.constant.finance.OrderInvoiceConstants.ORDER_INVOICE_NOT_EXIST;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 发票申请服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderInvoiceServiceImpl extends BaseServiceImpl<OrderInvoiceMapper, OrderInvoice> implements OrderInvoiceService {

    private final UserMapper userMapper;
    private final OrderService orderService;
    private final AdminMsgService adminMsgService;
    private final MessageService messageService;
    private final ConfigService configService;

    @Override
    public Page<OrderInvoiceVO> list(OrderInvoicePageQuery query) {
        // 排序字段
        Page<OrderInvoice> page = buildSortOrder(query);
        // 执行查询
        Page<OrderInvoiceVO> orderInvoicePage = this.baseMapper.orderInvoicePage(page, query);
        // 获取查询结果
        List<OrderInvoiceVO> orderInvoicePageRecords = orderInvoicePage.getRecords();

        if (CollUtil.isEmpty(orderInvoicePageRecords)) {
            return orderInvoicePage;
        }

        // 获取所有 ID
        List<Integer> userIds = orderInvoicePageRecords.stream()
                .map(OrderInvoiceVO::getUserId)
                .distinct()
                .toList();

        // 调用方法获取会员信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 转换为VO
        orderInvoicePageRecords.forEach(orderInvoice -> {
            User user = userMap.get(orderInvoice.getUserId());
            if (user != null) {
                // 将分类名称设置到对象中
                orderInvoice.setUsername(user.getUsername());
            }

            orderInvoice.setStatusName(OrderInvoiceStatusEnum.getStatusName(orderInvoice.getStatus()));
            orderInvoice.setInvoiceTypeName(OrderInvoiceType.getTypeName(orderInvoice.getInvoiceType()));

            if (StrUtil.isNotBlank(orderInvoice.getAddTime())) {
                DateTime date = DateUtil.date(Long.parseLong(orderInvoice.getAddTime()) * 1000);
                orderInvoice.setAddTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
            }

        });
        return orderInvoicePage;
    }

    @Override
    public OrderInvoiceVO detail(Integer id) {
        OrderInvoice orderInvoice = this.getById(id);
        OrderInvoiceVO orderInvoiceVO = new OrderInvoiceVO();
        BeanUtils.copyProperties(orderInvoice, orderInvoiceVO);

        User user = userMapper.selectById(orderInvoiceVO.getUserId());
        orderInvoiceVO.setUsername(user.getUsername());

        orderInvoiceVO.setInvoiceTypeName(OrderInvoiceType.getTypeName(orderInvoice.getInvoiceType()));
        orderInvoiceVO.setInvoiceAttachment(JSON.parseArray(orderInvoice.getInvoiceAttachment(), Object.class));

        return orderInvoiceVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void create(OrderInvoiceSaveParam createDTO) {
        OrderInvoice orderInvoice = new OrderInvoice();
        BeanUtils.copyProperties(createDTO, orderInvoice);

        this.save(orderInvoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clientCreate(OrderInvoiceSaveParam createDTO) {
        // 服务端兜底校验：发票功能关闭时拒绝申请，防止绕过前端隐藏直接调接口
        if (!"1".equals(configService.getConfigVal(SettingsEnum.CAN_INVOICE))) {
            throw new GlobalException("发票功能已关闭，无法申请开票");
        }
        // 订单信息更新发票状态
        Order order = orderService.getById(createDTO.getId());
        if (order == null) {
            return;
        }
        // 保存发票申请信息
        OrderInvoice orderInvoice = new OrderInvoice();
        BeanUtils.copyProperties(createDTO, orderInvoice);
        orderInvoice.setOrderId(createDTO.getId());
        orderInvoice.setAddTime(StringUtils.getCurrentTime());
        orderInvoice.setUserId(getCurrentUserId());
        orderInvoice.setStatus(0);
        orderInvoice.setAmount(order.getTotalAmount());
        // 发票申请ID置空,由数据库自增生成
        orderInvoice.setId(null);
        this.save(orderInvoice);

        order.setInvoiceData(JsonUtil.toJson(orderInvoice));
        orderService.updateById(order);

        // 获取用户信息
        Integer currentUserId = getCurrentUserId();
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new GlobalException("用户信息异常");
        }

        //发送站内信消息
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.INVOICE_REQUEST.getCatId());
        adminMsgCreateDTO.setTitle("您有新的发票申请,申请金额：" + orderInvoice.getAmount() + "，发票抬头:" + createDTO.getCompanyName());
        adminMsgCreateDTO.setContent("用户【" + user.getUsername() + "】针对订单【" + order.getOrderSn() + "】提交了发票申请");
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", order.getOrderId());
        relatedData.put("orderInvoiceId", orderInvoice.getId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(OrderInvoiceEditParam param) {
        OrderInvoice orderInvoice = new OrderInvoice();
        BeanUtils.copyProperties(param, orderInvoice);

        Order order = orderService.getById(param.getOrderId());
        if (order != null) {
            orderInvoice.setInvoiceAttachment(JSONArray.toJSONString(param.getInvoiceAttachment()));
            orderInvoice.setInvoicingTime(StringUtils.getCurrentTime());

            OrderInvoiceVO orerderInvoiceVO = BeanUtil.copyProperties(orderInvoice, OrderInvoiceVO.class);
            orerderInvoiceVO.setInvoiceAttachment(param.getInvoiceAttachment());
            order.setInvoiceData(JSON.toJSONString(orerderInvoiceVO));
            orderService.updateById(order);

            if (OrderInvoiceStatusEnum.STATUS_PASS.getCode() == param.getStatus()) {
                try {
                    messageService.sendUserMessage(order.getUserId(), order.getOrderId(), MessageTypeEnum.ORDER_INVOICE);
                } catch (Exception ex) {
                    log.error("发票通知 ORDER_INVOICE 发送失败，不影响发票主流程，orderId={}: {}", order.getOrderId(), ex.getMessage(), ex);
                }
            }
        }

        this.updateById(orderInvoice);
    }

    @Override
    public void clientUpdate(OrderInvoiceEditParam updateDTO) {
        Integer userId = getCurrentUserId();
        OrderInvoice orderInvoice1 = this.lambdaQuery()
                .eq(OrderInvoice::getOrderId, updateDTO.getId())
                .eq(OrderInvoice::getUserId, userId)
                .orderByDesc(OrderInvoice::getId)
                .last("limit 1")
                .one();
        Assert.notNull(orderInvoice1, () -> new GlobalException(ORDER_INVOICE_NOT_EXIST));
        Assert.isFalse(orderInvoice1.getStatus() == 1, () -> new GlobalException(ORDER_INVOICE_ALREADY_APPLIED));

        OrderInvoice orderInvoice = new OrderInvoice();
        BeanUtils.copyProperties(updateDTO, orderInvoice);
        orderInvoice.setId(orderInvoice1.getId());
        if (orderInvoice1.getApplyReply() != null && StringUtils.isNotEmpty(orderInvoice1.getApplyReply())) {
            orderInvoice.setApplyReply("");
        }
        orderInvoice.setStatus(OrderInvoiceStatusEnum.STATUS_WAIT.getCode());
        this.updateById(orderInvoice);

        Order order = orderService.getById(updateDTO.getId());
        order.setInvoiceData(JsonUtil.toJson(orderInvoice));
        orderService.updateById(order);

        // 获取用户信息
        User user = userMapper.selectById(userId);

        // 发送站内信消息
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.INVOICE_REQUEST.getCatId());
        adminMsgCreateDTO.setTitle("您有新的发票申请,申请金额：" + updateDTO.getAmount() + "，发票抬头:" + updateDTO.getCompanyName());
        adminMsgCreateDTO.setContent("用户【" + user.getUsername() + "】针对订单【" + order.getOrderSn() + "】提交了发票申请");
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", order.getOrderId());
        relatedData.put("orderInvoiceId", orderInvoice.getId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
    }

    @Override
    public OrderInvoiceDetailVO clientDetail(Integer id) {
        if (id != null) {
            Integer userId = getCurrentUserId();
            LambdaQueryWrapper<OrderInvoice> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderInvoice::getOrderId, id);
            queryWrapper.eq(OrderInvoice::getUserId, userId);
            queryWrapper.orderByDesc(OrderInvoice::getId);
            queryWrapper.last("limit 1");
            OrderInvoice orderInvoice = this.getOne(queryWrapper);
            Order order = orderService.getById(id);
            if (orderInvoice == null) {
                //查询订单是否有发票信息
                if (!Objects.equals(order.getInvoiceData(), "[]")) {
                    OrderInvoice orderInvoice1 = JSON.parseObject(order.getInvoiceData(), OrderInvoice.class);
                    orderInvoice1.setAddTime(StringUtils.getCurrentTime());
                    orderInvoice1.setId(null);
                    this.save(orderInvoice1);
                }
                return null;
            }
            //获取订单信息
            OrderInvoiceDetailVO orderInvoiceDetailVO = new OrderInvoiceDetailVO();
            BeanUtils.copyProperties(orderInvoice, orderInvoiceDetailVO);
            orderInvoiceDetailVO.setAddTime(TigUtils.handelTime(orderInvoice.getAddTime()));
            orderInvoiceDetailVO.setOrderSn(order.getOrderSn());
            orderInvoiceDetailVO.setTotalAmount(order.getTotalAmount());
            orderInvoiceDetailVO.setRegionNames(order.getRegionNames() != null && JSONUtil.isTypeJSON(order.getRegionNames()) ?
                    JSONUtil.toList(order.getRegionNames(), String.class) : null);
            orderInvoiceDetailVO.setAddress(order.getAddress());
            orderInvoiceDetailVO.setUserAddress(order.getAddress());
            orderInvoiceDetailVO.setStatusName(OrderInvoiceStatusEnum.getStatusName(orderInvoice.getStatus()));
            orderInvoiceDetailVO.setInvoiceTypeName(OrderInvoiceType.getTypeName(orderInvoice.getInvoiceType()));
            if (StrUtil.isNotBlank(orderInvoice.getInvoiceAttachment())) {
                orderInvoiceDetailVO.setInvoiceAttachment(JSON.parseArray(orderInvoice.getInvoiceAttachment(), Object.class));
            }
            try {
                orderInvoiceDetailVO.setTitleTypeName(OrderInvoiceTitle.getTitleName(orderInvoice.getTitleType()));
            } catch (Exception e) {
                // ignore
            }
            return orderInvoiceDetailVO;
        }
        return null;
    }
}
