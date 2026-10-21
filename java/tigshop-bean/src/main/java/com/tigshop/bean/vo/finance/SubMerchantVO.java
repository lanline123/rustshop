package com.tigshop.bean.vo.finance;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 子商户视图对象
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "子商户视图对象")
public class SubMerchantVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "子商户号")
    private String subMchId;

    @Schema(description = "子商户AppID")
    private String subAppid;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "状态:0待激活 1已激活 2已冻结")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "关联进件ID")
    private Long applicationId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;
}
