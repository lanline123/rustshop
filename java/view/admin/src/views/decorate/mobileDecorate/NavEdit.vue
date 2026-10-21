<template>
    <div class="nav-edit-wrap">
        <div class="dec-edit-header">
            <div class="dec-edit-header-title">{{ navTitle }}</div>
            <el-button @click="emit('back')">{{ $t('返回列表') }}</el-button>
        </div>

        <a-alert
            v-if="showTip"
            class="nav-edit-tip"
            type="info"
            show-icon
            closable
            @close="showTip = false"
            :message="$t('店铺每个页面可以通过菜单单独绑定。便捷的导航方便买家在栏目间快速切换，引导买家前往你期望的页面。')"
        />

        <a-spin :spinning="editLoading">
            <div class="flex box-wrapper">
                <!-- ==================== 左侧手机预览 ==================== -->
                <div class="decorateWrap">
                    <div class="decorate-page-window nav-preview-window">
                        <div class="theme-modules-warp">
                            <div class="list-item modules-item modules-item-topbar" draggable="false">
                                <div class="module-ad-con module-topbar-warp">
                                    <div class="module-topbar-con">
                                        <div class="module-topbar-title-con"><div class="title-item">{{ $t('页面标题') }}</div></div>
                                    </div>
                                </div>
                            </div>
                            <div class="preview-body"></div>
                            <div class="list-item modules-item modules-item-bottom_nav module-item-active">
                                <div class="module-ad-con">
                                    <div class="bottom-nav-con" :style="{ background: previewBg, borderRadius: previewRadius }">
                                        <div class="bottom-nav-item" v-for="(item, index) in module.navList" :key="index">
                                            <div class="item-content">
                                                <a class="item-img-a">
                                                    <img class="item-img" :src="imageFormat(previewIcon(item, index))" v-if="previewIcon(item, index)" />
                                                    <span class="item-img-empty" v-else></span>
                                                </a>
                                                <a class="item-text-a">
                                                    <span class="item-text" :style="{ color: previewTextColor(index) }">{{ item.picTitle }}</span>
                                                </a>
                                            </div>
                                        </div>
                                        <div class="bottom-nav-item bottom-nav-add" v-if="module.navList.length < 5">
                                            <el-icon><Plus /></el-icon>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ==================== 右侧设置区 ==================== -->
                <div class="right-box-warp">
                    <!-- ---------- 基础设置 ---------- -->
                    <div class="dec-spread-title"><div class="title">{{ $t('基础设置') }}</div></div>

                    <div class="base-form">
                        <div class="base-row">
                            <div class="base-label">{{ $t('导航名称') }}<span class="req">*</span></div>
                            <div class="base-control">
                                <el-input v-model="module.navName" maxlength="15" show-word-limit :placeholder="$t('请输入导航名称')" style="width: 320px" />
                            </div>
                        </div>

                        <div class="base-row">
                            <div class="base-label">{{ $t('导航背景色') }}<span class="req">*</span></div>
                            <div class="base-control">
                                <el-radio-group v-model="module.backgroundColorType">
                                    <el-radio :value="1">{{ $t('系统色') }}</el-radio>
                                    <el-radio :value="2">{{ $t('自定义颜色') }}</el-radio>
                                </el-radio-group>
                                <div class="custom-color-row" v-if="module.backgroundColorType === 2">
                                    <span class="label">{{ $t('自定义') }}</span>
                                    <el-input v-model="module.backgroundColor" placeholder="#FFFFFF" style="width: 120px; margin-right: 8px;" />
                                    <SelectColor v-model:color="module.backgroundColor" />
                                </div>
                            </div>
                        </div>

                        <div class="base-row">
                            <div class="base-label">{{ $t('背景圆角') }}<span class="req">*</span></div>
                            <div class="base-control">
                                <el-radio-group v-model="module.cornerStyle">
                                    <el-radio :value="1">{{ $t('直角') }}</el-radio>
                                    <el-radio :value="2">{{ $t('圆角') }}</el-radio>
                                    <el-radio :value="3">{{ $t('大圆角') }}</el-radio>
                                </el-radio-group>
                            </div>
                        </div>

                        <div class="base-row">
                            <div class="base-label">{{ $t('文字配色') }}<span class="req">*</span></div>
                            <div class="base-control">
                                <el-radio-group v-model="module.iconColorType">
                                    <el-radio :value="1">{{ $t('系统配色') }}</el-radio>
                                    <el-radio :value="2">{{ $t('自定义配置') }}</el-radio>
                                </el-radio-group>
                                <div class="icon-color-grid" v-if="module.iconColorType === 2">
                                    <div class="icon-color-col">
                                        <div class="col-title">{{ $t('选中') }}</div>
                                        <div class="color-line">
                                            <span class="label">{{ $t('文本色') }}</span>
                                            <el-input v-model="module.selectedTextColor" placeholder="#FF1616" style="width: 100px; margin-right: 8px;" />
                                            <SelectColor v-model:color="module.selectedTextColor" />
                                        </div>
                                    </div>
                                    <div class="icon-color-col">
                                        <div class="col-title">{{ $t('未选') }}</div>
                                        <div class="color-line">
                                            <span class="label">{{ $t('文本色') }}</span>
                                            <el-input v-model="module.unselectedTextColor" placeholder="#757575" style="width: 100px; margin-right: 8px;" />
                                            <SelectColor v-model:color="module.unselectedTextColor" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- ---------- 菜单设置 ---------- -->
                    <div class="dec-spread-title"><div class="title">{{ $t('菜单设置') }}</div></div>

                    <div class="base-row">
                        <div class="base-label">{{ $t('菜单配置') }}<span class="req">*</span></div>
                        <div class="base-control">
                            <draggable
                                class="menu-list"
                                :list="module.navList"
                                item-key="_id"
                                handle=".menu-drag"
                                ghost-class="ghost"
                                animation="300"
                                :move="onMenuMove"
                            >
                                <template #item="{ element, index }">
                                    <div class="menu-card">
                                        <div class="menu-card-head">
                                            <span v-if="!isFixedMenu(element)" class="menu-drag" :title="$t('拖拽排序')">
                                                <svg viewBox="0 0 16 16" width="16" height="16" aria-hidden="true">
                                                    <circle cx="5" cy="3" r="1.3" />
                                                    <circle cx="11" cy="3" r="1.3" />
                                                    <circle cx="5" cy="8" r="1.3" />
                                                    <circle cx="11" cy="8" r="1.3" />
                                                    <circle cx="5" cy="13" r="1.3" />
                                                    <circle cx="11" cy="13" r="1.3" />
                                                </svg>
                                            </span>
                                            <span v-else class="menu-drag-placeholder"></span>
                                            <span class="menu-card-title">{{ $t('菜单{index1}', { index1: index + 1 }) }}</span>
                                            <span v-if="!isFixedMenu(element)" class="menu-del iconfont-h5 icon-shanchu1" @click="onRemoveMenu(index)"></span>
                                        </div>
                                        <div class="menu-card-body">
                                            <div class="menu-row">
                                                <span class="row-label">{{ $t('菜单名称') }}<span class="req">*</span></span>
                                                <div class="row-control">
                                                    <el-input v-model="element.picTitle" maxlength="4" show-word-limit :placeholder="$t('请输入')" style="width: 240px" />
                                                </div>
                                            </div>
                                            <div class="menu-row menu-row-top">
                                                <span class="row-label">{{ $t('菜单图标') }}<span class="req">*</span></span>
                                                <div class="row-control">
                                                    <div class="icon-upload-group">
                                                        <div class="icon-upload-box">
                                                            <DialogForm type="gallery" @okCallback="onPickIcon" :params="{ isMultiple: false }" :data="{ index, kind: 'normal' }">
                                                                <div class="icon-box">
                                                                    <img v-if="element.picThumb || element.picUrl" :src="productImage(element)" />
                                                                    <el-icon v-else class="icon-box-add"><Plus /></el-icon>
                                                                </div>
                                                            </DialogForm>
                                                            <div class="icon-box-text">{{ $t('未选中') }}</div>
                                                        </div>
                                                        <div class="icon-upload-box">
                                                            <DialogForm type="gallery" @okCallback="onPickIcon" :params="{ isMultiple: false }" :data="{ index, kind: 'active' }">
                                                                <div class="icon-box">
                                                                    <img v-if="element.picActiveThumb || element.picActiveUrl" :src="imageFormat(element.picActiveThumb || element.picActiveUrl)" />
                                                                    <el-icon v-else class="icon-box-add"><Plus /></el-icon>
                                                                </div>
                                                            </DialogForm>
                                                            <div class="icon-box-text">{{ $t('选中') }}</div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="menu-row">
                                                <span class="row-label">{{ $t('跳转链接') }}<span class="req">*</span></span>
                                                <div class="row-control link-control">
                                                    <span class="link-name" v-if="element.picLink?.label">{{ element.picLink.label }}</span>
                                                    <span class="link-name empty" v-else>{{ $t('暂无链接') }}</span>
                                                    <DialogForm
                                                        v-if="!isFixedMenu(element)"
                                                        isDrawer
                                                        path="link/LinkSelect"
                                                        :title="$t('选择链接')"
                                                        width="700px"
                                                        :params="{ act: 'edit', id: 1, type: 'mobile', currentValue: element.picLink || {}, excludeShopId: true }"
                                                        @okCallback="(link: any) => element.picLink = link"
                                                    >
                                                        <a class="link-btn-text">{{ $t('修改') }}</a>
                                                    </DialogForm>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </draggable>

                            <div class="menu-add-row">
                                <a class="menu-add-btn" :class="{ disabled: module.navList.length >= 5 }" @click="onAddMenu">
                                    <el-icon><Plus /></el-icon>{{ $t('添加菜单（最多5个）') }} </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </a-spin>

        <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
            <div class="selected-action">
                <el-button :loading="publishLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('保存并发布') }}</el-button>
            </div>
        </div>

        <!-- 新建模式：进入后自动弹出导航类型模板市场，选模板填充菜单或新建空白 -->
        <DialogForm
            v-if="isAddMode"
            ref="tplDialogRef"
            :showHeader="false"
            :showFooter="false"
            width="1192px"
            :bodyStyle="{ padding: 0 }"
            dialogClass="template-library-dialog"
            path="decorate/mobileDecorate/TemplateLibrary"
            :params="{ decorateType: 1, pageType: 7, scene: 'editor' }"
            @okCallback="onTemplatePicked"
        >
            <span style="display: none"></span>
        </DialogForm>
    </div>
