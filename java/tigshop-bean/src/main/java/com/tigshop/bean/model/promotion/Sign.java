package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 签到
 *
 * @author wzh
 */
@Data
@TableName("sign")
@Schema(description = "签到")
public class Sign {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键id")
    private Integer id;

    @Schema(description ="会员id")
    private Integer userId;

    @Schema(description ="添加时间")
    private Long addTime;

    @Schema(description ="当前签到次数")
    private Integer signNum;
}
