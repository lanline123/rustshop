<!--
 * @file 平台端客户设置（D1116-shopMember）
 * @description 店铺客户总开关（能力门）+ 店铺客户设置（信息脱敏 / 添加客户 / 导入客户）。
 *   保存后刷新全局 config，使总开关与三项设置全站/店铺端即时生效。字段名不变（isOpenShopMember 等）。
-->
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form v-if="!loading" :model="formState" label-width="140px" style="display: flex; gap: 12px; flex-direction: column">
                <!-- 客户总开关（能力门）；o2o/多门店版本文案「店铺」→「门店」 -->
                <div class="content_wrapper">
                    <el-form-item :label="$t('{shopTerm}是否展示客户', { shopTerm })">
                        <div>
                            <div class="switch-line">
                                <el-switch v-model="formState.shopMemberEnabled" :active-value="1" :inactive-value="0" />
                                <span class="switch-state">{{ formState.shopMemberEnabled === 1 ? $t("已开启") : $t("已关闭") }}</span>
                            </div>
                            <div class="extra">
                                {{
                                    $t("开启后，顾客在{shopTerm}下单将自动成为该{shopTerm}客户，{shopTerm}可查看和管理本店客户；关闭后全平台冻结。", {
                                        shopTerm
                                    })
                                }}
                            </div>
                        </div>
                    </el-form-item>
                </div>

                <!-- 客户设置 -->
                <div class="content_wrapper">
                    <div class="block-title">{{ $t("{shopTerm}客户设置", { shopTerm }) }}</div>
                    <el-form-item :label="$t('敏感信息脱敏')">
                        <div>
                            <el-radio-group v-model="formState.shopMemberDesensitize" :disabled="settingsDisabled">
                                <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t("开启后，{shopTerm}端查看客户时对昵称/手机号脱敏展示。总开关关闭时默认关闭。", { shopTerm }) }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('允许{shopTerm}添加客户', { shopTerm })">
                        <div>
                            <el-radio-group v-model="formState.shopMemberAllowAdd" :disabled="settingsDisabled">
                                <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t("开启后，{shopTerm}端可手动添加客户为本店客户。", { shopTerm }) }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('允许{shopTerm}导入客户', { shopTerm })">
                        <div>
                            <el-radio-group v-model="formState.shopMemberAllowImport" :disabled="settingsDisabled">
                                <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t("开启后，{shopTerm}端可批量导入客户为本店客户。", { shopTerm }) }}</div>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '370px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t("保存") }}</el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { message } from "ant-design-vue";
import { useThemeStore } from "@/store/theme";
import { useConfigStore } from "@/store/config";
import { isStore } from "@/utils/version";
import { getShopMemberSetting, updateShopMemberSetting } from "@/api/user/shopMember";
import type { ShopMemberSettingState } from "@/types/user/shopMember.d";

const { t } = useI18n();
// o2o/多门店版本（VITE_IS_STORE）下经营主体称谓「店铺」→「门店」；平台端无 shop 上下文，只能用构建期开关
const shopTerm = computed(() => (isStore() ? t("门店") : t("店铺")));

const { themeInfo } = useThemeStore();
const configStore = useConfigStore();
const loading = ref<boolean>(true);
const confirmLoading = ref<boolean>(false);

const formState = ref<ShopMemberSettingState>({
    shopMemberEnabled: 0,
    shopMemberDesensitize: 0,
    shopMemberAllowAdd: 0,
    shopMemberAllowImport: 0
});

// 总开关关闭时，下方三项单选禁用
const settingsDisabled = computed<boolean>(() => formState.value.shopMemberEnabled !== 1);

// 总开关关闭时，三项设置强制默认值：脱敏开启，添加/导入关闭
const applyDisabledDefaults = () => {
    formState.value.shopMemberDesensitize = 1;
    formState.value.shopMemberAllowAdd = 0;
    formState.value.shopMemberAllowImport = 0;
};

// 关闭总开关时立即回落默认值
watch(
    () => formState.value.shopMemberEnabled,
    (val) => {
        if (val !== 1) applyDisabledDefaults();
    }
);

const loadSetting = async () => {
    try {
        const result = await getShopMemberSetting();
        if (result) {
            formState.value.shopMemberEnabled = result.shopMemberEnabled ?? 0;
            formState.value.shopMemberDesensitize = result.shopMemberDesensitize ?? 0;
            formState.value.shopMemberAllowAdd = result.shopMemberAllowAdd ?? 0;
            formState.value.shopMemberAllowImport = result.shopMemberAllowImport ?? 0;
        }
        // 载入后若总开关为关，回落默认值（脱敏开、其余关）
        if (formState.value.shopMemberEnabled !== 1) applyDisabledDefaults();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        await updateShopMemberSetting(formState.value);
        await configStore.updateConfig();
        message.success(t("操作成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};

onMounted(() => {
    loadSetting();
});
</script>

<style lang="less" scoped>
.block-title {
    font-weight: bold;
    font-size: 15px;
    margin-bottom: 20px;
}
.switch-line {
    display: flex;
    align-items: center;
    gap: 10px;
}
.switch-state {
    color: #999;
    font-size: 13px;
}
.extra {
    margin-top: 8px;
    color: #999;
    font-size: 13px;
    line-height: 1.6;
}
:deep(.el-form-item) {
    align-items: flex-start;
}
:deep(.el-form-item__label) {
    align-items: flex-start;
    height: auto;
    line-height: 32px;
}
</style>
