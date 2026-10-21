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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.SignInSettingCreateDTO;
import com.tigshop.bean.dto.promotion.SignInSettingListDTO;
import com.tigshop.bean.dto.promotion.SignInSettingUpdateDTO;
import com.tigshop.bean.model.promotion.SignInSetting;
import com.tigshop.bean.vo.promotion.SignInSettingVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.promotion.SignInSettingMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.SignInSettingService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tigshop.common.constant.promotion.SignInSettingConstants.SIGN_IN_SETTING_NOT_EXISTS;

/**
 * 积分签到服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class SignInSettingServiceImpl extends BaseServiceImpl<SignInSettingMapper, SignInSetting> implements SignInSettingService {
    @Override
    public Page<SignInSettingVO> list(SignInSettingListDTO listDTO) {
        // 分页
        Page<SignInSetting> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<SignInSetting> queryWrapper = new LambdaQueryWrapper<>();

        // 默认按排序值升序；明确传入 sortField=sortOrder&sortOrder=desc 时支持降序。
        boolean sortOrderDesc = "sortOrder".equals(listDTO.getSortField())
                && "desc".equalsIgnoreCase(listDTO.getSortOrder());
        if (sortOrderDesc) {
            queryWrapper.orderByDesc(SignInSetting::getSortOrder);
        } else {
            queryWrapper.orderByAsc(SignInSetting::getSortOrder);
        }
        // 排序值相同时按添加时间升序，最后按 id 升序保证结果稳定。
        queryWrapper
                .orderByAsc(SignInSetting::getCreateTime)
                .orderByAsc(SignInSetting::getId);

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(SignInSetting::getName, keyword);
        }
        // 执行查询
        Page<SignInSetting> signInSettingPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<SignInSetting> signInSettingPageRecords = signInSettingPage.getRecords();
        // 转换为VO
        List<SignInSettingVO> signInSettingVOList = signInSettingPageRecords.stream()
                .map(signInSetting -> {
                    SignInSettingVO signInSettingVO = new SignInSettingVO();
                    BeanUtils.copyProperties(signInSetting, signInSettingVO);
                    return signInSettingVO;
                }).toList();
        return PageUtil.convertPage(signInSettingPage, signInSettingVOList);
    }

    @Override
    public SignInSettingVO detail(Integer id) {
        if (id != null) {
            SignInSetting signInSetting = this.getById(id);
            SignInSettingVO signInSettingVO = new SignInSettingVO();
            BeanUtils.copyProperties(signInSetting, signInSettingVO);
            return signInSettingVO;
        }
        return null;
    }

    @Override
    public boolean create(SignInSettingCreateDTO createDTO) {
        if (createDTO == null) {
            return false;
        }
        SignInSetting latest = this.lambdaQuery()
                .select(SignInSetting::getDayNum)
                .orderByDesc(SignInSetting::getDayNum)
                .last("limit 1")
                .one();
        int nextDayNum = latest == null || latest.getDayNum() == null ? 1 : latest.getDayNum() + 1;

        SignInSetting signInSetting = new SignInSetting();
        BeanUtils.copyProperties(createDTO, signInSetting);
        signInSetting.setDayNum(nextDayNum);
        signInSetting.setSortOrder(createDTO.getSortOrder() == null ? 0 : createDTO.getSortOrder());
        signInSetting.setCreateTime(StringUtils.getCurrentTime());
        return this.save(signInSetting);
    }

    public Long getCount(Integer id) {
        return this.count(new LambdaQueryWrapper<SignInSetting>()
                .eq(SignInSetting::getId, id)
                .select(SignInSetting::getId));
    }

    @Override
    public boolean update(SignInSettingUpdateDTO updateDTO) {
        if (updateDTO != null) {
            SignInSetting signInSetting = new SignInSetting();
            Long count = getCount(updateDTO.getId());
            if (count == 0) {
                throw new GlobalException(SIGN_IN_SETTING_NOT_EXISTS);
            }
            signInSetting.setId(updateDTO.getId());
            signInSetting.setName(updateDTO.getName());
            signInSetting.setPoints(updateDTO.getPoints());
            if (updateDTO.getSortOrder() != null) {
                signInSetting.setSortOrder(updateDTO.getSortOrder());
            }
            return this.updateById(signInSetting);
        }
        return false;
    }
}
