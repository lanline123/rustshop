// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.msgTemplate;


import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.enums.msg.MessageTypeEnum;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/5/26 17:14
 */
public interface MessageService {
    /**
     * 发送消息到具体的渠道
     * @param message
     */
    void sendMessage(Message message);

    /**
     * 发送 四个端的消息
     * @param userId
     * @param orderId
     * @param messageTypeEnum
     * @param totalRefund
     */
    void sendUserMessage(Integer userId, Integer orderId, MessageTypeEnum messageTypeEnum, BigDecimal totalRefund);

    /**
     * 发送 四个端的消息
     * @param userId
     * @param orderId
     * @param messageTypeEnum
     */
    void sendUserMessage(Integer userId, Integer orderId, MessageTypeEnum messageTypeEnum);

    void sendMerchantApplyResultMessage(Integer merchantApplyId, MessageTypeEnum messageTypeEnum,String userName,String password);
}
