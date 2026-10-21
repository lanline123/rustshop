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

package com.tigshop.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserMessageCreateDTO;
import com.tigshop.bean.query.user.UserMessageListPageQuery;
import com.tigshop.bean.model.user.UserMessage;
import com.tigshop.bean.vo.user.UserMessageVO;
import com.tigshop.service.common.BaseService;
import com.tigshop.bean.vo.user.UnreadMessageCountVO;

/**
 * 站内消息服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface UserMessageService extends BaseService<UserMessage> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<UserMessageVO> list(Integer userId, UserMessageListPageQuery listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    UserMessageVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(UserMessageCreateDTO createDTO);

    /**
     * 设置全部已读
     *
     * @param userId 用户ID
     * @return boolean
     */
    boolean setAllMessageRead(Integer userId);

    /**
     * 设置已读
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     * @return boolean
     */
    boolean setMessageRead(Integer userId, Integer messageId);

    /**
     * 删除
     *
     * @param id      主键
     */
    void deleteUserMessage(Integer id);

    /**
     * 获取未读消息统计
     *
     * @param userId 用户ID
     * @return 未读消息统计VO
     */
    UnreadMessageCountVO getUnreadMessageCount(Integer userId);

    /**
     * 设置订单消息全部已读
     *
     * @param userId 用户ID
     * @return boolean
     */
    boolean setOrderMessageRead(Integer userId);
}
