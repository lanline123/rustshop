<template>
    <div class="lyecs-product-select-group">
        <el-space wrap>
            <DialogForm
                :params="{
                    selectedIds: ids,
                    isMultiple: props.isMultiple,
                    isSku: props.isSku,
                    isSelf: 1,
                    isFilterPresaleProduct: true
                }"
                isDrawer
                path="product/product/src/SelectProduct"
                :title="$t('选择商品')"
                width="600px"
                @okCallback="onOk"
            >
                <el-button :disabled="max > 0 && skuList?.length >= max" type="primary">{{ $t('选择商品') }}</el-button>
            </DialogForm>
            <span v-if="skuList.length > 0" class="ml10" style="white-space: nowrap;"
                >{{ $t('已选择') }} <b>{{ skuList?.length }}</b> {{ $t('个商品') }}</span
            >
            <el-button v-if="skuList.length > 0" @click="clear">{{ $t('清空') }}</el-button>
        </el-space>
        <div v-if="skuList.length > 0 && !loading" class="lyecs-product-selected-con">
            <el-table :data="skuList" style="width: 100%" max-height="550">
                <el-table-column :label="$t('商品编号')" prop="product.productSn" min-width="110" />
                <el-table-column :label="$t('商品信息')" min-width="250">
                    <template #default="{ row }">
                        <div class="product-info">
                            <div>
                                <img :src="imageFormat(row.product?.picThumb)" height="50" width="50" />
                            </div>
                            <div>
                                <div class="product-name">{{ row.product?.productName }}</div>
                                <div
                                    class="product-sku"
                                    v-if="
                                        row.product &&
                                        row.product.productSku &&
                                        row.product.productSku !== null &&
                                        row.product.productSku.length > 0 &&
                                        row.skuIds
                                    "
                                >
                                    <el-button type="primary" link size="mini" @click="checkSku(row)">{{ $t('+ 已选{length}个规格', { length: row.skuIds.length || 0 }) }}</el-button>
                                </div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('原价')" min-width="100">
                    <template #default="{ row }">
                        <div>{{ priceFormat(row.product?.productPrice) }}</div>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('优惠方式')" min-width="110">
                    <template #default="{ row }">
                        <div v-if="row.discountType">
                            <el-select v-model="row.discountType" size="mini">
                                <el-option :label="$t('打折')" :value="1" />
                                <el-option :label="$t('减钱')" :value="2" />
                                <el-option :label="$t('促销')" :value="3" />
                            </el-select>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('优惠内容')" min-width="150">
                    <template #default="{ row }">
                        <div v-if="row.discountType">
                            <div class="flex flex-align-center">
                                <div style="margin-right: 5px" v-if="row.discountType == 2">{{ $t('减') }}</div>
                                <TigInput type="decimal" v-if="row.discountType == 1" :min="0.1" :max="9.9" v-model="row.value" size="mini" width="70px" />
                                <TigInput
                                    type="decimal"
                                    v-else
                                    :min="1"
                                    :max="Number(row.product?.productPrice)"
                                    v-model="row.value"
                                    size="mini"
                                    width="70px"
                                />
                                <div style="margin-left: 5px" v-if="row.discountType > 1">{{ config.config.amountUnit }}</div>
                                <div style="margin-left: 5px" v-else>{{ $t('折') }}</div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('操作')" fixed="right" width="80">
                    <template #default="{ $index }">
                        <el-button link type="primary" class="del-btn" @click="del($index)">{{ $t('删除') }}</el-button>
                    </template>
                </el-table-column>
            </el-table>
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
import { onMounted, reactive, ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { imageFormat, priceFormat } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { message } from "ant-design-vue";
import { ProductFilterParams, type ProductFilterState } from "@/types/product/product";
import { useConfigStore } from "@/store/config";
import { Pagination } from "@/components/list";
import SelectSku from "@/views/promotion/productGift/src/SelectSku.vue";
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
    // 是否选择规格, 默认单选
    isSku: {
        type: Boolean,
        default: false
    }
});
const skuVisible = ref<boolean>(false);
const ids = defineModel<number[]>("ids", { type: Array, default: [] });
const skuIds = defineModel("skuIds", { type: Array, default: [] });
const skuList = ref<any[]>([]);
onMounted(async () => {
    if (ids.value && ids.value.length > 0) {
        await loadList(ids.value);
    } else {
        ids.value = [];
    }
    if (skuIds.value && skuIds.value.length > 0) {
        skuList.value = skuIds.value;
        skuIds.value.forEach((item: any) => {
            ids.value.push(item.productId);
        });
        loading.value = false;
    } else {
        skuList.value = [];
    }
});
// 商品列表
const total = ref(0);
const rowInfo = ref<any>({});
const config = useConfigStore();
const loading = ref<boolean>(true);
const filterParams = reactive<ProductFilterParams>({
    page: 1,
    sortField: "",
    sortOrder: "",
    keyword: ""
});
const loadList = async (id: number[]) => {
    loading.value = true;
    try {
        const result = await getProductList({ ids: id.join(","), ...filterParams, size: id.length });
        result.records.forEach((item: any) => {
            skuList.value.forEach((sku: any) => {
                if (sku.productId === item.productId) {
                    sku.product = item;
                }
            });
        });
        console.log(skuList.value);
        skuIds.value = skuList.value;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const onOk = (e: any) => {
    let _list = ids.value;
    if (props.isMultiple == false) {
        _list = [];
    }
    if (props.isSku) {
        for (let index in e) {
            _list.push(e[index].productId);
            e[index].discountType = 1;
            e[index].value = "";
            skuList.value.push(e[index]);
        }
    }
    console.log(skuList.value);
    ids.value = _list;
    loadList(_list);
};
// 清空
const clear = () => {
    skuList.value = [];
    ids.value = [];
};
// 删除
const del = (key: number) => {
    let del_productId = skuList.value[key].productId;
    ids.value = ids.value.filter((num) => num !== del_productId);
    skuList.value = skuList.value.filter((item) => item.productId !== del_productId);
    skuIds.value = skuIds.value.filter((item: any) => item.productId !== del_productId);
};
const checkSku = (item: any) => {
    item.product.skuIds = item.skuIds;
    rowInfo.value = item.product;
    skuVisible.value = true;
};
const closeSku = () => {
    rowInfo.value = "";
    skuVisible.value = false;
};
const skuRef: any = ref();
const submitSku = () => {
    if (skuRef.value.skuIds.length > 0) {
        skuList.value.forEach((item: any) => {
            if (item.productId == rowInfo.value.productId) {
                item.skuIds = skuRef.value.skuIds;
            }
        });
        rowInfo.value = "";
        skuVisible.value = false;
    } else {
        message.error(t('请选择规格'));
    }
};
defineExpose({
    ids,
    skuList
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

    :deep(.el-select__wrapper) {
        width: 80px !important;
    }
}

.lyecs-product-select-group .lyecs-product-selected-con {
    max-width: 810px;
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

.product-name {
    width: 160px;
    margin-right: 20px;
    line-height: 18px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
}

.product-sku {
    color: #999;
    cursor: pointer;
}

.product-info {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    padding: 5px 0;
}

.product-info img {
    margin-right: 10px;
    flex-shrink: 0;
}

@media only screen and (max-width: 767px) {
    .lyecs-product-selected-con {
        max-width: 100% !important;
        width: 100%;
    }

    :deep(.el-table) {
        width: 100% !important;
    }

    :deep(.el-table__header), :deep(.el-table__body) {
        min-width: 600px;
    }
    :deep(.el-table__body-wrapper) {
        overflow-x: auto;
    }
}
</style>
