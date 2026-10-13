<template>
    <view class="buyer-show-share">
        <tig-popup v-model:show="show" position="bottom">
            <view class="share-title"> {{ $t("分享") }} </view>
            <view class="share-content">
                <view class="share-item" @click="handleCopy">
                    <text class="iconfont-h5 icon-link" />
                    <text class="share-item-text">{{ $t("复制链接") }}</text>
                </view>
                <view class="share-item" @click="handlePoster">
                    <text class="iconfont-h5 icon-tupian" />
                    <text class="share-item-text">{{ $t("生成海报") }}</text>
                </view>
            </view>
        </tig-popup>
        <buyerShowPoster
            v-if="showPoster"
            v-model="showPoster"
            :buyer-show-id="buyerShowId"
            :buyer-name="buyerName"
            :buyer-photo="buyerPhoto"
            :content="content"
            :cover="cover"
        />
    </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { copy } from "@/utils";
import { useConfigStore } from "@/store/config";
import buyerShowPoster from "./buyerShowPoster.vue";
import { buildBuyerShowShareUrl } from "../utils/share";

const { t } = useI18n();
const configStore = useConfigStore();

const props = defineProps<{
    modelValue: boolean;
    buyerShowId: string | number;
    content?: string;
    buyerName?: string;
    buyerPhoto?: string;
    cover?: string;
}>();

const emits = defineEmits<{
    (e: "update:modelValue", value: boolean): void;
}>();

const show = computed({
    get() {
        return props.modelValue;
    },
    set(value) {
        emits("update:modelValue", value);
    }
});

const showPoster = ref(false);

/**
 * 生成海报（参考商品海报）
 */
const handlePoster = () => {
    emits("update:modelValue", false);
    showPoster.value = true;
};

/**
 * 获取分享链接（H5 买家秀详情页链接，用于复制）
 */
const getShareUrl = (): string => {
    return buildBuyerShowShareUrl(props.buyerShowId, configStore.baseInfo.h5Domain || "");
};

/**
 * 分享文案中买家秀内容的最大长度（含末尾省略号），避免长文案被社交平台或输入框二次截断
 */
const MAX_SHARE_DESC_LENGTH = 150;

/**
 * 按字符数截断买家秀内容
 *
 * 用 Array.from 而非 slice 按码元切分，买家秀内容普遍含 emoji，
 * 按码元截断会切出半个代理对导致乱码字符。
 */
const truncateDesc = (text: string): string => {
    const chars = Array.from(text);
    if (chars.length <= MAX_SHARE_DESC_LENGTH) return text;
    return `${chars.slice(0, MAX_SHARE_DESC_LENGTH - 1).join("")}…`;
};

/**
 * 分享文案中的买家秀描述：仅截断正文，无正文时回退「买家名的买家秀」
 */
const getShareDesc = (): string => {
    if (props.content) return truncateDesc(props.content);
    return props.buyerName ? `${props.buyerName}${t("的买家秀")}` : t("买家秀");
};

/**
 * 构建分享文案：【店名】链接 「内容/买家秀」（店名和链接始终完整，只截正文）
 */
const buildShareText = (url: string): string => {
    const shopName = configStore.baseInfo.shopTitle || "";
    return `【${shopName}】${url} 「${getShareDesc()}」`;
};

/**
 * 复制链接
 */
const handleCopy = () => {
    emits("update:modelValue", false);
    const shareUrl = getShareUrl();
    if (!shareUrl) {
        uni.showToast({
            title: t("未配置分享域名"),
            icon: "none"
        });
        return;
    }
    const shareText = buildShareText(shareUrl);
    copy(shareText, () => {
        uni.showToast({
            title: t("复制成功"),
            icon: "none"
        });
    });
};
</script>

<style lang="scss" scoped>
.share-title {
    height: 100rpx;
    line-height: 100rpx;
    text-align: center;
    font-weight: bold;
}
.share-content {
    display: flex;
    padding-bottom: 30rpx;
    .share-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 0 10rpx;

        &:not(:last-child) {
            border-right: 1rpx solid #ddd;
        }

        .iconfont,
        .iconfont-h5 {
            font-size: 70rpx;
            color: #777;
        }

        .iconfont-h5 {
            font-size: 60rpx;
            padding-bottom: 15rpx;
        }
    }
}
</style>
