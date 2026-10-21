package com.tigshop.bean.model.authority;

//**---------------------------------------------------------------------+
//** 实体类文件 -- 权限
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Jayce
 * @create 2024/10/14 11:03
 */
@Data
@TableName(value = "authority")
@Schema(description = "权限")
public class Authority implements Serializable {

    /**
     * 自增id号
     */
    @TableId(value = "authority_id", type = IdType.AUTO)
    private Integer authorityId;

    /**
     * 权限编号
     */
    @TableField(value = "authority_sn")
    private String authoritySn;

    /**
     * 权限名称
     */
    @TableField(value = "authority_name")
    private String authorityName;

    /**
     * 该权限的父类ID
     */
    @TableField(value = "parent_id")
    private Integer parentId;

    /**
     * 该权限在目录显示的的顺序,数字越大顺序越靠后,同数字,id在前的先显示
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 是否在目录显示 1显示; 0不显示
     */
    @TableField(value = "is_show")
    private Integer isShow;

    /**
     * JSON:子权限[{auth_name|auth_sn}]
     */
    @TableField(value = "child_auth")
    private String childAuth;

    /**
     * 路由链接
     */
    @TableField(value = "route_link")
    private String routeLink;

    /**
     *
     ICO图标
     */
    @TableField(value = "authority_ico")
    private String authorityIco;

    /**
     * 是否系统目录，是的话只能有限编辑或隐藏，不能删除
     */
    @TableField(value = "is_system")
    private Integer isSystem;

    /**
     * admin管理后台使用shop店铺使用
     */
    @TableField(value = "admin_type")
    private String adminType;
}
