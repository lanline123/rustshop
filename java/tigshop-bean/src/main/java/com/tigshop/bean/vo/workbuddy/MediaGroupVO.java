package com.tigshop.bean.vo.workbuddy;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 素材分组的非敏感展示信息。 */
@Data
public class MediaGroupVO {
    private Integer id;
    private String name;
    /** 分组所属素材类型，image 或 video。 */
    private String type;
    private Integer assetCount;
    /** 子分组；保留后台素材分组的完整树结构。 */
    private List<MediaGroupVO> children = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
