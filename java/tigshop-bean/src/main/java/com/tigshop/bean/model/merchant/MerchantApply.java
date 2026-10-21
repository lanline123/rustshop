// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 入驻申请model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("merchant_apply")
@Schema(description = "入驻申请")
public class MerchantApply {
    @Schema(description = "商家入驻申请ID")
    @TableId(type = IdType.AUTO)
    private Integer merchantApplyId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "状态，1：待审核，10：审核通过，20：审核未通过")
    private Integer status;

    @Schema(description = "类型，1：个人，2：企业")
    private Integer type;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "联系人手机")
    private String contactMobile;

    @Schema(description = "审核时间")
    private Long auditTime;

    @Schema(description = "审核管理员ID(admin_user.admin_id)，0 表示系统自动通过或未审核")
    private Integer auditAdminId;

    @Schema(description = "申请资料shop信息JSON")
    private String shopData;

    @Schema(description = "申请资料基本信息JSON")
    private String baseData;

    @Schema(description = "申请资料的商户信息JSON")
    private String merchantData;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "申请主体信息")
    private String corporateName;

    @Schema(description = "审核备注")
    private String auditRemark;
}