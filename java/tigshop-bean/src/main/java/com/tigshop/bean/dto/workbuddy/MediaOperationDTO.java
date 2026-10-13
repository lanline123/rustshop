package com.tigshop.bean.dto.workbuddy;

import lombok.Data;

import java.util.List;

/** MCP 素材预览/执行入参。客户端不能提交本地路径或存储凭证。 */
@Data
public class MediaOperationDTO {
    private String operation;
    private List<MediaUploadFileDTO> items;
    /** 单项素材/分组 ID；批量操作使用 ids。 */
    private Integer id;
    private List<Integer> ids;
    /** 素材或分组名称。 */
    private String name;
    /** image/video，写操作需要显式声明素材类型。 */
    private String type;
    private Integer groupId;
    /** 图片排序值；仅用于后台允许的排序字段。 */
    private Integer sort;
    private String previewId;
    private String requestId;
    private Boolean confirmation;
    /** 客户端若误传本地路径将被明确拒绝。 */
    private String localPath;
}
