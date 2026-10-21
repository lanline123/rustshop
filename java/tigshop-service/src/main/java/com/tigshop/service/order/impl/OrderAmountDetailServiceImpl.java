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
import com.tigshop.bean.dto.order.OrderAmountDetailCreateDTO;
import com.tigshop.bean.dto.order.OrderAmountDetailListDTO;
import com.tigshop.bean.dto.order.OrderAmountDetailUpdateDTO;
import com.tigshop.bean.model.order.OrderAmountDetail;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.OrderAmountDetailVO;
import com.tigshop.mapper.order.OrderAmountDetailMapper;
import com.tigshop.service.order.OrderAmountDetailService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单店铺金额服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class OrderAmountDetailServiceImpl extends BaseServiceImpl<OrderAmountDetailMapper, OrderAmountDetail> implements OrderAmountDetailService {
    @Override
    public ListResVO<OrderAmountDetailVO, OrderAmountDetailListDTO> list(OrderAmountDetailListDTO listDTO) {
        // 分页
        Page<OrderAmountDetail> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<OrderAmountDetail> queryWrapper = new LambdaQueryWrapper<>();

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
            queryWrapper.like("order_amount_detail", keyword);
        }*/
        // 执行查询
        Page<OrderAmountDetail> orderAmountDetailPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<OrderAmountDetail> orderAmountDetailPageRecords = orderAmountDetailPage.getRecords();
        // 转换为VO
        List<OrderAmountDetailVO> orderAmountDetailVOList = orderAmountDetailPageRecords.stream()
                .map(orderAmountDetail -> {
                    OrderAmountDetailVO orderAmountDetailVO = new OrderAmountDetailVO();
                    BeanUtils.copyProperties(orderAmountDetail, orderAmountDetailVO);
                    return orderAmountDetailVO;
                }).toList();
        return new ListResVO<>(orderAmountDetailVOList, listDTO, orderAmountDetailPage.getTotal());
    }

    @Override
    public OrderAmountDetailVO detail(Integer id) {
        if (id != null) {
            OrderAmountDetail orderAmountDetail = this.getById(id);
            OrderAmountDetailVO orderAmountDetailVO = new OrderAmountDetailVO();
            BeanUtils.copyProperties(orderAmountDetail, orderAmountDetailVO);
            return orderAmountDetailVO;
        }
        return new OrderAmountDetailVO();
    }

    @Override
    public boolean create(OrderAmountDetailCreateDTO createDTO) {
        if (createDTO != null) {
            OrderAmountDetail orderAmountDetail = new OrderAmountDetail();
            BeanUtils.copyProperties(createDTO, orderAmountDetail);
            return this.save(orderAmountDetail);
        }
        return false;
    }

    @Override
    public boolean update(OrderAmountDetailUpdateDTO updateDTO) {
        if (updateDTO != null) {
            OrderAmountDetail orderAmountDetail = new OrderAmountDetail();
            BeanUtils.copyProperties(updateDTO, orderAmountDetail);
            return this.updateById(orderAmountDetail);
        }
        return false;
    }
}
