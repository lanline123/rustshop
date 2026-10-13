// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.salesman;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.salesman.SalesmanGroup;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分销员分组VO
 *
 * @author kidd
 * @since 2025/06/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分销员分组参数")
public class SalesmanGroupVO {

    // *** SalesmanGroup ***

    @Schema(description = "分销员分组ID")
    private Integer groupId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "分组描述")
    private String describe;

    @Schema(description = "所属店铺ID")
    private Integer shopId;

    // *** Other ***

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "添加时间")
    private String addTime;

    public SalesmanGroupVO(SalesmanGroup salesmanGroup) {
        this.groupId = salesmanGroup.getGroupId();
        this.groupName = salesmanGroup.getGroupName();
        this.describe = salesmanGroup.getDescribe();
        this.shopId = salesmanGroup.getShopId();

        this.addTime = salesmanGroup.getAddTime().toString();
    }

}