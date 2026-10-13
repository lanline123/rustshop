package com.tigshop.bean.bo.decorate.discrete;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 开屏广告
 *
 * @author kidd
 * @since 2025/6/30 10:10
 */
@Data
public class OpenAdvertisingBO {

    @Schema(description = "开启状态；0-关闭，1-开启")
    private Integer state;

    @Schema(description = "素材类型；0-图片，1-视频")
    private Integer materialType;

    @Schema(description = "素材图片")
    private String materialImg;

    @Schema(description = "素材视频")
    private List<JSONObject> materialVideo;

    @Schema(description = "最大等待时间")
    private Integer maxWaitTime;

    @Schema(description = "跳转方式；0-不跳转，1-整体跳转")
    private Integer redirectType;

    @Schema(description = "跳转地址")
    private JSONObject redirectUrl;
}
