/** * @file 库存商品选择器组件 * @description 用于入库/出库/调拨等库存管理场景的商品选择器 * @features * - 弹窗选择商品 * - 支持导入商品Excel * - 批量设置数量 *
- 显示实物库存 * - 支持入库/出库数量校验 * - 查看模式支持搜索过滤 */
<template>
    <div class="lyecs-product-select-group">
        <el-space v-if="props.act === 'detail'">
            <el-form @keyup.enter="onSearchSubmit" @submit.prevent>
                <el-form-item label="" style="margin-bottom: 0">
                    <TigInput v-model="searchParams.productName" name="keyword" :placeholder="$t('请输入搜索')" clearable>
                        <template #append>
                            <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                        </template>
                    </TigInput>
                    <span v-if="isMultiple && searchList?.length > 0" class="ml10"
                        >{{ $t('共') }} <b>{{ searchList?.length }}</b> {{ $t('个商品') }}</span
                    >
                </el-form-item>
            </el-form>
        </el-space>
        <el-space v-else>
            <DialogForm
                v-if="!disabled"
                :params="{
                    selectedIds: ids,
                    isMultiple: props.isMultiple,
                    isSku: props.isSku,
                    isSelf: props.isSelf,
                    apiList: props.apiList,
                    inventoryFilterParams: props.inventoryFilterParams
                }"
                isDrawer
                path="product/product/src/SelectProduct"
                :title="$t('选择商品')"
                width="600px"
                @okCallback="onOk"
            >
                <el-button :disabled="max > 0 && ids?.length >= max" type="primary">{{ $t('选择商品') }}</el-button>
            </DialogForm>
            <el-button v-else :disabled="(max > 0 && ids?.length >= max) || disabled" type="primary">{{ $t('选择商品') }}</el-button>
            <DialogForm
                :maskClose="false"
                :params="{
                    templateFileApi: props.templateFileApi,
                    importSubmitApi: props.importSubmitApi,
                    importErrorData: props.importErrorData,
                    importTitle: props.importTitle
                }"
                isDrawer
                path="product/goodsArePutIntoStorage/src/InductsDialog"
                :title="props.importTitle"
                width="600px"
                @okCallback="onOk"
                :showClose="false"
                :showOnOk="false"
            >
                <el-button>{{ $t('导入商品') }}</el-button>
            </DialogForm>

            <ScanBarcodeInput
                ref="barcodeInputRef"
                class="barcode-input"
                v-model="barcode"
                :disabled="disabled || (max > 0 && ids?.length >= max)"
                :loading="barcodeLoading"
                :placeholder="$t('请输入商品条码')"
                buttonText="确定(Enter)"
                @confirm="onBarcodeConfirm"
            />
            <span v-if="isMultiple && ids?.length > 0" class="ml10"
                >{{ $t('已选择') }} <b>{{ ids?.length }}</b> {{ $t('个商品') }}</span
            >
            <el-button v-if="isMultiple && ids?.length > 0" :disabled="disabled" @click="clear">{{ $t('清空') }}</el-button>
        </el-space>
        <slot name="table" :searchList="searchList">
            <div v-if="productList.length > 0" class="lyecs-product-selected-con">
                <div class="product-selected-list">
                    <div class="product-selected-list-tr product-selected-list-th">
                        <div class="col col1">{{ $t('商品名称') }}</div>
                        <div class="col col2">{{ $t('商品规格') }}</div>
                        <!--                    <div class="col col3">商品单位</div>-->
                        <div class="col col3" v-if="props.importTitle === '导入商品'">{{ $t('实物库存') }}</div>
                        <div class="col col4">
                            <PopForm
                                v-if="props.act === 'add'"
                                :isDirect="true"
                                :isHover="false"
                                :max="100"
                                v-model:org-value="batchInput.quantity"
                                @callback="onBatchClick"
                                :label="$t('数量')"
                            >
                                <div class="flex flex-align-center flex-justify-center">
                                    {{ props.importTitle === $t('导入商品') ? $t('调拨数量') : props.inOrOut === 1 ? $t('入库数量') : $t('出库数量') }}
                                </div>
                            </PopForm>
                            <div v-else class="flex flex-align-center flex-justify-center">
                                {{ props.importTitle === $t('导入商品') ? $t('调拨数量') : props.inOrOut === 1 ? $t('入库数量') : $t('出库数量') }}
                            </div>
                        </div>
                        <div class="col col5 operation" v-if="props.act !== 'detail'">{{ $t('操作') }}</div>
                    </div>
                    <template v-for="(item, key) in searchList" :key="key">
                        <div class="product-selected-list-tr">
                            <div class="col col1 product-info">
                                <div>
                                    <img :src="imageFormat(item.productPicThumb)" height="50" width="50" />
                                </div>
                                <div>
                                    <div class="product-name">{{ item.productName }}</div>
                                </div>
                            </div>
                            <div class="col col2">{{ item.productSkuName || "--" }}</div>
                            <!--                        <div class="col col3">-->
                            <!--                            {{ item.productUnit || '&#45;&#45;' }}-->
                            <!--                        </div>-->
                            <div class="col col3" v-if="props.importTitle === '导入商品'">
                                {{ item.currentProductStock || "--" }}
                            </div>
                            <div class="col col4" style="text-align: center">
                                <span v-if="props.act === 'detail'">{{ item.quantity }}</span>
                                <template v-else>
                                    <el-input-number v-if="props.inOrOut === 1" :disabled="disabled" v-model="item.quantity" :min="1" width="100%" />
                                    <el-input-number
                                        v-else
                                        :disabled="disabled"
                                        v-model="item.quantity"
                                        :min="1"
                                        :max="item.currentProductStock"
                                        width="100%"
                                    />
                                </template>
                            </div>
                            <div class="col col5 operation" v-if="props.act !== 'detail'">
                                <el-button link type="primary" class="del-btn" @click="del(key)" :disabled="disabled">{{ $t('删除') }}</el-button>
                            </div>
                        </div>
                    </template>
                    <div class="empty-warp" v-if="searchList.length == 0">
                        <div class="empty-bg">{{ $t('暂无数据') }}</div>
                    </div>
                </div>
            </div>
        </slot>
    </div>
