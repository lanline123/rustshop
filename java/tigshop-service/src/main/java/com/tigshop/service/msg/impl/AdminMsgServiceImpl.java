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

package com.tigshop.service.msg.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.msg.AdminMsgListDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.model.msg.AdminMsg;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.utils.VendorOrderPriceCalculator;
import com.tigshop.bean.vo.msg.AdminMsgBroadcastVO;
import com.tigshop.bean.vo.msg.AdminMsgTypeListVO;
import com.tigshop.bean.vo.msg.AdminMsgVO;
import com.tigshop.bean.vo.msg.HomeMsgSummaryVO;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.msg.AdminMsgMapper;
import com.tigshop.mapper.order.AftersalesItemMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.msg.AdminMsgService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员消息服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class AdminMsgServiceImpl extends BaseServiceImpl<AdminMsgMapper, AdminMsg> implements AdminMsgService {

    /** 订单类消息类型（详情映射到订单，与消息中心/前端 DetailBtn 同源口径） */
    private static final List<Integer> ORDER_MSG_TYPES = Arrays.asList(11, 12, 13, 15, 16, 31, 54);
    /** 售后类消息类型 */
    private static final int AFTERSALE_MSG_TYPE = 32;

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private AftersalesMapper aftersalesMapper;
    @Resource
    private AftersalesItemMapper aftersalesItemMapper;

    @Override
    public Page<AdminMsgVO> list(AdminMsgListDTO listDTO) {
        // 分页
        Page<AdminMsg> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<AdminMsg> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer msgType = listDTO.getMsgType();
        if (msgType != null) {
            queryWrapper.eq(AdminMsg::getMsgType, msgType);
        }

        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(AdminMsg::getVendorId, getVendorId());
        } else {
            queryWrapper.eq(AdminMsg::getShopId, getShopId());
            queryWrapper.eq(AdminMsg::getVendorId, 0);
        }
        // 平台端（自营 shop_id=0）只展示会播报的自营消息，与语音播报口径对齐：
        // 排除第三方店铺订单写入平台的副本（is_broadcast=0），使内容列表仅显示自营店铺消息
        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.ADMIN) {
            queryWrapper.eq(AdminMsg::getIsBroadcast, 1);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(AdminMsg::getTitle, keyword);
        }
        // 执行查询
        Page<AdminMsg> adminMsgPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<AdminMsg> adminMsgPageRecords = adminMsgPage.getRecords();

        // 转换为VO
        List<AdminMsgVO> adminMsgVOList = adminMsgPageRecords.stream()
                .map(adminMsg -> {
                    AdminMsgVO adminMsgVO = new AdminMsgVO();
                    BeanUtils.copyProperties(adminMsg, adminMsgVO);
                    AdminMsgTypeEnum.getByCatId(adminMsg.getMsgType())
                            .ifPresent(type -> adminMsgVO.setMsgTypeName(type.getCatName()));
                    //relatedData 解析json
                    JSONObject relatedData = JsonUtil.fromJson(adminMsg.getRelatedData(), JSONObject.class);
                    applyDefaultRoute(adminMsg.getMsgType(), relatedData);
                    adminMsgVO.setRelatedData(relatedData);
                    adminMsgVO.setRoute(relatedData == null ? null : relatedData.getStr("route"));
                    adminMsgVO.setSendTime(DateUtil.format(DateUtil.date(adminMsg.getSendTime() * 1000), "yyyy-MM-dd HH:mm:ss"));

                    return adminMsgVO;
                }).toList();
        return PageUtil.convertPage(adminMsgPage, adminMsgVOList);
    }

    @Override
    public Page<AdminMsgBroadcastVO> broadcastList(AdminMsgListDTO listDTO) {
        // 复用消息中心 list 的身份/店铺/供应商/是否播报过滤口径与分页，保证可见性完全一致
        Page<AdminMsgVO> basePage = this.list(listDTO);
        List<AdminMsgVO> baseRecords = basePage.getRecords();

        List<AdminMsgBroadcastVO> voList = new ArrayList<>();
        Set<Integer> orderIds = new HashSet<>();
        Set<Integer> aftersaleIds = new HashSet<>();

        // 第一遍：拷贝基础字段 + 收集需要聚合的订单/售后 ID
        for (AdminMsgVO base : baseRecords) {
            AdminMsgBroadcastVO vo = new AdminMsgBroadcastVO();
            BeanUtils.copyProperties(base, vo);
            voList.add(vo);

            Integer msgType = base.getMsgType();
            if (msgType == null) {
                continue;
            }
            JSONObject related = base.getRelatedData();
            if (ORDER_MSG_TYPES.contains(msgType)) {
                Integer orderId = resolveOrderId(related, base.getOrderId());
                if (orderId != null && orderId > 0) {
                    orderIds.add(orderId);
                }
            } else if (msgType == AFTERSALE_MSG_TYPE) {
                Integer aftersaleId = related == null ? null : related.getInt("aftersaleId");
                if (aftersaleId != null && aftersaleId > 0) {
                    aftersaleIds.add(aftersaleId);
                }
            }
        }

        // 批量查询：订单 + 订单商品
        Map<Integer, Order> orderMap = new HashMap<>();
        Map<Integer, List<OrderItem>> orderItemsByOrder = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<Order> orders = orderMapper.selectBatchIds(orderIds);
            for (Order order : orders) {
                orderMap.put(order.getOrderId(), order);
            }
            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
            orderItemsByOrder = orderItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));
        }

        // 批量查询：售后 + 售后商品（售后商品的商品信息来自其关联的订单商品）
        Map<Integer, Aftersales> aftersalesMap = new HashMap<>();
        Map<Integer, List<AftersalesItem>> aftersalesItemsByAftersale = new HashMap<>();
        Map<Integer, OrderItem> orderItemByItemId = new HashMap<>();
        if (!aftersaleIds.isEmpty()) {
            List<Aftersales> aftersalesList = aftersalesMapper.selectBatchIds(aftersaleIds);
            for (Aftersales aftersales : aftersalesList) {
                aftersalesMap.put(aftersales.getAftersaleId(), aftersales);
            }
            List<AftersalesItem> aftersalesItems = aftersalesItemMapper.selectList(
                    new LambdaQueryWrapper<AftersalesItem>().in(AftersalesItem::getAftersaleId, aftersaleIds));
            aftersalesItemsByAftersale = aftersalesItems.stream()
                    .collect(Collectors.groupingBy(AftersalesItem::getAftersaleId));
            Set<Integer> orderItemIds = aftersalesItems.stream()
                    .map(AftersalesItem::getOrderItemId)
                    .filter(id -> id != null && id > 0)
                    .collect(Collectors.toSet());
            if (!orderItemIds.isEmpty()) {
                List<OrderItem> relatedItems = orderItemMapper.selectBatchIds(orderItemIds);
                for (OrderItem item : relatedItems) {
                    orderItemByItemId.put(item.getItemId(), item);
                }
            }
        }

        // 第二遍：按类型回填商品摘要与金额
        for (AdminMsgBroadcastVO vo : voList) {
            Integer msgType = vo.getMsgType();
            if (msgType == null) {
                continue;
            }
            JSONObject related = vo.getRelatedData();
            if (ORDER_MSG_TYPES.contains(msgType)) {
                Integer orderId = resolveOrderId(related, vo.getOrderId());
                if (orderId == null) {
                    continue;
                }
                Order order = orderMap.get(orderId);
                if (order != null) {
                    vo.setTotalAmount(order.getTotalAmount());
                    vo.setShippingFee(order.getShippingFee());
                    vo.setShippingTypeId(order.getShippingTypeId());
                }
                List<OrderItem> items = orderItemsByOrder.get(orderId);
                if (items != null && !items.isEmpty()) {
                    vo.setProducts(items.stream().map(item -> {
                        AdminMsgBroadcastVO.BroadcastProductVO p = new AdminMsgBroadcastVO.BroadcastProductVO();
                        p.setPicThumb(item.getPicThumb());
                        p.setProductName(item.getProductName());
                        p.setPrice(item.getPrice());
                        p.setQuantity(item.getQuantity());
                        p.setSupplyPrice(item.getVendorProductSupplyPrice());
                        p.setSupplySubtotal(VendorOrderPriceCalculator.calculateSupplySubtotal(
                                item.getPrice(), item.getVendorProductSupplyPrice(), item.getQuantity()));
                        return p;
                    }).collect(Collectors.toList()));
                    vo.setSupplyTotalAmount(vo.getProducts().stream()
                            .map(AdminMsgBroadcastVO.BroadcastProductVO::getSupplySubtotal)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                }
            } else if (msgType == AFTERSALE_MSG_TYPE) {
                Integer aftersaleId = related == null ? null : related.getInt("aftersaleId");
                if (aftersaleId == null) {
                    continue;
                }
                Aftersales aftersales = aftersalesMap.get(aftersaleId);
                if (aftersales != null) {
                    vo.setRefundAmount(aftersales.getRefundAmount());
                }
                List<AftersalesItem> items = aftersalesItemsByAftersale.get(aftersaleId);
                if (items != null && !items.isEmpty()) {
                    vo.setProducts(items.stream().map(item -> {
                        AdminMsgBroadcastVO.BroadcastProductVO p = new AdminMsgBroadcastVO.BroadcastProductVO();
                        OrderItem orderItem = orderItemByItemId.get(item.getOrderItemId());
                        if (orderItem != null) {
                            p.setPicThumb(orderItem.getPicThumb());
                            p.setProductName(orderItem.getProductName());
                            p.setPrice(orderItem.getPrice());
                        }
                        p.setQuantity(item.getNumber());
                        return p;
                    }).collect(Collectors.toList()));
                }
            }
        }

        return PageUtil.convertPage(basePage, voList);
    }

    /**
     * 解析订单 ID：优先取 relatedData.orderId（消息创建时写入），回退到消息表冗余的 orderId 字段。
     */
    private Integer resolveOrderId(JSONObject related, Integer fallbackOrderId) {
        Integer orderId = related == null ? null : related.getInt("orderId");
        if (orderId == null && fallbackOrderId != null && fallbackOrderId > 0) {
            orderId = fallbackOrderId;
        }
        return orderId;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createMessage(AdminMsgCreateDTO createDTO) {
        if (createDTO != null) {
            AdminMsg adminMsg = new AdminMsg();
            BeanUtils.copyProperties(createDTO, adminMsg);
            adminMsg.setSendTime(DateUtil.currentSeconds());
            adminMsg.setRelatedData(JsonUtil.toJson(createDTO.getRelatedData()));
            // 不传时默认参与播报，避免历史调用方因新增字段被静默改为不播报
            if (adminMsg.getIsBroadcast() == null) {
                adminMsg.setIsBroadcast(1);
            }
            this.save(adminMsg);
        }
    }

    @Override
    public boolean setReaded(Integer msgId) {
        if (msgId == null) {
            return false;
        }
        LambdaUpdateWrapper<AdminMsg> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AdminMsg::getMsgId, msgId);
        updateWrapper.set(AdminMsg::getIsReaded, 1);
        return this.update(updateWrapper);
    }

    @Override
    public boolean setAllReaded() {
        LambdaUpdateWrapper<AdminMsg> updateWrapper = new LambdaUpdateWrapper<>();
        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
            updateWrapper.eq(AdminMsg::getVendorId, getVendorId());
        } else {
            updateWrapper.eq(AdminMsg::getShopId, getShopId());
            updateWrapper.eq(AdminMsg::getVendorId, 0);
        }
        updateWrapper.set(AdminMsg::getIsReaded, 1);
        return this.update(updateWrapper);
    }

    @Override
    public Long getUnreadAdminMsgCount(String adminType, Integer shopId, Integer vendorId) {
        LambdaQueryWrapper<AdminMsg> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.fromCode(adminType) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(AdminMsg::getVendorId, vendorId);
        } else {
            queryWrapper.eq(AdminMsg::getShopId, shopId);
            queryWrapper.eq(AdminMsg::getVendorId, 0);
        }
        // 平台端未读红点与内容列表口径对齐：只统计会播报的自营消息，排除第三方店铺订单的平台副本
        if (AdminTypeEnum.fromCode(adminType) == AdminTypeEnum.ADMIN) {
            queryWrapper.eq(AdminMsg::getIsBroadcast, 1);
        }
        queryWrapper.eq(AdminMsg::getIsReaded, 0);
        return this.count(queryWrapper);
    }

    @Override
    public Map<Integer, Long> getNewMsgCountByType(String adminType, Integer shopId, Integer vendorId, Long startTime, List<Integer> msgTypes) {
        Map<Integer, Long> result = new HashMap<>();
        if (msgTypes == null || msgTypes.isEmpty()) {
            return result;
        }
        boolean isVendor = AdminTypeEnum.fromCode(adminType) == AdminTypeEnum.VENDOR;
        for (Integer msgType : msgTypes) {
            if (msgType == null) {
                continue;
            }
            LambdaQueryWrapper<AdminMsg> queryWrapper = new LambdaQueryWrapper<>();
            if (isVendor) {
                queryWrapper.eq(AdminMsg::getVendorId, vendorId);
            } else {
                queryWrapper.eq(AdminMsg::getShopId, shopId);
                queryWrapper.eq(AdminMsg::getVendorId, 0);
            }
            queryWrapper.eq(AdminMsg::getMsgType, msgType);
            // 仅统计需要播报的消息：自动审核售后等场景写入 is_broadcast=0，进消息中心但不触发语音播报
            queryWrapper.eq(AdminMsg::getIsBroadcast, 1);
            // 自 startTime（= 前端 lastOpenTime）起新增的消息；固定 startTime 下计数单调递增，满足增量播报判断
            if (startTime != null && startTime > 0) {
                queryWrapper.ge(AdminMsg::getSendTime, startTime);
            }
            result.put(msgType, this.count(queryWrapper));
        }
        return result;
    }

    @Override
    public List<AdminMsgTypeListVO> getAdminMsgTypeList() {
        // 获取所有未读消息列表
        LambdaQueryWrapper<AdminMsg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminMsg::getIsReaded, 0);
        queryWrapper.eq(AdminMsg::getAdminId, 0);
        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(AdminMsg::getVendorId, getVendorId());
        } else {
            queryWrapper.eq(AdminMsg::getShopId, getShopId());
            queryWrapper.eq(AdminMsg::getVendorId, 0);
        }
        // 平台端未读分类统计与内容列表口径对齐：只统计会播报的自营消息，排除第三方店铺订单的平台副本
        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.ADMIN) {
            queryWrapper.eq(AdminMsg::getIsBroadcast, 1);
        }
        queryWrapper.select(AdminMsg::getMsgType);
        List<AdminMsg> adminMsgList = this.list(queryWrapper);

        // 创建一个列表来存储树形结构
        List<AdminMsgTypeListVO> tree = new ArrayList<>();

        // 存储子分类的映射
        Map<Integer, List<AdminMsgTypeListVO.AdminMsgTypeListChildVO>> childMap = new HashMap<>();

        // 存储未读消息数量
        Map<Integer, Integer> unreadCountMap = new HashMap<>();

        // 遍历 AdminMsgType 枚举
        for (AdminMsgTypeEnum type : AdminMsgTypeEnum.values()) {
            if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
                if (!type.isVendorShow()) {
                    continue;
                }
            } else {
                if (!type.isShopShow()) {
                    continue;
                }
            }
            if (type.getParentId() == 0) {
                // 创建父分类的 VO 对象
                AdminMsgTypeListVO parentVO = new AdminMsgTypeListVO();
                parentVO.setCatId(type.getCatId());
                parentVO.setCatName(type.getCatName());
                parentVO.setUnreadCount(0);
                // 初始化子分类列表
                parentVO.setChild(new ArrayList<>());
                tree.add(parentVO);
            } else {
                // 如果是子分类，存储到 childMap
                AdminMsgTypeListVO.AdminMsgTypeListChildVO childVO = new AdminMsgTypeListVO.AdminMsgTypeListChildVO();
                // 这里使用 catId 作为 msgType
                childVO.setMsgType(type.getCatId());
                childVO.setName(type.getCatName());
                // 初始化子分类未读数量
                childVO.setUnreadCount(0);
                childMap.computeIfAbsent(type.getParentId(), k -> new ArrayList<>()).add(childVO);
            }
        }

        // 统计未读消息数量
        for (AdminMsg adminMsg : adminMsgList) {
            Integer msgType = adminMsg.getMsgType();
            //未读的增加
            unreadCountMap.put(msgType, unreadCountMap.getOrDefault(msgType, 0) + 1);
        }

        // 为每个父分类添加子分类，并更新未读数量
        for (AdminMsgTypeListVO parent : tree) {
            Integer parentId = parent.getCatId();
            if (childMap.containsKey(parentId)) {
                List<AdminMsgTypeListVO.AdminMsgTypeListChildVO> children = childMap.get(parentId);
                parent.setChild(children);
            }
            parent.setUnreadCount(unreadCountMap.getOrDefault(parentId, 0));
        }

        // 添加子分类的未读数量
        for (AdminMsgTypeListVO parent : tree) {
            List<AdminMsgTypeListVO.AdminMsgTypeListChildVO> children = parent.getChild();
            int childUnreadCount = 0;
            if (children != null) {
                for (AdminMsgTypeListVO.AdminMsgTypeListChildVO child : children) {
                    // 使用 msgType 获取未读数量
                    Integer childId = child.getMsgType();
                    Integer count = unreadCountMap.getOrDefault(childId, 0);
                    childUnreadCount += count;
                    child.setUnreadCount(count);
                }
            }
            parent.setUnreadCount(parent.getUnreadCount() + childUnreadCount);
        }

        return tree;
    }

    @Override
    public HomeMsgSummaryVO getHomeSummary() {
        HomeMsgSummaryVO summary = new HomeMsgSummaryVO();

        // 未读总数：复用 msgTypeArr 的统计口径（与消息中心铃铛角标一致）
        // 父分类的 unreadCount 已累加其子分类未读数，求和即为该身份总未读
        List<AdminMsgTypeListVO> typeList = getAdminMsgTypeList();
        int unreadCount = typeList.stream()
                .mapToInt(parent -> parent.getUnreadCount() == null ? 0 : parent.getUnreadCount())
                .sum();
        summary.setUnreadCount(unreadCount);

        // 最新一条：复用 list 的身份 where（去掉 msgType 过滤），按发送时间倒序取第 1 条
        LambdaQueryWrapper<AdminMsg> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(AdminMsg::getVendorId, getVendorId());
        } else {
            queryWrapper.eq(AdminMsg::getShopId, getShopId());
            queryWrapper.eq(AdminMsg::getVendorId, 0);
        }
        // 平台端首页最新一条与内容列表口径对齐：只取会播报的自营消息，排除第三方店铺订单的平台副本
        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.ADMIN) {
            queryWrapper.eq(AdminMsg::getIsBroadcast, 1);
        }
        queryWrapper.orderByDesc(AdminMsg::getSendTime).orderByDesc(AdminMsg::getMsgId);
        Page<AdminMsg> latestPage = this.page(new Page<>(1, 1), queryWrapper);
        List<AdminMsg> records = latestPage.getRecords();
        if (records == null || records.isEmpty()) {
            summary.setLatestMessage(null);
            return summary;
        }

        AdminMsg latest = records.get(0);
        HomeMsgSummaryVO.LatestMessage latestMessage = new HomeMsgSummaryVO.LatestMessage();
        latestMessage.setMsgId(latest.getMsgId());
        latestMessage.setMsgType(latest.getMsgType());
        latestMessage.setTypeName(getMsgTypeName(latest.getMsgType()));
        latestMessage.setTitle(latest.getTitle());
        latestMessage.setSendTime(latest.getSendTime() == null ? null
                : DateUtil.format(DateUtil.date(latest.getSendTime() * 1000), "yyyy-MM-dd HH:mm:ss"));
        summary.setLatestMessage(latestMessage);
        return summary;
    }

    /**
     * 将 msgType 映射为二级分类名，与 msgTypeArr 同源（不硬编码中文）。
     *
     * @param msgType 消息类型编号
     * @return 二级分类名，找不到时返回空字符串
     */
    private String getMsgTypeName(Integer msgType) {
        if (msgType == null) {
            return "";
        }
        for (AdminMsgTypeEnum type : AdminMsgTypeEnum.values()) {
            if (type.getCatId() == msgType) {
                return type.getCatName();
            }
        }
        return "";
    }

    /** 为历史消息补齐详情跳转标识；新消息优先使用创建时写入的 route。 */
    private void applyDefaultRoute(Integer msgType, JSONObject relatedData) {
        if (relatedData == null || StrUtil.isNotBlank(relatedData.getStr("route")) || msgType == null) {
            return;
        }
        String route = switch (msgType) {
            case 15, 16, 31, 54 -> "order";
            case 32, 36 -> "aftersale";
            case 21, 22, 23, 24 -> "product";
            case 25, 53, 55, 56, 57 -> "memberFeedback";
            case 26 -> "productInquiry";
            case 33 -> "withdrawal";
            case 34, 35 -> "invoice";
            case 37 -> "offlinePayment";
            case 44 -> "deposit";
            case 41, 42, 43 -> "shop";
            default -> null;
        };
        if (route != null) {
            relatedData.set("route", route);
        }
    }

}
