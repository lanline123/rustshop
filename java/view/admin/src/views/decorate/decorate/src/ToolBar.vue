<template>
    <div class="theme-toolbar">
        <div class="theme-menu-con show">
            <div class="theme-con-box">
                <el-tabs v-model="activeName" class="demo-tabs">
                    <el-tab-pane :label="$t('基础模块')" name="toolList" v-if="showBasicGroup"></el-tab-pane>
                    <el-tab-pane :label="$t('高级模块')" name="advancedList" v-if="showAdvancedGroup && isPro()"></el-tab-pane>
                    <el-tab-pane :label="$t('会员模块')" name="memberList" v-if="showMemberGroup"></el-tab-pane>
                    <el-tab-pane :label="$t('分类中心模块')" name="cateCenterList" v-if="showCateCenterGroup"></el-tab-pane>
                </el-tabs>
                <div class="inside">
                    <el-collapse v-model="activeNames">
                        <el-collapse-item :title="$t('基础模块')" name="tool" v-if="activeName === 'toolList' && showBasicGroup">
                            <template #title>
                                <div class="modules-head J_ModuleListHead">
                                    <span>{{ $t('基础模块') }}</span>
                                </div>
                            </template>
                            <draggable
                                class="module-list"
                                item-key="type"
                                :list="toolList"
                                ghost-class="sortable-ghost"
                                chosen-class="toolChosenClass"
                                animation="300"
                                @start=""
                                @end=""
                                :sort="false"
                                :group="{ name: 'advanced', pull: 'clone', put: false }"
                                :clone="clone"
                            >
                                <template #item="{ element, index }">
                                    <div class="list-item tool-list-item" @click="onAdd(index, 'toolList', element)" v-if="element.isShow">
                                        <span v-if="element.content" class="pic" v-html="element.content"> </span>
                                        <span v-if="element.label" class="content">{{ element.label }}</span>
                                    </div>
                                </template>
                            </draggable>
                        </el-collapse-item>
                        <el-collapse-item :title="$t('其它模块')" name="other" v-if="activeName === 'toolList' && showOtherGroup">
                            <template #title>
                                <div class="modules-head J_ModuleListHead">
                                    <span>{{ $t('其它模块') }}</span>
                                </div>
                            </template>
                            <draggable
                                class="module-list"
                                item-key="type"
                                :list="otherList"
                                ghost-class="sortable-ghost"
                                chosen-class="toolChosenClass"
                                animation="300"
                                @start=""
                                @end=""
                                :sort="false"
                                :group="{ name: 'advanced', pull: 'clone', put: false }"
                                :clone="clone"
                            >
                                <template #item="{ element, index }">
                                    <div class="list-item tool-list-item" @click="onAdd(index, 'otherList')" v-if="element.isShow">
                                        <span v-if="element.content" class="pic" v-html="element.content"> </span>
                                        <span v-if="element.label" class="content">{{ element.label }}</span>
                                    </div>
                                </template>
                            </draggable>
                        </el-collapse-item>
                        <el-collapse-item :title="$t('直播模块')" name="live" v-if="activeName === 'toolList' && showLiveGroup">
                            <template #title>
                                <div class="modules-head J_ModuleListHead">
                                    <span>{{ $t('直播模块') }}</span>
                                </div>
                            </template>
                            <draggable
                                class="module-list"
                                item-key="type"
                                :list="liveList"
                                ghost-class="sortable-ghost"
                                chosen-class="toolChosenClass"
                                animation="300"
                                @start=""
                                @end=""
                                :sort="false"
                                :group="{ name: 'advanced', pull: 'clone', put: false }"
                                :clone="clone"
                            >
                                <template #item="{ element, index }">
                                    <div class="list-item tool-list-item" @click="onAdd(index, 'liveList')" v-if="element.isShow">
                                        <span v-if="element.content" class="pic" v-html="element.content"> </span>
                                        <span v-if="element.label" class="content">{{ element.label }}</span>
                                    </div>
                                </template>
                            </draggable>
                        </el-collapse-item>
                        <el-collapse-item :title="$t('高级模块')" name="advanced" v-if="activeName === 'advancedList' && showAdvancedGroup && isPro() && adminType === 'admin'">
                            <template #title>
                                <div class="modules-head J_ModuleListHead">
                                    <span>{{ $t('高级模块') }}</span>
                                </div>
                            </template>
                            <draggable
                                class="module-list"
                                item-key="type"
                                :list="advancedList"
                                ghost-class="sortable-ghost"
                                chosen-class="toolChosenClass"
                                animation="300"
                                @start=""
                                @end=""
                                :sort="false"
                                :group="{ name: 'advanced', pull: 'clone', put: false }"
                                :clone="clone"
                            >
                                <template #item="{ element, index }">
                                    <div class="list-item tool-list-item" @click="onAdd(index, 'advancedList')" v-if="element.isShow">
                                        <span v-if="element.content" class="pic" v-html="element.content"> </span>
                                        <span v-if="element.label" class="content">{{ element.label }}</span>
                                    </div>
                                </template>
                            </draggable>
                        </el-collapse-item>
                        <el-collapse-item :title="$t('会员模块')" name="member" v-if="activeName === 'memberList' && showMemberGroup">
                            <template #title>
                                <div class="modules-head J_ModuleListHead">
                                    <span>{{ $t('会员模块') }}</span>
                                </div>
                            </template>
                            <draggable
                                class="module-list"
                                item-key="type"
                                :list="memberList"
                                ghost-class="sortable-ghost"
                                chosen-class="toolChosenClass"
                                animation="300"
                                @start=""
                                @end=""
                                :sort="false"
                                :group="{ name: 'advanced', pull: 'clone', put: false }"
                                :clone="clone"
                            >
                                <template #item="{ element, index }">
                                    <div class="list-item tool-list-item" @click="onAdd(index, 'memberList')" v-if="element.isShow">
                                        <span v-if="element.content" class="pic" v-html="element.content"> </span>
                                        <span v-if="element.label" class="content">{{ element.label }}</span>
                                    </div>
                                </template>
                            </draggable>
                        </el-collapse-item>
                        <el-collapse-item :title="$t('分类中心模块')" name="cateCenter" v-if="activeName === 'cateCenterList' && showCateCenterGroup">
                            <template #title>
                                <div class="modules-head J_ModuleListHead">
                                    <span>{{ $t('分类中心模块') }}</span>
                                </div>
                            </template>
                            <draggable
                                class="module-list"
                                item-key="type"
                                :list="cateCenterList"
                                ghost-class="sortable-ghost"
                                chosen-class="toolChosenClass"
                                animation="300"
                                @start=""
                                @end=""
                                :sort="false"
                                :group="{ name: 'advanced', pull: 'clone', put: false }"
                                :clone="clone"
                            >
                                <template #item="{ element, index }">
                                    <div class="list-item tool-list-item" @click="onAdd(index, 'cateCenterList')" v-if="element.isShow">
                                        <span v-if="element.content" class="pic" v-html="element.content"> </span>
                                        <span v-if="element.label" class="content">{{ element.label }}</span>
                                    </div>
                                </template>
                            </draggable>
                        </el-collapse-item>
                    </el-collapse>
                    <div style="height: 50px"></div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, onMounted } from "vue";
