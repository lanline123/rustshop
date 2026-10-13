package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.user.UserAddressCreateDTO;
import com.tigshop.bean.dto.user.UserAddressListDTO;
import com.tigshop.bean.dto.user.UserAddressUpdateDTO;
import com.tigshop.bean.model.user.UserAddress;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.core.entity.SortItem;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.user.UserAddressService;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户收货地址
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/address"))
@Tag(name = "用户收货地址")
@Validated
public class UserAddressClientController {
    @Resource
    UserAddressService userAddressService;

    @Resource
    MessageHelper messageHelper;

    @GetMapping("/tagList")
    @Operation(summary = "收货地址常用标签列表")
    public java.util.List<String> tagList() {
        return userAddressService.getAddressTagList();
    }

    @GetMapping("/list")
    @Operation(summary = "收货地址列表")
    public Page<UserAddressVO> list(UserAddressListDTO listDTO) {
        // 未传排序参数时使用地址默认的多字段排序；旧 sortField/sortOrder 写法保持原样兼容。
        if ((listDTO.getSortItems() == null || listDTO.getSortItems().isEmpty())
                && (listDTO.getSortField() == null || listDTO.getSortField().isBlank())) {
            listDTO.setSortItems(List.of(
                    new SortItem("is_selected", "desc"),
                    new SortItem("is_default", "desc")));
        }
        listDTO.setIsSystem(0);
        return userAddressService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "收货地址详情")
    public UserAddressVO detail(@RequestParam("id") Integer id) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId == 0) {
            throw new GlobalException("用户未登录");
        }
        return userAddressService.detail(id, userId);
    }

    @PostMapping("/del")
    @Operation(summary = "删除收货地址")
    public void del(@RequestBody OperateDTO operate) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId == 0) {
            throw new GlobalException("用户未登录");
        }
        UserAddress userAddress = userAddressService.getById(operate.getId());
        if (userAddress == null || !userId.equals(userAddress.getUserId())) {
            throw new GlobalException("地址不存在或无权限删除");
        }
        userAddressService.del(operate.getId());
    }

    @PostMapping("/create")
    @Operation(summary = "创建收货地址")
    public Integer create(@RequestBody UserAddressCreateDTO userAddress) {
        return userAddressService.create(userAddress);
    }


    @PostMapping("/update")
    @Operation(summary = "修改收货地址")
    public void update(@RequestBody UserAddressUpdateDTO userAddress) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId == 0) {
            throw new GlobalException("用户未登录");
        }
        UserAddress po = userAddressService.getById(userAddress.getAddressId());
        if(po == null || !userId.equals(po.getUserId())){
            throw new GlobalException(messageHelper.get("error.address.not.exist"));
        }
        userAddress.setIsSystem(po.getIsSystem());
        userAddress.setUserId(userId);
        userAddressService.update(userAddress);
    }

    @PostMapping("/setSelected")
    @Operation(summary = "设置选中状态")
    public void setSelected(@RequestBody UserAddressUpdateDTO userAddress) {
        userAddressService.setSelected(userAddress.getId());
    }
}
