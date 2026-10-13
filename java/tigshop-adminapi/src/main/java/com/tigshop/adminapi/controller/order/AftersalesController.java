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
package com.tigshop.adminapi.controller.order;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.*;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.param.order.AftersalesApplyParam;
import com.tigshop.bean.param.order.AftersalesEditParam;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.vo.order.AftersalesVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.order.AftersalesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.tigshop.common.utils.HeaderUtils.getAdminType;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@RestController
@RequestMapping("/adminapi/order/aftersales")
@Tag(name = "售后管理", description = "售后管理功能")
public class AftersalesController {
    @Resource
    private AftersalesService aftersalesService;

    @Resource
    private TigshopProperties tigshopProperties;

    @GetMapping("/applyType")
    @Operation(summary = "申请类型")
    public Map<Integer, String> applyType() {
        Map<Integer, String> type = new HashMap<>();
        type.put(1, "退货/退款");
        type.put(2, "仅退款");
        return type;
    }

    @GetMapping("/returnGoodsStatus")
    @Operation(summary = "退换货状态")
    public Map<Integer, String> returnGoodsStatus() {
        Map<Integer, String> reason = new HashMap<>();
        reason.put(1, "审核处理中");
        reason.put(2, "审核通过");
        reason.put(3, "审核未通过");
//        reason.put(4, "待用户回寄");
        reason.put(4, "待回寄");
        reason.put(5, "待商家收货");
        reason.put(6, "已完成");
        reason.put(7, "已取消");
        Integer isVendor = tigshopProperties.getIsVendor();
        if (isVendor == 1){
            reason.put(21, "供应商待审核");
            reason.put(22, "供应商待收货");
        }
        boolean isVendorAdmin = AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR;
        if (isVendorAdmin) {
            return Map.of(
                    21, "供应商待审核",
                    22, "供应商待收货",
                    6, "已完成"
                    );
        }
        return reason;
    }

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<AftersalesVO> list(AftersalesListPageQuery pageQuery) {
        return aftersalesService.list(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public AftersalesVO detail(@RequestParam Integer id) {
        return aftersalesService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    public void create(@RequestBody AftersalesApplyParam createDTO) {
        aftersalesService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('aftersalesModifyManage')")
    public void update(@RequestBody @Validated AftersalesEditParam param) {
        aftersalesService.update(param);
    }

    @PostMapping("/receive")
    @Operation(summary = "售后确认收货接口")
    @PreAuthorize("@pms.hasPermission('aftersalesModifyManage')")
    public void receive(@RequestBody Map<String, Integer> data) {
        aftersalesService.receive(data.get("id"));
    }

    @PostMapping("/rejectReceive")
    @Operation(summary = "售后拒收接口")
    @PreAuthorize("@pms.hasPermission('aftersalesModifyManage')")
    public void rejectReceive(@RequestBody @Validated AfterSalesRejectReceiveDTO dto) {
        aftersalesService.rejectReceive(dto.getId(), dto.getReply());
    }

    @PostMapping("/record")
    @Operation(summary = "提交售后反馈记录")
    @PreAuthorize("@pms.hasPermission('aftersalesModifyManage')")
    public void record(@RequestBody AftersalesRecordCreateDTO aftersalesRecordCreateDTO) {
        aftersalesService.submitFeedbackRecord(aftersalesRecordCreateDTO.getAftersaleId(), aftersalesRecordCreateDTO, SecurityUtils.getCurrentAdminId());
    }

    @PostMapping("/complete")
    @Operation(summary = "售后完结")
    @PreAuthorize("@pms.hasPermission('aftersalesModifyManage')")
    public void complete(@RequestBody AfterSalesCompleteDTO completeDTO) {
        aftersalesService.complete(completeDTO);
    }

}
