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
package com.tigshop.adminapi.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.user.UserCompanyAuditDTO;
import com.tigshop.bean.dto.user.UserCompanyListDTO;
import com.tigshop.bean.vo.user.UserCompanyItemVO;
import com.tigshop.bean.vo.user.UserCompanyVO;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.service.user.UserCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 会员企业认证控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/user/userCompany")
@Tag(name = "会员企业认证", description = "会员企业认证功能")
@Validated
@PreAuthorize("@pms.hasPermission('userCertificationManage')")
public class UserCompanyController {
    @Resource
    private UserCompanyService userCompanyService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<UserCompanyVO> list(UserCompanyListDTO listDTO) {
        if(StringUtils.isBlank(listDTO.getKeyword())){
            listDTO.setKeyword(listDTO.getUsername());
        }
        return userCompanyService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public UserCompanyItemVO detail(@RequestParam Integer id) {
        return userCompanyService.detail(id);
    }

    @PostMapping("/audit")
    @Operation(summary = "审核")
    public void audit(@Valid @RequestBody UserCompanyAuditDTO auditDTO) {
        userCompanyService.audit(auditDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        userCompanyService.del(operateDTO.getId());
    }

}
