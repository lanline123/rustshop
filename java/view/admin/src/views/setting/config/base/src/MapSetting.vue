/** * @file 地图设置组件 * @description 配置地图服务商及高德/天地图密钥（子组件） * @features * - 选择地图服务商（高德地图 / 天地图） * - 配置高德地图 Web / JS
Key 与 Secret * - 配置天地图服务端 / JS tk * - 密钥掩码回显与编辑弹窗，未改动原样回传不覆盖 * - 切换服务商时二次确认坐标偏差风险 */
<template>
    <div class="content_wrapper">
        <a-spin :spinning="loading">
            <a-skeleton :loading="loading" active :paragraph="{ rows: 8 }">
                <div class="title">{{ $t("地图服务商") }}</div>
                <el-form-item :label="$t('地图服务商')" prop="mapProvider">
                    <div>
                        <el-radio-group :model-value="currentProvider" class="itemWidth" @change="handleProviderChange">
                            <el-radio :value="MAP_PROVIDER_AMAP">{{ $t("高德地图") }}</el-radio>
                            <el-radio :value="MAP_PROVIDER_TIANDITU">{{ $t("天地图") }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t("两家地图坐标系不同，切换后已保存的历史地址与门店坐标不会自动转换，在新地图上可能有 50~500 米偏移。") }}</div>
                        <div class="extra">{{ $t("切换前请确认前端已同步发布对应的地图 SDK，否则新产生的定位数据同样会出现偏差。") }}</div>
                    </div>
                </el-form-item>
                <template v-if="!loading">
                    <template v-if="isAmap">
                        <div class="title">
                            {{ $t("高德地图设置") }}
                            <!-- <span v-if="isAmap" class="title-tag">{{ $t('当前启用') }}</span> -->
                        </div>
                        <el-form-item v-for="field in amapFields" :key="field.key" :label="field.label" :prop="field.key">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState[field.key] || $t("未配置") }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: field.label, content: formState[field.key] }"
                                        path="setting/config/src/EditSecret"
                                        :title="field.label"
                                        width="600px"
                                        @okCallback="formState[field.key] = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t("编辑") }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ field.tip }}</div>
                            </div>
                        </el-form-item>
                    </template>

                    <template v-if="isTianditu">
                        <div class="title">
                            {{ $t("天地图设置") }}
                            <!-- <span v-if="isTianditu" class="title-tag">{{ $t('当前启用') }}</span> -->
                        </div>
                        <el-form-item v-for="field in tiandituFields" :key="field.key" :label="field.label" :prop="field.key">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState[field.key] || $t("未配置") }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: field.label, content: formState[field.key] }"
                                        path="setting/config/src/EditSecret"
                                        :title="field.label"
                                        width="600px"
                                        @okCallback="formState[field.key] = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t("编辑") }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ field.tip }}</div>
                            </div>
                        </el-form-item>
                    </template>
                </template>
            </a-skeleton>
        </a-spin>
        <!-- <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改；未改动的密钥保持掩码原样即可，不会被覆盖。') }}</div> -->
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { computed, onMounted, ref } from "vue";
import { message, Modal } from "ant-design-vue";
import type { MapSettingsResponse } from "@/types/setting/map";
import { getMapSettings, saveMapSettings } from "@/api/setting/map";
import { MAP_PROVIDER_AMAP, MAP_PROVIDER_TIANDITU, resolveMapProvider } from "@/utils/map/provider";
import { clearRuntimeMapSettingsCache } from "@/utils/map/settings";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const loading = ref(true);

interface SecretField {
    key: keyof MapSettingsResponse;
    label: string;
    tip: string;
}

// amapMiniKey / amapMiniSecret / tiandituMiniTk 不在界面展示，但保留在 formState 中，
// 使保存时按掩码原样回传，后端判定为未改动，不会覆盖库中已有的小程序密钥
const formState = ref<MapSettingsResponse>({
    mapProvider: "",
    amapWebKey: "",
    amapWebSecret: "",
    amapJsKey: "",
    amapJsSecret: "",
    amapMiniKey: "",
    amapMiniSecret: "",
    tiandituWebTk: "",
    tiandituJsTk: "",
    tiandituMiniTk: ""
});

