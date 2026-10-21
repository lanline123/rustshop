/** * @file 消息计数组件 * @description 顶部导航栏的消息中心入口，显示未读消息数和用户菜单 * @features * - SSE实时消息推送 * - 未读消息计数 * - 新订单提醒 * - 用户下拉菜单（切换组织、管理账号、清除缓存、退出登录） */
<template>
    <div class="top-bar-right flex flex-align-center" :class="{ 'locale-en': locale === 'en' }">
        <div class="top-bar-item top-admin_msg" @click="markAsRead" :class="themeInfo.layout">
            <DialogForm
                :is-drawer="false"
                path="panel/adminMsg/AdminMsg"
                :title="$t('消息中心')"
                :showFooter="false"
                width="1100px"
                dialogClass="admin-msg-dialog"
                :bodyStyle="{ padding: 0, overflow: 'hidden' }"
                @callback="refreshMessageUI"
                @closeCallback="handleDialogClose"
            >
                <a class="top-bar-btn lyecs-dialogPage" :class="themeInfo.navTheme">
                    <i class="iconfont-admin icon-tongzhi-L"></i>
                    <span class="top-bar-text" v-if="themeInfo.layout !== 'topMenu'">{{ $t('消息') }}</span>
                    <span class="admin_msg-order_count" v-if="message?.newOrderMsgCount > 0 && themeInfo.layout !== 'topMenu'">{{ $t('新订单{newOrderMsgCount}', { newOrderMsgCount: message.newOrderMsgCount }) }}</span>
                    <em class="admin_msg-count" v-if="useStore.messageCount > 0">{{ useStore.messageCount }}</em>
                </a>
            </DialogForm>
        </div>
        <VoiceBroadcastEntry />
        <div class="top-bar-item" :class="themeInfo.layout" v-if="screenWidth > 767">
            <a class="top-bar-btn" :class="themeInfo.navTheme" :href="viewMallUrl" target="_blank"
                ><i class="iconfont icon-guanli"></i><span class="top-bar-text" v-if="themeInfo.layout !== 'topMenu'">{{ $t('查看商城') }}</span></a
            >
        </div>
        <div class="top-bar-item top-bar-user" id="dropdown-memu" :class="themeInfo.layout">
            <a-dropdown :trigger="['click']">
                <div class="admin-user-warp" v-if="userInfo.username">
                    <a class="top-bar-btn" :class="themeInfo.navTheme" v-if="screenWidth >= 900">
                        <span class="admin-user-photo">
                            <template v-if="extractContent(String(displayAvatar)) == 'def'">
                                <img :src="returnAvatar(displayAvatar)" />
                            </template>
                            <template v-else>
                                <img :src="imageFormat(displayAvatar)" />
                            </template>
                        </span>
                        <div class="admin-user-name" v-if="themeInfo.layout !== 'topMenu' || adminType === 'shop'">
                            {{ /^1[3-9]\d{9}$/.test(userInfo.username) ? userInfo.username.replace(/(\d{3})\d{4}/, "$1****") : userInfo.username }}
                        </div>
                        <i class="iconfont icon-xiala"></i>
                    </a>
                    <a class="top-bar-btn" v-else>
                        <i class="iconfont icon-xiala"></i>
                    </a>
                </div>
                <template #overlay>
                    <div class="dropdown-memu top-bar-memu" :class="{ 'locale-en': locale === 'en' }">
                        <div class="entrance-list">
                            <p class="entrance lyecs-openPage" v-if="isMerchant() || isS2b2c() || isStore()" @click="switchShop">
                                <i class="iconfont-tig icon-shanghu"></i>
                                <span>{{ $t('切换组织') }}</span>
                            </p>
                            <a class="entrance lyecs-openPage" :href="viewMallUrl" target="_blank" v-if="screenWidth <= 767">
                                <i class="iconfont icon-guanli"></i>
                                <span>{{ $t('查看商城') }}</span>
                            </a>
                            <router-link
                                :to="{
                                    path: adminType == 'shop' || adminType == 'vendor' ? '/setting/account-editing/index' : '/authority/account-editing/index'
                                }"
                            >
                                <p class="entrance lyecs-openPage">
                                    <!-- <i class="iconfont icon-gerenshezhi"></i> -->
                                    <i class="iconfont-tig icon-guanlizhanghao"></i>
                                    <span>{{ $t('管理账号') }}</span>
                                </p>
                            </router-link>
                            <p class="entrance" @click="clearCache">
                                <i class="iconfont-tig icon-qingchuhuancun"></i>
                                <span>{{ $t('清除缓存') }}</span>
                            </p>
                            <p class="entrance" @click="onLogout">
                                <i class="iconfont-tig icon-tuichudenglu"></i>
                                <span>{{ $t('退出登录') }}</span>
                            </p>
                        </div>
                    </div>
                </template>
            </a-dropdown>
        </div>
    </div>
    <div class="dialog-box">
        <el-dialog :z-index="12" v-model="dialogVisible">
            <SelectShop v-if="dialogVisible" @closePopup="closePopup" :isIndex="true" :username="userInfo.username"> </SelectShop>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, onBeforeUnmount, watch } from "vue";
