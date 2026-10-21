package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 二级商户映射 model
 *
 * @author Tigshop团队
 */
@Data
@TableName("sub_merchant")
@Schema(description = "二级商户映射")
public class SubMerchant {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "子商户映射ID")
    private Long id;

    @Schema(description = "店铺ID")
    private Long shopId;

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

    @Schema(description = "关联进件申请ID")
    private Long applicationId;

    @Schema(description = "最大分账比例(%)，汇付进件开通值；空则回退渠道默认配置")
    private BigDecimal maxSplitRatio;

    @TableLogic
    @Schema(description = "逻辑删除:0未删 非0已删")
    private Integer isDel;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date modifyTime;
}
