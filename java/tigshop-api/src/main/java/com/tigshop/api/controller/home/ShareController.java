// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.api.controller.home;


import com.tigshop.service.decorate.DecorateShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tigshop团队
 * @create 2025/4/30 14:17
 */
@RestController
@RequestMapping("/api/home/share")
@Tag(name = "装修分享模板")
public class ShareController {

    @Resource
    private DecorateShareService decorateShareService;
    @GetMapping("/import")
    @Operation(summary = "装修分享导入")
    public Object getImport(@RequestParam(value = "sn") String sn,
                            @RequestParam(value = "token") String token) {
        return decorateShareService.getInfoBySn(sn,token);
    }
}
