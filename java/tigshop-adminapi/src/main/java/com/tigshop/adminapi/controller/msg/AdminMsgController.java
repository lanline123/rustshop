// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.msg;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgListDTO;
import com.tigshop.bean.dto.msg.AdminMsgUpdateDTO;
import com.tigshop.bean.vo.msg.AdminMsgBroadcastVO;
import com.tigshop.bean.vo.msg.AdminMsgCountVO;
import com.tigshop.bean.vo.msg.AdminMsgTypeListVO;
import com.tigshop.bean.vo.msg.AdminMsgVO;
import com.tigshop.bean.vo.msg.HomeMsgSummaryVO;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.msg.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 管理员消息控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/msg/adminMsg")
@Tag(name = "管理员消息", description = "管理员消息功能")
@Validated
@RequiredArgsConstructor
public class AdminMsgController {
    private final AdminMsgService adminMsgService;
    private final SseService sseService;

    @GetMapping(value = "/msgCount", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Schema(description = "获取消息数量")
    public SseEmitter getMsgCount(@RequestParam Long startTime) {
        return sseService.msgCount(startTime);
    }


    @GetMapping("/msgTypeArr")
    @Operation(summary = "获取消息类别")
    public List<AdminMsgTypeListVO> msgTypeArr() {
        return adminMsgService.getAdminMsgTypeList();
    }

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<AdminMsgVO> list(AdminMsgListDTO listDTO) {
        return adminMsgService.list(listDTO);
    }

    @GetMapping("/broadcastList")
    @Operation(summary = "语音播报内容列表（含商品摘要，供播报抽屉一次性渲染）")
    public Page<AdminMsgBroadcastVO> broadcastList(AdminMsgListDTO listDTO) {
        return adminMsgService.broadcastList(listDTO);
    }

    @GetMapping("/homeSummary")
    @Operation(summary = "首页消息摘要（未读总数 + 最新一条）")
    public HomeMsgSummaryVO homeSummary() {
        return adminMsgService.getHomeSummary();
    }

    @PostMapping("/setReaded")
    @Operation(summary = "设置为单个已读")
    public void setReaded(@Valid @RequestBody AdminMsgUpdateDTO updateDTO) {
        adminMsgService.setReaded(updateDTO.getMsgId());
    }

    @PostMapping("/setAllReaded")
    @Operation(summary = "设置全部已读")
    public void setAllReaded() {
        adminMsgService.setAllReaded();
    }

}