import SelectShop from "@/components/merchant/SelectShop.vue";
import { extractContent } from "@/utils/util";
import { returnAvatar } from "@/utils/avatar";
import { getAdminMsgCount } from "@/api/common/common";
import { getAdminInfo } from "@/api/authority/accountEditing";
import { getAdminType } from "@/utils/storage";
import { DialogForm } from "@/components/dialog";
import { useConfigStore } from "@/store/config";
import { useMenusStore } from "@/store/menu";
import { isMerchant, isS2b2c, isStore } from "@/utils/version";
import { useThemeStore } from "@/store/theme";
import { useUserStore } from "@/store/user";
import { urlFormat, imageFormat, urlWapFormat } from "@/utils/format";
import { cleanUp } from "@/api/common/common";
import { notification } from "ant-design-vue";
import { updateMenu } from "@/utils/menus";
import type { MainMenu } from "@/types/common/common.d";
import { requestUrl } from "@/utils/request";
import VoiceBroadcastEntry from "./VoiceBroadcast/VoiceBroadcastEntry.vue";
import { dispatchVoiceBroadcast } from "@/utils/voiceBroadcastSSEAdapter";
import { voiceBroadcaster } from "@/utils/voiceBroadcaster";
import { voiceBroadcastLeader } from "@/utils/voiceBroadcastLeader";
import type { VoiceBroadcastSSEPayload } from "@/types/voiceBroadcast.d";
import { useI18n } from "vue-i18n"
const { t, locale } = useI18n()
const { themeInfo } = useThemeStore();
/** 管理员类型 */
const adminType = ref(localStorage.getItem("adminType"));
const useStore = useUserStore();
const menusStore = useMenusStore();
const { logout, updateUserInfo } = useUserStore();
/** 用户信息 */
const userInfo = computed(() => useUserStore().userInfo);

/**
 * 顶部头像独立拉取的头像值
 * 与 @/views/authority/accountEditing/Index.vue 共用同一接口、同一解析逻辑：
 *   - shop 类型：提交走 merchant/adminUserShop/modifyUser 只写 admin_user_shop.avatar，
 *     所以回显也必须读 result.avatar（admin_user_shop 表），不能用 store 里来自 mineDetail 的 admin_user.avatar，
 *     否则刷新页面后顶部头像会变回旧值。
 *   - 非 shop 类型：与 accountEditing 保持同样的兜底 result.avatar ?? result.adminUser?.avatar
 *
 * 为避免刷新瞬间"先显示 store 旧头像、接口返回后又切到新头像"的闪烁，
 * 把上一次成功取到的头像按 adminId 持久化到 localStorage，刷新时立即作为初始值。
 */
const AVATAR_CACHE_PREFIX = "msgCountAdminAvatar:";

