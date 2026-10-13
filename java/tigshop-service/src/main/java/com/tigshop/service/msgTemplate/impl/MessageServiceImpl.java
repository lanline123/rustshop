package com.tigshop.service.msgTemplate.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.dto.msg.MessageTemplateListDTO;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.msg.MessageTemplateType;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.OrderInvoice;
import com.tigshop.bean.model.finance.RefundLog;
import com.tigshop.bean.model.merchant.MerchantApply;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.MessageTemplate;
import com.tigshop.bean.model.setting.MessageType;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserAddress;
import com.tigshop.bean.model.user.UserAuthorize;
import com.tigshop.bean.model.user.UserCompany;
import com.tigshop.bean.utils.VendorOrderPriceCalculator;
import com.tigshop.common.constant.msg.OrderAdminMsgTemplate;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.mapper.finance.OrderInvoiceMapper;
import com.tigshop.mapper.merchant.MerchantApplyMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.setting.MessageTemplateMapper;
import com.tigshop.mapper.setting.MessageTypeMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserAuthorizeMapper;
import com.tigshop.mapper.user.UserCompanyMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.finance.RefundLogService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.msgTemplate.MessageSender;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserAddressService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.poi.excel.sax.AttributeName.t;
import static me.zhyd.oauth.enums.scope.AuthProginnScope.email;
import static me.zhyd.oauth.enums.scope.AuthProginnScope.realname;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Resource
    private List<MessageSender> senders;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private AdminMsgService adminMsgService;
    @Resource
    private UserAuthorizeMapper userAuthorizeService;
    @Resource
    private ConfigService configService;
    @Resource
    private MessageTypeMapper messageTypeService;
    @Resource
    private MessageTemplateMapper messageTemplateService;
    @Resource
    private OrderInvoiceMapper orderInvoiceMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private MerchantApplyMapper merchantApplyMapper;

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private RefundLogService refundLogService;
    @Resource
    private AftersalesService aftersalesService;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private ProductMapper productMapper;

    private Integer realNameAuthentication = 0;

    @Resource
    private TigshopProperties tigshopProperties;
    @Autowired
    private UserCompanyMapper userCompanyMapper;


    @Override
    public void sendMessage(Message message) {
        for (MessageSender sender : senders) {
            if (sender.supports(message.getType())) {
                // 底层防御性 try-catch：即使某个 Sender 内部容错被穿透，也不冒泡到调用方主流程
                try {
                    sender.send(message);
                } catch (Exception e) {
                    log.error("消息发送失败，渠道: {}，不影响主流程: {}", message.getType(), e.getMessage(), e);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sendUserMessage(Integer userId, Integer orderId, MessageTypeEnum messageTypeEnum) {
        sendUserMessage(userId, orderId, messageTypeEnum, BigDecimal.ZERO);
    }

    @Override
    public void sendMerchantApplyResultMessage(Integer merchantApplyId, MessageTypeEnum messageTypeEnum, String userName, String password) {
        // 容错说明：每个通道独立 try-catch，任一通道失败不阻塞其它通道，也不冒泡到商家入驻审核主流程
        MessageTemplateListDTO messageTemplateList;
        try {
            messageTemplateList = getMessageTemplateList(messageTypeEnum);
        } catch (Exception e) {
            log.error("获取商家入驻消息模板失败，跳过通知发送: {}", e.getMessage(), e);
            return;
        }

        try {
            //商家入驻发送短信
            sendMerchantApplyResultSmsMessage(messageTypeEnum, messageTemplateList, merchantApplyId, userName, password);
        } catch (Exception e) {
            log.error("商家入驻短信通知发送失败: {}", e.getMessage(), e);
        }

        try {
            //商家入驻发送邮件
            sendMerchantApplyResultMailMessage(messageTypeEnum, messageTemplateList, merchantApplyId, userName, password);
        } catch (Exception e) {
            log.error("商家入驻邮件通知发送失败: {}", e.getMessage(), e);
        }

        try {
            //商家入驻发送钉钉
            sendMerchantApplyResultDingMessage(messageTypeEnum, messageTemplateList, merchantApplyId, userName, password);
        } catch (Exception e) {
            log.error("商家入驻钉钉通知发送失败: {}", e.getMessage(), e);
        }

        try {
            //商家入驻发送企业微信
            sendMerchantApplyResultWxWorkMessage(messageTypeEnum, messageTemplateList, merchantApplyId, userName, password);
        } catch (Exception e) {
            log.error("商家入驻企业微信通知发送失败: {}", e.getMessage(), e);
        }
    }


    @Override
    public void sendUserMessage(Integer userId, Integer orderId, MessageTypeEnum messageTypeEnum, BigDecimal totalRefund) {
        if (!Arrays.asList(MessageTypeEnum.values()).contains(messageTypeEnum)) {
            return;
        }
        Order order = new Order();
        if (orderId < 0) {
            order.setUserId(userId);
            realNameAuthentication = orderId;
        } else {
            // 获取订单信息
            order = orderMapper.selectById(orderId);
            if (order == null || ObjectUtil.notEqual(order.getUserId(), userId)) {
                log.error("未查询到对应订单 {}", orderId);
                return;
            }
        }

        try {
            // 获取模板信息相关数据
            MessageTemplateListDTO messageTemplateList = getMessageTemplateList(messageTypeEnum);

            // 发送站内信
            sendWebSiteMessage(messageTemplateList, messageTypeEnum, order);

            // 需要发送短信
            sendSmsMessage(messageTypeEnum, messageTemplateList, order);

            // 需要发送公众号消息
            sendWechatMessage(messageTemplateList, messageTypeEnum, order, totalRefund);

            // 需要小程序消息
            sendMiniProgramMessage(userId, orderId, messageTypeEnum, messageTemplateList, order);

            // 需要发送邮件消息
            sendMailMessage(messageTypeEnum, messageTemplateList, order);

            // 需要给钉钉发送群消息
            sendDingMessage(messageTypeEnum, messageTemplateList, order);

            // 需要给企业微信发送群消息
            sendWXWorkMessage(messageTypeEnum, messageTemplateList, order);
        } catch (Exception e) {
            log.error("模板信息发送报错: ", e);
        }

        // 发送后台消息（防御性 try-catch：后台消息生成失败不应影响订单/支付主流程）
        try {
            BigDecimal vendorOrderAmount = calculateVendorOrderAmount(order);
            if (messageTypeEnum == MessageTypeEnum.NEW_ORDER) {
                AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
                adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.NEW_ORDER.getCatId());
                adminMsgCreateDTO.setTitle(OrderAdminMsgTemplate.newOrderTitle(order.getOrderSn(),order.getTotalAmount()));
                adminMsgCreateDTO.setContent(OrderAdminMsgTemplate.newOrderContent(order.getOrderSn()));
                adminMsgCreateDTO.setShopId(order.getShopId());
                Map<String, Object> relatedData = new HashMap<>();
                relatedData.put("orderId", order.getOrderId());
                adminMsgCreateDTO.setRelatedData(relatedData);
                adminMsgService.createMessage(adminMsgCreateDTO);
                if (order.getShopId() != null && order.getShopId() > 0) {
                    //如果是店铺订单则给平台订单也发一条：消息中心可见但恒不播报（平台只播自营）
                    adminMsgCreateDTO.setShopId(0);
                    adminMsgCreateDTO.setIsBroadcast(0);
                    adminMsgService.createMessage(adminMsgCreateDTO);
                }
                if (order.getVendorId() != null && order.getVendorId() > 0) {
                    //供应商需处理自己订单 → 正常播报（复用对象，须显式重置 is_broadcast）
                    adminMsgCreateDTO.setVendorId(order.getVendorId());
                    adminMsgCreateDTO.setIsBroadcast(1);
                    adminMsgCreateDTO.setTitle(OrderAdminMsgTemplate.newOrderTitle(order.getOrderSn(), vendorOrderAmount));
                    adminMsgService.createMessage(adminMsgCreateDTO);
                }

                // 用户下单后提醒店铺/供应商及时发货。
                sendOrderDeliveryReminderMessages(order);


            }

            if (messageTypeEnum == MessageTypeEnum.ORDER_PAY) {

                AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
                adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.PAID_ORDER.getCatId());
                adminMsgCreateDTO.setTitle(OrderAdminMsgTemplate.paidOrderTitle(order.getOrderSn(), order.getTotalAmount()));
                adminMsgCreateDTO.setContent(OrderAdminMsgTemplate.paidOrderContent(order.getOrderSn()));
                adminMsgCreateDTO.setShopId(order.getShopId());
                Map<String, Object> relatedData = new HashMap<>();
                relatedData.put("orderId", order.getOrderId());
                adminMsgCreateDTO.setRelatedData(relatedData);
                adminMsgService.createMessage(adminMsgCreateDTO);
                if (order.getShopId() != null && order.getShopId() > 0) {
                    //如果是店铺订单则给平台订单也发一条：消息中心可见但恒不播报（平台只播自营）
                    adminMsgCreateDTO.setShopId(0);
                    adminMsgCreateDTO.setIsBroadcast(0);
                    adminMsgService.createMessage(adminMsgCreateDTO);
                }
                if (order.getVendorId() != null && order.getVendorId() > 0) {
                    //供应商需处理自己订单 → 正常播报（复用对象，须显式重置 is_broadcast）
                    adminMsgCreateDTO.setVendorId(order.getVendorId());
                    adminMsgCreateDTO.setIsBroadcast(1);
                    adminMsgCreateDTO.setTitle(OrderAdminMsgTemplate.paidOrderTitle(order.getOrderSn(), vendorOrderAmount));
                    adminMsgService.createMessage(adminMsgCreateDTO);
                }
            }
        } catch (Exception e) {
            log.error("生成后台消息失败，不影响主流程: {}", e.getMessage(), e);
        }
    }

    /**
     * 为新订单的实际履约方创建发货提醒消息。
     *
     * <p>店铺订单的平台副本沿用新订单消息的不可播报口径；平台自营订单则由 shopId=0 的消息直接接收。</p>
     */
    private void sendOrderDeliveryReminderMessages(Order order) {
        if (order == null) {
            return;
        }

        BigDecimal platformOrderAmount = order.getTotalAmount();
        BigDecimal vendorOrderAmount = calculateVendorOrderAmount(order);
        createOrderDeliveryReminderMessage(order, order.getShopId(), 0, 1, platformOrderAmount);

        if (order.getShopId() != null && order.getShopId() > 0) {
            createOrderDeliveryReminderMessage(order, 0, 0, 0, platformOrderAmount);
        }
        if (order.getVendorId() != null && order.getVendorId() > 0) {
            createOrderDeliveryReminderMessage(order, 0, order.getVendorId(), 1, vendorOrderAmount);
        }
    }

    private void createOrderDeliveryReminderMessage(Order order, Integer shopId, Integer vendorId,
                                                    Integer isBroadcast, BigDecimal amount) {
        AdminMsgCreateDTO message = new AdminMsgCreateDTO();
        message.setMsgType(AdminMsgTypeEnum.QUICK_DELIVERY.getCatId());
        message.setTitle("发货提醒：" + order.getOrderSn() + "，金额：" + amount);
        message.setContent("收到订单号：" + order.getOrderSn() + "，金额：" + amount + "，请尽快进行发货");
        message.setShopId(shopId);
        message.setVendorId(vendorId);
        message.setOrderId(order.getOrderId());
        message.setIsBroadcast(isBroadcast);
        message.setRelatedData(Map.of("orderId", order.getOrderId()));
        adminMsgService.createMessage(message);
    }

    /**
     * 供应商消息文案使用订单商品上的供货价快照；历史订单缺少快照时回退到平台售价。
     */
    private BigDecimal calculateVendorOrderAmount(Order order) {
        if (order == null || order.getOrderId() == null) {
            return BigDecimal.ZERO;
        }
        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getOrderId()));
        if (orderItems == null || orderItems.isEmpty()) {
            return order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        }
        return orderItems.stream()
                .map(item -> VendorOrderPriceCalculator.calculateSupplySubtotal(
                        item.getPrice(), item.getVendorProductSupplyPrice(), item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void sendMiniProgramMessage(Integer userId, Integer orderId, MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Order order) {
       try {
           if (messageTemplateList != null && messageTemplateList.getMiniProgram() != null && messageTemplateList.getTypeInfo().getIsMiniProgram() == 1 && messageTemplateList.getMiniProgram().getDisabled() == 0) {
               String openId = Optional.ofNullable(userAuthorizeService.selectOne(new LambdaQueryWrapper<UserAuthorize>()
                               .eq(UserAuthorize::getUserId, userId)
                               .eq(UserAuthorize::getAuthorizeType, 2)))
                       .map(UserAuthorize::getOpenId)
                       .orElse("");
               String templateId = messageTemplateList.getMiniProgram().getInfo().getTemplateId();
               if (ObjectUtil.isNotEmpty(openId) && ObjectUtil.isNotEmpty(templateId)) {
                   String page = "/pages/user/order/info?id=" + orderId;
                   HashMap<String, Object> message = new HashMap<>();
                   message.put("touser", openId);
                   message.put("template_id", templateId);
                   message.put("page", page);
                   HashMap<String, Object> data = new HashMap<>();
                   if (messageTypeEnum == MessageTypeEnum.ORDER_PAY) {
                       data.put("character_string2", Map.of("value", order.getOrderSn()));
                       data.put("time1", Map.of("value", DateUtil.format(DateUtil.date(order.getAddTime() * 1000), "yyyy-MM-dd HH:mm:ss")));
                       data.put("amount4", Map.of("value", order.getTotalAmount()));
                   }
                   if (messageTypeEnum == MessageTypeEnum.ORDER_SHIPPING) {
                       data.put("thing4", Map.of("value", order.getLogisticsName()));
                       data.put("character_string5", Map.of("value", order.getTrackingNo()));
                       data.put("date3", Map.of("value", DateUtil.format(DateUtil.date(order.getShippingTime() * 1000), "yyyy-MM-dd HH:mm:ss")));
                       data.put("thing8", Map.of("value", order.getConsignee() + " " + order.getMobile()));
                       data.put("character_string2", Map.of("value", order.getOrderSn()));
                   }
                   message.put("data", data);
                   log.info("发送小程序消息：{}，内容：{}", templateId, message);
                   sendMessage(Message.builder()
                           .type(MessageTemplateType.MiniProgram)
                           .message(message)
                           .build());
               }
           }
       } catch (Exception e) {
           log.error("小程序发送报错: ", e);
       }
    }

    private void sendWechatMessage(MessageTemplateListDTO messageTemplateList, MessageTypeEnum messageTypeEnum, Order order, BigDecimal totalRefund) {
        if (messageTemplateList != null && messageTemplateList.getWechat() != null && messageTemplateList.getTypeInfo().getIsWechat() == 1 && messageTemplateList.getWechat().getDisabled() == 0) {
            String openId = Optional.ofNullable(userAuthorizeService.selectOne(new LambdaQueryWrapper<UserAuthorize>()
                            .eq(UserAuthorize::getUserId, order.getUserId())
                            .eq(UserAuthorize::getAuthorizeType, 1)))
                    .map(UserAuthorize::getOpenId)
                    .orElse("");
            String templateId = messageTemplateList.getWechat().getInfo().getTemplateId();
            if (ObjectUtil.isNotEmpty(openId) && ObjectUtil.isNotEmpty(templateId)) {
                String h5Domain = configService.getConfigVal(SettingsEnum.H5_DOMAIN);
                if (ObjectUtil.isEmpty(h5Domain)) {
                    h5Domain = configService.getConfigVal(SettingsEnum.PC_DOMAIN);
                }
                String url = h5Domain + "/pages/user/order/info?id=" + order.getOrderId();
                HashMap<String, Object> message = new HashMap<>();
                message.put("touser", openId);
                message.put("template_id", templateId);
                message.put("url", url);
                HashMap<String, Object> data = new HashMap<>();
                if (messageTypeEnum == MessageTypeEnum.ORDER_PAY) {
                    data.put("character_string3", Map.of("value", order.getOrderSn()));
                    data.put("time7", Map.of("value", DateUtil.format(DateUtil.date(order.getAddTime() * 1000), "yyyy-MM-dd HH:mm:ss")));
                    data.put("amount4", Map.of("value", order.getTotalAmount()));
                }
                if (messageTypeEnum == MessageTypeEnum.ORDER_SHIPPING) {
                    data.put("thing21", Map.of("value", order.getLogisticsName()));
                    data.put("character_string18", Map.of("value", order.getTrackingNo()));
                    data.put("time3", Map.of("value", DateUtil.format(DateUtil.date(order.getShippingTime() * 1000), "yyyy-MM-dd HH:mm:ss")));
                    data.put("thing17", Map.of("value", order.getConsignee() + " " + order.getMobile()));
                    data.put("character_string2", Map.of("value", order.getOrderSn()));
                }
                if (messageTypeEnum == MessageTypeEnum.ORDER_REFUND) {
                    data.put("character_string5", Map.of("value", order.getOrderSn()));
                    data.put("amount2", Map.of("value", totalRefund));
                }
                message.put("data", data);
                log.info("发送公众号消息：{}，内容：{}", templateId, message);
                sendMessage(Message.builder()
                        .type(MessageTemplateType.Wechat)
                        .message(message)
                        .build());
            }
        }
    }

    private void sendSmsMessage(MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Order order) {
        if (messageTemplateList != null && messageTemplateList.getMsg() != null && messageTemplateList.getTypeInfo().getIsMsg() == 1 && messageTemplateList.getMsg().getDisabled() == 0) {
            MessageTemplate msgTemplateInfo = messageTemplateList.getMsg().getInfo();
            HashMap<String, Object> content = new HashMap<>();
            String mobile = order.getMobile();
            // 仅当 mobile 来自订单收货地址时，才尝试用 addressData 里的 mobileAreaCode 拼接区号；
            // 走 user 表 / 发票 / 店铺配置 / 公司联系人等其它来源时，没有可靠区号，保持原样。
            boolean mobileFromOrderAddress = StrUtil.isNotEmpty(mobile);
            if (StrUtil.isEmpty(mobile)) {
                User user = userMapper.selectById(order.getUserId());
                mobile = user.getMobile();
            }

            if (messageTypeEnum == MessageTypeEnum.ORDER_INVOICE) {
                OrderInvoice orderInvoice = orderInvoiceMapper.selectOne(
                        Wrappers.lambdaQuery(OrderInvoice.class)
                                .eq(OrderInvoice::getOrderId, order.getOrderId())
                );
                mobile = orderInvoice.getMobile();
                mobileFromOrderAddress = false;
            }

            if (messageTypeEnum == MessageTypeEnum.NEW_ORDER) {
                content.put("order_sn", order.getOrderSn());
            }

            if (Arrays.asList(MessageTypeEnum.ORDER_PAY, MessageTypeEnum.ORDER_REFUND).contains(messageTypeEnum)) {
                content.put("order", order.getOrderSn());
            }

            if (messageTypeEnum == MessageTypeEnum.ORDER_SHIPPING) {
                content.put("shipping_name", order.getLogisticsName());
                content.put("code", order.getTrackingNo());
            }

            if (Arrays.asList(MessageTypeEnum.ORDER_PAY_SHOP, MessageTypeEnum.NEW_ORDER_SHOP).contains(messageTypeEnum)) {
                mobile = configService.getConfigVal(SettingsEnum.SMS_SHOP_MOBILE);
                content.put("order", order.getOrderSn());
                content.put("fee", order.getTotalAmount().setScale(2, RoundingMode.HALF_UP).toString());
                mobileFromOrderAddress = false;
            }

            if (messageTypeEnum == MessageTypeEnum.USER_COMPANY_AUTH) {
                UserCompany userCompany = userCompanyMapper.selectOne(Wrappers.lambdaQuery(UserCompany.class).eq(UserCompany::getUserId, order.getUserId()).last("limit 1"));
                mobile = userCompany.getContactMobile();
                if (realNameAuthentication == -1) {
                    content.put("type_text", "会员实名");
                    content.put("num", 7);
                }
                mobileFromOrderAddress = false;
            }

            if (mobileFromOrderAddress) {
                mobile = composeMobileWithAreaCode(order.getAddressData(), mobile);
            }

            String templateCode = msgTemplateInfo.getTemplateId();
            String contentOversea = msgTemplateInfo.getContentOversea();
            log.info("发送短信：{}（海外明文：{}），内容：{}", templateCode, contentOversea, content);
            Map<String, Object> smsMessage = new HashMap<>();
            smsMessage.put("mobile", mobile);
            smsMessage.put("template_code", templateCode);
            smsMessage.put("content_oversea", contentOversea);
            smsMessage.put("content", content);
            sendMessage(Message.builder()
                    .type(MessageTemplateType.SMS)
                    .message(smsMessage)
                    .build());
        }
    }

    /**
     * 兼容收货地址里的国际区号：从订单 addressData（hutool 序列化的 UserAddressVO JSON）
     * 中提取 mobileAreaCode 并拼到 mobile 前面。
     *
     * - 当区号为空或为 "86" 时，保持原 mobile 不变（继续走国内短信通道）；
     * - 当区号非 86 且 mobile 还未带区号时，返回 "{areaCode}{mobile}"（不带 +，符合阿里云国际短信 SendMessageToGlobe 的格式要求）；
     * - 入参 mobile 中的 +/空格/- 会先被清理。
     *
     * 历史背景：order 表只有 mobile 列，OrderCheckServiceImpl 把收货地址拷到订单时只搬了 mobile，
     * 区号其实存在 addressData JSON 里却从未被使用，导致下单短信发到海外号时丢区号。
     */
    private static String composeMobileWithAreaCode(String addressData, String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return mobile;
        }
        String cleanedMobile = mobile.replace("+", "").replaceAll("[\\s-]", "");
        String areaCode = extractAreaCodeFromAddressData(addressData);
        if (StrUtil.isBlank(areaCode)) {
            return cleanedMobile;
        }
        String cleanedAreaCode = areaCode.replace("+", "").trim();
        if ("86".equals(cleanedAreaCode)) {
            return cleanedMobile;
        }
        if (cleanedMobile.startsWith(cleanedAreaCode)) {
            return cleanedMobile;
        }
        return cleanedAreaCode + cleanedMobile;
    }

    private static String extractAreaCodeFromAddressData(String addressData) {
        if (StrUtil.isBlank(addressData)) {
            return null;
        }
        try {
            JSONObject obj = JSONUtil.parseObj(addressData);
            return obj.getStr("mobileAreaCode");
        } catch (Exception e) {
            return null;
        }
    }

    private void sendMerchantApplyResultSmsMessage(MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Integer merchantApplyId, String userName, String password) {
        if (messageTemplateList != null && messageTemplateList.getMsg() != null && messageTemplateList.getTypeInfo().getIsMsg() == 1 && messageTemplateList.getMsg().getDisabled() == 0) {
            MessageTemplate msgTemplateInfo = messageTemplateList.getMsg().getInfo();

            HashMap<String, Object> content = new HashMap<>();
            MerchantApply merchantApply = merchantApplyMapper.selectById(merchantApplyId);
            if (merchantApply == null) {
                return;
            }

            User user = userMapper.selectById(merchantApply.getUserId());
            if (user == null || user.getMobile() == null) {
                return;
            }

            if (Objects.equals(MessageTypeEnum.MERCHANT_SETTED_IN_SUCCESS, messageTypeEnum)) {
                content.put("username", userName);
                content.put("password", password);
            }

            String templateCode = msgTemplateInfo.getTemplateId();
            String contentOversea = msgTemplateInfo.getContentOversea();
            log.info("发送商家入驻短信：{}（海外明文：{}），内容：{}", templateCode, contentOversea, content);
            Map<String, Object> smsMessage = new HashMap<>();
            smsMessage.put("mobile", user.getMobile());
            smsMessage.put("template_code", templateCode);
            smsMessage.put("content_oversea", contentOversea);
            smsMessage.put("content", content);
            sendMessage(Message.builder()
                    .type(MessageTemplateType.SMS)
                    .message(smsMessage)
                    .build());
        }
    }

    private void sendMailMessage(MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Order order) {
        if (messageTemplateList != null && messageTemplateList.getMail() != null && messageTemplateList.getTypeInfo().getIsMail() == 1 && messageTemplateList.getMail().getDisabled() == 0) {
            String templateCode = messageTemplateList.getMail().getInfo().getTemplateId();
            String content = messageTemplateList.getMail().getInfo().getContent();
            String email = userMapper.selectById(order.getUserId()).getEmail();
            if (messageTypeEnum == MessageTypeEnum.ORDER_INVOICE) {
                OrderInvoice orderInvoice = orderInvoiceMapper.selectOne(
                        Wrappers.lambdaQuery(OrderInvoice.class)
                                .eq(OrderInvoice::getOrderId, order.getOrderId())
                );
                email = orderInvoice.getEmail();
            }
            content = getContent(messageTypeEnum, messageTemplateList, order, content);
            log.info("发送邮件：{}，内容：{}", templateCode, content);
            sendMessage(Message.builder()
                    .type(MessageTemplateType.Mail)
                    .message(Map.of("userEmailAddress", email, "templateName", messageTemplateList.getMail().getInfo().getTemplateName(), "content", content))
                    .build());
        }
    }

    private void sendMerchantApplyResultMailMessage(MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Integer merchantApplyId, String userName, String password) {

        if (messageTemplateList != null && messageTemplateList.getMail() != null && messageTemplateList.getTypeInfo().getIsMail() == 1 && messageTemplateList.getMail().getDisabled() == 0) {
            String cotent = null;

            MerchantApply merchantApply = merchantApplyMapper.selectById(merchantApplyId);
            if (merchantApply == null) {
                return;
            }
            String merchantData = merchantApply.getMerchantData();
            JSONObject jsonObject = JSONUtil.parseObj(merchantData);

            // 提取邮件号码
            String contactEmail = jsonObject.getStr("contactEmail");

            String shopTitle = configService.getConfigVal(SettingsEnum.SHOP_NAME);
            cotent = messageTemplateList.getMail().getInfo().getContent().replace("${merchantName}", shopTitle);

            if (messageTypeEnum == MessageTypeEnum.MERCHANT_SETTED_IN_SUCCESS) {
                cotent = cotent.replace("${username}", userName);
                cotent = cotent.replace("${password}", password);
            }
            if (messageTypeEnum == MessageTypeEnum.MERCHANT_SETTED_IN_FAIL) {
                cotent = cotent.replace("${shopName}", merchantApply.getShopTitle());
            }
            sendMessage(Message.builder()
                    .type(MessageTemplateType.Mail)
                    .message(Map.of("userEmailAddress", contactEmail, "templateName", messageTemplateList.getMail().getInfo().getTemplateName(), "content", cotent))
                    .build());
        }
    }

    private void sendDingMessage(MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Order order) {
        if (messageTemplateList != null && messageTemplateList.getDing() != null && messageTemplateList.getTypeInfo().getIsDing() == 1 && messageTemplateList.getDing().getDisabled() == 0) {
            String templateCode = messageTemplateList.getDing().getInfo().getTemplateId();
            String content = messageTemplateList.getDing().getInfo().getContent();
            content = getContent(messageTypeEnum, messageTemplateList, order, content);

            log.info("发送钉钉：{}，内容：{}", templateCode, content);
            sendMessage(Message.builder()
                    .type(MessageTemplateType.Ding)
                    .message(Map.of("templateName", messageTemplateList.getDing().getInfo().getTemplateName(), "content", content))
                    .build());
        }
    }

    private void sendMerchantApplyResultDingMessage(MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Integer merchantApplyId, String userName, String password) {

        if (messageTemplateList != null && messageTemplateList.getDing() != null && messageTemplateList.getTypeInfo().getIsDing() == 1 && messageTemplateList.getDing().getDisabled() == 0) {
            String content = null;

            MerchantApply merchantApply = merchantApplyMapper.selectById(merchantApplyId);
            if (merchantApply == null) {
                return;
            }

            String shopTitle = configService.getConfigVal(SettingsEnum.SHOP_NAME);
            content = messageTemplateList.getDing().getInfo().getContent().replace("${merchantName}", shopTitle);

            if (messageTypeEnum == MessageTypeEnum.MERCHANT_SETTED_IN_SUCCESS) {
                content = content.replace("${userName}", userName);
                content = content.replace("${password}", password);
            }
            sendMessage(Message.builder()
                    .type(MessageTemplateType.Ding)
                    .message(Map.of("templateName", messageTemplateList.getDing().getInfo().getTemplateName(), "content", content))
                    .build());
        }
    }

    private void sendWXWorkMessage(MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Order order) {
        if (messageTemplateList != null && messageTemplateList.getWxWork() != null && messageTemplateList.getTypeInfo().getIsWxWork() == 1 && messageTemplateList.getWxWork().getDisabled() == 0) {
            String templateCode = messageTemplateList.getWxWork().getInfo().getTemplateId();
            String content = messageTemplateList.getWxWork().getInfo().getContent();
            content = getContent(messageTypeEnum, messageTemplateList, order, content);
            log.info("发送企业微信：{}，内容：{}", templateCode, content);
            sendMessage(Message.builder()
                    .type(MessageTemplateType.WXWork)
                    .message(Map.of("templateName", messageTemplateList.getWxWork().getInfo().getTemplateName(), "content", content))
                    .build());
        }
    }

    private void sendMerchantApplyResultWxWorkMessage(MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Integer merchantApplyId, String userName, String password) {
        if (messageTemplateList != null && messageTemplateList.getWxWork() != null && messageTemplateList.getTypeInfo().getIsWxWork() == 1 && messageTemplateList.getWxWork().getDisabled() == 0) {
            String templateCode = messageTemplateList.getWxWork().getInfo().getTemplateId();
            String content = null;

            MerchantApply merchantApply = merchantApplyMapper.selectById(merchantApplyId);
            if (merchantApply == null) {
                return;
            }

            String shopTitle = configService.getConfigVal(SettingsEnum.SHOP_NAME);
            content = messageTemplateList.getWxWork().getInfo().getContent().replace("${merchantName}", shopTitle);

            if (messageTypeEnum == MessageTypeEnum.MERCHANT_SETTED_IN_SUCCESS) {
                content = content.replace("${userName}", userName);
                content = content.replace("${password}", password);
            }

            log.info("发送商家入驻企业微信：{}，内容：{}", templateCode, content);
            sendMessage(Message.builder()
                    .type(MessageTemplateType.WXWork)
                    .message(Map.of("templateName", messageTemplateList.getWxWork().getInfo().getTemplateName(), "content", content))
                    .build());
        }
    }

    private void sendWebSiteMessage(MessageTemplateListDTO messageTemplates, MessageTypeEnum messageTypeEnum, Order order) {
       try {
           if (messageTemplates != null && messageTemplates.getMessage() != null && messageTemplates.getTypeInfo().getIsMessage() == 1 && messageTemplates.getMessage().getDisabled() == 0) {
               String title = messageTemplates.getMessage().getInfo().getTemplateName();
               String content = messageTemplates.getMessage().getInfo().getContent();
               HashMap<String, Object> link = new HashMap<>();
               link.put("path", "order");
               link.put("label", "订单提醒");
               link.put("id", order.getOrderId());
               link.put("name", title);
               link.put("data", Map.of("order_sn",order.getOrderSn(),"logistics_name",order.getLogisticsName()
                       ,"tracking_no",order.getTrackingNo()));
               String messageTemplate = content;
               content = content.replace("{order_sn}", order.getOrderSn());
               if (messageTypeEnum == MessageTypeEnum.ORDER_SHIPPING) {
                   content = content.replace("{logistics_name}", order.getLogisticsName());
                   content = content.replace("{tracking_no}", order.getTrackingNo());
               }
               log.info("发送站内信：{}，内容：{}", title, content);
               sendMessage(Message.builder()
                       .type(MessageTemplateType.SITE)
                       .message(Map.of("user_id", order.getUserId(), "title", title, "content", content, "messageTemplate",messageTemplate, "link", link, "type", 1))
                       .build());
           }
        } catch (Exception e) {
            log.error("发送站内信失败！{}", e.getMessage());
        }
    }

    private MessageTemplateListDTO getMessageTemplateList(MessageTypeEnum messageTypeEnum) {
        MessageType messageType = messageTypeService.selectById(messageTypeEnum.getId());

        if (messageType == null) {
            return null;
        }

        //多商户|企业版本|供应商 入驻审核失败这三个通知（站内信、小程序、公众号）是不要的 || 跨境版本入驻审核失败的站内信也去掉
        if (tigshopProperties.getIsMerchant() == 1 && tigshopProperties.getIsOverseas() == 0) {
            if (messageType.getName().startsWith("入驻审核失败")) {
                messageType.setIsMessage(-1);
                messageType.setIsMiniProgram(-1);
                messageType.setIsWechat(-1);
            }
        }
        if (messageType.getName().startsWith("商家入驻")) {
            //单商户不需要商家入驻
            if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
                messageType.setIsMessage(-1);
                messageType.setIsMiniProgram(-1);
                messageType.setIsWechat(-1);
                messageType.setIsWxWork(-1);
                messageType.setIsDing(-1);
                messageType.setIsMail(-1);
                messageType.setIsApp(-1);
                messageType.setIsMsg(-1);
            }
        }
        if (messageType.getName().startsWith("会员实名认证通知")) {
            //跨境、企业版、供应商需要实名认证
            if (!(tigshopProperties.getIsMerchant() == 1 && tigshopProperties.getIsB2b() == 1)) {
                messageType.setIsMessage(-1);
                messageType.setIsMiniProgram(-1);
                messageType.setIsWechat(-1);
                messageType.setIsWxWork(-1);
                messageType.setIsDing(-1);
                messageType.setIsMail(-1);
                messageType.setIsApp(-1);
                messageType.setIsMsg(-1);
            }
        }
        //非跨境没有邮箱发送验证码
        if (tigshopProperties.getIsOverseas() != 1) {
            if (messageType.getName().startsWith("获取验证码")) {
                messageType.setIsMail(-1);
            }
        }

        MessageTemplateListDTO result = new MessageTemplateListDTO();
        result.setTypeInfo(messageType);

        MessageTemplateListDTO.MessageTemplateListVO message = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(4, messageTypeEnum, messageType, message, messageType.getIsMessage());
        result.setMessage(message);

        MessageTemplateListDTO.MessageTemplateListVO wechat = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(1, messageTypeEnum, messageType, wechat, messageType.getIsWechat());
        result.setWechat(wechat);

        MessageTemplateListDTO.MessageTemplateListVO miniProgram = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(2, messageTypeEnum, messageType, miniProgram, messageType.getIsMiniProgram());
        result.setMiniProgram(miniProgram);

        MessageTemplateListDTO.MessageTemplateListVO msg = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(3, messageTypeEnum, messageType, msg, messageType.getIsMsg());
        result.setMsg(msg);

        MessageTemplateListDTO.MessageTemplateListVO app = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(5, messageTypeEnum, messageType, app, messageType.getIsApp());
        result.setApp(msg);

        MessageTemplateListDTO.MessageTemplateListVO ding = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(6, messageTypeEnum, messageType, ding, messageType.getIsDing());
        result.setDing(ding);

        MessageTemplateListDTO.MessageTemplateListVO email = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(7, messageTypeEnum, messageType, email, messageType.getIsMail());
        result.setMail(email);

        MessageTemplateListDTO.MessageTemplateListVO wxWork = new MessageTemplateListDTO.MessageTemplateListVO();
        fillData(8, messageTypeEnum, messageType, wxWork, messageType.getIsWxWork());
        result.setWxWork(wxWork);
        return result;
    }

    private void fillData(Integer type, MessageTypeEnum messageTypeEnum, MessageType messageType, MessageTemplateListDTO.MessageTemplateListVO app, Integer disabled) {
        MessageTemplate appTemplate = messageTemplateService.selectOne(
                Wrappers.lambdaQuery(MessageTemplate.class)
                        .eq(MessageTemplate::getType, type)
                        .eq(MessageTemplate::getMessageId, messageTypeEnum.getId())
        );
        disabled = disabled == -1 ? 1 : 0;
        app.setDisabled(disabled);
        app.setInfo(appTemplate);
    }

    private String getContent(MessageTypeEnum messageTypeEnum, MessageTemplateListDTO messageTemplateList, Order order, String content) {

        User user = userMapper.selectById(order.getUserId());

        if (messageTypeEnum == MessageTypeEnum.ORDER_REFUND) {
            String shopName = "";
            if (order.getShopId() > 0) {
                Shop shop = shopMapper.selectById(order.getShopId());
                shopName = shop.getShopTitle();
            } else {
                ConfigPO shopTitle = configService.getOne(Wrappers.lambdaQuery(ConfigPO.class).eq(ConfigPO::getBizCode, "shopName").last("limit 1"));
                shopName = shopTitle.getBizVal();
            }
            content = content.replace("${shopTitle}", shopName);
            content = content.replace("${userName}", user.getUsername());

            Aftersales aftersales = aftersalesService.getOne(Wrappers.lambdaQuery(Aftersales.class)
                    .eq(Aftersales::getOrderId, order.getOrderId())
                    .eq(Aftersales::getUserId, user.getUserId())
                    .orderByDesc(Aftersales::getAddTime)
                    .last("limit 1"));

            if (aftersales != null) {
                content = content.replace("${afterSaleSn}", aftersales.getAftersalesSn());
            }
            RefundLog one = refundLogService.getOne(Wrappers.lambdaQuery(RefundLog.class)
                    .eq(RefundLog::getOrderId, order.getOrderId())
                    .eq(RefundLog::getUserId, user.getUserId())
                    .last("limit 1"));

            if (one != null) {
                content = content.replace("${refundTime}", DateUtil.format(DateUtil.date(one.getAddTime() * 1000), "yyyy-MM-dd HH:mm:ss"));
                content = content.replace("${totalRefund}", one.getRefundAmount().toString());
            }

        }

        if (messageTypeEnum == MessageTypeEnum.ORDER_SHIPPING) {
            content = content.replace("${shippingName}", order.getLogisticsName());
            content = content.replace("${code}", order.getTrackingNo());
        }

        if (messageTypeEnum == MessageTypeEnum.ORDER_PAY) {
            String shopName;
            if (order.getShopId() > 0) {
                Shop shop = shopMapper.selectById(order.getShopId());
                shopName = shop.getShopTitle();
            } else {
                ConfigPO shopTitle = configService.getOne(Wrappers.lambdaQuery(ConfigPO.class)
                        .eq(ConfigPO::getBizCode, "shopName")
                        .last("limit 1"));

                shopName = shopTitle.getBizVal();
            }
            content = content.replace("${shopTitle}", shopName);
            content = content.replace("${userName}", user.getUsername());
            List<OrderItem> orderItems = orderItemMapper.selectList(Wrappers.lambdaQuery(OrderItem.class)
                    .eq(OrderItem::getOrderId, order.getOrderId()));

            if (CollUtil.isNotEmpty(orderItems)) {
                List<Integer> productIds = orderItems.stream().map(OrderItem::getProductId).toList();
                List<Product> products = productMapper.selectBatchIds(productIds);
                if (CollUtil.isNotEmpty(products)) {
                    String productNames = products.stream().map(Product::getProductName).collect(Collectors.joining("|"));
                    content = content.replace("${productName}", productNames + " ");
                }
            }
        }

        if (messageTypeEnum == MessageTypeEnum.NEW_ORDER_SHOP || messageTypeEnum == MessageTypeEnum.ORDER_PAY_SHOP) {
            content = content.replace("${order}", order.getOrderSn());
            content = content.replace("${fee}", order.getTotalAmount().setScale(2, RoundingMode.HALF_UP).toString());
        }
        //实名认证通知：（【商家名称】）亲爱的【用户昵称】，您的实名认证已（【通过/失败】），请前往商城查看！
        if (messageTypeEnum == MessageTypeEnum.USER_COMPANY_AUTH) {
            content = content.replace("${userNickName}", user.getUsername());
            content = content.replace("${submitTime}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ",");

        }
        return content;
    }
}
