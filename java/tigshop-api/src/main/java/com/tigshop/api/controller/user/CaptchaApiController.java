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

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.utils.R;
import com.tigshop.bean.vo.setting.CaptchaCheckVO;
import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/common/verification"})
@Tag(name = "行为验证")
public class CaptchaApiController {
    @Resource
    private CaptchaService captchaService;

    public CaptchaApiController() {
    }

    @PostMapping({"/captcha"})
    @Operation(summary = "获取验证码")
    @IgnoreResponseAdvice
    public R<CaptchaVO> captcha(HttpServletRequest request) {
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaType("blockPuzzle");
        captchaVO.setBrowserInfo(getRemoteId(request));
        ResponseModel responseModel = this.captchaService.get(captchaVO);
        CaptchaVO repData = (CaptchaVO)responseModel.getRepData();
        return R.ok(repData);
    }

    @PostMapping({"/get"})
    @Operation(summary = "获取验证码(废弃)")
    public ResponseModel get(@RequestBody CaptchaVO data, HttpServletRequest request) {
        assert request.getRemoteHost() != null;

        data.setBrowserInfo(getRemoteId(request));
        return this.captchaService.get(data);
    }

    @PostMapping({"/check"})
    @Operation(summary = "校验验证码")
    @IgnoreResponseAdvice
    public R<CaptchaCheckVO> check(@RequestBody CaptchaVO data, HttpServletRequest request) {
        data.setBrowserInfo(getRemoteId(request));
        ResponseModel responseModel = this.captchaService.check(data);
        CaptchaVO repData = (CaptchaVO)responseModel.getRepData();
        if (repData == null) {
            return R.fail();
        }
        CaptchaCheckVO cdata = new CaptchaCheckVO();
        BeanUtils.copyProperties(repData, cdata);
        return R.ok(cdata);
    }

    public ResponseModel verify(@RequestBody CaptchaVO data, HttpServletRequest request) {
        return this.captchaService.verification(data);
    }

    public static String getRemoteId(HttpServletRequest request) {
        String xfwd = request.getHeader("X-Forwarded-For");
        String ip = getRemoteIpFromXfwd(xfwd);
        String ua = request.getHeader("user-agent");
        return StringUtils.isNotBlank(ip) ? ip + ua : request.getRemoteAddr() + ua;
    }

    private static String getRemoteIpFromXfwd(String xfwd) {
        if (StringUtils.isNotBlank(xfwd)) {
            String[] ipList = xfwd.split(",");
            return StringUtils.trim(ipList[0]);
        } else {
            return null;
        }
    }
}