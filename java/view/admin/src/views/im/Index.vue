<!--
 * @file IM即时通讯页面
 * @description 后台客服即时通讯系统，支持以下功能：
 *   - 会话列表管理（进行中/已结束会话）
 *   - 实时消息收发（基于WebSocket）
 *   - 联系人和聊天记录搜索
 *   - 待接入客户队列管理
 *   - 会话状态管理（恢复/结束会话）
 *   - 消息已读状态同步
 *   - 客服在线状态管理
 *   - 提示音播放
 * @module views/im/Index
-->
<template>
    <div class="app-layout-m_content_wYOV9">
        <div class="im">
            <div class="im-resizable im-left reception-panel im-resizable-right">
                <div class="reception-panel-search">
                    <div class="super-search-box-module_wrap_i3xn3 reception-panel-search-searcher">
                        <div class="super-search-box-module_search_2qwmN">
                            <span class="super-search-box-module_icon_1LUOA">
                                <SvgIcon name="im-search" width="16" height="16" />
                            </span>
                            <input
                                ref="searchInput"
                                v-model="keyword"
                                @focus="handleFocus"
                                @blur="handleBlur"
                                class="super-search-box-module_input_3HdzN"
                                :placeholder="$t('搜索最近联系人、聊天记录')"
                            />
                            <div v-show="keyword != ''" class="conversation-list-item-del">
                                <el-icon @click.stop.prevent="clearKeyword" size="13" color="#797979"><CircleCloseFilled /></el-icon>
                            </div>
                        </div>
                        <el-button v-show="isOnInput && keyword != ''" class="super-search-btn" type="primary" size="mini" @click="handleSearch"
                            >{{ $t('搜索') }}</el-button
                        >
                    </div>
                </div>
                <div class="reception-panel-mask" v-show="isOnInput">
                    <div class="reception-filter-content">
                        <div class="reception-filter-list" v-if="searchInfo.userList.length > 0">
                            <div class="reception-filter-item-title">{{ $t('联系人({length})', { length: searchInfo.userList?.length || 0 }) }}</div>
                            <div
                                class="reception-filter-item cursor-pointer"
                                v-for="(item, index) in searchInfo.userList"
                                :key="index"
                                @click="handleSwitchSessions(item, 1)"
                            >
                                <div class="reception-filter-item-head">
                                    <span class="zent-avatar zent-avatar--size-large zent-avatar--shape-circle zent-avatar--type-image">
                                        <img v-if="item.user?.avatar" class="zent-avatar-image" :src="avatarFormat(item.user?.avatar)" />
                                        <img v-else class="zent-avatar-image" src="@/assets/avatar/1.jpeg" />
                                    </span>
                                </div>
                                <div class="reception-filter-item-body">
                                    <div class="reception-filter-item-name">
                                        <div class="name">{{ item.user?.username }}</div>
                                        <div class="label text-transform">
                                            {{ item.userFrom }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="reception-filter-list" v-if="searchInfo.conversationList.length > 0">
                            <div class="reception-filter-item-title">{{ $t('聊天记录({length})', { length: searchInfo.conversationList?.length || 0 }) }}</div>
                            <div
                                class="reception-filter-item cursor-pointer"
                                v-for="(item, index) in searchInfo.conversationList"
                                :key="index"
                                @click="handleSwitchSessions(item, 1)"
                            >
                                <div class="reception-filter-item-head">
                                    <span class="zent-avatar zent-avatar--size-large zent-avatar--shape-circle zent-avatar--type-image">
                                        <img v-if="item.user?.avatar" class="zent-avatar-image" :src="avatarFormat(item.user?.avatar)" />
                                        <img v-else class="zent-avatar-image" src="@/assets/avatar/1.jpeg" />
                                    </span>
                                </div>
                                <div class="reception-filter-item-body">
                                    <div class="reception-filter-item-name">
                                        <div class="name">{{ item.user?.username }}</div>
                                        <div class="label text-transform">
                                            {{ item.userFrom }}
                                        </div>
                                    </div>
                                    <div class="reception-filter-item-desc">
                                        <div class="desc">{{ keyword }}</div>
                                        <div class="time"><RelativeTime :time="item.addTime" /></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="reception-panel-wrap">
                    <div class="zent-tabs zent-tabs-type__normal reception-panel-tabs Panel-m_reception-panel-tabs_82Sn7">
                        <div class="zent-tabs-nav zent-tabs-nav-type__normal zent-tabs-nav__stretch">
                            <div class="zent-tabs-nav-content">
                                <div class="zent-tabs-scroll" role="tablist">
                                    <div
                                        class="zent-tabs-tab zent-tabs-tab-type__normal"
                                        :class="{ 'zent-tabs-tab__actived': tabActive === 1 }"
                                        @click="tabClick(1)"
                                    >
                                        <el-badge is-dot :hidden="!isUnreadStrat" :offset="[-80, 15]" color="#f33">
                                            <div class="zent-tabs-tab-inner">{{ $t('会话中({length})', { length: conversationList.length }) }}</div>
                                        </el-badge>
                                    </div>
                                    <div
                                        class="zent-tabs-tab zent-tabs-tab-type__normal"
                                        :class="{ 'zent-tabs-tab__actived': tabActive === 2 }"
                                        @click="tabClick(2)"
                                    >
                                        <el-badge is-dot :hidden="!isUnreadEnd" :offset="[-80, 15]" color="#f33">
                                            <div class="zent-tabs-tab-inner">{{ $t('已结束({length})', { length: conversationListEnd.length }) }}</div>
                                        </el-badge>
                                    </div>
                                    <!-- <div
                                        class="zent-tabs-tab zent-tabs-tab-type__normal"
                                        :class="{ 'zent-tabs-tab__actived': tabActive === 3 }"
                                        @click="tabClick(3)"
                                    >
                                        <div class="zent-tabs-tab-inner">星标</div>
                                    </div> -->
                                </div>
                            </div>
                        </div>
                    </div>

                    <template v-if="tabActive === 1">
                        <div class="reception-panel-toolbar">
                            <CurrentQueueNumber ref="CurrentQueueNumbererRef" @close="currentQueueClose" style="width: 100%">
                                <div class="flex flex-justify-between">
                                    <div> {{ $t('待接入数量：') }}<span class="reception-panel-toolbar-waiting-count">{{ currentQueueNumber }}</span>
                                    </div>
                                    <SvgIcon class="zenticon zenticon-right" name="im-arrow" width="12" height="12" />
                                </div>
                            </CurrentQueueNumber>
                        </div>

                        <div class="collapse-m_wrap_H4xuY reception-panel-ongoing">
                            <div class="collapse-m_panel_2ePsT" style="flex-basis: 40px" :style="'flex-grow: 6'">
                                <div class="conversation-list">
                                    <div v-if="!loading" style="position: relative; width: 280px; overflow: auto; will-change: transform; direction: ltr">
                                        <!-- <div
                                            class="conversation-list-order"
                                            @click="handleSwitchSessions(-1)"
                                            :class="{ 'conversation-list-item-active': conversationId == -1 }"
                                        >
                                            <div class="zent-badge zent-badge--has-content conversation-list-item-avatar">
                                                <div class="zent-badge-content">
                                                    <span class="zent-avatar zent-avatar--size-large zent-avatar--shape-circle zent-avatar--type-image">
                                                        <img class="zent-avatar-image" src="@/assets/chats/ring_bk.png" alt="avatar" />
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="conversation-list-order-content">
                                                <div class="conversation-list-order-title">订单消息</div>
                                                <div class="conversation-list-order-description">新订单消息</div>
                                            </div>
                                        </div> -->
                                        <template v-for="(item, index) in conversationList">
                                            <div
                                                class="conversation-list-item"
                                                @click="handleSwitchSessions(item, 1)"
                                                :class="{ 'conversation-list-item-active': conversationId == item.id }"
                                            >
                                                <div class="conversation-list-item-mark">
                                                    <div class="zent-tooltip-wrapper">
                                                        <SvgIcon
                                                            class="zenticon zenticon-star conversation-list-item-mark-star"
                                                            name="im-markstar"
                                                            width="10"
                                                            height="10"
                                                        />
                                                    </div>
                                                </div>
                                                <div class="">
                                                    <div class="conversation-list-item-del">
                                                        <el-icon @click.stop.prevent="_delConversation(item)" size="20" color="#bbbbbb"
                                                            ><CircleCloseFilled
                                                        /></el-icon>
                                                    </div>
                                                </div>
                                                <div class="zent-badge zent-badge--has-content conversation-list-item-avatar">
                                                    <div class="zent-badge-content">
                                                        <span class="zent-avatar zent-avatar--size-large zent-avatar--shape-circle zent-avatar--type-image">
                                                            <img v-if="item.user?.avatar" class="zent-avatar-image" :src="avatarFormat(item.user?.avatar)" />
                                                            <img v-else class="zent-avatar-image" src="@/assets/avatar/1.jpeg" />
                                                        </span>
                                                        <span class="zent-badge-count" v-if="item.unreadMessageCount > 0">{{ item.unreadMessageCount }}</span>
                                                    </div>
                                                </div>
                                                <div class="conversation-list-item-content">
                                                    <div class="conversation-list-item-line">
                                                        <div class="conversation-list-item-name">{{ item.user ? item.user.username : "" }}</div>
                                                        <div class="conversation-list-item-tag">
                                                            <div
                                                                class="zent-tag zent-tag-style-grey-outline conversation-list-item-tag-default zent-tag-rounded"
                                                            >
                                                                <div class="zent-tag-content text-transform">{{ item.userFrom }}</div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="conversation-list-item-line">
                                                        <div class="conversation-list-item-message" v-if="item.lastMessage">
                                                            <div v-if="item.lastMessage[0]?.messageType == 'text'">
                                                                <div class="conversation-list-item-message-text" v-html="item.lastMessage[0]?.content?.content"></div>
                                                            </div>
                                                            <div v-else>
                                                                {{ item.lastMessage[0]?.messageTypeText }}
                                                            </div>
                                                        </div>
                                                        <div class="conversation-list-item-timestamp">
                                                            <RelativeTime :time="item.lastMessage[0]?.sendTime" />
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                    </div>
                                    <template v-else>
                                        <a-spin :spinning="loading" style="width: 100%; margin-top: 100px; height: 200px" />
                                    </template>
                                </div>
                            </div>
                        </div>
                    </template>
                    <template v-if="tabActive === 2">
                        <div class="collapse-m_wrap_H4xuY reception-panel-ongoing">
                            <div class="collapse-m_panel_2ePsT" style="flex-basis: 40px" :style="'flex-grow: 6'">
                                <div class="conversation-list reception-panel-finished-list">
                                    <div v-if="!loading" style="position: relative; width: 280px; overflow: auto; will-change: transform; direction: ltr">
                                        <template v-for="(item, index) in conversationListEnd">
                                            <div
                                                class="conversation-list-item"
                                                @click="handleSwitchSessions(item, tabActive)"
                                                :class="{ 'conversation-list-item-active': conversationId == item.id }"
                                            >
                                                <div class="conversation-list-item-mark">
                                                    <div class="zent-tooltip-wrapper">
                                                        <SvgIcon
                                                            class="zenticon zenticon-star conversation-list-item-mark-star"
                                                            name="im-markstar"
                                                            width="10"
                                                            height="10"
                                                        />
                                                    </div>
                                                </div>
                                                <div class="zent-badge zent-badge--has-content conversation-list-item-avatar">
                                                    <div class="zent-badge-content">
                                                        <span class="zent-avatar zent-avatar--size-large zent-avatar--shape-circle zent-avatar--type-image">
                                                            <img v-if="item.user?.avatar" class="zent-avatar-image" :src="avatarFormat(item.user?.avatar)" />
                                                            <img v-else class="zent-avatar-image" src="@/assets/avatar/1.jpeg" />
                                                        </span>
                                                        <span class="zent-badge-count" v-if="item.unreadMessageCount > 0">{{ item.unreadMessageCount }}</span>
                                                    </div>
                                                </div>
                                                <div class="conversation-list-item-content">
                                                    <div class="conversation-list-item-line">
                                                        <div class="conversation-list-item-name">{{ item.user ? item.user.username : "" }}</div>
                                                        <div class="conversation-list-item-tag">
                                                            <div
                                                                class="zent-tag zent-tag-style-grey-outline conversation-list-item-tag-default zent-tag-rounded"
                                                            >
                                                                <div class="zent-tag-content text-transform">{{ item.userFrom }}</div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="conversation-list-item-line">
                                                        <div class="conversation-list-item-message" v-if="item.lastMessage">
                                                            {{ item.lastMessage[0].content?.content || item.lastMessage[0].messageTypeText }}
                                                        </div>
                                                        <div class="conversation-list-item-timestamp"><RelativeTime :time="item.addTime" /></div>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                    </div>
                                    <template v-else>
                                        <a-spin :spinning="loading" style="width: 100%; margin-top: 100px; height: 200px" />
                                    </template>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
            <div class="im-resizable-anchor"></div>
            <ImMessage
                ref="ImMessageRef"
                v-if="conversationId"
                v-model:conversationId="conversationId"
                :currentNewMessage="currentNewMessage"
                :currentConversationData="currentConversationData"
                :msgType="tabActive"
                @resumeConversation="resumeConversation"
                @_getConversationStrat="_getConversationStrat"
                @_getConversationEnd="_getConversationEnd"
            ></ImMessage>
        </div>
        <promptTone ref="promptToneRef"></promptTone>
    </div>
</template>
<script setup lang="ts">
import { ref, onMounted, provide, nextTick } from "vue";
import ImMessage from "./src/Message.vue";
import { getConversation, delConversation } from "@/api/im/im";
import type { SearchInfo, SearchInfoList } from "@/types/im/im.d";
import WebSocketClient from "@/services/socket";
import CurrentQueueNumber from "@/views/im/src/CurrentQueueNumber.vue";
import { getWaitServantList, setModifyStatus, getConversationSearch, getServantStatus } from "@/api/im/conversation";
import { message } from "ant-design-vue";
import { RelativeTime } from "@/components/form";
import { avatarFormat } from "@/utils/format";
import { useImStore } from "@/store/im";
import { CircleCloseFilled } from "@element-plus/icons-vue";
import { ElMessageBox } from "element-plus";
import promptTone from "./src/promptTone.vue";
import { useRouter } from "vue-router";
import { useConfigStore } from "@/store/config";
import { isJsonString } from "@/utils/util";

/**
 * 组件事件定义
 * @event update:isEditComment - 编辑评论状态更新
 * @event successAdded - 成功添加事件
 * @event sendMessage - 发送消息事件
 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["update:isEditComment", "successAdded", "sendMessage"]);

/** IM消息组件引用 */
const ImMessageRef = ref();

/** 提示音组件引用 */
const promptToneRef = ref();

/** 当前选中的会话ID，-1表示未选中 */
const conversationId = ref<number>(-1);

/** 当前激活的Tab页：1-进行中，2-已结束 */
const tabActive = ref<number>(1);

/** 进行中会话是否有未读消息 */
const isUnreadStrat = ref(false);

/** 已结束会话是否有未读消息 */
const isUnreadEnd = ref(false);

/**
 * Tab切换处理
 * @param {number} value - Tab值：1-进行中，2-已结束
 */
const tabClick = (value: number) => {
    tabActive.value = value;
    if (value === 1) {
        _getConversationStrat("tab");
        isUnreadStrat.value = false;
    }
    if (value === 2) {
        _getConversationEnd("tab");
        isUnreadEnd.value = false;
    }
};

/** 搜索关键词 */
const keyword = ref<string>("");

/** 是否正在输入搜索（控制搜索遮罩显示） */
const isOnInput = ref<boolean>(false);

/** 搜索输入框引用 */
const searchInput = ref<any>(null);

/** 搜索结果数据 */
const searchInfo = ref<SearchInfo>({
    conversationList: [],
    userList: []
});

/**
 * 搜索框获得焦点
 */
const handleFocus = () => {
    isOnInput.value = true;
};

/**
 * 搜索框失去焦点
 */
const handleBlur = () => {
    if (keyword.value == "" && searchInfo.value.conversationList.length == 0 && searchInfo.value.userList.length == 0) {
        isOnInput.value = false;
    }
};

/**
 * 清除搜索关键词
 */
const clearKeyword = () => {
    keyword.value = "";
    searchInfo.value = {
        conversationList: [],
        userList: []
    };
    searchInput.value.focus();
};
/**
 * 执行搜索
 * @description 搜索联系人和聊天记录
 * @param {Event} event - 事件对象
 */
const handleSearch = async (event: any) => {
    searchInput.value.focus();
    if (keyword.value == "") {
        return;
    }
    try {
        const result = await getConversationSearch({ keyword: keyword.value });
        searchInfo.value = result;
        console.log(searchInfo.value);
    } catch (error: any) {
        message.error(error.message);
    }
};

/** 当前新消息（WebSocket接收到的消息） */
const currentNewMessage = ref({});

/** 当前会话数据 */
const currentConversationData = ref<any>({});

/**
 * 切换会话
 * @description 选择不同的会话并标记已读
 * @param {Object} item - 会话对象，-1表示取消选中
 * @param {number} type - 会话类型
 */
const handleSwitchSessions = (item: any, type?: number) => {
    if (item != -1) {
        currentConversationData.value = item;
        conversationId.value = item.id;
        if (item.unreadMessageCount > 0) {
            item.unreadMessageCount = 0;
            ImMessageRef.value?._setRead(item);
        }
    } else {
        conversationId.value = -1;
    }
};

/**
 * 删除会话
 * @param {Object} item - 会话对象
 */
const _delConversation = async (item: any) => {
    console.log(item);
    ElMessageBox.confirm(t('确认删除该会话?'), {
        confirmButtonText: t('确认'),
        cancelButtonText: t('取消'),
        type: "warning"
    }).then(() => {
        delConversation({
            conversationId: item.id
        })
            .then((res) => {
                currentConversationData.value = "";
                conversationId.value = -1;
                _getConversationStrat();
            })
            .catch((error: any) => {
                message.error(error.message);
            });
    });
};

/**
 * 恢复会话回调
 * @description 从已结束状态恢复会话
 * @param {any} data - 恢复结果数据
 */
const resumeConversation = (data: any) => {
    if (data) {
        currentConversationData.value.status = 1;
        tabClick(2);
        tabClick(1);
    }
};
/** 进行中的会话列表 */
const conversationList = ref<SearchInfoList[]>([]);

/** 已结束的会话列表 */
const conversationListEnd = ref<SearchInfoList[]>([]);

/** 加载状态 */
const loading = ref(false);

/**
 * 获取进行中的会话列表
 * @param {string} type - 调用类型，'tab'表示Tab切换调用，不更新未读状态
 */
const _getConversationStrat = async (type?: string) => {
    try {
        loading.value = true;
        console.log("====");
        const resultStrat = await getConversation({ status: 1 });
        conversationList.value = resultStrat.records;
        if (type !== "tab") {
            resultStrat.records.forEach((item: any) => {
                if (Number(item.unreadMessageCount) > 0) {
                    isUnreadStrat.value = true;
                }
            });
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/**
 * 获取已结束的会话列表
 * @param {string} type - 调用类型，'tab'表示Tab切换调用，不更新未读状态
 */
const _getConversationEnd = async (type?: string) => {
    try {
        loading.value = true;
        const resultEnd = await getConversation({ status: 2 });
        conversationListEnd.value = resultEnd.records;
        console.log(isUnreadEnd.value);
        if (type !== "tab") {
            resultEnd.records.forEach((item: any) => {
                if (Number(item.unreadMessageCount) > 0) {
                    isUnreadEnd.value = true;
                }
            });
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/**
 * 待接入队列关闭回调
 * @description 刷新所有会话列表
 */
const currentQueueClose = async () => {
    await _getConversationStrat();
    await _getConversationEnd();
};

/**
 * WebSocket连接成功回调
 * @param {Event} event - WebSocket事件
 */
const onOpen = (event: any) => {
    // console.log("连接成功", event);
};

/**
 * 格式化并处理接收到的消息
 * @description 将消息分发到对应的会话，更新未读数，播放提示音
 * @param {Object} data - 消息数据
 */
const formatMessage = (data: any) => {
    if (data.content?.contentCategory === "end_conversation") {
        const idx = conversationList.value.findIndex((item) => item.id === data.conversationId);
        if (idx > -1) {
            conversationList.value.splice(idx, 1);
        }
        if (data.conversationId === conversationId.value) {
            currentConversationData.value = {};
            conversationId.value = -1;
        }
        _getConversationEnd();
        isUnreadEnd.value = true;
        return;
    }

    const conversationIndex = conversationList.value.findIndex((item) => item.id === data.conversationId);
    if (conversationId.value > -1) {
        if (data.conversationId === conversationId.value) {
            currentNewMessage.value = data;
        } else if (conversationIndex === -1) {
            creatingSession(data);
        }
        if (conversationIndex > -1) {
            conversationList.value[conversationIndex].lastMessage[0] = data;
            conversationList.value[conversationIndex].unreadMessageCount++;
        }
    } else {
        if (conversationIndex > -1) {
            conversationList.value[conversationIndex].lastMessage[0] = data;
            conversationList.value[conversationIndex].unreadMessageCount++;
        } else {
            creatingSession(data);
        }
    }
    promptToneRef.value?.play();
};

/**
 * 创建新会话
 * @description 当收到新用户消息时创建会话记录
 * @param {Object} data - 消息数据
 */
const creatingSession = (data: any) => {
    const newConversationData: any = {
        id: data.conversationId,
        shopId: data.shopId,
        userId: data.userId,
        lastMessage: [data],
        unreadMessageCount: 1,
        user: data.user,
        userFrom: data.userFrom
    };
    conversationList.value = [newConversationData, ...conversationList.value];
};

/**
 * WebSocket消息接收处理
 * @description 处理不同类型的WebSocket消息（新消息、已读状态）
 * @param {MessageEvent} event - WebSocket消息事件
 */
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
        if (data.data[0].shopId === currentConversationData.value.shopId) {
            ImMessageRef.value?.setReadOneself();
        }
    }
};

/**
 * WebSocket连接关闭回调
 * @param {Event} event - WebSocket事件
 */
const onClose = (event: any) => {
    // console.log("连接关闭", event);
};

/**
 * WebSocket连接错误回调
 * @param {Event} event - WebSocket事件
 */
const onError = (event: any) => {
    // console.log("连接错误", event);
};

/**
 * 获取WebSocket连接URL
 * @description 优先使用环境变量配置，否则根据当前页面URL自动生成
 * @returns {string} WebSocket URL
 */
const getSocketUrl = () => {
    let url;
    if (import.meta.env.VITE_SOCKET_URL) {
        url = import.meta.env.VITE_SOCKET_URL;
    } else {
        url = location.origin ? location.origin.replace(/^http:/, "ws:").replace(/^https:/, "wss:") + "/ws" : "";
    }
    return url;
};

/**
 * 初始化WebSocket连接
 * @description 建立WebSocket连接并绑定事件处理函数
 */
const initWs = () => {
    const ws = new WebSocketClient(`${getSocketUrl()}?token=${localStorage.getItem("accessToken")}&platform=admin`);
    ws.onclose(onClose);
    ws.onopen(onOpen);
    ws.onerror(onError);
    ws.onmessage(onMessage);
    ws.connect();
};
initWs();

/** 待接入客户数量 */
const currentQueueNumber = ref<number>(0);

/** 待接入队列组件引用 */
const CurrentQueueNumbererRef = ref<any>(null);

/**
 * 加载初始化数据
 * @description 同步客服状态，获取待接入队列数量
 */
const loadFilter = async () => {
    try {
        // 初始化从后端同步客服状态（状态1在线2忙3离开）
        try {
            const ImStore = useImStore();
            const res: any = await getServantStatus();
            const status = typeof res === "number" ? res : res?.status;
            if (status === 1 || status === 2 || status === 3) {
                ImStore.setImPresence(status);
            }
        } catch (e) {
            // 不阻塞后续流程：失败就沿用本地 store
            console.error(e);
        }

        const result = await getWaitServantList({ page: 1 });
        currentQueueNumber.value = result.total;
        if (currentQueueNumber.value > 0) {
            nextTick(() => {
                CurrentQueueNumbererRef.value.showWin();
            });
        }
    } catch (error: any) {
        message.error(error.message);
    }
};

/**
 * 接收子组件消息回调
 * @description 通过provide/inject向下传递的回调函数，用于更新会话最后消息
 * @param {Object} value - 消息数据
 */
const receiveValueFromGrandChild = (value: any) => {
    console.log("receiveValueFromGrandChild", value);
    conversationList.value.map((item: any) => {
        if (item.id === value.conversationId) {
            item.lastMessage = [value];
        }
    });
};

provide("receiveValueFromGrandChild", receiveValueFromGrandChild);
const query = useRouter().currentRoute.value.query;
onMounted(async () => {
    await loadFilter();
    await _getConversationStrat();
    await _getConversationEnd();
    //获取路由数据。打开会话
    if (query.id) {
        const matchedConversation = conversationList.value.find((item) => item.id === parseInt(query.id as string));
        if (matchedConversation) {
            await handleSwitchSessions(matchedConversation, 1);
        }
    }
});
</script>
<style lang="less" scoped>
.app-layout-m_content_wYOV9 {
    font-size: 14px;
    width: 100%;
    height: 100%;
}
.im {
    display: flex;
    position: relative;
    width: 100%;
    height: 100%;
    .im-left {
        border-right: 1px solid #ebedf0;
        flex-shrink: 0;
        flex-grow: 0;
        flex-basis: 280px;
        width: 280px;
    }
    .im-resizable {
        position: relative;
        overflow: hidden;
    }
    .im-resizable-anchor {
        cursor: col-resize;
        z-index: 20;
        overflow: visible;
        height: 100%;
        width: 0;
        &:before {
            content: "";
            position: absolute;
            width: 10px;
            height: 100%;
            transform: translateX(-50%);
        }
    }
    .im-center {
        flex: 1 1 100%;
        display: flex;
        flex-direction: column;

        overflow: hidden;
        .im-message-box {
            flex: 1 1 100%;
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
                    }
                }
                /* 文本text */
                &.text .im-message-item__content {
                    white-space: pre-wrap;
                }
                /* 商品card */
                &.card .im-message-item__content-card-link {
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
                            width: 60px;
                            height: 60px;
                            overflow: hidden;
                            margin: 0;
                        }
                        .im-message-item__content-card-desc {
                            font-size: 14px;
                            color: #707070;
                            margin-left: 5px;
                        }
                    }
                }
                &.left .im-message-item__content {
                    background-color: #f8f8f8;
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
                    justify-content: flex-start;
                    align-items: center;
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
        .im-edit-box {
            display: flex;
            flex: 0 0 150px;
            overflow: hidden;
            border-top: 1px solid #e5e5e5;
            flex-direction: column;
            position: relative;
            .im-edit-box-mask {
                position: absolute;
                top: 0;
                right: 0;
                bottom: 0;
                left: 0;
                background-color: hsla(0, 0%, 100%, 0.8);
                font-size: 14px;
                color: #999;
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 2;
                line-height: 24px;
            }
            .im-edit-box-toolbar {
                display: flex;
                align-items: center;
                padding: 0 5px;
                flex: 0 0 42px;
                .zent-tooltip-wrapper {
                    display: flex;
                }
                .toolbar-pop-m_wrap_29o87 {
                    width: 38px;
                    height: 32px;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
            }
            .editor-m_wrap_QM07L {
                flex: 1 1 100%;
                display: flex;
                flex-direction: column;
                height: 80px;
                overflow: hidden;
                font-size: 14px;
                margin: 0 7px 7px;
            }
            .editor-wrap {
                flex: 1 1 100%;
                overflow-x: hidden;
                overflow-y: auto;
                position: relative;
                textarea {
                    width: 100%;
                    height: 100%;
                    resize: none;
                    border: 0;
                    &:focus {
                        border: none;
                        outline: none;
                    }
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
    }
}

.reception-panel {
    display: flex;
    flex-direction: column;
    .reception-panel-search {
        position: relative;
        overflow: visible;
        height: 50px;
        display: flex;
        align-items: center;
        .reception-panel-search-searcher {
            flex: 1 1 100%;
            position: relative;
        }
        .super-search-btn {
            width: 50px;
            height: 26px;
            margin-left: 5px;
        }
    }
    .reception-panel-mask {
        width: 100%;
        height: calc(100% - 50px);
        position: absolute;
        top: 50px;
        left: 0;
        z-index: 99999;
        background: rgba(0, 0, 0, 0.5);
        .reception-filter-content {
            background: #fff;
            .reception-filter-item-title {
                padding: 10px;
                font-size: 14px;
                color: #999;
                background-color: #f8f8f8;
            }
            .reception-filter-item {
                display: flex;
                align-items: center;
                padding: 10px;
                .reception-filter-item-head {
                    margin-right: 10px;
                    .zent-avatar-image {
                        width: 40px;
                        height: 40px;
                    }
                }
                .reception-filter-item-body {
                    flex: 1;
                    .reception-filter-item-name {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        .name {
                            font-size: 16px;
                            width: 160px;
                            white-space: nowrap; /* 不换行 */
                            overflow: hidden; /* 隐藏溢出部分 */
                            text-overflow: ellipsis; /* 显示省略号 */
                        }
                        .label {
                            height: 14px;
                            padding: 0 2px;
                            border: 1px solid #c8c9cc;
                            border-radius: 2px;
                            font-size: 12px;
                            line-height: 12px;
                        }
                    }
                    .reception-filter-item-desc {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        margin-top: 15px;
                        .desc {
                            font-size: 16px;
                            color: #155bd4;
                            width: 100px;
                            white-space: nowrap; /* 不换行 */
                            overflow: hidden; /* 隐藏溢出部分 */
                            text-overflow: ellipsis; /* 显示省略号 */
                        }
                        .time {
                            color: #999;
                        }
                    }
                }
            }
        }
    }
    .reception-panel-wrap {
        flex: 1 1 100%;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        position: relative;
    }
    .reception-panel-toolbar {
        border-bottom: 1px solid #ebedf0;
        flex: 0 0 36px;
        box-sizing: border-box;
        background-color: #f2f3f5;
        color: #969799;
        display: flex;
        padding: 0 5px 0 16px;
        align-items: center;
        line-height: 20px;
        .zenticon {
            margin: 4px;
        }
        .reception-panel-auto-join {
            display: flex;
            align-items: center;
            cursor: pointer;
            font-size: 14px;
        }
        .reception-panel-toolbar-waiting-count {
            color: #155bd4;
            font-size: 14px;
        }
    }
    .reception-panel-ongoing,
    .reception-panel-recent {
        flex: 1 1 100%;
        overflow: hidden;
    }
    .reception-panel-ongoing-collapse-title,
    .reception-panel-recent-collapse-title {
        background-color: #f6f7fa;
        color: #323233;
        cursor: auto;
    }
    .reception-panel-ongoing-collapse-count,
    .reception-panel-recent-collapse-count {
        margin-right: auto;
        cursor: default;
    }
    .reception-panel-ongoing-collapse-btn-wrapper,
    .reception-panel-recent-collapse-btn-wrapper {
        margin-left: 12px;
    }
    .reception-panel-ongoing-collapse-btn,
    .reception-panel-recent-collapse-btn {
        cursor: pointer;
        font-size: 16px;
        text-align: center;
    }
}

.super-search-box-module_wrap_i3xn3 {
    position: relative;
    margin: 0 5px;
    display: flex;
    align-items: center;
}
.super-search-box-module_search_2qwmN {
    flex: 1;
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
        padding: 0 16px;
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
.Panel-m_reception-panel-tabs_82Sn7 .zent-tabs-nav-type__normal {
    border-color: #ebedf0;
    & .zent-tabs-nav-content {
        padding: 0;
    }
    & .zent-tabs-tab__actived {
        border-color: #ebedf0;
    }
}
.zent-tabs.right-plugins-m_tabs_1oN_5 {
    width: 100%;
    height: 100%;
    overflow: hidden;
    display: flex;
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
}
.zent-tabs.right-plugins-m_tabs_1oN_5 .zent-tabs-nav-content .zent-tabs-tab {
    padding: 0 13.5px;
    flex: 1 1 auto;
    min-width: 65px;
}
.zent-tabs.right-plugins-m_tabs_1oN_5 .zent-tabs-nav {
    flex: 0 0 auto;
}
.zent-tabs.right-plugins-m_tabs_1oN_5 .zent-tabs-panel-wrapper {
    margin-top: 10px;
    flex: 1 1 100%;
    overflow: hidden;
    position: relative;
}
.zent-tabs.right-plugins-m_tabs_1oN_5 .zent-tabs-panel-wrapper .zent-tabs-panel {
    width: 100%;
    height: 100%;
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    display: flex;
    flex-direction: column;
}
.profile-anonymous-m_line_kvakE {
    display: flex;
    height: 30px;
    align-items: center;
    padding: 0 10px;
    label {
        color: #999;
        flex: 0 0 80px;
        text-align: right;
    }
}

.collapse-m_wrap_H4xuY {
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
.collapse-m_panel_2ePsT {
    overflow: hidden;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    transition: flex 0.3s ease;
}
.collapse-m_title_NsNAh {
    flex-grow: 0;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    padding: 0 14px;
    background-color: #f6f7fa;
    border-bottom: 1px solid #ebedf0;
    box-sizing: border-box;
    -webkit-user-select: none;
    -moz-user-select: none;
    user-select: none;
    .collapse-m_indicator_19-6d {
        font-size: 16px;
        transition: transform 0.2s ease-out;
        margin-right: 5px;
    }
}
.check-menu-m_option_1soJF {
    display: flex;
    align-items: center;
    &.active {
        svg {
            visibility: inherit;
        }
    }
    svg {
        visibility: hidden;
        margin-left: 8px;
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
    margin-right: auto;
}
.conversation-title-m_name_1G8ZZ > span:first-child {
    margin-right: 5px;
    display: inline-flex;
    align-items: center;
}
.message-list {
    flex: 1 1 100%;
    position: relative;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}
.message-list-content {
    width: 100%;
    flex: 1 1 100%;
    overflow-x: hidden;
    overflow-y: auto;
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
        &:hover {
            background-color: #f5f5f5;
            .conversation-list-item-del {
                visibility: inherit;
            }
        }
        &.conversation-list-item-active {
            background-color: #eee;
            color: #999;
        }
        .conversation-list-item-del {
            position: absolute;
            right: 10px;
            top: 0;
            margin-top: calc(35px - 10px);
            visibility: hidden;
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
            .conversation-list-item-message-text {
                :deep(.emoji) {
                    vertical-align: sub;
                    cursor: default;
                    height: 20px;
                    margin: 0 2px;
                }
            }
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
        &.conversation-list-item-active {
            background-color: #eee;
            color: #999;
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

/* tip提示 */
.zent-alert {
    border: 1px solid #dcdee0;
    padding: 12px 16px;
    border-radius: 2px;
    &.zent-alert-style-warning {
        background-color: #fff5ed;
        border-color: #f1924e;
    }
    .zent-alert-item {
        font-size: 14px;
        line-height: 20px;
        color: #323233;
        box-sizing: border-box;
        display: -webkit-box;
        display: flex;
        align-items: center;
    }
    .zent-alert-item-icon {
        margin-right: 8px;
    }
    .zent-alert-item-content {
        -webkit-box-flex: 1;
        flex: 1 1 auto;
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
</style>
