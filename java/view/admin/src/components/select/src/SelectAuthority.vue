/**
 * @file 权限选择器组件
 * @description 级联选择器形式的权限菜单选择
 * @features
 * - 级联选择权限树
 * - 支持单选和多选模式
 * - 支持管理员类型区分（admin/shop）
 * - 懒加载数据
 */
<template>
    <el-cascader class="cascader" :props="cascaderProps" :disabled="disabled" :placeholder="$t('顶级权限')" :options="options" v-model="ids" clearable @change="onChange" @clear="onClear"
                 @visible-change="visibleChange" />
</template>
<script setup lang="ts">
import { ref, onMounted, watch} from "vue"
import { ElCascader } from 'element-plus'
import {getAllAuthority} from "@/api/authority/authority";
import { AuthorityFormState } from '@/types/authority/authority';
import {message} from "ant-design-vue";
// import 'element-plus/es/components/cascader/style/css'
// 传值
const props = defineProps({
    authorityId: { type: [Number, Array], default: 0 },
    multiple: { type: Boolean, default: false },
    disabled: { type: Boolean, default: false },
    adminType: { type: String, default: 'admin' },
})
const cascaderProps = { label: 'authorityName', value: 'authorityId', children: 'children', multiple: props.multiple, checkStrictly: true }
//选项卡
const options = ref<AuthorityFormState[]>([]);
const loaded = ref(false)
// 当前值
const ids = ref()
// 手动控制显示和隐藏
const open = ref(false)
// 给父组件传值
const emit = defineEmits(['update:authorityId'])
watch(
    () => props.adminType,
    (newVal) => {
        loaded.value = false
        loadCategory()
    }
);
// 加载分类
onMounted(() => {
    if (props.authorityId) {
        loadCategory()
    }
});
const loadCategory = async () => {
    if (loaded.value) {
        return;
    }
    loaded.value = true
    try {
        const result = await getAllAuthority({adminType:props.adminType});
        options.value = result
        ids.value = props.authorityId
    } catch (error:any) {
        message.error(error.message);
    }
}
// 监听变化
const onChange = (ids:any) => {
    if (props.multiple == true) {
        const newArray: number[] = ids.map((subArray:any) => subArray[subArray.length - 1]);
        emit('update:authorityId', newArray)
    } else {
        emit('update:authorityId', ids ? ids[ids.length - 1] : 0)
    }
}
const visibleChange = (e:any) => {
    if (e) {
        loadCategory()
    }
}
const onClear = () => {
    if (props.multiple == true) {
        ids.value = [];
    } else {
        ids.value = 0;
    }

}
// 处理分类数组
function dealCatArr(arr:any) {
    arr = arr.flat();
    arr = buildTree(arr, 0)
    arr = processArray(arr)
    return arr
}
// 定义一个递归函数，用于处理多维数组并返回新的数组
function processArray(arr: any[]): any[] {
    return arr.map((item) => {
        if (Array.isArray(item) || typeof item === 'object') {
            if (item.children) {
                item.children = processArray(item.children);
            }
            item.isLeaf = false; // 修改为新的值
        }
        return item;
    });
}
function buildTree(categories:any, parentId = 0) {
    const tree = [];
    for (const authority of categories) {
        if (authority.parentId === parentId) {
            const children = buildTree(categories, authority.authorityId);
            if (children.length) {
                authority.children = children;
            }
            tree.push(authority);
        }
    }
    return tree;
}
</script>
<style lang="less" scoped></style>
