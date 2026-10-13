<template>
    <tig-layout :title="$t('订单评价')">
        <view v-if="commentEnabled" class="comment-info">
            <template v-for="(item, index) in formState" :key="index">
                <view class="comment-item">
                    <template v-if="!item.isShowed">
                        <view class="white-card">
                            <view class="product flex">
                                <view class="product-img">
                                    <tig-image :src="pickPic(item)" mode="aspectFit" />
                                </view>
                                <view class="product-info">
                                    <view class="product-name">
                                        {{ item.productName }}
                                    </view>
                                    <view class="product-price">
                                        <format-price :show-text="false" :is-bottom="false" :price-data="item.price" />
                                    </view>
                                    <template v-if="hasSkuText(item.skuData)">
                                        <view class="sku-card">
                                            <view class="sku-item line1">{{ getSkuText(item.skuData) }}</view>
                                        </view>
                                    </template>
                                </view>
                            </view>
                            <view class="comment">
                                <view class="flex comment-cell">
                                    <view class="lable-text">{{ $t("商品评分") }}：</view>
                                    <view class="flex">
                                        <uni-rate
                                            :readonly="item.isCommentInfo"
                                            :value="item.commentRank"
                                            color="#bfbfbf"
                                            active-color="#ffa800"
                                            size="18"
                                            @change="onChange(index, $event)"
                                        />
                                        <text style="margin-left: 10rpx">{{ item.commentRank }}{{ $t("分") }}</text>
                                    </view>
                                </view>
                                <template v-if="!item.rankVerification">
                                    <view class="flex comment-cell">
                                        <view class="lable-text" />
                                        <view class="tips">{{ $t("请先评分商品") }}</view>
                                    </view>
                                </template>
                                <view class="comment-cell">
                                    <view v-if="!item.isCommentInfo" class="label-list flex-wrap">
                                        <template v-for="(lb, lBindex) in item.allCommentTag" :key="lBindex">
                                            <view :class="{ active: item.commentTag.includes(lb) }" class="label-item" @click="checkTag(index, lb)">
                                                <text>{{ lb }}</text>
                                            </view>
                                        </template>

                                        <view v-if="!item.isAdd" class="label-edit" @click="isShowadd(index)">
                                            <view />
                                            <view>{{ $t("自定义") }}</view>
                                        </view>
                                        <view v-else class="label-edit-input flex align-center justify-between">
                                            <view>
                                                <input v-model="tabInput" type="text" auto-focus :placeholder="$t('请输入自定义标签')" />
                                            </view>
                                            <tig-button class="btn" @click="tabAdd(index)">{{ $t("确定") }}</tig-button>
                                        </view>
                                    </view>
                                    <view v-else class="label-list">
                                        <template v-for="(lb, lBindex) in item.commentTag" :key="lBindex">
                                            <view class="label-item-txt">
                                                <text>{{ lb }}</text>
                                            </view>
                                        </template>
                                    </view>
                                </view>
                                <view class="comment-cell">
                                    <view class="textarea-box">
                                        <textarea
                                            v-model="item.content"
                                            cols="50"
                                            name="content"
                                            :placeholder="$t('商品是否给力？快分享你的购买心得吧~')"
                                            rows="5"
                                            :maxlength="500"
                                            style="border: 1px solid #d9d9d9; padding: 4px"
                                            :disabled="item.isCommentInfo"
                                        />
                                        <view class="strlangth">{{ $t("10-500字") }}</view>
                                    </view>
                                </view>
                                <view class="comment-cell flex align-center justify-between">
                                    <view class="tips">
                                        <text v-if="!item.contentVerification">{{ $t("请填写购买心得") }}</text>
                                    </view>
                                </view>
                                <view class="comment-cell">
                                    <view class="lable-text">{{ $t("添加图片") }}</view>
                                    <view class="pic-box flex-wrap">
                                        <template v-for="(product, picIndex) in item.showPics" :key="picIndex">
                                            <view class="pic-img" style="transition: opacity 0.2s linear; margin-right: 10px">
                                                <tig-image :src="product.picThumb" mode="aspectFill" @click="imagePreview(imageFormat(product.picThumb))" />
                                                <view class="close-icon" @click.stop="close(item, Number(picIndex))">
                                                    <text class="iconfont-h5 icon-chacha close-icon-text" />
                                                </view>
                                            </view>
                                        </template>
                                        <!-- <template v-if="item.showPics.length > 0"> -->
                                        <tig-upload v-model="item.showPics" :count="9">
                                            <view class="add-img">
                                                <text class="iconfont-h5 icon-zengjia" style="font-size: 52rpx; color: #cccccc" />
                                            </view>
                                        </tig-upload>
                                        <!-- </template> -->
                                    </view>
                                </view>
                                <!-- 未评价条目可按配置选择匿名评价 -->
                                <view v-if="anonymousEnabled && !item.isCommentInfo" class="anonymous-row flex align-center">
                                    <tig-checkbox v-model:checked="item.isAnonymous" size="20" />
                                    <text class="anonymous-text" @click="item.isAnonymous = !item.isAnonymous">{{ $t("匿名评价") }}</text>
                                    <text v-if="anonymousPreview" class="anonymous-name">{{ anonymousPreview }}</text>
                                </view>
                            </view>
                        </view>
                    </template>
                </view>
            </template>
        </view>
        <empty-box v-else center-content height="calc(100vh - var(--nav-height) - var(--safe-bottom, 0px))" mode="list" :text="$t('评价功能已关闭')" />

        <tig-fixed-placeholder v-if="commentEnabled && pendingCommentItems.length" background-color="#fff">
            <view class="btn-box">
                <tig-button class="btn" :disabled="isSubmitting" :loading="isSubmitting" @click="onSubmit">{{ submitButtonText }}</tig-button>
            </view>
        </tig-fixed-placeholder>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 订单评价页（发表评价）
 * - 对订单商品进行评分（1-5星）
 * - 选择或自定义评价标签
 * - 填写评价内容（10-500字）
 * - 上传晒单图片（最多9张）
 * - 提交后自动跳转待晒单列表
 */
