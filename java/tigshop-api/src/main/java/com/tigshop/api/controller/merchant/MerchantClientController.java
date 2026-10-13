package com.tigshop.api.controller.merchant;

import com.tigshop.bean.param.merchant.MerchantApplyApplyParam;
import com.tigshop.bean.vo.merchant.MerchantApplyDetailVO;
import com.tigshop.bean.vo.merchant.MerchantMyApplyVO;
import com.tigshop.service.merchant.MerchantApplyService;
import com.tigshop.service.merchant.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商户控制器
 * @author Jayce
 * @create 2024/9/30 15:56
 */
@RestController
@RequestMapping("/api/merchant/merchant")
@Tag(name = "商户")
@Validated
public class MerchantClientController {
    @Resource
    private MerchantService merchantService;

    @Resource
    private MerchantApplyService merchantApplyService;

    @PostMapping("/uploadLicense")
    @Operation(summary = "入驻营业执照专用上传", description = "添加公示水印，不进入相册，返回图片地址保存至 merchantData.businessLicenseImg")
    public String uploadLicense(@RequestParam("file") MultipartFile file) {
        return merchantService.uploadLicensePic(file);
    }

    @GetMapping("/applyShopAgreement")
    @Operation(summary = "申请入驻协议")
    public String applyShopAgreement(){
        return merchantService.applyShopAgreement();
    }

    @GetMapping("/myApply")
    @Operation(summary = "我的申请")
    public MerchantMyApplyVO myApply(){
        return merchantApplyService.myApply();
    }

    @GetMapping("/applyDetail")
    @Operation(summary = "申请详情")
    public MerchantApplyDetailVO applyDetail(@RequestParam Integer id){
        return merchantApplyService.applyDetail(id);
    }

    @PostMapping("/apply")
    @Operation(summary = "申请入驻")
    public MerchantApplyDetailVO apply(@RequestBody @Validated MerchantApplyApplyParam param){
        return merchantApplyService.apply(param);
    }

}
