package com.tigshop.bean.model.user;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户表
 *
 * @author Tigshop
 */
@TableName(value = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "会员资料自增id")
    private Integer userId;

    @Schema(description = "是否是公司授权会员")
    private Integer isCompanyAuth;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "手机号是否已验证")
    private Integer mobileValidated;

    @Schema(description = "会员邮箱")
    private String email;

    @Schema(description = "邮箱是否已验证")
    private Integer emailValidated;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "出生日期")
    private String birthday;

    @Schema(description = "用户现有余额")
    private BigDecimal balance;

    @Schema(description = "用户冻结余额")
    private BigDecimal frozenBalance;

    @Schema(description = "分销佣金可用余额（D1107，独立于普通余额）")
    private BigDecimal commissionBalance;

    @Schema(description = "分销佣金冻结余额（D1107）")
    private BigDecimal frozenCommissionBalance;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "成长值")
    private Integer growthPoints;

    @Schema(description = "当前会员地址")
    private Integer addressId;

    @Schema(description = "注册时间")
    private Long regTime;

    @Schema(description = "最后一次登录时间")
    private Integer lastLogin;

    @Schema(description = "最后一次登录IP")
    private String lastIp;

    @Schema(description = "会员等级id")
    private Integer rankId;

    @Schema(description = "推荐人会员id")
    private Integer referrerUserId;

    @Schema(description = "公众号1,小程序2,H5,PC4,APP5")
    private Integer fromTag;

    @Schema(description = "是否是超级会员 1是 0不是 2过期")
    private Integer isSvip;

    @Schema(description = "会员过期时间")
    private Integer svipExpireTime;

    @Schema(description = "累积消费次数")
    private Integer orderCount;

    @Schema(description = "累积消费金额")
    private BigDecimal orderAmount;

    @Schema(description = "【JSON】历史浏览记录id：number[]")
    private String historyProductIds;

    @Schema(description = "是否是分销员")
    private Integer isDistribution;

    @Schema(description = "分销员注册时间")
    private Integer distributionRegisterTime;

    @Schema(description = "微信二维码图片")
    private String wechatImg;

    @Schema(description = "会员状态；1-正常，0-禁用")
    private Integer status;

    @Schema(description = "会员类型：0会员，1游客")
    private Integer userType;

    @Schema(description = "管理员备注")
    private String adminNote;

    @Schema(description = "标记")
    private Integer mark;

    public String getName(){
        return StrUtil.isNotBlank(this.nickname) ? this.nickname : this.username;
    }

}