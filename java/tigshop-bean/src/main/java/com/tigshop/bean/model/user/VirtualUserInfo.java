package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 虚拟用户信息
 * @author wzh
 */
@TableName(value ="virtual_user_info")
@Data
public class VirtualUserInfo {
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "头像")
    private String photo;

    @Schema(description = "名称")
    private String nickname;
}