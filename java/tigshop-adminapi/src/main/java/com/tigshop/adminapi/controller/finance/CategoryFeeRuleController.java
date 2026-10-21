// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRuleConflictCheckDTO;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRuleIdsDTO;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRulePageDTO;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRuleSaveDTO;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRuleUpdateStatusDTO;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleConflictVO;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleDetailVO;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleListVO;
import com.tigshop.common.annotation.Log;
import com.tigshop.service.finance.CategoryFeeRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 品类服务费率规则（平台）
 */
@RestController
@RequestMapping("/adminapi/finance/categoryFeeRule")
@Tag(name = "品类费率规则", description = "结算与分账-费率规则管理")
public class CategoryFeeRuleController {

    @Resource
    private CategoryFeeRuleService categoryFeeRuleService;

    @PostMapping("/list")
    @Operation(summary = "分页列表")
    @PreAuthorize("@pms.hasPermission('categoryFeeRuleManage')")
    public Page<CategoryFeeRuleListVO> list(@RequestBody @Valid CategoryFeeRulePageDTO dto) {
        return categoryFeeRuleService.page(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "详情")
    @PreAuthorize("@pms.hasPermission('categoryFeeRuleManage')")
    public CategoryFeeRuleDetailVO detail(@RequestParam Integer id) {
        return categoryFeeRuleService.detail(id);
    }

    @PostMapping("/save")
    @Operation(summary = "新建")
    @PreAuthorize("@pms.hasPermission('categoryFeeRuleUpdate')")
    @Log("创建费率规则: #{#dto.ruleName}")
    public List<String> save(@RequestBody @Valid CategoryFeeRuleSaveDTO dto) {
        return categoryFeeRuleService.save(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "编辑")
    @PreAuthorize("@pms.hasPermission('categoryFeeRuleUpdate')")
    @Log("编辑费率规则: #{#dto.ruleName}")
    public List<String> update(@RequestBody @Valid CategoryFeeRuleSaveDTO dto) {
        return categoryFeeRuleService.update(dto);
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "启用/禁用")
    @PreAuthorize("@pms.hasPermission('categoryFeeRuleUpdate')")
    @Log("更新费率规则状态 id=#{ #dto.id } status=#{ #dto.status }")
    public List<String> updateStatus(@RequestBody @Valid CategoryFeeRuleUpdateStatusDTO dto) {
        return categoryFeeRuleService.updateStatus(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('categoryFeeRuleUpdate')")
    @Log("删除费率规则 ids=#{ #dto.ids }")
    public void del(@RequestBody @Valid CategoryFeeRuleIdsDTO dto) {
        categoryFeeRuleService.delete(dto.getIds());
    }

    @PostMapping("/checkConflict")
    @Operation(summary = "冲突预检")
    @PreAuthorize("@pms.hasPermission('categoryFeeRuleManage')")
    public CategoryFeeRuleConflictVO checkConflict(@RequestBody @Valid CategoryFeeRuleConflictCheckDTO dto) {
        return categoryFeeRuleService.checkConflict(dto);
    }
}
