<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <a-spin :spinning="loading">
                    <div class="theme-style-info">
                        <div class="left">
                            <div class="flex title-group">
                                <div class="title">{{ $t('配色方案') }}</div>
                                <a-radio-group class="radio-group" v-model:value="colorMode" @change="onColorModeChange">
                                    <a-radio :value="1">{{ $t('系统配色') }}</a-radio>
                                    <a-radio :value="2">{{ $t('自定义配色') }}</a-radio>
                                </a-radio-group>
                            </div>

                            <!-- 系统配色 -->
                            <div v-if="colorMode === 1" class="color-list">
                                <div
                                    v-for="item in ColorList"
                                    :style="{ border: item.check ? '1px solid #0080f9' : '1px solid transparent' }"
                                    class="color-card"
                                    @click="selectItem(item.themeId)"
                                >
                                    <div :style="{ background: item['--main-bg'] }"></div>
                                    <div :style="{ background: item['--vice-bg'] }"></div>
                                </div>
                            </div>

                            <!-- 自定义配色 -->
                            <div v-if="colorMode === 2" class="custom-color-picker">
                                <div class="color-picker-item">
                                    <label>{{ $t('主题颜色') }}</label>
                                    <div class="colorBox">
                                        <span class="color-value">{{ toHexColor(customMainColor) }}</span>
                                        <SelectColor v-model:color="customMainColor" @update:color="onMainColorChange" />
                                    </div>
                                </div>
                                <div class="color-picker-item">
                                    <label>{{ $t('辅助颜色') }}</label>
                                    <div class="colorBox">
                                        <span class="color-value">{{ toHexColor(customViceColor) }}</span>
                                        <SelectColor v-model:color="customViceColor" @update:color="onViceColorChange" />
                                    </div>
                                </div>
                            </div>

                            <div class="form-btns">
                                <el-button class="form-submit-btn" @click="onImportColorCode">{{ $t('导入配色码') }}</el-button>
                                <el-button :loading="confirmLoading" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('保存') }}</el-button>
                                <el-button v-if="isTemplateExport" class="form-submit-btn" @click="onExport">{{ $t('导出') }}</el-button>
                            </div>
                        </div>
                        <div class="right">
                            <h3>{{ $t('预览效果') }}</h3>
                            <div class="example">
                                <div class="card info">
                                    <div class="info-item1">
                                        <p :style="{ color: formState['--price'] }" class="text">{{ priceFormat(amount) }}</p>
                                    </div>
                                    <div class="info-item2">
                                        <p :style="{ color: formState['--price'] }" class="text">{{ $t('由本系统配送并提供售后服务') }}</p>
                                    </div>
                                    <div class="info-item3">
                                        <div :style="{ color: formState['--vice-text'], background: formState['--vice-bg'] }" class="cu-card">{{ $t('9折') }}</div>
                                        <div :style="{ color: formState['--vice-text'], background: formState['--vice-bg'] }" class="cu-card">{{ $t('满100-10') }}</div>
                                        <div :style="{ color: formState['--vice-text'], background: formState['--vice-bg'] }" class="cu-card">{{ $t('满300-40') }}</div>
                                    </div>
                                    <div class="info-item4">
                                        <p :style="{ color: formState['--price'] }" class="text">{{ $t('商品介绍') }}</p>
                                    </div>
                                    <div class="info-item5">
                                        <p :style="{ color: formState['--main-text'], background: formState['--main-bg'] }" class="text">3</p>
                                    </div>
                                    <div class="info-item6">
                                        <div>
                                            <div :style="{ color: formState['--vice-text'], background: formState['--vice-bg'] }">{{ $t('加入购物车') }}</div>
                                            <div :style="{ color: formState['--main-text'], background: formState['--main-bg'] }">{{ $t('立即购买') }}</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="card buy">
                                    <div class="buy-item1">
                                        <p :style="{ color: formState['--price'] }" class="text">{{ priceFormat(amount) }}</p>
                                    </div>
                                    <div class="buy-item2">
                                        <div :style="{ color: formState['--general'], borderColor: formState['--main-bg'] }" class="cu-card">{{ $t('金色') }}</div>
                                    </div>
                                    <div class="buy-item3">
                                        <div :style="{ color: formState['--general'], borderColor: formState['--main-bg'] }" class="cu-card">XS</div>
                                    </div>
                                    <div class="buy-item4">
                                        <p :style="{ color: '#39bf3e' }" class="text">{{ $t('充足') }}</p>
                                    </div>
                                    <div class="buy-item5">
                                        <div>
                                            <div :style="{ color: formState['--vice-text'], background: formState['--vice-bg'] }">{{ $t('加入购物车') }}</div>
                                            <div :style="{ color: formState['--main-text'], background: formState['--main-bg'] }">{{ $t('立即购买') }}</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="card check">
                                    <div class="check-item1">
                                        <p :style="{ color: '#fff', background: formState['--main-bg'] }" class="text">{{ $t('自营') }}</p>
                                    </div>
                                    <div class="check-item2">
                                        <p :style="{ color: formState['--price'] }" class="text">{{ priceFormat(amount) }}</p>
                                    </div>
                                    <div class="check-item3">
                                        <p :style="{ color: formState['--price'] }" class="text">{{ points }}</p>
                                    </div>
                                    <div class="check-item4">
                                        <p :style="{ color: formState['--price'] }" class="text">{{ balance }}</p>
                                    </div>
                                    <div class="check-item5">
                                        <p :style="{ color: formState['--price'] }" class="text">{{ priceFormat(amountOu) }}</p>
                                    </div>
                                    <div class="check-item6">
                                        <div>
                                            <div :style="{ color: formState['--main-text'], background: formState['--main-bg'] }">{{ $t('提交') }}</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </a-spin>
            </div>
        </div>

        <!-- 导入配色码弹窗 -->
        <el-dialog
            v-model="importVisible"
            :title="$t('导入配色码')"
            width="460px"
            class="import-color-dialog"
            :append-to-body="true"
            :close-on-click-modal="false"
        >
            <div class="import-color-dialog__label">{{ $t('请把配色码粘贴在此处') }}</div>
            <el-input ref="importInputRef" v-model="importColorCode" placeholder="" />
            <template #footer>
                <el-button @click="importVisible = false">{{ $t('取消') }}</el-button>
                <el-button type="primary" :loading="importLoading" @click="confirmImportColorCode">{{ $t('确定') }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { nextTick, onMounted, ref, watch } from "vue";
import { message } from "ant-design-vue";
import type { ColorList, ThemeStyleFormState } from "@/types/decorate/themeStyle.d";
import { getConfigThemeStyle, saveConfigThemeStyle } from "@/api/setting/config";
import { getTemplateDetail } from "@/api/decorate/mobileDecorate";
import { priceFormat } from "@/utils/format";
import { copyDomText } from "@/utils/util";
import SelectColor from "@/components/select/src/SelectColor.vue";

// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
// 演示环境标识：控制「导出」入口显隐
const isTemplateExport = import.meta.env.VITE_IS_TEMPLATE_EXPORT || 0;
const confirmLoading = ref(false);
const amount = ref(395);
const points = ref(6666);
const balance = ref(29999.0);
const amountOu = ref(231.1);
const loading = ref<boolean>(true);
const formState = ref<ThemeStyleFormState>({
    themeId: 0
});
// 配色模式：1-系统配色，2-自定义配色
const colorMode = ref<number>(1);
const customMainColor = ref<string>("#4a90e2");
const customViceColor = ref<string>("#D6E9FC");
// 导入配色码弹窗
const importVisible = ref<boolean>(false);
const importColorCode = ref<string>("");
const importInputRef = ref();
const importLoading = ref<boolean>(false);

watch(importVisible, (val) => {
    if (val) {
        nextTick(() => {
            importInputRef.value?.focus();
        });
    }
});
const ColorList = ref<ColorList[]>([
    {
        check: false,
        themeId: 1,
        "--general": "#4a90e2",
        "--main-bg": "#4a90e2",
        "--main-bg-gradient": "#4a90e2",
        "--main-text": "#ffffff",
        "--vice-bg": "#D6E9FC",
        "--vice-text": "#0080FF",
        "--icon": "#0080FF",
        "--price": "#0080FF",
        "--tag-text": "#0080FF",
        "--tag-bg": "#E5F2FF",
        "--primary-light-3": "#4a90e2db",
        "--primary-light-5": "#a5c8f1",
        "--primary-light-7": "#c9def6",
        "--primary-light-8": "#dbe9f9",
        "--primary-light-9": "#edf4fc",
        "--primary-dark-2": "#3b73b5",
        "--ump-main-bg": "#4a90e2",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#D6E9FC",
        "--ump-vice-text": "#0080FF",
        "--ump-icon": "#0080FF",
        "--ump-price": "#0080FF",
        "--ump-tag-text": "#0080FF",
        "--ump-tag-bg": "#E5F2FF",
        "--ump-coupon-bg": "#F2F8FF",
        "--ump-border": "#CCE4FF",
        "--ump-start-bg": "#00B8FF",
        "--ump-end-bg": "#0078EF"
    },
    {
        check: false,
        themeId: 2,
        "--general": "#ff4444",
        "--main-bg": "#ff4444",
        "--main-bg-gradient": "#ff4444",
        "--main-text": "#ffffff",
        "--vice-bg": "#ff8855",
        "--vice-text": "#fff",
        "--icon": "#FC0000",
        "--price": "#FC0000",
        "--tag-text": "#CF0000",
        "--tag-bg": "#FFF2F2",
        "--primary-light-3": "#ff7c7c",
        "--primary-light-5": "#ffa2a2",
        "--primary-light-7": "#ffc7c7",
        "--primary-light-8": "#ffdada",
        "--primary-light-9": "#ffecec",
        "--primary-dark-2": "#cc3636",
        "--ump-main-bg": "#ff4444",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#ff8855",
        "--ump-vice-text": "#fff",
        "--ump-icon": "#FC0000",
        "--ump-price": "#FC0000",
        "--ump-tag-text": "#CF0000",
        "--ump-tag-bg": "#FFF2F2",
        "--ump-coupon-bg": "#FFF2F2",
        "--ump-border": "#FFCCCC",
        "--ump-start-bg": "#F86F30",
        "--ump-end-bg": "#E91717"
    },
    {
        check: false,
        themeId: 3,
        "--general": "#ff5e15",
        "--main-bg": "#ff5e15",
        "--main-bg-gradient": "#ff5e15",
        "--main-text": "#ffffff",
        "--vice-bg": "#FF9300",
        "--vice-text": "#ffffff",
        "--icon": "#ff5e15",
        "--price": "#ff5e15",
        "--tag-text": "#ff5e15",
        "--tag-bg": "#FFEDE6",
        "--primary-light-3": "#ff8e5b",
        "--primary-light-5": "#ffaf8a",
        "--primary-light-7": "#ffcfb9",
        "--primary-light-8": "#ffdfd0",
        "--primary-light-9": "#ffefe8",
        "--primary-dark-2": "#cc4b11",
        "--ump-main-bg": "#ff5e15",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#FF9300",
        "--ump-vice-text": "#ffffff",
        "--ump-icon": "#ff5e15",
        "--ump-price": "#ff5e15",
        "--ump-tag-text": "#ff5e15",
        "--ump-tag-bg": "#FFEDE6",
        "--ump-coupon-bg": "#FFF6F2",
        "--ump-border": "#FFDCCC",
        "--ump-start-bg": "#FF8C20",
        "--ump-end-bg": "#FF4300"
    },
    {
        check: false,
        themeId: 4,
        "--general": "#ff547b",
        "--main-bg": "#ff547b",
        "--main-bg-gradient": "#ff547b",
        "--main-text": "#ffffff",
        "--vice-bg": "#FFE6E8",
        "--vice-text": "#ff547b",
        "--icon": "#ff547b",
        "--price": "#ff547b",
        "--tag-text": "#ff547b",
        "--tag-bg": "#FFF2F5",
        "--primary-light-3": "#ff87a3",
        "--primary-light-5": "#ffaabd",
        "--primary-light-7": "#ffccd7",
        "--primary-light-8": "#ffdde5",
        "--primary-light-9": "#ffeef2",
        "--primary-dark-2": "#cc4362",
        "--ump-main-bg": "#ff547b",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#FFE6E8",
        "--ump-vice-text": "#ff547b",
        "--ump-icon": "#ff547b",
        "--ump-price": "#ff547b",
        "--ump-tag-text": "#ff547b",
        "--ump-tag-bg": "#FFF2F5",
        "--ump-coupon-bg": "#FFF2F5",
        "--ump-border": "#FFCCD8",
        "--ump-start-bg": "#FF44A3",
        "--ump-end-bg": "#FF4063"
    },
    {
        check: false,
        themeId: 5,
        "--general": "#FF4444",
        "--main-bg": "#FF4444",
        "--main-bg-gradient": "#FF4444",
        "--main-text": "#ffffff",
        "--vice-bg": "#555555",
        "--vice-text": "#ffffff",
        "--icon": "#FC0000",
        "--price": "#FC0000",
        "--tag-text": "#CF0000",
        "--tag-bg": "#FFF2F2",
        "--primary-light-3": "#ff7c7c",
        "--primary-light-5": "#ffa2a2",
        "--primary-light-7": "#ffc7c7",
        "--primary-light-8": "#ffdada",
        "--primary-light-9": "#ffecec",
        "--primary-dark-2": "#cc3636",
        "--ump-main-bg": "#FF4444",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#555555",
        "--ump-vice-text": "#ffffff",
        "--ump-icon": "#FC0000",
        "--ump-price": "#FC0000",
        "--ump-tag-text": "#CF0000",
        "--ump-tag-bg": "#FFF2F2",
        "--ump-coupon-bg": "#FFF2F2",
        "--ump-border": "#FFCCCC",
        "--ump-start-bg": "#FF8544",
        "--ump-end-bg": "#FF2E2E"
    },
    {
        check: false,
        themeId: 6,
        "--general": "#FCC600",
        "--main-bg": "#FCC600",
        "--main-bg-gradient": "#FCC600",
        "--main-text": "#ffffff",
        "--vice-bg": "#1D262E",
        "--vice-text": "#ffffff",
        "--icon": "#FCC600",
        "--price": "#FCC600",
        "--tag-text": "#FCC600",
        "--tag-bg": "#FFF4CD",
        "--primary-light-3": "#fdd74d",
        "--primary-light-5": "#fee380",
        "--primary-light-7": "#feeeb3",
        "--primary-light-8": "#fef4cc",
        "--primary-light-9": "#fff9e6",
        "--primary-dark-2": "#ca9e00",
        "--ump-main-bg": "#FCC600",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#1D262E",
        "--ump-vice-text": "#ffffff",
        "--ump-icon": "#FCC600",
        "--ump-price": "#FCC600",
        "--ump-tag-text": "#FCC600",
        "--ump-tag-bg": "#FFF4CD",
        "--ump-coupon-bg": "#FFFCF2",
        "--ump-border": "#FFF3CC",
        "--ump-start-bg": "#FF5B00",
        "--ump-end-bg": "#FFBF25"
    },
    {
        check: false,
        themeId: 7,
        "--general": "#65c4aa",
        "--main-bg": "#65c4aa",
        "--main-bg-gradient": "#65c4aa",
        "--main-text": "#ffffff",
        "--vice-bg": "#D9F6EF",
        "--vice-text": "#65c4aa",
        "--icon": "#65c4aa",
        "--price": "#65c4aa",
        "--tag-text": "#65c4aa",
        "--tag-bg": "#F2FFFC",
        "--primary-light-3": "#93d6c4",
        "--primary-light-5": "#b2e2d5",
        "--primary-light-7": "#d1ede6",
        "--primary-light-8": "#e0f3ee",
        "--primary-light-9": "#f0f9f7",
        "--primary-dark-2": "#519d88",
        "--ump-main-bg": "#65c4aa",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#D9F6EF",
        "--ump-vice-text": "#65c4aa",
        "--ump-icon": "#65c4aa",
        "--ump-price": "#65c4aa",
        "--ump-tag-text": "#65c4aa",
        "--ump-tag-bg": "#F2FFFC",
        "--ump-coupon-bg": "#FFFCF2",
        "--ump-border": "#CCFFF3",
        "--ump-start-bg": "#00D1C9",
        "--ump-end-bg": "#00BB87"
    },
    {
        check: false,
        themeId: 8,
        "--general": "#09bb07",
        "--main-bg": "#09bb07",
        "--main-bg-gradient": "#09bb07",
        "--main-text": "#ffffff",
        "--vice-bg": "#383838",
        "--vice-text": "#ffffff",
        "--icon": "#09BB07",
        "--price": "#09BB07",
        "--tag-text": "#09BB07",
        "--tag-bg": "#E6F8E6",
        "--primary-light-3": "#53cf51",
        "--primary-light-5": "#84dd83",
        "--primary-light-7": "#b5ebb5",
        "--primary-light-8": "#cef1cd",
        "--primary-light-9": "#e6f8e6",
        "--primary-dark-2": "#079606",
        "--ump-main-bg": "#09bb07",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#383838",
        "--ump-vice-text": "#ffffff",
        "--ump-icon": "#09BB07",
        "--ump-price": "#09BB07",
        "--ump-tag-text": "#09BB07",
        "--ump-tag-bg": "#E6F8E6",
        "--ump-coupon-bg": "#F2FFF2",
        "--ump-border": "#CCFFCC",
        "--ump-start-bg": "#10C80E",
        "--ump-end-bg": "#00A630"
    },
    {
        check: false,
        themeId: 9,
        "--general": "#63be72",
        "--main-bg": "#63be72",
        "--main-bg-gradient": "#63be72",
        "--main-text": "#ffffff",
        "--vice-bg": "#E1F4E3",
        "--vice-text": "#6CBE72",
        "--icon": "#6CBE72",
        "--price": "#6CBE72",
        "--tag-text": "#6CBE72",
        "--tag-bg": "#F0F8F0",
        "--primary-light-3": "#92d29c",
        "--primary-light-5": "#b1dfb9",
        "--primary-light-7": "#d0ecd5",
        "--primary-light-8": "#e0f2e3",
        "--primary-light-9": "#eff9f1",
        "--primary-dark-2": "#4f985b",
        "--ump-main-bg": "#63be72",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#E1F4E3",
        "--ump-vice-text": "#6CBE72",
        "--ump-icon": "#6CBE72",
        "--ump-price": "#6CBE72",
        "--ump-tag-text": "#6CBE72",
        "--ump-tag-bg": "#F0F8F0",
        "--ump-coupon-bg": "#F2FFF4",
        "--ump-border": "#CCFFD4",
        "--ump-start-bg": "#57D8AE",
        "--ump-end-bg": "#4CBA54"
    },
    {
        check: false,
        themeId: 10,
        "--general": "#c3a769",
        "--main-bg": "#c3a769",
        "--main-bg-gradient": "#c3a769",
        "--main-text": "#ffffff",
        "--vice-bg": "#F3EEE1",
        "--vice-text": "#C3A769",
        "--icon": "#C3A769",
        "--price": "#C3A769",
        "--tag-text": "#C3A769",
        "--tag-bg": "#F9F6F0",
        "--primary-light-3": "#d5c196",
        "--primary-light-5": "#e1d3b4",
        "--primary-light-7": "#ede5d2",
        "--primary-light-8": "#f3ede1",
        "--primary-light-9": "#f9f6f0",
        "--primary-dark-2": "#9c8654",
        "--ump-main-bg": "#c3a769",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#F3EEE1",
        "--ump-vice-text": "#C3A769",
        "--ump-icon": "#C3A769",
        "--ump-price": "#C3A769",
        "--ump-tag-text": "#C3A769",
        "--ump-tag-bg": "#F9F6F0",
        "--ump-coupon-bg": "#FFFBF2",
        "--ump-border": "#FFEFCC",
        "--ump-start-bg": "#B18836",
        "--ump-end-bg": "#DEB155"
    },
    {
        check: false,
        themeId: 11,
        "--general": "#2f2f34",
        "--main-bg": "#2f2f34",
        "--main-bg-gradient": "#2f2f34",
        "--main-text": "#ffffff",
        "--vice-bg": "#EBECF2",
        "--vice-text": "#2F2F34",
        "--icon": "#2F2F34",
        "--price": "#2F2F34",
        "--tag-text": "#2F2F34",
        "--tag-bg": "#EAEAEA",
        "--primary-light-3": "#6d6d71",
        "--primary-light-5": "#97979a",
        "--primary-light-7": "#c1c1c2",
        "--primary-light-8": "#d5d5d6",
        "--primary-light-9": "#eaeaeb",
        "--primary-dark-2": "#26262a",
        "--ump-main-bg": "#2f2f34",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#EBECF2",
        "--ump-vice-text": "#2F2F34",
        "--ump-icon": "#2F2F34",
        "--ump-price": "#2F2F34",
        "--ump-tag-text": "#2F2F34",
        "--ump-tag-bg": "#EAEAEA",
        "--ump-coupon-bg": "#F2F2FF",
        "--ump-border": "#CCCCFF",
        "--ump-start-bg": "#383D46",
        "--ump-end-bg": "#2E333D"
    },
    {
        check: false,
        themeId: 12,
        "--general": "#884cff",
        "--main-bg": "#884cff",
        "--main-bg-gradient": "#884cff",
        "--main-text": "#ffffff",
        "--vice-bg": "#EFE6FF",
        "--vice-text": "#884cff",
        "--icon": "#884cff",
        "--price": "#884cff",
        "--tag-text": "#884cff",
        "--tag-bg": "#F3EDFF",
        "--primary-light-3": "#ac82ff",
        "--primary-light-5": "#c4a6ff",
        "--primary-light-7": "#dbc9ff",
        "--primary-light-8": "#e7dbff",
        "--primary-light-9": "#f3edff",
        "--primary-dark-2": "#6d3dcc",
        "--ump-main-bg": "#884cff",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "#EFE6FF",
        "--ump-vice-text": "#884cff",
        "--ump-icon": "#884cff",
        "--ump-price": "#884cff",
        "--ump-tag-text": "#884cff",
        "--ump-tag-bg": "#F3EDFF",
        "--ump-coupon-bg": "#F7F2FF",
        "--ump-border": "#E0CCFF",
        "--ump-start-bg": "#CF4BF6",
        "--ump-end-bg": "#5F35EB"
    },
    {
        check: false,
        themeId: 13,
        "--general": "#EE0A24",
        "--main-bg": "#EE0A24",
        "--main-bg-gradient": "#EE0A24",
        "--main-text": "#ffffff",
        "--vice-bg": "#FF9518FF",
        "--vice-text": "#ffffff",
        "--icon": "#EE0A24",
        "--price": "#EE0A24",
        "--tag-text": "#EE0A24",
        "--tag-bg": "#FDE6E9",
        "--primary-light-3": "#f35466",
        "--primary-light-5": "#f78592",
        "--primary-light-7": "#fab6bd",
        "--primary-light-8": "#fcced3",
        "--primary-light-9": "#fde7e9",
        "--primary-dark-2": "#be081d",
        "--ump-main-bg": "#EE0A24",
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": "linear-gradient(to right, #FFD01E, #FF8917)",
        "--ump-vice-text": "#ffffff",
        "--ump-icon": "#EE0A24",
        "--ump-price": "#EE0A24",
        "--ump-tag-text": "#EE0A24",
        "--ump-tag-bg": "#FDE6E9",
        "--ump-coupon-bg": "#FFF2F4",
        "--ump-border": "#FCCED3",
        "--ump-start-bg": "#FF6034",
        "--ump-end-bg": "#EE0A24"
    }
]);
const selectItem = (themeId: number) => {
    ColorList.value.forEach((item) => {
        // 如果当前项是被点击的项，则将其 check 设置为 true
        // 否则，将其他项的 check 设置为 false
        if (item.themeId === themeId) {
            Object.assign(formState.value, item);
            item.check = true;
        } else {
            item.check = false;
        }
    });
};

// 解析颜色为RGBA对象（支持hex、rgb、rgba格式）
const parseColor = (color: string): { r: number; g: number; b: number; a: number } | null => {
    // 处理十六进制格式 #RRGGBB 或 #RRGGBBAA
    const hexMatch = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})?$/i.exec(color);
    if (hexMatch) {
        return {
            r: parseInt(hexMatch[1], 16),
            g: parseInt(hexMatch[2], 16),
            b: parseInt(hexMatch[3], 16),
            a: hexMatch[4] ? parseInt(hexMatch[4], 16) / 255 : 1
        };
    }

    // 处理rgb()或rgba()格式
    const rgbaMatch = /rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*([\d.]+)\s*)?\)/i.exec(color);
    if (rgbaMatch) {
        return {
            r: parseInt(rgbaMatch[1]),
            g: parseInt(rgbaMatch[2]),
            b: parseInt(rgbaMatch[3]),
            a: rgbaMatch[4] ? parseFloat(rgbaMatch[4]) : 1
        };
    }

    return null;
};

