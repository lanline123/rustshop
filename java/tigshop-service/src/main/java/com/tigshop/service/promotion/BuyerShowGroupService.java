package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.BuyerShowGroup;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowGroupSaveParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowGroupUpdateParam;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowGroupPageQuery;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowGroupPageVO;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowGroupVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
* @author wzh

*/
public interface BuyerShowGroupService extends BaseService<BuyerShowGroup> {


    /**
     * 新增买家秀分组
     *
     * @param param 新增买家秀分组参数
     */
    void create(BuyerShowGroupSaveParam param);

    /**
     * 修改买家秀分组
     *
     * @param param 修改买家秀分组参数
     */
    void update(BuyerShowGroupUpdateParam param);


    /**
     * 买家秀分组列表
     *
     * @param query 查询参数
     * @return 列表
     */
    Page<BuyerShowGroupPageVO> list(BuyerShowGroupPageQuery query);

    /**
     * 买家秀分组详情
     *
     * @param buyerShowGroupId 买家秀分组ID
     * @return 买家秀分组详情
     */
    BuyerShowGroupVO detail(Integer buyerShowGroupId);


    /**
     * 根据商品ID获取买家秀分组
     *
     * @param productIds 商品ID列表
     * @return 买家秀分组列表
     */
    List<Integer> getBuyerShowGroupByProductIds(List<Integer> productIds);
}
