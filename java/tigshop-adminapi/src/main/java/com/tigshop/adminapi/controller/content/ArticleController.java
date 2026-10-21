package com.tigshop.adminapi.controller.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.content.ArticleBatchParam;
import com.tigshop.bean.param.content.ArticleEditParam;
import com.tigshop.bean.param.content.ArticleSaveParam;
import com.tigshop.bean.query.content.ArticlePageQuery;
import com.tigshop.bean.vo.content.ArticleDetailVO;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.service.content.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.ARTICLE_FIELDS;
import static com.tigshop.common.constant.ResultTextConstants.ID_CANNOT_IS_NULL;

/**
 * 文章控制器
 *
 * @author kidd
 * @create 2025/7/4
 */
@RequiredArgsConstructor
@Tag(name = "文章")
@RestController
@PreAuthorize("@pms.hasPermission('articleManage')")
@RequestMapping("/adminapi/content/article")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/list")
    @Operation(summary = "文章列表")
    @PreAuthorize("@pms.hasPermission('decorate','articleManage')")
    public Page<ArticleVO> list(ArticlePageQuery pageQuery) {
        return articleService.list(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "文章详情")
    public ArticleDetailVO detail(@RequestParam("id") @NotNull(message = ID_CANNOT_IS_NULL) Integer id){
        return articleService.detail(id);
    }

    @Operation(summary = "创建文章")
    @PreAuthorize("@pms.hasPermission('articleModifyManage')")
    @PostMapping("/create")
    public void create(@RequestBody @Validated ArticleSaveParam param) {
        articleService.create(param);
    }

    @Operation(summary = "更新文章")
    @PreAuthorize("@pms.hasPermission('articleModifyManage')")
    @PostMapping("/update")
    public void update(@RequestBody @Validated ArticleEditParam param) {
        articleService.update(param);
    }

    @Operation(summary = "删除文章")
    @PreAuthorize("@pms.hasPermission('articleModifyManage')")
    @PostMapping("/del")
    public void del(@RequestBody @Validated OperateDTO operate){
        articleService.del(operate.getId());
    }

    @Operation(summary = "更新文章字段")
    @PreAuthorize("@pms.hasPermission('articleModifyManage')")
    @PostMapping("/updateField")
    public void updateField(@RequestBody @Validated UpdateFieldDTO updateField) {
        articleService.updateField(updateField, ARTICLE_FIELDS);
    }

    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('articleModifyManage')")
    @PostMapping("/batch")
    public void batch(@RequestBody @Validated ArticleBatchParam param) {
        articleService.batch(param);
    }

}
