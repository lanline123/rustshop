// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.product;

import com.tigshop.bean.dto.product.ProductPresaleDTO;
import com.tigshop.bean.model.product.ProductPresale;
import com.tigshop.bean.vo.product.ProductPresaleVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;

/**
 * 商品预售服务接口
 *
 * @author Tigshop团队
 * @create 2025年11月21日
 */
public interface ProductPresaleService extends BaseService<ProductPresale> {

    /**
     * 保存或更新预售信息
     *
     * @param productId   商品ID
     * @param presaleDTO  预售配置
     * @param productPrice 商品价格（用于校验固定金额定金）
     * @return 是否成功
     */
    boolean saveOrUpdate(Integer productId, ProductPresaleDTO presaleDTO, BigDecimal productPrice);

    /**
     * 根据商品ID查询预售信息
     *
     * @param productId 商品ID
     * @return 预售信息
     */
    ProductPresale getByProductId(Integer productId);

    /**
     * 根据商品ID查询预售信息并转换为VO（包含实时计算的定金金额）
     *
     * @param productId 商品ID
     * @return 预售信息VO
     */
    ProductPresaleVO getPresaleVOByProductId(Integer productId);


    /**
     * 获取商品的最低价格(多个sku的情况)
     * @param productId
     * @return
     */
     BigDecimal getBasePrice(Integer productId);

    /**
     * 根据商品ID删除预售信息
     *
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean deleteByProductId(Integer productId);


    /**
     * 实时计算定金金额
     *
     * @param productId      商品ID
     * @param presaleConfig  预售配置
     * @return 定金金额
     */
    BigDecimal calculateDepositAmount(Integer productId,Integer skuId, ProductPresale presaleConfig);

    /**
     * 校验预售配置
     *
     * @param saleMode     销售模式
     * @param presaleDTO   预售配置
     * @param productPrice 商品价格
     */
    void validatePresaleConfig(Integer saleMode, ProductPresaleDTO presaleDTO, BigDecimal productPrice);


}

