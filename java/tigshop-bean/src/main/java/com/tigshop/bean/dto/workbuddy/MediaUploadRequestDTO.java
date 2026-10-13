package com.tigshop.bean.dto.workbuddy;

import lombok.Data;
import java.util.List;

@Data
public class MediaUploadRequestDTO {
    private boolean confirmation;
    /** 未单独指定文件分组时使用的默认素材分组。 */
    private Integer groupId;
    private List<MediaUploadFileDTO> files;
}
