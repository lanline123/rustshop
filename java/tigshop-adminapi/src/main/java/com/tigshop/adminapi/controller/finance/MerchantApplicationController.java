package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.finance.MerchantApplicationAuditParam;
import com.tigshop.bean.param.finance.MerchantApplicationSubmitParam;
import com.tigshop.bean.query.finance.MerchantApplicationPageQuery;
import com.tigshop.bean.vo.finance.MerchantApplicationVO;
import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.service.finance.MerchantApplicationMediaService;
import com.tigshop.service.finance.MerchantApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 商户进件管理
 *
 * <p>店铺/门店/供应商端：提交申请 apply / 查看当前申请 current / 上传媒体 uploadMedia。
 * <p>平台端：审核列表 list / 详情 detail / 审核通过 approve / 审核拒绝 reject / 刷新状态 refresh。
 *
 * @author Tigshop团队
 */
@Tag(name = "进件管理", description = "微信特约商户进件 申请 / 审核 / 详情 / 刷新")
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/finance/merchantApplication")
public class MerchantApplicationController {

    private final MerchantApplicationService merchantApplicationService;

    // ============ 平台端：审核 ============
    // 注：店铺/供应商主账号的授权列表是 all，会直接通过 @pms.hasPermission，
    // 因此 list / detail / media 除注解外，服务层还会按登录主体做归属收口。

    @Operation(summary = "进件申请分页列表（平台审核列表）")
    @PreAuthorize("@pms.hasPermission('merchantApplicationManage')")
    @GetMapping("/list")
    public Page<MerchantApplicationVO> list(MerchantApplicationPageQuery query) {
        return merchantApplicationService.listPage(query);
    }

    @Operation(summary = "平台直接提交进件（进件管理）")
    @PreAuthorize("@pms.hasPermission('merchantApplicationSubmit')")
    @PostMapping("/submit")
    public MerchantApplicationVO submit(@RequestBody @Validated MerchantApplicationSubmitParam param) {
        return merchantApplicationService.submit(param);
    }

    @Operation(summary = "进件申请详情")
    @PreAuthorize("@pms.hasPermission('merchantApplicationManage')")
    @GetMapping("/detail")
    public MerchantApplicationVO detail(@RequestParam Long id) {
        return merchantApplicationService.detail(id);
    }

    /**
     * 证件原图预览。证件是营业执照与身份证正反面，存于私有存储、快照中只留渠道 MediaID，
     * 因此不下发可直接访问的地址，改由本接口鉴权后输出字节流，避免链接被转发后脱离权限控制。
     * 归属校验在 {@link MerchantApplicationService#loadMediaContent}：平台按权限、店铺/供应商仅本主体申请。
     */
    @Operation(summary = "进件证件原图预览")
    @PreAuthorize("@pms.hasPermission('merchantApplicationManage')")
    @IgnoreResponseAdvice
    @GetMapping("/media")
    public ResponseEntity<byte[]> media(
            @Parameter(description = "进件申请ID") @RequestParam Long id,
            @Parameter(description = "证件角色：1营业执照 2身份证人像面 3身份证国徽面") @RequestParam Integer role) {
        MerchantApplicationMediaService.MediaContent content =
                merchantApplicationService.loadMediaContent(id, role);
        if (content == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                // 证件属敏感材料，禁止各级缓存留存副本
                .cacheControl(CacheControl.noStore())
                .body(content.content());
    }

    @Operation(summary = "审核通过（提交微信进件）")
    @PreAuthorize("@pms.hasPermission('merchantApplicationApprove')")
    @PostMapping("/approve")
    public void approve(@RequestBody @Validated MerchantApplicationAuditParam param) {
        merchantApplicationService.approve(param.getId());
    }

    @Operation(summary = "审核拒绝")
    @PreAuthorize("@pms.hasPermission('merchantApplicationReject')")
    @PostMapping("/reject")
    public void reject(@RequestBody @Validated MerchantApplicationAuditParam param) {
        merchantApplicationService.reject(param.getId(), param.getRejectReason());
    }

    @Operation(summary = "刷新进件状态")
    @PreAuthorize("@pms.hasPermission('merchantApplicationRefresh')")
    @PostMapping("/refresh")
    public void refresh(@RequestParam Long id) {
        merchantApplicationService.refresh(id);
    }

    // ============ 店铺/门店/供应商端：申请 ============

    @Operation(summary = "提交进件申请（店铺/供应商端）")
    @PostMapping("/apply")
    public MerchantApplicationVO apply(@RequestBody @Validated MerchantApplicationSubmitParam param) {
        return merchantApplicationService.apply(param);
    }

    @Operation(summary = "当前登录主体最新进件申请（店铺/供应商端）")
    @GetMapping("/current")
    public MerchantApplicationVO current(@RequestParam(value = "payChannel", required = false) String payChannel) {
        return merchantApplicationService.current(payChannel);
    }

    @Operation(summary = "上传进件媒体文件")
    @PostMapping("/uploadMedia")
    public Map<String, String> uploadMedia(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "payChannel", required = false) String payChannel) {
        String mediaId = merchantApplicationService.uploadMedia(file, payChannel);
        return Map.of("mediaId", mediaId);
    }
}
