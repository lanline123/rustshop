package com.tigshop.bean.dto.finance.feerule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "费率规则批量删除")
public class CategoryFeeRuleIdsDTO {

    @NotEmpty(message = "请选择要删除的规则")
    private List<Integer> ids;
}