// 将RGBA转换为字符串（如果有透明度用rgba，否则用hex）
const colorToString = (r: number, g: number, b: number, a: number = 1): string => {
    if (a < 1) {
        // 有透明度，返回rgba格式
        return `rgba(${r}, ${g}, ${b}, ${a.toFixed(2)})`;
    }
    // 无透明度，返回hex格式
    return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
};

// 将任意颜色格式转换为16进制格式（用于显示）
const toHexColor = (color: string): string => {
    if (!color) return "";

    const rgba = parseColor(color);
    if (!rgba) return color;

    // 如果有透明度，显示8位hex格式 #RRGGBBAA
    if (rgba.a < 1) {
        const hex = ((1 << 24) + (rgba.r << 16) + (rgba.g << 8) + rgba.b).toString(16).slice(1);
        const alpha = Math.round(rgba.a * 255)
            .toString(16)
            .padStart(2, "0");
        return `#${hex}${alpha}`.toUpperCase();
    }

    // 无透明度，返回6位hex格式 #RRGGBB
    return "#" + ((1 << 24) + (rgba.r << 16) + (rgba.g << 8) + rgba.b).toString(16).slice(1).toUpperCase();
};

// 调整颜色亮度（保留透明度）
const adjustBrightness = (color: string, percent: number): string => {
    const rgba = parseColor(color);
    if (!rgba) return color;

    const r = Math.min(255, Math.max(0, Math.round(rgba.r + (255 - rgba.r) * percent)));
    const g = Math.min(255, Math.max(0, Math.round(rgba.g + (255 - rgba.g) * percent)));
    const b = Math.min(255, Math.max(0, Math.round(rgba.b + (255 - rgba.b) * percent)));

    return colorToString(r, g, b, rgba.a);
};

