package com.tigshop.bean.model.vendor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 供应商员工列表
 * @TableName admin_user_vendor
 */
@TableName(value ="admin_user_vendor")
@Data
public class AdminUserVendor {
    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 管理员ID
     */
    private Integer adminId;

    /**
     * 会员id
     */
    private Integer userId;

    /**
     * 供应商ID
     */
    private Integer vendorId;

    /**
     * 员工名称
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 员工权限 [JSON]
     */
    private String authList;

    /**
     * 是否停用：1 是，0 否
     */
    private Integer isUsing;

    /**
     * 是否为管理员：1 是，0 否
     */
    private Integer isAdmin;

    /**
     * 创建时间
     */
    private Long addTime;

    /**
     * 权限组ID
     */
    private Integer roleId;
}