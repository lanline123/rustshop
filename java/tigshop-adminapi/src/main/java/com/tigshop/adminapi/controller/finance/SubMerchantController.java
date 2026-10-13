package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.finance.SubMerchantBindParam;
import com.tigshop.bean.query.finance.SubMerchantPageQuery;
import com.tigshop.bean.vo.finance.SubMerchantVO;
import com.tigshop.service.finance.SubMerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 子商户管理（财务 &gt; 微信线上分账 &gt; 子商户管理）
 *
 * @author Tigshop团队
 */
@Tag(name = "子商户管理", description = "子商户列表 / 绑定 / 解绑")
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/finance/subMerchant")
public class SubMerchantController {

    private final SubMerchantService subMerchantService;

    @Operation(summary = "子商户分页列表")
    @GetMapping("/list")
    public Page<SubMerchantVO> list(SubMerchantPageQuery query) {
        return subMerchantService.listPage(query);
    }

    @Operation(summary = "手动绑定子商户")
    @PreAuthorize("@pms.hasPermission('subMerchantBind')")
    @PostMapping("/bind")
    public void bind(@RequestBody @Validated SubMerchantBindParam param) {
        subMerchantService.bind(param);
    }

    @Operation(summary = "解绑子商户")
    @PreAuthorize("@pms.hasPermission('subMerchantUnbind')")
    @PostMapping("/unbind")
    public void unbind(@RequestParam Long id) {
        subMerchantService.unbind(id);
    }
}
