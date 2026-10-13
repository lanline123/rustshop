// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
  * 收货人信息表更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "收货人信息表参数")
public class UserAddressUpdateDTO {


    @Schema(description = "收货人id")
    private Integer id;

    @Schema(description ="收货人id")
    private Integer addressId;

    @Schema(description ="地址标签(家/公司/学校/自定义)")
    private String addressTag;

    @Schema(description ="详细地址")
    private String address;


    @Schema(description ="定位地址")
    private String positionAddress;

    @Schema(description ="收货人的邮编")
    private String postcode;

    @Schema(description ="收货人")
    private String consignee;

    @Schema(description ="收货人邮箱")
    private String email;

    @Schema(description ="手机")
    private String mobile;

    @Schema(description ="区号")
    private String mobileAreaCode;


    @Schema(description ="电话")
    private String telephone;

    @Schema(description ="是否默认")
    private Integer isDefault;

    @Schema(description ="是否选中")
    private Integer isSelected;

    @Schema(description ="收货人地址")
    private List<Integer> regionIds;


    @Schema(description ="收货人id")
    private Integer userId;

    @Schema(description ="纬度")
    private java.math.BigDecimal latitude;

    @Schema(description ="经度")
    private java.math.BigDecimal longitude;

    @Schema(description ="是否是系统")
    private Integer isSystem;
}
