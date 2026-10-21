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

package com.tigshop.service.salesman.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tigshop.bean.enums.salesman.DistributionModeEnum;
import com.tigshop.bean.enums.salesman.RecruitMethodEnum;
import com.tigshop.bean.enums.salesman.SalesmanConfigTypeEnum;
import com.tigshop.bean.model.salesman.SalesmanConfig;
import com.tigshop.bean.param.salesman.SalesmanConfigSaveParam;
import com.tigshop.bean.vo.salesman.SalesmanConfigVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.mapper.salesman.SalesmanConfigMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.salesman.SalesmanConfigService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 分销模式设置服务实现类
 *
 * @author kidd
 * @since 2023-07-05
 */
@Service
public class SalesmanConfigServiceImpl extends BaseServiceImpl<SalesmanConfigMapper, SalesmanConfig> implements SalesmanConfigService {

    @Override
    public SalesmanConfigVO detail(String code) {
        SalesmanConfig salesmanConfig = this.lambdaQuery().eq(SalesmanConfig::getCode, code).one();

        SalesmanConfigVO salesmanConfigVO = new SalesmanConfigVO();
        if (salesmanConfig != null) {
            salesmanConfigVO.setId(salesmanConfig.getId());
            salesmanConfigVO.setShopId(salesmanConfig.getShopId());
            if (salesmanConfig.getData() != null) {
                salesmanConfigVO = JsonUtil.fromJson(salesmanConfig.getData(), SalesmanConfigVO.class);
            }
        }

        if (SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode().equals(code)) {
            // 老数据无 distributionMode 字段时一律按推广员等级模式
            if (salesmanConfigVO.getDistributionMode() == null) {
                salesmanConfigVO.setDistributionMode(DistributionModeEnum.PROMOTER.getCode());
            }
            if (salesmanConfigVO.getViewCustomerInfo() == null) {
                salesmanConfigVO.setViewCustomerInfo(0);
            }
            salesmanConfigVO.setWechatBizEnabled(Boolean.TRUE);
        }

        return salesmanConfigVO;
    }

    @Override
    public void save(String code, SalesmanConfigSaveParam param) {
        if (SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode().equals(code)) {
            this.saveSalesmanConfig(param);
        }

        if (SalesmanConfigTypeEnum.SALESMAN_SETTLEMENT.getCode().equals(code)) {
            this.saveSalesmanSettlement(param);
        }
    }

    private void saveSalesmanSettlement(SalesmanConfigSaveParam param) {
        Integer shopId = HeaderUtils.getShopId();
        String code = SalesmanConfigTypeEnum.SALESMAN_SETTLEMENT.getCode();

        // 1. 新增/更新分销模式
        saveOrUpdateConfig(param, code, shopId);
    }

    private void saveSalesmanConfig(SalesmanConfigSaveParam param) {
        Integer shopId = HeaderUtils.getShopId();
        String code = SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode();

        // 1. 验证分销模式参数
        validateDistributionMode(param);
        param.adjustLevels();

        // 2. 新增/更新分销模式
        saveOrUpdateConfig(param, code, shopId);
    }

    /**
     * 校验分销模式与下级招募方式
     * <p>微商模式（distributionMode=2）要求至少选择一种下级招募方式；
     * 推广员等级模式下招募方式不生效，保存时清空避免脏数据。</p>
     */
    private void validateDistributionMode(SalesmanConfigSaveParam param) {
        if (!DistributionModeEnum.isWechatBiz(param.getDistributionMode())) {
            param.setDistributionMode(DistributionModeEnum.PROMOTER.getCode());
            param.setRecruitMethods(null);
            return;
        }

        Assert.isTrue(CollUtil.isNotEmpty(param.getRecruitMethods()),
                () -> new GlobalException("微商模式下请至少选择一种下级招募方式"));
        for (Integer method : param.getRecruitMethods()) {
            Assert.isTrue(RecruitMethodEnum.isValidCode(method),
                    () -> new GlobalException("无效的下级招募方式：" + method));
        }
    }

    private void saveOrUpdateConfig(SalesmanConfigSaveParam param, String code, Integer shopId) {
        // 1. 查询分销模式
        SalesmanConfig salesmanConfig = this.lambdaQuery()
                .eq(SalesmanConfig::getCode, code)
                .eq(SalesmanConfig::getShopId, shopId)
                .one();

        // 2. 新增/更新分销模式
        if (salesmanConfig != null) {
            // 编辑
            salesmanConfig.setData(mergeConfigData(salesmanConfig.getData(), param));
            this.updateById(salesmanConfig);
        } else {
            // 新增
            salesmanConfig = SalesmanConfig.builder()
                    .code(code)
                    .shopId(shopId)
                    .data(mergeConfigData(null, param))
                    .build();
            this.save(salesmanConfig);
        }
    }

    static String mergeConfigData(String existingData, SalesmanConfigSaveParam param) {
        JSONObject existing = existingData == null || existingData.isBlank()
                ? new JSONObject()
                : JSON.parseObject(existingData);
        if (existing == null) {
            existing = new JSONObject();
        }

        JSONObject updates = JSON.parseObject(JSON.toJSONString(param));
        if (updates != null) {
            mergeJsonObject(existing, updates);
        }
        if (param.getRecruitMethods() == null) {
            existing.remove("recruitMethods");
        }
        return JSON.toJSONString(existing);
    }

    private static void mergeJsonObject(JSONObject target, JSONObject updates) {
        updates.forEach((key, value) -> target.put(key, mergeJsonValue(target.get(key), value)));
    }

    private static Object mergeJsonValue(Object existing, Object updates) {
        if (existing instanceof JSONObject existingObject && updates instanceof JSONObject updatesObject) {
            mergeJsonObject(existingObject, updatesObject);
            return existingObject;
        }
        if (existing instanceof JSONArray existingArray && updates instanceof JSONArray updatesArray) {
            JSONArray merged = new JSONArray();
            for (int i = 0; i < updatesArray.size(); i++) {
                Object updatedItem = updatesArray.get(i);
                Object existingItem = findExistingArrayItem(existingArray, updatedItem, i);
                merged.add(mergeJsonValue(existingItem, updatedItem));
            }
            return merged;
        }
        return updates;
    }

    private static Object findExistingArrayItem(JSONArray existingArray, Object updatedItem, int index) {
        if (updatedItem instanceof JSONObject updatedObject && updatedObject.containsKey("id")) {
            Object updatedId = updatedObject.get("id");
            for (Object existingItem : existingArray) {
                if (existingItem instanceof JSONObject existingObject
                        && Objects.equals(updatedId, existingObject.get("id"))) {
                    return existingItem;
                }
            }
            return null;
        }
        return index < existingArray.size() ? existingArray.get(index) : null;
    }

}
