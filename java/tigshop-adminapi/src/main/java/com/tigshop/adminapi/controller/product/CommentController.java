// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.product.comment.CommentSaveParam;
import com.tigshop.bean.query.product.CommentListPageQuery;
import com.tigshop.bean.dto.product.CommentReplyDTO;
import com.tigshop.bean.dto.product.CommentUpdateDTO;
import com.tigshop.bean.vo.product.CommentVO;
import com.tigshop.service.product.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.COMMENT_FIELDS;
import static com.tigshop.common.utils.SecurityUtils.getCurrentAdminId;

/**
 * 评价管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/product/comment")
@Tag(name = "评价管理", description = "评价管理功能")
@PreAuthorize("@pms.hasPermission('commentManage')")
public class CommentController {
    @Resource
    private CommentService commentService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<CommentVO> list(CommentListPageQuery pageQuery) {
        return commentService.list(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public CommentVO detail(@RequestParam Integer id) {
        return commentService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('commentModifyManage')")
    public void create(@RequestBody @Validated CommentSaveParam param) {
        commentService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('commentModifyManage')")
    public void update(@RequestBody CommentUpdateDTO updateDTO) {
        commentService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('commentModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        commentService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('commentModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        commentService.updateField(updateField, COMMENT_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('commentModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        commentService.batch(batchDTO);
    }

    @PostMapping("/replyComment")
    @Operation(summary = "回复评论")
    @PreAuthorize("@pms.hasPermission('commentModifyManage')")
    public void replyComment(@RequestBody CommentReplyDTO dto) {
        Integer adminId = getCurrentAdminId();
        commentService.replyComment(dto, adminId);
    }
}
