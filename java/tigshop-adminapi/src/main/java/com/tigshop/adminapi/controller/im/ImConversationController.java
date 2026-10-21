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
package com.tigshop.adminapi.controller.im;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.im.*;
import com.tigshop.bean.model.im.ImConversation;
import com.tigshop.bean.query.im.ImConversationPageQuery;
import com.tigshop.bean.vo.im.*;
import com.tigshop.common.constant.CheckFieldConstants;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.service.im.ImConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客服会话控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/im/conversation/conversation")
@Tag(name = "客服会话", description = "客服会话功能")
@Validated
public class ImConversationController {
    @Resource
    private ImConversationService imConversationService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ImConversationVO> list(ImConversationPageQuery listDTO) {
        //判断是否是后台
        boolean isAdminUser = TigUtils.getClientTypeByToken();
        if (isAdminUser) {
            listDTO.setLastServantId(SecurityUtils.getCurrentAdminId());
            Integer shopId = HeaderUtils.getShopId();
            listDTO.setShopId(shopId);
        } else {
            listDTO.setUserId(SecurityUtils.getCurrentUserId());
        }

        return imConversationService.list(listDTO);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索")
    public Map<String, Object> search(@RequestParam String keyword) {
        Integer servantId = SecurityUtils.getCurrentAdminId();
        List<ImSearchUserVO> userSearch = imConversationService.getUserSearch(keyword, servantId);
        List<ImSearchConversationVO> conversationSearch = imConversationService.getConversationSearch(keyword, servantId);
        Map<String, Object> map = new HashMap<>();
        map.put("userList", userSearch);
        map.put("conversationList", conversationSearch);
        return map;
    }

    @PostMapping("/transfer")
    @Operation(summary = "转接")
    public void transfer(@Valid @RequestBody TransferDTO dto) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        if(dto.getServantId() == null) {
            dto.setServantId(adminId);
        }
        imConversationService.transfer(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ImConversationVO detail(@RequestParam(value = "conversationId")  Integer id) {
        return imConversationService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    public ImConversation create(@Valid @RequestBody ImConversationCreateDTO createDTO) {
        Integer shopId = HeaderUtils.getShopId();
        createDTO.setShopId(shopId);
        createDTO.setLastServantId(SecurityUtils.getCurrentAdminId());
        return imConversationService.create(createDTO);
    }

    @GetMapping("/waitServantList")
    @Operation(summary = "获取待接入的客户列表")
    public Page<WaitServantListVO> waitServantList(WaitServantListDTO dto) {
        Integer shopId = HeaderUtils.getShopId();
        dto.setShopId(shopId);
        return imConversationService.waitServantList(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public void del(@Valid @RequestBody DelDTO dto) {
        Integer shopId = HeaderUtils.getShopId();
        dto.setShopId(shopId);
        imConversationService.delete(dto);
    }

    @PostMapping("/saveRemark")
    @Operation(summary = "保存备注")
    public void saveRemark(@Valid @RequestBody SaveRemarkDTO dto) {
        Integer shopId = HeaderUtils.getShopId();
        dto.setShopId(shopId);
        imConversationService.saveRemark(dto);
    }

    @GetMapping("/consultHistory")
    @Operation(summary = "获取历史会话")
    public Page<ConsultHistoryListVO> consultHistory(ImConversationPageQuery pageQuery) {
        return imConversationService.consultHistory(pageQuery);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        imConversationService.updateField(updateField,  CheckFieldConstants.GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        imConversationService.batch(batchDTO);
    }
}
