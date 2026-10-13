package com.tigshop.bean.model.authority;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 后台角色表
 *
 */
@TableName(value = "admin_role")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRole implements Serializable {

    /**
     * 角色ID
     */
    @TableId(type = IdType.AUTO)
    private Integer roleId;

    /**
     * 名称
     */
    private String roleName;

    /**
     * 系统角色稳定标识(如 pos_cashier)
     */
    private String roleCode;

    /**
     * 描述
     */
    private String roleDesc;

    /**
     * [JSON]权限
     */
    private String authorityList;

    /**
     * admin或shop
     */
    private String adminType;

    /**
     * 商户id
     */
    private Integer merchantId;

    /**
     *
     */
    private Integer shopId;

    /**
     * 供应商id
     */
    private Integer vendorId;

    /**
     * 是否系统内置角色 0-否 1-是
     */
    @Builder.Default
    private Integer isSystem = 0;

}