package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.*;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户自提信息表
 * @author Tigshop项目组
 */
@Data
@TableName("user_pickup_info")
@Builder
public class UserPickupInfo implements Serializable {

    /**
     * 自增id
     */
    @TableId(value = "user_pickup_id", type = IdType.AUTO)
    private Integer userPickupId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 收货人的名字
     */
    @TableField("consignee")
    private String consignee;

    /**
     * 收货人的手机号
     */
    @Sensitive(type = SensitiveType.PHONE)
    @TableField("mobile")
    private String mobile;

    /**
     * 区号
     */
    @TableField("mobile_area_code")
    private String mobileAreaCode;

    /**
     * 是否默认
     */
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 是否选中
     */
    @TableField("is_selected")
    private Integer isSelected;
}
