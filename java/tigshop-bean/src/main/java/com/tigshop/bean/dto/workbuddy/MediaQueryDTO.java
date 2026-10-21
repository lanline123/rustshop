package com.tigshop.bean.dto.workbuddy;

import lombok.Data;

/** MCP 素材查询入参。仅包含素材中心允许的筛选字段。 */
@Data
public class MediaQueryDTO {
    private String operation;
    private String type;
    private Integer groupId;
    private String keyword;
    private Integer page;
    private Integer pageSize;
    private Integer id;
}
