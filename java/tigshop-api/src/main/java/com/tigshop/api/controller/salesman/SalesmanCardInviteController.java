// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.salesman;

import com.tigshop.service.salesman.WechatBizRecruitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 微商模式名片邀请控制器
 *
 * @author Tigshop团队
 * @since 2026/6/12
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/salesman/salesman")
@Tag(name = "微商名片邀请", description = "微商模式名片邀请开通下级分销员")
public class SalesmanCardInviteController {

    private final WechatBizRecruitService wechatBizRecruitService;

    @Operation(summary = "接受名片邀请，一键成为下级分销员")
    @PostMapping("/acceptCardInvite")
    public void acceptCardInvite(@RequestParam(name = "salesmanId") Integer salesmanId) {
        wechatBizRecruitService.acceptCardInvite(getCurrentUserId(), salesmanId);
    }
}