// 调整颜色暗度（保留透明度）
const adjustDarkness = (color: string, percent: number): string => {
    const rgba = parseColor(color);
    if (!rgba) return color;

    const r = Math.min(255, Math.max(0, Math.round(rgba.r * (1 - percent))));
    const g = Math.min(255, Math.max(0, Math.round(rgba.g * (1 - percent))));
    const b = Math.min(255, Math.max(0, Math.round(rgba.b * (1 - percent))));

    return colorToString(r, g, b, rgba.a);
};

// 根据主题颜色和辅助颜色生成完整的配色方案
const generateColorScheme = (mainColor: string, viceColor: string) => {
    return {
        themeId: 0, // 自定义配色
        "--general": mainColor,
        "--main-bg": mainColor,
        "--main-bg-gradient": mainColor,
        "--main-text": "#ffffff",
        "--vice-bg": viceColor,
        "--vice-text": mainColor,
        "--icon": mainColor,
        "--price": mainColor,
        "--tag-text": mainColor,
        "--tag-bg": adjustBrightness(mainColor, 0.9),
        "--primary-light-3": adjustBrightness(mainColor, 0.3),
        "--primary-light-5": adjustBrightness(mainColor, 0.5),
        "--primary-light-7": adjustBrightness(mainColor, 0.7),
        "--primary-light-8": adjustBrightness(mainColor, 0.8),
        "--primary-light-9": adjustBrightness(mainColor, 0.9),
        "--primary-dark-2": adjustDarkness(mainColor, 0.2),
        "--ump-main-bg": mainColor,
        "--ump-main-text": "#ffffff",
        "--ump-vice-bg": viceColor,
        "--ump-vice-text": mainColor,
        "--ump-icon": mainColor,
        "--ump-price": mainColor,
        "--ump-tag-text": mainColor,
        "--ump-tag-bg": adjustBrightness(mainColor, 0.9),
        "--ump-coupon-bg": adjustBrightness(mainColor, 0.95),
        "--ump-border": adjustBrightness(mainColor, 0.8),
        "--ump-start-bg": adjustBrightness(mainColor, 0.2),
        "--ump-end-bg": adjustDarkness(mainColor, 0.1)
    };
};

