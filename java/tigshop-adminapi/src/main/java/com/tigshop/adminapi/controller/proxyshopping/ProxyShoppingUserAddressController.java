package com.tigshop.adminapi.controller.proxyshopping;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserAddressCreateDTO;
import com.tigshop.bean.dto.user.UserAddressListDTO;
import com.tigshop.bean.dto.user.UserAddressUpdateDTO;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.user.UserAddress;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.user.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/proxyShopping/address")
@Tag(name = "代客下单地址管理", description = "代客下单地址管理")
@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")
public class ProxyShoppingUserAddressController {

    @Resource
    private UserAddressService userAddressService;

    @Resource
    private RegionService regionService;

    @PostMapping("/list")
    @Operation(summary = "收货地址列表")
    public Page<UserAddressVO> list(@RequestBody UserAddressListDTO listDTO) {
        listDTO.setSortField("is_selected");
//        listDTO.setIsSystem(1);
        listDTO.setIsSystem(2);
        return userAddressService.list(listDTO);
    }

    @PostMapping("/create")
    @Operation(summary = "收货地址列表")
    public Integer create(@RequestBody UserAddressCreateDTO createDTO) {
        createDTO.setIsSystem(1);
        return userAddressService.create(createDTO);
    }


    @PostMapping("/update")
    @Operation(summary = "修改收货地址")
    public void update(@RequestBody UserAddressUpdateDTO userAddress) {
        UserAddress po = userAddressService.getById(userAddress.getAddressId());
        if(po == null){
            throw new GlobalException("地址不存在");
        }
        userAddress.setIsSystem(po.getIsSystem());
        userAddress.setUserId(po.getUserId());
        userAddressService.update(userAddress);
    }


    @PostMapping("/setSelected")
    @Operation(summary = "设置选中状态")
    public void setSelected(@RequestBody UserAddressUpdateDTO userAddress) {
        userAddressService.setSelected(userAddress.getId());
    }

    @GetMapping("/getRegionPath")
    @Operation(summary = "根据regionId获取完整地区路径")
    public List<Region> getRegionPath(@RequestParam("regionId") Integer regionId) {
        return regionService.getRegionPath(regionId);
    }

    @GetMapping("/getAllRegionTree")
    @Operation(summary = "获取所有地区")
    public List<Tree<Integer>> getAllRegionTree() {
        return regionService.getAllRegionTree();
    }
}