</template>
<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { DialogForm } from "@/components/dialog";
import { imageFormat } from "@/utils/format";
import { InventoryFilterState } from "@/types/product/inventoryQuery";
import { getShopType } from "@/utils/storage";
import { PopForm } from "@/components/pop-form";
import { message } from "ant-design-vue";
import ScanBarcodeInput from "./ScanBarcodeInput.vue";
// 传值
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    // 传入需要显示的商品list
    max: { type: Number, default: -1 },
    // 单选还是多选
    isMultiple: {
        type: Boolean,
        default: true
    },
    disabled: { type: Boolean, default: false },
    // 是否选择规格, 默认单选
    isSku: {
        type: Boolean,
        default: false
    },
    // 是否是选择赠品
    isGift: {
        type: Boolean,
        default: false
    },
    //是否自营商品
    isSelf: {
        type: Number,
        default: 0
    },
    // 商品接口
    apiList: {
        type: Function
    },
    inventoryFilterParams: {
        type: Object,
        default: () => {}
    },
    act: {
        type: String,
        default: ""
    },
    importTitle: {
        type: String,
        default: "导入入库单"
    },
    // 下载模板api
    templateFileApi: {
        type: Function
    },
    importSubmitApi: {
        type: Function
    },
    importErrorData: {
        type: Function
    },
    // 调拨出库组织shopId
    outboundOrganizationId: {
        type: Number
    },
    inOrOut: {
        type: Number,
        default: 1
    }
});
const searchParams = ref<any>({});
const filterParams = ref<any>({});
const batchInput = ref<any>({});
// 批量操作：
const onBatchClick = () => {
    console.log(batchInput.value.quantity);
    let i = 1;
    for (let index in productList.value) {
        if (batchInput.value.quantity) productList.value[index].quantity = batchInput.value.quantity;
        i++;
    }
};
// 商品列表
const productList = defineModel<InventoryFilterState[]>("productList", { type: Array, default: [] });
const searchList = computed(() => {
    if (!filterParams.value.productName) {
        return productList.value;
    }
    return productList.value.filter((item) => {
        return item.productName.indexOf(filterParams.value.productName) > -1;
    });
});

