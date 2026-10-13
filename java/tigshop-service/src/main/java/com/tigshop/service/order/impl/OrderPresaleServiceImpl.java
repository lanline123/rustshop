// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.order.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.dto.order.OrderPresaleDTO;
import com.tigshop.bean.model.order.OrderPresale;
import com.tigshop.bean.model.product.ProductPresale;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderPresaleVO;
import com.tigshop.bean.vo.product.ProductPresaleVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.order.OrderPresaleMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.OrderItemService;
import com.tigshop.service.order.OrderPresaleService;
import com.tigshop.service.product.ProductPresaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 订单预售服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月24日
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPresaleServiceImpl extends BaseServiceImpl<OrderPresaleMapper, OrderPresale> implements OrderPresaleService {

    private final OrderPresaleMapper orderPresaleMapper;
    private final OrderItemService orderItemService;
    private final ProductPresaleService productPresaleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPresaleOrder(Integer orderId, String orderSn, OrderPresaleDTO presaleDTO) {
        if (presaleDTO == null || presaleDTO.getSaleMode() == null) {
            throw new GlobalException("预售配置不能为空");
        }

        Long currentTime = StringUtils.getCurrentTime();

        OrderPresale orderPresale = OrderPresale.builder()
                .orderId(orderId)
                .orderSn(orderSn)
                .productId(presaleDTO.getProductId())
                .saleMode(presaleDTO.getSaleMode())
                .depositAmount(presaleDTO.getDepositAmount())
                .finalPaymentAmount(presaleDTO.getFinalPaymentAmount())
                .presaleStatus(0) // 待支付定金
                .deliveryDays(presaleDTO.getDeliveryDays())
                .deliveryDate(presaleDTO.getDeliveryDate())
                .createTime(currentTime)
                .updateTime(currentTime)
                .build();

        return this.save(orderPresale);
    }

    @Override
    public OrderPresale getByOrderId(Integer orderId) {
        return orderPresaleMapper.selectByOrderId(orderId);
    }

    @Override
    public OrderPresaleVO getPresaleVOByOrderId(Integer orderId) {
        OrderPresale orderPresale = getByOrderId(orderId);
        if (orderPresale == null) {
            return null;
        }


        OrderPresaleVO vo = BeanUtil.copyProperties(orderPresale, OrderPresaleVO.class);
        vo.setFinalPaymentStartTimeText(TigUtils.handelTime(vo.getFinalPaymentStartTime(),"yyyy-MM-dd HH:mm"));

        List<OrderItemVO> items = orderItemService.getItemByOrderIds(Arrays.asList(orderId));
        if(Objects.nonNull(items) && !items.isEmpty()){
            OrderItemVO item = items.get(0);
            Integer productId = item.getProductId();
            ProductPresaleVO productPresaleVO = productPresaleService.getPresaleVOByProductId(productId);
            vo.setProductPresale(productPresaleVO);
        }

        // 设置销售模式文本
        if (orderPresale.getSaleMode() != null) {
            vo.setSaleModeText(orderPresale.getSaleMode() == 1 ? "全款预售" : "定金预售");
        }

        // 设置预售状态文本
        if (orderPresale.getPresaleStatus() != null) {
            switch (orderPresale.getPresaleStatus()) {
                case 0:
                    vo.setPresaleStatusText("待支付定金");
                    break;
                case 1:
                    vo.setPresaleStatusText("待支付尾款");
                    break;
                case 2:
                    vo.setPresaleStatusText("已完成支付");
                    break;
                default:
                    vo.setPresaleStatusText("未知状态");
            }
        }

        // 判断是否可以支付尾款
        vo.setCanPayFinalPayment(canPayFinalPayment(orderId));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDepositPaid(Integer orderId, Integer finalPaymentRelativeDays) {
        OrderPresale orderPresale = getByOrderId(orderId);
        if (orderPresale == null) {
            throw new GlobalException("预售订单不存在");
        }

        Long currentTime = StringUtils.getCurrentTime();
        orderPresale.setDepositPaidTime(currentTime);
        
        // 如果是定金预售，计算尾款开始支付时间
        if (orderPresale.getSaleMode() == 2) {
            Long finalPaymentStartTime = calculateFinalPaymentStartTime(currentTime, finalPaymentRelativeDays);
            orderPresale.setFinalPaymentStartTime(finalPaymentStartTime);
            orderPresale.setPresaleStatus(1); // 待支付尾款
        } else {
            // 全款预售，直接完成
            orderPresale.setPresaleStatus(2); // 已完成支付
        }
        
        orderPresale.setUpdateTime(currentTime);

        return this.updateById(orderPresale);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFinalPaymentPaid(Integer orderId) {
        OrderPresale orderPresale = getByOrderId(orderId);
        if (orderPresale == null) {
            throw new GlobalException("预售订单不存在");
        }

        if (orderPresale.getSaleMode() != 2) {
            throw new GlobalException("该订单不是定金预售订单");
        }

        if (orderPresale.getPresaleStatus() != 1) {
            throw new GlobalException("订单状态不正确，无法支付尾款");
        }

        // 检查是否可以支付尾款（到了尾款开始支付时间）
//        if (!canPayFinalPayment(orderId)) {
//            throw new GlobalException("尚未到尾款支付时间");
//        }

        Long currentTime = StringUtils.getCurrentTime();
        orderPresale.setFinalPaymentPaidTime(currentTime);
        orderPresale.setPresaleStatus(2); // 已完成支付
        orderPresale.setUpdateTime(currentTime);

        return this.updateById(orderPresale);
    }

    @Override
    public boolean canPayFinalPayment(Integer orderId) {
        OrderPresale orderPresale = getByOrderId(orderId);
        if (orderPresale == null) {
            return false;
        }

        // 只有定金预售才需要支付尾款
        if (orderPresale.getSaleMode() != 2) {
            return false;
        }

        // 状态必须是待支付尾款
        if (orderPresale.getPresaleStatus() != 1) {
            return false;
        }

        // 必须已支付定金
        if (orderPresale.getDepositPaidTime() == null) {
            return false;
        }

        // 必须到了尾款开始支付时间
        Long currentTime = StringUtils.getCurrentTime();
        if (orderPresale.getFinalPaymentStartTime() == null || currentTime < orderPresale.getFinalPaymentStartTime()) {
            return false;
        }

        return true;
    }

    @Override
    public Long calculateFinalPaymentStartTime(Long depositPaidTime, Integer finalPaymentRelativeDays) {
        if (depositPaidTime == null || finalPaymentRelativeDays == null || finalPaymentRelativeDays <= 0) {
            // 如果没有设置相对天数，默认立即可以支付尾款
            return depositPaidTime;
        }

        // 计算尾款开始支付时间 = 定金支付时间 + N天
        return depositPaidTime + (finalPaymentRelativeDays * 24 * 60 * 60L);
    }
}