</template>
<script lang="ts" setup>
import "@/views/decorate/decorate/src/css/decorate.less";
import "@/style/css/list.less";
import { ref, computed, onMounted, nextTick } from "vue";
import { message } from "ant-design-vue";
import { Plus } from "@element-plus/icons-vue";
import draggable from "vuedraggable";
import { DialogForm } from "@/components/dialog";
import { SelectColor, SelectLink } from "@/components/select";
import { imageFormat, pickPic, productImage } from "@/utils/format";
import { getDecorate, updateDecorate, updateDecorateFiled } from "@/api/decorate/mobileDecorate";
import { useThemeStore } from "@/store/theme";
import type { NavMenuItem, NavModuleData } from "@/types/decorate/mobileDecorate.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();

const props = defineProps({
    id: { type: Number, required: true },
    title: { type: String, default: "" },
    // true=新增导航：进入后自动弹模板市场，首次保存时再创建记录
    isAdd: { type: Boolean, default: false }
});
const emit = defineEmits(["back"]);

// ===================== 默认数据 =====================
const defaultNavList: NavMenuItem[] = [
    {
        picTitle: t('首页'),
        picThumb: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/02/10/38ef58526a88462db5c6cbc3bf31d274.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/02/10/38ef58526a88462db5c6cbc3bf31d274.png",
        picActiveThumb: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/02/10/822846437e34451ab1b57e2c991bc666.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picActiveUrl: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/02/10/822846437e34451ab1b57e2c991bc666.png",
        picLink: { path: "base", label: t('商城首页'), name: t('商城首页'), link: "/" },
        picId: "2021034691685007360",
        picName: "1680247587QG2y44h7a7f0M1dx9T!!pic200x200.png"
    },
    {
        picTitle: t('我的'),
        picThumb: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/02/10/ecc4581c9a8f4a0c865af4261fae8053.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/02/10/ecc4581c9a8f4a0c865af4261fae8053.png",
        picActiveThumb: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/02/10/609f0ce77fb84f8fbd483e7bf3668239.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picActiveUrl: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/02/10/609f0ce77fb84f8fbd483e7bf3668239.png",
        picLink: { path: "base", label: t('会员中心'), name: t('会员中心'), link: "member" },
        picId: "2021034691664035840",
        picName: "1680247679vqWSO1Ci9sAGJCzzZa!!pic200x200.png"
    }
];

