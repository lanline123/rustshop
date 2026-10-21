<template>
    <div class="container">
        <div class="content_wrapper">
            <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                <el-form-item prop="type" :label="$t('搜索页风格：')">
                    <div class="page_tab">
                        <div class="item" v-for="(item, index) in pagesList" :key="index" @click="formState.type = index + 1">
                            <div class="preview" :class="[item.previewClass, { active: formState.type == index + 1 }]">
                                <img :src="item.previewImg" :alt="item.title" class="preview-img" />
                            </div>
                            <div class="title">{{ item.title }}</div>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label=" ">
                    <el-button class="btn" type="primary" @click="onSubmit()">{{ $t('保 存') }}</el-button>
                    <el-button class="btn" @click="onReset()">{{ $t('重 置') }}</el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { onMounted, ref } from "vue";
import { getConfigSearchStyle, saveConfigSearchStyle } from "@/api/setting/config";
import { message } from "ant-design-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const pagesList = ref<any[]>([
    {
        title: t('样式一（经典版）'),
        previewClass: "classic",
        previewImg: "https://oss.tigshop.com/img/gallery/202606/1782373674JqSs1sxbDajftKgthu.jpeg"
    },
    {
        title: t('样式二（新版）'),
        previewClass: "modern",
        previewImg: "https://oss.tigshop.com/img/gallery/202606/1782373674PUhO83DhgZKLxbXOuQ.jpeg"
    }
]);
const formState = ref<any>({
    type: 1
});
const loading = ref<boolean>(true);

const loadFilter = async () => {
    try {
        const result = await getConfigSearchStyle();
        formState.value.type = result.searchStyleType || 1;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onReset = async () => {
    formState.value.type = 1;
    onSubmit();
};

const onSubmit = async () => {
    try {
        await saveConfigSearchStyle({
            searchStyleType: formState.value.type
        });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};

onMounted(() => {
    loadFilter();
});
</script>

<style lang="less" scoped>
.page_tab {
    display: flex;
    flex-wrap: wrap;
    .item {
        width: 260px;
        margin-right: 40px;
        text-align: center;
        font-size: 14px;
        margin-bottom: 30px;
        cursor: pointer;
        .preview {
            width: 100%;
            height: 180px;
            margin-bottom: 10px;
            border: 2px solid #eee;
            border-radius: 10px;
            background: #fff;
            overflow: hidden;
            box-sizing: border-box;
            .preview-img {
                width: 100%;
                height: 100%;
                //object-fit: cover;
                object-position: top center;
                display: block;
            }
        }
        .active {
            border: 2px solid var(--tig-primary);
        }
        .active + .title {
            color: var(--tig-primary);
        }
    }
}
</style>
