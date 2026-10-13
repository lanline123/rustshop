// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.api.controller.product;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.model.promotion.ShortUrl;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.promotion.ShortUrlService;
import com.tigshop.service.setting.ConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.tigshop.bean.enums.setting.SettingsEnum.H5_DOMAIN;
import static com.tigshop.bean.enums.setting.SettingsEnum.PC_DOMAIN;


/**
 * @author Jayce
 * @create 2024/9/30 15:56
 */
@RestController
@Tag(name = "短链")
@Validated
@RequiredArgsConstructor
public class ShortUrlController {
    private final ShortUrlService shortUrlService;
    private final ConfigService configService;

    @GetMapping("/s/{code}")
    public void redirect(@PathVariable String code, HttpServletResponse response) throws IOException {
        ShortUrl shortUrl = shortUrlService.getByCode(code);
        String longUrl = shortUrl.getLongUrl();
        Integer type = shortUrl.getType();
        if(type==null || type<=1){
            //h5
            String configVal = configService.getConfigVal(H5_DOMAIN);
            if (StrUtil.isBlank(configVal)) {
                throw new GlobalException("请先配置H5域名");
            }
            response.sendRedirect(configVal + longUrl.replace("/mobile", ""));
        }else{
            //pc
            String configVal = configService.getConfigVal(PC_DOMAIN);
            if (StrUtil.isBlank(configVal)) {
                throw new GlobalException("请先配置PC域名");
            }
            response.sendRedirect(configVal + longUrl);
        }
    }
}