/** 取持久化缓存的头像（按 adminId 隔离，避免切换账号串号显示） */
const readCachedAvatar = (): string => {
    try {
        const raw = localStorage.getItem("user");
        const adminId = raw ? JSON.parse(raw)?.userInfo?.adminId : null;
        if (!adminId) return "";
        return localStorage.getItem(`${AVATAR_CACHE_PREFIX}${adminId}`) || "";
    } catch {
        return "";
    }
};

/** 写入持久化缓存 */
const writeCachedAvatar = (adminId: number | string, avatar: string) => {
    try {
        localStorage.setItem(`${AVATAR_CACHE_PREFIX}${adminId}`, avatar);
    } catch {
        /* 容量满或隐私模式等异常静默忽略 */
    }
};

const avatarFromAdminInfo = ref(readCachedAvatar());

/** 模板使用的头像源：优先用 adminInfo 接口（或其缓存）的，回退到 store userInfo.avatar */
const displayAvatar = computed(() => avatarFromAdminInfo.value || userInfo.value?.avatar || "");

const loadAvatarFromAdminInfo = async (adminId: number | string) => {
    if (!adminId) return;
    try {
        const result: any = await getAdminInfo({ id: adminId });
        const resolvedUser = getAdminType() != "shop" ? result : result?.adminUser || result;
        const resolvedAvatar = result?.avatar ?? resolvedUser?.avatar ?? "";
        const avatar = String(resolvedAvatar || "");
        avatarFromAdminInfo.value = avatar;
        writeCachedAvatar(adminId, avatar);
    } catch (e) {
        // 静默失败，模板会回退到 store userInfo.avatar
    }
};

// 等 store.updateUserInfo 拿到 adminId 后再请求详情，避免 adminId 为空时无效调用
watch(
    () => userInfo.value?.adminId,
    (id) => {
        if (id) loadAvatarFromAdminInfo(id);
    },
    { immediate: true }
);

import dayjs from "dayjs";
const config: any = useConfigStore();
/** 消息数据 */
const message = ref<any>(null);
/** 当前活动 SSE(fetch 流) 的中止控制器；重连/卸载时用它真正中断旧流，避免旧流残留写入导致计数横跳 */
let sseController: AbortController | null = null;
/** 组件已卸载标记：为 true 时不再自动重连 */
let sseClosed = false;
/** 上一次 SSE payload，用于语音播报增量计算；重连后置空避免跨连接误判 */
let prevVoicePayload: VoiceBroadcastSSEPayload | null = null;

/** 刷新消息UI */
const refreshMessageUI = () => {
    if (message.value) {
        message.value = { ...message.value };
    }
};

/**
 * 弹窗关闭后刷新顶部消息计数
 * @description 弹窗内读消息只会更新红点未读数，不会动按 startTime 计算的“新订单”计数；
 *              且旧 SSE 仍按旧 startTime 持续推送旧计数。这里把 lastOpenTime 更新为当前时间
 *              并重连 SSE，让顶部计数按最新基准从服务端重新取一帧，正确清掉“新订单”角标。
 *              重连首帧只重建播报基线、不触发播报，故不影响语音播报。
 */
const handleDialogClose = () => {
    localStorage.setItem("lastOpenTime", String(dayjs().unix()));
    initSSE();
};

/** 获取消息数量 */
const fetchMessage = async () => {
    try {
        const response = await getAdminMsgCount({ startTime: localStorage.getItem("lastOpenTime") });
        message.value = response;
        useStore.messageCount = response.unreadMsgCount;
    } catch (error) {
        console.error("请求消息失败:", error);
    }
};

/** 安排重连：仅当未卸载且发起方仍是当前活动流时才重连，避免旧流触发重复连接 */
const scheduleReconnect = (controller: AbortController) => {
    if (sseClosed || controller !== sseController) {
        return;
    }
    setTimeout(() => {
        if (!sseClosed && controller === sseController) {
            initSSE();
        }
    }, 5000);
};

