/**
 * @file 动态列表组件
 * @description 支持动态添加和删除项的列表，常用于表单中的多行输入
 * @features
 * - 动态添加/删除列表项
 * - 自定义模板字段和类型
 * - 插槽支持自定义内容
 */
<template>
    <div :style="{ width: `${minWidth}px`}">
        <div v-for="(item,index) in list" class="dynamic-style">
            <div class="content-style">
                <slot :item="item" name="dynamicSlot"></slot>
            </div>
            <div class="delete-style" @click="deleteList(index)">
                <em class="main_pel_m iconfont">&#xe62c;</em>
            </div>
        </div>
        <el-button :type="buttonType" @click="addList">{{ buttonName }}</el-button>
        <div v-if="extra" class="extra">{{ extra }}</div>
    </div>
</template>
<script lang="ts" setup>
import {computed} from "vue";

const props = defineProps({
    /** 列表数据（支持v-model） */
    modelValue: { default: [], type: Array },
    /** 新项的字段名数组 */
    templateList: { default: [], type: Array },
    /** 字段对应的类型数组 */
    templateListType: { default: [], type: Array },
    /** 最小宽度 */
    minWidth: { default: 400, type: Number },
    /** 添加按钮文字 */
    buttonName: { default: '添加', type: String },
    /** 额外说明文字 */
    extra: { default: '', type: String },
    /** 按钮类型 */
    buttonType: { default: '', type: String }
});

/** 计算属性，双向绑定列表数据 */
const list = computed({
    get: () => props.modelValue,
    set: (newValue) => {
        emit("update:modelValue", newValue)
    }
});

const emit = defineEmits(["update:modelValue"]);

/**
 * 添加新项
 * @description 根据模板创建新项并添加到列表末尾
 */
const addList = () => {
    const newItem = {};
    console.log(props.templateList);
    props.templateList.forEach((item: any, index: number) => {
        if (props.templateListType[index] === 'number') {
            (newItem as any)[item] = null;
        } else if (props.templateListType[index] === 'string') {
            (newItem as any)[item] = '';
        } else if (props.templateListType[index] === 'boolean') {
            (newItem as any)[item] = false;
        }else{
            (newItem as any)[item] = '';
        }
    });
    list.value = [...list.value, newItem];
};

/**
 * 删除指定项
 * @param {number} index - 要删除项的索引
 */
const deleteList = (index: number) => {
    if (index >= 0 && index < list.value.length) {
        const updatedList = [...list.value];
        updatedList.splice(index, 1);
        list.value = updatedList;
    }
};
</script>
<style lang="less" scoped>
.dynamic-style {
    display: flex;
    flex-direction: row;
    margin-bottom: 10px;

    .delete-style {
        cursor: pointer;
        margin-left: 8px;
    }

    .content-style {
        width: 100%
    }

}
</style>
<style>
.dynamic-div {
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    width: 100%;
    font-size: 12px;
    gap: 8px;
    white-space: nowrap;
}
</style>
