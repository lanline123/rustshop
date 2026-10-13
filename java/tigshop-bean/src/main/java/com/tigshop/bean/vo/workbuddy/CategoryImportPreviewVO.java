package com.tigshop.bean.vo.workbuddy;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 分类导入预览及逐节点校验结果。 */
@Data
public class CategoryImportPreviewVO {
    private String previewId;
    private String status;
    private Integer total;
    private Integer existingCount;
    private Integer missingParentCount;
    private Integer conflictCount;
    private List<NodeResult> nodes = new ArrayList<>();
    private String summary;

    @Data
    public static class NodeResult {
        private String path;
        private Integer id;
        private String name;
        private String state;
        private Boolean leaf;
        private Boolean hasChildren;
        private String reason;
    }
}
