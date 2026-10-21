package com.tigshop.adminapi.controller.proxyshopping;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserCouponListDTO;
import com.tigshop.bean.dto.user.UserDTO;
import com.tigshop.bean.dto.user.UserListDTO;
import com.tigshop.bean.param.user.UserCouponClaimParam;
import com.tigshop.bean.vo.user.UserItemVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.user.UserCouponService;
import com.tigshop.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @author Tigshop项目组
 * @create 2025年10月24日 17:05
 */
@RestController
@RequestMapping("/adminapi/proxyShopping/user")
@Validated
@Tag(name = "代客下单")
@RequiredArgsConstructor
@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")
public class ProxyShoppingUserController {
    private final UserService userService;

    @GetMapping("/list")
    @Operation(summary = "会员列表")
    public Page<UserDTO> list(UserListDTO userList) {
        String adminType = HeaderUtils.getAdminType();
        if("admin".equals(adminType) && Objects.equals(userList.getIsExact(),true)){
            userList.setIsExact(true);
        }else{
            userList.setIsExact(!"admin".equals(adminType));
        }
        Page<UserDTO> list = userService.list(userList);
        if (list != null && list.getRecords() != null) {
            list.getRecords().forEach(user -> {
                if (user != null) {
                    user.setPassword(null);
                }
            });
        }
        return list;
    }

    @GetMapping("/detail")
    @Operation(summary = "会员详情")
    public UserItemVO detail(@RequestParam Integer id) {
        UserItemVO userAndUserRank = userService.getUserAndUserRank(id);
        if (userAndUserRank != null) {
            userAndUserRank.setPassword(null);
        }
        return userAndUserRank;
    }
}