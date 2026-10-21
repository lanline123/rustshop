/** * @file 保证金设置页 * @description 配置保证金收取规则、缴存/补缴缓冲、保证金协议（对照 PRD 7.1） */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form v-if="!loading" ref="formRef" :model="formState" label-width="90px" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper">
                    <el-form-item :label="$t('保证金功能')" prop="depositEnable">
                        <div>
                            <el-switch v-model="formState.depositEnable" :active-value="1" :inactive-value="0" />
                            <div class="extra"> {{ $t('开启后平台启用店铺/供应商保证金管理能力，店铺/供应商需按配置规则缴纳保证金，用于履约保障、售后赔付及违规扣款；关闭则全部店铺/供应商免缴保证金。开启前请配置相关保证金账户') }} <el-button type="primary" link @click="goAccount">{{ $t('去设置>') }}</el-button>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('收取规则')" prop="depositCollectRule">
                        <el-radio-group v-model="formState.depositCollectRule">
                            <el-radio :value="CollectRule.HighestCategory" disabled>{{ $t('按店铺/供应商类目保证金最高收取') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('收取节点')" prop="depositCollectNode" :rules="[{ required: true, message: '请选择收取节点' }]">
                        <el-radio-group v-model="formState.depositCollectNode" class="vertical-radio-group">
                            <el-radio :value="CollectNode.OnEnter">{{ $t('店铺/供应商上架商品时收取保证金') }}</el-radio>
                            <el-radio :value="CollectNode.OnFirstOrder">{{ $t('店铺/供应商售卖第一单收取保证金') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('缴存缓冲')" required>
                        <div>
                            <div class="buffer-line">
                                <span>{{ $t('触发收取节点') }}</span>
                                <el-form-item prop="depositCollectBufferDays" :rules="daysRules">
                                    <TigInput type="integer" v-model="formState.depositCollectBufferDays" :min="0" width="100px" :placeholder="$t('请输入')" />
                                </el-form-item>
                                <span>{{ $t('天内，未足额缴纳保证金，店铺/供应商') }}</span>
                                <el-form-item prop="depositCollectBufferAction" :rules="actionRules">
                                    <el-select v-model="formState.depositCollectBufferAction" :placeholder="$t('请选择')" style="width: 180px">
                                        <el-option v-for="item in bufferActionList" :key="item.value" :label="item.label" :value="item.value" />
                                    </el-select>
                                </el-form-item>
                            </div>
                            <div class="extra">{{ $t('店铺/供应商入驻成功，初次缴纳保证金的缓冲机制') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('补缴缓冲')" required>
                        <div>
                            <div class="buffer-line">
                                <span>{{ $t('店铺/供应商类目调整或平台手动调整保证金') }}</span>
                                <el-form-item prop="depositSupplementBufferDays" :rules="daysRules">
                                    <TigInput type="integer" v-model="formState.depositSupplementBufferDays" :min="0" width="100px" :placeholder="$t('请输入')" />
                                </el-form-item>
                                <span>{{ $t('天内，未足额缴纳保证金，店铺/供应商') }}</span>
                                <el-form-item prop="depositSupplementBufferAction" :rules="actionRules">
                                    <el-select v-model="formState.depositSupplementBufferAction" :placeholder="$t('请选择')" style="width: 180px">
                                        <el-option v-for="item in bufferActionList" :key="item.value" :label="item.label" :value="item.value" />
                                    </el-select>
                                </el-form-item>
                            </div>
                            <div class="extra">{{ $t('店铺/供应商已交保证金，因规则调整，保证金扣除等情况的缓冲机制') }}</div>
                        </div>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <el-form-item :label="$t('保证金协议')" prop="depositProtocolEnable">
                        <div>
                            <el-switch v-model="formState.depositProtocolEnable" :active-value="1" :inactive-value="0" />
                            <div class="extra"> {{ $t('本协议功能用于在商家店铺/供应商【保证金】菜单内，展示保证金相关协议文本，供商家查看、知晓保证金的缴纳、使用、退还等相关规则，同时为功能开发提供明确的展示标准和内容边界。') }} </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('内容')" prop="depositProtocolContent">
                        <Editor v-model:html="formState.depositProtocolContent" height="400px"></Editor>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '370px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }} </el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";
import { Editor } from "@/components/editor/index";
import { useThemeStore } from "@/store/theme";
import { getDepositSetting, updateDepositSetting } from "@/api/finance/deposit";
import { BufferAction, CollectNode, CollectRule } from "@/types/finance/deposit.d";
import type { DepositSettingState } from "@/types/finance/deposit.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const router = useRouter();
const formRef = shallowRef();
const loading = ref<boolean>(true);
const confirmLoading = ref<boolean>(false);

const bufferActionList = [
    { value: BufferAction.LimitNewSpu, label: t('限制上架新品') },
    { value: BufferAction.OffAllSpu, label: t('全店商品下架') }
];

const daysRules = [
    {
        required: true,
        validator: (_rule: any, value: any, callback: (error?: Error) => void) => {
            if (value === undefined || value === null || value === "") {
                return callback(new Error(t('请输入缓冲天数')));
            }
            const num = Number(value);
            if (!Number.isInteger(num) || num < 0) {
                return callback(new Error(t('请输入大于等于 0 的整数')));
            }
            callback();
        },
        trigger: ["blur", "change"]
    }
];

const actionRules = [{ required: true, message: t('请选择处理方式'), trigger: "change" }];

const formState = ref<DepositSettingState>({
    depositEnable: 0,
    depositCollectRule: CollectRule.HighestCategory,
    depositCollectNode: CollectNode.OnEnter,
    depositCollectBufferDays: undefined,
    depositCollectBufferAction: undefined,
    depositSupplementBufferDays: undefined,
    depositSupplementBufferAction: undefined,
    depositProtocolEnable: 0,
    depositProtocolContent: ""
});

const goAccount = () => {
    router.push("/finance/deposit-account/list");
};

const loadFilter = async () => {
    try {
        const result = await getDepositSetting();
        if (result) {
            Object.assign(formState.value, result);
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    confirmLoading.value = true;
    try {
        await updateDepositSetting(formState.value);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};

onMounted(() => {
    loadFilter();
});
</script>

<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}
.buffer-line {
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 8px;
    color: #333;
    span {
        line-height: 32px;
        white-space: nowrap;
    }
    :deep(.el-form-item) {
        margin-bottom: 0;
        position: relative;
    }
    :deep(.el-form-item__error) {
        position: absolute;
        top: 100%;
        left: 0;
        white-space: nowrap;
    }
}
.vertical-radio-group {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
}
.extra :deep(.el-button) {
    font-size: inherit;
    height: auto;
    padding: 0;
    vertical-align: baseline;
}
</style>
