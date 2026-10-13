<template>
    <view class="page-content">
        <view class="thanos-layout">
            <view class="thanos-layout-header">
                <view class="wapim-conversation-banner">
                    <view class="shop-info" @click="returnFun">
                        <view class="iconfont icon-houtui" />
                    </view>
                    <template v-if="shopInfo.shopTitle">
                        <view class="banner-title">{{ $t(shopInfo.shopTitle) }}</view>
                    </template>
                    <!-- <view class="right-opt" @click="handleToList(`/pages/im/list`)">
                        <text class="ico-history wapim-conversation-banner-history" />
                    </view> -->
                </view>
            </view>
            <view class="thanos-layout-main-content" @click="handleSetRead">
                <scroll-view class="message-scroll" :scroll-top="scrollTop" scroll-y="true" @scroll="handleScroll">
                    <view class="message-box">
                        <view v-if="loadend" class="empty-text">{{ $t("已经没有更多消息了") }}</view>
                        <template v-for="item in messageList" :key="item.localKey || item.id">
                            <view
                                class="thanos-rich-message"
                                :class="[
                                    item.type == 2 ? 'in' : item.type == 1 ? 'out' : '',
                                    item.messageType === 'custom' && item.type == 1
                                        ? 'card-type'
                                        : item.messageType === 'image' || item.messageType === 'video'
                                          ? 'image-type'
                                          : '',
                                    enteringKeys.includes(item.localKey || String(item.id)) ? 'msg-enter' : ''
                                ]"
                            >
                                <view v-if="item.messageType !== 'product' && item.messageType !== 'order'" class="message-time">
                                    <span>{{ item.sendTime }}</span>
                                </view>
                                <view v-if="item.type === 3" class="message-time">
                                    <span>{{ item.content.content }}</span>
                                </view>
                                <view
                                    v-if="item.type != 3 && item.messageType !== 'product' && item.messageType !== 'order'"
                                    class="message-content message-content-left"
                                >
                                    <view class="bg-pic circle-bg-pic thanos-view thanos-hairline--surround message-avatar">
                                        <view
                                            class="bg-pic-content"
                                            :style="{
                                                backgroundImage: `url(${item.type === 1 ? imageFormat(item.user?.avatar) : imageFormat(shopLogo)})`
                                            }"
                                        />
                                    </view>
                                    <view class="message-content-inner">
                                        <template v-if="item.messageType">
                                            <view class="message-context">
                                                <view class="thanos-text-message">
                                                    <view v-if="item.messageType === 'text'">
                                                        <text v-if="!isEmojiText(item.content.content)">{{ item.content.content }}</text>
                                                        <emojiHtml v-else :content="item.content.content" />
                                                    </view>

                                                    <view v-if="item.messageType === 'image'" class="message-image">
                                                        <tig-image :src="item.content.pic" />
                                                    </view>
                                                    <view v-if="item.messageType === 'video'" class="message-video">
                                                        <view v-if="item.localPending" class="video-sending">
                                                            <image
                                                                v-if="item.localCover"
                                                                class="video-sending-poster"
                                                                :src="item.localCover"
                                                                mode="aspectFill"
                                                            />
                                                            <view class="video-sending-mask">
                                                                <view class="video-sending-spinner" />
                                                                <view class="video-sending-percent">
                                                                    {{ item.localProcessing ? $t("处理中") : (item.localPercent || 0) + "%" }}
                                                                </view>
                                                            </view>
                                                        </view>
                                                        <video
                                                            v-else
                                                            :src="imageFormat(item.content.videoUrl)"
                                                            :poster="item.content.videoCover ? imageFormat(item.content.videoCover) : ''"
                                                            class="im-video"
                                                            controls
                                                            object-fit="contain"
                                                        />
                                                    </view>
                                                    <view v-if="item.messageType === 'custom'">
                                                        <view v-if="item.content.product" class="custom-message-product">
                                                            <view class="product-img" @click="handleLink(`detail`, item)">
                                                                <tig-image
                                                                    v-if="item.content.product.picUrl"
                                                                    mode="widthFix"
                                                                    :src="pickPic(item.content.product)"
                                                                />
                                                            </view>
                                                            <view class="product-info">
                                                                <view class="product-name">
                                                                    {{ $t(item.content.product.productName) }}
                                                                </view>
                                                                <view class="product-price">
                                                                    <format-price
                                                                        :font-style="{ fontSize: '28rpx' }"
                                                                        :currency-style="{
                                                                            fontSize: '22rpx'
                                                                        }"
                                                                        :decimals-style="{
                                                                            fontSize: '24rpx'
                                                                        }"
                                                                        :price-data="item.content.product.productPrice"
                                                                    />
                                                                </view>
                                                            </view>
                                                        </view>
                                                        <view v-if="item.content.order" class="custom-message-order" @click="handleLink(`order`, item)">
                                                            <view class="tit">
                                                                {{ item.content.order.orderStatusName }}
                                                            </view>
                                                            <view class="info">
                                                                <view class="product-img">
                                                                    <tig-image
                                                                        v-if="item.content.order.picUrl"
                                                                        mode="widthFix"
                                                                        :src="pickPic(item.content.order)"
                                                                    />
                                                                </view>
                                                                <view class="product-info">
                                                                    <view class="product-name">
                                                                        {{ $t(item.content.order.productName) }}
                                                                    </view>
                                                                    <view class="product-txt">
                                                                        {{
                                                                            isOverseas()
                                                                                ? $t("共{0}件商品，合计", [item.content.order.productNum])
                                                                                : `共${item.content.order.productNum}件商品，合计`
                                                                        }}
                                                                        <format-price
                                                                            :font-style="{ fontSize: '28rpx' }"
                                                                            :currency-style="{
                                                                                fontSize: '22rpx'
                                                                            }"
                                                                            :decimals-style="{
                                                                                fontSize: '24rpx'
                                                                            }"
                                                                            :price-data="item.content.order.totalAmount"
                                                                        />
                                                                    </view>
                                                                </view>
                                                            </view>
                                                        </view>
                                                    </view>
                                                </view>
                                                <span class="message-status" />
                                            </view>
                                        </template>
                                    </view>
                                    <view v-if="item.type === 1" class="read-status" :class="[item.isRead ? 'read' : 'unread']">
                                        {{ $t(item.isRead ? "已读" : "未读") }}
                                    </view>
                                </view>
                                <view v-if="item.messageType === 'product'" class="im-goods-card">
                                    <view class="content">
                                        <view class="pic">
                                            <tig-image mode="widthFix" :src="pickPic(item)" />
                                        </view>
                                        <view class="desc">
                                            <view class="title">
                                                {{ $t(item.productName) }}
                                            </view>
                                            <view class="price">
                                                <format-price
                                                    :font-style="{ fontSize: '32rpx' }"
                                                    :currency-style="{
                                                        fontSize: '22rpx'
                                                    }"
                                                    :decimals-style="{
                                                        fontSize: '24rpx'
                                                    }"
                                                    :price-data="item.productPrice"
                                                />
                                            </view>
                                        </view>
                                    </view>
                                    <view class="line" />
                                    <view class="send" @click="sendGoods(item)">{{ $t("发送宝贝") }}</view>
                                </view>
                            </view>
                        </template>
                    </view>
                </scroll-view>
            </view>
            <view class="thanos-layout-footer">
                <view class="wapim-inputer">
                    <view class="thanos-hairline--top-bottom wapim-main-inputer">
                        <view class="input-box">
                            <view class="rich-input">
                                <up-input
                                    v-model="textAreaValue"
                                    style="height: 100%"
                                    placeholder=""
                                    border="none"
                                    :adjust-position="adjustPosition"
                                    @confirm="sendTxt"
                                    @change="onInput"
                                    @focus="onFocus"
                                    @blur="showKeyboard = false"
                                    @keyboardheightchange="handleKeyboardheightchange"
                            /></view>
                        </view>
                        <view class="wapim-inputer-operation">
                            <span class="operation face" @click="showEmojiHandle" />
                            <template v-if="hasTxt === true">
                                <span class="operation send" @click="sendTxt">{{ $t("发送") }}</span>
                            </template>
                            <template v-else>
                                <view class="operation plus" @click="showMoreHandle" />
                            </template>
                        </view>
                    </view>
                    <view v-show="moreHandle" class="wapim-inputer-plugin">
                        <view class="wapim-inputer-file-picker">
                            <view class="file-inputer-plugin">
                                <view class="pic-item plugin-item" @click="chooseMediaFile">
                                    <view class="photo icon" />
                                    <span class="title">{{ $t("相册") }}</span>
                                </view>
                                <!-- <view class="goods-list-item plugin-item">
                                    <view class="photo icon"></view>
                                    <span class="title">商品</span>
                                </view> -->
                            </view>
                        </view>
                    </view>
                    <view v-show="moreEmoji" class="wapim-inputer-plugin">
                        <view class="wapim-inputer-file-picker">
                            <view class="file-emoji-plugin">
                                <Emoji @insert-emoji="onInsertEmoji" />
                            </view>
                        </view>
                    </view>
                </view>
            </view>
        </view>
        <promptTone ref="promptToneRef" />
    </view>