// 为每个菜单项分配前端稳定 key（拖拽排序用，保存时剥离）
let uidSeed = 0;
const withIds = (list: NavMenuItem[]): NavMenuItem[] => (list || []).map((item) => ({ ...item, _id: item._id ?? ++uidSeed }));

const createDefaultModule = (): NavModuleData => ({
    navName: "",
    backgroundColorType: 1,
    backgroundColor: "#FFFFFF",
    cornerStyle: 1,
    iconColorType: 1,
    selectedTextColor: "#FF1616",
    unselectedTextColor: "#757575",
    navList: withIds(JSON.parse(JSON.stringify(defaultNavList)))
});

// ===================== 状态 =====================
const showTip = ref(true);
const editLoading = ref(false);
const publishLoading = ref(false);
const navTitle = ref<string>(props.title || t('编辑导航'));
const module = ref<NavModuleData>(createDefaultModule());
// 当前导航ID：新建模式初始为 0，首次保存创建后回填
const currentId = ref<number>(props.id);
// 新建模式标识：进入后自动弹模板市场
const isAddMode = ref<boolean>(props.isAdd);
// 模板市场弹窗引用
const tplDialogRef = ref();

// ===================== 预览计算 =====================
// 预览中默认把第 1 项作为选中态展示
const activeIndex = 0;
const previewBg = computed(() => (module.value.backgroundColorType === 2 ? module.value.backgroundColor || "#FFFFFF" : "#FFFFFF"));
const previewRadius = computed(() => {
    switch (module.value.cornerStyle) {
        case 2:
            return "8px 8px 0 0";
        case 3:
            return "16px 16px 0 0";
        default:
            return "0";
    }
});
const previewIcon = (item: NavMenuItem, index: number) => {
    if (index === activeIndex) {
        return item.picActiveUrl || item.picActiveThumb || pickPic(item);
    }
    return pickPic(item);
};
const previewTextColor = (index: number) => {
    const custom = module.value.iconColorType === 2;
    if (index === activeIndex) {
        return custom ? module.value.selectedTextColor || "#FF1616" : "#FF1616";
    }
    return custom ? module.value.unselectedTextColor || "#757575" : "#757575";
};

