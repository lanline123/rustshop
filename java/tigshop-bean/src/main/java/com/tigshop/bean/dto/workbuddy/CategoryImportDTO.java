package com.tigshop.bean.dto.workbuddy;

import lombok.Data;

import java.util.List;

/** MCP 商品分类批量导入的统一树形结构。文件解析由 AI 客户端完成。 */
@Data
public class CategoryImportDTO {
    private String operation;
    private List<Node> nodes;
    private String previewId;
    private String requestId;
    private Boolean confirmation;

    @Data
    public static class Node {
        private Integer id;
        private String name;
        private List<Node> children;
    }
}
