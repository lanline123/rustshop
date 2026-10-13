package com.tigshop.adminapi.controller.wholesale;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tigshop.bean.dto.user.UserListDTO;
import com.tigshop.bean.dto.wholesale.WholesalerAddDTO;
import com.tigshop.bean.dto.wholesale.WholesalerImportDTO;
import com.tigshop.bean.dto.wholesale.WholesalerRemoveByGradeDTO;
import com.tigshop.bean.dto.wholesale.WholesalerRemoveDTO;
import com.tigshop.bean.query.wholesale.WholesalerPageQuery;
import com.tigshop.bean.vo.wholesale.WholesalerImportResultVO;
import com.tigshop.bean.vo.wholesale.WholesalerSelectableUserVO;
import com.tigshop.bean.vo.wholesale.WholesalerVO;
import com.tigshop.service.wholesale.WholesalerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 批发商管理（admin）。
 *
 * <p>接口契约见 api-contract §1.A1~A5。返回值直接为业务对象，由全局 ResponseAdvice 包装为 AjaxResult；
 * 异常通过 {@link com.tigshop.common.exception.GlobalException} 抛出并由全局处理器映射。
 *
 * <p>本控制器仅做参数绑定与委托，所有业务逻辑收敛在 {@link WholesalerService}。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Tag(name = "批发商管理")
@RestController
@RequestMapping("/adminapi/wholesale/wholesaler")
@RequiredArgsConstructor
@Validated
public class WholesalerController {

    private final WholesalerService wholesalerService;

    @Operation(summary = "批发商分页")
    @GetMapping("/page")
    public IPage<WholesalerVO> page(WholesalerPageQuery query) {
        return wholesalerService.page(query);
    }

    @Operation(summary = "可选为批发商的会员分页（含 isWholesaler 标识，添加批发商选会员用）")
    @GetMapping("/selectableUserPage")
    public IPage<WholesalerSelectableUserVO> selectableUserPage(UserListDTO query) {
        return wholesalerService.selectableUserPage(query);
    }

    @Operation(summary = "添加批发商")
    @PostMapping("/add")
    public Map<String, Integer> add(@Valid @RequestBody WholesalerAddDTO dto) {
        return Map.of("wholesalerId", wholesalerService.add(dto));
    }

    @Operation(summary = "批量导入批发商")
    @PostMapping("/import")
    public WholesalerImportResultVO importBatch(@Valid @RequestBody WholesalerImportDTO dto) {
        return wholesalerService.importBatch(dto);
    }

    @Operation(summary = "批发商启停")
    @PostMapping("/setStatus")
    public void setStatus(
            @Parameter(description = "批发商 ID") @RequestParam Integer wholesalerId,
            @Parameter(description = "状态 0=禁用 1=启用") @RequestParam Integer status) {
        wholesalerService.setStatus(wholesalerId, status);
    }

    @Operation(summary = "批发商改等级")
    @PostMapping("/changeGrade")
    public void changeGrade(
            @Parameter(description = "批发商 ID") @RequestParam Integer wholesalerId,
            @Parameter(description = "启用中的批发等级 ID") @RequestParam Integer gradeId) {
        wholesalerService.changeGrade(wholesalerId, gradeId);
    }

    @Operation(summary = "按等级移除批发商")
    @PostMapping("/removeByGrade")
    public Map<String, Integer> removeByGrade(@Valid @RequestBody WholesalerRemoveByGradeDTO dto) {
        return Map.of("removedCount", wholesalerService.removeByGrade(dto));
    }

    @Operation(summary = "按 ID 直接移除批发商身份")
    @PostMapping("/remove")
    public Map<String, Integer> remove(@Valid @RequestBody WholesalerRemoveDTO dto) {
        return Map.of("removedCount", wholesalerService.remove(dto));
    }
}
