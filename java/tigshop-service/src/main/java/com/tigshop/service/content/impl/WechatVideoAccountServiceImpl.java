package com.tigshop.service.content.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.content.WechatVideoAccountCreateDTO;
import com.tigshop.bean.dto.content.WechatVideoAccountListDTO;
import com.tigshop.bean.dto.content.WechatVideoAccountUpdateDTO;
import com.tigshop.bean.model.content.WechatVideoAccount;
import com.tigshop.bean.vo.content.WechatVideoAccountVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.content.WechatVideoAccountMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.content.WechatVideoAccountService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tigshop.common.constant.content.WechatVideoAccountConstants.*;

/**
 * 视频号账号服务实现
 */
@Service
public class WechatVideoAccountServiceImpl extends BaseServiceImpl<WechatVideoAccountMapper, WechatVideoAccount>
        implements WechatVideoAccountService {

    @Override
    public Page<WechatVideoAccountVO> list(WechatVideoAccountListDTO listDTO) {
        LambdaQueryWrapper<WechatVideoAccount> queryWrapper = buildQueryWrapper(listDTO);
        Page<WechatVideoAccount> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        Page<WechatVideoAccount> accountPage = this.page(page, queryWrapper);
        List<WechatVideoAccountVO> voList = accountPage.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        return PageUtil.convertPage(accountPage, voList);
    }

    @Override
    public WechatVideoAccountVO detail(Integer id) {
        WechatVideoAccount account = this.getById(id);
        if (account == null) {
            throw new GlobalException(VIDEO_ACCOUNT_NOT_EXISTS);
        }
        return convertToVO(account);
    }

    @Override
    public boolean create(WechatVideoAccountCreateDTO createDTO) {
        checkFinderIdUnique(null, createDTO.getFinderId());
        WechatVideoAccount account = new WechatVideoAccount();
        BeanUtils.copyProperties(createDTO, account);
        Long now = StringUtils.getCurrentTime();
        account.setAddTime(now);
        account.setUpdateTime(now);
        account.setShopId(getShopId());
        return this.save(account);
    }

    @Override
    public boolean update(WechatVideoAccountUpdateDTO updateDTO) {
        WechatVideoAccount existed = this.getById(updateDTO.getId());
        if (existed == null) {
            throw new GlobalException(VIDEO_ACCOUNT_NOT_EXISTS);
        }
        checkFinderIdUnique(updateDTO.getId(), updateDTO.getFinderId());
        existed.setFinderId(updateDTO.getFinderId());
        existed.setUpdateTime(StringUtils.getCurrentTime());
        return this.updateById(existed);
    }

    @Override
    public boolean delete(Integer id) {
        WechatVideoAccount existed = this.getById(id);
        if (existed == null) {
            throw new GlobalException(VIDEO_ACCOUNT_NOT_EXISTS);
        }
        return this.removeById(id);
    }

    private WechatVideoAccountVO convertToVO(WechatVideoAccount account) {
        WechatVideoAccountVO vo = new WechatVideoAccountVO();
        BeanUtils.copyProperties(account, vo);
        vo.setRelationTime(formatTimestamp(account.getAddTime()));
        return vo;
    }

    private LambdaQueryWrapper<WechatVideoAccount> buildQueryWrapper(WechatVideoAccountListDTO listDTO) {
        LambdaQueryWrapper<WechatVideoAccount> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(listDTO.getFinderId())) {
            queryWrapper.like(WechatVideoAccount::getFinderId, listDTO.getFinderId());
        }

        if (StrUtil.isNotBlank(listDTO.getRelationTimeStart())) {
            queryWrapper.ge(WechatVideoAccount::getAddTime, StringUtils.dateToTimestampExample(listDTO.getRelationTimeStart()));
        }

        if (StrUtil.isNotBlank(listDTO.getRelationTimeEnd())) {
            long endTime = parseRelationEndTime(listDTO.getRelationTimeEnd());
            queryWrapper.le(WechatVideoAccount::getAddTime, endTime);
        }

        Integer shopId = getShopId();
        if (shopId != null && shopId > 0) {
            queryWrapper.eq(WechatVideoAccount::getShopId, shopId);
        }

        queryWrapper.orderByDesc(WechatVideoAccount::getId);
        return queryWrapper;
    }

    private long parseRelationEndTime(String dateTime) {
        String value = dateTime == null ? "" : dateTime.trim();
        if (value.matches("\\d{4}-\\d{2}-\\d{2}$")) {
            return StringUtils.getEndOfDayTimestamp(value + " 00:00:00");
        }
        return StringUtils.dateToTimestampExample(value);
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null || timestamp <= 0) {
            return "";
        }
        DateTime dateTime = DateUtil.date(timestamp * 1000);
        return DateUtil.format(dateTime, "yyyy-MM-dd HH:mm:ss");
    }

    private void checkFinderIdUnique(Integer currentId, String finderId) {
        LambdaQueryWrapper<WechatVideoAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WechatVideoAccount::getFinderId, finderId);
        Integer shopId = getShopId();
        if (shopId != null && shopId > 0) {
            queryWrapper.eq(WechatVideoAccount::getShopId, shopId);
        }
        if (currentId != null) {
            queryWrapper.ne(WechatVideoAccount::getId, currentId);
        }
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new GlobalException(VIDEO_ACCOUNT_FINDER_ID_EXISTS);
        }
    }
}
