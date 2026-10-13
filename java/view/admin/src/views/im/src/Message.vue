<template>
    <div :class="preview ? 'preview-style' : ''" class="im-layout" v-if="msgFilterParams.conversationId != -1" @click="handleSetRead">
        <div class="im-center">
            <div class="conversation-title-m_wrap_qcEBo">
                <div class="conversation-title-m_name_1G8ZZ" v-if="conversationDetail.user">
                    <div class="name">{{ currentConversationData.user?.username }}</div>
                    <div class="flex">
                        <div v-if="!preview" class="reconnection">
                            <Reconnection @serversUpdated="serversUpdated" :conversationId="msgFilterParams.conversationId"></Reconnection>
                        </div>
                        <div class="reception-panel-toolbar-waiting" @click="_updateFieldConversation">{{ $t('结束') }}</div>
                    </div>
                </div>
            </div>
            <div class="message-list im-message-m_container_31cB1 im-message-box">
                <div ref="messageBoxRef" @scroll="handleScroll" class="message-list-content">
                    <!-- 用户咨询 -->
                    <TransitionGroup :name="msgTransitionName">
                        <div class="im-message-row" v-for="item in messageList" :key="item.localKey || item.id">
                            <!--  -- 文本text -->
                            <div class="im-message-item text" :class="{ left: item.type === 1, right: item.type === 2 }" v-if="item.type !== 3">
                                <div class="im-message-item__time">{{ item.sendTime }}</div>
                                <div class="im-message-item__username" v-if="item.type === 2 && item.servant">{{ item.servant.username }}</div>
                                <div class="content__outer">
                                    <span class="zent-avatar im-message-item__avatar zent-avatar--size-large zent-avatar--shape-circle zent-avatar--type-image">
                                        <img class="zent-avatar-image" v-if="item.type === 2 && item.servant?.avatar" :src="avatarFormat(item.servant?.avatar)" />
                                        <img class="zent-avatar-image" v-else-if="item.type === 1 && item.user?.avatar" :src="avatarFormat(item.user?.avatar)" />
                                        <img v-else class="zent-avatar-image" src="@/assets/avatar/1.jpeg" />
                                    </span>
                                    <div class="im-message-item__content" v-if="item.messageType == 'text'">
                                        <div class="im-message-item__content-value" v-if="item.content && item.content.content">
                                            <span v-if="!replaceText(item.content.content)" v-html="item.content.content"></span>
                                            <emojiHtml v-else :content="item.content.content" />
                                        </div>
                                    </div>
                                    <div class="im-message-item__content" v-if="item.content && item.messageType == 'image'">
                                        <div class="im-message-item__content-value">
                                            <el-image
                                                style="height: 200px"
                                                :src="imageFormat(item.content?.pic)"
                                                :zoom-rate="1.2"
                                                :max-scale="7"
                                                :min-scale="0.2"
                                                :preview-src-list="[imageFormat(item.content?.pic)]"
                                                hide-on-click-modal
                                                fit="cover"
                                            />
                                        </div>
                                    </div>
                                    <div class="im-message-item__content" v-if="item.localPending">
                                        <div class="im-message-item__content-value">
                                            <div class="video-sending" :style="{ width: (item.localWidth || 260) + 'px' }">
                                                <img class="video-sending__poster" v-if="item.localCover" :src="item.localCover" />
                                                <div class="video-sending__mask">
                                                    <div class="video-sending__spinner"></div>
                                                    <div class="video-sending__percent">
                                                        {{ item.localProcessing ? $t("处理中") : (item.localPercent || 0) + "%" }}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="im-message-item__content" v-if="item.content && item.messageType == 'video' && !item.localPending">
                                        <div class="im-message-item__content-value">
                                            <video
                                                style="height: 200px; max-width: 260px; border-radius: 4px"
                                                :src="imageFormat(item.content?.videoUrl)"
                                                :poster="item.content?.videoCover ? imageFormat(item.content?.videoCover) : ''"
                                                controls
                                                preload="metadata"
                                            ></video>
                                        </div>
                                    </div>
                                    <div class="im-message-item__content" v-if="item.messageType == 'custom'">
                                        <div class="im-message-item__content-value im-message-item__content-card" v-if="item.content">
                                            <a
                                                v-if="item.content.product && item.content.product !== null"
                                                :href="urlFormat({ path: 'product', sn: item.content.product.productSn })"
                                                target="_blank"
                                                class="im-message-item__content-card-link"
                                            >
                                                <h4 class="im-message-item__content-card-title">{{ item.content.product.productName }}</h4>
                                                <div class="im-message-item__content-card-detail">
                                                    <figure class="im-message-item__content-card-figure">
                                                        <el-image
                                                            fit="cover"
                                                            width="100%"
                                                            style="border: 1px solid #f5f5f5"
                                                            :src="
                                                                imageFormat(
                                                                    item.content.product.picUrl ? item.content.product.picUrl : item.content.product.picThumb
                                                                )
                                                            "
                                                        />
                                                    </figure>
                                                    <p class="im-message-item__content-card-desc">{{ priceFormat(item.content.product.productPrice) }}</p>
                                                </div>
                                            </a>
                                            <a v-if="item.content.order && item.content.order !== null" class="im-message-item__content-card-link">
                                                <h4 class="im-message-item__content-card-title">{{ item.content.order.orderStatusName }}</h4>

                                                <div class="im-message-item__content-card-detail">
                                                    <figure class="im-message-item__content-card-figure">
                                                        <el-image
                                                            fit="cover"
                                                            width="100%"
                                                            style="border: 1px solid #f5f5f5"
                                                            :src="
                                                                imageFormat(item.content.order.picUrl ? item.content.order.picUrl : item.content.order.picThumb)
                                                            "
                                                        />
                                                    </figure>
                                                    <div class="right-info">
                                                        <div class="im-message-item__content-card-order-name">{{ item.content.order.productName }}</div>
                                                        <div class="im-message-item__content-card-order-info">
                                                            <div>{{ $t('共{productNum}件商品', { productNum: item.content.order.productNum }) }}</div>
                                                            <div>{{ $t('合计{priceFormat}', { priceFormat: priceFormat(item.content.order.totalAmount) }) }}</div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div v-if="item.type === 2">
                                    <span class="im-message-item__read" v-if="item.isRead">{{ $t('已读') }}</span>
                                    <span class="im-message-item__unread" v-else>{{ $t('未读') }}</span>
                                </div>
                            </div>
                            <div v-if="item.type === 3 && item.messageType === 'text'">
                                <div class="im-message-item notice">
                                    <div class="im-message-item__content">{{ item.sendTime }} {{ item.content ? item.content.content : "" }}</div>
                                </div>
                            </div>
                        </div>
                    </TransitionGroup>
                </div>
            </div>
            <div class="im-edit-box">
                <div class="im-edit-box-mask" v-if="status === 2"> {{ $t('如需联系客户，请点击') }} <a class="zent-link" @click="msgClick">{{ $t('开始对话') }}</a>
                </div>
                <SendMessage v-else :messageInfo="messageInfo" @sendMessage="getSendMessage" @videoPending="handleVideoPending"></SendMessage>
            </div>
        </div>
        <div class="im-resizable-anchor"></div>
        <div class="im-resizable im-right">
            <div class="zent-tabs zent-tabs-type__normal right-plugins-m_tabs_1oN_5">
                <div class="zent-tabs-nav zent-tabs-nav-type__normal">
                    <div class="zent-tabs-nav-content">
                        <div class="zent-tabs-scroll">
                            <div class="zent-tabs-tab" @click="checkTabs(1)" :class="{ 'zent-tabs-tab__actived': currentTabs == 1 }">
                                <div class="zent-tabs-tab-inner">{{ $t('资料') }}</div>
                            </div>
                            <div class="zent-tabs-tab" @click="checkTabs(2)" :class="{ 'zent-tabs-tab__actived': currentTabs == 2 }">
                                <div class="zent-tabs-tab-inner">{{ $t('订单') }}</div>
                            </div>
                            <div v-if="!preview" class="zent-tabs-tab" @click="checkTabs(3)" :class="{ 'zent-tabs-tab__actived': currentTabs == 3 }">
                                <div class="zent-tabs-tab-inner">{{ $t('商品') }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div>
                <template v-if="currentTabs == 1">
                    <div role="tabpanel" class="zent-tabs-panel thin-line">
                        <div class="profile-anonymous-m_line_kvakE" style="height: auto">
                            <label>
                                <div class="zent-badge zent-badge--has-content conversation-list-item-avatar" style="margin-right: 16px">
                                    <div class="zent-badge-content">
                                        <span class="zent-avatar zent-avatar--size-large zent-avatar--shape-circle zent-avatar--type-image">
                                            <span
                                                class="zent-avatar im-message-item__avatar zent-avatar--size-large zent-avatar--shape-circle zent-avatar--type-image"
                                            >
                                                <template v-if="!conversationDetail.user?.avatar">
                                                    <img class="zent-avatar-image" src="@/assets/avatar/1.jpeg" />
                                                </template>
                                                <template v-else-if="extractContent(String(conversationDetail.user?.avatar)) == 'def'">
                                                    <img class="zent-avatar-image" :src="returnAvatar(conversationDetail.user?.avatar as any)" />
                                                </template>
                                                <template v-else>
                                                    <img class="zent-avatar-image" :src="imageFormat(conversationDetail.user?.avatar as any)" />
                                                </template>
                                            </span>
                                        </span>
                                    </div>
                                </div>
                            </label>
                            <div class="wechat-from">
                                <div class="name">{{ conversationDetail.user?.username }}</div>
                                <div class="tags">
                                    <!-- <div class="add-tag">添加标签</div> -->
                                </div>
                            </div>
                        </div>
                        <div class="profile-anonymous-m_line_kvakE">
                            <label>{{ $t('昵称：') }}</label>
                            <span>{{ conversationDetail.user?.nickname }}</span>
                        </div>
                        <div class="profile-anonymous-m_line_kvakE">
                            <label>{{ $t('会话来源：') }}</label>
                            <span>{{ conversationDetail.userFrom }}</span>
                        </div>
                    </div>
                    <div role="tabpanel" class="zent-tabs-panel">
                        <div class="profile-anonymous-m_line_kvakE" style="height: auto; flex-direction: column; align-items: flex-start">
                            <label style="flex: auto; margin: 8px 0 4px 0">{{ $t('会话总结：') }}</label>
                            <div class="edit-input">
                                <TigInput v-model="conversationDetail.summary" />
                            </div>
                        </div>
                        <div class="profile-anonymous-m_line_kvakE" style="height: auto; flex-direction: column; align-items: flex-start">
                            <label style="flex: auto; margin: 8px 0 4px 0">{{ $t('会话备注：') }}</label>
                            <div class="edit-input">
                                <TigInput v-model="conversationDetail.remark" :rows="6" type="textarea" />
                            </div>
                        </div>
                        <div class="action">
                            <el-button type="primary" :disabled="conversationDetail.remark == ''" @click="_conversationSaveRemark()">{{ $t('保存') }}</el-button>
                        </div>
                    </div>
                </template>
                <template v-else-if="currentTabs == 2">
                    <OrderImList :preview="preview" :userId="currentConversationData.userId"></OrderImList>
                </template>
                <template v-else-if="currentTabs == 3">
                    <SelectImProduct :messageInfo="messageInfo" @sendMessage="getSendMessage"></SelectImProduct>
                </template>
            </div>
        </div>
    </div>
    <div class="im-layout" v-if="msgFilterParams.conversationId == -1">
        <div class="im-default-box">
            <div class="im-default">
                <div class="im-default-icon">
                    <el-icon color="#d5d5d5" size="50">
                        <ChatDotSquare />
                    </el-icon>
                </div>
                <div class="im-default-text"><span>{{ $t('没有选中会话哦') }}</span></div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, watch, onMounted, nextTick, reactive, inject } from "vue";