import { getCommentData, updateCommentData } from "@/api/user/comment";
import { computed, ref } from "vue";
import { onLoad, onUnload } from "@dcloudio/uni-app";
import { imageFormat, pickPic } from "@/utils/format";
import { getSkuText, hasSkuText } from "@/utils";
import { useI18n } from "vue-i18n";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";

const { t } = useI18n();
const configStore = useConfigStore();
const userStore = useUserStore();
const commentEnabled = computed(() => configStore.productCommentEnabled === 1);
const anonymousEnabled = computed(() => commentEnabled.value && configStore.anonymousCommentEnabled === 1);
// 掩码展示名由后端下发（DD-19），前端不复制掩码规则
const anonymousPreview = computed(() => userStore.userInfo?.anonymousUsername || "");

const close = (item: any, index: number) => {
    item.showPics.splice(index, 1);
};
const onChange = (index: any, e: any) => {
    formState.value[index].commentRank = e.value;
};
const commentId = ref<string>("");
const labelList = ref([t("送货快"), t("质量很好"), t("服务周到"), t("正品"), t("很划算"), t("包装仔细"), t("价格实惠"), t("还可以"), t("相当好")]);
const formState = ref<any[]>([]);
// 页面展示所有未晒单商品；提交时再筛未评价项和本次补了图的项
const pendingCommentItems = computed(() => formState.value.filter((item: any) => !item.isShowed));
const hasUncommentedItems = computed(() => pendingCommentItems.value.some((item: any) => !item.isCommentInfo));
const submitButtonText = computed(() => (hasUncommentedItems.value ? t("提交评价") : t("提交晒单")));
const isSubmitting = ref(false);
const fetchCommentData = async (id: string) => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getCommentData({ id: id });
        result.items.map((data: any) => {
            data.allCommentTag = JSON.parse(JSON.stringify(labelList.value)); // 使用深拷贝来创建独立的数组
            Object.assign(
                data,
                {
                    commentRank: 0,
                    content: "",
                    showPics: [],
                    commentTag: [],
                    isAdd: false,
                    rankVerification: true,
                    contentVerification: true,
                    isAnonymous: false,
                    isCommentInfo: data.commentInfo ? true : false
                },
                data.commentInfo
            );
            // 后端可能返回 0/1，统一为 Boolean 供 tig-checkbox 使用
            data.isAnonymous = Number(data.isAnonymous ?? 0) === 1;
        });
        Object.assign(formState.value, result.items);
        const allAreTrue = formState.value.every((obj: any) => obj.isShowed === 1);
        const allAreFalse = formState.value.every((obj: any) => obj.isShowed === 0);
        if (allAreTrue) {
            // #ifdef H5
            history.back();
            // #endif
            // #ifndef H5
            uni.navigateBack();
            // #endif
            uni.$emit("commentFinished", true);
        } else if (allAreFalse) {
            // 全部未晒单
            return true;
        }
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    } finally {
        uni.hideLoading();
    }
};
const tabInput = ref("");
const isShowadd = (index: number) => {
    formState.value[index].isAdd = !formState.value[index].isAdd;
};
const tabAdd = (index: number) => {
    if (tabInput.value !== "") {
        const newCommentTag = JSON.parse(JSON.stringify(formState.value[index].allCommentTag));
        if (newCommentTag.includes(tabInput.value)) {
            uni.showToast({
                title: t("您已添加相同标签"),
                icon: "none",
                duration: 1000
            });
            return;
        }
        newCommentTag.push(tabInput.value);
        formState.value[index].allCommentTag = newCommentTag;
        checkTag(index, tabInput.value);
    }
    tabInput.value = "";
    formState.value[index].isAdd = false;
};
const checkTag = (index: number, label: string) => {
    let arr = formState.value[index].commentTag;
    if (arr.includes(label)) {
        formState.value[index].commentTag = arr.filter((item: any) => item !== label);
    } else {
        arr.push(label);
    }
};
const imagePreview = (url: string) => {
    uni.previewImage({
        urls: [url]
    });
};
/**
 * 提交未评价商品，以及本次主动补了晒单图的已评价商品
 * - 已评价但未补图的条目不参与本次校验和提交，避免挡住其他商品首次评价
 * - 发请求前只校验本次要提交的条目，避免部分商品校验失败时产生半提交状态
 * - 逐条提交；中途失败时保留失败项和后续商品的草稿
 * - 全部成功后只刷新一次列表
 */
