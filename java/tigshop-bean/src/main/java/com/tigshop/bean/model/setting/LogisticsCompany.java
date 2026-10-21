// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 配送方式配置信息model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("logistics_company")
@Schema(description = "配送方式配置信息")
public class LogisticsCompany {
    @TableId(value = "logistics_id", type = IdType.AUTO)
    @Schema(description = "自增id号")
    private Integer logisticsId;

    @Schema(description = "配送方式的快递公司代号")
    private String logisticsCode;

    @Schema(description = "配送方式名称")
    private String logisticsName;

    @Schema(description = "配送方式描述")
    private String logisticsDesc;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "店铺ID")
    private Integer shopId;

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
