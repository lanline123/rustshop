package com.tigshop.service.deposit.support;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.tigshop.bean.enums.deposit.DepositLogType;
import com.tigshop.bean.enums.deposit.DepositPayeeAccountType;
import com.tigshop.bean.model.deposit.ShopDepositLog;
import com.tigshop.bean.model.deposit.ShopDepositRecharge;
import com.tigshop.bean.model.deposit.ShopDepositRefund;
import com.tigshop.bean.vo.deposit.DepositPayeeAccountVO;
import com.tigshop.bean.vo.deposit.DepositShopLogVO;
import com.tigshop.mapper.deposit.ShopDepositRechargeMapper;
import com.tigshop.mapper.deposit.ShopDepositRefundMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 店铺/供应商端保证金流水展示富化：来源账户、去向账户。
 */
@Component
public class DepositShopLogEnricher {

    /** 保证金账户余额（站内账户）。 */
    public static final String ACCOUNT_DEPOSIT_BALANCE = "保证金余额";

    /** 平台侧账户（扣除/调整出账去向）。 */
    public static final String ACCOUNT_PLATFORM = "平台账户";

    @Resource
    private ShopDepositRechargeMapper shopDepositRechargeMapper;

    @Resource
    private ShopDepositRefundMapper shopDepositRefundMapper;

    /**
     * 批量回填 {@link DepositShopLogVO} 的来源/去向账户（与 logs 顺序一一对应）。
     */
    public void fillSourceDestAccounts(List<ShopDepositLog> logs, List<DepositShopLogVO> vos) {
        if (logs == null || vos == null || logs.isEmpty()) {
            return;
        }
        Set<Integer> rechargeIds = new HashSet<>();
        Set<Integer> refundIds = new HashSet<>();
        for (ShopDepositLog log : logs) {
            if (log.getRelatedId() == null || log.getRelatedId() <= 0 || log.getType() == null) {
                continue;
            }
            if (DepositLogType.RECHARGE.getCode().equals(log.getType())) {
                rechargeIds.add(log.getRelatedId());
            } else if (DepositLogType.WITHDRAW.getCode().equals(log.getType())
                    || DepositLogType.REFUND.getCode().equals(log.getType())) {
                refundIds.add(log.getRelatedId());
            }
        }
        Map<Integer, ShopDepositRecharge> rechargeMap = batchMap(rechargeIds, shopDepositRechargeMapper::selectBatchIds,
                ShopDepositRecharge::getShopDepositRechargeId);
        Map<Integer, ShopDepositRefund> refundMap = batchMap(refundIds, shopDepositRefundMapper::selectBatchIds,
                ShopDepositRefund::getShopDepositRefundId);

        int size = Math.min(logs.size(), vos.size());
        for (int i = 0; i < size; i++) {
            applySourceDest(vos.get(i), logs.get(i), rechargeMap, refundMap);
        }
    }

    private static <T> Map<Integer, T> batchMap(Set<Integer> ids,
                                                Function<List<Integer>, List<T>> loader,
                                                Function<T, Integer> idGetter) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<T> list = loader.apply(ids.stream().toList());
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, T> map = new HashMap<>(list.size());
        for (T item : list) {
            if (item != null && idGetter.apply(item) != null) {
                map.put(idGetter.apply(item), item);
            }
        }
        return map;
    }

    private void applySourceDest(DepositShopLogVO vo, ShopDepositLog log,
                                 Map<Integer, ShopDepositRecharge> rechargeMap,
                                 Map<Integer, ShopDepositRefund> refundMap) {
        if (vo == null || log == null || log.getType() == null) {
            return;
        }
        Integer type = log.getType();
        Integer relatedId = log.getRelatedId();

        if (DepositLogType.RECHARGE.getCode().equals(type)) {
            ShopDepositRecharge recharge = relatedId != null ? rechargeMap.get(relatedId) : null;
            vo.setSourceAccount(formatRechargePayAccount(recharge));
            vo.setDestAccount(ACCOUNT_DEPOSIT_BALANCE);
            return;
        }
        if (DepositLogType.DEDUCT.getCode().equals(type)) {
            vo.setSourceAccount(ACCOUNT_DEPOSIT_BALANCE);
            vo.setDestAccount(ACCOUNT_PLATFORM);
            return;
        }
        if (DepositLogType.WITHDRAW.getCode().equals(type) || DepositLogType.REFUND.getCode().equals(type)) {
            ShopDepositRefund refund = relatedId != null ? refundMap.get(relatedId) : null;
            vo.setSourceAccount(ACCOUNT_DEPOSIT_BALANCE);
            vo.setDestAccount(formatRefundPayeeAccount(refund));
            return;
        }
        if (DepositLogType.ADJUST.getCode().equals(type)) {
            vo.setSourceAccount(ACCOUNT_DEPOSIT_BALANCE);
            vo.setDestAccount(ACCOUNT_DEPOSIT_BALANCE);
        }
    }

    /** 充值：来源=商户打款使用的平台收款账户（快照）。 */
    static String formatRechargePayAccount(ShopDepositRecharge recharge) {
        if (recharge == null) {
            return "";
        }
        if (StrUtil.isNotBlank(recharge.getPayeeSnapshot())) {
            DepositPayeeAccountVO payee = JSON.parseObject(recharge.getPayeeSnapshot(), DepositPayeeAccountVO.class);
            if (payee != null) {
                return formatPayeeDisplay(payee.getAccountType(), payee.getAccountName(), payee.getAccountNo());
            }
        }
        return formatPayeeDisplay(recharge.getPayeeAccountType(), null, null);
    }

    /** 提现/退还：去向=申请时填写的收款信息。 */
    static String formatRefundPayeeAccount(ShopDepositRefund refund) {
        if (refund == null || StrUtil.isBlank(refund.getPayeeInfo())) {
            return "";
        }
        return refund.getPayeeInfo().trim();
    }

    private static String formatPayeeDisplay(Integer accountType, String accountName, String accountNo) {
        String typeText = DepositPayeeAccountType.getDescByCode(accountType);
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(typeText)) {
            sb.append(typeText);
        }
        if (StrUtil.isNotBlank(accountName)) {
            if (!sb.isEmpty()) {
                sb.append('/');
            }
            sb.append(accountName);
        }
        if (StrUtil.isNotBlank(accountNo)) {
            if (!sb.isEmpty()) {
                sb.append('/');
            }
            sb.append(accountNo);
        }
        return sb.toString();
    }
}
