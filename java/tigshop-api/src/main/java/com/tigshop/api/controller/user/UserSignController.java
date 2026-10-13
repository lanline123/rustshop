package com.tigshop.api.controller.user;

import com.tigshop.bean.vo.user.UserSignInfoVo;
import com.tigshop.service.user.UserSignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户签到中心
 *
 * @author wzh
 */
@RestController
@RequestMapping(("/api/user/sign"))
@Tag(name = "用户签到中心")
@Validated
@AllArgsConstructor
public class UserSignController {

    private final UserSignService userSignService;

    @GetMapping("/index")
    @Operation(summary = "签到首页")
    public UserSignInfoVo getUserSignInfoVo() {
        return userSignService.getUserSignInfoVo();
    }


    @GetMapping("/sign")
    @Operation(summary = "签到")
    public void sign() {
        userSignService.sign();
    }

}
