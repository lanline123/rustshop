/**
 * @file 文章分类选择器组件
 * @description 树形结构的文章分类选择器
 * @features
 * - 树形下拉选择
 * - 支持任意层级选择
 * - 自动加载分类数据
 */
<template>
    <el-tree-select :style="{width: minWidth}" :placeholder="$t('请选择...')" v-model="props.catId" :fit-input-width="false" :props="treeProps" :multiple="false" :data="articleCatList" check-strictly
        :render-after-expand="false" check-on-click-node />
</template>
<script setup lang="ts">
import { ref, toRefs, onMounted } from "vue"
import request from '@/utils/request'
// 传值
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    catId: { type: [Number, Array], default: null },
    multiple: { type: Boolean, default: false },
    minWidth: { type: String, default: '' },
})
const treeProps = { label: 'catName', value: 'catId', children: 'children' }
// 文章列表
const articleCatList = ref([
    { catName: t('请选择...'), catId: 0 }
]);
const total = ref(0);
const loaded = ref(false)
// 给父组件传值
const emit = defineEmits(['update:catId'])
onMounted(async () => {
    loadList();
});

function loadList() {
    if (loaded.value == false) {
        request({
            url: 'content/articleCat/list',
            method: 'post',
            data: {
                ids: props.catId,
            }
        }).then((result: any) => {
            articleCatList.value.push(...result.records)
            total.value = result.total
        })
    } else {
        articleCatList.value = [];
        total.value = 0
    }

}
</script>

<style lang="less" scoped></style>
