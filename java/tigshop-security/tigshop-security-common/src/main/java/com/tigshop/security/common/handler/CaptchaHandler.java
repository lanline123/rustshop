// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.security.common.handler;

import cn.hutool.core.util.StrUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.tigshop.common.constant.Constants.LOGIN_ERROR;
import static com.tigshop.common.constant.login.LoginConstants.ERROR_THRESHOLD;

/**
 * 行为验证
 *
 * @author Jayce
 * @create 2024年11月25日 14:17
 */
@Component
public class CaptchaHandler {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CaptchaService captchaService;

    /**
     * 验证验证码(true=验证通过，false=需要验证码但未通过)
     * 注意：此方法不增加错误次数，仅用于登录前校验
     *
     * @param username     用户名
     * @param verifyToken 验证码
     * @return 是否验证通过
     */
    public Boolean captcha(String username, String verifyToken) {
        int count = getCurrentErrorCount(username);
        if (count >= ERROR_THRESHOLD) {
            if (StrUtil.isEmpty(verifyToken) || !validateCaptcha(verifyToken)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取当前错误次数（不增加计数）
     *
     * @param username 用户名
     * @return 错误次数
     */
    public int getCurrentErrorCount(String username) {
        Object errorCountObj = redisTemplate.opsForValue().get(StrUtil.format("{}{}", LOGIN_ERROR, username));
        return (errorCountObj == null) ? 0 : Integer.parseInt(errorCountObj.toString());
    }

    /**
     * 增加错误次数并返回新的次数
     *
     * @param username 用户名
     * @return 新的错误次数
     */
    public int incrementErrorCount(String username) {
        Object errorCountObj = redisTemplate.opsForValue().get(StrUtil.format("{}{}", LOGIN_ERROR, username));
        int count = (errorCountObj == null) ? 1 : Integer.parseInt(errorCountObj.toString()) + 1;
        redisTemplate.opsForValue().set(StrUtil.format("{}{}", LOGIN_ERROR, username), count);
        return count;
    }

    /**
     * 获取错误次数（会增加计数）
     * @deprecated 建议使用 {@link #getCurrentErrorCount(String)} 和 {@link #incrementErrorCount(String)} 分别获取和增加计数
     *
     * @param username 用户名
     * @return 错误次数
     */
    @Deprecated
    public int getErrorCount(String username) {
        return incrementErrorCount(username);
    }

    /**
     * 验证验证码是否正确
     *
     * @param verifyToken 验证码
     * @return 是否正确
     */
    public boolean validateCaptcha(String verifyToken) {
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(verifyToken);
        ResponseModel response = captchaService.verification(captchaVO);
        return response.isSuccess();
    }

    /**
     * 重置错误次数
     *
     * @param username 用户名
     */
    public void resetErrorCount(String username) {
        redisTemplate.opsForValue().set(StrUtil.format("{}{}", LOGIN_ERROR, username), 0);
    }

}