// ===================== 详情加载 =====================
const fetchNavDetail = async () => {
    editLoading.value = true;
    try {
        const result: any = await getDecorate("detail", { id: props.id, decorateType: 1, parentId: 0, localeId: 0 });
        navTitle.value = result?.decorateTitle || navTitle.value;
        const navData = result?.data || {};
        const base = createDefaultModule();
        if (navData.navList && navData.navList.length > 0) {
            // 老数据缺少新字段时用默认值补齐
            module.value = {
                ...base,
                ...navData,
                navName: navData.navName ?? result?.decorateTitle ?? "",
                navList: withIds(navData.navList)
            };
        } else {
            module.value = { ...base, navName: result?.decorateTitle ?? "" };
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        editLoading.value = false;
    }
};

// ===================== 固定菜单（首页 / 我的）=====================
// 首页、我的为系统固定菜单：不可删除、链接不可修改，仅允许改名称和图标。
// 以跳转链接的 link 值作为识别依据（名称可被改，不能作为判断依据）
const HOME_MENU_LINK = "/";
const MEMBER_MENU_LINK = "member";
const FIXED_MENU_LINKS = [HOME_MENU_LINK, MEMBER_MENU_LINK];
const getMenuLink = (item: NavMenuItem): string => {
    const link = item?.picLink;
    if (link && typeof link === "object") return (link as any).link || "";
    return typeof link === "string" ? link : "";
};
const isFixedMenu = (item: NavMenuItem): boolean => FIXED_MENU_LINKS.includes(getMenuLink(item));

// 拖拽守卫：固定菜单自身不可被拖动，其它菜单也不能挤占固定菜单的位置
const onMenuMove = (evt: any): boolean => {
    const dragged = evt?.draggedContext?.element;
    const related = evt?.relatedContext?.element;
    if (dragged && isFixedMenu(dragged)) return false;
    if (related && isFixedMenu(related)) return false;
    return true;
};

// ===================== 菜单操作 =====================
const onPickIcon = (result: any, data: any) => {
    const item = module.value.navList[data.index];
    if (!item || !result || !result[0]) return;
    if (data.kind === "active") {
        item.picActiveThumb = result[0].picThumb;
        item.picActiveUrl = result[0].picUrl;
    } else {
        item.picThumb = result[0].picThumb;
        item.picUrl = result[0].picUrl;
        if (result[0].picId) item.picId = result[0].picId;
        if (result[0].picName) item.picName = result[0].picName;
    }
};
const onAddMenu = () => {
    if (module.value.navList.length >= 5) {
        message.warning(t('菜单最多添加5个'));
        return;
    }
    const newItem: NavMenuItem = {
        _id: ++uidSeed,
        picTitle: "",
        picThumb: "",
        picUrl: "",
        picActiveThumb: "",
        picActiveUrl: "",
        picLink: ""
    };
    // 新增菜单插入到“我的/个人中心”之前：首页始终在第一个，我的始终在最后一个
    const list = module.value.navList;
    const memberIndex = list.findIndex((item) => getMenuLink(item) === MEMBER_MENU_LINK);
    const insertIndex = memberIndex >= 0 ? memberIndex : list.length;
    list.splice(insertIndex, 0, newItem);
};
const onRemoveMenu = (index: number) => {
    const item = module.value.navList[index];
    if (item && isFixedMenu(item)) {
        message.warning(t('首页、我的为固定菜单，不可删除'));
        return;
    }
    if (module.value.navList.length <= 1) {
        message.warning(t('至少保留1个菜单'));
        return;
    }
    module.value.navList.splice(index, 1);
};

// ===================== 校验 + 保存 =====================
const validate = (): boolean => {
    if (!module.value.navName || module.value.navName.trim() === "") {
        message.error(t('请填写导航名称'));
        return false;
    }
    const navList = module.value.navList;
    if (!navList || navList.length === 0) {
        message.error(t('请至少添加一个菜单'));
        return false;
    }
    if (navList.length > 5) {
        message.error(t('菜单最多不能超过5个'));
        return false;
    }
    for (let i = 0; i < navList.length; i++) {
        const item = navList[i];
        if (!item || !(item.picUrl || item.picThumb)) {
            message.error(t('第{i1}个菜单缺少未选中图标', { i1: i + 1 }));
            return false;
        }
        if (!(item.picActiveUrl || item.picActiveThumb)) {
            message.error(t('第{i1}个菜单缺少选中图标', { i1: i + 1 }));
            return false;
        }
        if (!item.picTitle || item.picTitle.trim() === "") {
            message.error(t('第{i1}个菜单缺少名称', { i1: i + 1 }));
            return false;
        }
    }
    if (module.value.backgroundColorType === 2 && !module.value.backgroundColor) {
        message.error(t('请设置自定义背景色'));
        return false;
    }
    return true;
};
const onSubmit = async () => {
    if (!validate()) return;
    try {
        publishLoading.value = true;
        // 剥离前端排序用的 _id，保持保存数据干净
        const payload: NavModuleData = {
            ...module.value,
            navList: module.value.navList.map(({ _id, ...rest }) => rest)
        };
        // 新建模式：尚无记录，先创建导航（pageType=7）拿到ID，再发布
        if (!currentId.value) {
            const res: any = await updateDecorate("create", { decorateType: 1, pageType: 7, decorateTitle: module.value.navName });
            currentId.value = res?.decorateId ?? res?.id ?? 0;
            if (!currentId.value) {
                message.error(t('创建失败'));
                return;
            }
            isAddMode.value = false;
        }
        await updateDecorate("publish", { id: currentId.value, data: payload, parentId: 0, localeId: 0 });
        // 同步导航名称到列表标题
        try {
            await updateDecorateFiled({ id: currentId.value, field: "decorateTitle", val: module.value.navName });
        } catch (e) {
            // 标题同步失败不阻断主流程
        }
        message.success(t('保存并发布成功'));
        emit("back");
    } catch (error: any) {
        message.error(error.message);
    } finally {
        publishLoading.value = false;
    }
};

/** 模板市场选择回调：使用模板则用其菜单数据填充，新建空白则保持默认导航 */
const onTemplatePicked = (result: any) => {
    if (result?.useTemplate && result.template?.json) {
        const json = result.template.json || {};
        const tplNavList = Array.isArray(json.navList) ? json.navList : [];
        const base = createDefaultModule();
        module.value = {
            ...base,
            ...json,
            navName: module.value.navName || json.navName || "",
            navList: tplNavList.length ? withIds(tplNavList) : base.navList
        };
    }
    // result.blank：保持默认导航（createDefaultModule 已初始化），无需处理
};

onMounted(() => {
    if (isAddMode.value) {
        // 新建模式：不拉详情，进入后自动弹出模板市场
        nextTick(() => tplDialogRef.value?.show?.());
    } else {
        fetchNavDetail();
    }
});
</script>
<style lang="less" scoped>
.nav-edit-wrap {
    padding-bottom: 80px; // 留出底部悬浮栏的空间，避免被遮挡
}

.dec-edit-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: #fff;
    border-bottom: 1px solid #f0f2f5;

    .dec-edit-header-title {
        font-size: 16px;
        font-weight: bold;
        color: #333;
    }
}

