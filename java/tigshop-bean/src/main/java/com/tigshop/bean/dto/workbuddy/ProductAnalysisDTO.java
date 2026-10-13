package com.tigshop.bean.dto.workbuddy;

import lombok.Data;

import java.time.LocalDateTime;

/** MCP 商品数据分析查询入参。分析工具只读，不接受写入确认字段。 */
@Data
public class ProductAnalysisDTO {
    private String operation;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer categoryId;
    private String keyword;
    private Integer sampleSize;
    private Integer page;
}