const validateCommentItem = (item: any) => {
    item.rankVerification = item.commentRank >= 1;
    item.contentVerification = item.content !== "";

    if (item.commentRank < 1) {
        return false;
    }

    if (item.content == "") {
        return false;
    }

    if (item.content.length < 10 || item.content.length > 500) {
        uni.showToast({
            title: t("购买心得在10-500字之间"),
            icon: "none",
            duration: 1000
        });
        return false;
    }

    if (item.isCommentInfo && (!item.showPics || item.showPics.length === 0)) {
        uni.showToast({
            title: t("请添加晒单图片"),
            icon: "none",
            duration: 1000
        });
        return false;
    }

    return true;
};

const getCommentPayload = (item: any) => ({
    productId: item.productId,
    orderId: item.orderId,
    orderItemId: item.itemId,
    shopId: item.shopId,
    commentRank: item.commentRank,
    commentTag: item.commentTag,
    content: item.content,
    showPics: item.showPics,
    isAnonymous: anonymousEnabled.value && item.isAnonymous ? 1 : 0
});

const hasShowPics = (item: any) => Array.isArray(item.showPics) && item.showPics.length > 0;

const onSubmit = async () => {
    if (isSubmitting.value) return;

    const items = pendingCommentItems.value.filter((item: any) => !item.isCommentInfo || hasShowPics(item));
    if (!items.length) {
        uni.showToast({
            title: t("请添加晒单图片"),
            icon: "none",
            duration: 1000
        });
        return;
    }
    if (!items.every(validateCommentItem)) return;

    isSubmitting.value = true;
    try {
        for (const item of items) {
            await updateCommentData(getCommentPayload(item));
            // 若后续某条失败，已成功项不再参与重试，其余项继续保留页面草稿
            item.isShowed = 1;
        }
        uni.showToast({
            title: t("提交成功"),
            icon: "none",
            duration: 1000
        });
        await new Promise((resolve) => setTimeout(resolve, 1000));
        const flag = await fetchCommentData(commentId.value);
        // 跳转到待晒单
        if (flag) {
            uni.reLaunch({
                url: "/pages/user/comment/list?currentType=1"
            });
        }
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    } finally {
        isSubmitting.value = false;
    }
};
onLoad(async (option) => {
    // 读取初始化后的配置后再请求评价数据
    await configStore.whenConfigReady();
    if (!commentEnabled.value) return;
    // 匿名开关开启时才需要拿展示名做预览
    if (anonymousEnabled.value) {
        userStore.getUserInfo();
    }
    if (option) {
        const { id } = option;
        if (id) {
            commentId.value = id;
            // 获取详情数据
            fetchCommentData(id);
        }
    }
});

