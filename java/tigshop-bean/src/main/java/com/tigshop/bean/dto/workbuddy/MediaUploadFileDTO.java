package com.tigshop.bean.dto.workbuddy;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class MediaUploadFileDTO {
    /** 更新/删除素材时使用的既有素材 ID；上传时为空。 */
    private Integer id;
    private String clientFileId;
    /** 通过临时上传接口生成的文件令牌；优先于 Base64。 */
    private String fileToken;
    @JsonAlias("fileName")
    private String name;
    @JsonAlias("mime")
    private String mimeType;
    @JsonAlias({"content", "base64"})
    private String contentBase64;
    private Integer groupId;
    private String role;
}
