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

package com.tigshop.service.im;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.im.*;
import com.tigshop.bean.model.im.ImMessage;
import com.tigshop.bean.vo.im.ImMessageListVO;
import com.tigshop.bean.vo.im.ImMessageSendVO;
import com.tigshop.bean.vo.im.ImMessageVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 会话消息服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ImMessageService extends BaseService<ImMessage> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return Page
     */
    Page<ImMessageListVO> list(ImMessageListDTO listDTO);

    /**
     * 客服消息播报记录列表（语音播报「播报记录」抽屉-客服消息 tab，需求 #646738）
     * <p>
     * 拉取指定客服/店铺收到的「用户发来的消息」（type=1、status=1 正常），按 send_time 倒序分页，
     * 口径与 SSE customerMsgCount（{@link #getReceivedImMessageCount}）一致。
     *
     * @param dto       分页参数
     * @param shopId    店铺 ID（由 Controller 从请求头注入，可空表示不限店铺）
     * @param servantId 当前登录客服（管理员）ID
     * @return Page
     */
    Page<ImMessageListVO> receivedList(ImMessageReceivedListDTO dto, Integer shopId, Integer servantId);

    /**
     * 详情
     *
     * @param id 主键
     * @return ImMessageVO
     */
    ImMessageVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ImMessageCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ImMessageUpdateDTO updateDTO);

    /**
     * 根据会话ID获取消息列表
     * @param conversationIds 会话ID集合
     * @return List
     */
    List<ImMessageVO> getListByConversationIds(List<Integer> conversationIds);

    /**
     * 获取未读消息数量
     * @param conversationId 会话ID
     * @return long
     */
    long getUnreadMessageCount(Integer conversationId);

    /**
     * 获取未读消息数量
     * @param conversationId 会话ID
     * @param type 消息类型，默认为1
     * @return long
     */
    long getUnreadMessageCount(Integer conversationId, Integer type);

    /**
     * 根据会话ID获取消息列表
     * @param conversationId 会话ID
     * @return List
     */
    List<ImMessageVO> getListByConversationIdLimitTwo(Integer conversationId);

    /**
     * 发送消息
     * @param dto 发送参数
     * @return ImMessageVO
     */
    ImMessageSendVO send(ImMessageSendDTO dto);

    /**
     * 未读消息数量
     *
     * @param shopId      店铺id
     * @param adminUserId 管理员id
     * @return Long
     */
    Long getUnreadImMessageCount(Integer shopId, Integer adminUserId);

    /**
     * 获取自 startTime 起收到的客服消息累计数量（用户发给客服的消息）
     * <p>
     * 注：本计数用于语音播报的增量判断，必须单调递增，故不带 isRead 过滤，
     * 仅按 sendTime &gt;= startTime 累计，避免已读后计数回落导致后续新消息漏播。
     *
     * @param shopId      店铺 ID
     * @param adminUserId 客服（管理员）ID
     * @param startTime   起始时间（秒级 epoch）
     * @return 数量
     */
    Long getReceivedImMessageCount(Integer shopId, Integer adminUserId, Long startTime);

    /**
     * 统计客服发送的未读消息数量（type=2 且 isRead=0）
     *
     * @return Long
     */
    Long countUnreadServantMessages();

    /**
     * 统计指定用户接收的客服未读消息数量（type=2 且 isRead=0）
     *
     * @param userId 用户ID
     * @return Long
     */
    Long countUnreadServantMessagesByUserId(Integer userId);

    /**
     * 设置已读
     * @param dto cs
     * @return boolean
     */
    boolean setRead(ImSetReadDTO dto);

    /**
     * 自动转接
     * @param conversationId 会话id
     * @param oldServantId 就客服id
     *  @param servantId 就客服id
     * @return boolean
     */
    boolean autoTransfer(Integer conversationId, Integer servantId, Integer oldServantId);

    /**
     * 自动欢迎
     * @param conversationId 会话id
     * @return boolean
     */
    boolean autoWelcome(Integer conversationId);

    /**
     * 自动等待
     * @param conversationId 会话id
     * @return boolean
     */
     boolean autoWaiting(Integer conversationId);

    /**
     * 自动离线
     * @param conversationId 会话id
     * @return boolean
     */
     boolean autoOffWork(Integer conversationId);

    /**
     * 自动发送消息
     * @param autoSend
     */
     void sendAutoMessage(AutoSendDTO autoSend);

    /**
     * 将所有未读消息标记为已读（针对客服发送的消息）
     * @param userId 用户ID
     * @return boolean
     */
    boolean setAllRead(Integer userId);
}