import draggable from "vuedraggable";
import { cloneDeep } from "lodash";
import { isPro, isOverseas, isStore } from "@/utils/version";
import { message } from "ant-design-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const EXCLUSIVE_TYPES = new Set(["cateCenterV1", "cateCenterV2"]);
const adminType = localStorage.getItem("adminType");
const activeNames = ref(["tool", "other", "live", "advanced", "member", "cateCenter", "login"]);
const props = defineProps({
    modules: {
        type: Object,
        default: () => ({})
    },
    pageType: {
        type: Number,
        default: 1
    }
});

const PAGE_ALLOWED_GROUPS: Record<number, string[]> = {
    1: ["basic", "other", "live", "advanced"],
    2: ["member"],
    3: ["basic", "other", "live"],
    4: ["cateCenter"]
};

const allowedGroups = computed(() => PAGE_ALLOWED_GROUPS[props.pageType] ?? ["basic", "other"]);
const showBasicGroup = computed(() => allowedGroups.value.includes("basic"));
const showOtherGroup = computed(() => allowedGroups.value.includes("other"));
const showLiveGroup = computed(() => allowedGroups.value.includes("live"));
const showAdvancedGroup = computed(() => allowedGroups.value.includes("advanced"));
const showMemberGroup = computed(() => allowedGroups.value.includes("member"));
const showCateCenterGroup = computed(() => allowedGroups.value.includes("cateCenter"));
const activeName = ref(
    props.pageType === 2 ? "memberList" : props.pageType === 4 ? "cateCenterList" : "toolList"
);

