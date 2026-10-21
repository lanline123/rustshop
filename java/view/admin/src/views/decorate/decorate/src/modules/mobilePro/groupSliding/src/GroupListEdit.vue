<template>
    <div class="demo-collapse">
        <draggable item-key="" :list="<any>groups" ghost-class="ghost" chosen-class="chosenClass" animation="300" @start="" @end="">
            <template #item="{ element, index }">
                <div class="collapse-item">
                    <div class="title-box">{{ $t('第【{index1}】组', { index1: index + 1 }) }}</div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('分组大标题') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group">
                                <el-input v-model="element.title" :placeholder="$t('请输入标题内容')"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('分组副标题') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group">
                                <el-input v-model="element.subTitle" :placeholder="$t('请输入标题内容')"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('标题链接') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-link-group">
                                <div class="lyecs-link-select">
                                    <SelectLink v-model="element.titleLink" decorateType="mobile"></SelectLink>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group dec-edit-group-block">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('选择商品') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-goods-group">
                                <ProductInfoSelect v-model:productList="element.productList" :isMultiple="true"></ProductInfoSelect>
                            </div>
                        </div>
                        <div class="dec-edit-group-desc">
                            <div>{{ $t('提示：您可以通过拖拽进行商品排序') }}</div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('商品标签') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group">
                                <el-input type="textarea" v-model="element.label" :placeholder="$t('请输入商品标签')"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group" v-if="showColor">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('背景颜色') }}</div>
                            <div class="value"></div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="element.backgroundColor = ''">{{ $t('重置') }}</a>
                                    <SelectColor v-model:color="element.backgroundColor"></SelectColor>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div v-if="isMultiple == true && showClose" class="del-item" @click="removePic(index)"><i class="ico-decorate icon-dec-cha"></i></div>
                </div>
            </template>
        </draggable>
        <div class="cobot-btn" v-if="isMultiple == true && showAdd">
            <a @click="onAdd" class="dec-pic-add-btn"><i class="ico-decorate icon-dec-jia"></i>{{ title }}</a>
        </div>
    </div>
</template>
<script lang="ts" setup>
import draggable from "vuedraggable";
import { SelectColor } from "@/components/select";
import { PicSelect, PicList, ProductInfoSelect } from "@/components/decorate";
import { ref } from "vue";
import { SelectLink } from "@/components/select";
import { ModuleGroupsType } from "@/types/decorate/decorate.d";
const groups = defineModel<ModuleGroupsType[]>("modelValue", { default: () => [] });
const props = defineProps({
    title: {
        type: String,
        default: ""
    },
    isMultiple: {
        type: Boolean,
        default: false
    },
    showTitlePic: {
        type: Boolean,
        default: true
    },
    showDesc: {
        type: Boolean,
        default: true
    },
    showColor: {
        type: Boolean,
        default: true
    },
    showGroupType: {
        type: Boolean,
        default: true
    },
    showClose: {
        type: Boolean,
        default: true
    },
    showAdd: {
        type: Boolean,
        default: true
    },
    activeNames: {
        type: Array,
        default: () => [1]
    }
});
const activeNames = ref(props.activeNames);
const onAdd = () => {
    let newGroup: ModuleGroupsType = {
        title: "", //分组大标题
        subTitle: "", //分组小标题
        label: "", //分组描述
        backgroundColor: "", //分组颜色
        productList: [], //商品列表
        titleLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        } //分组链接
    };
    groups.value.push(newGroup);
};
const removePic = (index: number) => {
    groups.value.splice(<any>index, 1);
};
</script>
<style scoped lang="less">
.demo-collapse {
    padding: 0 10px 15px 10px;
    :deep(.el-collapse) {
        border: none;
    }
    .collapse-item {
        width: 100%;
        position: relative;
        margin-top: 12px;
        border-radius: 2px;
        background-color: #fff;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);
        .title-box{
            padding: 15px;
            font-size: 14px;
            border-bottom: 1px solid #f0f0f0;
        }
        :deep(.el-collapse-item__header) {
            background-color: #f7f8fa !important;
            padding-left: 10px;
        }
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
    .collapse-item:hover .del-item {
        display: block;
    }
    .tips {
        color: var(--tig-primary);
    }
    .cobot-btn .dec-pic-add-btn {
        box-sizing: border-box;
        position: relative;
        display: -ms-flexbox;
        display: flex;
        -ms-flex-pack: center;
        justify-content: center;
        -ms-flex-align: center;
        align-items: center;
        margin-top: 12px;
        padding: 9px 16px;
        border: 1px solid var(--tig-primary);
        border-radius: 2px;
        background: #fff;
        color: var(--tig-primary);
        font-size: 14px;
        line-height: 20px;
        cursor: pointer;
    }
}
</style>
