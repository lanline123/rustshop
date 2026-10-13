<template>
    <tig-layout :title="$t('文章中心')">
        <template v-if="!loading">
            <view class="article-center">
                <view class="title">{{ formArticleState.articleTitle }}</view>
                <view v-if="formArticleState.addTime" class="time">{{ $t("时间") }}：{{ formArticleState.addTime }}</view>
                <view v-if="formArticleState.content" class="content">
                    <template v-if="formArticleState.articleThumb">
                        <tig-image :src="formArticleState.articleThumb" />
                    </template>
                    <rich-text class="v-html" :nodes="processedContent" />
                    <template v-if="productIds && productIds.length">
                        <productItem :product-ids="productIds" />
                    </template>
                </view>
                <block v-else>
                    <empty-box mode="news" :text="$t('暂无内容')" />
                </block>
                <tig-fixed-placeholder height="70rpx" background-color="#fff">
                    <view class="navigation">
                        <view class="navigation-btn navigation-btn-left">
                            <navigator
                                v-if="formArticleState.prev"
                                :url="`/pages/article/issue/detail?id=${formArticleState.prev?.articleId}`"
                                hover-class="navigator-hover"
                                open-type="navigate"
                            >
                                «{{ $t("上一篇") }}：{{ formArticleState.prev?.articleTitle }}
                            </navigator>
                        </view>
                        <view class="navigation-btn navigation-btn-right">
                            <navigator
                                v-if="formArticleState.next?.articleId"
                                :url="`/pages/article/issue/detail?id=${formArticleState.next?.articleId}`"
                                hover-class="navigator-hover"
                                open-type="navigate"
                            >
                                {{ $t("下一篇") }}：{{ formArticleState.next?.articleTitle }}»
                            </navigator>
                        </view>
                    </view>
                </tig-fixed-placeholder>
            </view>
        </template>
    </tig-layout>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { getArticle } from "@/api/article/article";
import type { ArticleFormState } from "@/types/article/article";
import { onLoad, onUnload } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";
import productItem from "../src/productItem.vue";
import { setPageMeta, setJsonLd, extractDescription, clearJsonLd } from "@/utils/seo";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();
const configStore = useConfigStore();

const props = defineProps({
    id: {
        type: String || Number,
        default: ""
    }
});

const loading = ref(false);
const formArticleState = ref<ArticleFormState>({});
const productIds = ref([]);

// 处理富文本内容，给 img 标签添加内联样式，防止小程序端图片溢出
const processedContent = computed(() => {
    const content = formArticleState.value.content;
    if (!content) return "";
    return content.replace(/<img([^>]*)>/gi, (match: string, attrs: string) => {
        if (/style\s*=/i.test(attrs)) {
            return match.replace(/style\s*=\s*["']([^"']*)["']/i, 'style="$1;max-width:100%;height:auto;display:block;"');
        }
        return `<img style="max-width:100%;height:auto;display:block;"${attrs}>`;
    });
});

const _getArticle = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        loading.value = true;
        const result: any = await getArticle({ id: props.id }, "issueInfo");
        productIds.value = result.item.productIds || [];
        Object.assign(formArticleState.value, result.item);
        // #ifdef H5
        setPageMeta({
            title: `${result.item.articleTitle} - ${configStore.baseInfo.shopTitle}`,
            description: extractDescription(result.item.content || ""),
            type: "article"
        });

        setJsonLd({
            "@context": "https://schema.org",
            "@type": "Article",
            headline: result.item.articleTitle,
            description: extractDescription(result.item.content || ""),
            datePublished: result.item.addTime,
            publisher: {
                "@type": "Organization",
                name: configStore.baseInfo.shopTitle
            }
        });
        // #endif
        formArticleState.value.prev = result.prev;
        formArticleState.value.next = result.next;
    } catch (error: any) {
        console.error(error.message);
    } finally {
        loading.value = false;
        uni.hideLoading();
    }
};

onUnload(() => {
    // #ifdef H5
    clearJsonLd();
    // #endif
});

onLoad(() => {
    _getArticle();
});
</script>

<style lang="scss" scoped>
page {
    background-color: #fff;
}
.article-center {
    padding: 24rpx;
    .title {
        line-height: 1.5;
        font-weight: 600;
        text-align: left;
        color: #000000;
        font-size: 32rpx;
    }
    .time {
        color: #888888;
        padding: 20rpx 0;
        text-align: center;
        font-size: 24rpx;
    }
    .content {
        padding: 24rpx 12rpx;
        .v-html {
            overflow: hidden;
            :deep(p) {
                font-size: 24rpx;
                line-height: 2;
            }
            :deep(img) {
                max-width: 100%;
                width: 100%;
                display: block;
            }
        }
    }
}
.empty {
    color: #999;
    padding: 48rpx 0;
    text-align: center;
    font-size: 28rpx;
}

.navigation {
    height: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 24rpx;
    padding: 0 24rpx;

    .navigation-btn {
        cursor: pointer;
        flex: 1;
        width: 50%;
        navigator {
            white-space: nowrap;
            max-width: 80%;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    }
    .navigation-btn-left {
        display: flex;
    }
    .navigation-btn-right {
        display: flex;
        justify-content: flex-end;
    }
}
</style>