import {
    getConversationMessage,
    getConversationDetail,
    conversationSaveRemark,
    conversationCreate,
    setRead,
    delConversation,
    updateFieldConversation
} from "@/api/im/im";
import type { msgFilterParams, MessageInfo, MessageList, MessageDetail } from "@/types/im/im.d";
import { extractContent } from "@/utils/util";
import { returnAvatar } from "@/utils/avatar";
import { imageFormat, priceFormat, avatarFormat, urlFormat } from "@/utils/format";
import SendMessage from "./SendMessage.vue";
import { message } from "ant-design-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import Reconnection from "@/views/im/src/Reconnection.vue";
import { ChatDotSquare } from "@element-plus/icons-vue";
import SelectProduct from "@/views/product/product/src/SelectProduct.vue";
import SelectImProduct from "@/components/select/src/SelectImProduct.vue";
import OrderImList from "@/views/im/src/OrderImList.vue";
import { useRouter } from "vue-router";
import { emojiHtml } from "@/components/emoji";
import { useUserStore } from "@/store/user";
import dayjs from "dayjs";
import { isSameConversation } from "./conversationScope";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    conversationId: {
        type: Number,
        default: ""
    },
    currentNewMessage: {
        type: Object,
        default: () => ({})
    },
    currentConversationData: {
        type: Object,
        default: () => ({})
    },
    msgType: {
        type: Number,
        default: 1
    },
    status: {
        type: Number,
        default: 1
    },
    preview: {
        type: Boolean,
        default: false
    }
});
const messageInfo = ref<MessageInfo>({});
const messageList = ref<MessageList[]>([]);
const loadend = ref(false);
const msgFilterParams = reactive<msgFilterParams>({
    conversationId: props.conversationId,
    sortOrder: "desc",
    firstId: -1
});
const messageBoxRef = ref<any>(null);
let isRecord = ref<boolean>(true);
let lastScrollTop = ref<number>(0);
const userStore = useUserStore();
// 仅在追加新消息时启用入场动画，批量加载历史消息时保持无动画
const msgTransitionName = ref("");
const scrollToBottom = (smooth = false) => {
    if (messageBoxRef.value) {
        if (smooth) {
            messageBoxRef.value.scrollTo({ top: messageBoxRef.value.scrollHeight, behavior: "smooth" });
        } else {
            messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight;
        }
    }
};
const handleScroll = () => {
    handleSetRead();
    if (messageBoxRef.value) {
        if (isRecord.value) {
            lastScrollTop.value = messageBoxRef.value.scrollTop;
            isRecord.value = false;
        }
        const { scrollTop, scrollHeight, clientHeight } = messageBoxRef.value;
        const firstSentMessage = messageList.value.find((item) => !item.localPending);
        if (scrollTop == 0 && loadend.value == false && firstSentMessage) {
            msgFilterParams.firstId = firstSentMessage.id;
            _getConversationMessage();
        }
    }
};
const emit = defineEmits(["update:conversationId", "resumeConversation", "_getConversationStrat", "_getConversationEnd"]);

