/**
 * @file 文章选择器组件
 * @description 用于选择文章的弹窗选择器，支持单选/多选模式
 * @features
 * - 弹窗选择文章
 * - 支持单选和多选模式
 * - 已选文章列表展示
 * - 分页加载已选文章
 * - 支持删除和清空操作
 */
<template>
    <div class="lyecs-article-select-group">
        <el-space>
            <DialogForm v-if="!disabled" :params="{ selectedIds: modelValue, isMultiple: props.isMultiple }" isDrawer path="content/article/src/SelectArticle" :title="$t('选择文章')" width="600px" @okCallback="onOk">
                <el-button :disabled="max>0&&modelValue.length>=max" type="primary">{{ $t('选择文章') }}</el-button>
            </DialogForm>
            <el-button v-else :disabled="max>0&&modelValue.length>=max || disabled" type="primary">{{ $t('选择文章') }}</el-button>
            <span v-if="isMultiple && modelValue.length > 0" class="ml10">{{ $t('已选择') }} <b>{{ modelValue.length }}</b> {{ $t('个文章') }}</span>
            <el-button :disabled="disabled" v-if="isMultiple && modelValue.length > 0" @click="clear">{{ $t('清空') }}</el-button>
        </el-space>
        <div v-if="articleList.length > 0&&!loading" class="lyecs-article-selected-con">
            <div class="article-selected-list">
                <div class="article-selected-list-tr article-selected-list-th">
                    <div class="col col2">{{ $t('文章名称') }}</div>
                    <div class="col col3">{{ $t('操作') }}</div>
                </div>
                <template v-for="(item, key) in articleList">
                    <div class="article-selected-list-tr">
                        <div class="col col2 article-info">
                            {{ item.articleTitle }}
                        </div>
                        <div class="col col3">
                            <a class="del-btn" @click="del(key)">{{ $t('删除') }}</a>
                        </div>
                    </div>
                </template>
            </div>
            <div v-if="total > 0" class="pagination-con">
                <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadList" 
                layout="slot ,prev, pager, next" :background="false"/>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {onMounted, reactive, ref} from "vue"
import {DialogForm} from '@/components/dialog'
import {getArticleList} from "@/api/content/article";
import {message} from "ant-design-vue";
import {ArticleFilterParams, type ArticleFilterState} from "@/types/content/article";
import {useConfigStore} from "@/store/config";
import {Pagination} from "@/components/list";
// 传值
const props = defineProps({
    max: {type: Number, default: -1},
    disabled: { type: Boolean, default: false },
    // 单选还是多选
    isMultiple: {
        type: Boolean,
        default: true
    }
})
const modelValue = defineModel<number[]>('modelValue',{ type: Array, default: [] });


onMounted(async () => {
    if (modelValue.value && modelValue.value.length > 0) {
        await loadList(modelValue.value)
    }
});
// 商品列表
const articleList = ref<ArticleFilterState[]>([]);
const total = ref(0);

const config:any = useConfigStore();
const loading = ref<boolean>(true);
const filterParams = reactive<ArticleFilterParams>({
    page: 1,
    size: config.get('pageSize'),
    sortField: '',
    sortOrder: '',
    keyword: '',
});
const articleIds = ref<string>("");
const loadList = async (ids:any) => {
    loading.value = true;
    try {
         //id是数组，现在用,号分割成字符串
         articleIds.value = Array.isArray(ids) ? ids.join(",") : "";
        const result = await getArticleList({articleIds: articleIds.value, ...filterParams});
        articleList.value = result.records;
        total.value = result.total;
    } catch (error:any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
}
const onOk = (e:any) => {
    let _list = modelValue.value;
    if (props.isMultiple == false) {
        _list = [];
    }
    for (let index in e) {
        _list.push(e[index])
    }
    modelValue.value = _list
    loadList(_list)
}
// 清空
const clear = () => {
    articleList.value = []
    modelValue.value = []
}
// 删除
const del = (key:any) => {
    let delArticleId = articleList.value[key].articleId;
    modelValue.value = modelValue.value.filter(num => num !== delArticleId);
    articleList.value.splice(<any>key, 1)
}
defineExpose({
    modelValue,
    articleList
})
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
.lyecs-article-select-group {
    margin-bottom: 0;
    width: 100%;
}

.lyecs-article-select-group .lyecs-article-selected-con {
    max-width: 710px;
    position: relative;
    padding-top: 50px;
    margin-top: 10px;
}

.lyecs-article-select-group .clear-btn {
    margin-left: 20px;
}

.lyecs-article-select-group .desc {
    margin-left: 20px;
    color: #999;
}

.lyecs-article-select-group .article-selected-list {
    margin-bottom: 20px;
    max-height: 550px;
    overflow-y: auto;
    min-width: 400px
}

.lyecs-article-select-group .article-selected-list-tr {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    border-bottom: 1px solid #f0f0f0;
}

.lyecs-article-select-group .article-selected-list-th {
    color: #333;
    font-weight: bold;
    position: absolute;
    top: 0;
    width: 100%;
    background: #fff;
    height: 50px;
}

.lyecs-article-select-group .article-selected-list-tr .col {
    padding: 10px;
}


.lyecs-article-select-group .article-selected-list-tr .col1 {
    width: 100px;
}

.lyecs-article-select-group .article-selected-list-tr .col2 {
    flex: 1;
}

.lyecs-article-select-group .article-selected-list-tr .col3 {
    width: 80px;
}

.lyecs-article-select-group .article-selected-list-tr .article-info {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}

.lyecs-article-select-group .article-selected-list-tr .article-info img {
    margin-right: 10px;
}
</style>
