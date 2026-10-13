package com.tigshop.bean.vo.login;

//**---------------------------------------------------------------------+
//** 服务层文件 -- 会员
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jayce
 * @create 2024/9/30 11:11
 */
@Getter
@Setter
@Builder
@Schema(description = "微信扫码登录返回对象(实际第三方通用)")
public class LoginWechatEventVO {
    @Schema(description = "token")
    private String token;

    @Schema(description = "类型: 1-正常用户登录 2-跳转到绑定手机号")
    private Integer type;

    @Schema(description = "openData")
    private Object openData;

    @Schema(description = "登录后前端跳转地址")
    private String returnUrl;

}