const getSendMessage = (data: any, pendingKey: string = "", targetConversationId?: number | string) => {
    if (targetConversationId !== undefined && !isSameConversation(targetConversationId, msgFilterParams.conversationId)) return;
    if (targetConversationId === undefined && data?.conversationId !== undefined && !isSameConversation(data.conversationId, msgFilterParams.conversationId)) return;
    msgTransitionName.value = "im-msg";
    const pendingIndex = pendingKey ? findPendingIndex(pendingKey) : -1;
    if (pendingIndex > -1) {
        // 沿用占位消息的 localKey，让气泡原地切换成真实视频而不重新入场
        const localKey = messageList.value[pendingIndex].localKey;
        messageList.value[pendingIndex] = { ...data, localKey };
    } else {
        messageList.value.push(data);
    }
    nextTick(() => {
        scrollToBottom(true);
    });
};
const findPendingIndex = (key: string) => {
    return messageList.value.findIndex((item) => item.localKey === `pending_${key}`);
};
const handleVideoPending = (payload: {
    key: string;
    status: string;
    conversationId: number | string;
    percent?: number;
    cover?: string;
    width?: number;
}) => {
    if (!isSameConversation(payload.conversationId, msgFilterParams.conversationId)) return;
    const { key, status } = payload;
    if (status === "start") {
        msgTransitionName.value = "im-msg";
        const lastSelfMessage = [...messageList.value].reverse().find((item) => item.type === 2 && item.servant);
        messageList.value.push({
            localKey: `pending_${key}`,
            localPending: true,
            localPercent: 0,
            localProcessing: false,
            localCover: "",
            id: `pending_${key}`,
            conversationId: msgFilterParams.conversationId,
            type: 2,
            messageType: "video",
            sendTime: dayjs().format("YYYY-MM-DD HH:mm:ss"),
            servant: lastSelfMessage?.servant || {
                adminId: userStore.userInfo.adminId,
                username: userStore.userInfo.username,
                avatar: userStore.userInfo.avatar
            },
            isRead: 0,
            content: {}
        } as any);
        nextTick(() => {
            scrollToBottom(true);
        });
        return;
    }
    const index = findPendingIndex(key);
    if (index === -1) return;
    if (status === "progress") {
        messageList.value[index].localPercent = payload.percent ?? 0;
    } else if (status === "processing") {
        messageList.value[index].localPercent = 95;
        messageList.value[index].localProcessing = true;
    } else if (status === "cover") {
        messageList.value[index].localCover = payload.cover || "";
        if (payload.width) {
            messageList.value[index].localWidth = payload.width;
        }
    } else if (status === "fail") {
        msgTransitionName.value = "im-msg";
        messageList.value.splice(index, 1);
    }
};
const router = useRouter();
const msgClick = () => {
    ElMessageBox.confirm(t('确认重新接入会话?'), {
        confirmButtonText: t('确认'),
        cancelButtonText: t('取消'),
        type: "warning"
    })
        .then(() => {
            conversationCreate({
                userId: props.currentConversationData.userId,
                userFrom: props.currentConversationData.userFrom
            })
                .then((res) => {
                    if (props.preview) {
                        //带参数跳转到首页，打开聊天框
                        router.push({
                            path: "/im/index",
                            query: { id: res.id }
                        });
                    } else {
                        msgFilterParams.conversationId = res.id;
                        msgFilterParams.firstId = -1;
                        messageList.value = [];
                        loadend.value = false;
                        _getConversationMessage();
                        emit("resumeConversation", true);
                    }
                })
                .catch((error: any) => {
                    message.error(error.message);
                });
        })
        .catch(() => {});
};

