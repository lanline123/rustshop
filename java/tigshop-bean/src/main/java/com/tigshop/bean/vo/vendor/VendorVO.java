// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.vendor;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/7/4 15:04
 */
@Data
@Schema(description = "供应商")
public class VendorVO {

    @Schema(description = "供应商Id")
    private Integer vendorId;

    @Schema(description = "供应商名称")
    @JsonTranslate
    private String vendorName;

    @Schema(description = "供应商logo")
    private String vendorLogo;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人手机")
    @Sensitive(type = SensitiveType.PHONE)
    private String contactMobile;

    @Schema(description = "登录账号")
    private String loginAccount;

    @Schema(description = "所属性质 1个人 2企业")
    private Integer type;

    @Schema(description = "供应商状态 1开启 2关闭")
    private Integer status;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "添加时间")
    private String addTime;
}
