package com.tigshop.service.deposit.impl;

import org.springframework.security.access.prepost.PreAuthorize;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositPayeeAccountDTO;
import com.tigshop.bean.dto.deposit.DepositPayeeAccountListDTO;
import com.tigshop.bean.enums.deposit.DepositPayeeAccountType;
import com.tigshop.bean.model.deposit.ShopDepositPayeeAccount;
import com.tigshop.bean.vo.deposit.DepositPayeeAccountVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.deposit.ShopDepositPayeeAccountMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.deposit.DepositPayeeAccountService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 平台保证金收款账户服务实现（独立表 shop_deposit_payee_account）。
 *
 * @author Tigshop团队
 */
@Service
public class DepositPayeeAccountServiceImpl
        extends BaseServiceImpl<ShopDepositPayeeAccountMapper, ShopDepositPayeeAccount>
        implements DepositPayeeAccountService {

    @Override
    public Page<DepositPayeeAccountVO> list(DepositPayeeAccountListDTO listDTO) {
        Page<ShopDepositPayeeAccount> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        LambdaQueryWrapper<ShopDepositPayeeAccount> qw = new LambdaQueryWrapper<>();
        qw.eq(listDTO.getAccountType() != null,
                ShopDepositPayeeAccount::getAccountType, listDTO.getAccountType());
        qw.eq(listDTO.getStatus() != null,
                ShopDepositPayeeAccount::getStatus, listDTO.getStatus());
        if (StrUtil.isNotBlank(listDTO.getKeyword())) {
            String kw = listDTO.getKeyword().trim();
            qw.and(w -> w.like(ShopDepositPayeeAccount::getAccountName, kw)
                    .or().like(ShopDepositPayeeAccount::getAccountNo, kw));
        }
        qw.orderByAsc(ShopDepositPayeeAccount::getSortOrder)
                .orderByDesc(ShopDepositPayeeAccount::getShopDepositPayeeAccountId);
        Page<ShopDepositPayeeAccount> accountPage = this.page(page, qw);
        List<DepositPayeeAccountVO> voList = accountPage.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageUtil.convertPage(accountPage, voList);
    }

    @Override
    public List<DepositPayeeAccountVO> listEnabled(Integer accountType, String keyword) {
        LambdaQueryWrapper<ShopDepositPayeeAccount> qw = Wrappers.<ShopDepositPayeeAccount>lambdaQuery()
                .eq(ShopDepositPayeeAccount::getStatus, 1)
                .eq(accountType != null, ShopDepositPayeeAccount::getAccountType, accountType);
        if (StrUtil.isNotBlank(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(ShopDepositPayeeAccount::getAccountName, kw)
                    .or().like(ShopDepositPayeeAccount::getAccountNo, kw));
        }
        qw.orderByAsc(ShopDepositPayeeAccount::getSortOrder)
                .orderByDesc(ShopDepositPayeeAccount::getShopDepositPayeeAccountId);
        List<ShopDepositPayeeAccount> list = this.list(qw);
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public DepositPayeeAccountVO detail(Integer id) {
        if (id == null) {
            return null;
        }
        ShopDepositPayeeAccount entity = this.getById(id);
        return entity == null ? null : toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@pms.hasPermission('depositPayeeAccountUpdate')")
    public void create(DepositPayeeAccountDTO dto) {
        validate(dto);
        long now = nowSeconds();
        ShopDepositPayeeAccount entity = new ShopDepositPayeeAccount();
        entity.setAccountType(dto.getAccountType());
        entity.setAccountName(dto.getAccountName());
        entity.setAccountNo(dto.getAccountNo());
        entity.setBankName(dto.getBankName());
        entity.setBankBranch(dto.getBankBranch());
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        entity.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        entity.setAddTime(now);
        entity.setUpdateTime(now);
        this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@pms.hasPermission('depositPayeeAccountUpdate')")
    public void update(DepositPayeeAccountDTO dto) {
        if (dto.getId() == null) {
            throw new GlobalException("账户ID不能为空");
        }
        validate(dto);
        ShopDepositPayeeAccount entity = this.getById(dto.getId());
        if (entity == null) {
            throw new GlobalException("收款账户不存在");
        }
        entity.setAccountType(dto.getAccountType());
        entity.setAccountName(dto.getAccountName());
        entity.setAccountNo(dto.getAccountNo());
        entity.setBankName(dto.getBankName());
        entity.setBankBranch(dto.getBankBranch());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }
        entity.setUpdateTime(nowSeconds());
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@pms.hasPermission('depositPayeeAccountUpdate')")
    public boolean del(Integer id) {
        if (id == null) {
            throw new GlobalException("账户ID不能为空");
        }
        ShopDepositPayeeAccount entity = this.getById(id);
        if (entity == null) {
            throw new GlobalException("收款账户不存在");
        }
        return this.removeById(id);
    }

    // ---------------------------------------------------------------------

    private void validate(DepositPayeeAccountDTO dto) {
        if (dto.getAccountType() == null) {
            throw new GlobalException("账户类型不能为空");
        }
        if (StrUtil.isBlank(dto.getAccountName())) {
            throw new GlobalException("账户名称不能为空");
        }
        if (StrUtil.isBlank(dto.getAccountNo())) {
            throw new GlobalException("账户号不能为空");
        }
    }

    private DepositPayeeAccountVO toVO(ShopDepositPayeeAccount entity) {
        DepositPayeeAccountVO vo = new DepositPayeeAccountVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setId(entity.getShopDepositPayeeAccountId());
        vo.setAccountTypeText(DepositPayeeAccountType.getDescByCode(entity.getAccountType()));
        return vo;
    }

    private static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