const _updateFieldConversation = async () => {
    ElMessageBox.confirm(t('确认结束该会话?'), {
        confirmButtonText: t('确认'),
        cancelButtonText: t('取消'),
        type: "warning"
    }).then(() => {
        updateFieldConversation({
            id: props.conversationId,
            field: 'status',
            val: 2
        })
            .then((res) => {
                conversationDetail.value = {};
                msgFilterParams.conversationId = -1;
                emit("update:conversationId", -1);
                emit("_getConversationStrat");
                emit("_getConversationEnd");
            })
            .catch((error: any) => {
                message.error(error.message);
                emit("_getConversationStrat");
                emit("_getConversationEnd");
            });
    });
};
const _getConversationMessage = async () => {
    try {
        const result = await getConversationMessage(msgFilterParams);
        if (result.records.length == 0) {
            loadend.value = true;
            return;
        }
        msgTransitionName.value = "";
        messageList.value = [...result.records, ...messageList.value];
        nextTick(() => {
            if (msgFilterParams.firstId == -1) {
                scrollToBottom();
            } else {
                setTimeout(() => {
                    if (messageBoxRef.value) {
                        messageBoxRef.value.scrollTop = lastScrollTop.value; // 恢复到之前的滚动位置
                        isRecord.value = true;
                    }
                }, 0);
            }
        });
    } catch (error) {
        console.error(error);
    }
};
const conversationDetail = ref<MessageDetail>({});
const _getConversationDetail = async () => {
    try {
        const result = await getConversationDetail({ conversationId: msgFilterParams.conversationId });
        conversationDetail.value = result;
    } catch (error: any) {
        message.error(error.message);
        console.error(error);
    }
};
const _conversationSaveRemark = async () => {
    try {
        const result = await conversationSaveRemark({
            conversationId: conversationDetail.value.id,
            remark: conversationDetail.value.remark,
            summary: conversationDetail.value.summary
        });
        message.success(t('保存成功'));
    } catch (error: any) {
        message.error(error.message);
    }
};
watch(
    () => props.conversationId,
    (newVal) => {
        if (newVal && newVal != -1) {
            msgFilterParams.conversationId = newVal;
            msgFilterParams.firstId = -1;
            messageList.value = [];
            loadend.value = false;
            _getConversationMessage();
            _getConversationDetail();
        }
        if (Object.keys(props.currentConversationData).length > 0) {
            messageInfo.value = {
                conversationId: props.currentConversationData.id,
                userId: props.currentConversationData.userId,
                shopId: props.currentConversationData.shopId
            };
        }
    },
    { immediate: true, deep: true }
);
const verifyMessage = (data: any) => {
    return messageList.value.findIndex((item) => item.id === data.id);
};
watch(
    () => props.currentNewMessage,
    (newVal) => {

        if (Object.keys(newVal).length > 0) {
            if (verifyMessage(newVal) === -1) {
                msgTransitionName.value = "im-msg";
                messageList.value.push(newVal as any);
                nextTick(() => {
                    scrollToBottom(true);
                });
            }
        }
    },
    { immediate: true, deep: true }
);

