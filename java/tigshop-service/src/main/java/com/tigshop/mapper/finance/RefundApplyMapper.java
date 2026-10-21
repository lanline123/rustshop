package com.tigshop.mapper.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.finance.RefundApplyPageQuery;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyVO;
import com.tigshop.mapper.common.BaseMapper;
import com.tigshop.bean.model.finance.RefundApply;
import org.apache.ibatis.annotations.Param;

/**
 * 退款申请映射
 *
 * @author kidd
 * @create 2025/7/7
 */
public interface RefundApplyMapper extends BaseMapper<RefundApply> {

    Page<RefundApplyVO> page(@Param("page") Page<RefundApply> page, @Param("pageQuery") RefundApplyPageQuery pageQuery);
}
