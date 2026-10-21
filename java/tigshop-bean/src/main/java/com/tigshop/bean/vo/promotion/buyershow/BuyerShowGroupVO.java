package com.tigshop.bean.vo.promotion.buyershow;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀分组详情信息")
public class BuyerShowGroupVO {

    @Schema(description = "买家秀分组ID")
    private Integer buyerShowGroupId;

    @Schema(description = "分组名称")
    @JsonTranslate(dataType = 16)
    private String groupName;

    @Schema(description = "商品分组Ids")
    private List<Integer> groupIds;

    @Schema(description = "关联商品分组")
    private List<String> productGroupName;
}
