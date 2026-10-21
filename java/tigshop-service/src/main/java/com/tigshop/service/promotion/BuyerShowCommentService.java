package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.BuyerShowComment;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentAuditParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentIsTopParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentShowOrHideParam;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowCommentPageQuery;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowCommentPageVO;
import com.tigshop.service.common.BaseService;

/**
* @author wzh
*/
public interface BuyerShowCommentService extends BaseService<BuyerShowComment> {

    /**
     * 获取买家秀评论列表
     *
     * @param query 查询参数
     * @return 买家秀评论列表
     */
    Page<BuyerShowCommentPageVO> list(BuyerShowCommentPageQuery query);


    /**
     * 审核（通过、拒绝）
     *
     * @param param 审核参数
     */
    void audit(BuyerShowCommentAuditParam param);

    /**
     * 显示或隐藏
     *
     * @param param 显示或隐藏参数
     */
    void showOrHide(BuyerShowCommentShowOrHideParam param);


    /**
     * 是否置顶
     *
     * @param param 是否置顶参数
     */
    void isTop(BuyerShowCommentIsTopParam param);

}
