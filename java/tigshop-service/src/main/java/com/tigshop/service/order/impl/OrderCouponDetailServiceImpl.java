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

package com.tigshop.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderCouponDetailCreateDTO;
import com.tigshop.bean.dto.order.OrderCouponDetailListDTO;
import com.tigshop.bean.dto.order.OrderCouponDetailUpdateDTO;
import com.tigshop.bean.model.order.OrderCouponDetail;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.OrderCouponDetailVO;
import com.tigshop.mapper.order.OrderCouponDetailMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.OrderCouponDetailService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单店铺优惠券服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class OrderCouponDetailServiceImpl extends BaseServiceImpl<OrderCouponDetailMapper, OrderCouponDetail> implements OrderCouponDetailService {
    @Override
    public ListResVO<OrderCouponDetailVO, OrderCouponDetailListDTO> list(OrderCouponDetailListDTO listDTO) {
        // 分页
        Page<OrderCouponDetail> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<OrderCouponDetail> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        /*Integer isShow = listDTO.getIsShow();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq("is_show", isShow);
        }*/

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like("order_coupon_detail", keyword);
        }*/
        // 执行查询
        Page<OrderCouponDetail> orderCouponDetailPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<OrderCouponDetail> orderCouponDetailPageRecords = orderCouponDetailPage.getRecords();
        // 转换为VO
        List<OrderCouponDetailVO> orderCouponDetailVOList = orderCouponDetailPageRecords.stream()
                .map(orderCouponDetail -> {
                    OrderCouponDetailVO orderCouponDetailVO = new OrderCouponDetailVO();
                    BeanUtils.copyProperties(orderCouponDetail, orderCouponDetailVO);
                    return orderCouponDetailVO;
                }).toList();
        return new ListResVO<>(orderCouponDetailVOList, listDTO, orderCouponDetailPage.getTotal());
    }

    @Override
    public OrderCouponDetailVO detail(Integer id) {
        if (id != null) {
            OrderCouponDetail orderCouponDetail = this.getById(id);
            OrderCouponDetailVO orderCouponDetailVO = new OrderCouponDetailVO();
            BeanUtils.copyProperties(orderCouponDetail, orderCouponDetailVO);
            return orderCouponDetailVO;
        }
        return new OrderCouponDetailVO();
    }

    @Override
    public boolean create(OrderCouponDetailCreateDTO createDTO) {
        if (createDTO != null) {
            OrderCouponDetail orderCouponDetail = new OrderCouponDetail();
            BeanUtils.copyProperties(createDTO, orderCouponDetail);
            return this.save(orderCouponDetail);
        }
        return false;
    }

    @Override
    public boolean update(OrderCouponDetailUpdateDTO updateDTO) {
        if (updateDTO != null) {
            OrderCouponDetail orderCouponDetail = new OrderCouponDetail();
            BeanUtils.copyProperties(updateDTO, orderCouponDetail);
            return this.updateById(orderCouponDetail);
        }
        return false;
    }
}
