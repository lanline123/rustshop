// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.user;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员企业认证model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("user_company")
@Schema(description = "会员企业认证")
public class UserCompany implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "会员企业认证ID")
    private Integer id;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "认证类型：1 个人 2 企业")
    private Integer type;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "联系人手机")
    private String contactMobile;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "企业认证信息[json]")
    private String companyData;

    @Schema(description = "审核状态：1 待审核 2 审核通过 3 审核未通过")
    private Integer status;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "审核时间")
    private Long auditTime;

    @Schema(description = "添加时间")
    private Long addTime;
}