const onSearchSubmit = () => {
    filterParams.value.productName = searchParams.value.productName;
};

const ids = computed(() => {
    return productList.value.map((item) => item.inventoryId) || [];
});
const total = ref(0);

const onOk = (e: InventoryFilterState[]) => {
    // 默认数量为1
    e.forEach((item) => {
        item.quantity = item.quantity || 1;
    });
    productList.value.push(...e);
};

// 条码检索加入
const barcodeInputRef = ref<any>();
const barcode = ref<string>("");
const barcodeLoading = ref(false);
const onBarcodeConfirm = async (options?: { focusAfter?: boolean }) => {
    if (props.disabled) return;
    if (props.max > 0 && ids.value?.length >= props.max) {
        message.warning(t('已达到最大选择数量'));
        return;
    }
    const code = (barcode.value || "").trim();
    if (!code) {
        message.error(t('请输入商品条码'));
        return;
    }
    if (!props.apiList) {
        message.error(t('未配置商品查询接口'));
        return;
    }

    barcodeLoading.value = true;
    try {
        const result: any = await props.apiList({
            ...(props.inventoryFilterParams || {}),
            page: 1,
            size: 1,
            keyword: code
        });
        const record = result?.records?.[0];
        if (!record) {
            message.error(t('未找到该条码对应的商品'));
            return;
        }

        const nextItem: any = {
            ...record,
            inventoryId: record.inventoryId ?? record.productId + record.productSkuId,
            quantity: record.quantity || 1
        };
        if (ids.value?.includes(nextItem.inventoryId)) {
            const existing: any = productList.value.find((item: any) => item.inventoryId === nextItem.inventoryId);
            if (existing) {
                const currentQty = Number(existing.quantity || 0);
                let nextQty = currentQty + 1;
                // 出库/调拨：不允许超过实物库存上限（UI 上也有限制，这里做一次兜底）
                if (props.inOrOut !== 1 && typeof existing.currentProductStock === "number") {
                    const maxStock = Number(existing.currentProductStock || 0);
                    if (maxStock > 0) {
                        nextQty = Math.min(nextQty, maxStock);
                        if (nextQty === currentQty) {
                            message.warning(t('已达到库存上限'));
                            return;
                        }
                    }
                }
                existing.quantity = nextQty;
                message.success(t('已添加商品'));
            }
            return;
        }
        productList.value.push(nextItem);
        message.success(t('已添加商品'));
    } catch (error: any) {
        message.error(error?.message || t('检索失败'));
    } finally {
        barcodeLoading.value = false;
        barcode.value = "";
        if (options?.focusAfter !== false) {
            barcodeInputRef.value?.focus?.();
        }
    }
};

// -----------------------------
// 扫码枪（键盘模拟）全局监听：不聚焦输入框也能接收
// 说明：仅在页面处于激活状态时有效；会忽略在其他输入框/可编辑区域的按键
// -----------------------------
const scanBuffer = ref<string>("");
let scanTimer: number | undefined;
let lastKeyAt = 0;
let scanStartAt = 0;
const SCAN_KEY_GAP_MS = 80; // 扫码枪通常间隔很短，超过则认为是人工输入
const MIN_SCAN_LEN = 4; // 过短容易误判

const resetScan = () => {
    scanBuffer.value = "";
    lastKeyAt = 0;
    scanStartAt = 0;
    if (scanTimer) {
        window.clearTimeout(scanTimer);
        scanTimer = undefined;
    }
};

const isEditableTarget = (el: any) => {
    const target = el as HTMLElement | null;
    if (!target) return false;
    const tag = (target.tagName || "").toUpperCase();
    if (target.isContentEditable) return true;
    return tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT";
};

