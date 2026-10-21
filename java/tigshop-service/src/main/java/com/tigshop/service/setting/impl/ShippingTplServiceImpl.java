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

package com.tigshop.service.setting.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.ShippingTplInitDataDTO;
import com.tigshop.bean.dto.setting.ShippingTplListDTO;
import com.tigshop.bean.dto.setting.ShippingTplUpdateDTO;
import com.tigshop.bean.model.setting.ShippingTpl;
import com.tigshop.bean.model.setting.ShippingTplInfo;
import com.tigshop.bean.model.setting.ShippingType;
import com.tigshop.bean.model.shop.ShopFunds;
import com.tigshop.bean.param.settings.shippingtpl.ShippingTplSaveParam;
import com.tigshop.bean.param.settings.shippingtpl.ShippingTypeParam;
import com.tigshop.bean.vo.product.ProductShippingTplListVO;
import com.tigshop.bean.vo.setting.shippingtpl.ShippingTplVO;
import com.tigshop.bean.vo.setting.shippingtpl.ShippingTypeVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.setting.ShippingTplMapper;
import com.tigshop.mapper.shop.ShopFundsMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ShippingTplInfoService;
import com.tigshop.service.setting.ShippingTplService;
import com.tigshop.service.setting.ShippingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tigshop.common.constant.setting.ShippingTypeConstants.SHIPPING_TYPE_NOT_NULL;

