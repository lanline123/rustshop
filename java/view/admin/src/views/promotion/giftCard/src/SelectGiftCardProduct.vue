<template>
    <div class="lyecs-product-select-group">
        <el-space wrap>
            <DialogForm
                :params="{ selectedIds: skuList, isMultiple: props.isMultiple, isSelf: 1, excludeProductTypes: props.excludeProductTypes }"
                isDrawer
                path="promotion/giftCard/src/SelectProduct"
                :title="$t('选择商品')"
                width="1000px"
                @okCallback="onOk"
            >
                <el-button :disabled="max > 0 && skuList?.length >= max" type="primary">{{ $t('选择商品') }}</el-button>
            </DialogForm>
            <span v-if="skuList.length > 0" class="ml10" style="white-space: nowrap;"
                >{{ $t('已选择') }} <b>{{ skuList?.length }}</b> {{ $t('个商品') }}</span
            >
            <el-button v-if="skuList.length > 0" @click="clear">{{ $t('清空') }}</el-button>
        </el-space>
        <div v-if="skuList.length > 0" class="lyecs-product-selected-con">
            <el-table :data="skuList" style="width: 100%" max-height="550">
                <el-table-column prop="productSn" :label="$t('商品编号')" width="150" />
                <el-table-column :label="$t('商品信息')" min-width="250">
                    <template #default="{ row }">
                        <div class="product-info">
                            <div>
                                <img :src="imageFormat(row?.picUrl)" height="50" width="50" />
                            </div>
                            <div class="product-info-right">
                                <div class="product-name">{{ row?.productName }}</div>
                                <div v-if="row?.skuData" class="product-sku">
                                    <el-tooltip :content="getSkuInfo(row?.skuData)" placement="bottom" effect="dark" :show-after="100">
                                        {{ getSkuInfo(row?.skuData) }}
                                    </el-tooltip>
                                </div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('原价')" width="120">
                    <template #default="{ row }">
                        {{ priceFormat(row?.skuId > 0 ? row?.skuPrice : row?.productPrice) }}
                    </template>
                </el-table-column>
                <el-table-column :label="$t('操作')" width="80" fixed="right">
                    <template #default="{ $index }">
                        <el-button link type="primary" class="del-btn" @click="del($index)">{{ $t('删除') }}</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { imageFormat, priceFormat } from "@/utils/format";
// 传值
const props = defineProps({
    // 传入需要显示的商品list
    max: { type: Number, default: -1 },
    // 单选还是多选
    isMultiple: {
        type: Boolean,
        default: true
    },
    // 排除的商品类型
    excludeProductTypes: {
        type: Array,
        default: () => []
    }
});
const ids = defineModel<any[]>("ids", { type: Array, default: [] });
const skuList = ref<any[]>([]);
onMounted(async () => {
    if (ids.value && ids.value.length > 0) {
        skuList.value = ids.value;
        ids.value = skuList.value.map((item: any) => ({ productId: item.productId, skuId: item.skuId }));
    } else {
        ids.value = [];
    }
});

const getSkuInfo = (skuData: string) => {
    if (!skuData || !skuData.length) {
        return "-";
    }
    try {
        const skuInfo = JSON.parse(skuData);
        return skuInfo
            .map((item: any) => {
                return item.name + ": " + item.value;
            })
            .join("; ");
    } catch {
        return "-";
    }
};

const onOk = (data: any) => {
    // 排除掉已经存在的（按 productId + skuId 精确去重，避免仅按 skuId 误判）
    data = data.filter(
        (item: any) =>
            !skuList.value.some(
                (skuItem) => String(skuItem.productId) === String(item.productId) && String(skuItem.skuId) === String(item.skuId)
            )
    );
    skuList.value = [...skuList.value, ...data];
    ids.value = skuList.value.map((item: any) => ({ productId: item.productId, skuId: item.skuId }));
};
// 清空
const clear = () => {
    skuList.value = [];
    ids.value = [];
};
// 删除
const del = (index: number) => {
    const delSkuId = skuList.value[index].skuId;
    const delProductId = skuList.value[index].productId;
    // 仅删除当前点击的那一项（productId + skuId 精确匹配）
    skuList.value = skuList.value.filter((item) => !(item.productId === delProductId && item.skuId === delSkuId));
    ids.value = ids.value.filter((item) => !(item.productId === delProductId && item.skuId === delSkuId));
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

.product-sku {
    color: #999;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
    max-width: 90%;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-info img {
    margin-right: 10px;
    object-fit: cover;
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
