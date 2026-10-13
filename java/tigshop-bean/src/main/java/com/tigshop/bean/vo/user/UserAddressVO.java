package com.tigshop.bean.vo.user;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author TigShop
 */
@Data
@Schema(name = "用户收获地址信息")
public class UserAddressVO {

    @Schema(description = "用户地址")
    private String address;


    @Schema(description ="定位地址")
    private String positionAddress;

    @Schema(description = "用户地址id")
    private Integer addressId;

    @Schema(description = "收货人名称")
    private String consignee;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "是否选中")
    private Integer isSelected;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description ="区号")
    private String mobileAreaCode;


    @Schema(description = "地区")
    private String regionName;

    @Schema(description = "地区")
    private List<String> regionNames;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "固定电话")
    private String telephone;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "地区id列表")
    private List<Integer> regionIds;

    @Schema(description = "收货人的邮编")
    private String postcode;

    @Schema(description = "收货地址别名")
    private String addressTag;

    @Schema(description = "纬度")
    private java.math.BigDecimal latitude;

    @Schema(description = "经度")
    private java.math.BigDecimal longitude;

    @Schema(description = "是否是系统添加")
    private Integer isSystem;

}
