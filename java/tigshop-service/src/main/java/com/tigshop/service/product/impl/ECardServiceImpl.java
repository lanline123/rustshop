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

package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.enums.order.OrderType;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.param.product.ECardEditParam;
import com.tigshop.bean.param.product.ECardSaveParam;
import com.tigshop.bean.query.product.ECardPageQuery;
import com.tigshop.bean.vo.product.ECardItemVO;
import com.tigshop.bean.vo.product.ECardVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.product.ECardMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ECardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tigshop.common.constant.ResultTextConstants.INVALID_FIELD_VALUE;
import static com.tigshop.common.constant.product.ECardConstants.*;

/**
 * 电子卡券服务实现类
 *
 * @author kidd
 * @create 2025/7/2
 */
@RequiredArgsConstructor
@Service
public class ECardServiceImpl extends BaseServiceImpl<ECardMapper, ECard> implements ECardService {

    private final AdminLogService adminLogService;

    private final OrderMapper orderMapper;

    private final OrderItemMapper orderItemMapper;

    private final ProductMapper productMapper;

    @Override
    public Page<ECardVO> list(ECardPageQuery pageQuery) {
        Page<ECard> page = buildSortOrder(pageQuery);

        Page<ECard> eCardPage = this.lambdaQuery()
                .eq(ECard::getGroupId, pageQuery.getGroupId())
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), ECard::getCardNumber, pageQuery.getKeyword())
                .page(page);

        List<ECard> eCardRecords = eCardPage.getRecords();
        if (CollUtil.isEmpty(eCardRecords)) {
            return new Page<>();
        }

        List<ECardVO> resultRecords = eCardRecords.stream().map(ECardVO::new).toList();
        return PageUtil.convertPage(eCardPage, resultRecords);
    }

    @Override
    public ECardVO detail(Integer id) {
        ECard eCard = this.getById(id);
        return new ECardVO(eCard);
    }


    @Override
    public void create(ECardSaveParam param) {
        // 查询数据库，检查卡券是否已存在
        checkCardUnique(param);

        ECard eCard = param.createECard();
        this.save(eCard);
    }

    private void checkCardUnique(ECardSaveParam param) {
        Long cardCount = this.lambdaQuery().eq(ECard::getCardNumber, param.getCardNumber()).count();
        Assert.isTrue(cardCount == 0, () -> new GlobalException(CARD_NUMBER_EXISTS));
    }

    @Override
    public void update(ECardEditParam param) {
        ECard eCard = this.getById(param.getCardId());
        Assert.notNull(eCard, () -> new GlobalException(CARD_NOT_EXISTS));
        Assert.isFalse(eCard.getOrderId() > 0, () -> new GlobalException(CARD_USED_BY_ORDER));

        ECard updateECard = param.createECard();
        this.updateById(updateECard);
    }

    @Override
    public boolean del(Integer id) {
        if (id == null) {
            return false;
        }
        ECard eCardData = this.getById(id);
        Assert.isFalse(eCardData.getOrderId() > 0, () -> new GlobalException(CARD_USED_BY_ORDER));

        // 新增日志
        adminLogService.createByLogInfo(StrUtil.format("删除电子卡券信息：id:{}", id));
        return this.removeById(id);
    }

    @Override
    public boolean updateField(UpdateFieldDTO updateField, String[] allowFields) {
        ECard eCardData = this.getById(updateField.getId());
        Assert.isFalse(eCardData.getOrderId() > 0, () -> new GlobalException(CARD_USED_BY_ORDER));

        // 校验字段名
        String field = updateField.getField();
        field = getColumnByPropertyName(field);
        Assert.isTrue(Arrays.asList(allowFields).contains(field), () -> new GlobalException(INVALID_FIELD_VALUE));

        // 构造更新条件
        UpdateWrapper<ECard> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(getKeyColumn(), updateField.getId()).set(field, updateField.getVal());

        //新增日志
        adminLogService.createByLogInfo(StrUtil.format("更新电子卡券信息：id:{}", updateField.getId()));
        return this.update(updateWrapper);
    }


    @Override
    public List<ECardItemVO> getCardByOrderItemId(Integer orderItemId) {
        LambdaQueryWrapper<ECard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ECard::getOrderItemId, orderItemId);
        List<ECard> eCards = this.list(queryWrapper);
        if (eCards.isEmpty()) {
            return new ArrayList<>();
        }
        return this.list(queryWrapper).stream().map(eCard -> {
            ECardItemVO eCardItemVO = new ECardItemVO();
            BeanUtils.copyProperties(eCard, eCardItemVO);
            return eCardItemVO;
        }).toList();
    }

    @Override
    @Transactional
    public boolean getCardByOrderId(Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null && order.getOrderType() == OrderType.CARD.getCode()) {
            OrderItem orderItem = orderItemMapper.selectOne(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
            Product product = productMapper.selectById(orderItem.getProductId());
            // 获取可使用的卡券
            List<ECard> eCards = getNewCardByGroupId(product.getCardGroupId(), orderItem.getQuantity());
            for (ECard eCard : eCards) {
                eCard.setIsUse(1);
                eCard.setOrderId(orderId);
                eCard.setOrderItemId(orderItem.getItemId());
            }
            updateBatchById(eCards);
        }
        return true;
    }

    @Override
    public List<ECard> getNewCardByGroupId(Integer cardGroupId, Integer quantity) {
        QueryWrapper<ECard> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", cardGroupId)
                .eq("is_use", 0)
                .orderByDesc("card_id")
                .last("LIMIT " + quantity);
        return list(wrapper);
    }

    /**
     * 分组内未使用最新的卡券
     */
    public List<ECard> getNewCardByGroupId(int groupId, int limit) {
        List<ECard> list = list(new LambdaQueryWrapper<ECard>()
                .eq(ECard::getGroupId, groupId)
                .eq(ECard::getIsUse, 0)
                .orderByDesc(ECard::getCardId)
                .last("limit " + limit)
        );
        return list;
    }

}
