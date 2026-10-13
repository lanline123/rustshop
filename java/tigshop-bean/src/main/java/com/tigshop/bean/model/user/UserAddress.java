package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 收货人信息表
 *
 * @author Tigshop
 */
@TableName(value = "user_address")
@Data
public class UserAddress implements Serializable {
    /**
     * 自增id
     */
    @TableId(type = IdType.AUTO)
    private Integer addressId;

    /**
     * 收货地址别名
     */
    private String addressTag;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 收货人的名字
     */
    private String consignee;

    /**
     * 收货人的email
     */
    private String email;

    /**
     * [JSON]收货地址id:number[]
     */
    private String regionIds;

    /**
     * [JSON]地区名称:string[]
     */
    private String regionNames;

    /**
     * 收货人的详细地址
     */
    private String address;

    /**
     * 定位地址
     */
    private String positionAddress;

    /**
     * 收货人的邮编
     */
    private String postcode;

    /**
     * 收货人的电话
     */
    private String telephone;

    /**
     * 收货人的手机号
     */
    private String mobile;

    /**
     * 区号
     */
    private String mobileAreaCode;


    /**
     * 是否默认
     */
    private Integer isDefault;

    /**
     * 是否选中
     */
    private Integer isSelected;

    /**
     * 是否是系统添加
     */
    private Integer isSystem;

    /**
     * 纬度
     */
    private java.math.BigDecimal latitude;

    /**
     * 经度
     */
    private java.math.BigDecimal longitude;
}
