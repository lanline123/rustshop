<template>
    <div>
        <draggable
            class="dec-goods-group-list"
            item-key="prodcutId"
            :list="productList"
            ghost-class="ghost"
            chosen-class="chosenClass"
            animation="300"
            @start=""
            @end=""
        >
            <template #item="{ element, index }">
                <div class="dec-goods-group-item dec-tip dec-goods-group-item-goods">
                    <div class="dec-goods-group-item-con">
                        <div class="item-goods-con">
                            <a :href="urlFormat({ path: 'product', sn: element.productSn })" target="_blank" :title="element.productName"
                                ><img class="item-goods-img" :src="imageFormat(element.picThumb)"
                            /></a>
                        </div>
                        <div class="del-item" @click="del(index)"><i class="ico-decorate icon-dec-cha"></i></div>
                    </div>
                </div>
            </template>
            <template #footer>
                <DialogForm
                    isDrawer
                    @okCallback="onAdd"
                    :title="$t('选择商品')"
                    width="600px"
                    path="product/product/src/SelectProduct"
                    :params="{ selectedIds: ids, isMultiple: props.isMultiple, maxSelection: props.maxSelection, excludeShopId: true }"
                >
                    <div class="dec-goods-group-item dec-goods-group-add dec-tip" data-title="添加商品">
                        <div class="dec-goods-group-item-con">
                            <div class="item-goods-con">
                                <a href="javascript:;"><i class="ico-decorate icon-dec-jia"></i></a>
                            </div>
                        </div>
                    </div>
                </DialogForm>
            </template>
        </draggable>
    </div>
</template>
<script setup lang="ts">
import { computed, ref, toRefs, onMounted } from "vue";
import type { Ref } from "vue";
import { DialogForm } from "@/components/dialog";
import draggable from "vuedraggable";
import { imageFormat, urlFormat } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { message } from "ant-design-vue";
const dom: Ref<HTMLDivElement> = ref(null) as any;
interface productList {
    productId?: number;
    productSn?: string;
    productName?: string;
    productDesc?: string;
    productPrice?: number;
    marketPrice?: number;
    picThumb?: string;
}
const props = defineProps({
    isMultiple: {
        type: Boolean,
        default: false
    },
    maxSelection: {
        type: Number,
        default: 0
    }
});
const ids = ref<number[]>([]);
const productList = defineModel<productList[]>("productList") as Ref<productList[]>;
onMounted(() => {
    // if (productList.value && productList.value.length > 0) {
    //     ids.value = productList.value
    //         .filter((item: any) => item.productId !== 0) // 过滤掉productId为0的项
    //         .map((item: any) => item.productId);
    // }
    // console.log(ids.value);
    // if (ids.value.length > 0) {
    //     loadList();
    // }
});
const loadList = async () => {};
const onAdd = async (e: any) => {
    try {
        const result = await getProductList({
            ids: e.join(','),
            size: 100,
            page: 1
        });
        console.log(result.records);
        result.records.forEach((item: any) => {
            let data = {
                productId: item.productId,
                productSn: item.productSn,
                productName: item.productName,
                productDesc: item.productDesc,
                productPrice: item.productPrice,
                marketPrice: item.marketPrice,
                picThumb: item.picThumb
            };
        });
        productList.value.push(...result.records);
        console.log(productList.value);
    } catch (error: any) {
        message.error(error.message);
    }
};
// 清空
const clear = () => {
    productList.value = [];
    ids.value = [];
};
// 删除
const del = (key: number) => {
    productList.value.splice(<any>key, 1);
    ids.value = productList.value
        .filter((item: any) => item.productId !== 0) // 过滤掉productId为0的项
        .map((item: any) => item.productId);
};
</script>

<style lang="less" scoped></style>
