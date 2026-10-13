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

package com.tigshop.service.shop.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderConfigDTO;
import com.tigshop.bean.dto.shop.OrderConfigCreateDTO;
import com.tigshop.bean.dto.shop.OrderConfigListDTO;
import com.tigshop.bean.dto.shop.OrderConfigUpdateDTO;
import com.tigshop.bean.model.shop.OrderConfig;
import com.tigshop.bean.vo.shop.OrderConfigVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.shop.OrderConfigMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.shop.OrderConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单配置服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class OrderConfigServiceImpl extends BaseServiceImpl<OrderConfigMapper, OrderConfig> implements OrderConfigService {
    @Override
    public Page<OrderConfigVO> list(OrderConfigListDTO listDTO) {
        // 分页
        Page<OrderConfig> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<OrderConfig> queryWrapper = new LambdaQueryWrapper<>();

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
        Page<OrderConfig> orderConfigPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<OrderConfig> orderConfigPageRecords = orderConfigPage.getRecords();
        // 转换为VO
        List<OrderConfigVO> orderConfigVOList = orderConfigPageRecords.stream()
                .map(orderConfig -> {
                    OrderConfigVO orderConfigVO = new OrderConfigVO();
                    BeanUtils.copyProperties(orderConfig, orderConfigVO);
                    return orderConfigVO;
                }).toList();
        return PageUtil.convertPage(orderConfigPage, orderConfigVOList);
    }

    @Override
    public OrderConfigVO detail(Integer id) {
        if (id != null) {
            OrderConfig orderConfig = this.getById(id);
            OrderConfigVO orderConfigVO = new OrderConfigVO();
            BeanUtils.copyProperties(orderConfig, orderConfigVO);
            return orderConfigVO;
        }
        return null;
    }

    @Override
    public JSON shopOrderConfigDetail(Integer shopId, String code) {
        OrderConfig config = this.lambdaQuery()
                .eq(OrderConfig::getCode, code)
                .eq(OrderConfig::getShopId, shopId)
                .one();
        return config != null && config.getData() != null ? JSONUtil.parse(config.getData()) : null;
    }

    @Override
    public boolean create(OrderConfigCreateDTO createDTO) {
        if (createDTO != null) {
            OrderConfig orderConfig = new OrderConfig();
            BeanUtils.copyProperties(createDTO, orderConfig);
            return this.save(orderConfig);
        }
        return false;
    }

    @Override
    public boolean update(OrderConfigUpdateDTO updateDTO) {
        if (updateDTO != null) {
            OrderConfig orderConfig = new OrderConfig();
            BeanUtils.copyProperties(updateDTO, orderConfig);
            return this.updateById(orderConfig);
        }
        return false;
    }

    @Override
    public void saveOrderConfig(OrderConfigDTO dto) {
        if (dto == null) {
            return;
        }
        OrderConfig one = getOne(new LambdaQueryWrapper<OrderConfig>().eq(OrderConfig::getCode, dto.getCode()).eq(OrderConfig::getShopId, dto.getShopId()));
        if (one == null) {
            //create
            OrderConfig orderConfig = new OrderConfig();
            orderConfig.setCode(dto.getCode());
            orderConfig.setShopId(dto.getShopId());
            orderConfig.setData(JSONUtil.toJsonStr(dto));
            this.save(orderConfig);
        } else {
            //update
            one.setCode(dto.getCode());
            one.setShopId(dto.getShopId());
            one.setData(JSONUtil.toJsonStr(dto));
            this.updateById(one);
        }
    }
}
