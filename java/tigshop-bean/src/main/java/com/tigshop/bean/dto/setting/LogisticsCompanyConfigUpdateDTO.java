package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "物流公司配置更新参数")
public class LogisticsCompanyConfigUpdateDTO {
    @NotNull(message = "配置ID不能为空")
    @Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;

    @Schema(description = "物流公司ID")
    private Integer logisticsId;

    @Schema(description = "客户编码")
    private String customerName;

    @Schema(description = "密码")
    private String customerPwd;

    @Schema(description = "月结号")
    private String monthCode;

    @Schema(description = "网点编码")
    private String sendSite;

    @Schema(description = "网点名称")
    private String sendStaff;

    @Schema(description = "快递业务类型")
    private String expType;

    @Schema(description = "快递100电子面单-电子面单账户/月结账号 partnerId")
    private String kuaidi100PartnerId;

    @Schema(description = "快递100电子面单-电子面单密码 partnerKey")
    private String kuaidi100PartnerKey;

    @Schema(description = "快递100电子面单-电子面单密钥 partnerSecret")
    private String kuaidi100PartnerSecret;

    @Schema(description = "快递100电子面单-收件网点名 net")
    private String kuaidi100Net;

    @Schema(description = "快递100电子面单-承载快递员名 checkMan")
    private String kuaidi100CheckMan;

    @Schema(description = "快递100电子面单-产品类型 expType")
    private String kuaidi100ExpType;

    @Schema(description = "快递100电子面单-主单模板ID tempId（IMAGE/HTML/CLOUD 网点面单必填）")
    private String kuaidi100TempId;

    @Schema(description = "快递100电子面单-子单模板ID childTempId")
    private String kuaidi100ChildTempId;

    @Schema(description = "快递100电子面单-回单模板ID backTempId")
    private String kuaidi100BackTempId;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否启用：1启用，0禁用")
    private Integer isEnabled;
}