const onGlobalKeydown = async (e: KeyboardEvent) => {
    // 业务开关：禁用、已满、接口处理中时不接收
    if (props.act === "detail") return;
    if (props.disabled) return;
    if (barcodeLoading.value) return;
    if (props.max > 0 && ids.value?.length >= props.max) return;

    // 如果正在其他输入框里打字，避免误抓（如你希望“即使焦点在别的输入框也能扫”，可去掉这段）
    if (isEditableTarget(e.target)) return;

    if (e.ctrlKey || e.altKey || e.metaKey) return;

    const now = Date.now();
    if (lastKeyAt && now - lastKeyAt > SCAN_KEY_GAP_MS) {
        resetScan();
    }
    lastKeyAt = now;

    // 常见扫码枪以 Enter 结尾
    if (e.key === "Enter") {
        const code = (scanBuffer.value || "").trim();
        // 进一步用“速度”做防误判：总耗时过长更像人工输入
        const duration = scanStartAt ? now - scanStartAt : 999999;
        if (code.length >= MIN_SCAN_LEN && duration <= 1200) {
            e.preventDefault();
            e.stopPropagation();
            barcode.value = code;
            resetScan();
            await onBarcodeConfirm({ focusAfter: false });
        } else {
            resetScan();
        }
        return;
    }

    // 收集可打印字符（字母/数字/符号）
    if (e.key && e.key.length === 1) {
        if (!scanStartAt) scanStartAt = now;
        scanBuffer.value += e.key;
        if (scanTimer) window.clearTimeout(scanTimer);
        scanTimer = window.setTimeout(() => {
            resetScan();
        }, 300);
    }
};

onMounted(() => {
    window.addEventListener("keydown", onGlobalKeydown, true);
});

onBeforeUnmount(() => {
    window.removeEventListener("keydown", onGlobalKeydown, true);
    resetScan();
});
// 清空
const clear = () => {
    productList.value = [];
};
// 删除
const del = (key: number) => {
    productList.value.splice(<any>key, 1);
    total.value--;
};
defineExpose({
    ids,
    productList
});
</script>

<style lang="less" scoped>
.all_brand {
    padding: 8px;
}

.all_brand ul {
    display: flex;
    flex-wrap: wrap;
    width: 290px;
}

.all_brand li a {
    padding: 2px 7px;
    display: inline-block;
    border-radius: 3px;
    line-height: 20px;
}

.all_brand li a:hover {
    background: #f4f4f4;
}

/*商品选择器*/
.lyecs-product-select-group {
    margin-bottom: 0;
    width: 100%;
}

.lyecs-product-select-group .lyecs-product-selected-con {
    position: relative;
    padding-top: 50px;
    margin-top: 10px;
}

.lyecs-product-select-group .clear-btn {
    margin-left: 20px;
}

.lyecs-product-select-group .desc {
    margin-left: 20px;
    color: #999;
}

.lyecs-product-select-group .product-selected-list {
    margin-bottom: 20px;
    max-height: 550px;
    overflow-y: auto;
    min-width: 400px;
}

.lyecs-product-select-group .product-selected-list-tr {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    border-bottom: 1px solid #f0f0f0;
}
.product-selected-list-th {
    padding-right: 20px;
}

.lyecs-product-select-group .product-selected-list-th {
    color: #333;
    font-weight: bold;
    position: absolute;
    top: 0;
    width: 100%;
    background: #fff;
    height: 50px;
}

.lyecs-product-select-group .product-selected-list-tr .col {
    padding: 10px;
    box-sizing: border-box;
    flex: 1;
}
.lyecs-product-select-group .product-selected-list-tr .col1 {
    min-width: 250px;
}
.lyecs-product-select-group .product-selected-list-tr .col5 {
    flex: 0.8;
}

.lyecs-product-select-group .product-selected-list-tr .product-info {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    //margin-right: 15px;
}
.lyecs-product-select-group .product-selected-list-tr .product-name {
    flex: 1;
    line-height: 18px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
}

.lyecs-product-select-group .product-selected-list-tr .product-info img {
    margin-right: 8px;
}
@media only screen and (max-width: 767px) {
    .lyecs-product-selected-con {
        overflow-x: auto;
        max-width: 100% !important;
    }
    .lyecs-product-select-group .product-selected-list {
        min-width: auto;
    }
    .lyecs-product-select-group .product-selected-list-tr .col1 {
        width: 50px;
        word-break: break-all;
    }
    .lyecs-product-select-group .product-selected-list-tr .col3 {
        width: 30px;
    }
    .lyecs-product-select-group .product-selected-list-tr .product-info {
        flex-wrap: wrap;
    }
}

:deep(.el-input-number) {
    width: 120px;
}

.operation {
    text-align: center;
}

.barcode-input {
    width: auto;
}
</style>
