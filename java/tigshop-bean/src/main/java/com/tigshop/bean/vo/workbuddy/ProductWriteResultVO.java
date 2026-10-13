package com.tigshop.bean.vo.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单商品写操作的结果。
 *
 * <p>携带服务端读到的改前值与改后值。调用方自述的入参与它实际发出的一致，
 * 只有服务端回显才能暴露串位——用户看到"把 A 的价格从 99 改成 89"才有机会发现改错了商品。
 */
@Data
@Schema(description = "WorkBuddy商品写操作结果")
public class ProductWriteResultVO {

    private Integer productId;

    private String productName;

    /** 改动项的中文名，如"价格""库存""上下架状态"。 */
    private String changedField;

    /** 改动前的值，已转为面向人的表述。 */
    private String before;

    /** 改动后的值，已转为面向人的表述。 */
    private String after;

    /**
     * 本次操作的连带后果，供如实告知用户。
     *
     * <p>例如上架后前台立即可见、下架会移除该商品的拼团活动、移入回收站可还原。
     * 这些用户预期之外的副作用若不说明，事后被发现会归咎于操作方。
     */
    private List<String> consequences = new ArrayList<>();
}
