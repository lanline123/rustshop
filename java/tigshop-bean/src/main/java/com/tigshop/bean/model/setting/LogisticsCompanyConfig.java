package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("logistics_company_config")
@Schema(description = "物流公司配置信息")
public class LogisticsCompanyConfig {
    @TableId(value = "config_id", type = IdType.AUTO)
    @Schema(description = "配置ID")
    private Integer configId;

    @Schema(description = "物流公司ID")
    private Integer logisticsId;

    @Schema(description = "店铺ID（平台=0，店铺/门店>0）")
    private Integer shopId;

    @Schema(description = "供应商ID（非供应商=0，供应商>0）")
    private Integer vendorId;

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

    @Schema(description = "是否默认：1默认，0非默认")
    private Integer isDefault;

    @Schema(description = "是否启用：1启用，0禁用")
    private Integer isEnabled;
}
