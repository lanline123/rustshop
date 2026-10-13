package com.tigshop.bean.vo.user;

import com.tigshop.bean.model.promotion.SignInSetting;
import com.tigshop.bean.vo.product.ProductInfoVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "用户签到信息")
public class UserSignInfoVo {
    @Schema(description = "签到配置")
    private List<SignInSetting> days;

    @Schema(description = "签到天数")
    private Integer record;

    @Schema(description = "今天是否签到")
    private Integer isSign;

    @Schema(description = "获取今天签到获得的积分")
    private Integer signPoints;

    @Schema(description = "推荐商品信息")
    private List<ProductInfoVo> recommendGoods;
}
