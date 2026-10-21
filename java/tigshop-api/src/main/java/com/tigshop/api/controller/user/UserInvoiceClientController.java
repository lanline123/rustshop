package com.tigshop.api.controller.user;

import com.tigshop.bean.dto.finance.UserInvoiceCreateDTO;
import com.tigshop.bean.dto.finance.UserInvoiceUpdateDTO;
import com.tigshop.bean.vo.finance.UserInvoiceVO;
import com.tigshop.service.finance.UserInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 增票资质发票
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/invoice"))
@Tag(name = "前台用户增票资质发票")
@Validated

public class UserInvoiceClientController {
    @Resource
    UserInvoiceService userInvoiceService;

    @GetMapping("/getStatus")
    @Operation(summary = "判断当前用户的增票资质是否审核通过")
    public UserInvoiceVO getStatus() {
        return userInvoiceService.getStatus();
    }

    @GetMapping("/detail")
    @Operation(summary = "增票资质详情")
    public UserInvoiceVO detail() {
        return userInvoiceService.clientDetail();
    }

    @PostMapping("/create")
    @Operation(summary = "增票资质创建")
    public void create(@RequestBody UserInvoiceCreateDTO createDTO) {
        userInvoiceService.clientCreate(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "增票资质修改")
    public void update(@RequestBody UserInvoiceUpdateDTO updateDTO) {
        userInvoiceService.clientUpdate(updateDTO);
    }

}
