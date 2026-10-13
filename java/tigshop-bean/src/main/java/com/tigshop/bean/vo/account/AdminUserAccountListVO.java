// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.account;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺或供应商ListVO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账号管理分页列表")
public class AdminUserAccountListVO {

    @Schema(description = "adminId")
    private Integer adminId;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "账号名称")
    private String username;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "创建时间")
    private String addTime;

    @Schema(description = "关联店铺数")
    private Long shopCount;

    @Schema(description = "关联供应商")
    private Long vendorCount;

    @Schema(description = "关联门店")
    private Long storeCount;

    @Schema(description = "是否主账号：1 是，0 否")
    private Integer isAdmin;

}
