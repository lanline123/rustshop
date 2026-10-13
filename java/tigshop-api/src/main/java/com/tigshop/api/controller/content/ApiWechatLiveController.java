package com.tigshop.api.controller.content;

import com.tigshop.bean.feign.wechat.GetFinderLiveRecordListResult;
import com.tigshop.service.content.WechatLiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信直播控制器（前台API）
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/api/content/wechatVideoAccount")
@Tag(name = "微信直播", description = "微信视频号前台接口")
public class ApiWechatLiveController {

    @Resource
    private WechatLiveService wechatLiveService;

    /**
     * 获取视频号当前的直播记录
     *
     * @param id 直播记录ID
     * @return 直播记录
     */
    @GetMapping("/finderLiveRecord")
    @Operation(summary = "获取视频号当前的直播记录", description = "获取当前正在进行的")
    public GetFinderLiveRecordListResult.LiveRecord getFinderLiveRecord(@RequestParam(value = "id") String id) {
        return wechatLiveService.getFinderLiveRecord(id);
    }
}
