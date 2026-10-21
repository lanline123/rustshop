package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.bean.param.finance.refundapply.RefundApplyAuditParam;
import com.tigshop.bean.param.finance.refundapply.RefundApplyEditParam;
import com.tigshop.bean.param.finance.refundapply.RefundApplySaveParam;
import com.tigshop.bean.param.finance.refundapply.OfflineAuditParam;
import com.tigshop.bean.query.finance.RefundApplyPageQuery;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyDetailVO;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyVO;
import com.tigshop.service.common.BaseService;

import java.util.Map;

/**
 * 退款申请服务
 *
 * @author kidd
 * @create 2025/7/7
 */
public interface RefundApplyService extends BaseService<RefundApply> {

    /**
     * 获取审核状态配置
     */
    Map<Integer, String> config();

    /**
     * 列表
     */
    Page<RefundApplyVO> list(RefundApplyPageQuery pageQuery);

    /**
     * 详情
     */
    RefundApplyDetailVO detail(Integer id);

    /**
     * 审核
     */
    void audit(RefundApplyAuditParam param);

    /**
     * 确认线下转账
     */
    void offlineAudit(OfflineAuditParam param);

    /**
     * 创建
     */
    void create(RefundApplySaveParam param);

    /**
     * 更新
     */
    void update(RefundApplyEditParam param);

    RefundApply getByPaylogRefundId(Integer refundPayLogId);
}
