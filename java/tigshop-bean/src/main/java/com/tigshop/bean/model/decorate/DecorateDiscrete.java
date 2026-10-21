package com.tigshop.bean.model.decorate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 装修组件model
 *
 * @author Kidd
 * @since 2025/6/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("decorate_discrete")
@Schema(description = "装修组件")
public class DecorateDiscrete {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "装修组件ID")
    private Integer id;

    @Schema(description ="模块SN")
    private String decorateSn;

    @Schema(description ="模块名称")
    private String decorateName;

    @Schema(description ="模块数据，JSON格式")
    private String data;

    @Schema(description ="店铺ID")
    private Integer shopId;
}
