package com.tigshop.adminapi.controller.user;

import com.tigshop.bean.dto.user.MemberAudienceDTO;
import com.tigshop.service.user.MemberAudienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 会员标签二期：人群圈选公共能力（预估人数 / 文字描述）。
 * 供营销活动、消息群发等配置页面调用，按 MemberAudienceDTO 评估人群。
 */
@RestController
@RequestMapping("/adminapi/user/memberAudience")
@Tag(name = "会员人群圈选", description = "会员标签二期人群圈选公共能力")
public class MemberAudienceController {

    @Resource
    private MemberAudienceService memberAudienceService;

    @PostMapping("/estimate")
    @Operation(summary = "预估人群命中人数")
    public Map<String, Object> estimate(@RequestBody MemberAudienceDTO audience) {
        long count = memberAudienceService.countUsers(audience);
        return Map.of("count", count);
    }

    @PostMapping("/describe")
    @Operation(summary = "人群条件文字描述")
    public Map<String, Object> describe(@RequestBody MemberAudienceDTO audience) {
        return Map.of("text", memberAudienceService.describe(audience));
    }
}
