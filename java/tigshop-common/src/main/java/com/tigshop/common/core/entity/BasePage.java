package com.tigshop.common.core.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Jayce
 */
@Data
@Schema(description = "分页参数")
public class BasePage {
    /**
     * 当前页码
     */
    @Schema(description = "当前页码")
    private Integer page;

    /**
     * 分页大小
     */
    @Schema(description = "分页大小")
    private Integer size;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    private String sortField;

    /**
     * 查询参数
     */
    @Schema(description = "查询参数")
    private String keyword;

    /**
     * desc|asc
     */
    @Schema(description = "显示顺序")
    private String sortOrder;

    /** 多字段排序，按列表顺序依次生效。 */
    @Schema(description = "多字段排序项")
    private List<SortItem> sortItems;

    public Integer getPage() {
        if (null == this.page) {
            return 1;
        }
        return this.page;
    }

    public Integer getSize() {
        if (null == this.size) {
            return 15;
        }
        if (this.size > 100) {
            return 100;
        }
        return this.size;
    }

    public BasePage(){
        this.page = 1;
        this.size = 15;
    }
}