// 主题颜色变化时
const onMainColorChange = (color: string) => {
    customMainColor.value = color;
    // 只有当主题颜色和辅助颜色都存在时才更新配色方案
    if (customMainColor.value && customViceColor.value) {
        updateCustomColorScheme();
    }
};

// 辅助颜色变化时
const onViceColorChange = (color: string) => {
    customViceColor.value = color;
    // 只有当主题颜色和辅助颜色都存在时才更新配色方案
    if (customMainColor.value && customViceColor.value) {
        updateCustomColorScheme();
    }
};

// 更新自定义配色方案（根据主题色和辅助色生成完整配色规则）
const updateCustomColorScheme = () => {
    const colorScheme = generateColorScheme(customMainColor.value, customViceColor.value);
    Object.assign(formState.value, colorScheme);
};

// 配色模式变化时
const onColorModeChange = () => {
    if (colorMode.value === 2) {
        // 切换到自定义配色，应用当前选择的颜色
        if (customMainColor.value && customViceColor.value) {
            updateCustomColorScheme();
        } else {
            // 如果没有自定义颜色，使用默认值
            customMainColor.value = customMainColor.value || "#4a90e2";
            customViceColor.value = customViceColor.value || "#D6E9FC";
            updateCustomColorScheme();
        }
    } else {
        // 切换到系统配色，应用当前选中的主题，如果没有选中则选择第一个
        const checkedItem = ColorList.value.find((item) => item.check);
        if (checkedItem) {
            // 重新应用已选中的主题
            selectItem(checkedItem.themeId);
        } else {
            // 没有选中的，默认选择第一个
            selectItem(1);
        }
    }
};

