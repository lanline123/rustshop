package com.tigshop.bean.vo.user;

import com.tigshop.bean.dto.user.UserDTO;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserItemVO {

    @Schema(description = "来源标签名称")
    private String fromTagName;

    @Schema(description = "会员标签列表")
    private List<UserMemberTagVO> memberTags;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatar;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
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

    @Schema(description = "旧密码")
    private String pwdConfirm;

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

    @Schema(description = "等级名称")
    private String rankName;

    @Schema(description = "等级图标")
    private String rankIco;

    @Schema(description = "折扣")
    private String discount;

    @Schema(description = "用户地址")
    private UserAddressVO userAddress;

    @Schema(description = "管理员备注")
    private String adminNote;

    @Schema(description = "标记")
    private Integer mark;

    public UserItemVO(UserDTO item) {
        this.fromTagName = item.getFromTagName();
        this.userId = item.getUserId();
        this.username = item.getUsername();
        this.avatar = item.getAvatar();
        this.mobile = item.getMobile();
        this.mobileValidated = item.getMobileValidated();
        this.email = item.getEmail();
        this.emailValidated = item.getEmailValidated();
        this.nickname = item.getNickname();
        this.password = item.getPassword();
        this.pwdConfirm = item.getPwdConfirm();
        this.birthday = item.getBirthday();
        this.balance = item.getBalance();
        this.frozenBalance = item.getFrozenBalance();
        this.points = item.getPoints();
        this.growthPoints = item.getGrowthPoints();
        this.addressId = item.getAddressId();
        this.regTime = item.getRegTime();
        this.lastLogin = item.getLastLogin();
        this.lastIp = item.getLastIp();
        this.rankId = item.getRankId();
        this.referrerUserId = item.getReferrerUserId();
        this.fromTag = item.getFromTag();
        this.isSvip = item.getIsSvip();
        this.svipExpireTime = item.getSvipExpireTime();
        this.orderCount = item.getOrderCount();
        this.orderAmount = item.getOrderAmount();
        this.historyProductIds = item.getHistoryProductIds();
        this.isDistribution = item.getIsDistribution();
        this.distributionRegisterTime = item.getDistributionRegisterTime();
        this.wechatImg = item.getWechatImg();
        this.isCompanyAuth = item.getIsCompanyAuth();
        this.rankName = item.getRankName();
        this.rankIco = item.getRankIco();
        this.discount = item.getDiscount();
        this.userAddress = item.getUserAddress();
        this.adminNote = item.getAdminNote();
        this.mark = item.getMark();
    }
}
