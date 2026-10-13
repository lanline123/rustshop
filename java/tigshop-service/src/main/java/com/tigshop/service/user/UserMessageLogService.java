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
import com.tigshop.bean.query.user.UserMessageListPageQuery;
import com.tigshop.bean.dto.user.UserMessageLogCreateDTO;
import com.tigshop.bean.model.user.UserMessageLog;
import com.tigshop.bean.param.user.UserMessageLogRecallParam;
import com.tigshop.bean.vo.user.UserMessageLogPageVO;
import com.tigshop.service.common.BaseService;

/**
 * @author Tigshop团队
 */
public interface UserMessageLogService extends BaseService<UserMessageLog> {

    /**
     * 获取用户消息列表
     */
    Page<UserMessageLogPageVO> list(UserMessageListPageQuery pageQuery);

    /**
     * 获取用户消息详细信息
     *
     * @param id 用户消息ID，用于查询特定用户消息的详细信息
     * @return 用户消息数据传输对象，包含用户消息详细信息
     */
    UserMessageLog detail(Integer id);

    /**
     * 创建新用户消息
     *
     * @param dto 用户消息数据传输对象，包含创建新用户消息所需的信息
     * @return 创建操作是否成功的布尔值
     */
    boolean create(UserMessageLogCreateDTO dto);

    /**
     * 撤回用户消息
     *
     */
    void recall(UserMessageLogRecallParam param);

}