</template>
<script lang="ts" setup>
/**
 * 聊天详情页
 * - 与客服/商家实时聊天
 * - WebSocket实时消息收发
 * - 支持文字、图片、表情、商品卡片
 */
import { ref, nextTick, reactive, getCurrentInstance, computed } from "vue";
import WebSocketClient from "@/utils/websocket";
import { sendMessage, getConversationMessage, setRead } from "@/api/im/im";
import { getShopDetail } from "@/api/shop/shop";
import { getProductDetail } from "@/api/product/product";
import { getOrder } from "@/api/user/order";
import { imageFormat, pickPic } from "@/utils/format";
import { getElementRect } from "@/utils/index";
import Emoji from "./src/Emoji.vue";
import emojiHtml from "./src/emojiHtml.vue";
import promptTone from "./src/promptTone.vue";
import { onLoad } from "@dcloudio/uni-app";
import { useConfigStore } from "@/store/config";
import { getSecret, baseUrl } from "@/utils/request";
import { staticResource, isJsonString } from "@/utils";
import { useI18n } from "vue-i18n";
import { isOverseas } from "@/utils";
import { useUserStore } from "@/store/user";

const { t } = useI18n();

const configStore = useConfigStore();

const userStore = useUserStore();

const filterParams = reactive<any>({
    firstId: -1,
    sortOrder: "desc",
    userFrom: "",
    shopId: 0
});
const messageList = ref<any[]>([]);
const textAreaValue = ref<string>("");
const hasTxt = ref<boolean>(false);
const moreHandle = ref<boolean>(false);
const moreEmoji = ref<boolean>(false);
let isRecord = ref<boolean>(true);
let lastScrollTop = ref<number>(0);
const loadend = ref<boolean>(false);

const scrollTop = ref(0);
const instance = getCurrentInstance();

// 记录需要播放入场动画的消息，动画结束后移除，避免列表重渲染时反复播放
const enteringKeys = ref<string[]>([]);
const markMessageEnter = (key: string) => {
    if (!key) return;
    enteringKeys.value = [...enteringKeys.value, key];
    setTimeout(() => {
        enteringKeys.value = enteringKeys.value.filter((item) => item !== key);
    }, 320);
};

const returnFun = () => {
    // #ifdef H5
    history.back();
    // #endif

    // #ifndef H5
    uni.navigateBack();
    // #endif
};

const messageScrollHeight = ref(0);
const messageBoxHeight = ref(0);
const getMessageScrollHeight = async () => {
    try {
        const result = await getElementRect(".message-scroll", instance);
        messageScrollHeight.value = result!.height;
    } catch (error) {
        console.error("Error getting box info:", error);
    }
};
const getMessageBoxHeight = async () => {
    try {
        const result = await getElementRect(".message-box", instance);
        messageBoxHeight.value = result!.height;
    } catch (error) {
        console.error("Error getting box info:", error);
    }
};

const scrollToBottom = async () => {
    await getMessageBoxHeight();
    await getMessageScrollHeight();
    console.log("messageScrollHeight", messageScrollHeight.value, "messageBoxHeight", messageBoxHeight.value);
    if (messageScrollHeight.value && messageBoxHeight.value) {
        const top = messageBoxHeight.value - messageScrollHeight.value;
        if (top && top > 0) {
            scrollTop.value = top;
        }
    }
};

