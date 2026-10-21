package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.PriceInquiryDTO;
import com.tigshop.bean.dto.product.PriceInquiryReplyDTO;
import com.tigshop.bean.model.product.PriceInquiry;
import com.tigshop.bean.vo.product.PriceInquiryDetailVO;
import com.tigshop.bean.vo.product.PriceInquiryVO;
import com.tigshop.service.common.BaseService;

/**
 * 商品询价
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface PriceInquiryService extends BaseService<PriceInquiry> {

    /**
     * 分页查询
     * @param dto 查询参数
     * @return 分页结果
     */
    Page<PriceInquiryVO> list(PriceInquiryDTO dto);

    /**
     * 后台询价详情
     * @param id 询价id
     * @return 询价详情
     */
    PriceInquiryVO detail(Integer id);

    /**
     * 用户端询价详情
     * @param id 询价id
     * @param userId 用户id
     * @return 询价详情
     */
    PriceInquiryDetailVO detail(Integer id, Integer userId);

    /**
     * 商家回复
     * @param dto 回复参数
     */
    void replyByAdmin(PriceInquiryReplyDTO dto);

    /**
     * 用户回复商家
     * @param dto 回复参数
     * @param userId 用户id
     */
    void replyByUser(PriceInquiryReplyDTO dto, Integer userId);

    /**
     * 用户删除询价（仅用户侧隐藏）
     * @param id 询价id
     * @param userId 用户id
     */
    void deleteByUser(Integer id, Integer userId);
}
