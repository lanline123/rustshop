package com.tigshop.adminapi.controller.proxyshopping;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.PickupContactCreateDTO;
import com.tigshop.bean.dto.user.PickupContactListDTO;
import com.tigshop.bean.dto.user.PickupContactUpdateDTO;
import com.tigshop.bean.vo.user.PickupContactVO;
import com.tigshop.service.user.PickupContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/proxyShopping/pickupContact")
@Tag(name = "代客下单自提联系人管理", description = "代客下单自提联系人管理")
@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")
public class ProxyShoppingPickupContactController {

    @Resource
    private PickupContactService pickupContactService;

    @PostMapping("/list")
    @Operation(summary = "自提联系人分页列表")
    public Page<PickupContactVO> list(@RequestBody PickupContactListDTO listDTO) {
        return pickupContactService.list(listDTO);
    }

    @GetMapping("/listByUserId")
    @Operation(summary = "根据用户ID获取自提联系人列表")
    public List<PickupContactVO> listByUserId(@RequestParam("userId") Integer userId) {
        return pickupContactService.listByUserId(userId);
    }

    @GetMapping("/detail")
    @Operation(summary = "自提联系人详情")
    public PickupContactVO detail(@RequestParam("id") Long id) {
        return pickupContactService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建自提联系人")
    public Long create(@RequestBody PickupContactCreateDTO createDTO) {
        return pickupContactService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "修改自提联系人")
    public boolean update(@RequestBody PickupContactUpdateDTO updateDTO) {
        return pickupContactService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除自提联系人")
    public boolean del(@RequestParam("id") Long id) {
        return pickupContactService.del(id);
    }
}
