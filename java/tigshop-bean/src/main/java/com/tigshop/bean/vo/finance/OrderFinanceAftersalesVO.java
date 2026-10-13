// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 售后申请表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "售后申请表参数")
public class OrderFinanceAftersalesVO {
    @Schema(description = "售后申请表ID")
    private Integer orderFinanceAftersalesId;

    @Schema(description ="售后申请表名称")
    private String orderFinanceAftersalesName;

    @Schema(description ="售后申请表图片")
    private String orderFinanceAftersalesPic;

    @Schema(description ="售后申请表排序")
    private Integer sortOrder;

    @Schema(description ="是否展示")
    private Integer isShow;

    @Schema(description ="状态")
    private Integer status;

    @Schema(description ="售后申请表描述")
    private String orderFinanceAftersalesDesc;
}