/** 初始化SSE连接 */
const initSSE = () => {
    // 真正中止上一条 fetch 流，避免旧流残留继续写入 message.value 导致计数横跳
    if (sseController) {
        sseController.abort();
        sseController = null;
    }
    // 重连后清空 prev，避免用旧 payload 与新连接首条做 delta 误判
    prevVoicePayload = null;
    fetchSSEPolyfill();
};

// 使用Fetch API实现的SSE Polyfill
const fetchSSEPolyfill = () => {
    const headers = requestUrl.headers();
    const startTime = localStorage.getItem("lastOpenTime") || "";
    const controller = new AbortController();
    sseController = controller;
    /** 仅当本流仍是当前活动流且未卸载时，才允许处理数据 */
    const isActive = () => !sseClosed && controller === sseController && !controller.signal.aborted;
    /** 应用一帧 payload（带活动流校验，防止旧流写入） */
    const applyData = (data: any) => {
        if (!isActive() || data?.code !== 0) {
            return;
        }
        dispatchVoiceBroadcast(prevVoicePayload, data.data);
        prevVoicePayload = data.data;
        message.value = data.data;
        useStore.messageCount = data.data.unreadMsgCount;
    };
    // 使用fetch流式API模拟SSE
    fetch(`${requestUrl.prefix}/msg/adminMsg/msgCount?startTime=${startTime}`, {
        headers: headers,
        signal: controller.signal
    })
        .then((response) => {
            // 检查响应是否成功
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const reader = response.body?.getReader();
            const decoder = new TextDecoder();

            function read() {
                reader
                    ?.read()
                    .then(({ done, value }) => {
                        // 本流已被取代/中止则停止读取
                        if (!isActive()) {
                            return;
                        }
                        if (done) {
                            scheduleReconnect(controller);
                            return;
                        }
                        const text = decoder.decode(value);
                        try {
                            const data = JSON.parse(text);
                            applyData(data);
                        } catch (parseError) {
                            const lines = text.split("\n");
                            lines.forEach((line) => {
                                if (line.startsWith("data:")) {
                                    try {
                                        const jsonData = line.substring(5).trim();
                                        const start = jsonData.indexOf("{");
                                        const end = jsonData.lastIndexOf("}");

                                        if (start !== -1 && end !== -1 && end > start) {
                                            const jsonString = jsonData.substring(start, end + 1);
                                            const data = JSON.parse(jsonString);
                                            applyData(data);
                                        } else {
                                            console.error("未能找到有效的JSON结构");
                                        }
                                    } catch (error) {
                                        console.error("解析SSE消息失败:", error);
                                        // console.log("原始行数据:", line);
                                    }
                                } else if (line.trim() !== "") {
                                    // console.log("收到其他类型的数据:", line);
                                }
                            });
                        }

                        read();
                    })
                    .catch((error) => {
                        // 主动中止(abort)不算异常，不重连
                        if (controller.signal.aborted) {
                            return;
                        }
                        console.error("SSE读取错误:", error);
                        scheduleReconnect(controller);
                    });
            }

            read();
        })
        .catch((error) => {
            if (controller.signal.aborted) {
                return;
            }
            console.error("SSE连接失败:", error);
            scheduleReconnect(controller);
        });
};

/** 关闭SSE连接 */
const closeSSE = () => {
    sseClosed = true;
    if (sseController) {
        sseController.abort();
        sseController = null;
    }
};

/** 标记消息为已读 */
const markAsRead = () => {
    if (message.value) {
        message.value.orderCount = 0;
        message.value.newOrderMsgCount = 0;
        message.value.imMsgCount = 0;
        message.value.unreadMsgCount = 0;
    }
    useStore.messageCount = 0;
    localStorage.setItem("lastOpenTime", String(dayjs().unix()));
    // 重连 SSE 让新的 startTime 立即生效，否则旧连接仍按旧 startTime 推送，
    // 会在 5s 内把"新订单"角标/计数重新推回非 0（表现为"点了已读没变化，刷新才变"）
    initSSE();
};

