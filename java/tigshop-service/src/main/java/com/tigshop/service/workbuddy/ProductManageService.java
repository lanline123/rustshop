package com.tigshop.service.workbuddy;

import com.tigshop.bean.dto.workbuddy.ProductSearchDTO;
import com.tigshop.bean.vo.workbuddy.ProductDetailVO;
import com.tigshop.bean.vo.workbuddy.ProductSearchVO;
import com.tigshop.bean.vo.workbuddy.ProductWriteResultVO;
import com.tigshop.bean.vo.workbuddy.PromotionLinksVO;
import com.tigshop.bean.vo.workbuddy.BatchPreviewVO;
import com.tigshop.bean.vo.workbuddy.BatchResultVO;
import java.util.List;

/**
 * 面向 WorkBuddy 的商品列表管理能力。
 *
 * <p>包装后台商品列表页的既有接口，但不是透传：
 * 数字码在此层译成中文、易错的参数组合在此层收敛、UI 层才有的业务约束在此层补齐。
 * 调用方是 AI，它不会看后台页面，也没有"这个按钮在这个页签才出现"的上下文。
 */
public interface ProductManageService {

    ProductSearchVO search(ProductSearchDTO query, Integer shopId);

    /** 判断商品编码是否已被商城占用，用于生成候选编码前的唯一性复核。 */
    boolean isProductSnOccupied(String productSn, Integer shopId);

    ProductDetailVO detail(Integer productId, Integer shopId);

    PromotionLinksVO promotionLinks(Integer productId, Integer shopId);

    /**
     * 改单个字段。
     *
     * @param field 仅接受 productName / productSn / productPrice / sortOrder / productStock / productSku。
     *              删除与上下架不在其列：它们语义不是改值，各自成独立方法才挂得上对应的校验与确认
     */
    ProductWriteResultVO updateField(Integer productId, String field, Object value, Integer shopId);

    /**
     * 上架或下架。
     *
     * <p>单独成方法而非并入 {@link #updateField}：上架有前置条件（须审核通过），
     * 下架有连带后果（B2C 下会移除该商品的拼团活动项），二者都需要专门表达。
     */
    ProductWriteResultVO setStatus(Integer productId, boolean onSale, Integer shopId);

    /** 移入回收站，可还原。 */
    ProductWriteResultVO recycle(Integer productId, Integer shopId);

    /** 从回收站还原。 */
    ProductWriteResultVO restore(Integer productId, Integer shopId);

    ProductWriteResultVO copy(Integer productId, Integer shopId);

    ProductWriteResultVO deletePermanently(Integer productId, Integer shopId);

    BatchPreviewVO previewBatch(String operation, List<Integer> productIds, Integer shopId);

    BatchResultVO executeBatch(String operation, List<Integer> productIds, String previewId,
                               boolean confirmation, Integer shopId);
}
