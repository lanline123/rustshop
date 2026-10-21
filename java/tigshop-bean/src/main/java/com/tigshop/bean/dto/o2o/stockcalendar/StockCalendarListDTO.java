package com.tigshop.bean.dto.o2o.stockcalendar;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 库存日历管理分页查询参数
 *
 * @author Tigshop项目组
 * @create 2025年01月01日
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "库存日历管理分页查询参数")
public class StockCalendarListDTO extends BasePage {

    /**
     * 开始日期（必填）
     * 支持两种格式：
     * 1. yyyy-MM：查询某个月的库存（如果是当月则从今天开始到本月结束）
     * 2. yyyy-MM-dd：查询某个时间范围的库存
     */
    @Schema(description = "开始日期，格式：yyyy-MM 或 yyyy-MM-dd", required = true)
    @NotBlank(message = "开始日期不能为空")
    private String startDate;

    /**
     * 结束日期（可选）
     * 当startDate为yyyy-MM格式时，此字段可为空，系统自动计算月末日期
     * 当startDate为yyyy-MM-dd格式时，此字段必填且格式为yyyy-MM-dd
     */
    @Schema(description = "结束日期，格式：yyyy-MM-dd（当startDate为yyyy-MM格式时可为空）")
    private String endDate;

    /**
     * 库存查看范围类型：0-全部门店，1-指定门店
     */
    @Schema(description = "库存查看范围类型：0-全部门店，1-指定门店")
    private Integer shopScopeType;

    /**
     * 门店ID列表（为空则查询所有门店）
     */
    @Schema(description = "门店ID列表，为空则查询所有门店")
    private String shopIds;

    /**
     * 门店ID列表（为空则查询所有门店）
     */
    @Schema(description = "门店ID列表，为空则查询所有门店", hidden = true)
    private List<Integer> shopIdList;

    /**
     * 商品名称（模糊查询）
     */
    @Schema(description = "商品名称，支持模糊查询")
    private String productName;

    /**
     * 规格值（模糊查询）
     */
    @Schema(description = "规格值，支持模糊查询")
    private String specValue;
}