/** 清除缓存 */
const clearCache = async () => {
    try {
        const result = await cleanUp();
        const configStore = useConfigStore();
        updateUserInfo();
        configStore.updateConfig();
        notification["success"]({
            message: t('缓存已清除'),
            description: t('缓存清除后可刷新页面更新效果')
        });
    } catch (error: any) {}
};

/** 退出登录 */
const onLogout = async () => {
    logout();
};
/** 查看商城跳转地址 */
const viewMallUrl = computed(() => {
    const shopId = Number(localStorage.getItem("shopId")) || 0;
    const shopType = Number(localStorage.getItem("shopType")) || 0;
    if (shopId > 0 && adminType.value === "shop") {
        if (isStore() && shopType === 2) {
            return urlWapFormat(`/pages/store/index?id=${shopId}`);
        } else if (shopType === 1) {
            return urlFormat(`/shop/${shopId}`);
        }
    }
    return isStore() ? urlWapFormat("/") : urlFormat("/");
});

/** 切换组织弹窗显示状态 */
const dialogVisible = ref(false);

/** 打开切换组织弹窗 */
const switchShop = () => {
    dialogVisible.value = true;
};

/** 关闭弹窗 */
const closePopup = () => {
    dialogVisible.value = false;
};

/** 屏幕宽度 */
const screenWidth = ref(window.innerWidth);

/** 处理窗口大小变化 */
const handleResize = () => {
    screenWidth.value = window.innerWidth;
};

/**
 * 解析语音播报 leader 锁的账号隔离键（CR-20260615-173101 Q6）
 *
 * @description 优先取 useUserStore.userInfo.adminId；挂载早期接口未回时回退读 localStorage `user` 缓存中的 adminId；
 *              都无（极端首次登录场景）则返回空字符串，退化为同浏览器全局锁、仍能保证单 tab 播报。
 */
const resolveVoiceLeaderUserKey = (): string => {
    try {
        const fromStore = useStore.userInfo?.adminId;
        if (fromStore) return String(fromStore);
        const raw = localStorage.getItem("user");
        if (raw) {
            const cached = JSON.parse(raw);
            const id = cached?.userInfo?.adminId;
            if (id) return String(id);
        }
    } catch {
        /* localStorage 解析异常，回退到空 key */
    }
    return "";
};

onMounted(() => {
    updateUserInfo();
    // fetchMessage(); // 组件挂载时立即请求一次
    sseClosed = false; // 重置关闭标记，允许（重）连接
    // CR-20260615-173101：启动多 tab leader 选举；
    //   userKey 优先用 userInfo.adminId，挂载早期 store 未更新时回退读 localStorage 缓存；
    //   都无则传空（退化为同浏览器全局锁，仍保证单 tab 播报）
    voiceBroadcastLeader.start(resolveVoiceLeaderUserKey());
    initSSE(); // 初始化SSE连接
    // startPolling(); // 开始轮询 - 删除这行
    window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
    // stopPolling(); // 组件卸载时停止轮询 - 删除这行
    closeSSE(); // 组件卸载时关闭SSE连接
    voiceBroadcaster.cancelAll(); // 清空语音播报队列
});

onBeforeUnmount(() => {
    window.removeEventListener("resize", handleResize);
});
</script>

<style scoped lang="less">
.top-bar-right {
    gap: 10px;
    margin-right: 10px;
}

/* 英文：界面文案单词首字母大写，账号名称除外 */
.top-bar-right.locale-en {
    .top-bar-text,
    .admin_msg-order_count {
        text-transform: capitalize;
    }

    .admin-user-name {
        text-transform: none;
    }
}

.dropdown-memu.locale-en {
    .entrance-list span {
        text-transform: capitalize;
    }
}

/* 你的组件样式 */
.top-bar-item {
    position: relative;
    display: flex;
    align-items: center;
}

.default {
    .top-bar-btn {
        display: block;
        height: 40px;
        padding: 0 12px;
        font-size: 14px;
        color: #788d9b;
        text-align: center;
        transition:
            background 0.2s ease 0s,
            color 0.2s ease 0s;
        z-index: 2;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 5px;

        &:hover {
            background: #f7f7f7;
        }
    }
}

