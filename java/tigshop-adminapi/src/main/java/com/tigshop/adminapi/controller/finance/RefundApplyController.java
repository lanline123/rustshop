package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.finance.refundapply.RefundApplyAuditParam;
import com.tigshop.bean.param.finance.refundapply.RefundApplyEditParam;
import com.tigshop.bean.param.finance.refundapply.RefundApplySaveParam;
import com.tigshop.bean.param.finance.refundapply.OfflineAuditParam;
import com.tigshop.bean.query.finance.RefundApplyPageQuery;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyDetailVO;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyVO;
import com.tigshop.common.annotation.Idempotent;
import com.tigshop.service.finance.RefundApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 退款申请控制器
 *
 * @author kidd
 * @create 2025/7/7
 */
@Tag(name = "退款申请", description = "退款申请功能")
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/finance/refundApply")
public class RefundApplyController {

    private final RefundApplyService refundApplyService;

    @Operation(summary = "获取审核状态配置")
    @GetMapping("/config")
    public Map<Integer, String> config() {
        return refundApplyService.config();
    }

    @Operation(summary = "获取列表")
    @GetMapping("/list")
    public Page<RefundApplyVO> list(RefundApplyPageQuery pageQuery) {
        return refundApplyService.list(pageQuery);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public RefundApplyDetailVO detail(@RequestParam(value = "id") Integer id) {
        return refundApplyService.detail(id);
    }

    @Operation(summary = "审核")
    @PreAuthorize("@pms.hasPermission('refundApplyUpdateManage')")
    @Idempotent(
        key = "#param.refundId",
        expireTime = 10,
        message = "退款审核正在处理中，请勿重复提交"
    )
    @PostMapping("/audit")
    public void audit(@RequestBody @Validated RefundApplyAuditParam param) {
        refundApplyService.audit(param);
    }

    @Operation(summary = "确认线下转账")
    @PreAuthorize("@pms.hasPermission('refundApplyUpdateManage')")
    @PostMapping("/offlineAudit")
    public void offlineAudit(@RequestBody @Validated OfflineAuditParam param) {
        refundApplyService.offlineAudit(param);
    }

    @Operation(summary = "创建")
    @PostMapping("/create")
    public void create(@RequestBody @Validated RefundApplySaveParam param) {
        refundApplyService.create(param);
    }

    @Operation(summary = "更新")
    @PostMapping("/update")
    @PreAuthorize("@pms.hasPermission('refundApplyUpdateManage')")
    public void update(@RequestBody @Validated RefundApplyEditParam param) {
        refundApplyService.update(param);
    }

    @Operation(summary = "删除")
    @PostMapping("/del")
    public void del(@RequestBody OperateDTO operateDTO) {
        refundApplyService.del(operateDTO.getId());
    }

    @Operation(summary = "更新字段")
    @PostMapping("/updateField")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        refundApplyService.updateField(updateField, GENERAL_FIELDS);
    }

    @Operation(summary = "批量操作")
    @PostMapping("/batch")
    public void batch(@RequestBody BatchDTO batchDTO) {
        refundApplyService.batch(batchDTO);
    }
}
