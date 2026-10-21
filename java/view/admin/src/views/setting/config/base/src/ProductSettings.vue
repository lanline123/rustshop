/** * @file 商品设置页 * @description 配置商品相关的显示和业务规则 * @features * - 设置货币符号（¥/$/€等） * - 配置商品货号前缀和市场价换算比例 * -
控制销量/市场价/询价功能显示 * - B2B批量采购显示设置 * - 商品评价设置 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="170px" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper">
                    <div class="title">{{ $t('商品货币设置') }}</div>
                    <el-form-item :label="$t('商品货币符')" prop="dollarSign">
                        <div>
                            <TigInput v-model="formState.dollarSign" width="200px" />
                            <div class="extra">{{ $t('货币符号 ，如：¥、€、$') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('商品货币中文')" prop="dollarSignCn">
                        <div>
                            <TigInput v-model="formState.dollarSignCn" width="200px" />
                            <div class="extra">{{ $t('货币符号中文，如：元、美元、欧元等') }}</div>
                        </div>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('商品录入设置') }}</div>
                    <el-form-item :label="$t('商品货号前缀')" prop="snPrefix">
                        <TigInput v-model="formState.snPrefix" width="200px" />
                    </el-form-item>
                    <el-form-item :label="$t('商品名称长度')" prop="customProductNameLength">
                        <div>
                            <el-input-number
                                v-model="formState.customProductNameLength"
                                class="left-number-input"
                                style="width: 200px"
                                :min="0"
                                :max="PRODUCT_NAME_LENGTH_MAX"
                                :step="1"
                                :precision="0"
                                step-strictly
                                controls-position="right"
                                @blur="onProductNameLengthBlur"
                            />
                            <div class="extra"> {{ $t('商品列表编辑弹窗和商品新增/编辑抽屉中的商品名称输入长度将同步使用该配置，最大不超过750，超出后将自动重置为750') }} </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('市场价换算')" prop="marketPriceRate">
                        <div>
                            <TigInput v-model="formState.marketPriceRate" :decimalPlaces="1" width="200px" type="decimal" />
                            <div class="extra">{{ $t('输入商品价格时将自动根据该比例计算市场价格，推荐：1.2') }}</div>
                        </div>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('商品编辑设置') }}</div>
                    <el-form-item :label="$t('商品编辑库存是否可修改')" prop="productEditStockModify">
                        <el-radio-group v-model="formState.productEditStockModify" class="itemWidth">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                        <div class="extra"> {{ $t('管控商品编辑页、商品列表页的库存编辑权限；选择否时，仅可通过库存管理 - 库存调整模块变更库存，新增商品不受该规则限制') }} </div>
                    </el-form-item>
                    <el-form-item label="规格图片" prop="specImageMode">
                        <el-radio-group v-model="formState.specImageMode" class="itemWidth">
                            <el-radio :value="1">仅第一组规格可上传图片</el-radio>
                            <el-radio :value="2">全部规格可上传图片</el-radio>
                        </el-radio-group>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('商品信息显示设置') }}</div>
                    <el-form-item :label="$t('显示销量')" prop="showSelledCount">
                        <el-radio-group v-model="formState.showSelledCount" class="itemWidth">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t('是否在商品详情页显示商品销量') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('显示市场价')" prop="showMarketprice">
                        <el-radio-group v-model="formState.showMarketprice" class="itemWidth">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t('是否在商品详情页价格栏目中显示市场价') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('是否开启询价功能')" prop="isEnquiry" v-if="isB2b() || isS2b2c() || isOverseas() || isStore()">
                        <el-radio-group v-model="formState.isEnquiry" class="itemWidth">
                            <el-radio :value="1">{{ $t('开启') }}</el-radio>
                            <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                        </el-radio-group>
                        <div class="extra"> {{ $t('开启后，前台商城会显示询价按钮。') }} <el-popover :width="400" placement="right-end" trigger="click">
                                <template #reference>
                                    <a>{{ $t('查看示例') }}</a>
                                </template>
                                <template #default>
                                    <img src="@/style/images/enquiry.png" style="width: 380px" />
                                </template>
                            </el-popover>
                        </div>
                    </el-form-item>
                    <template v-if="isB2b() || isStore()">
                        <el-form-item :label="$t('商品规格显示批量采购')" prop="bulkPurchase">
                            <el-radio-group v-model="formState.bulkPurchase" class="itemWidth">
                                <el-radio :value="1">{{ $t('是') }}</el-radio>
                                <el-radio :value="0">{{ $t('否') }}</el-radio>
                            </el-radio-group>
                            <div class="extra"> {{ $t('商品详情页将同时支持单品采购和批量采购。') }}<el-popover :width="300" placement="right-end" trigger="click">
                                    <template #reference>
                                        <a>{{ $t('查看示例') }}</a>
                                    </template>
                                    <template #default>
                                        <img src="@/style/images/bulkPurchase-mobile.png" style="width: 280px" />
                                    </template>
                                </el-popover>
                            </div>
                        </el-form-item>
                    </template>
                    <el-form-item :label="$t('显示筛选信息')" prop="enableAttributeFilter">
                        <el-radio-group v-model="formState.enableAttributeFilter" class="itemWidth">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('商品评价设置') }}</div>
                    <el-form-item :label="$t('是否开启商品评价')" prop="productCommentEnabled">
                        <el-radio-group v-model="formState.productCommentEnabled" class="itemWidth">
                            <el-radio :value="1">{{ $t('开启') }}</el-radio>
                            <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t('关闭后，前台不展示评价入口，用户也无法提交评价。') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('是否开启匿名评价')" prop="anonymousCommentEnabled">
                        <el-radio-group v-model="formState.anonymousCommentEnabled" class="itemWidth" :disabled="formState.productCommentEnabled !== 1">
                            <el-radio :value="1">{{ $t('开启') }}</el-radio>
                            <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">
                            {{ $t('关闭后，客户发表评价不展示匿名评价入口') }}
                            <el-popover :width="300" placement="right-end" trigger="click">
                                <template #reference>
                                    <a>{{ $t('示例') }}</a>
                                </template>
                                <template #default>
                                    <img src="@/style/images/anonymousComment.png" style="width: 280px" />
                                </template>
                            </el-popover>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }} </el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef, nextTick, watch } from "vue";
import { message } from "ant-design-vue";
import { BasicProductConfig } from "@/types/setting/config";
import { getAdminBase, getConfigProduct, saveConfigProduct } from "@/api/setting/config";
import { useConfigStore } from "@/store/config";
import { useRoute } from "vue-router";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { isB2b, isB2CPro, isMerchant, isOverseas, isS2b2c, isStore } from "@/utils/version";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
const PRODUCT_NAME_LENGTH_MAX = 750;
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<Partial<BasicProductConfig>>({
    dollarSign: "",
    dollarSignCn: "",
    snPrefix: "",
    customProductNameLength: 200,
    showSelledCount: undefined,
    showMarketprice: undefined,
    isEnquiry: undefined,
    bulkPurchase: undefined,
    marketPriceRate: 0,
    productEditStockModify: 1,
    specImageMode: 1,
    productCommentEnabled: 1,
    anonymousCommentEnabled: 1
});
// 商品评价关闭时匿名评价强制归零，并由 :disabled 锁死，避免留下「评价已关但匿名仍开启」的矛盾状态
watch(
    () => formState.value.productCommentEnabled,
    (val) => {
        if (val !== 1) {
            formState.value.anonymousCommentEnabled = 0;
        }
    }
);
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const normalizeProductNameLength = (value: unknown, fallback = 200) => {
    const normalizedValue = Number(value);
    if (!Number.isFinite(normalizedValue)) {
        return fallback;
    }
    return Math.min(Math.max(0, Math.trunc(normalizedValue)), PRODUCT_NAME_LENGTH_MAX);
};
const onProductNameLengthBlur = () => {
    formState.value.customProductNameLength = normalizeProductNameLength(formState.value.customProductNameLength, 200);
};
const loadFilter = async () => {
    try {
        const [productResult, adminBaseResult] = await Promise.all([getConfigProduct(), getAdminBase()]);
        const customProductNameLength = normalizeProductNameLength(adminBaseResult.customProductNameLength, 200);
        Object.assign(formState.value, convertNullsToEmptyStrings(productResult), {
            customProductNameLength
        });
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        await saveConfigProduct(formState.value);
        message.success(t('修改成功'));
        // 重新拉取 getAdminBase，同步全局库存开关（productEditStockModify 等），令商品编辑页/列表即时生效
        await configStore.updateConfig();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
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

.left-number-input :deep(.el-input__inner) {
    text-align: left;
}
</style>
