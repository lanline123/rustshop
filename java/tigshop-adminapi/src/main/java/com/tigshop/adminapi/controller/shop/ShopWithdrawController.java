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
package com.tigshop.adminapi.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.shop.*;
import com.tigshop.bean.vo.common.StatusListVO;
import com.tigshop.bean.vo.shop.ShopWithdrawSummaryVO;
import com.tigshop.bean.vo.shop.ShopWithdrawVO;
import com.tigshop.service.shop.ShopWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 商家账户表控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/merchant/shopWithdraw")
@Tag(name = "商家账户表", description = "商家账户表功能")
public class ShopWithdrawController {
    @Resource
    private ShopWithdrawService shopWithdrawService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ShopWithdrawVO> list(ShopWithdrawListDTO listDTO) {
        return shopWithdrawService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ShopWithdrawVO detail(@RequestParam Integer id) {
        return shopWithdrawService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('merchantShopWithdrawModifyManage')")
    public void create(@RequestBody ShopWithdrawCreateDTO createDTO) {
        shopWithdrawService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('merchantShopWithdrawModifyManage')")
    public void update(@RequestBody ShopWithdrawUpdateDTO updateDTO) {
        shopWithdrawService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public void del(@RequestBody OperateDTO operateDTO) {
        shopWithdrawService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        shopWithdrawService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    public void batch(@RequestBody BatchDTO batchDTO) {
        shopWithdrawService.batch(batchDTO);
    }

    @GetMapping("/config")
    @Operation(summary = "获取配置")
    public StatusListVO<Map<Integer, String>> config() {
       return shopWithdrawService.getStatusList();
    }

    @PostMapping("/audit")
    @Operation(summary = "审核提现")
    @PreAuthorize("@pms.hasPermission('merchantShopWithdrawModifyManage')")
    public void audit(@RequestBody ShopWithdrawAuditDTO dto) {
        shopWithdrawService.audit(dto);
    }

    @PostMapping("/uploadPayVoucher")
    @Operation(summary = "上传打款凭证")
    @PreAuthorize("@pms.hasPermission('merchantShopWithdrawModifyManage')")
    public void uploadPayVoucher(@RequestBody ShopWithdrawPayVoucherDTO dto) {
        shopWithdrawService.uploadPayVoucher(dto);
    }

    @GetMapping("/summaryList")
    @Operation(summary = "店铺提现汇总列表")
    public Page<ShopWithdrawSummaryVO> summaryList(ShopWithdrawSummaryListDTO listDTO) {
        return shopWithdrawService.summaryList(listDTO);
    }

    @GetMapping("/dualBalance")
    @Operation(summary = "双余额查询（线下余额 + 三渠道子商户可用余额）")
    public Map<String, Object> dualBalance() {
        return shopWithdrawService.getDualBalance();
    }

    // ====== 三渠道独立体系（CR-20260721-144356）：微信沿用上方通用端点；汇付/支付宝各自独立端点，随渠道开关整体存废 ======


}
