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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.OrderFinanceAftersalesCreateDTO;
import com.tigshop.bean.dto.finance.OrderFinanceAftersalesListDTO;
import com.tigshop.bean.dto.finance.OrderFinanceAftersalesUpdateDTO;
import com.tigshop.bean.model.finance.OrderFinanceAftersales;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.finance.OrderFinanceAftersalesVO;
import com.tigshop.mapper.finance.OrderFinanceAftersalesMapper;
import com.tigshop.service.finance.OrderFinanceAftersalesService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 售后申请表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class OrderFinanceAftersalesServiceImpl extends BaseServiceImpl<OrderFinanceAftersalesMapper, OrderFinanceAftersales> implements OrderFinanceAftersalesService {
    @Override
    public ListResVO<OrderFinanceAftersalesVO, OrderFinanceAftersalesListDTO> list(OrderFinanceAftersalesListDTO listDTO) {
        // 分页
        Page<OrderFinanceAftersales> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<OrderFinanceAftersales> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        /*Integer isShow = listDTO.getIsShow();
        if (IsShowType.fromTypeCode(isShow) != null) {
            queryWrapper.eq("is_show", isShow);
        }*/

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like("", keyword);
        }*/
        // 执行查询
        Page<OrderFinanceAftersales> orderFinanceAftersalesPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<OrderFinanceAftersales> orderFinanceAftersalesPageRecords = orderFinanceAftersalesPage.getRecords();
        // 转换为VO
        List<OrderFinanceAftersalesVO> orderFinanceAftersalesVOList = orderFinanceAftersalesPageRecords.stream()
                .map(orderFinanceAftersales -> {
                    OrderFinanceAftersalesVO orderFinanceAftersalesVO = new OrderFinanceAftersalesVO();
                    BeanUtils.copyProperties(orderFinanceAftersales, orderFinanceAftersalesVO);
                    return orderFinanceAftersalesVO;
                }).toList();
        return new ListResVO<>(orderFinanceAftersalesVOList, listDTO, orderFinanceAftersalesPage.getTotal());
    }

    @Override
    public OrderFinanceAftersalesVO detail(Integer id) {
        if (id != null) {
            OrderFinanceAftersales orderFinanceAftersales = this.getById(id);
            OrderFinanceAftersalesVO orderFinanceAftersalesVO = new OrderFinanceAftersalesVO();
            BeanUtils.copyProperties(orderFinanceAftersales, orderFinanceAftersalesVO);
            return orderFinanceAftersalesVO;
        }
        return new OrderFinanceAftersalesVO();
    }

    @Override
    public boolean create(OrderFinanceAftersalesCreateDTO createDTO) {
        if (createDTO != null) {
            OrderFinanceAftersales orderFinanceAftersales = new OrderFinanceAftersales();
            BeanUtils.copyProperties(createDTO, orderFinanceAftersales);
            return this.save(orderFinanceAftersales);
        }
        return false;
    }

    @Override
    public boolean update(OrderFinanceAftersalesUpdateDTO updateDTO) {
        if (updateDTO != null) {
            OrderFinanceAftersales orderFinanceAftersales = new OrderFinanceAftersales();
            BeanUtils.copyProperties(updateDTO, orderFinanceAftersales);
            return this.updateById(orderFinanceAftersales);
        }
        return false;
    }

    @Override
    public OrderFinanceAftersales getInfoById(Integer id) {
        return this.getById(id);
    }
}
