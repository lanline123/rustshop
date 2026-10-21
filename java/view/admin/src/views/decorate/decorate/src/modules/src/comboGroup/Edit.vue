<template>
    <div class="demo-collapse">
        <el-collapse v-model="activeNames">
            <draggable item-key="" :list="<any>comboGroups" ghost-class="ghost" chosen-class="chosenClass" animation="300" @start="" @end="">
                <template #item="{ element, index }">
                    <div class="collapse-item">
                        <el-collapse-item :name="index + 1">
                            <template #title>{{ $t('第【{index1}】组', { index1: index + 1 }) }}</template>
                            <div class="dec-edit-group" v-if="showTitlePic">
                                <div class="dec-edit-group-title">
                                    <div class="label">{{ $t('标题头图：宽150内,高30') }}</div>
                                    <div class="value"></div>
                                </div>
                                <div class="dec-edit-group-con">
                                    <div class="dec-pic-group dec-picSingle-group">
                                        <PicSelect v-model:photo="element.titleImage"> </PicSelect>
                                    </div>
                                </div>
                            </div>
                            <div class="dec-edit-group">
                                <div class="dec-edit-group-title">
                                    <div class="label">{{ $t('分组大标题') }}</div>
                                </div>
                                <div class="dec-edit-group-con">
                                    <div class="dec-input-group">
                                        <el-input v-model="element.groupTitle" :placeholder="$t('请输入标题内容')"></el-input>
                                    </div>
                                </div>
                            </div>
                            <div class="dec-edit-group">
                                <div class="dec-edit-group-title">
                                    <div class="label">{{ $t('分组小标题') }}</div>
                                </div>
                                <div class="dec-edit-group-con">
                                    <div class="dec-input-group">
                                        <el-input v-model="element.groupSubTitle" :placeholder="$t('请输入标题内容')"></el-input>
                                    </div>
                                </div>
                            </div>
                            <div class="dec-edit-group" v-if="showDesc">
                                <div class="dec-edit-group-title">
                                    <div class="label">{{ $t('分组描述') }}</div>
                                </div>
                                <div class="dec-edit-group-con">
                                    <div class="dec-input-group">
                                        <el-input v-model="element.groupDesc" :placeholder="$t('请输入描述内容')"></el-input>
                                    </div>
                                </div>
                            </div>
                            <div class="dec-edit-group" v-if="showColor">
                                <div class="dec-edit-group-title">
                                    <div class="label">{{ $t('分组颜色') }}</div>
                                    <div class="value"></div>
                                </div>
                                <div class="dec-edit-group-con">
                                    <div class="dec-color-group">
                                        <div class="dec-color-button">
                                            <a class="dec-color-reset" @click="element.groupColor = ''">{{ $t('重置') }}</a>
                                            <SelectColor v-model:color="element.groupColor"></SelectColor>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="dec-edit-group">
                                <div class="dec-edit-group-title">
                                    <div class="label">{{ $t('查看更多链接') }}</div>
                                </div>
                                <div class="dec-edit-group-con">
                                    <div class="dec-link-group">
                                        <div class="lyecs-link-select">
                                            <SelectLink v-model="element.moreLink" decorateType="mobile"></SelectLink>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="dec-edit-group" v-if="showGroupType">
                                <div class="dec-edit-group-title">
                                    <div class="label">{{ $t('分组内容') }}</div>
                                    <div class="value"></div>
                                </div>
                                <div class="dec-edit-group-con">
                                    <el-radio-group class="dec-radio-group" v-model="element.groupType">
                                        <el-radio-button :value="'image'">{{ $t('图片') }}</el-radio-button>
                                        <el-radio-button :value="'product'">{{ $t('商品') }}</el-radio-button>
                                    </el-radio-group>
                                </div>
                            </div>
                            <div class="dec-edit-group" v-if="element.groupType == 'image'">
                                <div style="width: 100%">
                                    <div class="tips">{{ $t('轮播图片: 宽300, 高180') }}</div>
                                    <PicList :isMultiple="true" v-model:photos="element.picList" :title="$t('添加图片')" decorateType="mobile"></PicList>
                                </div>
                            </div>
                            <div class="dec-edit-group dec-edit-group-block" v-if="element.groupType == 'product'">
                                <div class="dec-edit-group-title">
                                    <div class="label">{{ $t('选择商品') }}</div>
                                </div>
                                <div class="dec-edit-group-con">
                                    <div class="dec-goods-group">
                                        <ProductInfoSelect v-model:productList="element.pruductList" :isMultiple="true"></ProductInfoSelect>
                                    </div>
                                </div>
                                <div class="dec-edit-group-desc">
                                    <div>{{ $t('提示：您可以通过拖拽进行商品排序') }}</div>
                                </div>
                            </div>
                        </el-collapse-item>
                        <div v-if="isMultiple == true && showClose" class="del-item" @click="removePic(index)"><i class="ico-decorate icon-dec-cha"></i></div>
                    </div>
                </template>
            </draggable>
        </el-collapse>
        <div class="cobot-btn" v-if="isMultiple == true && showAdd">
            <a @click="onAdd" class="dec-pic-add-btn"><i class="ico-decorate icon-dec-jia"></i>{{ title }}</a>
        </div>
    </div>
</template>
<script lang="ts" setup>
import draggable from "vuedraggable";
import { SelectColor } from "@/components/select";
import { PicSelect, PicList, ProductInfoSelect } from "@/components/decorate";
import { ref, onMounted, ComputedRef, computed } from "vue";
import { SelectLink } from "@/components/select";
import { ComboGroupType } from "@/types/decorate/decorate.d";
const comboGroups = defineModel<ComboGroupType[]>("modelValue", { default: () => [] });
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
    let newGroup: ComboGroupType = {
        titleImage: {
            picUrl: "",
            picThumb: ""
        }, //标题头图
        groupTitle: "", //分组大标题
        groupSubTitle: "", //分组小标题
        groupDesc: "", //分组描述
        groupColor: "", //分组颜色
        groupType: "image", //分组类型 image: 图片组合  product: 商品组合
        picList: [], //图片列表
        pruductList: [], //商品列表
        moreLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        } //分组链接
    };
    comboGroups.value.push(newGroup);
};
const removePic = (index: number) => {
    comboGroups.value.splice(<any>index, 1);
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
