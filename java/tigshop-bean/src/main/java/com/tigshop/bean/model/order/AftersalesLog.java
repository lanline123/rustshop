// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 售后记录model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("aftersales_log")
@Schema(description = "售后记录")
public class AftersalesLog implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer logId;

    /**
     * 售后申请id
     */
    private Integer aftersaleId;

    /**
     * 申请原因
     */
    private String logInfo;

    /**
     * 添加时间
     */
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String addTime;

    /**
     * 后台操作人
     */
    private String adminName;

    /**
     * 退款金额
     */
    private BigDecimal refundMoney;

    /**
     * 售后类型
     */
    private Integer refundType;

    /**
     * 售后描述
     */
    private String refundDesc;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 回复图片
     */
    private String returnPic;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 供应商id
     */
    private Integer vendorId;

}