// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.user;

import cn.hutool.json.JSONObject;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.annotation.SensitiveJson;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员企业认证视图对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "会员企业认证视图对象")
public class UserCompanyVO implements Serializable {
    @Schema(description = "会员企业认证ID")
    private Integer id;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "认证类型：1 个人 2 企业")
    private Integer type;

    @Schema(description = "联系人名称")
    private String contactName;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "联系人手机")
    private String contactMobile;

    @Schema(description = "企业名称")
    private String companyName;

    @SensitiveJson(keys = {"contactPhone"})
    @Schema(description = "企业认证信息[json]")
    private JSONObject companyData;

    @Schema(description = "审核状态：1 待审核 2 审核通过 3 审核未通过")
    private Integer status;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "审核时间")
    private String auditTime;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "是否企业认证")
    private Integer isCompanyAuth;

    @Schema(description = "用户信息")
    private UserVO user;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "类型文本")
    private String typeText;

    // 嵌套的用户信息类
    @Data
    public static class UserVO {
        @Schema(description = "用户id")
        private Integer userId;

        @Schema(description = "用户名")
        private String username;

        @Sensitive(type = SensitiveType.PHONE)
        @Schema(description = "手机号")
        private String mobile;

        @Schema(description = "是否企业认证")
        private Integer isCompanyAuth;
    }
}