.nav-edit-tip {
    margin: 12px 16px 0;
    width: auto;
}

.box-wrapper {
    padding: 16px;
    align-items: flex-start;
    justify-content: center;
}

.req {
    color: #f56c6c;
    margin-right: 2px;
}

// ---------- 左侧预览 ----------
.decorateWrap {
    position: sticky;
    top: 16px;
    align-self: flex-start; // 必须配合 flex 父容器的 align-items 才能正常 sticky
    z-index: 1;
}
.nav-preview-window {
    width: 375px;
    min-height: 640px;
    background: #f5f5f5;
    position: relative;
    margin-right: 24px;
    border: 1px solid #eee;

    .preview-body {
        height: 480px;
    }
}
.modules-item-bottom_nav {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
}
.bottom-nav-con {
    display: flex;
    flex-wrap: nowrap;
    justify-content: center;
    text-align: center;
    height: 54px;
    padding: 5px 0;
    box-sizing: border-box;
}
.bottom-nav-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
}
.bottom-nav-item .item-content .item-img-a {
    display: block;
}
.bottom-nav-item .item-content .item-img-a img,
.bottom-nav-item .item-content .item-img-a .item-img-empty {
    width: 24px;
    height: 24px;
    display: inline-block;
}
.bottom-nav-item .item-content .item-text-a {
    margin-top: 2px;
    display: block;
    font-size: 11px;
}
.bottom-nav-add .el-icon {
    color: #bbb;
    font-size: 18px;
}

