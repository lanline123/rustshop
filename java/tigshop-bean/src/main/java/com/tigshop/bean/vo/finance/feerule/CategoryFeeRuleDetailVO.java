package com.tigshop.bean.vo.finance.feerule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "费率规则详情")
public class CategoryFeeRuleDetailVO extends CategoryFeeRuleListVO {

    private String remark;
    private List<CategoryItem> categories;
    private List<TargetItem> targets;
    private LocalDateTime modifyTime;

    @Data
    public static class CategoryItem {
        private Integer categoryId;
        private String categoryName;
        private String categoryPath;
    }

    @Data
    public static class TargetItem {
        private Integer targetId;
        private String targetName;
    }
}
