package com.tigshop.bean.param.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 生成微信二维码
 *
 * @author kidd
 * @since 2025/4/23 15:31
 */
@Schema(description = "生成微信二维码")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQrcodeParam {

    @JsonProperty("expire_seconds")
    private Integer expireSeconds;

    @JsonProperty("action_name")
    private String actionName;

    @JsonProperty("action_info")
    private ActionInfo actionInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionInfo {

        @Schema(description = "场景值")
        private Map<String, Object> scene;

    }
}
