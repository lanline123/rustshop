<template>
    <div class="left-menu-container">
        <div class="app-layout-m_nav_o1XZi">
            <div class="avg" v-if="getAdminType() === 'admin'">
                <template v-if="extractContent(String(userInfo.avatar)) == 'def'">
                    <img class="image" :src="returnAvatar(userInfo.avatar)" />
                </template>
                <template v-else>
                    <img class="image" :src="imageFormat(userInfo.avatar)" />
                </template>
            </div>
            <div class="avg" v-if="getAdminType() === 'shop'">
                <img class="image" :src="imageFormat(shopInfo.shopLogo)" />
            </div>
            <div class="popover-box">
                <el-popover placement="right-start" :width="100" trigger="click">
                    <template #reference>
                        <div class="status">
                            <StatusDot
                                v-if="ImStore.imPresence == 1"
                                style="margin-top: 2px"
                                :inline="false"
                                :flicker="true"
                                :size="8"
                                color="#3ebd00"
                            ></StatusDot>
                            <StatusDot
                                v-if="ImStore.imPresence == 2"
                                style="margin-top: 2px"
                                :inline="false"
                                :flicker="true"
                                :size="8"
                                color="#f44444"
                            ></StatusDot>
                            <StatusDot
                                v-if="ImStore.imPresence == 3"
                                style="margin-top: 2px"
                                :inline="false"
                                :flicker="true"
                                :size="8"
                                color="#d0d0d0"
                            ></StatusDot>
                            <div class="text" v-if="ImStore.imPresence == 1">{{ $t('在线') }}</div>
                            <div class="text" v-if="ImStore.imPresence == 2">{{ $t('忙碌') }}</div>
                            <div class="text" v-if="ImStore.imPresence == 3">{{ $t('离开') }}</div>
                        </div>
                    </template>
                    <div class="status-pop-box">
                        <div
                            class="status-pop-item flex align-center cursor-pointer"
                            v-for="item in statusList"
                            :key="item.value"
                            @click="onChangeStatus(item.value)"
                        >
                            <div class="status-pop-item-icon" :class="{ opacity: item.value == ImStore.imPresence }">
                                <el-icon size="14" color="#000"><Select /></el-icon>
                            </div>
                            <div class="status-pop-item-content" v-if="item.value == 1">
                                <div class="label green"></div>
                                <div class="txt">{{ $t('在线') }}</div>
                            </div>
                            <div class="status-pop-item-content" v-if="item.value == 2">
                                <div>
                                    <el-icon size="14" color="#ff4444"><RemoveFilled /></el-icon>
                                </div>
                                <div class="txt">{{ $t('忙碌') }}</div>
                            </div>
                            <div class="status-pop-item-content" v-if="item.value == 3">
                                <div class="label"></div>
                                <div class="txt">{{ $t('离线') }}</div>
                            </div>
                        </div>
                    </div>
                </el-popover>
            </div>

            <div
                v-for="item in menuList"
                class="app-layout-m_item_1hZOk"
                :class="{ 'app-layout-m_active_1p89k': isActive(item.path) }"
                @click="toPage(item.path)"
            >
                <div class="app-layout-m_item-icon_1yDNO">
                    <SvgIcon :name="item.svg" width="16" height="16" />
                </div>
                <div class="app-layout-m_item-title_2Je8J">{{ item.title }}</div>
            </div>
        </div>
    </div>
    <div v-if="minMenuList.length > 0 && false" class="right-menu-container">
        <div class="app-layout-m_nav_o1XZi">
            <a
                v-for="item in minMenuList"
                class="app-layout-m_item_1hZOk"
                :class="{ 'app-layout-m_active_1p89k': doesPathEndWith(route.path, item.path) }"
                :href="item.path"
            >
                <div class="app-layout-m_item-title_2Je8J">{{ item.title }}</div>
            </a>
        </div>
    </div>
</template>
<script setup lang="ts">
import { message } from "ant-design-vue";
import { useRoute, useRouter } from "vue-router";
import { ref, watch, computed } from "vue";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { returnAvatar } from "@/utils/avatar";
import { extractContent } from "@/utils/util";
import { urlFormat, imageFormat } from "@/utils/format";
import { useImStore } from "@/store/im";
import { Select, RemoveFilled } from "@element-plus/icons-vue";
import { setModifyStatus } from "@/api/im/conversation";
import { getAdminType } from "@/utils/storage";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const ImStore = useImStore();
const userInfo = computed(() => JSON.parse(localStorage.getItem("user") || "{}").userInfo);
const shopInfo = computed(() => JSON.parse(localStorage.getItem("shopInfo") || "{}"));

const route = useRoute();
const statusList = ref<any[]>([
    { value: 1, label: t('在线') },
    { value: 2, label: t('忙碌') },
    { value: 3, label: t('离开') }
]);

const onChangeStatus = async (value: number) => {
    try {
        const resultStatus = await setModifyStatus({ status: value });
        ImStore.setImPresence(value);
    } catch (error: any) {
        message.error(error.message);
    }
};
const isActive = (path: string) => {
    if (route.path.split("/").length == 4) {
        let lastSlashIndex = route.path.lastIndexOf("/");
        let a = route.path.substring(0, lastSlashIndex);
        return a === path;
    }
    return route.path === path;
};