// ---------- 右侧设置 ----------
.right-box-warp {
    flex: 1;
    min-width: 520px;
    max-width: 680px;
    // 让右侧面板始终至少和预览区一样高（含 sticky 安全距离），
    // 这样 .box-wrapper 永远有足够空间，左侧 sticky 不会被父容器底边推动上移
    min-height: calc(100vh - 100px);
}
.dec-spread-title {
    font-size: 15px;
    font-weight: bold;
    color: #333;
    padding: 10px 0;
    margin-bottom: 6px;
    border-bottom: 1px solid #f0f2f5;
}
// ---------- 基础设置：左右结构 ----------
.base-form {
    padding: 8px 0;
}
.base-row {
    display: flex;
    align-items: flex-start;
    padding: 12px 0;

    .base-label {
        width: 96px;
        flex-shrink: 0;
        line-height: 32px;
        color: #333;
        font-size: 13px;
        text-align: left;
    }
    .base-control {
        flex: 1;
        min-height: 32px;
        display: flex;
        flex-direction: column;
        justify-content: center;

        :deep(.el-radio-group) {
            line-height: 32px;
        }
    }
}

.custom-color-row {
    margin-top: 12px;
    display: flex;
    align-items: center;
    background: #f5f7fa;
    border-radius: 6px;
    padding: 10px 14px;
    width: fit-content;

    .label {
        color: #666;
        margin-right: 12px;
    }
}

