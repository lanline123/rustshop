// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.vendor;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;


/**
 * @author Tigshop团队
 * @create 2025/7/16 9:16
 */
@Data
public class VendorUpdateInfoParam {


    @Schema(description ="供应商图片")
    private String vendorLogo;

    @Schema(description ="供应商名称")
    private String vendorName;

    @Schema(description ="联系电话")
    private String contactPhone;

    @Schema(description = "描述")
    @Size(max = 255, message = "描述长度不能超过255个字符")
    private String description;

    @Schema(description = "供应商地址")
    private List<Integer> vendorAddress;

    @Schema(description = "详细地址")
    private String detailedAddress;
}