const currentProvider = computed(() => resolveMapProvider(formState.value.mapProvider));
const isAmap = computed(() => currentProvider.value === MAP_PROVIDER_AMAP);
const isTianditu = computed(() => currentProvider.value === MAP_PROVIDER_TIANDITU);

const amapFields = computed<SecretField[]>(() => [
    { key: "amapWebKey", label: "WebKey", tip: t("高德 Web 端 Key，服务端调用") },
    { key: "amapWebSecret", label: "WebSecret", tip: t("高德 Web 端 Secret，服务端调用") },
    { key: "amapJsKey", label: "JsKey", tip: t("高德 JS API Key，浏览器加载地图 SDK 使用") },
    { key: "amapJsSecret", label: "JsSecret", tip: t("高德 JS API Secret，浏览器加载地图 SDK 使用") }
]);

const tiandituFields = computed<SecretField[]>(() => [
    {
        key: "tiandituWebTk",
        label: "WebTk",
        tip: t("天地图服务端 tk，在天地图控制台 console.tianditu.gov.cn 申请；切到天地图后必填，否则定位接口会报未配置天地图密钥")
    },
    { key: "tiandituJsTk", label: "JsTk", tip: t("天地图 JS API tk，浏览器加载地图 SDK 使用") }
]);

onMounted(async () => {
    await loadFilter();
});

const loadFilter = async () => {
    try {
        // 不传 use，拿掩码值回显；未改动时原样回传，后端判定为未改不写库
        const result = await getMapSettings();
        const provider = resolveMapProvider(result.mapProvider);
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
        formState.value.mapProvider = provider;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/** 切换服务商前的坐标偏差二次确认 */
const confirmProviderSwitch = (): Promise<boolean> => {
    return new Promise((resolve) => {
        Modal.confirm({
            title: t("确认切换地图服务商？"),
            content: t(
                "切换地图服务商后，已保存的历史地址与门店坐标不会自动转换，在新地图上显示可能有 50~500 米偏移。请确认前端已同步发布对应的地图 SDK，否则新产生的定位数据也会出现偏差。"
            ),
            okText: t("确定切换"),
            cancelText: t("取消"),
            onOk: () => resolve(true),
            onCancel: () => resolve(false)
        });
    });
};

/** 选择服务商时先确认，确认前不改变表单值，避免配置区闪切 */
const handleProviderChange = async (value: string | number | boolean) => {
    const provider = resolveMapProvider(String(value));
    if (provider === currentProvider.value) return;
    const confirmed = await confirmProviderSwitch();
    if (confirmed) {
        formState.value.mapProvider = provider;
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    const provider = currentProvider.value;
    if (provider === MAP_PROVIDER_TIANDITU && !String(formState.value.tiandituWebTk || "").trim()) {
        message.error(t("请先填写天地图服务端 tk 再切换到天地图"));
        return;
    }
    try {
        await saveMapSettings({ ...formState.value, mapProvider: provider });
        // 服务商或密钥可能已变，清掉运行态缓存，避免同一会话内地图仍按旧配置加载
        clearRuntimeMapSettingsCache();
        message.success(t("修改成功"));
    } catch (error: any) {
        message.error(error.message);
    }
};
defineExpose({ onSubmit });
</script>
<style lang="less" scoped>
.content {
    padding: 0 20px;
}

.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}

.title-tag {
    margin-left: 8px;
    padding: 2px 8px;
    border-radius: 10px;
    background: #e8f3ff;
    color: #2466e9;
    font-size: 12px;
    font-weight: normal;
    vertical-align: middle;
}

.subtitle {
    color: #999;
    font-weight: normal;
    font-size: 12px;
}

.mr8 {
    margin-right: 8px;
}

.width60 {
    width: 60px;
}

.ml8 {
    margin-left: 8px;
}

.itemWidth {
    width: 100%;
}

.error {
    color: red;
}
</style>
