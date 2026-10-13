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

package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.product.comment.CommentEvaluateParam;
import com.tigshop.bean.dto.product.CommentReplyDTO;
import com.tigshop.bean.dto.product.CommentUpdateDTO;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.bean.param.product.comment.CommentSaveParam;
import com.tigshop.bean.query.product.CommentListPageQuery;
import com.tigshop.bean.vo.product.CommentSubNumVO;
import com.tigshop.bean.vo.product.CommentVO;
import com.tigshop.bean.vo.product.ProductCommentStatisticVO;
import com.tigshop.service.common.BaseService;

/**
 * 评价管理服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface CommentService extends BaseService<Comment> {

    /**
     * 列表（后台评价管理用，不受商品评价开关影响，不脱敏）
     */
    Page<CommentVO> list(CommentListPageQuery pageQuery);

    /**
     * C 端评价列表（D1146）
     *
     * <p>与 {@link #list} 的区别：商品评价开关关闭时返回空分页，且匿名评价的用户名做脱敏。
     * 后台评价管理必须继续走 {@link #list}，否则关闭开关后历史评价将无法管理。
     */
    Page<CommentVO> listForClient(CommentListPageQuery pageQuery);

    /**
     * 商品评价功能是否开启（D1146，供 C 端接口守卫使用）
     */
    boolean isProductCommentEnabled();

    /**
     * 店铺评价列表
     * @param pageQuery
     * @return
     */
    Page<CommentVO> listByShopId(CommentListPageQuery pageQuery);

    /**
     * 详情
     */
    CommentVO detail(Integer id);

    /**
     * 创建
     */
    void create(CommentSaveParam param);

    /**
     * 更新
     */
    boolean update(CommentUpdateDTO updateDTO);

    /**
     * 统计商品评价
     */
    ProductCommentStatisticVO getProductCommentDetail(Integer productId, Integer shopId);

    /**
     * 评论晒单数量
     */
    CommentSubNumVO subNum(Integer userId);

    /**
     * 评论晒单
     */
    void evaluate(CommentEvaluateParam param);

    /**
     * 回复评论
     */
    boolean replyComment(CommentReplyDTO dto, Integer adminId);
}
