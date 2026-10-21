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

package com.tigshop.service.im.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.im.ImServantCreateDTO;
import com.tigshop.bean.dto.im.ImServantListDTO;
import com.tigshop.bean.dto.im.ImServantUpdateDTO;
import com.tigshop.bean.dto.im.ServantModifyStatusDTO;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.im.ImServant;
import com.tigshop.bean.vo.im.ImServantVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.im.ImServantMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.im.ImServantService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客服服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ImServantServiceImpl extends BaseServiceImpl<ImServantMapper, ImServant> implements ImServantService {
    @Resource
    private AdminUserMapper adminUserMapper;
    @Override
    public Page<ImServantVO> list(ImServantListDTO listDTO) {
        // 分页
        Page<ImServant> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ImServant> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        if(listDTO.getShopId() != null){
            queryWrapper.eq(ImServant::getShopId, listDTO.getShopId());
        }

        // 按用户名过滤：username 存在于 admin_user 表，先查出匹配的客服id再过滤
        if(StringUtils.isNotEmpty(listDTO.getUsername())){
            LambdaQueryWrapper<AdminUser> adminUserQueryWrapper = new LambdaQueryWrapper<>();
            adminUserQueryWrapper.select(AdminUser::getAdminId)
                    .like(AdminUser::getUsername, listDTO.getUsername());
            List<Integer> matchedServantIds = adminUserMapper.selectList(adminUserQueryWrapper).stream()
                    .map(AdminUser::getAdminId)
                    .toList();
            if(matchedServantIds.isEmpty()){
                return new Page<>();
            }
            queryWrapper.in(ImServant::getServantId, matchedServantIds);
        }

        // 执行查询
        Page<ImServant> imServantPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ImServant> imServantPageRecords = imServantPage.getRecords();
        if(imServantPageRecords.isEmpty()) {
            return new Page<>();
        }
        //取出servantId
        List<Integer> servantIds = imServantPageRecords.stream()
                .map(ImServant::getServantId)
                .distinct()
                .toList();
        LambdaQueryWrapper<AdminUser> servantQueryWrapper = new LambdaQueryWrapper<>();
        servantQueryWrapper.in(AdminUser::getAdminId, servantIds);
        List<AdminUser> adminUsers = adminUserMapper.selectList(servantQueryWrapper);
        Map<Integer, AdminUser> servantMap = adminUsers.stream()
                .collect(Collectors.toMap(AdminUser::getAdminId, Function.identity()));
        // 转换为VO
        List<ImServantVO> imServantVOList = imServantPageRecords.stream()
                .map(imServant -> {
                    ImServantVO imServantVO = new ImServantVO();
                    BeanUtils.copyProperties(imServant, imServantVO);
                    imServantVO.setLastUpdateTime(TigUtils.handelTime(imServant.getLastUpdateTime()));
                    imServantVO.setAddTime(TigUtils.handelTime(imServant.getAddTime()));
                    ImServantVO.UserVO userVO = new ImServantVO.UserVO();
                    if(servantMap.get(imServant.getServantId()) != null){
                        BeanUtils.copyProperties(servantMap.get(imServant.getServantId()), userVO);
                    } else {
                        userVO = null;
                    }

                    imServantVO.setUser(userVO);
                    return imServantVO;
                }).toList();
        return PageUtil.convertPage(imServantPage, imServantVOList);
    }

    @Override
    public ImServantVO detail(Integer id) {
        if (id != null) {
            ImServant imServant = this.getById(id);
            ImServantVO imServantVO = new ImServantVO();
            BeanUtils.copyProperties(imServant, imServantVO);
            return imServantVO;
        }
        return null;
    }

    @Override
    public boolean create(ImServantCreateDTO createDTO) {
        if (createDTO != null) {
            ImServant imServant = new ImServant();
            BeanUtils.copyProperties(createDTO, imServant);
            return this.save(imServant);
        }
        return false;
    }

    @Override
    public boolean update(ImServantUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ImServant imServant = new ImServant();
            BeanUtils.copyProperties(updateDTO, imServant);
            return this.updateById(imServant);
        }
        return false;
    }

    @Override
    public boolean modifyStatus(ServantModifyStatusDTO dto) {
        LambdaQueryWrapper<ImServant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImServant::getServantId, dto.getServantId());
        queryWrapper.eq(ImServant::getShopId, dto.getShopId());
        ImServant imServant = this.getOne(queryWrapper);
        if(imServant == null) {
            ImServant imServantUp = new ImServant();
            imServantUp.setServantId(dto.getServantId());
            imServantUp.setShopId(dto.getShopId());
            imServantUp.setAddTime(StringUtils.getCurrentTime());
            imServantUp.setStatus(dto.getStatus());
            imServantUp.setLastUpdateTime(StringUtils.getCurrentTime());
            return this.save(imServantUp);
        } else {
            imServant.setStatus(dto.getStatus());
            imServant.setLastUpdateTime(StringUtils.getCurrentTime());
            imServant.setShopId(dto.getShopId());
            return this.updateById(imServant);
        }
    }

    @Override
    public ImServantVO getStatus(Integer servantId, Integer shopId) {
        LambdaQueryWrapper<ImServant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImServant::getServantId, servantId);
        if (shopId != null) {
            queryWrapper.eq(ImServant::getShopId, shopId);
        }
        ImServant imServant = this.getOne(queryWrapper);
        if (imServant == null) {
            imServant = new ImServant();
            imServant.setServantId(servantId);
            imServant.setShopId(shopId);
            imServant.setAddTime(StringUtils.getCurrentTime());
            //默认离线
            imServant.setStatus(3);
            imServant.setLastUpdateTime(StringUtils.getCurrentTime());
            this.save(imServant);
        }
        ImServantVO imServantVO = new ImServantVO();
        imServantVO.setServantId(imServant.getServantId());
        imServantVO.setStatus(imServant.getStatus());
        imServantVO.setLastUpdateTime(TigUtils.handelTime(imServant.getLastUpdateTime()));
        imServantVO.setShopId(imServant.getShopId());
        return imServantVO;
    }
}