// 关闭预览图片
onUnload(() => {
    // #ifdef  H5 || APP
    uni.closePreviewImage();
    // #endif
});
</script>

<style lang="scss" scoped>
.btn-box {
    padding: 25rpx;

    .btn {
        font-size: 28rpx;
    }
}

.comment-info {
    padding: 20rpx;
    .comment-item {
        .white-card {
            background-color: #fff;
            padding: 20rpx;
            border-radius: 20rpx;
            margin-bottom: 20rpx;
            .product {
                margin-bottom: 25rpx;
                .product-img {
                    width: 130rpx;
                    height: 130rpx;
                    background-color: rd;
                    margin-right: 20rpx;
                    overflow: hidden;
                }
                .product-info {
                    flex: 1;
                    font-size: 24rpx;
                    line-height: 32rpx;
                    .product-price {
                        color: var(--general);
                        margin-top: 10rpx;
                    }
                }
            }
            .comment {
                .comment-cell {
                    font-size: 24rpx;
                    padding-bottom: 20rpx;
                    .lable-text {
                        width: 130rpx;
                    }
                    .tips {
                        color: var(--general);
                    }
                    .label-list {
                        .label-item,
                        .label-edit,
                        .label-item-txt {
                            display: inline-block;
                            background-color: #f5f5f5;
                            padding: 8rpx 15rpx;
                            margin-right: 15rpx;
                            margin-bottom: 15rpx;
                            border-radius: 5rpx;
                        }
                        .active {
                            background-color: var(--general);
                            color: #fff;
                        }
                        .label-edit-input {
                            width: 100vw;
                            padding: 10rpx 0;
                            input {
                                width: 68vw;
                                font-size: 24rpx;
                                padding: 15rpx;
                                background-color: #f5f5f5;
                            }
                            .btn {
                                padding: 16rpx 30rpx;
                                border-radius: 5rpx;
                            }
                        }
                    }
                    .label-list-edit {
                        .label-item {
                            .iconfont {
                                margin-left: 10rpx;
                            }
                        }
                        .label-edit-input {
                            input {
                                width: 200rpx;
                            }
                        }
                    }
                    .textarea-box {
                        width: 100%;
                        padding: 20rpx 20rpx 0rpx;
                        border-radius: 10rpx;
                        background-color: #f5f5f5;
                        textarea {
                            width: 98%;
                            font-size: 24rpx;
                            border: none !important;
                        }
                    }

                    .strlangth {
                        text-align: end;
                        color: #999;
                        font-size: 22rpx;
                        padding: 2rpx 6px 10rpx;
                    }
                    .pic-box {
                        margin-top: 20rpx;
                        .pic-img {
                            width: 130rpx;
                            height: 130rpx;
                            margin-bottom: 20rpx;
                            position: relative;
                            overflow: hidden;
                            uni-image {
                                border-radius: 10rpx;
                            }
                            .close-icon {
                                position: absolute;
                                top: 4rpx;
                                right: 4rpx;
                                width: 38rpx;
                                height: 38rpx;
                                border-radius: 50%;
                                background: rgba(0, 0, 0, 0.6);
                                z-index: 10;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                .close-icon-text {
                                    font-size: 22rpx;
                                    color: #fff;
                                    line-height: 1;
                                }
                            }
                        }
                        .add-img {
                            width: 130rpx;
                            height: 130rpx;
                            border: 1rpx solid #c6c4c4;
                            border-radius: 10rpx;
                            text-align: center;
                            line-height: 130rpx;
                        }
                    }
                }
            }
        }
        .anonymous-row {
            font-size: 24rpx;
            padding-bottom: 20rpx;
            color: #999999;
            .anonymous-text {
                margin-left: 10rpx;
            }
            .anonymous-name {
                margin-left: 10rpx;
                color: #666666;
            }
        }
    }
}
</style>
