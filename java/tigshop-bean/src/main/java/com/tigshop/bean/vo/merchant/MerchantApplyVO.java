// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.merchant;

import cn.hutool.json.JSONObject;
import com.tigshop.bean.enums.merchant.MerchantApplyStatusEnum;
import com.tigshop.bean.model.merchant.MerchantApply;
import com.tigshop.bean.model.user.User;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.annotation.SensitiveJson;
import com.tigshop.common.enums.SensitiveType;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 入驻申请VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入驻申请参数")
public class MerchantApplyVO {

    // *** MerchantApply ***

    @Schema(description = "商家入驻申请ID")
    private Integer merchantApplyId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "状态，1：待审核，10：审核通过，20：审核未通过")
    private Integer status;

    @Schema(description = "类型，1：个人，2：企业")
    private Integer type;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "联系人手机")
    @Sensitive(type = SensitiveType.PHONE)
    private String contactMobile;

    @SensitiveJson(keys = {"contactMobile"})
    @Schema(description = "申请资料shop信息JSON")
    private JSONObject shopData;

    @Schema(description = "申请资料基本信息JSON")
    private JSONObject baseData;

    @SensitiveJson(keys = {"contactPhone", "customerServicePhone"})
    @Schema(description = "申请资料的商户信息JSON")
    private JSONObject merchantData;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "申请主体信息")
    private String corporateName;

    @Schema(description = "审核备注")
    private String auditRemark;

    // *** User ***

    @Schema(description = "用户")
    private User user;

    // *** Other ***

    @Schema(description = "审核文案")
    private String statusText;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "审核时间")
    private String auditTime;

    @Schema(description = "审核管理员ID(admin_user.admin_id)，0 表示系统自动通过或未审核")
    private Integer auditAdminId;

    @Schema(description = "审核管理员姓名，0 或管理员已删除时返回空串")
    private String auditAdminName;

    public MerchantApplyVO(MerchantApply merchantApply, User user) {
        this.merchantApplyId = merchantApply.getMerchantApplyId();
        this.userId = user.getUserId();
        this.shopTitle = merchantApply.getShopTitle();
        this.status = merchantApply.getStatus();
        this.type = merchantApply.getType();
        this.contactName = merchantApply.getContactName();
        this.contactMobile = merchantApply.getContactMobile();

        this.shopData = merchantApply.getShopData() != null ? JsonUtil.fromJson(merchantApply.getShopData(), JSONObject.class) : null;
        this.baseData = merchantApply.getBaseData() != null ? JsonUtil.fromJson(merchantApply.getBaseData(), JSONObject.class) : null;
        this.merchantData = merchantApply.getMerchantData() != null ? JsonUtil.fromJson(merchantApply.getMerchantData(), JSONObject.class) : null;

        this.companyName = merchantApply.getCompanyName();
        this.corporateName = merchantApply.getCorporateName();
        this.auditRemark = merchantApply.getAuditRemark();

        this.user = user;

        this.statusText = MerchantApplyStatusEnum.getStatusName(merchantApply.getStatus());
        this.addTime = TigUtils.handelTime(merchantApply.getAddTime());
        this.auditTime = TigUtils.handelTime(merchantApply.getAuditTime());
        this.auditAdminId = merchantApply.getAuditAdminId();
        this.auditAdminName = "";

    }
}