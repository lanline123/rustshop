// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.user;

import com.tigshop.bean.dto.login.*;
import com.tigshop.bean.param.login.BindWechatParam;
import com.tigshop.bean.param.login.CheckEmailParam;
import com.tigshop.bean.param.user.LoginGetMobileParam;
import com.tigshop.bean.param.user.LoginUpdateUserOpenIdParam;
import com.tigshop.bean.vo.login.LoginWechatEventVO;
import com.tigshop.bean.vo.user.WechatLoginUrlVO;
import com.tigshop.bean.vo.user.WxLoginInfoByCodeVO;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * 登录控制器
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/login"))
@Validated
@Tag(name = "登录控制器")
public class LoginController {
    @Resource
    private ConfigService configService;

    @Resource
    private UserService userService;

    @RequestMapping("/wechatServer")
    @Operation(summary = "微信服务器校验")
    public void wechatServer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        userService.wechatServer(request, response);
    }

    @GetMapping("/getQuickLoginSetting")
    @Operation(summary = "获取快捷登录设置")
    public Map<String, Object> getQuickLoginSetting() {
        return configService.getQuickLoginSetting();
    }

    @GetMapping("/getWxLoginUrl")
    @Operation(summary = "获得微信授权url")
    public WechatLoginUrlVO getWechatLoginUrl(@RequestParam(value = "url", required = false) String redirectUrl) {
        return configService.getWechatLoginUrl(redirectUrl);
    }

    @PostMapping("/sendMobileCode")
    @Operation(summary = "发送短信验证码")
    public void sendMobileCode(@RequestBody RegisterSmsDTO dto) {
        configService.sendSms(dto);
    }

    @PostMapping("/sendEmailCode")
    @Operation(summary = "发送邮箱验证码")
    public void sendEmailCode(@RequestBody RegisterEmailDTO dto) {
        configService.sendEmail(dto);
    }

    @PostMapping("/checkMobile")
    @Operation(summary = "校验手机号,仅用于重置密码")
    public String checkMobile(@RequestBody @Valid CheckMobileDTO dto) {
        return userService.checkMobile(dto);
    }

    @PostMapping("/checkEmail")
    @Operation(summary = "校验邮箱,仅用于重置密码")
    public String checkEmail(@RequestBody @Valid CheckEmailParam param) {
        return userService.checkEmail(param);
    }

    @PostMapping("/forgetPassword")
    @Operation(summary = "修改密码")
    public void forgetPassword(@RequestBody @Valid ForgetPasswordDTO dto) {
        userService.modifyPassword(dto);
    }

    @PostMapping("/bindMobile")
    @Operation(summary = "绑定手机")
    public String bindMobile(@RequestBody @Valid BindMobileDTO dto) {
        return userService.bindMobile(dto);
    }

    @PostMapping("/wechatEvent")
    @Operation(summary = "检测用户扫码后处理事件")
    public LoginWechatEventVO wechatEvent(@RequestBody @Valid WechatEventDTO dto) {
        return userService.wechatEvent(dto);
    }

    @PostMapping("/getMobile")
    @Operation(summary = "获取用户手机号")
    public String getMobile(@RequestBody @Validated LoginGetMobileParam param) {
        return userService.getMobile(param);
    }

    @PostMapping("/updateUserOpenid")
    @Operation(summary = "获取用户openid")
    public void updateUserOpenId(@RequestBody @Validated LoginUpdateUserOpenIdParam param) {
        userService.updateUserOpenId(param);
    }

    @GetMapping("/getWxLoginInfoByCode")
    @Operation(summary = "通过微信code获得微信用户信息")
    public WxLoginInfoByCodeVO getWxLoginInfoByCode(@RequestParam("code") @NotBlank(message = "code不能为空") String code) throws WxErrorException {
        return userService.getWxLoginInfoByCode(code);
    }

    @PostMapping("/bindWechat")
    @Operation(summary = "绑定微信公众号")
    public void bindWechat(@RequestBody @Validated BindWechatParam param) {
        userService.bindWechat(param);
    }

    @GetMapping("/unbindWechat")
    @Operation(summary = "解绑微信公众号")
    public void unbindWechat() {
        userService.unbindWechat();
    }
}