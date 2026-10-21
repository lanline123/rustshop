package com.tigshop.adminapi.controller.wholesale;

import com.tigshop.bean.dto.wholesale.WholesalerGradeSaveDTO;
import com.tigshop.bean.vo.wholesale.WholesalerGradeVO;
import com.tigshop.service.wholesale.WholesalerGradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 批发商等级管理（admin）。
 *
 * <p>接口契约见 api-contract §1.A6/A7/A8。返回值直接为业务对象，由全局 ResponseAdvice 包装为 AjaxResult。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Tag(name = "批发商等级管理")
@RestController
@RequestMapping("/adminapi/wholesale/grade")
@RequiredArgsConstructor
@Validated
public class WholesalerGradeController {

    private final WholesalerGradeService gradeService;

    @Operation(summary = "等级列表")
    @GetMapping("/list")
    public List<WholesalerGradeVO> list(
            @Parameter(description = "状态筛选 0=禁用 1=启用") @RequestParam(required = false) Integer status,
            @Parameter(description = "是否回填已绑定客户数") @RequestParam(defaultValue = "true") boolean withUserCount) {
        return gradeService.list(status, withUserCount);
    }

    @Operation(summary = "等级新增/编辑")
    @PostMapping("/save")
    @PreAuthorize("@pms.hasPermission('wholesalerGradeSaveManage')")
    public Map<String, Integer> save(@Valid @RequestBody WholesalerGradeSaveDTO dto) {
        return Map.of("gradeId", gradeService.save(dto));
    }

    @Operation(summary = "等级启停")
    @PostMapping("/setStatus")
    @PreAuthorize("@pms.hasPermission('wholesalerGradeSetStatusManage')")
    public void setStatus(@RequestParam Integer gradeId, @RequestParam Integer status) {
        gradeService.setStatus(gradeId, status);
    }

    @Operation(summary = "等级删除（软删；绑定批发商→5305 拦截，默认等级→5306 拦截）")
    @PostMapping("/delete")
    @PreAuthorize("@pms.hasPermission('wholesalerGradeDeleteManage')")
    public void delete(@RequestParam Integer gradeId) {
        gradeService.delete(gradeId);
    }
}
