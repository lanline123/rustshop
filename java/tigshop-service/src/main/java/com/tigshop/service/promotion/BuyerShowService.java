package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.BuyerShow;
import com.tigshop.bean.param.promotion.buyershow.*;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowCommentPageClientQuery;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowPageClientQuery;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowPageQuery;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowVideoFeedClientQuery;
import com.tigshop.bean.vo.promotion.buyershow.*;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
* @author wzh

*/
public interface BuyerShowService extends BaseService<BuyerShow> {

    /**
     * 新增买家秀
     *
     * @param param 新增参数
     */
    void create(BuyerShowSaveParam param);

    void info() throws Exception;

    /**
     * 获取买家信息（随机）
     *
     * @return 买家信息对象
     */
    BuyerVO getBuyerInfo();


    /**
     * 买家秀列表
     *
     * @param query 查询参数
     * @return 买家秀列表
     */
    Page<BuyerShowPageVO> list(BuyerShowPageQuery query);


    /**
     * 获取买家秀详情
     *
     * @param buyerShowId 买家秀ID
     * @return 买家秀详情对象
     */
    BuyerShowVO detail(Integer buyerShowId);

    /**
     * 修改买家秀
     *
     * @param param 修改参数
     */
    void update(BuyerShowUpdateParam param);

    /**
     * 删除已审核的买家秀及其关联数据
     *
     * @param id 买家秀ID
     * @return 删除结果
     */
    @Override
    boolean del(Integer id);

    /**
     * 批量删除已审核的买家秀及其关联数据
     *
     * @param ids 买家秀ID列表
     * @return 删除结果
     */
    boolean batchDel(List<Integer> ids);

    /**
     * 审核（通过、拒绝）
     *
     * @param param 审核参数
     */
    void audit(BuyerShowAuditParam param);

    /**
     * 显示或隐藏
     *
     * @param param 显示或隐藏参数
     */
    void showOrHide(BuyerShowShowOrHideParam param);

    /**
     * 评论
     *
     * @param param 评论参数
     */
    void comment(BuyerShowCommentParam param);


    /**
     * 买家秀列表（客户端）
     *
     * @param query 查询参数
     * @return 买家秀列表
     */
    Page<BuyerShowPageClientVO> clientList(BuyerShowPageClientQuery query);

    /**
     * 买家秀视频流（客户端，只返回视频类买家秀，游标翻页）
     *
     * @param query 查询参数
     * @return 视频流分页结果
     */
    BuyerShowVideoFeedPageClientVO videoFeed(BuyerShowVideoFeedClientQuery query);

    /**
     * 点赞/取消点赞
     *
     * @param param 点赞参数
     */
    void like(BuyerShowLikeClientParam param);

    /**
     * 收藏/取消收藏买家秀（客户端）
     *
     * @param param 收藏参数
     */
    void favorite(BuyerShowFavoriteClientParam param);

    /**
     * 关注/取关作者（客户端）
     *
     * @param param 关注参数
     */
    void followAuthor(BuyerShowFollowAuthorClientParam param);

    /**
     * 新增买家秀（客户端）
     *
     * @param param 新增参数
     */
    void clientAdd(BuyerShowSaveClientParam param);

    /**
     * 获取买家秀详情（客户端）
     *
     * @param buyerShowId 买家秀ID
     * @return 买家秀详情对象
     */
    BuyerShowClientVO clientDetail(Integer buyerShowId);

    /**
     * 获取买家秀分享落地页 HTML
     * <p>
     * 返回带 Open Graph 标签的 HTML 供社交平台（Facebook 等）抓取链接预览卡，
     * 真实访客访问后由落地页自动跳转到 H5 买家秀详情页。
     *
     * @param buyerShowId 买家秀ID
     * @return 落地页 HTML
     */
    String getSharePageHtml(Integer buyerShowId);


    /**
     * 评论（客户端）
     *
     * @param param 评论参数
     */
    void clientComment(BuyerShowCommentClientParam param);

    /**
     * 评论列表（客户端）
     *
     * @param query 评论列表查询参数
     * @return 评论列表
     */
    Page<BuyerShowCommentPageClientVO> clientCommentPageList(BuyerShowCommentPageClientQuery query);



    /**
     * 评论点赞/取消点赞（客户端）
     *
     * @param param 点赞参数
     */
    void commentLike(BuyerShowCommentLikeClientParam param);

}
