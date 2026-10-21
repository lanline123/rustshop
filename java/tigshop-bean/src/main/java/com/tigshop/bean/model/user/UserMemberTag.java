package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_member_tag")
@Schema(description = "用户会员标签关联")
public class UserMemberTag implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "店铺ID，0=平台")
    private Integer shopId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "标签ID")
    private Long tagId;

    @Schema(description = "打标时间")
    private Long createTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
