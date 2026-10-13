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
import com.tigshop.bean.dto.product.ProductAttributeDTO;
import com.tigshop.bean.dto.product.ProductAttrsDTO;
import com.tigshop.bean.enums.product.AttrType;
import com.tigshop.bean.model.product.ProductAttribute;
import com.tigshop.mapper.product.ProductAttributesMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductAttributesService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品属性服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月28日 09:56
 */
@Service
public class ProductAttributesServiceImpl extends BaseServiceImpl<ProductAttributesMapper, ProductAttribute> implements ProductAttributesService {
    @Override
    public List<ProductAttribute> getAttributesByProductId(int productId) {
        LambdaQueryWrapper<ProductAttribute> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductAttribute::getProductId, productId)
                .orderByAsc(ProductAttribute::getSortOrder);
        return this.list(queryWrapper);
    }

    @Override
    public ProductAttrsDTO getProductAttributes(int productId) {
        // 获取商品的所有属性
        List<ProductAttribute> attributes = getAttributesByProductId(productId);

        // 创建DTO
        ProductAttrsDTO dto = new ProductAttrsDTO();

        // 按属性类型分组
        Map<Integer, List<ProductAttributeDTO>> groupedByType = attributes.stream()
                .map(attr -> {
                    ProductAttributeDTO productAttributeDTO = new ProductAttributeDTO();
                    BeanUtils.copyProperties(attr, productAttributeDTO);
                    productAttributeDTO.setAttrPrice(attr.getAttrPrice());
                    return productAttributeDTO;
                })
                .collect(Collectors.groupingBy(ProductAttributeDTO::getAttrType));

        // 处理普通属性
        List<ProductAttrsDTO.Attribute> normalAttributes = groupAttributesByName(groupedByType.getOrDefault(AttrType.NORMAL.getCode(), List.of()));
        // 处理规格属性
        List<ProductAttrsDTO.Attribute> specAttributes = groupAttributesByName(groupedByType.getOrDefault(AttrType.SPEC.getCode(), List.of()));

        // 处理附加属性
        List<ProductAttrsDTO.Attribute> extraAttributes = groupAttributesByName(groupedByType.getOrDefault(AttrType.EXTRA.getCode(), List.of()));

        // 设置DTO
        dto.setNormal(normalAttributes);
        dto.setSpe(specAttributes);
        dto.setExtra(extraAttributes);

        return dto;
    }

    /**
     * 按 attrName 分组并转换为 Attribute 列表
     */
    private List<ProductAttrsDTO.Attribute> groupAttributesByName(List<ProductAttributeDTO> attributes) {
        if (attributes.isEmpty()) {
            return List.of();
        }

        // 按 attrName 分组
        Map<String, List<ProductAttributeDTO>> groupedByName = attributes.stream()
                .collect(Collectors.groupingBy(
                        ProductAttributeDTO::getAttrName,
                        LinkedHashMap::new, // 👈 指定用 LinkedHashMap
                        Collectors.toList()
                ));

        // 转换为 Attribute 对象列表
        return groupedByName.entrySet().stream()
                .map(entry -> {
                    ProductAttrsDTO.Attribute attribute = new ProductAttrsDTO.Attribute();
                    attribute.setAttrName(entry.getKey());
                    attribute.setAttrList(entry.getValue());
                    return attribute;
                }).collect(Collectors.toList());
    }

    @Override
    public List<ProductAttribute> getProductAttributesByIds(List<Integer> ids) {
        LambdaQueryWrapper<ProductAttribute> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductAttribute::getAttributesId, ids);
        return this.list(queryWrapper);
    }
}
