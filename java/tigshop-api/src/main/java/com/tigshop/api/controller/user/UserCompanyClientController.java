package com.tigshop.api.controller.user;

import com.tigshop.bean.dto.user.UserCompanyCreateDTO;
import com.tigshop.bean.vo.user.UserCompanyApplyVO;
import com.tigshop.bean.vo.user.UserCompanyItemVO;
import com.tigshop.bean.vo.user.UserCompanyVO;
import com.tigshop.service.user.UserCompanyService;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 用户实名认证
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/company"))
@Tag(name = "用户实名认证")
public class UserCompanyClientController {
    @Resource
    UserCompanyService userCompanyService;
    @PostMapping("/apply")
    @Operation(summary = "实名申请")
    public UserCompanyVO apply(@RequestBody UserCompanyCreateDTO createDTO) {
        return userCompanyService.companyAudit(createDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "实名认证详情")
    public UserCompanyItemVO detail(@RequestParam Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        return userCompanyService.clientDetail(id, userId);
    }

    @GetMapping("/myApply")
    @Operation(summary = "我的实名认证申请")
    public UserCompanyApplyVO myApply() {
        return userCompanyService.myApply();
    }

}
