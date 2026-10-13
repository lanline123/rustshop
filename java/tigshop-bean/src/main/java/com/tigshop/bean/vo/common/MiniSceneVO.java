package com.tigshop.bean.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 小程序码 scene 解析结果
 * <p>
 * 当 id + salesmanId 拼接超过微信 scene 32 字符上限时，后端仅存短码到 scene，
 * 小程序 onLoad 通过短码回查得到真实参数。
 */
@Data
@Schema(description = "小程序码 scene 解析结果")
public class MiniSceneVO implements Serializable {

    @Schema(description = "小程序页面路径")
    private String path;

    @Schema(description = "关联ID（如商品ID）")
    private Integer id;

    @Schema(description = "分销员ID")
    private Integer salesmanId;

    @Schema(description = "进店归因门店ID（自营为 0）")
    private Integer shopId;
}
