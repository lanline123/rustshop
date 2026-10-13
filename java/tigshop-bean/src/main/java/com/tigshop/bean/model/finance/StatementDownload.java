package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 对账单下载
 *
 * @author Tigshop项目组
 * @create 2025年08月12日 10:26
 */
@Data
@Builder
@TableName(value ="statement_download")
public class StatementDownload {
    @TableId(value = "statement_download_id", type = IdType.AUTO)
    private Long statementDownloadId;
    @Schema(description = "申请时间")
    private Long gmtCreate;
    @Schema(description = "开始时间")
    private Long startTime;
    @Schema(description = "结束时间")
    private Long endTime;
    @Schema(description = "店铺名称")
    private String remark;
    @Schema(description = "店铺ID")
    private Long shopId;
    @Schema(description = "供应商id")
    private Integer vendorId;
}