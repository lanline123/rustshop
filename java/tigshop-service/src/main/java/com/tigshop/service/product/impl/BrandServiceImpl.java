// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

//**---------------------------------------------------------------------+
//** 业务实现类文件 -- 品牌
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+
package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.product.BrandAuditDTO;
import com.tigshop.bean.dto.product.BrandDTO;
import com.tigshop.bean.dto.product.BrandListDTO;
import com.tigshop.bean.dto.product.BrandSearchDTO;
import com.tigshop.bean.enums.product.BrandStatusEnum;
import com.tigshop.bean.enums.product.CheckStatus;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.product.BrandSearchVO;
import com.tigshop.bean.vo.product.BrandVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.product.BrandMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.BrandService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;
import static com.tigshop.common.constant.product.BrandConstants.BRAND_NAME_EXISTS;
import static com.tigshop.common.constant.product.BrandConstants.BRAND_NOT_FOUND;

/**
 * 商品品牌
 *
 * @author Jayce
 * @create 2024-09-30 15:16:06
 */
@Service
public class BrandServiceImpl extends BaseServiceImpl<BrandMapper, Brand> implements BrandService {


    @Resource
    private AdminLogService adminLogService;

    @Resource
    private ShopMapper shopMapper;

    /**
     * 构建品牌查询条件
     *
     * @param dto 查询参数
     * @return QueryWrapper
     */
    private static LambdaQueryWrapper<Brand> getBrandQueryWrapper(BrandListDTO dto) {
        LambdaQueryWrapper<Brand> queryWrapper = new LambdaQueryWrapper<>();
        Integer shopId = HeaderUtils.getShopId();

        if (shopId != null && shopId == 0 && dto.getShopId() != null) {
            queryWrapper.eq(Brand::getShopId, dto.getShopId());
        }

        if (shopId != null && shopId > 0) {
            queryWrapper.eq(Brand::getShopId, shopId);
        }

        Integer status = dto.getStatus();
        if (status != null && status > -1) {
            queryWrapper.eq(Brand::getStatus, status);
        }

        // 品牌名称判断
        String keyword = dto.getKeyword();
        if (keyword != null) {
            queryWrapper.like(Brand::getBrandName, keyword);
        }

        // 品牌首字母判断
        String firstWord = dto.getFirstWord();
        if (firstWord != null) {
            queryWrapper.like(Brand::getFirstWord, firstWord);
        }

        // 显示状态判断
        Integer isShow = dto.getIsShow();
        if (isShow != null) {
            queryWrapper.eq(Brand::getIsShow, isShow);
        }

        // 热门品牌判断
        Integer brandIsHot = dto.getBrandIsHot();
        if (brandIsHot != null) {
            queryWrapper.eq(Brand::getBrandIsHot, brandIsHot);
        }

        Integer isAudit = dto.getIsAudit();
        if (isAudit != null && isAudit == 1) {
            queryWrapper.ne(Brand::getShopId, 0);
        }
        return queryWrapper;
    }

    @Override
    public Page<BrandVO> list(BrandListDTO dto) {
        //分页参数
        Page<Brand> page = new Page<>(dto.getPage(), dto.getSize());
        // 创建查询构造器
        LambdaQueryWrapper<Brand> queryWrapper = getBrandQueryWrapper(dto);

        // 排序字段
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());

