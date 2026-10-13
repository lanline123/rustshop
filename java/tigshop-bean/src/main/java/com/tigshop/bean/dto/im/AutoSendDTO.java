package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 自动发送参数
 * @author Tigshop团队
 * @create 2025年03月05日 17:52
 */
@Data
@NoArgsConstructor
@Schema(description = "自动发送参数")
public class AutoSendDTO  implements Serializable {
    @Schema(description = "会话id")
    private String conversationId;

    @Schema(description = "发送类型")
    private String type;

    @Schema(description = "旧客服id")
    private String oldServantId;

    @Schema(description = "客服id")
    private String servantId;

    public AutoSendDTO(String conversationId, String type, String oldServantId, String servantId) {
        this.conversationId = conversationId;
        this.type = type;
        this.oldServantId = oldServantId;
        this.servantId = servantId;
    }

    public AutoSendDTO(String conversationId, String type) {
        this.conversationId = conversationId;
        this.type = type;
    }
}
