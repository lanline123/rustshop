package com.tigshop.bean.param.promotion.giftcard;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.tigshop.bean.enums.promotion.GiftGroupsEnum;
import com.tigshop.bean.enums.promotion.ValidityTypeEnum;
import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.model.promotion.GiftCardProducts;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年10月29日 10:44
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "卡创建参数")
public class GiftCardCreateParam {

    @Schema(description = "所属卡组ID")
    private Integer cardGroupsId;

    @Schema(description = "卡号")
    private String cardNumber;

    @Schema(description = "卡密码")
    private String cardPassword;

    @Schema(description = "1-可用 0-不可用")
    private Integer isAvailable;
}