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

package com.tigshop.service.user.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserMessageCreateDTO;
import com.tigshop.bean.enums.order.OrderType;
import com.tigshop.bean.query.user.UserMessageListPageQuery;
import com.tigshop.bean.model.user.UserMessage;
import com.tigshop.bean.vo.user.UnreadMessageCountVO;
import com.tigshop.bean.vo.user.UserMessageVO;
import com.tigshop.common.constant.order.OrderConstants;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.user.UserMessageMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.im.ImMessageService;
import com.tigshop.service.user.UserMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tigshop.bean.vo.order.OrderVO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 站内消息服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@Service
public class UserMessageServiceImpl extends BaseServiceImpl<UserMessageMapper, UserMessage> implements UserMessageService {


    @Resource
    TranslatePackage translatePackage;

    @Resource
    ImMessageService imMessageService;

    @Resource
    com.tigshop.service.order.OrderService orderService;

    @Override
    public Page<UserMessageVO> list(Integer userId, UserMessageListPageQuery listDTO) {
        // 分页
        Page<UserMessage> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        queryWrapper.eq(UserMessage::getUserId, userId);

        //unRead筛选
        if (listDTO.getUnread() != null && listDTO.getUnread() == 1) {
            queryWrapper.eq(UserMessage::getIsRead, 0);
        }

        //消息类型
        if (listDTO.getType() != null && listDTO.getType() > 0) {
            queryWrapper.eq(UserMessage::getType, listDTO.getType());
        } else {
            // 未传type时，排除type为1的记录
            queryWrapper.ne(UserMessage::getType, 1);
        }

        // 执行查询
        Page<UserMessage> userMessagePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserMessage> userMessagePageRecords = userMessagePage.getRecords();
        // 转换为VO
        List<UserMessageVO> userMessageVOList = userMessagePageRecords.stream()
                .map(userMessage -> {
                    UserMessageVO userMessageVO = new UserMessageVO();
                    BeanUtils.copyProperties(userMessage, userMessageVO);
                    DateTime date = DateUtil.date(userMessage.getAddTime() * 1000);
                    userMessageVO.setAddTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
                    userMessageVO.setAddTimeDate(DateUtil.format(date, "yyyy-MM-dd"));
                    userMessageVO.setAddTimeHms(DateUtil.format(date, "HH:mm:ss"));

                    JSONObject link = JsonUtil.fromJson(userMessage.getLink(), JSONObject.class);
                    userMessageVO.setLink(link);

                    // 处理订单信息
                    if (link != null) {
                        try {
                            // 判断是否是JSON格式且包含path字段
                            String path = link.getStr("path");
                            if ("order".equals(path)) {
                                // 从id字段获取订单ID
                                Integer orderId = link.getInt("id");
                                if (orderId != null && orderId > 0) {
                                    try {
                                        // 查询订单信息
                                        OrderVO orderVO = orderService.detail(orderId, userId);
                                        // 拆单父单被物理删除时 detail 返回空 VO：该通知在用户端已无法打开 → 隐藏（返回 null 后统一过滤）
                                        if (orderVO == null || orderVO.getOrderId() == null) {
                                            return null;
                                        }
                                        Integer orderType = orderVO.getOrderType();
                                        if(OrderType.isReservationOrder(orderType)){
                                            link.set("path", "reservationOrder");
                                        }
                                        userMessageVO.setOrderInfo(orderVO);
                                    } catch (GlobalException ge) {
                                        // 拆单父单 isValid=0（ORDER_SPLIT_ERROR）：用户端已看不到父单，点击会空白 → 隐藏该通知
                                        if (OrderConstants.ORDER_SPLIT_ERROR.equals(ge.getMessage())) {
                                            return null;
                                        }
                                        // 其它业务异常保持原行为：保留通知，仅无 orderInfo，避免误伤普通订单通知
                                        log.error("查询订单信息失败，orderId: {}", orderId, ge);
                                    } catch (Exception e) {
                                        // 如果查询失败，不影响主流程
                                        log.error("查询订单信息失败，orderId: {}", orderId, e);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // 解析失败不影响主流程
                            log.error("解析link字段失败", e);
                        }
                    }

                    //填充翻译
                    String content = userMessage.getContent();
                    String messageTemplate = userMessage.getMessageTemplate();
                    if (StringUtils.isNotBlank(messageTemplate) && Objects.nonNull(link)) {
                        //转化
                        content = translatePackage.translate(messageTemplate);
                        String fillParams = link.getStr("data");
                        if (StringUtils.isNotBlank(fillParams)) {
                            Map<String, Object> dataParams = JSONUtil.parseObj(fillParams);
                            content = MessageTemplateUtils.fillMessageTemplate(content, Optional.ofNullable(dataParams).orElse(Collections.emptyMap()));
                        }
                    }
                    userMessageVO.setContent(content);
                    return userMessageVO;
                })
                // 过滤掉指向拆单父单（用户端已不可访问）的通知，避免点击后空白页
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return PageUtil.convertPage(userMessagePage, userMessageVOList);
    }

    @Override
    public UserMessageVO detail(Integer id) {
        if (id != null) {
            UserMessage userMessage = this.getById(id);
            UserMessageVO userMessageVO = new UserMessageVO();
            BeanUtils.copyProperties(userMessage, userMessageVO);
            return userMessageVO;
        }
        return new UserMessageVO();
    }

    @Override
    public boolean create(UserMessageCreateDTO createDTO) {
        if (createDTO != null) {
            UserMessage userMessage = new UserMessage();
            BeanUtils.copyProperties(createDTO, userMessage);
            return this.save(userMessage);
        }
        return false;
    }

    @Override
    public boolean setAllMessageRead(Integer userId) {
        if (userId == null) {
            return false;
        }
        LambdaUpdateWrapper<UserMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserMessage::getUserId, userId);
        updateWrapper.eq(UserMessage::getIsRead, 0);
        updateWrapper.set(UserMessage::getIsRead, 1);
        return this.update(updateWrapper);
    }

    @Override
    public boolean setOrderMessageRead(Integer userId) {
        if (userId == null) {
            return false;
        }
        LambdaUpdateWrapper<UserMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserMessage::getUserId, userId);
        updateWrapper.eq(UserMessage::getIsRead, 0);
        updateWrapper.eq(UserMessage::getType, 1);
        updateWrapper.set(UserMessage::getIsRead, 1);
        return this.update(updateWrapper);
    }

    @Override
    public boolean setMessageRead(Integer userId, Integer messageId) {
        if (userId == null || messageId == null) {
            return false;
        }
        LambdaUpdateWrapper<UserMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserMessage::getUserId, userId);
        updateWrapper.eq(UserMessage::getMessageId, messageId);
        updateWrapper.set(UserMessage::getIsRead, 1);
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserMessage(Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (id == null || id == 0) {
            throw new GlobalException("删除失败，消息不存在");
        }
        UserMessage userMessage = this.getById(id);
        if (userMessage != null) {
            Integer messageUserId = userMessage.getUserId();
            if (messageUserId == null || !messageUserId.equals(userId)) {
                throw new GlobalException("删除失败，您没有权限删除此消息");
            }
        }
        this.removeById(id);
    }

    @Override
    public UnreadMessageCountVO getUnreadMessageCount(Integer userId) {
        if (userId == null) {
            return UnreadMessageCountVO.builder()
                    .userMessageCount(0L)
                    .orderMessageCount(0L)
                    .imMessageCount(0L)
                    .build();
        }

        // 统计站内信未读消息数量（is_read = 0）
        LambdaQueryWrapper<UserMessage> allUnreadWrapper = new LambdaQueryWrapper<>();
        allUnreadWrapper.eq(UserMessage::getUserId, userId);
        allUnreadWrapper.eq(UserMessage::getIsRead, 0);
        Long userMessageCount = this.count(allUnreadWrapper);

        // 统计订单消息未读数量（type = 1 && is_read = 0）
        LambdaQueryWrapper<UserMessage> orderUnreadWrapper = new LambdaQueryWrapper<>();
        orderUnreadWrapper.eq(UserMessage::getUserId, userId);
        orderUnreadWrapper.eq(UserMessage::getType, 1);
        orderUnreadWrapper.eq(UserMessage::getIsRead, 0);
        Long orderMessageCount = this.count(orderUnreadWrapper);

        // 统计IM消息未读数量（type = 2 && is_read = 0）
        Long imMessageCount = imMessageService.countUnreadServantMessagesByUserId(userId);

        return UnreadMessageCountVO.builder()
                .userMessageCount(userMessageCount)
                .orderMessageCount(orderMessageCount)
                .imMessageCount(imMessageCount)
                .build();
    }

}