const modules = ref(props.modules);
const toolList = ref([
    // { type: "article", url: "mobile", label: "文章列表", content: '<i class="iconfont-admin icon-article"></i>', module: {}, isShow: true },
    { type: "imageAd", url: "mobile", label: t('图片广告1'), content: '<i class="iconfont-admin icon-image_nav"></i>', module: {}, isShow: true },
    { type: "imageSquareAd", url: "mobile", label: t('图片魔方'), content: '<i class="iconfont-admin icon-image_square_ad"></i>', module: {}, isShow: true },
    { type: "imageHotarea", url: "mobile", label: t('热图绘制'), content: '<i class="iconfont-admin icon-image_hotarea"></i>', module: {}, isShow: true },
    { type: "product", url: "mobile", label: t('商品'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    { type: "whiteLine", url: "mobile", label: t('分割线'), content: '<i class="iconfont-admin icon-white_line"></i>', module: {}, isShow: true },
    { type: "whiteBlank", url: "mobile", label: t('空白辅助'), content: '<i class="iconfont-admin icon-white_blank"></i>', module: {}, isShow: true },
    { type: "imageNav", url: "mobile", label: t('图文导航'), content: '<i class="iconfont-admin icon-image_nav"></i>', module: {}, isShow: true },
    { type: "titleAd", url: "mobile", label: t('文本标题'), content: '<i class="iconfont-admin icon-title_ad"></i>', module: {}, isShow: true },
    { type: "notice", url: "mobile", label: t('公告'), content: '<i class="iconfont-admin icon-notice"></i>', module: {}, isShow: true }
]);
const advancedList = ref([
    // { type: "customProduct", url: "mobilePro", label: "个性商品", content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    {
        type: "imageTextComponent",
        url: "mobilePro",
        label: t('图文介绍'),
        content: '<i class="iconfont-admin icon-image_text_component"></i>',
        module: {},
        isShow: true
    },
    {
        type: "imageShowcase",
        url: "mobilePro",
        label: t('图片橱窗'),
        content: '<i class="iconfont-admin icon-image_nav"></i>',
        module: {},
        isShow: true
    },
    { type: "productGrouping", url: "mobilePro", label: t('商品分组'), content: '<i class="iconfont-admin icon-product_grouping"></i>', module: {}, isShow: true },
    { type: "groupSliding", url: "mobilePro", label: t('分组滑动'), content: '<i class="iconfont-admin icon-group_sliding"></i>', module: {}, isShow: true },
    { type: "countdown", url: "mobilePro", label: t('倒计时'), content: '<i class="iconfont-admin icon-countdown"></i>', module: {}, isShow: true },
    { type: "categoryRec", url: "mobilePro", label: t('分类推荐'), content: '<i class="iconfont-admin icon-category_rec"></i>', module: {}, isShow: true },
    { type: "categoryRecA1", url: "mobilePro", label: t('分类推荐A1'), content: '<i class="iconfont-admin icon-category_rec"></i>', module: {}, isShow: true },
    { type: "productA1", url: "mobilePro", label: t('商品A1'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    {
        type: "mixedProductDisplay",
        url: "mobilePro",
        label: t('商品混排'),
        content: '<i class="iconfont-admin icon-mixed_product_display"></i>',
        module: {},
        isShow: true
    },
    { type: "specialOffer", url: "mobilePro", label: t('天天特价'), content: '<i class="iconfont-admin icon-special_offer"></i>', module: {}, isShow: true },
    { type: "noticeA2", url: "mobilePro", label: t('公告'), content: '<i class="iconfont-admin icon-notice"></i>', module: {}, isShow: true },
    { type: "mixedNotice", url: "mobilePro", label: t('公告混排'), content: '<i class="iconfont-admin icon-notice"></i>', module: {}, isShow: true },
    {
        type: "comboSuggestions",
        url: "mobilePro",
        label: t('组合推荐'),
        content: '<i class="iconfont-admin icon-combo_suggestions"></i>',
        module: {},
        isShow: true
    },
    {
        type: "slideLargeImage",
        url: "mobilePro",
        label: t('大图滑动'),
        content: '<i class="iconfont-admin icon-slide_large_image"></i>',
        module: {},
        isShow: true
    },
    { type: "slidingImage", url: "mobilePro", label: t('小图滑动'), content: '<i class="iconfont-admin icon-sliding_image"></i>', module: {}, isShow: true },
    { type: "banner", url: "mobilePro", label: t('首焦轮播'), content: '<i class="iconfont-admin icon-banner"></i>', module: {}, isShow: true },
    {
        type: "bannerA2",
        url: "mobilePro",
        label: t('首焦轮播A2'),
        content: '<i class="iconfont-admin icon-banner"></i>',
        module: {},
        isShow: isStore() ? true : false
    },
    { type: "categoryNav", url: "mobilePro", label: t('类目导航'), content: '<i class="iconfont-admin icon-category_nav"></i>', module: {}, isShow: true },
    { type: "categoryNavA1", url: "mobilePro", label: t('类目导航A1'), content: '<i class="iconfont-admin icon-category_nav"></i>', module: {}, isShow: true },
    { type: "categoryNavA2", url: "mobilePro", label: t('类目导航A2'), content: '<i class="iconfont-admin icon-category_nav"></i>', module: {}, isShow: true },
    { type: "categoryNavA3", url: "mobilePro", label: t('类目导航A3'), content: '<i class="iconfont-admin icon-category_nav"></i>', module: {}, isShow: true },
    { type: "tagNav", url: "mobilePro", label: t('标签导航'), content: '<i class="iconfont-admin icon-category_nav"></i>', module: {}, isShow: true },
    {
        type: "shopTips",
        url: "mobilePro",
        label: t('商品提示'),
        content: '<i class="iconfont-admin icon-notice"></i>',
        module: {},
        isShow: true
    },
    {
        type: "noticeA3",
        url: "mobilePro",
        label: t('商城公告'),
        content: '<i class="iconfont-admin icon-notice"></i>',
        module: {},
        isShow: true
    },
    { type: "newcomerCombo", url: "mobilePro", label: t('新人二重礼'), content: '<i class="iconfont-admin icon-coupon"></i>', module: {}, isShow: true },
    { type: "limitedSpecial", url: "mobilePro", label: t('限时特惠'), content: '<i class="iconfont-admin icon-seckill"></i>', module: {}, isShow: true },
    { type: "productSlide", url: "mobilePro", label: t('商品滑动'), content: '<i class="iconfont-admin icon-group_sliding"></i>', module: {}, isShow: true },
    { type: "productCarousel", url: "mobilePro", label: t('商品轮播'), content: '<i class="iconfont-admin icon-group_sliding"></i>', module: {}, isShow: true },
]);
const otherList = ref([
    { type: "coupon", url: "mobilePromotion", label: t('优惠券'), content: '<i class="iconfont-admin icon-coupon"></i>', module: {}, isShow: true }
]);
const liveList = ref([
    { type: "live", url: "mobilePromotion", label: t('小程序直播'), content: '<i class="iconfont-tig icon-xiaochengxuzhibo"></i>', module: {}, isShow: true },
    { type: "videoNumber", url: "mobilePromotion", label: t('视频号'), content: '<i class="iconfont-tig icon-shipinhaozhibo"></i>', module: {}, isShow: true }
]);
const adminToolList = ref([
    { type: "searchBar", url: "mobile", label: t('商品搜索'), content: '<i class="iconfont-admin icon-search_bar"></i>', module: {}, isShow: true },
    { type: "catNav", url: "mobile", label: t('分类导航'), content: '<i class="iconfont-admin icon-cat_nav"></i>', module: {}, isShow: true }
]);
const adminOtherList = ref([
    { type: "seckill", url: "mobilePromotion", label: t('秒杀'), content: '<i class="iconfont-admin icon-seckill"></i>', module: {}, isShow: true },
    { type: "groupon", url: "mobilePromotion", label: t('拼团'), content: '<i class="iconfont-tig icon-pintuan"></i>', module: {}, isShow: true }
]);
const shopToolList = ref([
    { type: "shopInfo", url: "mobilePromotion", label: t('店铺信息'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true }
]);
const memberList = ref([
    { type: "userInfo", url: "mobileMember", label: t('用户信息'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    { type: "userInfoV2", url: "mobileMember", label: t('用户信息2'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    { type: "userInfo3", url: "mobileMember", label: t('用户信息3'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    { type: "orderEntry", url: "mobileMember", label: t('我的订单'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    { type: "myWallet", url: "mobileMember", label: t('我的钱包'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    { type: "myWalletV2", url: "mobileMember", label: t('我的钱包2'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    { type: "adImage", url: "mobileMember", label: t('广告图'), content: '<i class="iconfont-admin icon-image_nav"></i>', module: {}, isShow: true },
    { type: "myService", url: "mobileMember", label: t('我的服务'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    { type: "account", url: "mobileMember", label: t('账号'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: isOverseas() ? true : false }
]);
const cateCenterList = ref([
    { type: "cateCenterV1", url: "mobileCateCenter", label: t('分类中心V1'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true },
    { type: "cateCenterV2", url: "mobileCateCenter", label: t('分类中心V2'), content: '<i class="iconfont-admin icon-product"></i>', module: {}, isShow: true }
]);
const hasExclusiveModule = () => modules.value.moduleList?.some((m: any) => EXCLUSIVE_TYPES.has(m.type));

const clone = (obj: any) => {
    if (hasExclusiveModule()) {
        message.warning(t('页面已包含独占模块，不能添加其他模块'));
        return {};
    }
    if (EXCLUSIVE_TYPES.has(obj.type) && modules.value.moduleList?.length > 0) {
        message.warning(t('独占模块只能在空页面中添加，请先清空已有模块'));
        return {};
    }
    let newObj = cloneDeep(obj);
    newObj.moduleIndex = Date.now();
    delete newObj.content;
    return newObj;
};
const emit = defineEmits(["item-added"]);
const onAdd = (index: number, type = "toolList", e?: MouseEvent) => {
    const listMap: Record<string, any[]> = {
        toolList: toolList.value,
        advancedList: advancedList.value,
        otherList: otherList.value,
        liveList: liveList.value,
        memberList: memberList.value,
        cateCenterList: cateCenterList.value
    };

    const selectedList = listMap[type];
    if (!selectedList || !selectedList[index]) {
        throw new Error(t('无效的类型或索引'));
    }

    const itemType = selectedList[index].type;
    if (hasExclusiveModule()) {
        message.warning(t('页面已包含独占模块，不能添加其他模块'));
        return;
    }
    if (EXCLUSIVE_TYPES.has(itemType) && modules.value.moduleList?.length > 0) {
        message.warning(t('独占模块只能在空页面中添加，请先清空已有模块'));
        return;
    }

    const addItem = cloneDeep(selectedList[index]);
    addItem.moduleIndex = Date.now();
    modules.value.moduleList.push(addItem);
    emit("item-added", { newIndex: modules.value.moduleList.length - 1 });
};
onMounted(() => {
    const shopId = localStorage.getItem("shopId");
    if (shopId && shopId != "0") {
        toolList.value = [...toolList.value, ...shopToolList.value, ...adminOtherList.value];
    } else {
        toolList.value = [...toolList.value, ...adminToolList.value];
        otherList.value = [...otherList.value, ...adminOtherList.value];
    }
});
</script>
<style lang="scss" scoped>
.theme-con-box {
    .modules-head{
        width: auto !important;
    }
    :deep(.el-tabs__item) {
        padding: 0;
        margin: 5px 20px;
    }
    :deep(.el-collapse) {
        border: none !important;
    }
    :deep(.el-collapse-item__header) {
        border: none !important;
    }
    :deep(.el-collapse-item__wrap) {
        border: none !important;
    }
    :deep(.el-collapse-item__content) {
        padding-bottom: 10px;
        line-height: 0.8;
    }
    :deep(.el-collapse-icon-position-right .el-collapse-item__header) {
        padding-right: 0 !important;
    }
}
</style>
