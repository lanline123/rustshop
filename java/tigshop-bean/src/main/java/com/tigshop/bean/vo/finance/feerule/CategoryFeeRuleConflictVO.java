package com.tigshop.bean.vo.finance.feerule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "费率规则冲突检测")
public class CategoryFeeRuleConflictVO {

    private Boolean hasConflict;
    private List<ConflictItem> conflicts = new ArrayList<>();

    @Data
    public static class ConflictItem {
        private Integer categoryId;
        private String categoryName;
        private Integer existingRuleId;
        private String existingRuleName;
        private Integer targetId;
        private String targetName;
    }
}
