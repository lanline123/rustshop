// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "提交售后反馈记录")
public class AftersalesRecordCreateDTO {

    @Schema(description = "日志")
    private String logInfo;

    @Schema(description = "售后管理排序")
    private Integer aftersaleId;

    @Schema(description = "售后管理描述")
    private List<Object> returnPic;

    @Schema(description = "单号")
    private String trackingNo;

    @Schema(description = "物流")
    private String logisticsName;
}