const triggerSetReadStatus = ref(false);
const handleSetRead = () => {
    if (props.currentConversationData.unreadMessageCount > 0 && !triggerSetReadStatus.value) {
        _setRead(props.currentConversationData);
    }
};
const _setRead = async (item: any) => {
    try {
        triggerSetReadStatus.value = true;
        const result = await setRead({
            conversationId: item.id,
            userId: item.userId,
            shopId: item.shopId
        });
        props.currentConversationData.unreadMessageCount = 0;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        triggerSetReadStatus.value = false;
    }
};

const setReadOneself = () => {
    messageList.value.forEach((item) => {
        if (item.type === 2 && item.isRead === 0) {
            item.isRead = 1;
        }
    });
};

// 判断文字是否被[]包裹，如果被包裹则返回true
const replaceText = (text: string) => {
    const reg = /\[(.*?)\]/g;
    return reg.test(text);
};

const serversUpdated = () => {
    emit("update:conversationId", -1);
    emit("_getConversationStrat");
    // 调用注入的函数
};
defineExpose({
    _setRead,
    setReadOneself
});
const currentTabs = ref(1);
const checkTabs = (val: number) => {
    currentTabs.value = val;
};
</script>
<style lang="less" scoped>
.zent-tabs-panel {
    position: relative;
    margin: 0 16px;
    padding: 8px 0;

    &.fav .profile-anonymous-m_line_kvakE label {
        flex: 0 0 85px;
    }

    &.last .profile-anonymous-m_line_kvakE label {
        flex: 0 0 70px;
    }

    .someflex {
        flex: 1 1 auto;
    }

    .edit-remark {
        color: #155bd4;
        font-size: 12px;
    }

    .edit-input {
        width: calc(100% - 6px);

        input {
            width: 100%;
            border: 1px solid #ebedf0;
            border-radius: 2px;
            height: 22px;

            &:focus {
                outline: none;
            }
        }

        textarea {
            border: 1px solid #ebedf0;
            resize: none;
            border-radius: 2px;
            padding: 8px;

            &:focus {
                outline: none;
            }

            &::placeholder {
                color: #bbb;
            }
        }
    }

    .action {
        margin-top: 5px;
    }
}

.wechat-from {
    color: #155bd4;
    line-height: 2;

    .name {
        font-weight: 500;
    }

    .tags {
        font-size: 12px;
    }
}

.thin-line::after {
    content: "";
    position: absolute;
    bottom: 0;
    width: 100%;
    height: 1px;
    background-color: #ebedf0;
}

.im-layout {
    display: flex;
    width: 100%;
    height: 100%;
}

