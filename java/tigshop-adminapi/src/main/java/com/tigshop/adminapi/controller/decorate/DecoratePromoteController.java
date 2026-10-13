// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.decorate;

import com.tigshop.bean.dto.decorate.DecorateShareInfoDTO;
import com.tigshop.bean.vo.decorate.DecoratePromoteVO;
import com.tigshop.bean.vo.decorate.DecorateShareInfoVO;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.promotion.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 装修推广模块
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/decorate/promote")
@Tag(name = "装修推广模块", description = "装修推广短链相关接口")
@Validated
public class DecoratePromoteController {

    @Resource
    private ShortUrlService shortUrlService;

    @Resource
    private DecorateService decorateService;

    @PostMapping("/generateShortLink")
    @Operation(summary = "根据长链接生成装修推广短链")
    public DecoratePromoteVO generateShortLink(
            @RequestParam @NotBlank(message = "链接不能为空") String longUrl) {
        return shortUrlService.generateDecoratePromoteShortLink(longUrl);
    }

    @PostMapping("/saveShareInfo")
    @Operation(summary = "保存装修分享信息")
    public boolean saveShareInfo(@RequestBody @Valid DecorateShareInfoDTO dto) {
        return decorateService.saveShareInfo(dto);
    }

    @GetMapping("/shareInfo")
    @Operation(summary = "获取装修分享信息")
    public DecorateShareInfoVO shareInfo(
            @RequestParam @NotNull(message = "装修ID不能为空") Integer decorateId) {
        return decorateService.getShareInfo(decorateId);
    }
}