.icon-color-grid {
    margin-top: 12px;
    display: flex;
    gap: 16px;

    .icon-color-col {
        background: #f8f9fa;
        border: 1px solid #e9ecef;
        border-radius: 6px;
        padding: 12px;
        flex: 1;

        .col-title {
            font-size: 12px;
            font-weight: bold;
            color: #495057;
            margin-bottom: 8px;
        }
        .color-line {
            display: flex;
            align-items: center;

            .label {
                width: 48px;
                font-size: 12px;
                color: #6c757d;
            }
        }
    }
}

// ---------- 菜单卡片 ----------
.menu-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
    margin-top: 8px;
}
.menu-card {
    border-radius: 8px;
    background: #fafafa;
    max-width: 675px;

    .menu-card-head {
        display: flex;
        align-items: center;
        padding: 16px 14px;
        background: #fafafa;
        border-bottom: 1px solid #f0f0f0;
        border-radius: 8px 8px 0 0;

        .menu-drag {
            cursor: move;
            margin-right: 8px;
            display: inline-flex;
            align-items: center;
            padding: 2px;

            svg {
                fill: #c0c4cc;
            }
            &:hover svg {
                fill: #909399;
            }
        }
        .menu-drag-placeholder {
            display: inline-block;
            width: 20px;
            margin-right: 8px;
        }
        .menu-card-title {
            font-weight: bold;
            color: #333;
            flex: 1;
        }
        .menu-del {
            cursor: pointer;
            color: #999;

            &:hover {
                color: #f56c6c;
            }
        }
    }
    .menu-card-body {
        padding: 14px;
    }
}
.menu-row {
    display: flex;
    align-items: center;
    margin-bottom: 16px;

    &:last-child {
        margin-bottom: 0;
    }
    &.menu-row-top {
        align-items: flex-start;
    }
    .row-label {
        width: 80px;
        color: #333;
        flex-shrink: 0;
        font-size: 13px;
        line-height: 32px;
    }
    .row-control {
        flex: 1;
        display: flex;
        align-items: center;
    }
}
.link-control {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 13px;

    .link-name {
        color: #333;
        &.empty {
            color: #999;
        }
    }
    .link-btn-text {
        color: #155bd4;
        cursor: pointer;
        text-decoration: none;
        &:hover {
            color: #2276e6;
        }
    }
}
.icon-upload-group {
    display: flex;
    gap: 24px;
}
.icon-upload-box {
    text-align: center;

    .icon-box {
        width: 60px;
        height: 60px;
        border: 1px dashed #dcdfe6;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        overflow: hidden;

        &:hover {
            border-color: var(--el-color-primary);
        }
        img {
            width: 100%;
            height: 100%;
            object-fit: contain;
        }
        .icon-box-add {
            color: #c0c4cc;
            font-size: 20px;
        }
    }
    .icon-box-text {
        margin-top: 6px;
        color: #999;
        font-size: 12px;
    }
}
.menu-add-row {
    margin-top: 16px;
    text-align: center;
    max-width: 675px;

    .menu-add-btn {
        width: 100%;
        border: 1px solid #f0f2f5;
        border-radius: 6px;
        cursor: pointer;
        padding: 10px 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        color: var(--el-color-primary);
        cursor: pointer;
        font-size: 14px;
        gap: 4px;

        &:hover {
            color: #2276e6;
        }
        &.disabled {
            color: #c0c4cc;
            cursor: not-allowed;
        }
    }
}
.ghost {
    opacity: 0.5;
}
.selected-action-warp {
    right: 0;
    justify-content: center;

    .selected-action {
        padding-left: 0;
        justify-content: center;
    }
    .form-submit-btn {
        min-width: 320px;
    }
}
</style>
