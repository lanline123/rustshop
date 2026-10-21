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
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.RegionDataDTO;
import com.tigshop.bean.dto.setting.ShippingTplInfoCreateDTO;
import com.tigshop.bean.dto.setting.ShippingTplInfoListDTO;
import com.tigshop.bean.dto.setting.ShippingTplInfoUpdateDTO;
import com.tigshop.bean.model.setting.ShippingTpl;
import com.tigshop.bean.model.setting.ShippingTplInfo;
import com.tigshop.bean.model.user.UserAddress;
import com.tigshop.bean.param.settings.shippingtpl.ShippingTypeParam;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.setting.ShippingTplInfoVO;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.mapper.setting.ShippingTplInfoMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.ShippingTplInfoService;
import com.tigshop.service.user.UserAddressService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 运费模板内容服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ShippingTplInfoServiceImpl extends BaseServiceImpl<ShippingTplInfoMapper, ShippingTplInfo> implements ShippingTplInfoService {

    @Resource
    private ConfigService configService;

    @Resource
    private UserAddressService userAddressService;

    @Override
    public ListResVO<ShippingTplInfoVO, ShippingTplInfoListDTO> list(ShippingTplInfoListDTO listDTO) {
        // 分页
        Page<ShippingTplInfo> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        QueryWrapper<ShippingTplInfo> queryWrapper = new QueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
//        Integer isShow = listDTO.getIsShow();
//        if (IsShowType.fromTypeCode(isShow) != null) {
//            queryWrapper.eq("is_show", isShow);
//        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like("", keyword);
        }
        // 执行查询
        Page<ShippingTplInfo> shippingTplInfoPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ShippingTplInfo> shippingTplInfoPageRecords = shippingTplInfoPage.getRecords();
        // 转换为VO
        List<ShippingTplInfoVO> shippingTplInfoVOList = shippingTplInfoPageRecords.stream()
                .map(shippingTplInfo -> {
                    ShippingTplInfoVO shippingTplInfoVO = new ShippingTplInfoVO();
                    BeanUtils.copyProperties(shippingTplInfo, shippingTplInfoVO);
                    return shippingTplInfoVO;
                }).toList();
        return new ListResVO<>(shippingTplInfoVOList, listDTO, shippingTplInfoPage.getTotal());
    }

    @Override
    public ShippingTplInfoVO detail(Integer id) {
        if (id != null) {
            ShippingTplInfo shippingTplInfo = this.getById(id);
            ShippingTplInfoVO shippingTplInfoVO = new ShippingTplInfoVO();
            BeanUtils.copyProperties(shippingTplInfo, shippingTplInfoVO);
            return shippingTplInfoVO;
        }
        return new ShippingTplInfoVO();
    }

    @Override
    public boolean create(ShippingTplInfoCreateDTO createDTO) {
        if (createDTO != null) {
            ShippingTplInfo shippingTplInfo = new ShippingTplInfo();
            BeanUtils.copyProperties(createDTO, shippingTplInfo);
            return this.save(shippingTplInfo);
        }
        return false;
    }

    @Override
    public boolean update(ShippingTplInfoUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ShippingTplInfo shippingTplInfo = new ShippingTplInfo();
            BeanUtils.copyProperties(updateDTO, shippingTplInfo);
            return this.updateById(shippingTplInfo);
        }
        return false;
    }

    @Transactional
    @Override
    public void saveShippingTplInfo(List<ShippingTypeParam> shippingTypeParams, ShippingTpl shippingTpl) {
        List<ShippingTplInfo> shippingTplInfos = shippingTypeParams.stream()
                .flatMap(shippingType -> {
                    List<ShippingTplInfo> currShippingTplInfos = new ArrayList<>();

                    // 处理默认模版信息
                    ShippingTplInfo defaultShippingTplInfo = shippingType.createDefaultShippingTplInfo(shippingTpl);

                    // 处理其他模版信息
                    List<ShippingTplInfo> areaShippingTplInfos = shippingType.createAreaShippingTplInfos(shippingTpl);

                    currShippingTplInfos.add(defaultShippingTplInfo);
                    currShippingTplInfos.addAll(areaShippingTplInfos);

                    return currShippingTplInfos.stream();
                })
                .toList();

        this.saveBatch(shippingTplInfos);
    }

    @Transactional
    @Override
    public void updateShippingTplInfo(List<ShippingTypeParam> shippingTypeParams, ShippingTpl shippingTpl) {
        this.lambdaUpdate().eq(ShippingTplInfo::getShippingTplId, shippingTpl.getShippingTplId()).remove();
        this.saveShippingTplInfo(shippingTypeParams, shippingTpl);
    }

    @Override
    public List<ShippingTplInfo> getShippingTplInfoByShippingTplId(Integer shippingTplId) {
        return this.lambdaQuery()
                .eq(ShippingTplInfo::getShippingTplId, shippingTplId)
                .list();
    }

    @Override
    public boolean verifyUserAddress(Integer shippingTplId, Integer addressId) {
        List<ShippingTplInfo> shippingTplInfos = this.lambdaQuery()
                .eq(ShippingTplInfo::getShippingTplId, shippingTplId)
                .eq(ShippingTplInfo::getIsDefault, 0)
                .list();
        if (shippingTplInfos != null && !shippingTplInfos.isEmpty()) {
            for (ShippingTplInfo shippingTplInfo : shippingTplInfos) {
                RegionDataDTO regionDataDTO = JSONUtil.toBean(shippingTplInfo.getRegionData(), RegionDataDTO.class);
                if (CollUtil.isNotEmpty(regionDataDTO.getAreaRegions())) {
                    UserAddress userAddress = userAddressService.getById(addressId);
                    List<Integer> regionIds = JsonUtil.jsonToList(userAddress.getRegionIds(), Integer.class);
                    boolean addressAllowed = isAddressAllowed(regionDataDTO.getAreaRegions(), regionIds);
                    if (addressAllowed) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断用户地址是否允许配送
     *
     * @param allowedList 允许配送的编码规则，例如：[[1,360000], [1,360000,360100]]
     * @param userAddress 用户地址，例如：[360000, 360100, 360192]
     * @return true 表示允许配送
     */
    public boolean isAddressAllowed(List<List<Integer>> allowedList, List<Integer> userAddress) {
        Integer one = userAddress.get(0);
        Integer two = userAddress.get(1);
        Integer three = userAddress.get(2);
        for (List<Integer> allowed : allowedList) {
            if (allowed.contains(one)) {
                if (allowed.size() == 2) {
                    return true;
                }
                if (allowed.contains(two)) {
                    if (allowed.size() == 3) {
                        return true;
                    }
                    if (allowed.contains(three)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private BigDecimal handleData(String data) {
        if (StrUtil.isNotEmpty(data)) {
            return new BigDecimal(data);
        } else {
            return new BigDecimal(0);
        }
    }
}

