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
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('商品来源') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                                <DialogForm
                                    isDrawer
                                    @okCallback="onChange"
                                    :title="$t('选择商品分组')"
                                    width="600px"
                                    path="product/productGroup/src/SelectProductGroup"
                                    :params="{ selectedIds: [element.productGroupId], index: index, isMultiple: props.isMultiple }"
                                    style="width: 100%"
                                >
                                    <el-button type="primary" link :icon="Edit">{{ element.productGroupName }}</el-button>
                                </DialogForm>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('菜单名称') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                                <el-input v-model="element.menuTitle" :placeholder="$t('请输入菜单名称')"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('商品个数') }}</div>
                            <div class="value"></div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group class="dec-radio-group" v-model="element.productNumType">
                                <el-radio :value="'number'">
                                    <el-input style="width: 90px" type="number" v-model="element.productNum" :placeholder="$t('请输入商品数量')"></el-input>
                                </el-radio>
                                <el-radio :value="'all'">{{ $t('全部') }}</el-radio>
                            </el-radio-group>
                        </div>
                    </div>
                    <div v-if="type === 'productGroup'">
                        <div class="dec-edit-group">
                            <div class="dec-edit-group-title">
                                <div class="label">{{ $t('主标题') }}</div>
                            </div>
                            <div class="dec-edit-group-con">
                                <div class="dec-input-group">
                                    <el-input v-model="element.title" :placeholder="$t('请输入标题内容')"></el-input>
                                </div>
                            </div>
                        </div>
                        <div class="dec-edit-group">
                            <div class="dec-edit-group-title">
                                <div class="label">{{ $t('副标题') }}</div>
                            </div>
                            <div class="dec-edit-group-con">
                                <div class="dec-input-group">
                                    <el-input v-model="element.subTitle" :placeholder="$t('请输入标题内容')"></el-input>
                                </div>
                            </div>
                        </div>
                        <div v-if="showCustomImage" class="dec-edit-group dec-edit-group-block">
                            <div class="dec-edit-group-title">
                                <div class="label">{{ $t('自定义图片') }}</div>
                            </div>
                            <div class="dec-edit-group-con">
                                <PicList v-model:photo="element.bannerPic" :showText="false" decorateType="mobile"></PicList>
                            </div>
                        </div>
                    </div>
                    <div class="del-item" @click="del(index)"><i class="ico-decorate icon-dec-cha"></i></div>
                </div>
            </template>
        </draggable>
        <div class="gallery-list-warp" v-if="isMultiple || productGroups.length < 1">
            <div class="dec-pic-group">
                <div class="dec-pic-add-con">
                    <DialogForm
                        isDrawer
                        @okCallback="onAdd"
                        :title="$t('选择商品分组')"
                        width="600px"
                        path="product/productGroup/src/SelectProductGroup"
                        :params="{ selectedIds: groupIds, isMultiple: props.isMultiple }"
                        style="width: 100%"
                    >
                        <a class="dec-pic-add-btn"><i class="ico-decorate icon-dec-jia"></i>{{ $t('添加商品分组') }}</a>
                    </DialogForm>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { nextTick, ref } from "vue";
import { Edit } from "@element-plus/icons-vue";
import { PicSelect, PicList } from "@/components/decorate";
import { SelectLink } from "@/components/select";
import type { Ref } from "vue";
import { DialogForm } from "@/components/dialog";
import draggable from "vuedraggable";
import { getProductGroupList } from "@/api/product/productGroup";
import { message } from "ant-design-vue";
import { ProductGroupsType } from "@/types/decorate/decorate.d";
import { min } from "lodash-es";
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
    },
    // 是否启用「自定义图片」设置项（仅 productGrouping 模块开启；默认 false 保证 specialOffer 等其他消费方零影响）
    showCustomImage: {
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
        if (index === -1) {
            let arr: ProductGroupsType[] = [];
            result.records.forEach((item: any) => {
                let data = {
                    productGroupId: item.productGroupId,
                    productGroupName: item.productGroupName,
                    title: item.productGroupName,
                    subTitle: "",
                    menuTitle: item.productGroupName,
                    productNumType: "number",
                    productNum: item.productIds?.length || 6,
                    bannerPic: {},
                    bannerLink: {
                        link: {},
                        title: ""
                    }
                };
                arr.push(data);
            });
            productGroups.value = [...productGroups.value, ...arr];
        } else {
            productGroups.value[index].productGroupId = result.records[0].productGroupId;
            productGroups.value[index].productGroupName = result.records[0].productGroupName;
            productGroups.value[index].menuTitle = result.records[0].productGroupName;
        }
        groupIds.value = productGroups.value.map((item: any) => item.productGroupId);
    } catch (error: any) {
        message.error(error.message);
    }
};
// 清空
const clear = () => {
    productGroups.value = [];
    groupIds.value = [];
};
// 删除
const del = (key: number) => {
    productGroups.value.splice(<any>key, 1);
    groupIds.value.splice(<any>key, 1);
};
// 修改
const onChange = (e: any) => {
    loadFilter(e.ids, e.index);
};
</script>

<style lang="less" scoped>
.dec-pic-add-con {
    padding: 0 15px;
}
.gallery-list-warp {
    margin-bottom: 20px;
}
.dec-goods-group-list {
    padding: 0px 15px 0px 15px;
    .dec-goods-group-item {
        width: 100%;
        position: relative;
        margin-top: 12px;
        border-radius: 2px;
        background-color: #fff;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);
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
</style>
