package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * app版本管理
 * @TableName app_version
 */
@TableName(value ="app_version")
@Data
public class AppVersion implements Serializable {

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "ios版本号")
    private String iosVersion;

    @Schema(description = "Android版本号")
    private String androidVersion;

    @Schema(description = "ios下载链接")
    private String iosLink;

    @Schema(description = "Android下载链接")
    private String androidLink;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}