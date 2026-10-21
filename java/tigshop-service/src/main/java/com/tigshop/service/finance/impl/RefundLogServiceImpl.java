// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.finance.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.finance.RefundLogCreateDTO;
import com.tigshop.bean.dto.finance.RefundLogListDTO;
import com.tigshop.bean.dto.finance.RefundLogUpdateDTO;
import com.tigshop.bean.model.finance.OrderFinanceAftersales;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.bean.model.finance.RefundLog;
import com.tigshop.bean.vo.finance.RefundLogVO;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.finance.OrderFinanceAftersalesMapper;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.finance.RefundLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RefundLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tigshop.common.constant.finance.RefundLogConstants.REFUND_TYPE_MAP;

/**
 * 退款记录服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
@RequiredArgsConstructor
public class RefundLogServiceImpl extends BaseServiceImpl<RefundLogMapper, RefundLog> implements RefundLogService {

    private final RefundApplyMapper refundApplyMapper;
    private final OrderFinanceAftersalesMapper orderFinanceAftersalesMapper;

    @Override
    public Page<RefundLogVO> list(RefundLogListDTO listDTO) {
        // 分页
        Page<RefundLog> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        MPJLambdaWrapper<RefundLog> queryWrapper = new MPJLambdaWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字(售后单号)
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper
                    .leftJoin(RefundApply.class, RefundApply::getRefundId, RefundLog::getRefundApplyId)
                    .leftJoin(OrderFinanceAftersales.class, OrderFinanceAftersales::getAftersaleId, RefundApply::getAftersaleId)
                    .like(OrderFinanceAftersales::getAftersalesSn, keyword);
        }
        // 退款类型
        Integer type = listDTO.getType();
        if (type != null) {
            queryWrapper.eq(RefundLog::getRefundType, type);
        }
        // 执行查询
        Page<RefundLog> refundLogPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<RefundLog> refundLogPageRecords = refundLogPage.getRecords();
        // 转换为VO
        List<RefundLogVO> refundLogVOList = refundLogPageRecords.stream()
                .map(refundLog -> {
                    RefundLogVO refundLogVO = new RefundLogVO();
                    BeanUtils.copyProperties(refundLog, refundLogVO);
                    // 先查退款申请
                    RefundApply refundApply = refundApplyMapper.selectById(refundLog.getRefundApplyId());
                    RefundApplyVO refundApplyVO = new RefundApplyVO();
                    BeanUtils.copyProperties(refundApply, refundApplyVO);
                    // 再查售后订单
                    OrderFinanceAftersales orderFinanceAftersales = orderFinanceAftersalesMapper.selectById(refundApply.getAftersaleId());
                    refundApplyVO.setAftersales(orderFinanceAftersales);
                    refundLogVO.setRefund(refundApplyVO);
                    refundLogVO.setAddTime(TigUtils.handelTime(refundLog.getAddTime()));

                    refundLogVO.setRefundTypeText(REFUND_TYPE_MAP.get(refundLog.getRefundType()));
                    return refundLogVO;
                }).toList();
        return PageUtil.convertPage(refundLogPage, refundLogVOList);
    }

    @Override
    public RefundLogVO detail(Integer id) {
        if (id != null) {
            RefundLog refundLog = this.getById(id);
            RefundLogVO refundLogVO = new RefundLogVO();
            BeanUtils.copyProperties(refundLog, refundLogVO);
            return refundLogVO;
        }
        return null;
    }

    @Override
    public boolean create(RefundLogCreateDTO createDTO) {
        if (createDTO != null) {
            RefundLog refundLog = new RefundLog();
            BeanUtils.copyProperties(createDTO, refundLog);
            refundLog.setAddTime(StringUtils.getCurrentTime());
            return this.save(refundLog);
        }
        return false;
    }

    @Override
    public boolean update(RefundLogUpdateDTO updateDTO) {
        if (updateDTO != null) {
            RefundLog refundLog = new RefundLog();
            BeanUtils.copyProperties(updateDTO, refundLog);
            return this.updateById(refundLog);
        }
        return false;
    }
}
