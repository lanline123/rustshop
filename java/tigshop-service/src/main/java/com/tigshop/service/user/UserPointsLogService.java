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
import com.tigshop.bean.dto.user.UserPointsLogDTO;
import com.tigshop.bean.query.user.UserPointsLogListPageQuery;
import com.tigshop.bean.model.user.UserPointsLog;
import com.tigshop.bean.vo.user.UserPointsLogPageVO;
import com.tigshop.service.common.BaseService;

/**
 * @author Tigshop团队
 */
public interface UserPointsLogService extends BaseService<UserPointsLog> {

    /**
     * 获取会员积分
     */
    Integer getPoints();

    /**
     * 会员等级列表
     */
    Page<UserPointsLogDTO> list(UserPointsLogListPageQuery dto);

    /**
     * 增加积分
     */
    void incPoints(Integer usePoints, Integer userId, String desc, Integer relationId);

    /**
     * 增加积分
     */
    void subPoints(Integer usePoints, Integer userId, String desc, Integer relationId);

    /**
     * 积分记录列表
     */
    UserPointsLogPageVO listPage(UserPointsLogListPageQuery pageQuery);
}
