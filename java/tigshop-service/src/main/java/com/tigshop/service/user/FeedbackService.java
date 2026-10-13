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
import com.tigshop.bean.query.user.FeedBackListPageQuery;
import com.tigshop.bean.dto.user.FeedbackCreateDTO;
import com.tigshop.bean.dto.user.FeedbackUpdateDTO;
import com.tigshop.bean.model.user.Feedback;
import com.tigshop.bean.vo.user.UserFeedbackDetailVO;
import com.tigshop.bean.vo.user.UserFeedbackVO;
import com.tigshop.service.common.BaseService;

/**
 * 会员留言
 *
 * @author Tigshop
 */
public interface FeedbackService extends BaseService<Feedback> {
    /**
     * 列表
     */
    Page<UserFeedbackVO> list(FeedBackListPageQuery pageQuery);

    /**
     * 后台列表（应用当前后台租户与留言类型权限）
     */
    Page<UserFeedbackVO> listForAdmin(FeedBackListPageQuery pageQuery);

    /**
     * 商品咨询列表（按商品最终归属店铺隔离，只返回根留言）
     */
    Page<UserFeedbackVO> listProductConsultations(FeedBackListPageQuery pageQuery);

    /**
     * 会员留言详情
     *
     * @param id 会员留言ID
     * return 会员留言详情
     */
    UserFeedbackDetailVO detail(Integer id);

    /**
     * 提交留言
     * @param dto 留言信息
     * @return boolean
     */
    boolean submit(FeedbackCreateDTO dto);

    /**
     * 后台回复留言
     * @param dto 回复信息
     * @return boolean
     */
    boolean update(FeedbackUpdateDTO dto);

}
