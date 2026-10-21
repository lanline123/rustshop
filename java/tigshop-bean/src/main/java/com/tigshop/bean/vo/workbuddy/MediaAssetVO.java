package com.tigshop.bean.vo.workbuddy;

import lombok.Data;

@Data
public class MediaAssetVO {
    private String clientFileId;
    private Integer assetId;
    private String type;
    private String role;
    private String url;
    private String coverUrl;
    private String format;
    private Integer groupId;
}
