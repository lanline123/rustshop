<template>
    <div class="container">
        <div class="content_wrapper">
            <a-spin :spinning="loading">
                <div class="tit mode-tit" v-if="!loading">
                    <h2>{{ $t('分销模式') }}</h2>
                </div>
                <RadioType v-if="!loading" width="250px" v-model:modelValue="formState.distributionMode" :radioList="modeRadioList">
                    <template #tooltip="{ item }">
                        <div v-if="item.tooltipDetail" class="promote-mode-tooltip">
                            <div class="promote-mode-tooltip__lead">{{ item.tooltipDetail.lead }}</div>
                            <div class="promote-mode-tooltip__section">
                                <div v-for="(line, idx) in item.tooltipDetail.example" :key="idx" class="promote-mode-tooltip__text">
                                    {{ line }}
                                </div>
                            </div>
                            <div class="promote-mode-tooltip__section">
                                <div class="promote-mode-tooltip__text">{{ item.tooltipDetail.suitable }}</div>
                            </div>
                        </div>
                    </template>
                </RadioType>
                <div class="tip-text" v-if="!loading"> {{ $t('推广员等级模式和微商模式的分销方案、分销等级、准入方式两种模式完全相同，区别仅在计佣方式和下级招募，详情请查看') }} <a class="link" @click="compareDrawerVisible = true">{{ $t('《功能对比》') }}</a>
                </div>
                <div class="tit" v-if="!loading">
                    <h2>{{ $t('分销方案') }}</h2>
                </div>
                <RadioType
                    v-if="!loading"
                    width="250px"
                    v-model:modelValue="formState.saleType"
                    :radioList="[
                        { key: 1, title: t('一级分销'), desc: t('仅分销员本人推广可获取佣金') },
                        { key: 2, title: t('二级分销'), desc: t('下级分销员成交后，上级可获得邀请奖励') }
                    ]"
                >
                </RadioType>
                <div class="tit" v-if="!loading">
                    <h2>{{ $t('分销员等级方案') }}</h2>
                </div>
                <div class="promote-table">
                    <stepTable
                        v-if="!loading"
                        ref="stepTableRef"
                        :distributionLevel="formState.saleType"
                        :distributionMode="formState.distributionMode"
                        :typeData="formState.level"
                    >
                    </stepTable>
                </div>
                <el-form :model="formState" label-width="auto" class="mt20">
                    <el-form-item :label="$t('成为分销员')" required>
                        <div>
                            <el-radio-group v-model="formState.registerToSalesman">
                                <el-radio :value="0">{{ $t('手动创建') }}</el-radio>
                                <el-radio :value="1">{{ $t('商城注册自动开通') }}</el-radio>
                            </el-radio-group>
                            <div class="extra" v-if="formState.registerToSalesman === 0">{{ $t('手动创建分销员：商城所有一级分销员，需管理员在后台手动创建') }}</div>
                            <div class="extra" v-if="formState.registerToSalesman === 1"> {{ $t('注册自动开通：用户通过商城完成注册后，自动成为分销员') }} </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('下级招募方式')" :required="isWechatBiz">
                        <div>
                            <el-checkbox-group v-model="formState.recruitMethods" :disabled="!isWechatBiz">
                                <el-checkbox :value="1">{{ $t('消费自动开通') }}</el-checkbox>
                                <el-checkbox :value="2">{{ $t('名片邀请开通') }}</el-checkbox>
                            </el-checkbox-group>
                            <div class="extra">{{ $t('消费自动开通：客户通过分销员推广链接下单后，自动成为该分销员的下级') }}</div>
                            <div class="extra">{{ $t('名片邀请开通：分销员分享专属名片，商城新用户扫码并注册即可成为其下级') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('分销隐私')">
                        <div>
                            <el-checkbox v-model="formState.viewCustomerInfo" :true-value="1" :false-value="0">{{ $t('查看客户信息') }}</el-checkbox>
                            <div class="extra">
                                {{ $t('开启后，分销员可查看自己的客户信息') }}
                                <el-popover placement="right-start" trigger="hover" :width="300">
                                    <template #default>
                                        <el-image
                                            class="privacy-example-image"
                                            :src="viewCustomerInfoExampleImg"
                                            :preview-src-list="[viewCustomerInfoExampleImg]"
                                            preview-teleported
                                            fit="contain"
                                        />
                                    </template>
                                    <template #reference>
                                        <a class="example-link">{{ $t('示例') }}</a>
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </el-form-item>
                </el-form>
            </a-spin>
        </div>
        <div style="height: 20px"></div>
        <div class="selected-action-warp selected-warp-left" v-if="!loading" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
            <div class="selected-action">
                <el-button :loading="loading" class="form-submit-btn" size="large" type="primary" @click="handleSubmit">{{ $t('保 存') }}</el-button>
            </div>
        </div>

        <!-- 分销模式功能对比抽屉 -->
        <el-drawer
            v-model="compareDrawerVisible"
            :title="$t('分销模式功能对比')"
            size="800px"
            append-to-body
            :destroy-on-close="true"
        >
            <div class="compare-drawer">
                <section class="compare-drawer__section">
                    <h3 class="compare-drawer__title">{{ $t('模式差异：') }}</h3>
                    <div class="compare-table">
                        <div class="compare-table__row compare-table__row--head">
                            <div class="compare-table__cell compare-table__cell--label">{{ $t('对比项') }}</div>
                            <div class="compare-table__cell">{{ $t('推广员等级模式') }}</div>
                            <div class="compare-table__cell">{{ $t('微商模式') }}</div>
                        </div>
                        <div v-for="(row, index) in compareRows" :key="index" class="compare-table__row">
                            <div class="compare-table__cell compare-table__cell--label">{{ row.label }}</div>
                            <div class="compare-table__cell">{{ row.promote }}</div>
                            <div class="compare-table__cell">{{ row.micro }}</div>
                        </div>
                    </div>
                </section>

                <section class="compare-drawer__section">
                    <h3 class="compare-drawer__title">{{ $t('如何选择这种分销模式？') }}</h3>
                    <div class="compare-faq">
                        <div v-for="(item, index) in compareFaq" :key="index" class="compare-faq__item">
                            <div class="compare-faq__line">
                                <span class="compare-faq__tag compare-faq__tag--q">Q</span>
                                <span class="compare-faq__text compare-faq__text--q">{{ item.question }}</span>
                            </div>
                            <div class="compare-faq__line">
                                <span class="compare-faq__tag compare-faq__tag--a">A</span>
                                <span class="compare-faq__text compare-faq__text--a">{{ item.answer }}</span>
                            </div>
                            <div v-if="item.example" class="compare-faq__scene">
                                {{ item.example }}
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </el-drawer>

        <!-- 切换分销模式确认弹窗 -->
        <el-dialog
            v-model="modeConfirmVisible"
            :title="modeConfirmContent.title"
            width="440px"
            class="mode-confirm-dialog"
            :close-on-click-modal="false"
            @close="cancelModeChange"
        >
            <div class="mode-confirm-text">{{ modeConfirmContent.content }}</div>
            <template #footer>
                <el-button @click="cancelModeChange">{{ $t('取消') }}</el-button>
                <el-button type="primary" @click="confirmModeChange">{{ $t('确认') }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import stepTable from "./src/stepTable.vue";
import RadioType from "@/components/radio/src/RadioType.vue";
import { ref, onMounted, shallowRef, computed, nextTick, watch } from "vue";
import { message } from "ant-design-vue";
import type { SalesmanConfigFormState } from "@/types/salesman/promoteMode.d";
import { getSalesmanConfig, saveSalesmanConfig } from "@/api/salesman/promoteMode";
import { useThemeStore } from "@/store/theme";
import viewCustomerInfoExampleImg from "@/assets/salesman/viewCustomerInfoExample.png";

/** 分销模式：1-推广员等级模式 2-微商模式 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const MODE_PROMOTER = 1;
const MODE_WECHAT_BIZ = 2;

const { themeInfo } = useThemeStore();
const loading = ref<boolean>(true);
const stepTableRef = shallowRef();
const formState = ref<SalesmanConfigFormState>({
    distributionMode: MODE_PROMOTER,
    recruitMethods: [],
    viewCustomerInfo: 0,
    level: [],
    saleType: 1,
    registerToSalesman: 1
});
const isWechatBiz = computed(() => formState.value.distributionMode === MODE_WECHAT_BIZ);

// 分销模式卡片（含富文本 tooltip）
const modeRadioList = computed(() => [
    {
        key: MODE_PROMOTER,
        title: t('推广员等级模式'),
        desc: t('分销员分享商品链接，任何人点进来下单，分销员就拿佣金'),
        tips: true,
        tooltipDetail: {
            lead: t('分销员分享商品链接，任何人点进来下单，分销员就拿佣金。二级分销下，分销员的上级再拿一份。'),
            example: [t('示例：分销员分享链接，客户通过分享的链接下单，该分销员获得佣金奖励'), t('若客户自己回来买（不走链接），算正常下单，不计算佣金')],
            suitable: t('适合场景：导购分享、KOL 推广、按次推广结算')
        }
    },
    {
        key: MODE_WECHAT_BIZ,
        title: t('微商模式'),
        desc: t('先邀请客户成为下级，客户下单后，上级(推荐人)就能拿佣金。'),
        tips: true,
        tooltipDetail: {
            lead: t('先邀请客户成为下级，客户下单后，上级(邀请人)就能拿佣金。二级分销下，再往上一级也能拿邀请佣金。'),
            example: [t('示例：分销员 A 发展 B 为下级，B 自行购买分销商品（无需 A 再分享链接）A 可拿分销佣金。')],
            suitable: t('适合场景：私域老客户、复购场景、长期绑定关系')
        }
    }
]);

// 功能对比抽屉
const compareDrawerVisible = ref<boolean>(false);

const compareRows = computed(() => [
    {
        label: t('计佣触发方式'),
        promote: t('客户必须通过分销员的推广链接下单'),
        micro: t('下级分销员绑定后，自行购买分销商品即触发')
    },
    {
        label: t('上级是否需重复推广'),
        promote: t('是，每次下单都需分享链接'),
        micro: t('否，绑定一次，后续自购也计佣')
    },
    {
        label: t('下级分销员从哪来'),
        promote: t('不支持专属招募计划'),
        micro: t('支持：消费自动开通/名片邀请开通')
    },
    {
        label: t('典型运营场景'),
        promote: t('导购分享、KOL推广、按次结算'),
        micro: t('私域绑定、老客户复购、长期关系')
    }
]);

const compareFaq = computed(() => [
    {
        question: t('我希望分销员每次主动分享商品，客户点了链接才给佣金'),
        answer: t('选【推广员等级模式】'),
        example: t('场景：门店导购、达人带货、临时推广活动')
    },
    {
        question: t('我希望绑定客户/下级后，对方自己回来买也能持续给我佣金'),
        answer: t('选【微商模式】'),
        example: t('场景：私域绑定、老客户复购、复购型商品')
    },
    {
        question: t('分销方案(一级/二级)、等级、准入方式两种模式一样吗？'),
        answer: t('一样，两种模式只区别在「计佣方式」和「下级招募方式」。')
    },
    {
        question: t('可以随时切换吗？'),
        answer: t('可以。切换后新订单按新模式计佣，历史订单不受影响。')
    }
]);

// 切换分销模式确认弹窗
const modeConfirmVisible = ref<boolean>(false);
const pendingMode = ref<number | null>(null);
let internalUpdate = false;

const modeConfirmContent = computed(() => {
    if (pendingMode.value === MODE_WECHAT_BIZ) {
        return {
            title: t('切换为微商模式？'),
            content:
                t('切换为【微商模式】后，下级分销员购买分销商品时，上级将自动获得佣金，无需每次推广。同时可配置下级招募方式。分销方案、等级等配置保持不变。确认切换？')
        };
    }
    return {
        title: t('切换为推广员等级模式？'),
        content: t('切换为【推广员等级模式】后，上级仅在客户通过推广链接下单时才获得佣金。下级招募方式将不再生效。确认切换？')
    };
});

// 切换模式拦截：先弹确认，确认后再真正修改 distributionMode
watch(
    () => formState.value.distributionMode,
    async (val, oldVal) => {
        if (internalUpdate || loading.value || val === oldVal) {
            return;
        }
        // 回滚 UI 选择，弹窗确认后再真正写入
        pendingMode.value = val;
        internalUpdate = true;
        formState.value.distributionMode = oldVal;
        await nextTick();
        internalUpdate = false;
        modeConfirmVisible.value = true;
    }
);

const confirmModeChange = async () => {
    if (pendingMode.value !== null) {
        internalUpdate = true;
        formState.value.distributionMode = pendingMode.value;
        if (pendingMode.value !== MODE_WECHAT_BIZ) {
            formState.value.recruitMethods = [];
        }
        await nextTick();
        internalUpdate = false;
    }
    modeConfirmVisible.value = false;
    pendingMode.value = null;
};

const cancelModeChange = () => {
    modeConfirmVisible.value = false;
    pendingMode.value = null;
};

const handleSubmit = async () => {
    await stepTableRef.value.formRef.validate();
    if (isWechatBiz.value && (!formState.value.recruitMethods || formState.value.recruitMethods.length === 0)) {
        message.error(t('微商模式下请至少选择一种下级招募方式'));
        return;
    }
    formState.value.level = stepTableRef.value.form.typeData;
    try {
        const { wechatBizEnabled: _omit, ...saveData } = formState.value;
        await saveSalesmanConfig({ ...saveData });
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    }
};
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getSalesmanConfig();
        internalUpdate = true;
        Object.assign(formState.value, {
            ...result,
            level: Array.isArray((result as SalesmanConfigFormState)?.level) ? (result as SalesmanConfigFormState).level : [],
            saleType: Number((result as SalesmanConfigFormState)?.saleType ?? 1),
            registerToSalesman: Number((result as SalesmanConfigFormState)?.registerToSalesman ?? 1),
            distributionMode: Number((result as SalesmanConfigFormState)?.distributionMode ?? MODE_PROMOTER),
            recruitMethods: Array.isArray((result as SalesmanConfigFormState)?.recruitMethods)
                ? (result as SalesmanConfigFormState).recruitMethods
                : [],
            viewCustomerInfo: Number((result as SalesmanConfigFormState)?.viewCustomerInfo ?? 0)
        });
        await nextTick();
        internalUpdate = false;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.container {
    :deep(.radio-style .triangle) {
        &::after {
            right: 3px;
            top: 19px;
            font-size: 18px;
        }
    }
    .tit {
        margin-top: 40px;
        margin-bottom: 20px;
    }
    .tip-text {
        color: #979797;
        margin-top: 16px;
        .link {
            color: var(--el-color-primary);
            cursor: pointer;
        }
    }
    .example-link {
        color: var(--el-color-primary);
        cursor: pointer;
        margin-left: 4px;
    }
}
.privacy-example-image {
    display: block;
    width: 280px;
    max-width: 100%;
    margin: 0 auto;
}
.footer {
    width: 100%;
    position: fixed;
    bottom: 0;
    left: 0;
    justify-content: center;
    height: 30px;
    line-height: 30px;
    background-color: #fff;
    box-shadow: 0 -2px 8px 0 rgba(200, 201, 204, 0.2);
    z-index: 10;
}

.mode-confirm-text {
    font-size: 14px;
    line-height: 1.7;
    color: rgba(0, 0, 0, 0.65);
}

// 对齐 Vort 版 dialog 简洁风格：标题更紧凑、body/footer 内边距收紧、footer 无顶部空白
:deep(.mode-confirm-dialog) {
    .el-dialog__header {
        padding: 16px 20px 12px;
        margin-right: 0;
        border-bottom: none;
    }
    .el-dialog__title {
        font-size: 16px;
        font-weight: 600;
        color: rgba(0, 0, 0, 0.88);
        line-height: 1.4;
    }
    .el-dialog__headerbtn {
        top: 12px;
        right: 12px;
    }
    .el-dialog__body {
        padding: 4px 20px 20px;
    }
    .el-dialog__footer {
        padding: 0 20px 16px;
        border-top: none;
    }
}

.compare-drawer {
    &__section {
        margin-bottom: 28px;

        &:last-child {
            margin-bottom: 0;
        }
    }

    &__title {
        font-size: 15px;
        font-weight: 600;
        color: rgba(0, 0, 0, 0.88);
        margin-bottom: 14px;
    }
}

.compare-table {
    border: 1px solid #ebedf2;
    border-radius: 8px;
    overflow: hidden;

    &__row {
        display: flex;
        border-bottom: 1px solid #ebedf2;

        &:last-child {
            border-bottom: none;
        }

        &--head {
            background-color: #f7f8fa;

            .compare-table__cell {
                font-weight: 600;
                color: rgba(0, 0, 0, 0.88);
            }
        }
    }

    &__cell {
        flex: 1;
        padding: 12px 16px;
        font-size: 13px;
        line-height: 1.6;
        color: rgba(0, 0, 0, 0.65);
        border-right: 1px solid #ebedf2;

        &:last-child {
            border-right: none;
        }

        &--label {
            flex: 0 0 180px;
            background-color: #fafbfc;
            color: rgba(0, 0, 0, 0.85);
            font-weight: 500;
        }
    }
}

.compare-faq {
    display: flex;
    flex-direction: column;
    gap: 14px;

    &__item {
        display: flex;
        flex-direction: column;
        gap: 10px;
        background-color: #f7f8fa;
        border-radius: 8px;
        padding: 16px;
    }

    &__line {
        display: flex;
        align-items: flex-start;
        gap: 8px;
    }

    &__tag {
        flex: 0 0 auto;
        width: 22px;
        height: 22px;
        line-height: 22px;
        text-align: center;
        border-radius: 6px;
        font-size: 12px;
        color: #fff;

        &--q {
            background-color: var(--el-color-primary);
        }

        &--a {
            background-color: #00b578;
        }
    }

    &__text {
        flex: 1;
        font-size: 13px;
        line-height: 1.7;
        padding-top: 1px;

        &--q {
            color: rgba(0, 0, 0, 0.88);
            font-weight: 500;
        }

        &--a {
            color: rgba(0, 0, 0, 0.88);
            font-weight: 500;
        }
    }

    &__scene {
        margin-left: 30px;
        margin-top: 2px;
        font-size: 12px;
        color: #8c8c8c;
        line-height: 1.6;
    }
}
</style>

<style lang="less">
.promote-mode-tooltip {
    padding: 2px 0;
    line-height: 1.7;

    &__lead {
        font-size: 13px;
        margin-bottom: 8px;
    }

    &__section {
        margin-top: 6px;

        &:first-of-type {
            margin-top: 0;
        }
    }

    &__text {
        font-size: 13px;
        margin-bottom: 2px;

        &:last-child {
            margin-bottom: 0;
        }
    }
}
</style>