.topMenu {
    .top-bar-btn {
        display: block;
        height: 40px;
        padding: 0 10px !important;
        font-size: 14px;
        text-align: center;
        transition:
            background 0.2s ease 0s,
            color 0.2s ease 0s;
        z-index: 2;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 5px;
        position: relative;
        color: var(--tig-menu-text-color);
    }

    .top-bar-btn:hover {
        background: #9c9c9c2e !important;
    }

    .light {
        color: #788d9b;
    }

    .light:hover {
        background: #f7f7f7 !important;
    }
}

.top-bar-item .top-bar-btn .admin-user-photo img {
    width: 26px;
    height: 26px;
    border-radius: 26px;
    margin-right: 10px;
}

.top-bar-item .top-bar-btn i {
    font-size: 18px;
}

.top-bar-item .top-bar-btn i.fs-18 {
    font-size: 18px;
}

.top-bar-item .top-bar-btn .noc_count {
    background: #ff9900;
    padding: 4px 7px;
    border-radius: 6px;
    vertical-align: top;
    margin-left: 4px;
}

.top-bar-item .top-bar-btn .admin_msg-count {
    position: absolute;
    left: 10px;
    top: 1px;
    font-size: 12px;
    color: #fff;
    background-color: #f33;
    line-height: 14px;
    padding: 0 5px;
    border-radius: 8px;
}

.admin_msg-order_count {
    font-size: 10px;
    color: #fff;
    background-color: #ffb53e;
    line-height: 14px;
    padding: 2px 8px;
    border-radius: 14px;
    margin-left: 5px;
}

.dropdown-memu {
    background-color: #ffffff;
    box-shadow:
        0 6px 16px 0 rgba(0, 0, 0, 0.08),
        0 3px 6px -4px rgba(0, 0, 0, 0.12),
        0 9px 28px 8px rgba(0, 0, 0, 0.05);
    font-size: 12px;
    border-radius: 9px;
    overflow: hidden;
    width: 130px !important;

    .entrance-list {
        display: flex;
        flex-direction: column;

        .entrance {
            border-radius: 2px;
            color: #333;
            text-align: left;
            display: flex;
            align-items: center;
            justify-content: flex-start;
            padding: 6px 16px 6px 16px;
            color: #999;
            column-gap: 8px;
            cursor: pointer;
            text-decoration: none;

            &:hover {
                background: #f5f5f5;
            }
        }

        span {
            color: #323233;
            font-weight: 700;
            font-size: 14px;
            text-align: left;
        }

        i {
            font-size: 20px;
            margin-top: 1px;
            flex-shrink: 0;
        }
    }

    .admin_exit {
        border-top: 1px solid #f5f5f5;
        height: 50px;
        display: flex;
        align-items: center;
        justify-content: center;

        a {
            line-height: 40px;
            padding: 0 10px;
            flex: 1;
            text-align: center;
        }
    }
}

@media only screen and (max-width: 1200px) {
    .top-bar-item {
        .top-bar-btn > .top-bar-text {
            display: none;
        }
    }
    .top-bar-right {
        gap: 5px;
    }
}

@media only screen and (max-width: 767px) {
    .top-bar-right {
        gap: 2px !important;
        margin-right: 0 !important;
    }
    .top-bar-item .top-bar-btn {
        padding: 0 5px !important;
    }
}
</style>
<style scoped lang="less">
.dialog-box {
    :deep(.el-dialog__header) {
        display: none;
    }

    :deep(.el-dialog) {
        width: 760px;
    }
}

@media only screen and (max-width: 767px) {
    .dialog-box {
        :deep(.el-dialog) {
            width: 80%;
        }
    }
}
</style>
<style lang="less">
/* 消息中心弹窗：限制内容不撑破白底，避免列表标题/时间溢出到页面表格上 */
.admin-msg-dialog.ant-modal .ant-modal-content {
    overflow: hidden;
    width: 100%;
}
.admin-msg-dialog.ant-modal .ant-modal-body {
    overflow: hidden;
}
</style>
