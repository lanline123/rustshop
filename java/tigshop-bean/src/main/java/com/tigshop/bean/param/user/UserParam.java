package com.tigshop.bean.param.user;

import com.tigshop.bean.vo.user.UserAddressVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop项目组
 */
@Data
@Schema(name = "用户信息")
public class UserParam {

    // *** User ***

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "手机号")
    @Pattern(regexp = "^$|^[1-9]\\d*$", message = "手机号只能是数字")
    private String mobile;

    @Schema(description = "手机号是否验证")
    private Integer mobileValidated;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "邮箱是否验证")
    private Integer emailValidated;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "生日")
    private String birthday;

    @Schema(description = "余额")
    private BigDecimal balance;

    @Schema(description = "冻结余额")
    private BigDecimal frozenBalance;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "成长积分")
    private Integer growthPoints;

    @Schema(description = "地址ID")
    private Integer addressId;

    @Schema(description = "注册时间")
    private String regTime;

    @Schema(description = "最后登录时间")
    private Long lastLogin;

    @Schema(description = "最后登录IP")
    private String lastIp;

    @Schema(description = "等级ID")
    private Integer rankId;

    @Schema(description = "推荐人用户ID")
    private Integer referrerUserId;

    @Schema(description = "来源标签")
    private Integer fromTag;

    @Schema(description = "是否SVIP")
    private Integer isSvip;

    @Schema(description = "SVIP过期时间")
    private Long svipExpireTime;

    @Schema(description = "订单数量")
    private Integer orderCount;

    @Schema(description = "订单金额")
    private String orderAmount;

    @Schema(description = "历史产品ID列表")
    private Object historyProductIds;

    @Schema(description = "是否分销")
    private Integer isDistribution;

    @Schema(description = "分销注册时间")
    private String distributionRegisterTime;

    @Schema(description = "微信头像")
    private String wechatImg;

    @Schema(description = "是否企业认证")
    private Integer isCompanyAuth;

    @Schema(description = "会员状态；1-正常，0-禁用")
    private Integer status;

    // *** Other ***

    @Schema(description = "来源标签名称")
    private String fromTagName;

    @Schema(description = "旧密码")
    private String pwdConfirm;

    @Schema(description = "等级名称")
    private String rankName;

    @Schema(description = "等级图标")
    private String rankIco;

    @Schema(description = "折扣")
    private String discount;

    @Schema(description = "用户地址")
    private UserAddressVO userAddress;
}
