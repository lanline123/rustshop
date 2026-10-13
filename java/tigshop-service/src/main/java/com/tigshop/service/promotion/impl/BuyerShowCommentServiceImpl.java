package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.model.promotion.BuyerShow;
import com.tigshop.bean.model.promotion.BuyerShowComment;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentAuditParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentIsTopParam;
import com.tigshop.bean.param.promotion.buyershow.BuyerShowCommentShowOrHideParam;
import com.tigshop.bean.query.promotion.buyershow.BuyerShowCommentPageQuery;
import com.tigshop.bean.vo.promotion.buyershow.BuyerShowCommentPageVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.promotion.BuyerShowCommentMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.BuyerShowCommentService;
import com.tigshop.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author wzh
*/
@RequiredArgsConstructor
@Service
public class BuyerShowCommentServiceImpl extends BaseServiceImpl<BuyerShowCommentMapper, BuyerShowComment>
    implements BuyerShowCommentService {

    private final UserService userService;

    @Override
    public Page<BuyerShowCommentPageVO> list(BuyerShowCommentPageQuery query) {
        // 分页
        Page<BuyerShowComment> page = new Page<>(query.getPage(), query.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<BuyerShowComment> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, query.getSortField(), query.getSortOrder());

        queryWrapper.eq(query.getBuyerShowId() != null, BuyerShowComment::getBuyerShowId, query.getBuyerShowId())
                .eq(query.getStatus() != null && query.getStatus() == -1, BuyerShowComment::getStatus, 0)
                .ne(query.getStatus() != null && query.getStatus() > -1, BuyerShowComment::getStatus, 0)
                .eq(query.getStatus() != null && query.getStatus() > -1, BuyerShowComment::getIsShow, query.getStatus());

        if (StrUtil.isNotBlank(query.getStartTime()) && StrUtil.isNotBlank(query.getEndTime())) {
            queryWrapper.between(BuyerShowComment::getAddTime,
                    TigUtils.toTimestampYmd(query.getStartTime()), TigUtils.toTimestampYmd(query.getEndTime()));
        }

        // 执行查询
        Page<BuyerShowComment> buyerShowCommentPage = this.page(page, queryWrapper);

        List<BuyerShowComment> buyerShowCommentList = buyerShowCommentPage.getRecords();
        if (CollUtil.isEmpty(buyerShowCommentList)) {
            return new Page<>();
        }

        List<Integer> userIds = buyerShowCommentList.stream()
                .map(BuyerShowComment::getUserId)
                .filter(userId -> userId > 0)
                .toList();

        Map<Integer, User> userMap = CollUtil.isEmpty(userIds) ? new HashMap<>() : userService.lambdaQuery()
                .in(User::getUserId, userIds)
                .list()
                .stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        List<BuyerShowCommentPageVO> buyerShowCommentPageVOList = buyerShowCommentList.stream()
                .map(buyerShowComment -> {
                    BuyerShowCommentPageVO buyerShowCommentPageVO = new BuyerShowCommentPageVO();
                    BeanUtils.copyProperties(buyerShowComment, buyerShowCommentPageVO);
                    buyerShowCommentPageVO.setAddTime(TigUtils.handelTime(buyerShowComment.getAddTime()));
                    if (buyerShowComment.getUserId() > 0) {
                        User user = userMap.get(buyerShowComment.getUserId());
                        if (user != null) {
                            buyerShowComment.setUserName(user.getName());
                        }
                    }
                    String type = buyerShowComment.getUserId() == 0 ? "(系统)" : "(客户)";
                    buyerShowCommentPageVO.setUserName(buyerShowComment.getUserName() + type);
                    String statusText = buyerShowComment.getStatus() == 0 ? "待审核" : buyerShowComment.getIsShow() == 1 ? "公开" : "隐藏";
                    buyerShowCommentPageVO.setStatusText(statusText);

                    return buyerShowCommentPageVO;
                }).toList();
        return PageUtil.convertPage(buyerShowCommentPage, buyerShowCommentPageVOList);
    }

    @Override
    public void audit(BuyerShowCommentAuditParam param) {
        this.lambdaUpdate()
                .eq(BuyerShowComment::getBuyerShowCommentId, param.getBuyerShowCommentId())
                .set(BuyerShowComment::getStatus, param.getStatus())
                .set(BuyerShowComment::getIsShow, param.getStatus() == 1 ? Constants.YES : Constants.NO)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if ("audit".equals(batchDTO.getType())) {
            this.lambdaUpdate()
                    .in(BuyerShowComment::getBuyerShowCommentId, batchDTO.getIds())
                    .set(BuyerShowComment::getStatus, batchDTO.getValue())
                    .set(BuyerShowComment::getIsShow, batchDTO.getValue() == 1 ? Constants.YES : Constants.NO)
                    .update();
            return true;
        }
        return false;
    }

    @Override
    public void showOrHide(BuyerShowCommentShowOrHideParam param) {
        this.lambdaUpdate()
                .eq(BuyerShowComment::getBuyerShowCommentId, param.getBuyerShowCommentId())
                .set(BuyerShowComment::getIsShow, param.getIsShow())
                .update();
    }

    @Override
    public void isTop(BuyerShowCommentIsTopParam param) {
        this.lambdaUpdate()
                .eq(BuyerShowComment::getBuyerShowCommentId, param.getBuyerShowCommentId())
                .set(BuyerShowComment::getIsTop, param.getIsTop())
                .update();
    }
}




