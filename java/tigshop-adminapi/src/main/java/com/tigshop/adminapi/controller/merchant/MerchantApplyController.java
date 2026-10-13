// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.merchant;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.merchant.MerchantApplyAuditParam;
import com.tigshop.bean.query.merchant.MerchantApplyListPageQuery;
import com.tigshop.bean.dto.merchant.MerchantApplyUpdateDTO;
import com.tigshop.bean.vo.common.StatusListVO;
import com.tigshop.bean.vo.merchant.MerchantApplyVO;
import com.tigshop.service.merchant.MerchantApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 入驻申请控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/merchant/apply")
@Tag(name = "入驻申请", description = "入驻申请功能")
public class MerchantApplyController {

    @Resource
    private MerchantApplyService merchantApplyService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<MerchantApplyVO> list(MerchantApplyListPageQuery pageQuery) {
        return merchantApplyService.list(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public MerchantApplyVO detail(@RequestParam("id") Integer id) {
        return merchantApplyService.detail(id);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('merchantApplyModifyManage')")
    public void update(@RequestBody MerchantApplyUpdateDTO updateDTO) {
        merchantApplyService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('merchantApplyModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        merchantApplyService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('merchantApplyModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        merchantApplyService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('merchantApplyModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        merchantApplyService.batch(batchDTO);
    }

    @PostMapping("/audit")
    @Operation(summary = "审核操作")
    @PreAuthorize("@pms.hasPermission('merchantApplyModifyManage')")
    public void audit(@RequestBody MerchantApplyAuditParam param) {
        merchantApplyService.audit(param);
    }

    @GetMapping("/config")
    @Operation(summary = "获取状态参数")
    public List<Map<String, String>> config() {
        return merchantApplyService.config();
    }
}
