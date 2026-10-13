// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 地区列表
 *
 * @author Jayce
 * @create 2024年11月11日 14:12
 */
@Data
@TableName("region")
@Schema(description = "地区列表")
public class Region implements Serializable {
    @TableId(type = IdType.AUTO, value = "region_id")
    @Schema(description = "主键")
    private Integer regionId;

    @TableField(value = "region_name")
    @Schema(description = "地区名称")
    @JsonTranslate
    private String regionName;

    @TableField(value = "parent_id")
    @Schema(description = "父级id")
    private Integer parentId;

    @TableField(value = "level")
    @Schema(description = "级别")
    private Integer level;

    @TableField(value = "is_hot")
    @Schema(description = "是否热门")
    private Integer isHot;

    @TableField(value = "sort_order")
    @Schema(description = "排序值，数值越小越靠前")
    private Integer sortOrder;

    @TableField(value = "first_word")
    @Schema(description = "首字母")
    private String firstWord;

}
