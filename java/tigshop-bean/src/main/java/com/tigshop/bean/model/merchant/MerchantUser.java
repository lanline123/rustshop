package com.tigshop.bean.model.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("merchant_user")
@Schema(description = "商户用户表")
public class MerchantUser {

    @Schema(description = "商户用户ID")
    @TableId(type = IdType.AUTO)
    private Integer merchantUserId;

    @Schema(description = "前台用户ID")
    private Integer userId;

    @Schema(description = "后台用户ID")
    private Integer adminUserId;

    @Schema(description = "商户ID")
    private Integer merchantId;

    @Schema(description = "是否管理员，1是0不是")
    private Integer isAdmin;
}
