<template>
    <div>
        <draggable
            class="dec-goods-group-list"
            item-key="productGroupId"
            :list="productGroups"
            ghost-class="ghost"
            chosen-class="chosenClass"
            animation="300"
            @start=""
            @end=""
        >
            <template #item="{ element, index }">
                <div class="dec-goods-group-item">
                    <div class="dec-edit-group">
                        <div class="productGroupName" v-if="element.productGroupName">{{ element.productGroupName }}</div>
                    </div>
                </div>
            </template>
        </draggable>
        <div class="gallery-list-warp">
            <div class="dec-pic-group">
                <div class="dec-pic-add-con">
                    <DialogForm
                        isDrawer
                        @okCallback="onAdd"
                        :title="$t('选择商品分组')"
                        width="600px"
                        path="product/productGroup/src/SelectProductGroup"
                        :params="{ selectedIds: groupIds, isMultiple: props.isMultiple, buyShowSelectedIds: productGroups }"
                        style="width: 100%"
                    >
                        <el-button v-if="productGroups.length < 1">{{ $t('选择商品分组') }}</el-button>
                        <a class='btn-link' v-else>{{ $t('修改') }}</a>
                    </DialogForm>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import type { Ref } from "vue";
import { DialogForm } from "@/components/dialog";
import draggable from "vuedraggable";
import { getProductGroupList } from "@/api/product/productGroup";
import { message } from "ant-design-vue";
import { ProductGroupsType } from "@/types/decorate/decorate.d";
const props = defineProps({
    productIds: {
        type: Boolean,
        default: false
    },
    type: {
        type: String,
        default: "goods"
    },
    isMultiple: {
        type: Boolean,
        default: false
    }
});
const groupIds = ref<number[]>([]);
const productGroups = defineModel<ProductGroupsType[]>("productGroups", { default: [] }) as Ref<ProductGroupsType[]>;
const onAdd = async (e: any) => {
    loadFilter(e.ids);
};
// 获取列表的查询结果
const loadFilter = async (ids: any[], index?: number = -1, size?: number = 6) => {
    try {
        const result = await getProductGroupList({
            productGroupIds: ids.join(","),
            size: size,
            page: 1
        });
        productGroups.value = result.records.map((item: any) => {
            return {
                productGroupId: item.productGroupId,
                productGroupName: item.productGroupName,
            }
        })
        groupIds.value = productGroups.value.map((item: any) => item.productGroupId);
    } catch (error: any) {
        message.error(error.message);
    }
};
</script>

<style lang="less" scoped>
.gallery-list-warp {
    margin-bottom: 20px;
}
.dec-goods-group-list {
    display: flex;
    gap: 10px;
    .dec-goods-group-item {
        position: relative;
        border-radius: 2px;
        background-color: #fff;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);
        flex-shrink: 0;
        .del-item {
            position: absolute;
            cursor: pointer;
            right: -10px;
            top: -10px;
            color: #fff;
            background: #bbb;
            border-radius: 50%;
            z-index: 2;
            width: 18px;
            height: 18px;
            text-align: center;
            line-height: 18px;
            display: none;
            .ico-decorate{
                font-size: 12px;
            }
        }
    }
    .dec-goods-group-item:hover {
        .del-item {
            display: block;
        }
    }
}

.productGroupName{
    border: 1px solid #ccc;
    padding: 0 10px;
}
</style>
