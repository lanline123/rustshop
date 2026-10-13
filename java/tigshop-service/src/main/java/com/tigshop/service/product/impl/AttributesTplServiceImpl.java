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

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.AttributeTplDataDTO;
import com.tigshop.bean.dto.product.AttributesTplDTO;
import com.tigshop.bean.model.product.AttributesTpl;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.mapper.product.AttributesTplMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.AttributesTplService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.tigshop.common.constant.product.AttributesTplConstants.TPL_NOT_FOUND;

/**
 * 商品属性模板服务实现类
 *
 * @author Tigshop团队
 * @create 2024年11月04日 17:16
 */
@Service
public class AttributesTplServiceImpl extends BaseServiceImpl<AttributesTplMapper, AttributesTpl> implements AttributesTplService {

    @Override
    public boolean create(AttributesTplDTO dto) {
        dto.setShopId(currentShopId());
        return this.save(convertToBean(dto));
    }

    @Override
    public boolean update(AttributesTplDTO dto) {
        assertOwned(dto.getTplId());
        dto.setShopId(currentShopId());
        return this.updateById(convertToBean(dto));
    }

    /**
     * 转换为实体
     *
     * @param dto 属性模板 DTO
     * @return 属性模板实体
     */
    private static AttributesTpl convertToBean(AttributesTplDTO dto) {
        AttributesTpl attributesTpl = new AttributesTpl();
        BeanUtils.copyProperties(dto, attributesTpl);

        // 使用 Optional.ofNullable 和三元运算符简化 tplData 转换
        attributesTpl.setTplData(
            Optional.ofNullable(dto.getTplData())
                    .map(JSONUtil::toJsonStr)
                    .orElse("")
        );

        return attributesTpl;
    }

    @Override
    public Page<AttributesTplDTO> list(BasePage basePage) {
        // 分页参数
        Page<AttributesTpl> page = new Page<>(basePage.getPage(), basePage.getSize());
        // 创建查询构造器
        LambdaQueryWrapper<AttributesTpl> queryWrapper = new LambdaQueryWrapper<>();
        // 搜索条件关键字
        Integer shopId = Optional.ofNullable(HeaderUtils.getShopId()).orElse(0);
        queryWrapper.eq(AttributesTpl::getShopId, shopId);
        if (basePage.getKeyword() != null) {
            queryWrapper.like(AttributesTpl::getTplName, basePage.getKeyword());
        }

        // 排序字段
        buildSortOrder(page, basePage.getSortField(), basePage.getSortOrder());

        // 执行分页查询
        Page<AttributesTpl> attributesTplPage = this.page(page, queryWrapper);
        // 转换记录为 DTO
        List<AttributesTplDTO> attributesTplList = attributesTplPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();
        return PageUtil.convertPage(attributesTplPage, attributesTplList);
    }

    /**
     * 转换为 DTO
     *
     * @param attributesTpl 属性模板实体
     * @return 属性模板 DTO
     */
    private AttributesTplDTO convertToDTO(AttributesTpl attributesTpl) {
        AttributesTplDTO dto = new AttributesTplDTO();
        BeanUtils.copyProperties(attributesTpl, dto);

        // 使用 Optional 处理 tplData 的转换
        dto.setTplData(
            Optional.ofNullable(attributesTpl.getTplData())
                    .filter(StrUtil::isNotEmpty)
                    .map(JSONUtil::parseObj)
                    .map(tplDataObject -> tplDataObject.toBean(AttributeTplDataDTO.class))
                    .orElse(null)
        );

        return dto;
    }

    @Override
    public AttributesTplDTO detail(Integer id) {
        return convertToDTO(assertOwned(id));
    }

    @Override
    public List<AttributesTplDTO> listByCategory(Integer categoryId) {
        List<AttributesTpl> templates = this.lambdaQuery()
                .eq(AttributesTpl::getShopId, Optional.ofNullable(HeaderUtils.getShopId()).orElse(0))
                .eq(AttributesTpl::getCategoryId, categoryId)
                .list();
        return templates.stream().map(this::convertToDTO).toList();
    }

    @Override
    public boolean del(Integer id) {
        assertOwned(id);
        return super.del(id);
    }

    @Override
    public boolean batch(com.tigshop.bean.dto.common.BatchDTO batchDTO) {
        if (batchDTO.getIds() != null) {
            batchDTO.getIds().forEach(this::assertOwned);
        }
        return super.batch(batchDTO);
    }

    @Override
    public boolean updateField(com.tigshop.bean.dto.common.UpdateFieldDTO updateField, String[] allowFields) {
        assertOwned(updateField.getId());
        return super.updateField(updateField, allowFields);
    }

    private Integer currentShopId() {
        return Optional.ofNullable(HeaderUtils.getShopId()).orElse(0);
    }

    private AttributesTpl assertOwned(Integer id) {
        Assert.notNull(id, () -> new GlobalException(TPL_NOT_FOUND));
        AttributesTpl attributesTpl = this.lambdaQuery()
                .eq(AttributesTpl::getTplId, id)
                .eq(AttributesTpl::getShopId, currentShopId())
                .one();
        Assert.notNull(attributesTpl, () -> new GlobalException(TPL_NOT_FOUND));
        return attributesTpl;
    }
}
