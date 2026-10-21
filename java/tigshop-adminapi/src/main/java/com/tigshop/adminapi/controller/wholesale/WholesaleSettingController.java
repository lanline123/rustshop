package com.tigshop.adminapi.controller.wholesale;

import com.tigshop.bean.dto.wholesale.WholesaleSettingSaveDTO;
import com.tigshop.bean.vo.wholesale.WholesaleSettingVO;
import com.tigshop.service.wholesale.WholesaleSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 批发设置（admin）。
 *
 * <p>接口契约见 api-contract §1.A10/A11。返回值直接为业务对象，由全局 ResponseAdvice 包装为 AjaxResult；
 * 异常通过 {@link com.tigshop.common.exception.GlobalException} 抛出并由全局处理器映射。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Tag(name = "批发设置")
@RestController
@RequestMapping("/adminapi/wholesale/setting")
@RequiredArgsConstructor
@Validated
public class WholesaleSettingController {

    private final WholesaleSettingService settingService;

    @Operation(summary = "查询批发设置")
    @GetMapping("/get")
    public WholesaleSettingVO get() {
        return settingService.get();
    }

    @Operation(summary = "保存批发设置")
    @PostMapping("/save")
    public void save(@Valid @RequestBody WholesaleSettingSaveDTO dto) {
        settingService.save(dto);
    }
}
