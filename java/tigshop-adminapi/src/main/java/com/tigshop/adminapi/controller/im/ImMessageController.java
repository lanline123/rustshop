package com.tigshop.adminapi.controller.im;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.im.ImMessageListDTO;
import com.tigshop.bean.dto.im.ImMessageReceivedListDTO;
import com.tigshop.bean.dto.im.ImMessageSendDTO;
import com.tigshop.bean.dto.im.ImSetReadDTO;
import com.tigshop.bean.vo.im.ImMessageListVO;
import com.tigshop.bean.vo.im.ImMessageSendVO;
import com.tigshop.bean.vo.setting.UploadVideoInfoVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.security.admin.handler.PermissionHandler;
import com.tigshop.service.im.ImMessageService;
import com.tigshop.service.video.GalleryVideoInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * IM消息制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/im/conversation/message")
@Tag(name = "消息列表", description = "消息列表")
@Validated
public class ImMessageController {
    @Resource
    private ImMessageService imMessageService;

    @Resource
    private GalleryVideoInfoService galleryVideoInfoService;

    @Resource
    private PermissionHandler permissionHandler;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ImMessageListVO> list(ImMessageListDTO listDTO) {
        //判断是否是后台
        boolean isAdmin = TigUtils.getClientTypeByToken();
        if (isAdmin) {
            Integer shopId = HeaderUtils.getShopId();
            listDTO.setShopId(shopId);
            boolean isHistoryQuery = listDTO.getConversationId() != null
                    && listDTO.getConversationId() > 0
                    && shopId != null
                    && shopId > -1
                    && permissionHandler.hasPermission("kefuHistoryList");
            if (isHistoryQuery) {
                // 历史会话由菜单权限控制可见范围，并继续按当前店铺隔离。
                listDTO.setServantId(null);
            } else {
                listDTO.setServantId(SecurityUtils.getCurrentAdminId());
            }
        } else {
            listDTO.setUserId(SecurityUtils.getCurrentUserId());
        }

        return imMessageService.list(listDTO);
    }

    @GetMapping("/receivedList")
    @Operation(summary = "客服消息播报记录列表")
    public Page<ImMessageListVO> receivedList(ImMessageReceivedListDTO dto) {
        // 店铺、客服由服务端固定注入，保证仅返回当前客服/店铺收到的用户消息（与 SSE 播报同口径）
        Integer shopId = HeaderUtils.getShopId();
        Integer servantId = SecurityUtils.getCurrentAdminId();
        return imMessageService.receivedList(dto, shopId, servantId);
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息")
    public ImMessageSendVO send(@Valid @RequestBody ImMessageSendDTO dto) {
        boolean isAdmin = TigUtils.getClientTypeByToken();
        if (isAdmin) {
            Integer shopId = HeaderUtils.getShopId();
            if (shopId != null) {
                dto.setShopId(shopId);
            }
            dto.setType(2);
            dto.setServantId(SecurityUtils.getCurrentAdminId());
        } else {
            dto.setType(1);
            dto.setUserId(SecurityUtils.getCurrentUserId());
        }
        return imMessageService.send(dto);
    }

    @PostMapping("/uploadVideo")
    @Operation(summary = "上传视频")
    public UploadVideoInfoVO uploadVideo(@RequestParam("file") MultipartFile file) {
        return galleryVideoInfoService.uploadVideo(file);
    }

    @PostMapping("/setRead")
    @Operation(summary = "设置已读")
    public boolean setRead(@RequestBody ImSetReadDTO dto) {
        return imMessageService.setRead(dto);
    }

}