const handleScroll = (event: any) => {
    handleSetRead();
    if (isRecord.value) {
        lastScrollTop.value = scrollTop.value;
        isRecord.value = false;
    }
    if (event.detail.scrollTop == 0 && loadend.value == false && messageList.value.length > 0) {
        const firstSentMessage = messageList.value.find((item: any) => !item.localPending);
        if (!firstSentMessage) return;
        filterParams.firstId = firstSentMessage.id;
        _getConversationMessage();
    }
};
// 监听实时输入事件
function onInput(value: string) {
    hasTxt.value = value.length > 0 ? true : false;
}
const adjustPosition = computed(() => {
    let val = true;
    // #ifdef MP-WEIXIN
    val = false;
    // #endif
    return val;
});
const keyboardHeight = ref(0);
const showKeyboard = ref(false);
const handleKeyboardheightchange = (e: any) => {
    // #ifdef MP-WEIXIN
    keyboardHeight.value = e.detail.height;
    console.log("handleKeyboardheightchange", e.detail.height);
    // #endif
};

const onFocus = () => {
    showKeyboard.value = true;
    moreEmoji.value = false;
    moreHandle.value = false;
};
const onInsertEmoji = (emoji: string) => {
    textAreaValue.value += "[" + emoji + "]";
    // #ifdef MP-WEIXIN || H5
    hasTxt.value = textAreaValue.value.length > 0 ? true : false;
    //  #endif
};
const mediaExtensions = [".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".mp4"];

const isVideoFile = (file: any) => {
    const type = file?.type || "";
    if (type === "video" || type.indexOf("video/") === 0) return true;
    const path = file?.name || file?.url || "";
    return /\.(mp4|mov|m4v|3gp|avi)$/i.test(path);
};

// 图片与视频共用同一个入口，按文件类型分派到各自的上传逻辑
const dispatchMediaFile = (file: any) => {
    if (!file?.url) return;
    if (isVideoFile(file)) {
        uploadVideoFile(file);
    } else {
        uploadFile(file);
    }
};

const chooseImageFile = () => {
    uni.chooseImage({
        count: 1,
        success: (res: any) => {
            const paths = res.tempFilePaths || [];
            const files = res.tempFiles || [];
            files.forEach((item: any, index: number) => {
                dispatchMediaFile({
                    url: item.path || paths[index],
                    size: item.size,
                    name: item.name,
                    type: "image"
                });
            });
        }
    });
};

const chooseVideoFile = () => {
    uni.chooseVideo({
        sourceType: ["album", "camera"],
        success: (res: any) => {
            dispatchMediaFile({
                url: res.tempFilePath,
                size: res.size,
                thumb: res.thumbTempFilePath || "",
                type: "video"
            });
        }
    });
};

// 各端可用的选择能力不同，微信与 H5 可一次性混选，其余端先让用户选类型
const chooseMediaFile = () => {
    // #ifdef MP-WEIXIN
    uni.chooseMedia({
        count: 1,
        mediaType: ["image", "video"],
        success: (res: any) => {
            (res.tempFiles || []).forEach((item: any) => {
                dispatchMediaFile({
                    url: item.tempFilePath,
                    size: item.size,
                    thumb: item.thumbTempFilePath || "",
                    type: item.fileType
                });
            });
        }
    });
    // #endif

    // #ifdef H5
    uni.chooseFile({
        count: 1,
        extension: mediaExtensions,
        success: (res: any) => {
            const paths = res.tempFilePaths || [];
            (res.tempFiles || []).forEach((item: any, index: number) => {
                dispatchMediaFile({
                    url: paths[index] || item.path,
                    size: item.size,
                    name: item.name,
                    type: item.type
                });
            });
        }
    });
    // #endif

    // #ifndef MP-WEIXIN || H5
    uni.showActionSheet({
        itemList: [t("照片"), t("视频")],
        success: (res: any) => {
            if (res.tapIndex === 0) {
                chooseImageFile();
            } else {
                chooseVideoFile();
            }
        }
    });
    // #endif
};

const uploadFile = (filePath: any) => {
    uni.uploadFile({
        url: baseUrl + import.meta.env.VITE_API_PREFIX + "user/user/uploadImg",
        filePath: filePath.url,
        name: "file",
        header: {
            Authorization: uni.getStorageSync("token"),
            Secret: getSecret()
        },
        success: (uploadFileRes: any) => {
            const { data } = JSON.parse(uploadFileRes.data);
            handleSendMessage("image", { pic: data.picUrl });
        },
        fail: (error) => {
            console.log("error", error);
            uni.showToast({
                title: t("图片上传失败"),
                icon: "none"
            });
        }
    });
};

// 查找占位消息在列表中的位置
const findPendingIndex = (key: string) => {
    return messageList.value.findIndex((item: any) => item.localKey === key);
};

// 移除占位消息
const removePendingMessage = (key: string) => {
    messageList.value = messageList.value.filter((item: any) => item.localKey !== key);
};

// 更新占位消息的运行态字段
const updatePendingMessage = (key: string, patch: Record<string, any>) => {
    const index = findPendingIndex(key);
    if (index === -1) return;
    const list = [...messageList.value];
    list[index] = { ...list[index], ...patch };
    messageList.value = list;
};

// 插入一条发送中的视频占位消息
const pushPendingVideoMessage = (key: string, cover: string) => {
    messageList.value = [
        ...messageList.value,
        {
            id: key,
            localKey: key,
            localPending: true,
            localPercent: 0,
            localProcessing: false,
            localCover: cover || "",
            type: 1,
            messageType: "video",
            sendTime: formatSendTime(),
            user: { avatar: userStore.userInfo?.avatar || "" },
            isRead: 0,
            content: {}
        }
    ];
    markMessageEnter(key);
    nextTick(() => {
        scrollToBottom();
    });
};

// 发送时间按会话内已有消息的格式展示
const formatSendTime = () => {
    const now = new Date();
    const pad = (value: number) => String(value).padStart(2, "0");
    return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
};

const uploadVideoFile = (filePath: any) => {
    if (filePath?.size && filePath.size / 1024 / 1024 > 50) {
        uni.showToast({ title: t("只能上传50M以内的视频"), icon: "none" });
        return;
    }

    const key = `pending_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
    pushPendingVideoMessage(key, filePath?.thumb || "");

    let completed = false;
    let timeoutTimer: ReturnType<typeof setTimeout> | null = null;
    const failPendingVideo = (message?: string) => {
        removePendingMessage(key);
        uni.showToast({ title: message || t("视频上传失败"), icon: "none" });
    };

    const uploadTask = uni.uploadFile({
        url: baseUrl + import.meta.env.VITE_API_PREFIX + "user/user/uploadVideo",
        filePath: filePath.url,
        name: "file",
        header: {
            Authorization: uni.getStorageSync("token"),
            Secret: getSecret()
        },
        success: (uploadFileRes: any) => {
            if (completed) return;
            completed = true;
            if (timeoutTimer) clearTimeout(timeoutTimer);

            try {
                if (uploadFileRes.statusCode && (uploadFileRes.statusCode < 200 || uploadFileRes.statusCode >= 300)) {
                    throw new Error(t("视频上传失败"));
                }
                const result = JSON.parse(uploadFileRes.data || "null");
                if (result?.code !== 0 || !result?.data?.videoUrl) {
                    failPendingVideo(result?.message);
                    return;
                }
                updatePendingMessage(key, { localPercent: 95, localProcessing: true });
                handleSendMessage("video", { videoUrl: result.data.videoUrl, videoCover: result.data.videoCover }, key);
            } catch (error) {
                console.error("视频上传响应解析失败:", error, uploadFileRes.data);
                failPendingVideo();
            }
        },
        fail: (error) => {
            if (completed) return;
            completed = true;
            if (timeoutTimer) clearTimeout(timeoutTimer);
            console.log("error", error);
            failPendingVideo();
        }
    });

    timeoutTimer = setTimeout(() => {
        if (completed) return;
        completed = true;
        uploadTask?.abort?.();
        failPendingVideo(t("视频上传超时，请重试"));
    }, 60000);

    uploadTask?.onProgressUpdate?.((res: any) => {
        // progress 只统计字节推送，服务端接收处理和消息发送不在其中，压到 95% 以内留给后续阶段
        const pushedPercent = res.progress || 0;
        if (pushedPercent >= 100) {
            updatePendingMessage(key, { localPercent: 95, localProcessing: true });
        } else {
            updatePendingMessage(key, { localPercent: Math.min(95, Math.round(pushedPercent * 0.95)) });
        }
    });
};

const sendTxt = () => {
    if (!textAreaValue.value) return;
    handleSendMessage();
};
const sendGoods = (product: any) => {
    handleSendMessage("custom", { product });
};

const showMoreHandle = () => {
    uni.hideKeyboard();
    moreHandle.value = !moreHandle.value;
    moreEmoji.value = false;

    nextTick(() => {
        scrollToBottom();
    });
};
const showEmojiHandle = () => {
    uni.hideKeyboard();
    moreEmoji.value = !moreEmoji.value;
    moreHandle.value = false;

    nextTick(() => {
        scrollToBottom();
    });
};

const _getConversationMessage = async () => {
    try {
        const result = await getConversationMessage(filterParams);
        if (result.records.length == 0) {
            loadend.value = true;
            return;
        }
        messageList.value = [...result.records, ...messageList.value];
        nextTick(async () => {
            if (filterParams.firstId == -1) {
                await scrollToBottom();
            } else {
                setTimeout(() => {
                    if (lastScrollTop.value) {
                        scrollTop.value = lastScrollTop.value; // 恢复到之前的滚动位置
                        isRecord.value = true;
                    }
                }, 0);
            }
        });
    } catch (error) {
        console.error(error);
    }
};

const handleSendMessage = async (type?: string, info?: any, pendingKey?: string) => {
    try {
        const result = await sendMessage({
            content: {
                messageType: type || "text",
                content: textAreaValue.value || "",
                ...info
            },
            conversationId: filterParams.conversationId,
            shopId: filterParams.shopId,
            productId: filterParams.productId,
            orderId: filterParams.orderId
        });
        textAreaValue.value = "";
        hasTxt.value = false;
        moreHandle.value = false;
        moreEmoji.value = false;

        const pendingIndex = pendingKey ? findPendingIndex(pendingKey) : -1;
        if (pendingIndex > -1) {
            // 沿用占位消息的 localKey，让气泡原地切换成真实视频而不重新入场
            const list = [...messageList.value];
            list[pendingIndex] = { ...result, localKey: pendingKey };
            messageList.value = list;
        } else {
            messageList.value = [...messageList.value, result];
            markMessageEnter(String(result.id));
        }
        nextTick(() => {
            scrollToBottom();
        });
    } catch (error: any) {
        if (pendingKey) {
            removePendingMessage(pendingKey);
            uni.showToast({ title: t("视频发送失败，请重试"), icon: "none" });
        }
        console.log(error);
    }
};

const shopInfo = ref<any>({});
const _getShopDetail = async (id: number) => {
    try {
        const result = await getShopDetail(id);
        shopInfo.value = result;
    } catch (error) {
        console.error(error);
    }
};

// 计算 IM 商品卡片展示价格：周期购商品优先使用周期购价格
const getCycleBuyDisplayPrice = (detail: any): string | number | undefined => {
    const cycleConfig = detail?.cycleConfig;
    const specs = cycleConfig?.specs;
    if (!Array.isArray(specs) || specs.length === 0) {
        return undefined;
    }

    const settingMode = Number(cycleConfig?.settingMode || 1);

    // 统一设置：直接取第一个有效期数价格
    if (settingMode === 1) {
        const firstEnabledSpec = specs.find((spec: any) => Number(spec?.disabled || 0) !== 1 && spec?.cyclePrice !== undefined);
        return firstEnabledSpec?.cyclePrice;
    }

    // SKU 单独设置：先定位默认 SKU，再取该 SKU 下第一个有效期数价格
    const checkedValue = Array.isArray(detail?.checkedValue) ? detail.checkedValue : [];
    const skuList = Array.isArray(detail?.skuList) ? detail.skuList : [];

    let currentSkuId = 0;
    if (checkedValue.length > 0) {
        const selectedSku = skuList.find((item: any) => item?.skuValue === checkedValue.join("|"));
        currentSkuId = Number(selectedSku?.skuId || 0);
    }
    if (!currentSkuId && skuList.length > 0) {
        const firstSkuWithStock = skuList.find((item: any) => Number(item?.skuStock || 0) > 0) || skuList[0];
        currentSkuId = Number(firstSkuWithStock?.skuId || 0);
    }

    if (!currentSkuId) {
        return undefined;
    }

    for (const spec of specs) {
        const skuSpecs = Array.isArray(spec?.skuSpecs) ? spec.skuSpecs : [];
        const skuSpec = skuSpecs.find((s: any) => Number(s?.skuId) === currentSkuId && Number(s?.disabled || 0) !== 1 && s?.cyclePrice !== undefined);
        if (skuSpec) {
            return skuSpec.cyclePrice;
        }
    }

    return undefined;
};

const _getProductDetail = async (id: any) => {
    try {
        const result = await getProductDetail(id);
        const cycleBuyPrice = getCycleBuyDisplayPrice(result);
        let productInfo: any = {
            messageType: "product",
            productId: result.item.productId,
            picUrl: pickPic(result.item),
            productName: result.item.productName,
            productPrice: cycleBuyPrice ?? result.item.productPrice,
            productSn: result.item.productSn
        };
        messageList.value = [...messageList.value, productInfo];
        nextTick(() => {
            scrollToBottom();
        });
    } catch (error) {
        console.error(error);
    }
};
const _getOrderDetail = async (id: any) => {
    try {
        const result = await getOrder({ id: id });
        let order = {
            messageType: "order",
            orderId: result.orderId,
            orderSn: result.orderSn,
            picUrl: pickPic(result.items[0]),
            productName: result.items[0].productName,
            productType: result.items[0].productType,
            productNum: result.items.length,
            totalAmount: result.totalAmount,
            orderStatusName: result.orderStatusName
        };
        const orderIndex = messageList.value.findIndex(
            (item: any) => item.content.messageType === "custom" && item.content.order && item.content.order.orderId == id
        );
        if (orderIndex === -1) {
            handleSendMessage("custom", { order });
        }
    } catch (error) {
        console.error(error);
    }
};
const triggerSetReadStatus = ref(false);
const handleSetRead = () => {
    if (
        messageList.value[messageList.value.length - 1].isRead === 0 &&
        messageList.value[messageList.value.length - 1].type === 2 &&
        !triggerSetReadStatus.value
    ) {
        _setRead();
    }
};
const _setRead = async () => {
    try {
        triggerSetReadStatus.value = true;

        await setRead({
            shopId: filterParams.shopId,
            servantId: messageList.value[messageList.value.length - 1].servantId
        });
    } catch (error: any) {
        console.error(error);
    } finally {
        triggerSetReadStatus.value = false;
    }
};
const promptToneRef = ref();

const pcDomain = computed(() => {
    if (configStore.baseInfo.pcDomain) {
        return configStore.baseInfo.pcDomain;
    }
    let domain = "";
    // #ifdef H5
    domain = window.location.origin;
    // #endif
    return domain;
});

type AnyObject = Record<string, any>;

const urlMap = reactive<{ [key: string]: { [key: string]: string } }>({
    pc: {
        detail: `${pcDomain.value}/item/`,
        order: `${pcDomain.value}/member/order/info?id=`,
        shop: `${pcDomain.value}/shop/${filterParams.shopId}`
    },
    mobile: {
        detail: `/pages/product/index?id=`,
        order: `/pages/user/order/info?id=`,
        giftCard: `/pages/giftCard/order/info?id=`,
        shop: `/pages/shop/index?shopId=${filterParams.shopId}`
    }
});

const getUrl = (type: string, userFrom: string, data?: AnyObject) => {
    let url = "";
    switch (type) {
        case "detail":
            url = urlMap[userFrom][type] + (userFrom === "pc" && data ? data.content.product.productSn : data?.content.product.productId);
            break;
        case "order":
            url = urlMap[userFrom][type] + (data ? data.content.order.orderId : "");
            break;
        case "giftCard":
            url = urlMap[userFrom][type] + (data ? data.content.order.orderId : "");
            break;
        case "shop":
            url = urlMap[userFrom][type];
            break;
    }
    return url;
};

const handleLink = (type: string, data?: AnyObject) => {
    let url = "";
    if (platform.value === "pc") {
        url = getUrl(type, "pc", data);
        window.open(url, "_blank");
    } else {
        if (type === "order") {
            if (data?.content.order.productType && data?.content.order.productType === 8) {
                url = getUrl("giftCard", "mobile", data);
            } else {
                url = getUrl(type, "mobile", data);
            }
        } else {
            url = getUrl(type, "mobile", data);
        }
        uni.navigateTo({
            url
        });
    }
};

const handleToList = (url: string) => {
    uni.navigateTo({
        url
    });
};

const shopLogo = computed(() => {
    return shopInfo.value.shopLogo ? shopInfo.value.shopLogo : configStore.baseInfo.shopLogo;
});
const shopTitle = computed(() => {
    return t(shopInfo.value.shopTitle || t("在线客服"));
});

/** websocket 逻辑处理  */

const onOpen = (_event: any) => {
    // console.log('连接成功', event)
};
const onMessage = (event: any) => {
    if (!isJsonString(event.data)) {
        console.error("收到非JSON格式数据");
        return;
    }

    const data = JSON.parse(event.data);

    if (data.type === "message") {
        if (data.data) {
            formatMessage(data.data[0]);
        }
    }

    if (data.type === "read") {
        messageList.value.forEach((item: any) => {
            if (item.type === 1) {
                item.isRead = 1;
            }
        });
    }
};

const verifyMessage = (data: any) => {
    return messageList.value.findIndex((item: any) => item.id === data.id);
};
const formatMessage = (data: any) => {
    if (data.shopId === filterParams.shopId) {
        const index = verifyMessage(data);
        if (index === -1) {
            messageList.value = [...messageList.value, data];
            markMessageEnter(String(data.id));
            promptToneRef.value?.play();
            nextTick(() => {
                scrollToBottom();
            });
        }
    }
};
const onClose = (_event: any) => {
    // console.log('连接关闭', event)
};
const onError = (_event: any) => {
    // console.log('连接错误', event)
};
const ws = ref();
const platform = ref("");
const getSocketUrl = () => {
    let url;
    // #ifdef H5
    if (import.meta.env.VITE_SOCKET_URL) {
        url = import.meta.env.VITE_SOCKET_URL;
    } else {
        url = location.origin ? location.origin.replace(/^http:/, "ws:").replace(/^https:/, "wss:") + "/ws" : "";
    }
    // #endif

    // #ifndef H5
    url = import.meta.env.VITE_SOCKET_URL;
    // #endif
    return url;
};
onLoad(async (options) => {
    if (options) {
        if (options.shopId !== undefined && options.shopId !== null && options.shopId !== "" && !Number.isNaN(Number(options.shopId))) {
            filterParams.shopId = Number(options.shopId);
            await _getShopDetail(filterParams.shopId);
        }
        if (options.productId && !!Number(options.productId)) {
            filterParams.productId = Number(options.productId);
            _getProductDetail(options.productId);
        }
        if (options.orderId && !!Number(options.orderId)) {
            filterParams.orderId = Number(options.orderId);
        }
        if (options.conversationId && !!Number(options.conversationId)) {
            filterParams.conversationId = Number(options.conversationId);
        }

        if (options.platform) {
            platform.value = options.platform;
        }

        if (options.token) {
            // uni.setStorageSync("token", options.token);
            uni.setStorage({
                key: "token",
                data: options.token,
                success: async () => {
                    userStore.getUserInfo();
                    filterParams.userFrom = configStore.XClientType;
                    ws.value = new WebSocketClient(
                        `${getSocketUrl()}?token=${options.token}&platform=${platform.value ? platform.value : filterParams.userFrom}`
                    );
                    ws.value.onclose(onClose);
                    ws.value.onopen(onOpen);
                    ws.value.onerror(onError);
                    ws.value.onmessage(onMessage);
                    // 连接
                    ws.value.connect();
                    await _getConversationMessage();
                    if (filterParams.orderId) {
                        await _getOrderDetail(filterParams.orderId);
                    }
                }
            });
        }
    }
});
const gallaryBg = computed(() => {
    return `url(${staticResource("im/gallary.png")})`;
});
const goodspicBg = computed(() => {
    return `url(${staticResource("im/goodspic.png")})`;
});
const plusBg = computed(() => {
    return `url(${staticResource("im/plus@2x.png")})`;
});
const icoFaceBg = computed(() => {
    return `url(${staticResource("im/ico_face_v2@2x.png")})`;
});
const talkBg = computed(() => {
    return `url(${staticResource("im/talk@2x.png")})`;
});
const headerHeight = computed(() => {
    let height = "44px";
    // #ifdef MP-WEIXIN
    height = configStore.menuButtonInfo.height + 10 + "px";
    // #endif
    return height;
});

const headerTop = computed(() => {
    let top = configStore.safeAreaInsets.top + "px";
    // #ifdef MP-WEIXIN
    top = configStore.menuButtonInfo.top + "px";
    // #endif
    return top;
});

// #ifdef MP-WEIXIN
const paddingRight = computed(() => {
    return configStore.menuButtonInfo.width + 10 + "px";
});
// #endif
const footerHight = computed(() => {
    let num = 100;
    if (moreEmoji.value) {
        num += 324;
    }
    if (moreHandle.value) {
        num += 196;
    }
    if (showKeyboard.value) {
        return `calc(${num}rpx + ${keyboardHeight.value}px)`;
    }
    return `${num}rpx`;
});
// 判断文字是否被[]包裹（含表情），是则走 emojiHtml 渲染，否则用原生 <text>
const isEmojiText = (text: string) => {
    return /\[(.*?)\]/.test(text);
};
</script>
<style>
page {
    background-color: #f5f5f5;
}
</style>
<style scoped lang="scss">
.message-scroll {
    width: 100%;
    height: 100%;
}

.icon-houtui {
    color: #000;
    font-size: 40rpx;
    padding-left: 20rpx;
}

.message-image {
    width: 300rpx;
    height: 300rpx;
}

.message-video {
    width: 400rpx;

    .im-video {
        width: 400rpx;
        height: 300rpx;
        border-radius: 10rpx;
        background-color: #000;
    }

    .video-sending {
        position: relative;
        width: 400rpx;
        height: 300rpx;
        border-radius: 10rpx;
        overflow: hidden;
        background-color: #000;

        .video-sending-poster {
            width: 100%;
            height: 100%;
        }

        .video-sending-mask {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background-color: rgba(0, 0, 0, 0.45);
        }

        .video-sending-spinner {
            width: 44rpx;
            height: 44rpx;
            border-radius: 50%;
            border: 4rpx solid rgba(255, 255, 255, 0.35);
            border-top-color: #fff;
            animation: video-sending-rotate 0.9s linear infinite;
        }

        .video-sending-percent {
            margin-top: 12rpx;
            font-size: 24rpx;
            color: #fff;
        }
    }
}

@keyframes video-sending-rotate {
    to {
        transform: rotate(360deg);
    }
}

.msg-enter {
    animation: msg-enter-in 0.3s ease;
}

@keyframes msg-enter-in {
    from {
        opacity: 0;
        transform: translateY(20rpx);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.page-content {
    display: flex;
    height: 100vh;
    flex: 1;
    width: 100vw;
    overflow: auto;
    color: #333;
}

.thanos-layout {
    position: relative;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
    width: 100%;
    height: 100%;

    .thanos-layout-header {
        height: v-bind("headerHeight");
        box-sizing: content-box;
        display: flex;
        align-items: flex-end;
        background-color: #fff;
        padding-top: v-bind(headerTop);
        // #ifdef MP-WEIXIN
        padding-right: v-bind(paddingRight);
        // #endif
    }

    .thanos-layout-main-content {
        flex: 1;
        overflow: auto;
        display: flex;
        flex-direction: column;
        justify-content: center;
        padding-bottom: env(safe-area-inset-bottom);
        margin-bottom: v-bind(footerHight);
        transition: all 0.1s;
        box-sizing: border-box;
    }

    .thanos-layout-footer {
        position: fixed;
        box-sizing: content-box;
        padding-bottom: env(safe-area-inset-bottom);
        left: 0;
        bottom: 0;
        width: 100%;
        height: v-bind(footerHight);
        transition: all 0.1s;
    }
}

.thanos-inputer {
    resize: none;
    width: 100%;
    height: 100%;
    max-height: 200rpx;
    box-sizing: border-box;
    border-radius: 8rpx;
    padding: 10rpx;
    line-height: 48rpx;
    font-size: 28rpx;
    border: none;

    &:focus {
        border: none;
        outline: none;
    }
}

.wapim-conversation-banner {
    height: 100%;
    width: 100%;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-sizing: content-box;
    // // #ifdef MP-WEIXIN
    // align-items: flex-start;
    // // #endif
    .shop-info {
        font-size: 30rpx;
        position: relative;
        z-index: 2;
    }

    .banner-title {
        position: absolute;
        left: 140rpx;
        right: 140rpx;
        top: 0;
        bottom: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        z-index: 1;
        pointer-events: none;
        font-size: 32rpx;
        // #ifdef MP-WEIXIN
        left: auto;
        // #endif
    }

    .right-opt {
        height: 100%;
        display: flex;
        align-items: center;
        position: relative;
        z-index: 2;
        .ico-history {
            width: 80rpx;
            height: 48rpx;
            background: transparent v-bind(talkBg) center center no-repeat;
            background-size: 48rpx 40rpx;
        }

        .wapim-conversation-banner-history {
            display: inline-block;
            position: relative;
            margin-right: 20rpx;
        }
    }
}

.wapim-inputer {
    position: relative;
    background-color: #f5f5f7;
    height: 100%;
    .wapim-main-inputer {
        display: flex;
        align-items: center;
        box-sizing: border-box;
        position: relative;
        .input-box {
            width: calc(100vw - 150rpx);
        }

        .rich-input {
            margin: 16rpx 0;
            margin-left: 20rpx;
            min-height: 60rpx;
            padding: 0 10rpx;
            background-color: #fff;
            color: #333;
            border-radius: 10rpx;
            height: 100%;
            display: flex;
            align-items: center;
            :deep(.emoji) {
                vertical-align: sub;
                cursor: default;
                height: 40rpx;
                margin: 0 4rpx;
            }
        }
        .wapim-inputer-operation {
            margin-left: 20rpx;
            width: 200rpx;
            display: flex;
            align-items: center;
            justify-content: center;

            .operation {
                width: 60rpx;
                height: 60rpx;
                margin-right: 28rpx;
            }

            .face {
                background: transparent v-bind(icoFaceBg) center center no-repeat;
                background-size: 60rpx 60rpx;
                left: 20rpx;
            }

            .plus {
                background: transparent v-bind(plusBg) center center no-repeat;
                background-size: 60rpx 60rpx;
                position: relative;
            }

            .send {
                width: 80rpx;
                height: 60rpx;
                background-color: #0983f6;
                border-radius: 4rpx;
                line-height: 60rpx;
                text-align: center;
                font-size: 28rpx;
                color: #fff;
            }
        }
    }
}

.file-inputer-plugin {
    box-sizing: content-box;
    padding: 32rpx;
    height: 132rpx;
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 20rpx;

    .plugin-item {
        width: 90rpx;
        text-align: center;

        .title {
            font-size: 24rpx;
            color: #666;
            line-height: 64rpx;
        }
    }

    .plugin-item .photo {
        height: 90rpx;
        box-sizing: border-box;
        border-width: 0;
        border-radius: 4rpx;

        input {
            width: 100%;
            height: 100%;
            opacity: 0;
            display: inline-block;
        }
    }

    .pic-item .photo {
        background: #fff center center v-bind("goodspicBg") no-repeat;
        background-size: 64rpx;
    }

    .video-item .photo {
        background: #fff center center v-bind("gallaryBg") no-repeat;
        background-size: 64rpx;
    }

    .goods-list-item .icon {
        padding: 12rpx;
        background: #fff v-bind("gallaryBg") center center no-repeat;
        background-size: 64rpx;
        position: relative;
    }
}

.file-emoji-plugin {
    padding: 20rpx;
    height: 18vh;
    display: -webkit-box;
    display: -webkit-flex;
    display: flex;
    overflow-y: scroll;
}

.bg-pic {
    display: inline-block;
    width: 90rpx;
    height: 90rpx;
    overflow: hidden;
    margin: 0;

    .bg-pic-content {
        background-size: contain;
        width: 100%;
        height: 100%;
        background-repeat: no-repeat;
        background-position: center;
    }
}

.self-service-menu {
    position: relative;
    height: 76rpx;
    z-index: 1;
    background: #f5f5f7;
    box-shadow: 0 -2rpx 0 #e5e5e5;
    -webkit-transform: translateY(2rpx);
    transform: translateY(2rpx);
    display: -webkit-box;
    display: -webkit-flex;
    display: flex;
    -webkit-box-align: end;
    -webkit-align-items: flex-end;
    align-items: flex-end;
    overflow: auto;
    gap: 16rpx;
    margin-left: 32rpx;

    .menu-item {
        -webkit-flex-shrink: 0;
        flex-shrink: 0;
        font-size: 24rpx;
        background: #f5f5f7;
        color: #666;
        padding: 12rpx 20rpx;
        border: 0.1rpx solid #bbb;
        border-radius: 24rpx;
        -webkit-user-select: none;
        user-select: none;
        cursor: pointer;
    }
}

.empty-text {
    align-items: center;
    font-size: 24rpx;
    text-align: center;
    color: #666;
    margin: 24rpx 0;
}

.message-box {
    height: auto;
    display: flex;
    flex-direction: column;
    flex: 1;
    padding: 15rpx 0;

    .message-time {
        text-align: center;
        font-size: 24rpx;
        line-height: 28rpx;
        margin-bottom: 30rpx;
        color: #999;

        span {
            padding: 6rpx 10rpx;
            background-color: #dbdbdb;
            display: inline-block;
            border-radius: 6rpx;
            min-width: 120rpx;
            color: #fff;
        }
    }

    .message-content {
        overflow: hidden;
        position: relative;

        .message-content-left {
            padding-left: 2em;
        }

        .message-avatar {
            width: 80rpx;
            height: 80rpx;
            vertical-align: middle;
            box-sizing: border-box;
            border-radius: 50%;
            position: relative;
            overflow: hidden;
            margin: 0;
            float: right;
        }

        .message-content-inner {
            position: relative;
            width: auto;
            max-width: 70%;
            padding: 18rpx 24rpx;
            font-size: 32rpx;
            color: #333;
            vertical-align: middle;
            border-radius: 6rpx;
        }
    }
}

.thanos-rich-message {
    margin-bottom: 40rpx;
    padding: 0 30rpx;

    .thanos-text-message {
        white-space: pre-wrap;
        word-break: break-word;
        word-wrap: break-word;
        overflow-wrap: break-word;
        line-height: 1.4em;
        :deep(.van-image__img) {
            border-radius: 10rpx;
        }
        :deep(.emoji) {
            vertical-align: sub;
            cursor: default;
            height: 40rpx;
            margin: 0 4rpx;
        }
        .custom-message-product {
            display: flex;
            min-width: 400rpx;
            .product-img {
                width: 120rpx;
                margin-right: 16rpx;
            }
            .product-info {
                flex: 1;
                .product-name {
                    margin-bottom: 20rpx;
                    font-size: 28rpx;
                    line-height: 30rpx;
                    display: -webkit-box; /* 使用WebKit的盒模型 */
                    -webkit-box-orient: vertical; /* 垂直排列 */
                    -webkit-line-clamp: 2; /* 显示的行数 */
                    overflow: hidden; /* 隐藏溢出的内容 */
                    text-overflow: ellipsis; /* 显示省略号 */
                }
                .product-price {
                    color: red;
                }
            }
        }
        .custom-message-order {
            min-width: 400rpx;
            .tit {
                font-size: 28rpx;
                font-weight: bold;
                margin-bottom: 10rpx;
            }
            .info {
                display: flex;
                .product-img {
                    width: 120rpx;
                    margin-right: 16rpx;
                }
                .product-info {
                    flex: 1;
                    .product-name {
                        margin-bottom: 20rpx;
                        font-size: 26rpx;
                        line-height: 35rpx;
                        display: -webkit-box; /* 使用WebKit的盒模型 */
                        -webkit-box-orient: vertical; /* 垂直排列 */
                        -webkit-line-clamp: 2; /* 显示的行数 */
                        overflow: hidden; /* 隐藏溢出的内容 */
                        text-overflow: ellipsis; /* 显示省略号 */
                    }
                    .product-txt {
                        font-size: 22rpx;
                        color: #999;
                    }
                }
            }
        }
    }

    &.out .message-content-inner {
        float: right;
        margin-right: 24rpx;
        background-color: #a0e75a;

        &::after {
            position: absolute;
            top: 20rpx;
            content: "";
            border: 20rpx solid transparent;
        }

        &::after {
            border-left-color: #a0e75a;
            right: -32rpx;
        }

        &.card-message {
            background-color: #fff;
            width: 70%;
        }

        &.card-message::after {
            border-left-color: #fff;
        }
    }

    &.in .message-avatar {
        float: left;
    }

    &.in .message-content-inner {
        float: left;
        margin-left: 24rpx;
        background-color: #fff;

        &::after {
            position: absolute;
            top: 20rpx;
            content: "";
            border: 20rpx solid transparent;
        }

        &::after {
            border-right-color: #fff;
            left: -32rpx;
        }
    }

    .read-status {
        font-size: 26rpx;
        padding-top: 8rpx;
        margin-right: 102rpx;
        display: flex;
        justify-content: flex-end;
        clear: both;

        &.read {
            color: #969799;
        }

        &.unread {
            color: #1989fa;
        }
    }
    &.card-type .message-content-inner {
        background-color: #fff;
        &::after {
            border-left-color: #fff;
        }
        &.card-message {
            background-color: #fff;
        }
        &.card-message::after {
            border-left-color: #fff;
        }
    }
    &.image-type .message-content-inner {
        padding: 0;
        background-color: rgba(0, 0, 0, 0);
        &::after {
            border-left-color: rgba(0, 0, 0, 0);
            border-right-color: rgba(0, 0, 0, 0);
        }
        &.card-message {
            background-color: rgba(0, 0, 0, 0);
        }
        &.card-message::after {
            border-left-color: rgba(0, 0, 0, 0);
        }
    }
}

.goods-card-message {
    color: #333;
    padding: 12rpx 0;
    display: flex;
    flex: 1;
}

.thanos-goods-card {
    background-color: #fff;
    text-align: left;
    display: flex;
    width: 100%;
    min-width: 336rpx;

    .goods-cover {
        flex: 0 0 120rpx;
        height: 120rpx;
    }

    .goods-image {
        display: flex;
        align-content: center;
        justify-content: center;
        overflow: hidden;
    }

    .goods-text-info {
        margin-left: 16rpx;
        font-size: 28rpx;
        line-height: 40rpx;
        min-width: 200rpx;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .goods-title {
            overflow: hidden;
            text-overflow: ellipsis;
            word-break: break-all;
            word-wrap: normal;
            white-space: pre-wrap;
            flex: 1 0 80rpx;
        }

        .goods-price {
            bottom: 0;
            color: #ff3434;
            word-break: break-all;

            .currency {
                font-size: 20rpx;
                margin-right: 4rpx;
            }
        }
    }
}

.im-goods-card {
    background-color: #fff;
    text-align: center;
    margin: 0 32rpx 32rpx;
    display: flex;
    flex-direction: column;
    border-radius: 8rpx;
    padding: 32rpx 32rpx 0 32rpx;
    color: #333;

    .content {
        display: flex;

        .pic {
            height: 144rpx;
            width: 144rpx;
            margin-bottom: 32rpx;
            margin-right: 24rpx;
            border-radius: 4rpx;
            overflow: hidden;
        }

        .desc {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            flex: 1;
            padding-bottom: 32rpx;

            .title {
                overflow: hidden;
                text-overflow: ellipsis;
                word-break: break-all;
                word-wrap: normal;
                white-space: pre-wrap;
                line-height: 40rpx;
                flex: 0 1 80rpx;
                text-align: left;
            }

            .price {
                display: flex;
                align-items: flex-end;
                flex: 1 0;
                font-weight: 500;
            }
        }
    }

    .line {
        height: 0.1rpx;
        background-color: #ebedf0;
    }

    .send {
        padding: 28rpx 0;
        justify-content: center;
        align-items: center;
        line-height: 32rpx;
        color: #1989fa;
        font-weight: 500;
    }
}
</style>
