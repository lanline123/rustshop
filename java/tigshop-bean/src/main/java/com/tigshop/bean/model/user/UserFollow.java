package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户关注关系
 * &#064;TableName  user_follow
 *
 * @author wzh
 */
@TableName(value = "user_follow")
@Data
@Schema(description = "用户关注关系")
public class UserFollow implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "关注者用户ID")
    private Integer userId;

    @Schema(description = "被关注作者用户ID")
    private Integer authorId;

    @Schema(description = "关注时间")
    private Long addTime;
}
