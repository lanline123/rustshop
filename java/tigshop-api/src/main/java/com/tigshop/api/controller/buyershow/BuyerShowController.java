package com.tigshop.api.controller.buyershow;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.BuyerShowGroup;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentClientParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentLikeClientParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowFavoriteClientParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowFollowAuthorClientParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowLikeClientParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowSaveClientParam;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowCommentPageClientQuery;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowPageClientQuery;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowVideoFeedClientQuery;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowClientVO;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowCommentPageClientVO;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowPageClientVO;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowVideoFeedPageClientVO;
import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.service.promotion.BuyerShowGroupService;
import com.tigshop.service.promotion.BuyerShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 买家秀
 *
 * @author wzh
 */
@RestController
@RequestMapping("/api/buyerShow/buyerShow")
@Tag(name = "买家秀")
@Validated
@RequiredArgsConstructor
public class BuyerShowController {

    private final BuyerShowService buyerShowService;

    private final BuyerShowGroupService buyerShowGroupService;

    @GetMapping("/list")
    @Operation(summary = "获取买家秀列表")
    public Page<BuyerShowPageClientVO> clientList(BuyerShowPageClientQuery query) {
        return buyerShowService.clientList(query);
    }

    @GetMapping("/videoFeed")
    @Operation(summary = "买家秀视频流（只返回含视频的买家秀，游标翻页）")
    public BuyerShowVideoFeedPageClientVO videoFeed(@Valid BuyerShowVideoFeedClientQuery query) {
        return buyerShowService.videoFeed(query);
    }

    @PostMapping("/like")
    @Operation(summary = "买家秀点赞/取消点赞")
    public void like(@Valid @RequestBody BuyerShowLikeClientParam param) {
        buyerShowService.like(param);
    }

    @PostMapping("/favorite")
    @Operation(summary = "买家秀收藏/取消收藏")
    public void favorite(@Valid @RequestBody BuyerShowFavoriteClientParam param) {
        buyerShowService.favorite(param);
    }

    @PostMapping("/followAuthor")
    @Operation(summary = "关注/取关买家秀作者")
    public void followAuthor(@Valid @RequestBody BuyerShowFollowAuthorClientParam param) {
        buyerShowService.followAuthor(param);
    }

    @PostMapping("/add")
    @Operation(summary = "添加买家秀信息")
    public void clientAdd(@Valid @RequestBody BuyerShowSaveClientParam param) {
        buyerShowService.clientAdd(param);
    }

    @GetMapping("/groupList")
    @Operation(summary = "分组列表")
    public List<BuyerShowGroup> groupList() {
        return buyerShowGroupService.list();
    }

    @GetMapping("/detail")
    @Operation(summary = "获取买家秀详情")
    public BuyerShowClientVO clientDetail(@RequestParam(value = "buyerShowId") @NotNull(message = "买家秀主键不能为空") Integer buyerShowId) {
        return buyerShowService.clientDetail(buyerShowId);
    }

    /**
     * 买家秀分享落地页
     * <p>
     * 返回带 Open Graph 标签的 HTML 供社交平台抓取预览，访客自动跳转到 H5 买家秀详情页。
     */
    @IgnoreResponseAdvice
    @GetMapping("/share")
    @Operation(summary = "买家秀分享落地页")
    public void sharePage(@RequestParam("buyerShowId") @NotNull(message = "买家秀主键不能为空") Integer buyerShowId,
                          HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(buyerShowService.getSharePageHtml(buyerShowId));
    }

    @PostMapping("/comment")
    @Operation(summary = "评论买家秀信息")
    public void clientComment(@Valid @RequestBody BuyerShowCommentClientParam param) {
        buyerShowService.clientComment(param);
    }

    @GetMapping("/comment/pageList")
    @Operation(summary = "评论分页列表")
    public Page<BuyerShowCommentPageClientVO> clientCommentPageList(BuyerShowCommentPageClientQuery query) {
        return buyerShowService.clientCommentPageList(query);
    }


    @PostMapping("/comment/like")
    @Operation(summary = "买家秀评论点赞/取消点赞")
    public void commentLike(@Valid @RequestBody BuyerShowCommentLikeClientParam param) {
        buyerShowService.commentLike(param);
    }

}
