/**
 * @file 商品选择器组件（简版）
 * @description 用于选择商品的弹窗选择器，展示已选商品列表
 * @features
 * - 弹窗选择商品
 * - 支持单选和多选模式
 * - 已选商品分页列表
 * - 支持删除和清空操作
 * - 支持自营商品筛选
 */
<template>
    <div class="lyecs-product-select-group">
        <el-space wrap>
            <DialogForm
                isDrawer
                @okCallback="onOk"
                :title="$t('选择商品')"
                width="600px"
                path="product/product/src/SelectProduct"
                :params="{ selectedIds: ids, isMultiple: props.isMultiple, isSelf: isSelf }"
            >
                <el-button type="primary">{{ $t('选择商品') }}</el-button>
            </DialogForm>
            <span v-if="ids.length > 0" class="ml10" style="white-space: nowrap;"
                >{{ $t('已选择') }} <b>{{ ids.length }}</b> {{ $t('个商品') }}</span
            >
            <el-button v-if="ids.length > 0" @click="clear">{{ $t('清空') }}</el-button>
        </el-space>
        <div class="lyecs-product-selected-con" v-if="productList.length > 0">
            <div class="product-selected-list">
                <div class="product-selected-list-tr product-selected-list-th">
                    <div class="col col1">{{ $t('商品编号') }}</div>
                    <div class="col col2">{{ $t('商品信息') }}</div>
                    <div class="col col3">{{ $t('操作') }}</div>
                </div>
                <template v-for="(item, key) in productList">
                    <div class="product-selected-list-tr">
                        <div class="col col1">{{ item.productSn }}</div>
                        <div class="col col2 product-info">
                            <img width="50" height="50" :src="imageFormat(item.picThumb)" />
                            <span>{{ item.productName }}</span>
                        </div>
                        <div class="col col3">
                            <a class="del-btn" @click="del(key)">{{ $t('删除') }}</a>
                        </div>
                    </div>
                </template>
            </div>
            <div class="pagination-con" v-if="ids.length > 0" style="justify-content: right">
                <el-pagination
                    size="small"
                    @current-change="pageChange"
                    :current-page="page"
                    :page-size="size"
                    layout="slot ,prev, pager, next"
                    :total="ids.length"
                >
                    <template #default>{{ $t('第{size1}-{pagesize}条 / 总共{length}条', { size1: (page - 1) * size + 1, pagesize: page * size, length: ids.length }) }}</template>
                </el-pagination>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, toRefs, onMounted, watch } from "vue";
import request from "@/utils/request";
import { DialogForm } from "@/components/dialog";
import { imageFormat } from "@/utils/format";
// 传值
const props = defineProps({
    // 单选还是多选
    isMultiple: {
        type: Boolean,
        default: true
    },
    //是否自营商品
    isSelf: {
        type: Number,
        default: 0
    }
});
const ids = defineModel<number[]>("ids", { type: Array, default: [] });
onMounted(async () => {
    if (ids.value) {
        loadList();
    }
});
interface productForm {
    productSn?: string;
    picThumb: string;
    productName?: string;
    productId?: number;
}
// 商品列表
const productList = ref<productForm[]>([]);
const total = ref(0);
const size = ref(10);
const page = ref(1);
watch(
    () => ids,
    (newIds) => {
        console.log(newIds.value);
        loadList();
    },
    { deep: true }
);
// 给父组件传值
function loadList() {
    if (ids.value.length > 0) {
        request({
            url: "product/product/list",
            method: "get",
            params: {
                ids: ids.value,
                size: size.value,
                page: page.value
            }
        }).then((result: any) => {
            productList.value = result.records;
            total.value = result.total;
        });
    } else {
        productList.value = [];
        total.value = 0;
    }
}
const pageChange = (curPage: number, pageSize: any) => {
    page.value = curPage;
    loadList();
};
const onOk = (e: any) => {
    let _list = [];
    if (props.isMultiple == false) {
        ids.value = [];
    }
    for (let index in e) {
        ids.value.push(e[index]);
    }
    total.value = ids.value.length;
    // emit('update:ids', ids)
    loadList();
};
// 清空
const clear = () => {
    productList.value = [];
    ids.value = [];
};
// 删除
const del = (key: number) => {
    ids.value.splice(<any>key, 1);
    productList.value.splice(<any>key, 1);
};
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
    max-width: 710px;
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
}

.lyecs-product-select-group .product-selected-list-tr .col1 {
    width: 100px;
}

.lyecs-product-select-group .product-selected-list-tr .col2 {
    flex: 1;
}

.lyecs-product-select-group .product-selected-list-tr .col3 {
    width: 80px;
}

.lyecs-product-select-group .product-selected-list-tr .product-info {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}

.lyecs-product-select-group .product-selected-list-tr .product-info img {
    margin-right: 10px;
}
</style>