const loadFilter = async () => {
    try {
        const result = await getConfigThemeStyle();
        formState.value = JSON.parse(result.themeStyle as any);

        // 判断是系统配色还是自定义配色
        if (formState.value.themeId === 0) {
            // 自定义配色
            colorMode.value = 2;
            customMainColor.value = formState.value["--main-bg"] || "#4a90e2";
            customViceColor.value = formState.value["--vice-bg"] || "#D6E9FC";
        } else {
            // 系统配色
            colorMode.value = 1;
            selectItem(Number(formState.value.themeId));
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    // 获取详情数据
    loadFilter();
});

// 表单通过验证后提交
const onSubmit = async () => {
    try {
        emit("update:confirmLoading", true);

        // 如果是自定义配色，确保生成完整的配色方案
        if (colorMode.value === 2) {
            const colorScheme = generateColorScheme(customMainColor.value, customViceColor.value);
            Object.assign(formState.value, colorScheme);
        }

        delete formState.value.check;
        const result = await saveConfigThemeStyle({ themeStyle: JSON.stringify(formState.value) });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

/** 导出当前主题配色 JSON（仅演示环境，复制到剪贴板） */
const onExport = async () => {
    try {
        const exportData: ThemeStyleFormState = { ...formState.value };
        // 移除非配色的 UI 状态字段，保证导出结果可直接复用
        delete exportData.check;
        const jsonText = JSON.stringify(exportData, null, 2);
        try {
            await navigator.clipboard.writeText(jsonText);
        } catch {
            copyDomText(jsonText);
        }
        message.success(t('主题配色JSON已复制到剪贴板'));
    } catch (error: any) {
        message.error(error.message);
    }
};

/** 打开导入配色码弹窗 */
const onImportColorCode = () => {
    importColorCode.value = "";
    importVisible.value = true;
};

/**
 * 应用配色码：配色码即 theme 主题色模板的 sn，调详情接口取该页 json（与添加导航/页面的 onUseTemplate 一致）。
 * theme 页 detail 返回 item.colors=[主背景, 辅助背景]，回退取 json 的 --main-bg / --vice-bg。
 */
const confirmImportColorCode = async () => {
    const sn = importColorCode.value.trim();
    if (!sn) {
        message.warning(t('请输入配色码'));
        return;
    }
    importLoading.value = true;
    try {
        const res: any = await getTemplateDetail(sn);
        const item = res?.item ?? {};
        let mainColor = "";
        let viceColor = "";
        // theme 主题色页专用：colors = [--main-bg, --vice-bg]，已由服务端提取
        if (Array.isArray(item.colors) && item.colors.length) {
            mainColor = item.colors[0] || "";
            viceColor = item.colors[1] || "";
        }
        // 回退：从该页装修 json 的 CSS 变量取主/辅背景色
        if (!mainColor) {
            let json: any = item.json;
            if (!json && item.content) {
                try {
                    json = JSON.parse(item.content);
                } catch {
                    json = {};
                }
            }
            json = json || {};
            mainColor = json["--main-bg"] || "";
            viceColor = json["--vice-bg"] || "";
        }
        if (!mainColor) {
            message.error(t('该配色码未返回有效配色'));
            return;
        }
        // 填充主题色/辅助色，并切换到自定义配色
        customMainColor.value = mainColor;
        if (viceColor) customViceColor.value = viceColor;
        colorMode.value = 2;
        updateCustomColorScheme();
        importVisible.value = false;
        message.success(t('导入成功'));
    } catch (error: any) {
        message.error(error?.message || t('配色码无效'));
    } finally {
        importLoading.value = false;
    }
};

// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.theme-style-info {
    display: flex;
    flex-direction: row;
    gap: 20px;

    .left {
        width: 30%;
        padding: 0 20px;
        box-sizing: border-box;
        border-right: 1px solid #e0e0e0;
        .title-group {
            display: flex;
            align-items: center;

            .title {
                width: 20%;
                margin-right: 10px;
            }
        }

        .form-btns {
            display: flex;
            align-items: center;
        }

        .form-submit-btn {
            margin-top: 50px;
            padding: 0 25px;
            height: 40px;
            font-size: 14px;
        }

        .color-list {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 15px;

            .color-card {
                display: flex;
                box-sizing: border-box;
                padding: 2px;
                cursor: pointer;

                & > div {
                    height: 24px;
                    width: 24px;
                    flex: 1;
                }
            }
        }

        .custom-color-picker {
            margin-top: 20px;
            margin-left: calc(20% + 10px);

            .color-picker-item {
                display: flex;
                align-items: center;
                margin-bottom: 20px;
                gap: 10px;
                .colorBox {
                    width: 60%;
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    border: 1px solid #e0e0e0;
                    padding: 3px;
                    border-radius: 4px;
                }

                label {
                    min-width: 80px;
                    font-size: 14px;
                    color: #333;
                }

                .color-value {
                    font-size: 14px;
                    color: #000;
                    font-family: monospace;
                }
            }
        }
    }

    .right {
        flex: 1;
        padding: 0 20px;

        .example {
            margin-top: 10px;
            display: flex;
            flex-direction: row;
            gap: 10px;
            flex-wrap: wrap;
            white-space: nowrap;

            .bg-card {
                font-size: 12px;
                display: inline-block;
                height: 12px;
                padding: 3px;
                border-radius: 12px;
                transform: scale(0.6667);
                transform-origin: top left;
            }

            .card {
                width: 225px;
                height: 460px;
                border: 0.1px solid #e0e0e0;
                position: relative; /* 设置父容器为相对定位 */
            }

            .info {
                background-image: url("@/style/images/decorate/themeStyle/temp1.jpg");
                background-position: center center;
                background-size: cover;

                .info-item1 {
                    position: absolute;
                    left: 9px;
                    top: 245px;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    transform: scale(0.8333);
                    transform-origin: top left;

                    .text {
                        font-size: 12px;
                        font-weight: bold;
                    }
                }

                .info-item2 {
                    position: absolute;
                    left: 40px;
                    top: 350px;
                    display: flex;
                    overflow: hidden;
                    transform: scale(0.5);
                    transform-origin: top left;

                    .text {
                        font-size: 12px;
                        font-weight: 500;
                    }
                }

                .info-item3 {
                    position: absolute;
                    left: 32px;
                    top: 385px;
                    display: flex;
                    overflow: hidden;
                    gap: 8px;
                    transform: scale(0.5);
                    transform-origin: top left;

                    .cu-card {
                        padding: 4px 12px;
                        font-size: 12px;
                    }
                }

                .info-item4 {
                    position: absolute;
                    left: 40px;
                    top: 418px;
                    display: flex;
                    overflow: hidden;
                    transform: scale(0.625);
                    transform-origin: top left;

                    .text {
                        font-size: 12px;
                        font-weight: 500;
                    }
                }

                .info-item5 {
                    position: absolute;
                    left: 54px;
                    top: 433px;
                    display: flex;
                    overflow: hidden;
                    transform: scale(0.5833);
                    transform-origin: top left;

                    .text {
                        padding: 0 1.71px;
                        border-radius: 50%;
                        font-size: 12px;
                        font-weight: 500;
                    }
                }

                .info-item6 {
                    position: absolute;
                    bottom: 4px;
                    right: 4px;
                    display: flex;
                    border-radius: 27px;
                    overflow: hidden;
                    white-space: nowrap;
                    transform: scale(0.6667);
                    transform-origin: bottom right;

                    & > div {
                        display: flex;

                        & > div {
                            padding: 9px 30px;
                            font-size: 12px;
                        }
                    }
                }
            }

            .buy {
                background: url("@/style/images/decorate/themeStyle/temp2.jpg") center center;
                background-size: cover;

                .buy-item1 {
                    position: absolute;
                    left: 66px;
                    top: 176px;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    transform: scale(0.8333);
                    transform-origin: top left;

                    .text {
                        font-size: 12px;
                        font-weight: bold;
                    }
                }

                .buy-item2 {
                    position: absolute;
                    left: 76px;
                    top: 267px;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    transform: scale(0.6417);
                    transform-origin: top left;

                    & > div {
                        border: 0.78px solid;
                        padding: 5.45px 9.35px;
                        font-size: 12px;
                        border-radius: 3.12px;
                        font-weight: 500;
                        background-color: #f7f8fa;
                    }
                }

                .buy-item3 {
                    position: absolute;
                    left: 9px;
                    top: 360.5px;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    transform: scale(0.6417);
                    transform-origin: top left;

                    & > div {
                        border: 0.78px solid;
                        padding: 5.45px 9.35px;
                        font-size: 12px;
                        border-radius: 3.12px;
                        font-weight: 500;
                        background-color: #f7f8fa;
                    }
                }

                .buy-item4 {
                    position: absolute;
                    right: 5px;
                    top: 398px;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    transform: scale(0.5833);
                    transform-origin: top right;

                    .text {
                        font-size: 12px;
                        font-weight: bold;
                    }
                }

                .buy-item5 {
                    position: absolute;
                    bottom: 4px;
                    right: 2px;
                    display: flex;
                    border-radius: 27px;
                    overflow: hidden;
                    white-space: nowrap;
                    transform: scale(0.6667);
                    transform-origin: bottom right;

                    & > div {
                        display: flex;

                        & > div {
                            padding: 9px 55.5px;
                            font-size: 12px;
                        }
                    }
                }
            }

            .check {
                background: url("@/style/images/decorate/themeStyle/temp3.jpg") center center;
                background-size: cover;

                .check-item1 {
                    position: absolute;
                    left: 5px;
                    top: 102px;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    transform: scale(0.5833);
                    transform-origin: top left;

                    .text {
                        padding: 1.71px 3.43px;
                        border-radius: 3.43px;
                        font-size: 12px;
                    }
                }

                .check-item2 {
                    position: absolute;
                    left: 58px;
                    top: 155.5px;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    transform: scale(0.6667);
                    transform-origin: top left;

                    .text {
                        font-size: 12px;
                    }
                }

                .check-item3 {
                    position: absolute;
                    left: 74px;
                    top: 254.5px;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    transform: scale(0.6667);
                    transform-origin: top left;

                    .text {
                        font-size: 12px;
                    }
                }

                .check-item4 {
                    position: absolute;
                    left: 74px;
                    top: 277.5px;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    transform: scale(0.6667);
                    transform-origin: top left;

                    .text {
                        font-size: 12px;
                    }
                }

                .check-item5 {
                    position: absolute;
                    left: 6px;
                    bottom: 6px;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    transform: scale(0.8333);
                    transform-origin: bottom left;

                    .text {
                        font-size: 12px;
                        font-weight: bold;
                    }
                }

                .check-item6 {
                    position: absolute;
                    right: 8px;
                    bottom: 3px;
                    display: flex;
                    border-radius: 27px;
                    overflow: hidden;
                    white-space: nowrap;
                    transform: scale(0.6667);
                    transform-origin: bottom right;

                    & > div {
                        display: flex;

                        & > div {
                            padding: 9px 30px;
                            font-size: 12px;
                        }
                    }
                }
            }
        }
    }
}
</style>

<!-- 导入配色码弹窗：el-dialog 会 teleport 到 body，scoped 样式无法命中其内部结构，故用独立的非 scoped 样式块按 class 隔离还原设计稿 -->
<style lang="less">
.import-color-dialog {
    border-radius: 4px !important;
    overflow: hidden;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08) !important;
    border: none !important;

    .el-dialog__header {
        margin-right: 0;
        padding: 0 0;
        border-bottom: none !important;
    }

    .el-dialog__title {
        font-size: 18px;
        font-weight: 600;
        line-height: 24px;
        color: #1d2129;
    }

    .el-dialog__headerbtn {
        top: 20px;
        right: 20px;
        width: 24px;
        height: 24px;
        font-size: 16px;

        .el-dialog__close {
            font-size: 16px;
            color: #4e5969;
            
            &:hover {
                color: #1d2129;
            }
        }
    }

    .el-dialog__body {
        padding: 0;
        border-bottom: none;
    }

    .import-color-dialog__label {
        margin-top: 24px;
        margin-bottom: 10px;
        font-size: 14px;
        line-height: 20px;
        color: #1d2129;
    }

    .el-input {
        width: 100% !important;
        
        .el-input__wrapper {
            width: 100% !important;
            box-sizing: border-box;
            height: 36px;
            border-radius: 4px;
            box-shadow: 0 0 0 1px #dcdfe6 inset !important;
            background-color: #fff;
            padding: 0 12px;
            
            &:hover {
                box-shadow: 0 0 0 1px #c0c4cc inset !important;
            }
            
            &.is-focus {
                box-shadow: 0 0 0 1px #165dff inset !important;
            }
        }
        
        .el-input__inner {
            height: 34px;
            font-size: 14px;
            color: #1d2129;
        }
    }

    .el-dialog__footer {
        padding: 0;
        margin-top: 24px;
        text-align: right;
        box-sizing: border-box;
        border-top: none;
        
        .el-button {
            min-width: 72px;
            height: 32px;
            padding: 0 16px;
            border-radius: 4px;
            font-size: 14px;
            border-color: #e5e6eb;
            color: #4e5969;
            background-color: #fff;
            
            &:hover {
                color: #1d2129;
                border-color: #c0c4cc;
                background-color: #f5f7fa;
            }
            
            &--primary {
                background-color: #165dff !important;
                border-color: #165dff !important;
                color: #fff !important;
                
                &:hover {
                    background-color: #3e7fff !important;
                    border-color: #3e7fff !important;
                    color: #fff !important;
                }
            }
            
            & + .el-button {
                margin-left: 12px;
            }
        }
    }
}
</style>
