// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order;

import com.tigshop.bean.dto.order.ReturnPicDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 售后记录VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "售后记录参数")
public class AftersalesLogVO {
    @Schema(description = "售后记录ID")
    private Integer logId;

    @Schema(description = "用户名")
    private String adminName;

    @Schema(description = "售后id")
    private String aftersalesId;

    @Schema(description = "售后记录图片")
    private List<ReturnPicDTO> returnPic;

    @Schema(description = "日志信息")
    private String logInfo;

    @Schema(description = "售后描述")
    private String refundDesc;

    @Schema(description = "退款金额")
    private BigDecimal refundMoney;

    @Schema(description = "售后类型")
    private Integer refundType;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "供应商id")
    private Integer vendorId;
}