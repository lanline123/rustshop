// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.common;

import com.tigshop.bean.vo.config.GetLoginProtocolContentVO;
import com.tigshop.bean.vo.config.GetLoginProtocolVO;
import com.tigshop.bean.vo.config.InitConfigSettingsVO;
import com.tigshop.bean.vo.config.ThemeSettingsVO;
import com.tigshop.bean.vo.setting.AreaCodeVO;
import com.tigshop.bean.vo.setting.config.AfterSalesSettingsVO;
import com.tigshop.common.enums.DataType;
import com.tigshop.service.setting.AreaCodeService;
import com.tigshop.service.setting.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 配置
 *
 * @author Tigshop团队
 * @create 2025年01月15日 17:18
 */
@RestController
@RequestMapping("/api/common/config")
@Tag(name = "配置")
public class ConfigController {
    @Resource
    private ConfigService configService;

    @Resource
    private AreaCodeService areaCodeService;


    @GetMapping("/initConfigSettings")
    @Operation(summary = "获取初始化配置项")
    public InitConfigSettingsVO getInitConfigSettings(@RequestParam(value = "previewId", required = false) Integer previewId) {
        return configService.getInitConfigSettings(previewId);
    }

    @GetMapping("/themeSettings")
    @Operation(summary = "获取主题配置项")
    public ThemeSettingsVO getThemeSettings() {
        return configService.getThemeSettings();
    }

    @GetMapping("/productDetailStyle")
    @Operation(summary = "获取商品详情页风格；1-样式一，2-样式二")
    public Integer getProductDetailStyle() {
        return configService.getProductDetailStyleType();
    }


    @GetMapping("/searchStyle")
    @Operation(summary = "获取搜索页风格；1-样式一，2-样式二")
    public Integer getSearchStyle() {
        return configService.getSearchStyleType();
    }

    @GetMapping("/mobileSearchStyle")
    @Operation(summary = "获取移动端搜索风格；1-普通商品搜索，2-门店聚合搜索")
    public Integer getMobileSearchStyle() {
        return configService.getMobileSearchStyleType();
    }

    @GetMapping("/base")
    @Operation(summary = "获取基础配置")
    public Map<String, Object> getBase(@RequestParam(value = "previewId", required = false) Integer previewId) {
        return configService.getBase(previewId);
    }

    @GetMapping("/afterSalesService")
    @Operation(summary = "获取售后服务")
    public AfterSalesSettingsVO afterSalesService(){
        AfterSalesSettingsVO afterSalesSettings = configService.getAfterSalesSettings();
        return afterSalesSettings;
    }

    @GetMapping("/mobileAreaCode")
    @Operation(summary = "获取移动端区号")
    public List<AreaCodeVO> mobileAreaCode(){
        return areaCodeService.mobileAreaCode();
    }

    @Operation(summary = "获取登录协议")
    @GetMapping("/getLoginProtocol")
    public GetLoginProtocolVO getLoginProtocol() {
        return configService.getLoginProtocol();
    }

    @Operation(summary = "获取登录协议内容  termsOfService:服务协议；privacyPolicy:隐私政策；afterSalesService:售后服务")
    @GetMapping("/getLoginProtocolContent")
    public GetLoginProtocolContentVO getLoginProtocolContentVO(String code) {
        return configService.getLoginProtocolContentVO(code);
    }
}