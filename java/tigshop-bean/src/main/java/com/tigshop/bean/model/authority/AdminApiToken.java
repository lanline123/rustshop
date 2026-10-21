package com.tigshop.bean.model.authority;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@TableName(value = "admin_api_token")
@Data
@Schema(description = "管理员API Token")
public class AdminApiToken implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "admin_id")
    private Integer adminId;

    @TableField(value = "token_hash")
    private String tokenHash;

    @TableField(value = "token_prefix")
    private String tokenPrefix;

    @TableField(value = "name")
    private String name;

    @TableField(value = "created_at")
    private Long createdAt;

    @TableField(value = "last_used_at")
    private Long lastUsedAt;

    @TableField(value = "expires_at")
    private Long expiresAt;

    @TableField(value = "is_active")
    private Integer isActive;
}
