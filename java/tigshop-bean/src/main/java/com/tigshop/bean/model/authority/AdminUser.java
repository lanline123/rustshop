// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.authority;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台用户
 * @author Jayce
 * @create 2024-09-27 10:34:44
 */
@TableName(value ="admin_user")
@Data
@Schema(description = "后台用户")
public class AdminUser implements Serializable {
    /**
     * 自增id号,管理员代码
     */
    @TableId(value = "admin_id", type = IdType.AUTO)
    private Integer adminId;

    /**
     * 管理员登录名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 管理员类型，admin:管理员，shop:店铺
     */
    @TableField(value = "admin_type")
    private String adminType;

    /**
     * 手机号
     */
    @TableField(value = "mobile")
    private String mobile;

    /**
     * 头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 管理员登录密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 管理员邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 管理员添加时间
     */
    @TableField(value = "add_time")
    private Long addTime;

    /**
     * [JSON]管理员管理权限列表
     */
    @TableField(value = "auth_list")
    private String authList;

    /**
     * 绑定的会员id
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 供应商id
     */
    @TableField(value = "suppliers_id")
    private Integer suppliersId;

    /**
     * 角色id
     */
    @TableField(value = "role_id")
    private Integer roleId;

    /**
     * 商户id
     */
    @TableField(value = "merchant_id")
    private Integer merchantId;

    /**
     * 父管理员id，表明当前为子管理员
     */
    @TableField(value = "parent_id")
    private Integer parentId;

    /**
     * [JSON]店铺存的标签
     */
    @TableField(value = "menu_tag")
    private String menuTag;

    /**
     * [JSON]订单导出存的标签
     */
    @TableField(value = "order_export")
    private String orderExport;

    /**
     * [JSON]扩展信息
     */
    @TableField(value = "extra")
    private String extra;

    /**
     * 店铺id
     */
    @TableField(value = "shop_id")
    private Integer shopId;

    /**
     * 是否停用：1 是 0 否
     */
    @TableField(value = "is_using")
    private Integer isUsing;

    /**
     * 初始密码
     */
    @TableField(value = "initial_password")
    private String initialPassword;

    /**
     * 是否删除：1 是 0 否
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;
}