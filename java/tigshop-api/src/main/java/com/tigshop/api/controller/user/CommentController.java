package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.product.comment.CommentEvaluateParam;
import com.tigshop.bean.query.order.OrderListPageQuery;
import com.tigshop.bean.query.product.CommentListPageQuery;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.product.CommentSubNumVO;
import com.tigshop.bean.vo.product.CommentVO;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.product.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 用户评论晒单
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(("/api/user/comment"))
@Tag(name = "前台用户评论晒单")
public class CommentController {

    private final CommentService commentService;

    private final OrderService orderService;

    @GetMapping("/subNum")
    @Operation(summary = "评论晒单数量")
    public CommentSubNumVO subNum(){
        Integer userId = getCurrentUserId();
        return commentService.subNum(userId) ;
    }

    @GetMapping("/showedList")
    @Operation(summary = "晒单列表")
    public Page<OrderVO> showedList(OrderListPageQuery listDTO){
        // D1146：评价功能关闭时返空。此处在 controller 守卫而非 orderService.list()，
        // 因为后者是通用订单列表，被大量非评价场景复用。
        if (!commentService.isProductCommentEnabled()) {
            return new Page<>();
        }
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        return orderService.list(listDTO);
    }

    @GetMapping("/list")
    @Operation(summary = "已评价列表")
    public Page<CommentVO> list(CommentListPageQuery listDTO){
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        return commentService.listForClient(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "评论详情")
    public OrderVO detail(@RequestParam Integer id){
        Integer userId = getCurrentUserId();
        return orderService.detail(id, userId);
    }

    @PostMapping("/evaluate")
    @Operation(summary = "商品评价 / 晒单")
    public void evaluate(@RequestBody @Valid CommentEvaluateParam param){
        commentService.evaluate(param);
    }
}