.im-default-box {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;

    .im-default {
        text-align: center;

        .im-default-icon {
            width: 100px;
            height: 100px;
            background: #f8f8f8;
            border-radius: 50%;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .im-default-text {
            color: #999;
            margin-top: 10px;
        }
    }
}

.im-center {
    width: calc(100vw - 676px);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    justify-content: space-between;

    .im-message-box {
        height: 75%;
        overflow-y: auto;

        .im-message-item {
            font-size: 14px;
            line-height: 18px;
            padding: 10px;
            color: #323233;

            &.notice {
                text-align: center;

                .im-message-item__content {
                    display: inline-block;
                    font-size: 12px;
                    border-radius: 3px;
                    padding: 4px 10px;
                    overflow: hidden;
                    white-space: pre-line;
                    color: #999;
                    background-color: #f8f8f8;
                }
            }

            .im-message-item__time {
                text-align: center;
                font-size: 12px;
                padding: 2px 40px;
                margin: 5px auto 2px;
                width: 380px;
                color: #bebcbc;
                visibility: hidden;
            }

            &:hover .im-message-item__time {
                visibility: visible;
            }

            .im-message-item__username {
                font-size: 12px;
                color: #999;
                line-height: 20px;
                margin-right: 48px;
            }

            .im-message-item__content {
                position: relative;
                min-height: 14px;
                border-radius: 4px;
                max-width: calc(100% - 100px);
                word-break: break-all;
                display: inline-block;

                &:before {
                    content: " ";
                    position: absolute;
                    top: 1px;
                    width: 9px;
                    height: 16px;
                    background-position: 50%;
                    background-repeat: no-repeat;
                    background-size: contain;
                }

                .im-message-item__content-value {
                    padding: 10px;

                    .im-message-item__content-image {
                        height: 200px;
                    }

                    :deep(.emoji) {
                        vertical-align: sub;
                        cursor: default;
                        height: 20px;
                        margin: 0 2px;
                    }
                }
            }

            /* 文本text */

            &.text .im-message-item__content {
                white-space: pre-wrap;
            }

            /* 商品card */

            .im-message-item__content-card-link {
                display: block;
                background-color: #fff;
                border-radius: 6px;
                min-width: 258px;
                padding: 10px;

                .im-message-item__content-card-title {
                    font-size: 14px;
                    color: #000;
                    margin-bottom: 5px;
                }

                .im-message-item__content-card-detail {
                    display: flex;
                    align-items: flex-start;

                    .im-message-item__content-card-figure {
                        display: -webkit-box;
                        display: flex;
                        -webkit-box-align: center;
                        align-items: center;
                        -webkit-box-pack: center;
                        justify-content: center;
                        min-width: 60px;
                        min-height: 60px;
                        max-width: 60px;
                        max-height: 60px;
                        overflow: hidden;
                        margin: 0 10px 0 0;
                    }

                    .im-message-item__content-card-desc {
                        font-size: 14px;
                        color: #707070;
                        margin-left: 5px;
                    }
                    .right-info {
                        display: flex;
                        flex-direction: column;
                        justify-content: space-between;
                        height: 60px;
                        .im-message-item__content-card-order-name {
                            font-size: 14px;
                            color: black;
                        }
                        .im-message-item__content-card-order-info {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            color: #999999;
                            font-size: 12px;
                        }
                    }
                }
            }

            &.left .im-message-item__content {
                background-color: #f8f8f8;

                &:before {
                    left: -6px;
                    background-image: url("@/assets/chats/in-arrow.png");
                }
            }

            &.left .content__outer {
                display: flex;
                align-items: flex-start;

                .zent-avatar {
                    margin-right: 10px;
                }
            }

            &.right .im-message-item__content {
                background-color: #eaf4fe;
                margin-right: 10px;

                &:before {
                    right: -6px;
                    background-image: url("@/assets/chats/out-arrow.png");
                }
            }

            &.right > .im-message-item__avatar,
            &.right > .im-message-item__link_avatar {
                float: right;
            }

            &.right .im-message-item__username {
                text-align: right;
            }

            &.right .content__outer {
                display: flex;
                align-items: flex-start;
                //align-items: center;
                flex-direction: row-reverse;

                .zent-avatar {
                    overflow: inherit;

                    img {
                        position: relative;
                        top: -20px;
                        border-radius: 50%;
                    }
                }
            }

            .im-message-item__read,
            .im-message-item__unread {
                display: -webkit-box;
                display: flex;
                clear: both;
                padding-top: 8px;
                margin-right: 50px;
                font-size: 13px;
                -webkit-box-pack: end;
                justify-content: flex-end;
            }

            .im-message-item__read {
                color: #a0a1a3;
            }

            .im-message-item__unread {
                color: #1989fa;
            }
        }
    }
}

.im-right {
    border-left: 1px solid #ebedf0;
    flex-shrink: 0;
    flex-grow: 0;
    flex-basis: 260px;
    position: relative;
    display: flex;
    flex-direction: column;
}

.super-search-box-module_wrap_i3xn3 {
    position: relative;
    margin: 0 10px;
    display: flex;
    align-items: center;
}

.super-search-box-module_search_2qwmN {
    width: 100%;
    box-sizing: border-box;
    border-radius: 2px;
    background-color: #f2f2f2;
    padding: 0 8px;
    height: 26px;
    transition: 0.15s ease-out;
    display: flex;
    align-items: center;
    justify-items: center;
}

.super-search-box-module_icon_1LUOA {
    font-size: 13px;
    color: #bbb;
    font-weight: 100;
    flex: 0 0 13px;
}

.super-search-box-module_input_3HdzN {
    flex: 1 1 100%;
    border: 0;
    outline: 0;
    box-shadow: none;
    margin-left: 5px;
    line-height: 16px;
    padding: 3px 0;
    height: 26px;
    background: transparent;
    box-sizing: border-box;
    min-width: 120px;
}

/* 切换按钮 */
.zent-tabs {
    font-size: 14px;
    line-height: 20px;
    display: -webkit-box;
    display: flex;
    -webkit-box-orient: vertical;
    -webkit-box-direction: normal;
    flex-direction: column;

    .zent-tabs-nav {
        position: relative;
        outline: none;
        display: -webkit-box;
        display: flex;
        -webkit-box-pack: start;
        justify-content: flex-start;
        box-sizing: border-box;
    }

    .zent-tabs-nav-content {
        -webkit-box-flex: 1;
        flex: 1 1 auto;
        overflow: hidden;
    }

    .zent-tabs-scroll {
        white-space: nowrap;
        overflow-x: auto;
        display: -webkit-box;
        display: flex;
    }

    .zent-tabs-tab {
        color: #646566;
        position: relative;
        cursor: pointer;
        box-sizing: border-box;
        text-align: center;
    }
}

.zent-tabs-nav-type__normal {
    border-bottom: 1px solid #dcdee0;
    height: 48px;

    & .zent-tabs-nav-content {
        margin-bottom: -1px;
    }

    & .zent-tabs-tab {
        padding: 0 16px;
    }

    & .zent-tabs-tab__actived {
        border-bottom: 1px solid #dcdee0;
    }

    &.zent-tabs-nav__stretch .zent-tabs-tab {
        -webkit-box-flex: 1;
        flex: 1 1 auto;
    }

    & .zent-tabs-tab-inner {
        padding: 14px 16px;

        &:hover {
            color: #333;
        }
    }

    & .zent-tabs-tab__actived .zent-tabs-tab-inner {
        border-bottom: 2px solid #155bd4;
        padding: 14px 16px 12px;
        margin-bottom: -1px;
    }
}

.profile-anonymous-m_line_kvakE {
    display: flex;
    height: 30px;
    align-items: center;

    label {
        color: #999;
        flex: 0 0 80px;
        text-align: right;
    }
}

.zenticon {
    display: inline-block;
    font-style: normal;
    vertical-align: baseline;
    text-align: center;
    text-transform: none;
    font-variant: normal;
    text-rendering: auto;
    text-decoration: inherit;
    line-height: 1;
    text-rendering: optimizeLegibility;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;

    &.zenticon-right {
        transform: rotateZ(-90deg);
    }

    &.zenticon-caret-right {
        transform: rotateZ(-90deg);
    }
}

.zent-badge {
    position: relative;
    display: inline-block;
    vertical-align: middle;

    .zent-badge-count {
        background-color: #d40000;
        color: #fff;
        display: inline-block;
        box-sizing: border-box;
        border: 1px solid #fff;
        border-radius: 40px;
        text-align: center;
        padding: 0 4px;
        font-size: 12px;
        line-height: 14px;
        display: none;
    }
}

.zent-badge--has-content .zent-badge-count {
    position: absolute;
    top: -4px;
    right: -6px;
    display: flex;
}

.zenticon-warning {
    color: #ed6a0c;
}

.conversation-title-m_wrap_qcEBo {
    flex: 0 0 48px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    border-bottom: 1px solid #e5e5e5;
    padding: 0 10px;
    position: relative;
    overflow: visible;
}

.conversation-title-m_name_1G8ZZ {
    cursor: pointer;
    //margin-right: auto;
    width: 100%;
    display: flex;
    justify-content: space-between;
    margin: 0 5px;

    .name {
        line-height: 24px;
        flex: 1;
    }

    .reconnection {
        flex: 1;
        display: flex;
        justify-content: flex-end;
    }
}

.message-list {
    // flex: 1 1 100%;
    position: relative;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}

.message-list-content {
    width: 100%;
    // flex: 1 1 100%;
    overflow-x: hidden;
    overflow-y: auto;
}

.im-msg-enter-active {
    transition: opacity 0.28s ease, transform 0.28s ease;
}

.im-msg-enter-from {
    opacity: 0;
    transform: translateY(12px);
}

.im-msg-leave-active {
    transition: opacity 0.2s ease;
}

.im-msg-leave-to {
    opacity: 0;
}

.video-sending {
    position: relative;
    height: 200px;
    border-radius: 4px;
    overflow: hidden;
    background-color: #dfe3e8;
    transition: width 0.2s ease;

    .video-sending__poster {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }

    .video-sending__mask {
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 8px;
        background-color: rgba(0, 0, 0, 0.35);
    }

    .video-sending__spinner {
        width: 26px;
        height: 26px;
        border-radius: 50%;
        border: 2px solid rgba(255, 255, 255, 0.35);
        border-top-color: #fff;
        animation: video-sending-rotate 0.8s linear infinite;
    }

    .video-sending__percent {
        font-size: 13px;
        line-height: 18px;
        color: #fff;
    }
}

@keyframes video-sending-rotate {
    to {
        transform: rotate(360deg);
    }
}

.highlight-notice-message-m_notice_umi1U {
    text-align: center;
    font-size: 14px;
    line-height: 18px;
    padding: 10px;
}

.highlight-notice-message-m_content_Ppm8P {
    display: inline-block;
    font-size: 12px;
    padding: 4px 10px;
    overflow: hidden;
    white-space: pre-line;
    color: #999;
    background-color: #f8f8f8;
    position: relative;
    min-height: 14px;
    border-radius: 4px;
    max-width: calc(100% - 100px);
    word-break: break-all;
}

.highlight-notice-message-m_highlight-area_EhH2T {
    margin-left: 12px;
    cursor: pointer;
    color: #3773da;
}

.conversation-list {
    flex: 1 1 100%;
    overflow: hidden;

    .conversation-list-item-avatar {
        margin-right: 10px;
    }

    .conversation-list-item {
        display: flex;
        height: 70px;
        box-sizing: border-box;
        padding: 0 12px;
        background-color: #fff;
        cursor: pointer;
        align-items: center;
        border-bottom: 1px solid #ebedf0;
        position: relative;
        /*
        &:hover {
            .conversation-list-item-mark {
                visibility: inherit;
            }
        }
        */

        &.conversation-list-item-active {
            background-color: #eee;
            color: #999;
        }

        .conversation-list-item-mark {
            position: absolute;
            top: 0;
            left: 0;
            width: 20px;
            height: 20px;
            background: linear-gradient(-45deg, transparent, transparent 50%, #bbb 0, #bbb);
            visibility: hidden;

            &.active {
                visibility: visible;
                background: linear-gradient(-45deg, transparent, transparent 50%, #f5a623 0, #f5a623);
            }
        }

        .conversation-list-item-mark-star.zenticon {
            color: #fff;
            font-size: 12px;
            position: absolute;
            top: 1px;
            left: 0;
            transform: scale(0.9);
        }

        .conversation-list-item-content {
            flex: 1 1 100%;
            overflow: hidden;
        }

        .conversation-list-item-close {
            position: absolute;
            top: 0;
            right: 0;
            bottom: 0;
            width: 48px;
            background: linear-gradient(90deg, transparent, #eee 50%);
            align-items: center;
            justify-content: flex-end;
            padding-right: 8px;
            display: none;
        }

        &:hover .conversation-list-item-close {
            display: flex;
        }

        .conversation-list-item-line {
            display: flex;
            align-items: center;
            overflow: hidden;
            margin-bottom: 6px;
        }

        .conversation-list-item-name {
            text-overflow: ellipsis;
            white-space: nowrap;
            overflow: hidden;
            line-height: 24px;
            font-size: 16px;
            color: #323233;
            flex: 0 1 auto;
            margin-right: 10px;
        }

        .conversation-list-item-tag {
            flex: 0 0 auto;
            margin-right: 5px;
        }

        .conversation-list-item-line {
            display: flex;
            align-items: center;
            overflow: hidden;
            margin-bottom: 6px;
        }

        .conversation-list-item-line:last-child {
            margin-bottom: 2px;
        }

        .conversation-list-item-message {
            text-overflow: ellipsis;
            white-space: nowrap;
            overflow: hidden;
            color: #969799;
            font-size: 14px;
            line-height: 20px;
            flex: 1 1 100%;
        }

        .conversation-list-item-timestamp {
            font-size: 12px;
            line-height: 20px;
            flex: 0 0 auto;
            min-width: 68px;
            font-weight: 400;
            color: #969799;
        }
    }

    .conversation-list-order {
        display: flex;
        height: 70px;
        box-sizing: border-box;
        padding: 0 12px;
        background-color: #fff;
        cursor: pointer;
        align-items: center;
        border-bottom: 1px solid #ebedf0;

        .conversation-list-order-content {
            flex: 1 1 100%;
        }

        .conversation-list-order-title {
            line-height: 20px;
            font-size: 14px;
            color: #333;
        }

        .conversation-list-order-description {
            color: #999;
            line-height: 22px;
            font-size: 12px;
        }
    }
}

.reception-filter-content {
    position: relative;
    font-size: 12px;

    .reception-filter-m_title_2x_xr {
        font-weight: 600;
        margin-bottom: 10px;
    }

    .reception-filter-m_group_2tidL {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        column-gap: 12px;
        row-gap: 12px;
        margin: 12px;
    }

    .reception-filter-m_item_2FrHn {
        height: 24px;
        line-height: 24px;
        padding: 0 10px;
        display: flex;
        border-radius: 12px;
        border: 1px solid #e5e5e5;
        text-align: center;
        justify-content: center;
        cursor: pointer;
    }

    .reception-filter-m_item_2FrHn.reception-filter-m_active_2pulP {
        color: #155bd4;
        border-color: #155bd4;
    }

    .reception-filter-m_actions_1LYfx {
        display: flex;
        justify-content: space-around;
    }
}

.zent-btn {
    color: #323233;
    background-color: #fff;
    display: inline-block;
    height: 32px;
    line-height: 30px;
    font-size: 14px;
    padding: 0 16px;
    border-radius: 2px;
    font-family: inherit;
    border: 1px solid #dcdee0;
    text-align: center;
    vertical-align: middle;
    box-sizing: border-box;
    cursor: pointer;
    -webkit-transition: all 0.3s;
    transition: all 0.3s;

    &.zent-btn-primary {
        background-color: #155bd4;
        border-color: #155bd4;
        color: #fff;
    }

    &.zent-btn-primary-outline {
        border-color: #155bd4;
        background-color: #fff;
        color: #155bd4;
    }
}

/* 头像 */
.zent-avatar {
    color: #fff;
    display: inline-block;
    box-sizing: border-box;
    text-align: center;
    cursor: default;
    overflow: hidden;
    position: relative;
    font-size: 16px;
}

.zent-avatar--size-default {
    width: 32px;
    height: 32px;
    line-height: 32px;
}

.zent-avatar--size-large {
    width: 40px;
    height: 40px;
    line-height: 40px;
}

.zent-avatar--type-image {
    background: transparent;

    .zent-avatar-image {
        width: 100%;
        height: 100%;
        display: block;
    }
}

.zent-avatar--shape-circle {
    border-radius: 50%;
}

/* 标签 */
.zent-tag {
    color: #fff;
    display: inline-block;
    font-size: 12px;
    line-height: 1;
    padding: 2px 4px;
    border: 1px solid #fff;
    box-sizing: border-box;

    &.zent-tag-rounded {
        border-radius: 2px;
    }

    &.zent-tag-style-grey-outline {
        color: #323233;
        border-color: #c8c9cc;
    }
}

/* a标签 */
.zent-link {
    font-size: 14px;
    line-height: 20px;
    text-decoration: none;
    display: inline-block;
    padding: 2px 8px;
    border-radius: 2px;

    &:active,
    &:hover {
        background-color: #f2f3f5;
    }
}

.im-edit-box {
    .im-edit-box-mask {
        border-top: 1px solid #e5e5e5;
        text-align: center;
        padding-top: 50px;
        padding-bottom: 50px;
    }
}
.preview-style {
    border: 1px solid #ebedf0;
    box-sizing: border-box;
}
.reception-panel-toolbar-waiting {
    display: inline-block;
    text-decoration: none;
    align-items: center;
    line-height: 20px;
    color: #155bd4;
    //cursor: pointer;
    justify-content: space-between;
    padding: 2px 8px;
    border-radius: 2px;
    margin-left: 10px;
}
.reception-panel-toolbar-waiting:hover {
    background-color: #f2f3f5;
}
</style>
