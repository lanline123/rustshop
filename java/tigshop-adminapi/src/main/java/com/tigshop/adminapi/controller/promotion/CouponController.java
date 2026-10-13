// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.promotion.CouponDTO;
import com.tigshop.bean.param.promotion.CouponManualIssueParam;
import com.tigshop.bean.query.promotion.coupon.CouponPageQuery;
import com.tigshop.bean.query.promotion.coupon.CouponIssueBatchPageQuery;
import com.tigshop.bean.query.promotion.coupon.CouponIssueLogPageQuery;
import com.tigshop.bean.query.promotion.coupon.CouponIssueUserPageQuery;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.vo.promotion.CouponIssueBatchVO;
import com.tigshop.bean.vo.promotion.CouponIssueLogVO;
import com.tigshop.bean.vo.promotion.CouponIssueUserVO;
import com.tigshop.bean.vo.promotion.CouponManualIssuePreviewVO;
import com.tigshop.bean.vo.promotion.CouponManualIssueResultVO;
import com.tigshop.bean.vo.promotion.CouponVO;
import com.tigshop.common.constant.CheckFieldConstants;
import com.tigshop.service.promotion.CouponIssueBatchService;
import com.tigshop.service.promotion.CouponIssueLogService;
import com.tigshop.service.promotion.CouponService;
import com.tigshop.service.user.UserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/coupon")
@Tag(name = "优惠券", description = "优惠券功能")
@Validated
@PreAuthorize("@pms.hasPermission('couponManage')")
public class CouponController {
    @Resource
    private CouponService couponService;

    @Resource
    private CouponIssueBatchService couponIssueBatchService;

    @Resource
    private CouponIssueLogService couponIssueLogService;

    @Resource
    private UserCouponService userCouponService;

    @GetMapping("/list")
    @PreAuthorize("@pms.hasPermission('decorate','couponManage')")
    @Operation(summary = "获取列表")
    public Page<CouponVO> list(CouponPageQuery listDTO) {
        return couponService.list(listDTO);
    }

    @GetMapping("/listByIds")
    @PreAuthorize("@pms.hasPermission('decorate','couponManage')")
    @Operation(summary = "按 ids 批量获取优惠券")
    public List<CouponVO> listByIds(@RequestParam String ids) {
        if (cn.hutool.core.util.StrUtil.isBlank(ids)) {
            return java.util.Collections.emptyList();
        }
        List<Integer> idList = java.util.Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && s.matches("\\d+"))
                .map(Integer::parseInt)
                .toList();
        if (idList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return couponService.getCouponListByIds(idList);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public CouponVO detail(@RequestParam Integer id) {
        return couponService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('promotionCouponModifyManage')")
    public void create(@Valid @RequestBody CouponDTO createDTO) {
        couponService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('promotionCouponModifyManage')")
    public void update(@Valid @RequestBody CouponDTO updateDTO) {
        couponService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('promotionCouponModifyManage')")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        couponService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('promotionCouponModifyManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        couponService.updateField(updateField, CheckFieldConstants.COUPON_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('promotionCouponModifyManage')")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        couponService.batch(batchDTO);
    }

    @GetMapping("/config")
    @Operation(summary = "营销配置项")
    public List<UserRank> config() {
        return couponService.config();
    }

    @GetMapping("/validList")
    @Operation(summary = "查询平台有效优惠券列表")
    @PreAuthorize("@pms.hasPermission('promotionCouponModifyManage')")
    public Page<CouponVO> validList(CouponPageQuery query) {
        return couponService.getValidList(query);
    }

    @GetMapping("/issue/users")
    @Operation(summary = "手动发放会员搜索")
    public Page<CouponIssueUserVO> issueUsers(CouponIssueUserPageQuery query) {
        return userCouponService.searchIssueUsers(query);
    }

    @PostMapping("/issue/preview")
    @Operation(summary = "手动发放优惠券预览")
//    @PreAuthorize("@pms.hasPermission('promotionCouponModifyManage')")
    public CouponManualIssuePreviewVO issuePreview(@Valid @RequestBody CouponManualIssueParam param) {
        return userCouponService.previewManualIssue(param);
    }

    @PostMapping("/issue/send")
    @Operation(summary = "手动发放优惠券")
    @PreAuthorize("@pms.hasPermission('promotionCouponModifyManage')")
    public CouponManualIssueResultVO issueSend(@Valid @RequestBody CouponManualIssueParam param) {
        return userCouponService.manualIssue(param);
    }

    @GetMapping("/issue/log")
    @Operation(summary = "优惠券发放日志列表")
    public Page<CouponIssueLogVO> issueLog(CouponIssueLogPageQuery query) {
        return couponIssueLogService.list(query);
    }

    @GetMapping("/issue/batch")
    @Operation(summary = "优惠券发放批次列表")
    public Page<CouponIssueBatchVO> issueBatch(CouponIssueBatchPageQuery query) {
        return couponIssueBatchService.list(query);
    }
}
