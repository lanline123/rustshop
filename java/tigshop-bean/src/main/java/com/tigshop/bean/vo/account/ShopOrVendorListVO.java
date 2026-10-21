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
import com.tigshop.common.config.TimestampToDateSerializer;
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
@Schema(description = "店铺或供应商ListVO")
public class ShopOrVendorListVO {

    @Schema(description = "ID（店铺ID或供应商ID）")
    private Integer id;

    @Schema(description = "名称（店铺名称或供应商名称）")
    private String name;

    @Schema(description = "Logo（店铺logo或供应商logo）")
    private String logo;

    @Schema(description = "商户名称（仅店铺有此字段）")
    private String merchantName;

    @Schema(description = "状态（店铺状态1开业4暂停运营10关店）（供应商状态1开启2关闭）")
    private Integer status;

    @Schema(description = "主账号名称")
    private String adminUsername;

    @Schema(description = "创建时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String addTime;

    @Schema(description = "类型：shop-店铺，vendor-供应商")
    private String type;

}