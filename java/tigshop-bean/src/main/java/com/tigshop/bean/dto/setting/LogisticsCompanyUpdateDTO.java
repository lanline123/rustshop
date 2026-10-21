// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
  * 配送方式配置信息更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "配送方式配置信息参数")
public class LogisticsCompanyUpdateDTO {
    @Schema(description ="配送方式配置信息id")
    private Integer id;

    @Schema(description ="配送方式配置信息排序")
    private Integer sortOrder;

    @Schema(description ="是否展示")
    private Integer isShow;

    @Schema(description ="配送方式名称")
    private String logisticsName;

    @Schema(description = "配送方式的快递公司代号")
    private String logisticsCode;

    @Schema(description = "配送方式描述")
    private String logisticsDesc;

    @Schema(description = "无服务调用物流编号")
    private String apiLogisticsCode;


    @Schema(description = "快递100快递公司编码（小写，如 shunfeng/yuantong；留空则走内置映射）")
    private String kuaidi100Code;

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

    @Schema(description = "是否启用：1启用，0禁用")
    private Integer isEnabled;
}