/**
 * 运费模板服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class ShippingTplServiceImpl extends BaseServiceImpl<ShippingTplMapper, ShippingTpl> implements ShippingTplService {

    private final ShippingTplInfoService shippingTplInfoService;

    private final AdminLogService adminLogService;

    private final ShippingTypeService shippingTypeService;

    private final ShopFundsMapper shopFundsMapper;

    @Override
    public Page<ShippingTplVO> list(ShippingTplListDTO listDTO) {
        // 分页
        Page<ShippingTpl> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ShippingTpl> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer shopId = getShopId();
        if (shopId != null) {
            queryWrapper.eq(ShippingTpl::getShopId, shopId);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(ShippingTpl::getShippingTplName, keyword);
        }
        // 执行查询
        Page<ShippingTpl> shippingTplPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ShippingTpl> shippingTplPageRecords = shippingTplPage.getRecords();
        // 转换为VO
        List<ShippingTplVO> shippingTplVOList = shippingTplPageRecords.stream()
                .map(shippingTpl -> {
                    ShippingTplVO shippingTplVO = new ShippingTplVO();
                    BeanUtils.copyProperties(shippingTpl, shippingTplVO);
                    return shippingTplVO;
                }).toList();
        return PageUtil.convertPage(shippingTplPage, shippingTplVOList);
    }

    @Override
    public ShippingTplVO detail(Integer id) {
        ShippingTpl shippingTpl = this.getById(id);
        Assert.notNull(shippingTpl, () -> new GlobalException("该运费模板不存在"));

        // 获取运费模版内容
        List<ShippingTplInfo> shippingTplInfos = shippingTplInfoService.lambdaQuery()
                .eq(ShippingTplInfo::getShippingTplId, id)
                .list();

        ShippingTypeVO shippingType = new ShippingTypeVO(new ShippingType(), shippingTplInfos);

        return new ShippingTplVO(shippingTpl, Collections.singletonList(shippingType));
    }

    @Transactional
    @Override
    public void create(ShippingTplSaveParam param) {
        Integer shopId = HeaderUtils.getShopId();

        // 校验同店铺下模板名称不能重复
        long nameCount = this.lambdaQuery()
                .eq(ShippingTpl::getShopId, shopId)
                .eq(ShippingTpl::getShippingTplName, param.getShippingTplName())
                .count();
        Assert.isTrue(nameCount == 0, () -> new GlobalException("运费模板名称已存在"));

        // 检测是否有运送方式
        List<ShippingTypeParam> checkedShippingTypes = param.getShippingTplInfo().stream().filter(shippingType -> Constants.YES.equals(shippingType.getIsChecked())).toList();
        Assert.isTrue(CollUtil.isNotEmpty(checkedShippingTypes), () -> new GlobalException(SHIPPING_TYPE_NOT_NULL));

        // 如果设置为默认运费模板，则取消其他默认运费模板
        if (Constants.YES.equals(param.getIsDefault())) {
            this.lambdaUpdate()
                    .eq(ShippingTpl::getShopId, shopId)
                    .set(ShippingTpl::getIsDefault, 0)
                    .update();
        }

        // 保存运费模板
        ShippingTpl shippingTpl = param.createShippingTpl(shopId);
        this.save(shippingTpl);

        // 保存运费模板信息
        shippingTplInfoService.saveShippingTplInfo(checkedShippingTypes, shippingTpl);

        // 保存日志信息
        adminLogService.createByLogInfo("创建运费模板:" + shippingTpl.getShippingTplName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ShippingTplUpdateDTO updateDTO) {
        Integer shopId = getShopId();

        // 校验同店铺下模板名称不能重复（排除自身）
        if (StrUtil.isNotBlank(updateDTO.getShippingTplName())) {
            long nameCount = this.lambdaQuery()
                    .eq(ShippingTpl::getShopId, shopId)
                    .eq(ShippingTpl::getShippingTplName, updateDTO.getShippingTplName())
                    .ne(ShippingTpl::getShippingTplId, updateDTO.getId())
                    .count();
            Assert.isTrue(nameCount == 0, () -> new GlobalException("运费模板名称已存在"));
        }

        List<ShippingTypeParam> shippingTplInfo = updateDTO.getShippingTplInfo();
        AtomicBoolean hasShippingType = new AtomicBoolean(false);
        shippingTplInfo.forEach(shippingTplInfo1 -> {
            //检测里面is_checked是否大于1，没有就抛异常 请选择运送方式
            if (shippingTplInfo1.getIsChecked() > 0) {
                hasShippingType.set(true);
            }
        });
        if (!hasShippingType.get()) {
            throw new GlobalException(SHIPPING_TYPE_NOT_NULL);
        }

        // 如果设置为默认运费模板，则取消同店铺其他默认运费模板（与 create/setDefault 保持一致）
        if (Constants.YES.equals(updateDTO.getIsDefault())) {
            this.lambdaUpdate()
                    .eq(ShippingTpl::getShopId, shopId)
                    .set(ShippingTpl::getIsDefault, 0)
                    .update();
        }
        ShippingTpl shippingTpl = new ShippingTpl();
        BeanUtils.copyProperties(updateDTO, shippingTpl);
        shippingTpl.setShippingTplId(updateDTO.getId());
        this.updateById(shippingTpl);
        // 保存运费模板信息
        shippingTplInfoService.updateShippingTplInfo(shippingTplInfo, shippingTpl);
        adminLogService.createByLogInfo("修改运费模板:" + updateDTO.getShippingTplName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initShippingTpl(Integer shopId) {
        //运费模板
        ShippingTpl shippingTpl = new ShippingTpl();
        shippingTpl.setShippingTime("1天内");
        shippingTpl.setShippingTplName("默认模板");
        shippingTpl.setIsFree(0);
        shippingTpl.setPricingType(1);
        shippingTpl.setIsDefault(1);
        shippingTpl.setShopId(shopId);
        this.save(shippingTpl);
        //运费模板内容
        ShippingTplInfo shippingTplInfo = new ShippingTplInfo();
        shippingTplInfo.setShippingTypeId(1L);
        shippingTplInfo.setIsFree(0);
        shippingTplInfo.setIsDefault(1);
        ShippingTplInitDataDTO regionDataDTO = new ShippingTplInitDataDTO();
        List<Integer> areaRegions = new ArrayList<>();
        List<String> areaRegionNames = new ArrayList<>();
        regionDataDTO.setAreaRegions(areaRegions);
        regionDataDTO.setAreaRegionNames(areaRegionNames);
        shippingTplInfo.setRegionData(JsonUtil.toJson(regionDataDTO));
        shippingTplInfo.setStartNumber(new BigDecimal(1));
        shippingTplInfo.setStartPrice(new BigDecimal(1));
        shippingTplInfo.setAddNumber(new BigDecimal(1));
        shippingTplInfo.setAddPrice(new BigDecimal(1));
        shippingTplInfo.setFreePrice(new BigDecimal(0));
        shippingTplInfo.setPricingType(1);
        shippingTplInfo.setShippingTplId(shippingTpl.getShippingTplId());
        shippingTplInfoService.save(shippingTplInfo);
        //adminLogService.createByLogInfo("初始化运费模板:默认模板");
    }

    @Override
    public List<ShippingTypeVO> getShippingTypeListByShopId() {
        Integer shopId = getShopId();
        return shippingTypeService.getShippingTypeListByShopId(shopId);
    }

    @Override
    public List<ProductShippingTplListVO> getShippingTplListByShopId() {
        Integer shopId = getShopId();
        LambdaQueryWrapper<ShippingTpl> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShippingTpl::getShopId, shopId);
        List<ShippingTpl> shippingTplList = this.list(queryWrapper);
        return shippingTplList.stream().map(shippingTpl -> {
            ProductShippingTplListVO productShippingTplListVO = new ProductShippingTplListVO();
            BeanUtils.copyProperties(shippingTpl, productShippingTplListVO);
            productShippingTplListVO.setIsDefault(shippingTpl.getIsDefault());
            productShippingTplListVO.setShippingTplId(shippingTpl.getShippingTplId());
            productShippingTplListVO.setShippingTplName(shippingTpl.getShippingTplName());
            productShippingTplListVO.setShopId(shippingTpl.getShopId());
            ShopFunds shopFunds = shopFundsMapper.selectOne(
                    Wrappers.lambdaQuery(ShopFunds.class).eq(ShopFunds::getShopId, shopId)
            );
            productShippingTplListVO.setShop(shopFunds);
            return productShippingTplListVO;
        }).toList();
    }

    @Override
    public Long getDefaultShippingTplId(Integer shopId) {
        ShippingTpl shippingTpl = this.getOne(new LambdaQueryWrapper<ShippingTpl>().eq(ShippingTpl::getShopId, shopId).eq(ShippingTpl::getIsDefault, 1).last("limit 1"));

        if (shippingTpl != null) {
            return shippingTpl.getShippingTplId();
        }
        return 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        Integer shopId = getShopId();
        ShippingTpl shippingTpl = this.getById(id);
        Assert.notNull(shippingTpl, () -> new GlobalException("该运费模板不存在"));
        Assert.isTrue(shopId.equals(shippingTpl.getShopId()), () -> new GlobalException("无权操作该运费模板"));

        // 先取消当前店铺所有默认运费模板
        this.lambdaUpdate()
                .eq(ShippingTpl::getShopId, shopId)
                .set(ShippingTpl::getIsDefault, 0)
                .update();

        // 设置指定模板为默认
        this.lambdaUpdate()
                .eq(ShippingTpl::getShippingTplId, id)
                .set(ShippingTpl::getIsDefault, 1)
                .update();

        adminLogService.createByLogInfo("设置默认运费模板:" + shippingTpl.getShippingTplName());
    }
}
