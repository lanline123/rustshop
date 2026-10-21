package com.tigshop.bean.vo.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作预览。
 *
 * <p>批量写入影响面大且不可逐条撤销，故先预览后执行。预览必须列出受影响的每一个商品，
 * 只给数量不给清单，用户是在信息不足的情况下点的确认。
 */
@Data
@Schema(description = "WorkBuddy批量操作预览")
public class BatchPreviewVO {

    /**
     * 执行时必须原样带回。
     *
     * <p>绑定本次入参的指纹，用于防止预览与执行之间命中集发生漂移。
     */
    private String previewId;

    /** 操作的中文名，如"批量下架"。 */
    private String operation;

    /** 执行后的连带后果，措辞与单商品写操作一致。 */
    private List<String> consequences = new ArrayList<>();

    /** 受影响的商品，逐个列出。 */
    private List<ProductSummaryVO> affected = new ArrayList<>();

    /**
     * 无法执行的商品及原因，如未审核不能上架、不在回收站不能删除。
     *
     * <p>预览阶段就点明，好过执行到一半才报错。
     */
    private List<Blocked> blocked = new ArrayList<>();

    /** 本次操作是否不可恢复；为真时调用方须以更郑重的措辞征求确认。 */
    private boolean irreversible;

    @Data
    @Schema(description = "被阻断的商品")
    public static class Blocked {

        private Integer productId;

        private String productName;

        /** 阻断原因，须写清下一步该怎么办。 */
        private String reason;
    }
}