const menuList: any = ref([
    { path: "/im/index", svg: "im-recently", name: "index", title: "即时会话", children: [] }
]);
const minMenuList: any = ref([]);

const router = useRouter();
const path: any = ref("");
watch(
    () => route,
    (newValue) => {
        if (newValue.path.split("/").length == 3) {
            let a = menuList.value.filter((item: any) => item.path === newValue.path);
            let b = a.length > 0 ? a[0] : {};
            if (b.children?.length > 0) {
                minMenuList.value = b.children;
                path.value = newValue.path;

                router.push({ path: minMenuList.value[0].path });
            }
        } else {
            let str = newValue.path;
            let lastSlashIndex = str.lastIndexOf("/");
            let beforeLastSegment = "";
            let lastSegment = "";
            if (lastSlashIndex !== -1) {
                // 去掉最后一个 '/' 后面的内容
                beforeLastSegment = str.substring(0, lastSlashIndex);

                // 获取最后一个 '/' 后面的内容
                lastSegment = str.substring(lastSlashIndex + 1);
            }
            let a = menuList.value.filter((item: any) => item.path === beforeLastSegment);
            let b = a.length > 0 ? a[0] : {};
            if (b.children?.length > 0) {
                minMenuList.value = b.children;
                path.value = newValue.path;
            }
        }
    },
    { deep: true, immediate: true }
);
const toPage = (path: any) => {
    router.push({
        path
    });
};
function doesPathEndWith(name: string, path: string) {
    return path === name;
}
</script>
<style lang="less" scoped>
.left-menu-container {
    max-width: 232px;
    min-width: 116px;

    display: flex;
    height: 100%;
    justify-content: space-between;
    .app-layout-m_nav_o1XZi {
        width: 100%;
        background-color: #343f4c;
        display: flex;
        flex-direction: column;
        align-items: center;

        .avg {
            display: flex;
            align-items: center;
            justify-content: center;

            .image {
                width: 32px;
                height: 32px;
                margin: 16px auto 8px;
                border-radius: 50%;
            }
        }

        .status {
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 20px;
            height: 18px;

            .text {
                color: #c8c9cc;
                font-size: 12px;
            }
        }

        .app-layout-m_item_1hZOk {
            width: 100%;
            cursor: pointer;
            justify-content: center;
            display: flex;
            align-items: center;
            gap: 8px;
            flex-direction: row;
            height: 64px;
            fill: #c8c9cc;
            color: #c8c9cc;
            font-size: 14px;
            box-sizing: border-box;
        }

        .app-layout-m_item_1hZOk:hover {
            background-color: rgba(0, 0, 0, 0.24);
            color: #fff;
            fill: #fff;
        }

        .app-layout-m_active_1p89k {
            background-color: rgba(0, 0, 0, 0.24);
            color: #fff;
            fill: #fff;
        }
    }
}

.right-menu-container {
    min-width: 116px;
    max-width: 116px;
    display: flex;
    height: 100vh;
    justify-content: space-between;

    .app-layout-m_nav_o1XZi {
        width: 100%;
        background-color: white;
        display: flex;
        flex-direction: column;
        align-items: center;
        border-right: 1px solid #e5e5e5;
        box-sizing: border-box;
        padding-top: 10px;

        .app-layout-m_item_1hZOk {
            width: 99px;
            box-sizing: border-box;
            display: block;
            padding: 0 5px;
            line-height: 30px;
            transition: background-color 0.3s;
            font-size: 14px;
            margin: 0 10px 10px;
            border-radius: 2px;
            position: relative;
            color: #333;

            .app-layout-m_item-title_2Je8J {
                display: flex;
                align-items: center;
                justify-content: center;
            }
        }

        .app-layout-m_item_1hZOk:hover {
            background-color: #f2f2f2;
            color: #333;
        }

        .app-layout-m_active_1p89k {
            background-color: #f2f2f2;
            color: #333;
        }
    }
}
.status-pop-box {
    .status-pop-item {
        padding: 7px 0;
        .status-pop-item-icon {
            margin-right: 10px;
            opacity: 0;
            margin-top: 2px;
        }
        .opacity {
            opacity: 1;
        }
        .status-pop-item-content {
            display: flex;
            align-items: center;
            .label {
                width: 13px;
                height: 13px;
                border-radius: 50%;
                background-color: #d0d0d0;
            }
            .green {
                background-color: #3ebd00;
            }
            .txt {
                margin-left: 10px;
            }
        }
    }
}
// <div class="status-pop-box">
//     <div class="status-pop-item flex align-center" v-for="item in statusList" :key="item.value">
//         <div class="status-pop-item-icon" v-if="item.value == ImStore.imPresence">
//             <el-icon size="14" color="#323233"><Select /></el-icon>
//         </div>
//         <div class="status-pop-item-content" v-if="item.value == 1">
//             <div class="label green"></div>
//             <div class="txt">在线</div>
//         </div>
//         <div class="status-pop-item-content" v-if="item.value == 2">
//             <div>
//                 <el-icon size="14" color="#ff4444"><RemoveFilled /></el-icon>
//             </div>
//             <div class="txt">忙碌</div>
//         </div>
//         <div class="status-pop-item-content" v-if="item.value == 3">
//             <div class="label"></div>
//             <div class="txt">离线</div>
//         </div>
//     </div>
// </div>
</style>
