/**
 * @file 商品选择器组件（完整版）
 * @description 功能完整的商品选择器，支持SKU选择、赠品库存设置等高级功能
 * @features
 * - 弹窗选择商品，支持单选/多选
 * - 支持SKU规格选择
 * - 支持赠品库存设置
 * - 已选商品分页列表
 * - 支持自营/预售商品过滤
 */
<template>
    <div class="lyecs-product-select-group">
        <el-space wrap v-if="['add', 'edit'].includes(props.act)">
            <DialogForm
                v-if="!disabled"
                :params="{
                    selectedIds: ids,
                    isMultiple: props.isMultiple,
                    isSku: props.isSku,
                    isSelf: props.isSelf,
                    isFilterPresaleProduct: props.isFilterPresaleProduct,
                    otherParams: props.otherParams,
                    apiList: props.apiList
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
            <slot name="otherBtn"></slot>
            <span v-if="isMultiple && ids?.length > 0" class="ml10" style="white-space: nowrap;"
                >{{ $t('已选择') }} <b>{{ ids?.length }}</b> {{ $t('个商品') }}</span
            >
            <el-button v-if="isMultiple && ids?.length > 0" :disabled="disabled" @click="clear">{{ $t('清空') }}</el-button>
        </el-space>
        <div v-else>
            <span v-if="isMultiple && ids?.length > 0" class="ml10" style="white-space: nowrap;"
                >{{ $t('已选择') }} <b>{{ ids?.length }}</b> {{ $t('个商品') }}</span
            >
        </div>
        <div v-if="!props.hideSelectedList && productList.length > 0 && !loading" class="lyecs-product-selected-con">
            <el-table :data="productList" style="width: 100%" max-height="550">
                <el-table-column prop="productSn" :label="$t('商品编号')" width="110" />
                <el-table-column :label="$t('商品信息')" min-width="250">
                    <template #default="{ row }">
                        <div class="product-info">
                            <div>
                                <img :src="imageFormat(row.picThumb)" height="50" width="50" />
                            </div>
                            <div class="product-info-right">
                                <div class="product-name">{{ row.productName }}</div>
                                <div class="product-sku" v-if="isSku && skuId">
                                    <el-button :disabled="disabled" link size="mini" @click="checkSku(row)">{{ $t('+ 已选1个规格') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="productPrice" :label="$t('商品价格')" width="100" />
                <el-table-column :label="$t('商品库存')" width="100">
                    <template #default="{ row }">
                        <span v-if="isSku && skuId && row.productSku !== null">
                            {{ row.productSku.find((item: any) => item.skuId == skuId)?.skuStock }}
                        </span>
                        <span v-else>
                            {{ row.productStock }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('赠品库存')" width="120" v-if="isSku && isGift">
                    <template #default="{ row }">
                        <TigInput
                            v-if="row.productSku !== null"
                            :disabled="disabled"
                            :min="0"
                            type="integer"
                            :max="row.productSku.find((item: any) => item.skuId == skuId)?.skuStock"
                            v-model="skuStock"
                            width="100%"
                        />
                        <TigInput v-else :disabled="disabled" :min="0" type="integer" :max="row.productStock" v-model="skuStock" width="100%" />
                    </template>
                </el-table-column>
                <el-table-column :label="$t('操作')" fixed="right" width="80" v-if="['add', 'edit'].includes(props.act)">
                    <template #default="{ $index }">
                        <el-button link type="primary" class="del-btn" @click="del($index)" :disabled="disabled">{{ $t('删除') }}</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div v-if="isMultiple && ids?.length > 0 && total > 0" class="pagination-con">
                <Pagination
                    v-model:page="filterParams.page"
                    v-model:size="filterParams.size"
                    :total="total"
                    @callback="loadList"
                    layout="slot ,prev, pager, next"
                    :background="false"
                />
            </div>
        </div>
    </div>
    <el-dialog v-model="skuVisible" :title="$t('选择规格')" width="500" :before-close="closeSku" :close-on-click-modal="false">
        <SelectSku v-if="skuVisible" ref="skuRef" :productInfo="rowInfo" :isMultiple="isMultiple"></SelectSku>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="closeSku">{{ $t('取消') }}</el-button>
                <el-button type="primary" @click="submitSku">{{ $t('保存') }}</el-button>
            </div>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref, watch } from "vue";
import { DialogForm } from "@/components/dialog";
import { imageFormat } from "@/utils/format";
import { getProductList as defaultGetProductList } from "@/api/product/product";
import { message } from "ant-design-vue";
import { ProductFilterParams, type ProductFilterState } from "@/types/product/product";
import { useConfigStore } from "@/store/config";
import { Pagination } from "@/components/list";
import SelectSku from "@/views/promotion/productGift/src/SelectSku.vue";
// 传值
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    act: {
        type: String,
        default: "add"
    },
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
    // 是否过滤预售商品
    isFilterPresaleProduct: {
        type: Boolean,
        default: false
    },
    // 其他参数
    otherParams: {
        type: Object,
        default: ()=>{}
    },
    /** 仅复用选品入口，由业务页面自行渲染唯一的已选商品表格 */
    hideSelectedList: {
        type: Boolean,
        default: false
    },
    apiList: {
        type: Function,
        default: null
    }
});
const productApi = props.apiList || defaultGetProductList;

