<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>新人二重礼</h3>
            <div class="dec-edit-desc">商品横滑 + 优惠券横排的复合促销卡片（优惠券为前端假数据）</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <div v-if="activeName === 'content'">
            <div class="dec-edit-title-box">
                <div class="title">头部装饰</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">显示头部</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showHeader">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <template v-if="module.showHeader">
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">主标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input v-model="module.headerTitle" placeholder="如：新人专享二重礼" maxlength="20" show-word-limit></el-input>
                    </div>
                </div>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">副标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input v-model="module.headerSubTitle" placeholder="如：限量供应 先到先得" maxlength="20" show-word-limit></el-input>
                    </div>
                </div>
            </template>

            <div class="dec-divider-line"></div>
            <div class="dec-edit-title-box">
                <div class="title">1 重礼·商品设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">显示本区块</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showSection1">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <template v-if="module.showSection1">
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">徽章文字</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input v-model="module.section1Badge" placeholder="如：1重礼" maxlength="6" show-word-limit></el-input>
                    </div>
                </div>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">副标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input v-model="module.section1Title" placeholder="如：新人尝鲜价" maxlength="12" show-word-limit></el-input>
                    </div>
                </div>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">查看更多链接</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="lyecs-link-select">
                            <SelectLink v-model="module.section1MoreLink"></SelectLink>
                        </div>
                    </div>
                </div>

                <CommonProductEdit v-model="module.products"></CommonProductEdit>

                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">商品图片比例</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.productPicRatio">
                            <el-radio-button value="default">默认</el-radio-button>
                            <el-radio-button value="1:1">1:1</el-radio-button>
                            <el-radio-button value="3:4">3:4</el-radio-button>
                            <el-radio-button value="3:2">3:2</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">图片模式</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.productPicFillType">
                            <el-radio-button value="cover">填充</el-radio-button>
                            <el-radio-button value="contain">周边留白</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品图片圆角</div>
                        <div class="value">{{ module.productImgRadius }}px</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider v-model="module.productImgRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品百分比</div>
                        <div class="value">{{ module.productPercent }}%</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider v-model="module.productPercent" show-input :show-input-controls="false" size="small" input-size="default" :min="20" :max="50" />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">显示商品名称</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.showProductName">
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品外边距</div>
                        <div class="value">{{ module.productItemGap }}px</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider v-model="module.productItemGap" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品内边距</div>
                        <div class="value">{{ module.productItemPadding }}px</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider v-model="module.productItemPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">显示划线价</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.showMarketPrice">
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">购买按钮样式</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.buyBtnStyle">
                            <el-radio-button :value="0">不显示</el-radio-button>
                            <el-radio-button :value="1">样式一</el-radio-button>
                            <el-radio-button :value="2">样式二</el-radio-button>
                            <el-radio-button :value="3">样式三</el-radio-button>
                            <el-radio-button :value="4">样式四</el-radio-button>
                            <el-radio-button :value="5">样式五</el-radio-button>
                            <el-radio-button :value="6">样式六</el-radio-button>
                            <el-radio-button :value="7">样式七</el-radio-button>
                            <el-radio-button :value="8">样式八</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">商品卖点</div>
                </div>
                <div class="dec-edit-group-desc" v-if="productCount === 0">
                    <div>请先在上方「选择商品」中添加商品后再来配置卖点文字</div>
                </div>
                <div class="product-brief-list" v-if="productCount > 0">
                    <div class="brief-card" v-for="index in productIndexes" :key="index">
                        <span class="brief-card-label">
                            第 {{ index + 1 }} 个商品<span v-if="isDynamic" class="brief-card-dynamic">动态拉取</span>
                        </span>
                        <el-input
                            v-model="productBriefListProxy[index]"
                            placeholder="如：新品 / 折扣"
                            maxlength="6"
                            show-word-limit
                        ></el-input>
                    </div>
                </div>
            </template>

            <div class="dec-divider-line"></div>
            <div class="dec-edit-title-box">
                <div class="title">2 重礼·优惠券设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">显示本区块</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showSection2">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <template v-if="module.showSection2">
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">徽章文字</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input v-model="module.section2Badge" placeholder="如：2重礼" maxlength="6" show-word-limit></el-input>
                    </div>
                </div>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">副标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input v-model="module.section2Title" placeholder="如：新人专享礼包" maxlength="12" show-word-limit></el-input>
                    </div>
                </div>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">查看更多链接</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="lyecs-link-select">
                            <SelectLink v-model="module.section2MoreLink"></SelectLink>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">优惠券布局</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.couponPerRow">
                            <el-radio-button :value="1">一行一个</el-radio-button>
                            <el-radio-button :value="2">一行两个</el-radio-button>
                            <el-radio-button :value="3">一行三个</el-radio-button>
                            <el-radio-button :value="4">一行四个</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">优惠券列表</div>
                </div>
                <div class="coupon-list">
                    <draggable
                        :list="coupons"
                        item-key="couponId"
                        handle=".coupon-handle"
                        ghost-class="ghost"
                        chosen-class="chosenClass"
                        animation="300"
                    >
                        <template #item="{ index }">
                            <div class="coupon-list-item">
                                <svg class="coupon-handle" width="14" height="14" viewBox="0 0 14 14" aria-hidden="true">
                                    <line x1="2" y1="3.5" x2="12" y2="3.5" stroke="#bbb" stroke-width="1.5" />
                                    <line x1="2" y1="7" x2="12" y2="7" stroke="#bbb" stroke-width="1.5" />
                                    <line x1="2" y1="10.5" x2="12" y2="10.5" stroke="#bbb" stroke-width="1.5" />
                                </svg>
                                <span class="coupon-text">
                                    优惠券:
                                    {{ displayCoupons[index]?.info?.couponName || (couponInfoLoading ? "加载中..." : "信息缺失") }}
                                </span>
                                <a class="coupon-del" @click="removeCoupon(index)">
                                    <i class="ico-decorate icon-dec-cha"></i>
                                </a>
                            </div>
                        </template>
                    </draggable>
                    <div class="coupon-add">
                        <DialogForm
                            isDrawer
                            width="700px"
                            title="选择优惠券"
                            path="promotion/coupon/src/MultiCouponSelectList"
                            :params="{ selectedIds: excludedCouponIds }"
                            @okCallback="onCouponsSelected"
                        >
                            <a><i class="ico-decorate icon-dec-jia"></i>添加优惠券</a>
                        </DialogForm>
                    </div>
                </div>
            </template>
        </div>

        <div v-if="activeName === 'extend'">
            <div class="dec-edit-title-box">
                <div class="title">商品配色</div>
            </div>
            <div class="dec-edit-group" v-for="row in productColorRows" :key="row.key">
                <div class="dec-edit-group-title">
                    <div class="label">{{ row.label }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetColor(row.key, row.default)">重置</a>
                            <SelectColor
                                :color="(module as any)[row.key]"
                                @update:color="(v: string) => ((module as any)[row.key] = v)"
                            ></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">卖点背景渐变</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-gradient-pair">
                        <a class="dec-color-reset" @click="resetGradient('productBriefGradientStart', 'productBriefGradientEnd', defaults.productBriefGradientStart, defaults.productBriefGradientEnd)">重置</a>
                        <SelectColor v-model:color="module.productBriefGradientStart"></SelectColor>
                        <span class="gradient-sep">~</span>
                        <SelectColor v-model:color="module.productBriefGradientEnd"></SelectColor>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">按钮背景渐变</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-gradient-pair">
                        <a class="dec-color-reset" @click="resetGradient('buyBtnGradientStart', 'buyBtnGradientEnd', defaults.buyBtnGradientStart, defaults.buyBtnGradientEnd)">重置</a>
                        <SelectColor v-model:color="module.buyBtnGradientStart"></SelectColor>
                        <span class="gradient-sep">~</span>
                        <SelectColor v-model:color="module.buyBtnGradientEnd"></SelectColor>
                    </div>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-edit-title-box">
                <div class="title">优惠券配色</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">优惠券背景渐变</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-gradient-pair">
                        <a class="dec-color-reset" @click="resetGradient('couponBgGradientStart', 'couponBgGradientEnd', defaults.couponBgGradientStart, defaults.couponBgGradientEnd)">重置</a>
                        <SelectColor v-model:color="module.couponBgGradientStart"></SelectColor>
                        <span class="gradient-sep">~</span>
                        <SelectColor v-model:color="module.couponBgGradientEnd"></SelectColor>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-for="row in couponColorRows" :key="row.key">
                <div class="dec-edit-group-title">
                    <div class="label">{{ row.label }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetColor(row.key, row.default)">重置</a>
                            <SelectColor
                                :color="(module as any)[row.key]"
                                @update:color="(v: string) => ((module as any)[row.key] = v)"
                            ></SelectColor>
                        </div>
                    </div>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-edit-title-box">
                <div class="title">整体修饰</div>
            </div>
            <div class="dec-edit-group" v-for="row in decorationColorRows" :key="row.key">
                <div class="dec-edit-group-title">
                    <div class="label">{{ row.label }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetColor(row.key, row.default)">重置</a>
                            <SelectColor
                                :color="(module as any)[row.key]"
                                @update:color="(v: string) => ((module as any)[row.key] = v)"
                            ></SelectColor>
                        </div>
                    </div>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-edit-title-box">
                <div class="title">容器设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">外层背景渐变</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-gradient-pair">
                        <a class="dec-color-reset" @click="resetGradient('boxGradientStart', 'boxGradientEnd', defaults.boxGradientStart, defaults.boxGradientEnd)">重置</a>
                        <SelectColor v-model:color="module.boxGradientStart"></SelectColor>
                        <span class="gradient-sep">~</span>
                        <SelectColor v-model:color="module.boxGradientEnd"></SelectColor>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">容器背景图</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="containerBgImageProxy" :default-value="defaults.containerBgImage"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">圆角</div>
                    <div class="value">{{ frame.boxRadius }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.boxRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">左右边距</div>
                    <div class="value">{{ frame.boxPadding }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.boxPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">上边距</div>
                    <div class="value">{{ frame.boxPaddingTop }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.boxPaddingTop" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">下边距</div>
                    <div class="value">{{ frame.boxPaddingBottom }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.boxPaddingBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import draggable from "vuedraggable";
import Tabs from "@/components/tabs/Index.vue";
import { SelectColor, SelectLink } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import { DialogForm } from "@/components/dialog";
import CommonProductEdit from "@/views/decorate/decorate/src/modules/src/commonProduct/Edit.vue";
import { NewcomerCouponItem, ModuleFrameType } from "@/types/decorate/decorate.d";
import { defaultNewcomerCombo } from "@/views/decorate/decorate/src/modules/index";
import { useCouponInfoMap } from "@/hooks/useCouponInfoMap";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { NewcomerComboModule } from "./types";

// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，
// 模板内 v-model 直接写该 reactive 对象（与旧 defineModel 等价，公共子组件零改造复用）
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as NewcomerComboModule);

const tabs = ref<any[]>([
    { label: "基础设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");

const defaults = defaultNewcomerCombo;

const frame = computed<ModuleFrameType>({
    get() {
        if (!module.value.frame) {
            module.value.frame = { ...(defaults.frame as ModuleFrameType) };
        }
        return module.value.frame as ModuleFrameType;
    },
    set(v) {
        module.value.frame = v;
    }
});

const productCount = computed(() => {
    const p: any = module.value?.products;
    if (!p) return 0;
    if (p.productSelectType === 1) return p.productIds?.length || 0;
    return Number(p.productNumber) || 0;
});
const productIndexes = computed<number[]>(() => Array.from({ length: productCount.value }, (_, i) => i));

const isDynamic = computed(() => {
    const t = (module.value?.products as any)?.productSelectType;
    return t === 2 || t === 3;
});

const ensureProductBriefList = () => {
    if (!Array.isArray(module.value.productBriefList)) {
        module.value.productBriefList = [];
    }
};

const productBriefListProxy = computed({
    get() {
        ensureProductBriefList();
        const arr = module.value.productBriefList as string[];
        while (arr.length < productCount.value) arr.push("");
        return arr;
    },
    set(v: string[]) {
        module.value.productBriefList = v;
    }
});

let prevProductIds: number[] = [];
watch(
    () => {
        const ids = (module.value?.products as any)?.productIds;
        return Array.isArray(ids) ? [...ids] : [];
    },
    (newIds) => {
        const t = (module.value?.products as any)?.productSelectType;
        if (t !== 1) {
            prevProductIds = [];
            return;
        }
        if (!newIds) return;
        if (prevProductIds.length === 0 && newIds.length > 0) {
            prevProductIds = [...newIds];
            return;
        }
        const prevIdToIdx = new Map<number, number>();
        prevProductIds.forEach((id, i) => prevIdToIdx.set(id, i));
        ensureProductBriefList();
        const oldBriefs = [...(module.value.productBriefList as string[])];
        module.value.productBriefList = newIds.map((id) => {
            const oldIdx = prevIdToIdx.get(id);
            return oldIdx !== undefined ? oldBriefs[oldIdx] || "" : "";
        });
        prevProductIds = [...newIds];
    },
    { immediate: true }
);

watch(
    () => [(module.value?.products as any)?.productSelectType, (module.value?.products as any)?.productNumber],
    () => {
        ensureProductBriefList();
        const n = productCount.value;
        const arr = module.value.productBriefList as string[];
        while (arr.length < n) arr.push("");
        if (arr.length > n) arr.length = n;
    }
);

const coupons = computed<NewcomerCouponItem[]>({
    get() {
        if (!Array.isArray(module.value.coupons)) {
            module.value.coupons = [];
        }
        return module.value.coupons as NewcomerCouponItem[];
    },
    set(v) {
        module.value.coupons = v;
    }
});

const excludedCouponIds = computed<number[]>(() =>
    coupons.value.map((c) => c.couponId).filter((id): id is number => Boolean(id))
);

const { couponInfoMap, fetchCouponInfo, removeCouponInfo } = useCouponInfoMap();
const couponInfoLoading = ref(false);

const displayCoupons = computed(() =>
    coupons.value.map((c) => ({
        ...c,
        info: couponInfoMap.value.get(c.couponId) || null
    }))
);

watch(
    () => excludedCouponIds.value.join(","),
    async () => {
        const ids = excludedCouponIds.value;
        if (!ids.length) return;
        couponInfoLoading.value = true;
        try {
            await fetchCouponInfo(ids);
        } finally {
            couponInfoLoading.value = false;
        }
    },
    { immediate: true }
);

const onCouponsSelected = (rows: any) => {
    const list = Array.isArray(rows) ? rows : [rows];
    // 弹窗返回的是"最终选中的完整列表"（语义同选择商品弹窗）
    // 取消勾选的不会出现在列表里 → 自动从 coupons 移除
    // 新增的进入列表 → 自动加入
    // 已存在的保留原 btnText（用户已配置的不能丢）
    const existingBtnTextMap = new Map(coupons.value.map((c) => [c.couponId, c.btnText]));
    coupons.value = list
        .filter((r: any) => r && r.couponId)
        .map((r: any) => ({
            couponId: r.couponId,
            btnText: existingBtnTextMap.get(r.couponId) || "立即领取"
        }));
};

const removeCoupon = (idx: number) => {
    const removed = coupons.value[idx];
    coupons.value.splice(idx, 1);
    if (removed?.couponId) {
        removeCouponInfo(removed.couponId);
    }
};

type ColorRowDef = { label: string; key: keyof NewcomerComboType; default: string };

const productColorRows: ColorRowDef[] = [
    { label: "商品标题色", key: "productTitleColor", default: defaults.productTitleColor },
    { label: "描述文字色", key: "productBriefColor", default: defaults.productBriefColor },
    { label: "商品价格色", key: "productPriceColor", default: defaults.productPriceColor },
    { label: "商品原价色", key: "productMarketPriceColor", default: defaults.productMarketPriceColor },
    { label: "卖点文字色", key: "productBriefTextColor", default: defaults.productBriefTextColor },
    { label: "按钮文字色", key: "buyBtnTextColor", default: defaults.buyBtnTextColor }
];

const couponColorRows: ColorRowDef[] = [
    { label: "金额文字色", key: "couponAmountColor", default: defaults.couponAmountColor },
    { label: "条件文字色", key: "couponConditionColor", default: defaults.couponConditionColor },
    { label: "按钮文字色", key: "couponBtnTextColor", default: defaults.couponBtnTextColor }
];

const decorationColorRows: ColorRowDef[] = [
    { label: "商品信息背景色", key: "contentBgColor", default: defaults.contentBgColor },
    { label: "修饰背景色", key: "decorationBgColor", default: defaults.decorationBgColor }
];

const resetColor = (key: keyof NewcomerComboType, defaultValue: string) => {
    (module.value as any)[key] = defaultValue;
};

const resetGradient = (
    startKey: keyof NewcomerComboType,
    endKey: keyof NewcomerComboType,
    defaultStart: string,
    defaultEnd: string
) => {
    (module.value as any)[startKey] = defaultStart;
    (module.value as any)[endKey] = defaultEnd;
};

const containerBgImageProxy = computed({
    get() {
        if (!module.value.containerBgImage) {
            module.value.containerBgImage = { picUrl: "", picThumb: "" };
        }
        return module.value.containerBgImage as { picUrl?: string; picThumb?: string };
    },
    set(v: { picUrl?: string; picThumb?: string }) {
        module.value.containerBgImage = v;
    }
});
</script>
<style lang="less" scoped>
.dec-gradient-pair {
    display: flex;
    align-items: center;
    gap: 8px;
    .gradient-sep {
        color: #999;
    }
}
.product-brief-list {
    padding: 0 10px 10px;
    .brief-card {
        background-color: #fff;
        border-radius: 4px;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.15);
        padding: 8px 10px 10px;
        margin-top: 10px;
        .brief-card-label {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 6px;
            .brief-card-dynamic {
                font-size: 11px;
                font-weight: 400;
                color: #999;
                padding: 1px 6px;
                background-color: #f5f7fa;
                border-radius: 2px;
            }
        }
    }
}
.coupon-list {
    padding: 0 16px 15px;
    .coupon-list-item {
        position: relative;
        display: flex;
        align-items: center;
        gap: 8px;
        background-color: #fff;
        border-radius: 2px;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);
        padding: 10px 12px;
        margin-top: 12px;
        .coupon-handle {
            flex-shrink: 0;
            cursor: move;
            display: block;
        }
        .coupon-text {
            flex: 1;
            font-size: 13px;
            color: #333;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .coupon-del {
            position: absolute;
            top: -10px;
            right: -10px;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: #bbb;
            border: 2px solid #fff;
            color: #fff;
            text-align: center;
            line-height: 18px;
            display: none;
            cursor: pointer;
            z-index: 2;
            i {
                font-size: 12px;
            }
        }
        &:hover .coupon-del {
            display: block;
        }
    }
    .coupon-add {
        margin-top: 12px;
        :deep(.dialog-link) {
            display: block !important;
            width: 100%;
        }
        a {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 10px 0;
            border: 1px solid #155bd4;
            color: #155bd4;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            i {
                margin-right: 4px;
            }
            &:hover {
                color: #155bd4;
                border-color: #155bd4;
            }
        }
    }
}
</style>
