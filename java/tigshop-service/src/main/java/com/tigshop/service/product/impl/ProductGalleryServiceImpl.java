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

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.product.ProductGallery;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.mapper.product.ProductGalleryMapper;
import com.tigshop.service.product.ProductGalleryService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品相册服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月27日 15:51
 */
@Service
public class ProductGalleryServiceImpl extends BaseServiceImpl<ProductGalleryMapper, ProductGallery> implements ProductGalleryService {

    @Autowired
    private TranslatePackage translatePackage;

    @Override
    public List<ProductGallery> getProductGalleryList(Integer productId) {
        LambdaQueryWrapper<ProductGallery> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductGallery::getProductId, productId)
                .eq(ProductGallery::getShopId, 0L)
                .orderByAsc(ProductGallery::getSortOrder);
        List<ProductGallery> list = this.list(queryWrapper);
        translateGalleryPic(list);
        return list;
    }

    @Override
    public List<ProductGallery> getProductGalleryList(Integer productId, Long shopId) {
        if (shopId == null || shopId == 0L) {
            return getProductGalleryList(productId);
        }
        LambdaQueryWrapper<ProductGallery> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductGallery::getProductId, productId)
                .eq(ProductGallery::getShopId, shopId)
                .orderByAsc(ProductGallery::getSortOrder);
        List<ProductGallery> list = this.list(queryWrapper);
        if (list.isEmpty()) {
            return getProductGalleryList(productId);
        }
        translateGalleryPic(list);
        return list;
    }

    private void translateGalleryPic(List<ProductGallery> list) {
        list.forEach(item -> {
            String picUrlOld = item.getPicUrl();
            //翻译

            String translate = translatePackage.translate(item.getPicUrl(), 7);
            if (StrUtil.isNotBlank(translate)){
                item.setPicUrl(translate);
            }
            if(!item.getPicUrl().equals(picUrlOld)){
                item.setPicLarge(item.getPicUrl());
            }

        });
    }
}
