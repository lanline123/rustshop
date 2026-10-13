<template>
    <div class="dec-pc-category-nav" :style="frameStyle">
        <div class="dec-pc-category-nav__inner" :style="innerStyle">
            <div v-if="moduleConfig.showTitle === 1" class="dec-pc-category-nav__top">
                <div class="dec-pc-category-nav__title">{{ moduleConfig.titleName || "热门分类" }}</div>
            </div>

            <div class="dec-pc-category-nav__list-wrap">
                <div v-if="loading" class="dec-pc-category-nav__empty">
                    <div class="dec-pc-category-nav__empty-text">加载中...</div>
                </div>

                <div v-else-if="!hotCategories.length" class="dec-pc-category-nav__empty">
                    <div class="dec-pc-category-nav__empty-text">暂无热门分类（请在分类管理中勾选"热门"）</div>
                </div>

                <div v-else class="dec-pc-category-nav__list">
                    <Swiper :key="swiperKey" :swiper-option="swiperOption" :model-value="hotCategories" style="width: 100%">
                        <template #default="{ item }">
                            <div class="dec-pc-category-nav__item">
                                <a class="dec-pc-category-nav__link" href="javascript:;">
                                    <div class="dec-pc-category-nav__image">
                                        <img
                                            class="dec-pc-category-nav__img"
                                            :src="formatImage(item.categoryPic)"
                                            :alt="item.categoryName"
                                            @error="handleImgError"
                                        />
                                    </div>
                                    <div class="dec-pc-category-nav__name">
                                        {{ item.categoryName }}
                                    </div>
                                </a>
                            </div>
                        </template>
                    </Swiper>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 分类导航模块 - 预览组件
 * 双层结构对齐 PC 端 PcCategoryNavWrapper：
 *   外层 .dec-pc-category-nav — 上下左右间距（padding），无背景色
 *   内层 .dec-pc-category-nav__inner — 背景色、圆角、固定高度
 */
import { computed, onMounted, ref } from "vue";
import type { CategoryFilterParams, CategoryFilterState } from "@/types/product/category";
import type { PcCategoryNavModule } from "./types";
import { pcCategoryNavDefaults } from ".";
import { Swiper } from "@/components/swiper";
import { deepMerge } from "../../../utils/object";
import { getCategoryList } from "@/api/product/category";
import { imageFormat } from "@/utils/format";

defineOptions({ name: "PcCategoryNavPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcCategoryNavModule>(() => {
    return deepMerge(pcCategoryNavDefaults, props.module as Partial<PcCategoryNavModule>);
});

const loading = ref(false);
const hotCategories = ref<CategoryFilterState[]>([]);

async function fetchHotCategories() {
    loading.value = true;
    try {
        // 后端 getAllCategory 的树节点只附带 isShow/categoryPic、不含 isHot，无法筛热门；
        // 改用 list 接口（记录由 Category 实体拷贝，含 isHot/isShow），parentId 传 -1（后端 <0 不加层级条件）取全部分类后前端筛
        const res = await getCategoryList({ page: 1, size: 100, parentId: -1 } as CategoryFilterParams);
        const records = (res?.records || []) as CategoryFilterState[];
        hotCategories.value = records.filter((x) => Number(x.isHot) === 1 && Number(x.isShow) === 1);
    } catch {
        hotCategories.value = [];
    } finally {
        loading.value = false;
    }
}

onMounted(() => {
    fetchHotCategories();
});

const displayNum = computed(() => {
    const raw = Number(moduleConfig.value.displayNum ?? 6);
    return Number.isFinite(raw) ? Math.min(10, Math.max(1, raw)) : 6;
});

/**
 * 容器层（扩展设置「背景颜色」）：背景色 + 圆角；上/下/左右边距作为留白，露出容器底色
 */
const frameStyle = computed(() => {
    const frame = moduleConfig.value.frame;
    const boxRadius = frame?.boxRadius ?? 8;
    return {
        backgroundColor: frame?.backgroundColor || "transparent",
        borderRadius: `${boxRadius}px`,
        overflow: boxRadius ? "hidden" : undefined,
        paddingLeft: `${frame?.boxPadding ?? 0}px`,
        paddingRight: `${frame?.boxPadding ?? 0}px`,
        paddingTop: `${frame?.boxPaddingTop ?? 0}px`,
        paddingBottom: `${frame?.boxPaddingBottom ?? 0}px`
    };
});

/**
 * 内容层（内容设置「背景颜色」）：与容器层相互独立，叠在容器层之上
 */
const innerStyle = computed(() => {
    const data = moduleConfig.value;
    const frame = data.frame;
    return {
        backgroundColor: data.backgroundColor || "#f7f7f7",
        borderRadius: "8px",
        height: `${frame?.boxHeight ?? 328}px`
    };
});

const swiperOption = computed(() => {
    const num = displayNum.value;
    const canLoop = hotCategories.value.length > num;
    return {
        autoplay: {
            delay: 5000,
            disableOnInteraction: false,
            pauseOnMouseEnter: true
        },
        slidesPerGroup: num,
        slidesPerView: num,
        pagination: false,
        navigation: true,
        effect: "slide",
        loop: canLoop,
        observer: true,
        observeParents: true,
        resizeObserver: true
    };
});

// 分类数量 / 一屏显示数变化时强制重建 Swiper，保证画布即时预览（与 pc-image-ad 同机制）
const swiperKey = computed(() => `${hotCategories.value.length}-${displayNum.value}`);

const emptyImgSvg =
    "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' width='180' height='180'><rect fill='%23f0f0f0' width='180' height='180'/></svg>";

function formatImage(url?: string): string {
    if (!url) {
        return emptyImgSvg;
    }
    // categoryPic 多为相对路径，补 storageUrl 域名前缀（http 链接原样返回）
    return imageFormat(url);
}

function handleImgError(e: Event) {
    const img = e.target as HTMLImageElement | null;
    if (!img) return;
    if (img.src === emptyImgSvg) return;
    img.onerror = null;
    img.src = emptyImgSvg;
}
</script>

<style lang="less" src="./preview.less" />
