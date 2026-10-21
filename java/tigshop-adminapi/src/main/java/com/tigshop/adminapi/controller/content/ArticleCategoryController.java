package com.tigshop.adminapi.controller.content;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.content.ArticleCategoryEditParam;
import com.tigshop.bean.param.content.ArticleCategorySaveParam;
import com.tigshop.bean.query.content.ArticleCategoryPageQuery;
import com.tigshop.bean.vo.content.ArticleCategoryVO;
import com.tigshop.service.content.ArticleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.ARTICLE_CATEGORY_FIELDS;

/**
 * 文章分类 控制器
 *
 * @author kidd
 * @create 2025/7/4
 */
@RequiredArgsConstructor
@Tag(name = "文章分类")
@RestController
@PreAuthorize("@pms.hasPermission('articleCategoryManage')")
@RequestMapping("/adminapi/content/articleCategory")
public class ArticleCategoryController {

    private final ArticleCategoryService articleCategoryService;

    @Operation(summary = "文章分类列表")
    @GetMapping("/list")
    public Page<ArticleCategoryVO> list(ArticleCategoryPageQuery pageQuery) {
        return articleCategoryService.list(pageQuery);
    }

    @Operation(summary = "文章分类详情")
    @GetMapping("/detail")
    public ArticleCategoryVO detail(@RequestParam("id") Integer id){
        return articleCategoryService.detail(id);
    }

    @Operation(summary = "创建文章分类")
    @PreAuthorize("@pms.hasPermission('articleCategoryModifyManage')")
    @PostMapping("/create")
    public void create(@RequestBody @Validated ArticleCategorySaveParam param) {
        articleCategoryService.create(param);
    }

    @Operation(summary = "更新文章分类")
    @PreAuthorize("@pms.hasPermission('articleCategoryModifyManage')")
    @PostMapping("/update")
    public void update(@RequestBody @Validated ArticleCategoryEditParam param) {
        articleCategoryService.update(param);
    }

    @PostMapping("/updateField")
    @PreAuthorize("@pms.hasPermission('articleCategoryModifyManage')")
    @Operation(summary = "更新文章分类字段")
    public void updateField(@RequestBody @Validated UpdateFieldDTO updateField) {
        articleCategoryService.updateField(updateField, ARTICLE_CATEGORY_FIELDS);
    }

    @Operation(summary = "删除文章分类")
    @PreAuthorize("@pms.hasPermission('articleCategoryModifyManage')")
    @PostMapping("/del")
    public void del(@RequestBody @Validated OperateDTO operate){
        articleCategoryService.del(operate.getId());
    }

    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('articleCategoryModifyManage')")
    @PostMapping("/batch")
    public void batch(@RequestBody @Validated BatchDTO batch) {
        articleCategoryService.batch(batch);
    }

    @Operation(summary = "文章分类树")
    @GetMapping("/tree")
    public List<Tree<Integer>> tree() {
        return articleCategoryService.getArticleCategoryTree();
    }
}
