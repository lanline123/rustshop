package com.tigshop.adminapi.controller.decorate;

import com.tigshop.bean.dto.decorate.DecorateDiscreteMenuItemDTO;
import com.tigshop.bean.param.decorate.DecorateDiscreteUpdateParam;
import com.tigshop.bean.vo.decorate.DecorateDiscreteVO;
import com.tigshop.bean.vo.decorate.DecorateMemberDataVO;
import com.tigshop.service.decorate.DecorateDiscreteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 装修组件控制器
 *
 * @author Kidd
 * @since 2025/6/30
 */
@RequiredArgsConstructor
@Tag(name = "装修模块管理")
@RestController
@PreAuthorize("@pms.hasPermission('pcDecorateOtherManage','mobileShopDecorate')")
@RequestMapping("/adminapi/decorate/decorateDiscrete")
public class DecorateDiscreteController {

    private final DecorateDiscreteService decorateDiscreteService;

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public DecorateDiscreteVO detail(@RequestParam(value = "decorateSn") String decorateSn) {
        return decorateDiscreteService.detail(decorateSn);
    }

    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('decorateDiscreteModifyManage')")
    @PostMapping("/update")
    public void update(@RequestBody @Validated DecorateDiscreteUpdateParam param) {
        decorateDiscreteService.update(param);
    }

    @Operation(summary = "个人中心基础数据")
    @GetMapping("/memberDecorateData")
    public DecorateMemberDataVO<List<DecorateDiscreteMenuItemDTO>> memberDecorateData() {
        return decorateDiscreteService.getMemberDecorateData();
    }

}
