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

package com.tigshop.service.merchant.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.merchant.MerchantAccountCreateDTO;
import com.tigshop.bean.dto.merchant.MerchantAccountListDTO;
import com.tigshop.bean.dto.merchant.MerchantAccountUpdateDTO;
import com.tigshop.bean.enums.merchant.MerchantAccountType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.merchant.MerchantAccount;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.merchant.MerchantAccountVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.merchant.MerchantAccountMapper;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.merchant.MerchantAccountService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 商家账户表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
@RequiredArgsConstructor
public class MerchantAccountServiceImpl extends BaseServiceImpl<MerchantAccountMapper, MerchantAccount> implements MerchantAccountService {
    private final MerchantMapper merchantMapper;
    private final ShopMapper shopMapper;
    private final AdminUserMapper adminUserMapper;
    private final ConfigService configService;

    @Override
    public Page<MerchantAccountVO> list(MerchantAccountListDTO listDTO) {
        // 分页
        Page<MerchantAccount> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<MerchantAccount> queryWrapper = new LambdaQueryWrapper<>();
        //获取商户信息
        if (HeaderUtils.getShopId() != null && HeaderUtils.getShopId() > 0) {
            Shop shop = shopMapper.selectById(HeaderUtils.getShopId());
            if (shop == null) {
                throw new GlobalException("店铺不存在");
            }

            Merchant merchant = merchantMapper.selectById(shop.getMerchantId());
            if (merchant == null) {
                throw new GlobalException("商户不存在");
            }
            //queryWrapper.eq(MerchantAccount::getMerchantId, merchant.getMerchantId());
        }

        queryWrapper.eq(HeaderUtils.getShopId() != null && HeaderUtils.getShopId() > 0,
                MerchantAccount::getShopId, HeaderUtils.getShopId());

        // 查询设置的提现方式
        String withdrawalReceiptMethod = configService.getConfigVal(SettingsEnum.WITHDRAWAL_RECEIPT_METHOD);
        if (StrUtil.isEmpty(withdrawalReceiptMethod)) {
            queryWrapper.in(MerchantAccount::getAccountType, JSON.toJSONString(Arrays.asList(1,2,3)));
        }else {
            JSONArray ja = JSON.parseArray(withdrawalReceiptMethod);
            if (!ja.isEmpty()) {
                queryWrapper.in(MerchantAccount::getAccountType, ja);
            }
        }
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 执行查询
        Page<MerchantAccount> merchantAccountPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<MerchantAccount> merchantAccountPageRecords = merchantAccountPage.getRecords();
        // 转换为VO
        List<MerchantAccountVO> merchantAccountVOList = merchantAccountPageRecords.stream()
                .map(merchantAccount -> {
                    MerchantAccountVO merchantAccountVO = new MerchantAccountVO();
                    BeanUtils.copyProperties(merchantAccount, merchantAccountVO);
                    merchantAccountVO.setAccountTypeText(MerchantAccountType.getTypeName(merchantAccount.getAccountType()));
                    merchantAccountVO.setAddTime(TigUtils.handelTime(merchantAccount.getAddTime()));
                    return merchantAccountVO;
                }).toList();
        return PageUtil.convertPage(merchantAccountPage, merchantAccountVOList);
    }

    @Override
    public MerchantAccountVO detail(Integer id) {
        if (id != null) {
            MerchantAccount merchantAccount = this.getById(id);
            MerchantAccountVO merchantAccountVO = new MerchantAccountVO();
            BeanUtils.copyProperties(merchantAccount, merchantAccountVO);
            return merchantAccountVO;
        }
        return null;
    }

    @Override
    public boolean create(MerchantAccountCreateDTO createDTO) {
        if (createDTO != null) {
            MerchantAccount merchantAccount = new MerchantAccount();
            BeanUtils.copyProperties(createDTO, merchantAccount);
            return this.save(merchantAccount);
        }
        return false;
    }

    @Override
    public boolean update(MerchantAccountUpdateDTO updateDTO) {
        if (updateDTO != null) {
            MerchantAccount merchantAccount = new MerchantAccount();
            BeanUtils.copyProperties(updateDTO, merchantAccount);
            return this.updateById(merchantAccount);
        }
        return false;
    }

    @Override
    public Integer getCardCount(Integer merchantId, Integer shopId) {
        QueryWrapper<MerchantAccount> queryWrapper = new QueryWrapper<>();
        if (merchantId > 0) {
            queryWrapper.eq("merchant_id", merchantId);
        }
        if (shopId > 0) {
            queryWrapper.eq("shop_id", shopId);
        }
        queryWrapper.select("count(*) as account_id");
        return this.getOne(queryWrapper).getAccountId();
    }

    @Override
    public boolean checkMerchantAuth(Integer adminId, Integer merchantId) {
        AdminUser adminUser = adminUserMapper.selectById(adminId);
        if (adminUser != null) {
            if (!Objects.equals(adminUser.getMerchantId(), merchantId)) {
                throw new GlobalException("数据不存在");
            }
        }
        return true;
    }
}
