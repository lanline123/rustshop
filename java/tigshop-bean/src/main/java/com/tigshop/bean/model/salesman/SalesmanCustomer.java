package com.tigshop.bean.model.salesman;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存日志model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("salesman_customer")
@Schema(description = "分销客户记录")
public class SalesmanCustomer {
    @TableId(value = "salesman_customer_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer salesmanCustomerId;

    @Schema(description = "分销员ID")
    private Integer salesmanId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "客户绑定渠道")
    private String channel;

    @Schema(description = "客户备注")
    private String remark = "";

}
