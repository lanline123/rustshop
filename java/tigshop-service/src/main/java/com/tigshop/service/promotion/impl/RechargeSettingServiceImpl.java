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

package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.RechargeSettingCreateDTO;
import com.tigshop.bean.dto.promotion.RechargeSettingListDTO;
import com.tigshop.bean.dto.promotion.RechargeSettingUpdateDTO;
import com.tigshop.bean.enums.common.IsShowType;
import com.tigshop.bean.model.promotion.RechargeSetting;
import com.tigshop.bean.query.promotion.recharge.RechargeSettingPageQuery;
import com.tigshop.bean.vo.promotion.RechargeSettingVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.promotion.RechargeSettingMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.RechargeSettingService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充值余额服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class RechargeSettingServiceImpl extends BaseServiceImpl<RechargeSettingMapper, RechargeSetting> implements RechargeSettingService {
    @Override
    public Page<RechargeSettingVO> list(RechargeSettingListDTO listDTO) {
        // 分页
        Page<RechargeSetting> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<RechargeSetting> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer isShow = listDTO.getIsShow();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq(RechargeSetting::getIsShow, isShow);
        }
        // 执行查询
        Page<RechargeSetting> rechargeSettingPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<RechargeSetting> rechargeSettingPageRecords = rechargeSettingPage.getRecords();
        // 转换为VO
        List<RechargeSettingVO> rechargeSettingVOList = rechargeSettingPageRecords.stream()
                .map(rechargeSetting -> {
                    RechargeSettingVO rechargeSettingVO = new RechargeSettingVO();
                    BeanUtils.copyProperties(rechargeSetting, rechargeSettingVO);
                    return rechargeSettingVO;
                }).toList();
        return PageUtil.convertPage(rechargeSettingPage, rechargeSettingVOList);
    }

    @Override
    public RechargeSettingVO detail(Integer id) {
        if (id != null) {
            RechargeSetting rechargeSetting = this.getById(id);
            RechargeSettingVO rechargeSettingVO = new RechargeSettingVO();
            BeanUtils.copyProperties(rechargeSetting, rechargeSettingVO);
            return rechargeSettingVO;
        }
        return null;
    }

    @Override
    public boolean create(RechargeSettingCreateDTO createDTO) {
        if (createDTO != null) {
            if(createDTO.getDiscountMoney().compareTo(BigDecimal.ZERO) < 0
                    ||
                    createDTO.getMoney().compareTo(BigDecimal.ZERO) < 0
            ) {
                throw new GlobalException("充值金额或赠送金额不能小于0");
            }
            RechargeSetting rechargeSetting = new RechargeSetting();
            BeanUtils.copyProperties(createDTO, rechargeSetting);
            return this.save(rechargeSetting);
        }
        return false;
    }

    @Override
    public boolean update(RechargeSettingUpdateDTO updateDTO) {
        if (updateDTO != null) {
            RechargeSetting rechargeSetting = new RechargeSetting();
            BeanUtils.copyProperties(updateDTO, rechargeSetting);
            return this.updateById(rechargeSetting);
        }
        return false;
    }

    @Override
    public Page<RechargeSettingVO> settingPage(RechargeSettingPageQuery pageQuery) {
        Page<RechargeSetting> page = buildSortOrder(pageQuery);

        page = this.lambdaQuery().eq(RechargeSetting::getIsShow, 1).page(page);

        List<RechargeSetting> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<RechargeSettingVO> resultRecords = records.stream()
                .map(rechargeSetting -> {
                    RechargeSettingVO rechargeSettingVO = new RechargeSettingVO();
                    BeanUtils.copyProperties(rechargeSetting, rechargeSettingVO);
                    return rechargeSettingVO;
                }).toList();

        return PageUtil.convertPage(page, resultRecords);
    }
}
