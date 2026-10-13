package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 前台提交的修改密码信息
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "前台提交的删除最近浏览信息")
public class DelHistoryProductDTO {
    @Schema(description = "商品id")
    private List<Integer> ids;
}
