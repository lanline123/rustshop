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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.user.UserCouponConstants.*;

/**
 * 用户优惠券创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "用户优惠券参数")
public class UserCouponCreateDTO {
    @Schema(description ="用户优惠券名称")
    @NotBlank(message = _NOT_NULL)
    @Size(max = 50, message = _OVER_LENGTH)
    private String userCouponName;

    @Schema(description ="用户优惠券图片")
    private String userCouponPic;

    @Schema(description ="用户优惠券排序")
    private Integer sortOrder;

    @Schema(description ="是否展示")
    private Integer isShow;

    @Schema(description ="状态")
    private Integer status;

    @Schema(description ="用户优惠券描述")
    private String userCouponDesc;
}
