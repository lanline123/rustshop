package com.tigshop.service.finance.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.finance.SubMerchantStatus;
import com.tigshop.bean.model.finance.SubMerchant;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.param.finance.SubMerchantBindParam;
import com.tigshop.bean.query.finance.SubMerchantPageQuery;
import com.tigshop.bean.vo.finance.SubMerchantVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.mapper.finance.SubMerchantMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.SubMerchantService;
import com.tigshop.service.finance.profitsharing.ProfitSharingApiClient;
import com.tigshop.service.finance.profitsharing.ProfitSharingClientRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 子商户 Service 实现
 *
 * @author Tigshop团队
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubMerchantServiceImpl extends BaseServiceImpl<SubMerchantMapper, SubMerchant>
        implements SubMerchantService {

    private static final String DEFAULT_CHANNEL = "WECHAT";

    private final ShopMapper shopMapper;

    private final TigshopProperties tigshopProperties;

    private final ProfitSharingClientRegistry profitSharingClientRegistry;

    @Override
    public Page<SubMerchantVO> listPage(SubMerchantPageQuery query) {
        Page<SubMerchant> pageParam = buildSortOrder(query);
        LambdaQueryWrapper<SubMerchant> wrapper = new LambdaQueryWrapper<>();
        if (query.getShopId() != null) {
            wrapper.eq(SubMerchant::getShopId, query.getShopId());
        }
        if (StrUtil.isNotBlank(query.getPayChannel())) {
            wrapper.eq(SubMerchant::getPayChannel, query.getPayChannel());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SubMerchant::getStatus, query.getStatus());
        }
        if (StrUtil.isNotBlank(query.getSubMchId())) {
            wrapper.eq(SubMerchant::getSubMchId, query.getSubMchId());
        }
        Page<SubMerchant> entityPage = this.page(pageParam, wrapper);

        Page<SubMerchantVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<SubMerchantVO> voList = entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        fillShopNames(voList);
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void bind(SubMerchantBindParam param) {
        String channel = StrUtil.isBlank(param.getPayChannel()) ? DEFAULT_CHANNEL : param.getPayChannel();
        // 手动绑定原本零校验，随手填的商户号会被置 ACTIVE 拿去收款，用户下单时才被微信拒「受理关系不存在」。
        // 这里先向渠道确认归属，把错误提前到绑定时；无法判定（网络抖动/平台权限未开）的渠道会放行。
        assertSubMerchantOwned(channel, param.getSubMchId());
        SubMerchant existing = lambdaQuery()
                .eq(SubMerchant::getShopId, param.getShopId())
                .eq(SubMerchant::getPayChannel, channel)
                .one();
        Date now = new Date();
        if (existing != null) {
            if (SubMerchantStatus.ACTIVE.getCode().equals(existing.getStatus())) {
                throw new GlobalException("该店铺已绑定此支付渠道的子商户");
            }
            existing.setSubMchId(param.getSubMchId());
            existing.setSubAppid(param.getSubAppid());
            existing.setMerchantName(param.getMerchantName());
            existing.setStatus(SubMerchantStatus.ACTIVE.getCode());
            existing.setModifyTime(now);
            updateById(existing);
            return;
        }
        SubMerchant entity = new SubMerchant();
        entity.setShopId(param.getShopId());
        entity.setPayChannel(channel);
        entity.setSubMchId(param.getSubMchId());
        entity.setSubAppid(param.getSubAppid());
        entity.setMerchantName(param.getMerchantName());
        entity.setStatus(SubMerchantStatus.ACTIVE.getCode());
        entity.setCreateTime(now);
        entity.setModifyTime(now);
        save(entity);
    }

    private void assertSubMerchantOwned(String channel, String subMchId) {
        if (StrUtil.isBlank(subMchId)) {
            throw new GlobalException("子商户号不能为空");
        }
        // 渠道未启用时 registry 直接抛「分账渠道未启用或不存在」，绑一个收不了款的渠道本就无意义
        ProfitSharingApiClient client = profitSharingClientRegistry.get(channel);
        if (!client.isSubMerchantOwned(subMchId)) {
            throw new GlobalException("子商户号[" + subMchId + "]不在当前服务商名下，请核对后重试或改走系统进件");
        }
    }

    @Override
    public void unbind(Long id) {
        SubMerchant entity = getById(id);
        if (entity == null) {
            throw new GlobalException("子商户不存在");
        }
        entity.setStatus(SubMerchantStatus.FROZEN.getCode());
        entity.setModifyTime(new Date());
        updateById(entity);
    }

    @Override
    public SubMerchant getActiveByShop(Long shopId, String payChannel) {
        if (shopId == null) {
            return null;
        }
        String channel = StrUtil.isBlank(payChannel) ? DEFAULT_CHANNEL : payChannel;
        return lambdaQuery()
                .eq(SubMerchant::getShopId, shopId)
                .eq(SubMerchant::getPayChannel, channel)
                .eq(SubMerchant::getStatus, SubMerchantStatus.ACTIVE.getCode())
                .last("limit 1")
                .one();
    }

    private SubMerchantVO toVO(SubMerchant entity) {
        SubMerchantVO vo = new SubMerchantVO();
        vo.setId(entity.getId());
        vo.setShopId(entity.getShopId());
        vo.setPayChannel(entity.getPayChannel());
        vo.setSubMchId(entity.getSubMchId());
        vo.setSubAppid(entity.getSubAppid());
        vo.setMerchantName(entity.getMerchantName());
        vo.setStatus(entity.getStatus());
        SubMerchantStatus status = SubMerchantStatus.fromCode(entity.getStatus());
        vo.setStatusDesc(status != null ? status.getDesc() : null);
        vo.setApplicationId(entity.getApplicationId());
        vo.setCreateTime(entity.getCreateTime());
        vo.setModifyTime(entity.getModifyTime());
        return vo;
    }

    private void fillShopNames(List<SubMerchantVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        List<Integer> shopIds = voList.stream()
                .map(SubMerchantVO::getShopId)
                .filter(id -> id != null && id > 0)
                .map(Long::intValue)
                .distinct()
                .collect(Collectors.toList());
        // 不能因 shopIds 为空提前返回：整页只有平台自营（shopId=0）时仍需回填展示名
        Map<Integer, String> shopNameMap = shopIds.isEmpty()
                ? Map.of()
                : shopMapper.selectBatchIds(shopIds).stream()
                        .collect(Collectors.toMap(Shop::getShopId, s -> StrUtil.nullToEmpty(s.getShopTitle()), (a, b) -> a));
        voList.forEach(vo -> {
            Long shopId = vo.getShopId();
            if (shopId == null) {
                vo.setShopName("");
            } else if (shopId == 0L) {
                // 平台自营是虚拟门店、不在 shop 表，按形态给固定展示名
                vo.setShopName(selfOperatedShopName());
            } else {
                vo.setShopName(shopNameMap.getOrDefault(shopId.intValue(), ""));
            }
        });
    }

    /**
     * 平台自营的展示名：O2O 形态下经营主体是门店，其余形态是店铺。
     */
    private String selfOperatedShopName() {
        boolean o2o = tigshopProperties != null && tigshopProperties.getIsO2o() != null
                && tigshopProperties.getIsO2o() == 1;
        return o2o ? "自营门店" : "自营店铺";
    }
}
