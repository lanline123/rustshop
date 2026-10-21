package com.tigshop.bean.vo.promotion;

import com.tigshop.bean.model.user.UserRank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author Tigshop团队
 * @create 2025/4/2 9:39
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "配置")
public class ProductPromotionConfigVO {
    @Schema(description = "配置")
    private Map<Integer, String> promotionStatus;
    @Schema(description = "会员等级配置信息")
    private List<UserRank> rankList;
}
