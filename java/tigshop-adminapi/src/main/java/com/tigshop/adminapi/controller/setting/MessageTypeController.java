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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.setting.MessageTypeDetailDTO;
import com.tigshop.bean.dto.setting.MessageTypeListDTO;
import com.tigshop.bean.vo.setting.MessageTypeListVO;
import com.tigshop.common.constant.CheckFieldConstants;
import com.tigshop.service.setting.MessageTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 消息分类控制器
 *
 * @author Tigshop团队
 * @create 2024年12月26日 14:24
 */
@RestController
@RequestMapping("/adminapi/setting/messageType")
@Tag(name = "消息分类")
@Validated
@PreAuthorize("@pms.hasPermission('messageTypeManage')")
public class MessageTypeController {
    @Resource
    private MessageTypeService messageTypeService;

    @GetMapping("/list")
    @Operation(summary = "消息分类列表")
    public Page<MessageTypeListVO> list(MessageTypeListDTO dto) {
        return messageTypeService.list(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "消息分类详情")
    public MessageTypeDetailDTO detail(@RequestParam Integer id) {
        return messageTypeService.detail(id);
    }

    @PostMapping("/update")
    @Operation(summary = "修改消息分类")
    @PreAuthorize("@pms.hasPermission('messageTypeUpdateManage')")
    public void update(@Validated @RequestBody MessageTypeDetailDTO dto) {
        messageTypeService.update(dto);
    }

    // 单个字段更新
    @PostMapping("/updateField")
    @Operation(summary = "修改消息分类单个字段")
    @PreAuthorize("@pms.hasPermission('messageTypeUpdateManage')")
    public void updateField(@RequestBody @Validated UpdateFieldDTO updateField) {
        messageTypeService.updateField(updateField, CheckFieldConstants.MESSAGE_TYPE_FIELDS);
    }

    @PostMapping("/miniProgramMessageTemplate")
    @Operation(summary = "小程序模板生成")
    @PreAuthorize("@pms.hasPermission('miniProgramMessageTemplateManage')")
    public void miniProgramMessageTemplate() {
        messageTypeService.miniProgramMessageTemplate();
    }

    @PostMapping("/wechatMessageTemplate")
    @Operation(summary = "公众号模板生成")
    @PreAuthorize("@pms.hasPermission('wechatMessageTemplate')")
    public void wechatMessageTemplate() {
        messageTypeService.wechatMessageTemplate();
    }


}
