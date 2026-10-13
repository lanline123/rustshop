package com.tigshop.bean.dto.product;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.product.CheckStatus;
import com.tigshop.common.exception.GlobalException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 产品审核 入参
 *
 * @author kidd
 * @since 2025/3/26 11:02
 */
@Data
public class ProductAuditDTO {

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "审核状态")
    private Integer checkStatus;

    @Schema(description = "审核原因")
    private String checkReason;

    public void validParam() {
        if (CheckStatus.REJECTED.getCode() == checkStatus) {
            Assert.isTrue(StrUtil.isNotBlank(checkReason), () -> new GlobalException("请输入审核不通过的原因"));
        }
    }
}
