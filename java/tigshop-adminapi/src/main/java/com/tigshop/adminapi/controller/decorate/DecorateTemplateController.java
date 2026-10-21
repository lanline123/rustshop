package com.tigshop.adminapi.controller.decorate;

import com.tigshop.service.decorate.DecorateTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 装修模板库控制器（转发官网开放接口）
 * <p>
 * 仅做薄转发，业务数据保持 snake_case 原样返回，前端自行适配。
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/decorate/template")
@Tag(name = "装修模板库")
public class DecorateTemplateController {

    @Resource
    private DecorateTemplateService decorateTemplateService;

    @GetMapping("/category")
    @Operation(summary = "模板分类树")
    public Map<String, Object> category() {
        return decorateTemplateService.category();
    }

    @GetMapping("/list")
    @Operation(summary = "模板页面列表")
    public Map<String, Object> list(@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                    @RequestParam(value = "pageType", required = false, defaultValue = "") String pageType,
                                    @RequestParam(value = "categoryIds", required = false, defaultValue = "") String categoryIds,
                                    @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                    @RequestParam(value = "size", required = false, defaultValue = "15") Integer size) {
        return decorateTemplateService.list(keyword, pageType, categoryIds, page, size);
    }

    @GetMapping("/detail")
    @Operation(summary = "模板页面详情")
    public Map<String, Object> detail(@RequestParam("sn") String sn) {
        return decorateTemplateService.detail(sn);
    }
}
