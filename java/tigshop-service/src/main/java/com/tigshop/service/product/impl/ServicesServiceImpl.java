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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ServicesDTO;
import com.tigshop.bean.model.product.Services;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.product.ServicesMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ServicesService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tigshop.common.constant.product.ServicesConstants.SERVICE_NOT_FOUND;

/**
 * 商品服务实现
 *
 * @author Jayce
 * @create 2024年11月06日 14:00
 */
@Service
public class ServicesServiceImpl extends BaseServiceImpl<ServicesMapper, Services> implements ServicesService {
    @Override
    public Page<ServicesDTO> list(BasePage basePage) {
        // 分页参数
        Page<Services> page = new Page<>(basePage.getPage(), basePage.getSize());
        // 创建查询构造器
        LambdaQueryWrapper<Services> queryWrapper = new LambdaQueryWrapper<>();
        // 搜索关键字
        String keyword = basePage.getKeyword();
        if (keyword != null) {
            queryWrapper.like(Services::getProductServiceName, keyword);
        }

        // 排序字段
        buildSortOrder(page, basePage.getSortField(), basePage.getSortOrder());

        Page<Services> servicesPage = this.page(page, queryWrapper);
        // 转换记录为 DTO
        List<ServicesDTO> servicesList = servicesPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();
        return PageUtil.convertPage(servicesPage, servicesList);
    }

    /**
     * 转换为DTO
     *
     * @param services 商品服务实体
     * @return 商品服务DTO
     */
    private ServicesDTO convertToDTO(Services services) {
        ServicesDTO dto = new ServicesDTO();
        BeanUtils.copyProperties(services, dto);
        return dto;
    }

    private Services convertToBean(ServicesDTO dto) {
        Services services = new Services();
        BeanUtils.copyProperties(dto, services);
        return services;
    }

    @Override
    public boolean create(ServicesDTO servicesDTO) {
        return this.save(convertToBean(servicesDTO));
    }

    @Override
    public boolean update(ServicesDTO servicesDTO) {
        return this.updateById(convertToBean(servicesDTO));
    }

    @Override
    public ServicesDTO detail(Integer id) {
        Services service = this.getById(id);
        if (service == null) {
            throw new GlobalException(SERVICE_NOT_FOUND);
        }
        return convertToDTO(service);
    }
}