/** 已选商品ID数组 */
const ids = defineModel<number[]>("ids", { type: Array, default: [] });
/** 选中的SKU ID */
const skuId = defineModel("skuId", { type: String, default: "" });
/** SKU库存数量 */
const skuStock = defineModel("skuStock", { type: Number, default: 0 });

const emit = defineEmits(["update:ids"]);

/** 商品列表数据 */
const productList = ref<ProductFilterState[]>([]);
/** 商品总数 */
const total = ref(0);

const config = useConfigStore();
/** 加载状态 */
const loading = ref<boolean>(true);
/** 查询参数 */
const filterParams = reactive<ProductFilterParams>({
    page: 1,
    size: config.get("pageSize") as number,
    sortField: "",
    sortOrder: "",
    keyword: ""
});
/** 商品ID字符串（逗号分隔） */
const productIds = ref<string>("");

/**
 * 加载商品列表
 * @param {number[]} id - 商品ID数组
 */
const loadList = async (id: number[]) => {
    loading.value = true;
    try {
        productIds.value = Array.isArray(id) ? id.join(",") : ids?.value?.join(",") || "";
        const result = await productApi({ ids: productIds.value, ...filterParams });
        productList.value = result.records.map((item: any) => {
            if (item.productId === undefined && item.id !== undefined) {
                return {
                    ...item,
                    productId: item.id,
                    productSn: item.skus?.[0]?.skuSn ?? "",
                    productStock: item.sumSkuStock ?? 0
                };
            }
            return item;
        });
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/**
 * 选择商品确认回调
 * @description 处理弹窗选择商品后的数据更新
 * @param {any} e - 选中的商品数据
 */
const onOk = (e: any) => {
    let _list = ids.value || [];
    skuId.value = "";
    skuStock.value = 0;
    if (props.isMultiple == false) {
        _list = [];
    }
    if (props.isSku) {
        _list.push(e[0].productId);
        if (e[0].skuIds) {
            skuId.value = e[0].skuIds[0];
        }
    } else {
        for (let index in e) {
            _list.push(e[index]);
        }
    }
    ids.value = _list;
    emit("update:ids", _list);
    if (!props.hideSelectedList) loadList(_list);
};

// 监听ids变化，自动加载商品列表
watch(
    ids,
    async (val) => {
        if (props.hideSelectedList) return;
        if (val && val.length > 0) {
            await loadList(val);
        }
    },
    { deep: true, immediate: true }
);

/** 清空已选商品 */
const clear = () => {
    productList.value = [];
    ids.value = [];
};

/**
 * 删除指定商品
 * @param {number} key - 商品在列表中的索引
 */
const del = (key: number) => {
    let delProductId = productList.value[key].productId;
    ids.value = ids.value.filter((num) => num !== delProductId);
    productList.value.splice(<any>key, 1);
    total.value--;
};

/** SKU选择组件引用 */
const skuRef: any = ref();
/** SKU弹窗显示状态 */
const skuVisible = ref<boolean>(false);
/** 当前操作的商品行信息 */
const rowInfo = ref<any>({});

/**
 * 打开SKU选择弹窗
 * @param {any} item - 商品信息
 */
const checkSku = (item: any) => {
    item.skuIds = [skuId.value];
    rowInfo.value = item;
    skuVisible.value = true;
};

/** 关闭SKU选择弹窗 */
const closeSku = () => {
    rowInfo.value = "";
    skuVisible.value = false;
};

/** 确认SKU选择 */
const submitSku = () => {
    if (skuRef.value.skuIds.length > 0) {
        skuId.value = skuRef.value.skuIds[0];
        rowInfo.value = "";
        skuVisible.value = false;
    } else {
        message.error(t('请选择规格'));
    }
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
    overflow: hidden;
    min-width: 0;
}

.lyecs-product-select-group .lyecs-product-selected-con {
    max-width: 710px;
    position: relative;
    margin-top: 10px;
}

.lyecs-product-select-group .clear-btn {
    margin-left: 20px;
}

.lyecs-product-select-group .desc {
    margin-left: 20px;
    color: #999;
}

.product-info {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}

.product-name {
    margin-right: 20px;
    line-height: 18px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
}

.product-info img {
    margin-right: 10px;
}

@media only screen and (max-width: 767px) {
    .lyecs-product-select-group .lyecs-product-selected-con {
        max-width: 100% !important;
        width: 100%;
    }
    :deep(.el-table) {
        width: 100% !important;
    }
}
</style>
