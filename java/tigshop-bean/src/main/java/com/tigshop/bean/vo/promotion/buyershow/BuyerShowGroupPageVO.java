package com.tigshop.bean.vo.promotion.buyershow;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀分组分页信息")
public class BuyerShowGroupPageVO {

    @Schema(description = "自增ID，买家秀分组ID")
    private Integer buyerShowGroupId;

    @Schema(description = "分组名称")
    @JsonTranslate(dataType = 16)
    private String groupName;

    @Schema(description = "关联商品分组")
    private List<String> productGroupName;

    @Schema(description = "买家秀数量")
    private Long buyerShowCount;

}
