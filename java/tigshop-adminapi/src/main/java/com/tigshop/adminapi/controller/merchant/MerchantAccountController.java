package com.tigshop.adminapi.controller.merchant;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.merchant.MerchantAccountCreateDTO;
import com.tigshop.bean.dto.merchant.MerchantAccountListDTO;
import com.tigshop.bean.dto.merchant.MerchantAccountUpdateDTO;
import com.tigshop.bean.enums.merchant.MerchantAccountType;
import com.tigshop.bean.vo.merchant.MerchantAccountVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.merchant.MerchantAccountService;
import com.tigshop.service.shop.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户账户控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/merchant/account")
@Tag(name = "商户账户", description = "商户账户功能")
public class MerchantAccountController {
    @Resource
    private MerchantAccountService merchantAccountService;
    @Resource
    private AdminUserService adminUserService;
    @Autowired
    private ShopService shopService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<MerchantAccountVO> list(MerchantAccountListDTO listDTO) {
        return merchantAccountService.list(listDTO);
    }

    @GetMapping("config")
    @Operation(summary = "获取配置")
    public List<Map<String, Object>> config() {
        Map<Integer, String> typeListMap = MerchantAccountType.getTypeList();
        List<Map<String, Object>> typeList = typeListMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> type = new HashMap<>();
                    type.put("accountType", entry.getKey());
                    type.put("accountTypeText", entry.getValue());
                    return type;
                })
                .toList();
        return typeList;
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public MerchantAccountVO detail(Integer id) {
        MerchantAccountVO detail = merchantAccountService.detail(id);
        Integer merchantId = detail.getMerchantId();
        merchantAccountService.checkMerchantAuth(SecurityUtils.getCurrentAdminId(), merchantId);
        return detail;
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('accountModifyManage')")
    public void create(@RequestBody MerchantAccountCreateDTO dto) {
        Integer shopId = HeaderUtils.getShopId();
        dto.setShopId(shopId);
        // Integer merchantId = adminUserService.getMerchantId(SecurityUtils.getCurrentAdminId());
        Integer merchantId = shopService.getById(shopId).getMerchantId();
        dto.setMerchantId(merchantId);
        merchantAccountService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('accountModifyManage')")
    public void update(@RequestBody MerchantAccountUpdateDTO dto) {
        merchantAccountService.update(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('accountModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        merchantAccountService.del(operateDTO.getId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('accountModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        merchantAccountService.batch(batchDTO);
    }

}