        Page<Brand> brandPage = this.page(page, queryWrapper);
        List<Brand> records = brandPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageUtil.convertPage(brandPage, new ArrayList<>());
        }

        List<Integer> shopIds = records.stream()
                .map(Brand::getShopId)
                .filter(shopId -> shopId != null && shopId > 0)
                .distinct()
                .toList();
        Map<Integer, Shop> brandMap = new HashMap<>();

        if (CollUtil.isNotEmpty(shopIds)) {
            List<Shop> shopList = shopMapper.selectBatchIds(shopIds);
            brandMap = shopList.stream().collect(Collectors.toMap(Shop::getShopId, Function.identity()));
        }
        Map<Integer, Shop> finalBrandMap = brandMap;
        List<BrandVO> list = records.stream().map(brand -> {
            BrandVO brandVO = new BrandVO();
            BeanUtils.copyProperties(brand, brandVO);
            brandVO.setShop(finalBrandMap.get(brand.getShopId()));
            brandVO.setStatusText(BrandStatusEnum.getStatusName(brand.getStatus()));
            return brandVO;
        }).toList();
        // 分页查询
        return PageUtil.convertPage(brandPage, list);
    }

    @Override
    public BrandDTO detail(Integer id) {
        Brand brand = this.getById(id);
        if (brand == null) {
            throw new GlobalException(BRAND_NOT_FOUND);
        }
        // 转换为VO
        BrandDTO brandDTO = new BrandDTO();
        BeanUtils.copyProperties(brand, brandDTO);
        return brandDTO;
    }

    @Override
    public String create(BrandDTO brandDTO) {
        Integer shopId = HeaderUtils.getShopId();
        checkBrand(brandDTO, shopId);
        Brand brand = new Brand();
        BeanUtils.copyProperties(brandDTO, brand);
        this.getFirstWord(brand);
        if (shopId != null && shopId == 0) {
            brand.setStatus(BrandStatusEnum.APPROVED.getCode());
        }
        brand.setShopId(HeaderUtils.getShopId());
        this.save(brand);
        if (shopId != null && shopId > 0) {
            return "添加成功,等待管理员审核";
        } else {
            return "添加成功";
        }
    }

    private void getFirstWord(Brand brand) {
        if (StrUtil.isBlank(brand.getFirstWord())) {
            String firstWord = this.getFirstPinyin(brand.getBrandName());
            if (firstWord == null) {
                firstWord = "#";
            }
            // 更新品牌的首字母信息
            brand.setFirstWord(firstWord);
        }
    }

    /**
     * 检查品牌名称是否重复
     *
     * @param brandDTO 品牌参数
     */
    public void checkBrand(BrandDTO brandDTO, Integer shopId) {
        long count = this.count(new LambdaQueryWrapper<Brand>()
                .eq(Brand::getBrandName, brandDTO.getBrandName())
                .eq(Brand::getShopId, shopId)
                .ne(brandDTO.getBrandId() != null, Brand::getBrandId, brandDTO.getBrandId())
                .select(Brand::getBrandName));
        if (count > 0) {
            throw new GlobalException(BRAND_NAME_EXISTS, SERVICE_DATA_ERROR);
        }
    }

    @Override
    public String update(BrandDTO brandDTO) {
        Integer shopId = HeaderUtils.getShopId();
        checkBrand(brandDTO, shopId);
        Brand brand = new Brand();
        BeanUtils.copyProperties(brandDTO, brand);
        this.getFirstWord(brand);
        if (shopId != null && shopId > 0) {
            brand.setStatus(BrandStatusEnum.PENDING.getCode());
        }
        this.updateById(brand);

        if (shopId != null && shopId > 0) {
            return "修改成功,等待管理员审核";
        } else {
            return "修改成功";
        }
    }

    @Override
    public BrandSearchVO search() {
        // 查询所有品牌
        List<Brand> list = this.lambdaQuery()
                .eq(Brand::getStatus, BrandStatusEnum.APPROVED.getCode())
                .eq(Brand::getIsShow, 1)
                .list();
        List<BrandSearchDTO> resList = list.stream()
                .map(brand -> {
                    BrandSearchDTO dto = new BrandSearchDTO();
                    BeanUtils.copyProperties(brand, dto);
                    return dto;
                })
                .toList();
        // 构造查询条件
        LambdaQueryWrapper<Brand> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Brand::getFirstWord);
        queryWrapper.groupBy(Brand::getFirstWord);
        // 首字母列表
        List<String> firstWordList = this.listObjs(queryWrapper, Object::toString);
        return new BrandSearchVO(resList, firstWordList);
    }

    @Override
    public List<String> getBrandNameByIds(List<Integer> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) {
            return null;
        }
        return this.listObjs(new LambdaQueryWrapper<Brand>()
                .in(Brand::getBrandId, brandIds)
                .select(Brand::getBrandName), Object::toString);

    }

    @Transactional
    @Override
    public void updateFirstWord() {
        List<Brand> brands = this.list();
        for (Brand brand : brands) {
            // 获取品牌名称的拼音首字母
            String firstWord = this.getFirstPinyin(brand.getBrandName());
            if (firstWord == null) {
                firstWord = "#";
            }
            // 更新品牌的首字母信息
            brand.setFirstWord(firstWord);
        }
        this.updateBatchById(brands);
        adminLogService.createByLogInfo("品牌名称批量更新");
    }

    private String getFirstPinyin(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        char firstChar = str.charAt(0);
        if ((firstChar >= 'A' && firstChar <= 'Z') || (firstChar >= 'a' && firstChar <= 'z')) {
            return String.valueOf(Character.toUpperCase(firstChar));
        }
        if ((firstChar >= '0' && firstChar <= '9')) {
            return String.valueOf(firstChar);
        }
        String pinyin = PinyinUtil.getFirstLetter(str, "");
        if (StrUtil.isNotBlank(pinyin)) {
            firstChar = pinyin.charAt(0);
            return String.valueOf(Character.toUpperCase(firstChar));
        }
        return null;
    }

    @Override
    public Integer getBrandId(String brandName, Integer isAutoBrand) {
        int brandId = 0;

        if (StringUtils.isNotBlank(brandName)) {
            Brand brand = this.lambdaQuery().eq(Brand::getBrandName, brandName).one();
            if (brand != null) {
                brandId = brand.getBrandId();
            } else if (isAutoBrand == 1) {
                Integer shopId = HeaderUtils.getShopId();
                brand = Brand.builder()
                        .brandName(brandName)
                        .isShow(Constants.YES)
                        .shopId(shopId != null ? shopId : 0)
                        .status(shopId != null && shopId == 0
                                ? BrandStatusEnum.APPROVED.getCode()
                                : BrandStatusEnum.PENDING.getCode())
                        .build();
                this.getFirstWord(brand);
                this.save(brand);
                brandId = brand.getBrandId();
            }
        }

        return brandId;
    }

    @Override
    @Transactional
    public String audit(BrandAuditDTO dto) {
        Brand brand = this.getById(dto.getBrandId());
        if (!Arrays.asList(CheckStatus.APPROVED.getCode(), CheckStatus.REJECTED.getCode()).contains(dto.getStatus())) {
            throw new GlobalException("审核状态错误");
        }

        if (dto.getStatus() == CheckStatus.REJECTED.getCode() && StringUtils.isBlank(dto.getRejectRemark())) {
            throw new GlobalException("请填写拒绝理由");
        }

        adminLogService.createByLogInfo("审核品牌" + brand.getBrandName());

        this.lambdaUpdate()
                .set(Brand::getStatus, dto.getStatus())
                .set(StrUtil.isNotBlank(dto.getRejectRemark()), Brand::getRejectRemark, dto.getRejectRemark())
                .eq(Brand::getBrandId, dto.getBrandId())
                .update();

        return "审核成功";
    }

    @Override
    public Long auditWaitNum() {
        return this.lambdaQuery()
                .eq(Brand::getStatus, CheckStatus.PENDING.getCode())
                .count();
    }


    @Override
    public boolean updateField(UpdateFieldDTO updateField, String[] productPromotionFields) {
        Brand brand = this.getById(updateField.getId());
        Assert.notNull(brand, "品牌不存在");

        if ("brandName".equals(updateField.getField())) {
            Long count = this.lambdaQuery()
                    .eq(Brand::getBrandName, updateField.getVal())
                    .eq(Brand::getShopId, brand.getShopId())
                    .ne(Brand::getBrandId, brand.getBrandId())
                    .count();
            Assert.isTrue(count == 0, "该品牌名称已存在!");
        }

        if (brand.getShopId() > 0) {
            this.lambdaUpdate()
                    .eq(Brand::getBrandId, brand.getBrandId())
                    .set(Brand::getStatus, BrandStatusEnum.PENDING.getCode())
                    .update();
        }
        return super.updateField(updateField, productPromotionFields);
    }

}