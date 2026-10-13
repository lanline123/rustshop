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

package com.tigshop.adminapi.controller.setting;

import com.tigshop.bean.dto.setting.MailTemplatesDTO;
import com.tigshop.bean.vo.setting.MailTemplatesVO;
import com.tigshop.service.setting.MailTemplatesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 邮箱模板控制器
 *
 * @author Tigshop团队
 * @create 2024年12月25日 14:49
 */
@RestController
@RequestMapping("/adminapi/setting/mailTemplates")
@Validated
@Tag(name = "邮箱模板管理", description = "邮箱模板管理")
@PreAuthorize("@pms.hasPermission('mailTemplateManage')")
public class MailTemplatesController {
    @Resource
    private MailTemplatesService mailTemplatesService;

    @GetMapping("/list")
    @Operation(summary = "邮箱模板列表")
    public List<MailTemplatesVO> list() {
        return mailTemplatesService.mailTemplatesList();
    }

    // 邮件模板更新
    @PostMapping("/update")
    @Operation(summary = "更新邮件模板")
    @PreAuthorize("@pms.hasPermission('mailTemplatesUpdateManage')")
    public void update(@Valid @RequestBody MailTemplatesDTO dto) {
        mailTemplatesService.updateMailTemplates(dto);
    }
}
