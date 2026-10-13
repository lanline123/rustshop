package com.tigshop.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.PickupContactCreateDTO;
import com.tigshop.bean.dto.user.PickupContactListDTO;
import com.tigshop.bean.dto.user.PickupContactUpdateDTO;
import com.tigshop.bean.model.user.PickupContact;
import com.tigshop.bean.vo.user.PickupContactVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.user.PickupContactMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.PickupContactService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自提联系人服务实现类
 *
 * @author Tigshop
 */
@Service
public class PickupContactServiceImpl extends BaseServiceImpl<PickupContactMapper, PickupContact> implements PickupContactService {

    @Override
    public Page<PickupContactVO> list(PickupContactListDTO listDTO) {
        Page<PickupContact> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        LambdaQueryWrapper<PickupContact> queryWrapper = new LambdaQueryWrapper<>();

        if (listDTO.getUserId() != null) {
            queryWrapper.eq(PickupContact::getUserId, listDTO.getUserId());
        }
        if (listDTO.getKeyword() != null && !listDTO.getKeyword().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(PickupContact::getContactName, listDTO.getKeyword())
                    .or()
                    .like(PickupContact::getContactPhone, listDTO.getKeyword()));
        }

        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        Page<PickupContact> result = this.page(page, queryWrapper);

        List<PickupContactVO> voList = result.getRecords().stream()
                .map(entity -> {
                    PickupContactVO vo = new PickupContactVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                }).toList();

        return PageUtil.convertPage(result, voList);
    }

    @Override
    public List<PickupContactVO> listByUserId(Integer userId) {
        LambdaQueryWrapper<PickupContact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PickupContact::getUserId, userId);
        queryWrapper.orderByDesc(PickupContact::getId);
        List<PickupContact> list = this.list(queryWrapper);

        return list.stream().map(entity -> {
            PickupContactVO vo = new PickupContactVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).toList();
    }

    @Override
    public PickupContactVO detail(Long id) {
        PickupContact entity = this.getById(id);
        if (entity == null) {
            throw new GlobalException("自提联系人不存在");
        }
        PickupContactVO vo = new PickupContactVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Long create(PickupContactCreateDTO createDTO) {
        PickupContact entity = PickupContact.builder()
                .userId(createDTO.getUserId())
                .contactName(createDTO.getContactName())
                .contactPhone(createDTO.getContactPhone())
                .build();
        this.save(entity);
        return entity.getId();
    }

    @Override
    public boolean update(PickupContactUpdateDTO updateDTO) {
        PickupContact entity = this.getById(updateDTO.getId());
        if (entity == null) {
            throw new GlobalException("自提联系人不存在");
        }
        entity.setContactName(updateDTO.getContactName());
        entity.setContactPhone(updateDTO.getContactPhone());
        if (updateDTO.getUserId() != null) {
            entity.setUserId(updateDTO.getUserId());
        }
        return this.updateById(entity);
    }

    @Override
    public boolean del(Long id) {
        if (id == null) {
            return false;
        }
        return this.removeById(id);
    }
}
