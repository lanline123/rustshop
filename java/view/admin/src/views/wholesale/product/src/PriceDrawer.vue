/** * @file 批发价设置主抽屉（核心） * @description 折扣/直接录入 * 注：批量子抽屉仅套用到前端表单态（覆盖），最终由本抽屉「确定」走全量替换统一保存 */
<template>
    <div class="price-drawer" v-loading="loading">
        <el-alert class="mb10" type="primary" :closable="false" :title="$t('提示 价格优先级：类型售价 > 通用售价；已配置类型售价时可不填通用售价')" show-icon />

        <el-form class="top-form" label-width="170px">
            <!-- 多规格混批总价计算（仅多规格） -->
            <el-form-item v-if="form.isMultiSpec" required>
                <template #label>
                    <span class="form-label"> {{ $t('多规格混批总价计算') }} <el-tooltip placement="top" effect="light">
                            <template #content>
                                <div class="tip-content">
                                    <div class="tip-section">
                                        <div class="tip-title">{{ $t('商品级别(混批)计价：') }}</div>
                                        <div>
                                            <strong>{{ $t('定义：') }}</strong>{{ $t('这种是指将一个商品下所有SKU（如颜色、尺寸）作为一个整体来计算购买数量，以确定适用的阶梯价格。') }} </div>
                                        <div>{{ $t('先把所有规格的购买数量相加，用总件数匹配阶梯；匹配成功后，每个规格按自己该档的批发单价分别乘数量再求和。') }}</div>
                                        <div>
                                            <strong>{{ $t('示例：') }}</strong>{{ $t('假设某服装店的T恤衫有S码和M码，S码3件以上，批发价￥100，M码5件以上，批发价90元。 现执行混批策略，顾客购买了2件S码，3件M码，共5件。则总价为100x2+3x90=470。') }} </div>
                                    </div>
                                    <div class="tip-section">
                                        <div class="tip-title">{{ $t('SKU级别计价：') }}</div>
                                        <div>
                                            <strong>{{ $t('定义：') }}</strong
                                            >{{ $t('这种是指针对一个商品下每个具体的SKU（如颜色、尺寸）单独计算购买数量，以确定适用的阶梯价格。即每个SKU都有独立的折扣空间和阶梯价格。') }} </div>
                                        <div>
                                            <strong>{{ $t('示例：') }}</strong
                                            >{{ $t('假设某服装店的T恤衫有S码和M码，并采用了以下阶梯价策略：Ｓ码阶梯价为：1件50元，2-5件每件45元，6件以上40元。Ｍ码阶梯价为：1件40元，2-5件每件35元，6件以上30元。顾客购买了2件S码T恤和6件M码T恤，则总价格为2×45+6×30=270元。') }} </div>
                                    </div>
                                </div>
                            </template>
                            <el-icon class="tip-icon"><QuestionFilled /></el-icon>
                        </el-tooltip>
                    </span>
                </template>
                <el-radio-group v-model="form.calcMode">
                    <el-radio value="spuMix">{{ $t('商品级别(混批)') }}</el-radio>
                    <el-radio value="skuLevel">{{ $t('SKU级别') }}</el-radio>
                </el-radio-group>
            </el-form-item>

            <!-- 批发价设价方式 -->
            <el-form-item required>
                <template #label>
                    <span class="form-label"> {{ $t('批发价设价方式') }} <el-tooltip placement="top" effect="light">
                            <template #content>
                                <div class="tip-content">
                                    <div class="tip-section">
                                        <div class="tip-title">{{ $t('折扣换算批发价') }}</div>
                                        <div><strong>{{ $t('定义：') }}</strong>{{ $t('该方式会按照商品未参与任何活动前的零售价进行打折，商品零售价变更后，价格也会随之改变。') }}</div>
                                        <div>
                                            <strong>{{ $t('示例：') }}</strong
                                            >{{ $t('T恤零售价100元，1件5折、2-5件4.5折。购买2件批发价=100*0.45=45元；改零售价200元后，购买2件=200*0.45=90元。') }} </div>
                                    </div>
                                    <div class="tip-section">
                                        <div class="tip-title">{{ $t('直接录入批发价') }}</div>
                                        <div><strong>{{ $t('定义：') }}</strong>{{ $t('该方式会按照商品录入的价格作为批发价，商品零售价变更后，批发价也不会改变。') }}</div>
                                        <div><strong>{{ $t('示例：') }}</strong>{{ $t('1件50元、2-5件45元。购买2件批发价固定45元；改零售价后仍为45元。') }}</div>
                                    </div>
                                </div>
                            </template>
                            <el-icon class="tip-icon"><QuestionFilled /></el-icon>
                        </el-tooltip>
                    </span>
                </template>
                <el-radio-group v-model="form.priceMode" @change="onPriceModeChange">
                    <el-radio value="discount">{{ $t('折扣换算批发价') }}</el-radio>
                    <el-radio value="fixed">{{ $t('直接录入批发价') }}</el-radio>
                </el-radio-group>
            </el-form-item>

            <!-- 批量操作按钮（仅多规格，独占一行；label=" " 占位以保持内容列对齐；通过 DialogForm 拉起子抽屉，统一项目内抽屉接入方式） -->
            <el-form-item v-if="form.isMultiSpec" label="">
                <div class="batch-actions">
                    <DialogForm
                        isDrawer
                        :title="$t('批量设置通用售价')"
                        width="1000px"
                        path="wholesale/product/src/BatchGeneralPriceDrawer"
                        :params="{ skus: skuOptions, priceMode: form.priceMode, retailPrice: form.skus[0]?.retailPrice }"
                        @okCallback="onApplyGeneral"
                    >
                        <el-button type="primary">{{ $t('批量设置通用售价') }}</el-button>
                    </DialogForm>
                    <DialogForm
                        isDrawer
                        :title="$t('批量设置类型售价')"
                        width="1000px"
                        path="wholesale/product/src/BatchGradePriceDrawer"
                        :params="{ skus: skuOptions, gradeOptions, priceMode: form.priceMode, retailPrice: form.skus[0]?.retailPrice }"
                        @okCallback="onApplyGrade"
                    >
                        <el-button type="primary">{{ $t('批量设置类型售价') }}</el-button>
                    </DialogForm>
                </div>
            </el-form-item>
            <el-form-item label="" class="sku-form-item">
                <!-- 规格分支：单规格显示「统一规格」，多规格按 SKU 展开 -->
                <div v-if="!loading && form.skus.length > 0" class="multi-panel">
                    <el-collapse v-model="activeSkuKeys">
                        <el-collapse-item v-for="(sku, idx) in form.skus" :key="sku.skuId" :name="sku.skuId">
                            <template #title>
                                <span class="sku-title">{{ sku.skuName }}</span>
                            </template>
                            <div class="custom-tabs">
                                <div class="custom-tab-item" :class="{ active: skuTabs[idx] === 'general' }" @click="skuTabs[idx] = 'general'">{{ $t('通用售价') }}</div>
                                <div class="custom-tab-item" :class="{ active: skuTabs[idx] === 'grade' }" @click="skuTabs[idx] = 'grade'">{{ $t('类型售价') }}</div>
                            </div>
                            <div class="custom-tab-content">
                                <div v-show="skuTabs[idx] === 'general'">
                                    <LadderTable
                                        v-model="sku.generalLadders"
                                        :priceMode="form.priceMode"
                                        :retailPrice="sku.retailPrice"
                                        :errors="getGeneralErrors(sku)"
                                        :errorKeyPrefix="`general:${sku.skuId}`"
                                        @clear-error="(p) => clearGeneralError(sku.skuId, p.index, p.field)"
                                        @blur="validateForm(true)"
                                    />
                                </div>
                                <div v-show="skuTabs[idx] === 'grade'">
                                    <GradeLadderTable
                                        v-model="sku.gradeLadders"
                                        :gradeOptions="gradeOptions"
                                        :priceMode="form.priceMode"
                                        :retailPrice="sku.retailPrice"
                                        :errors="getGradeErrors(sku)"
                                        :errorKeyPrefix="`grade:${sku.skuId}`"
                                        @clear-error="(p) => clearGradeError(sku.skuId, p.gradeId, p.index, p.field)"
                                        @blur="validateForm(true)"
                                    />
                                </div>
                            </div>
                        </el-collapse-item>
                    </el-collapse>
                </div>
            </el-form-item>
        </el-form>
        <div v-if="!loading && form.skus.length === 0" class="empty-tip">{{ $t('该商品无可定价规格') }}</div>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { message } from "ant-design-vue";
import { QuestionFilled } from "@element-plus/icons-vue";
import LadderTable from "./LadderTable.vue";
import GradeLadderTable from "./GradeLadderTable.vue";
import DialogForm from "@/components/dialog/src/DialogForm.vue";
import { getWholesalePriceDetail, saveWholesalePrice } from "@/api/wholesale/product";
import { getGradeList } from "@/api/wholesale/grade";
import { validateLadderGroup, isLadderEmpty, isLadderGroupEmpty, hasConfiguredGradeLadders } from "./ladderValidate";
import { buildUniformPriceRows, cloneSkuPriceDraft, transitionSkuPriceMode } from "@/views/wholesale/contractLogic";
import type { PriceForm, SkuPriceForm, Ladder, GradeLadder, LadderError, PriceRow, WholesalePriceDetailVO } from "@/types/wholesale/price";
import type { WholesalerGradeVO } from "@/types/wholesale/grade";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    productId: { type: Number, default: 0 },
    productName: { type: String, default: "" }
});

const loading = ref<boolean>(true);
const gradeOptions = ref<WholesalerGradeVO[]>([]);
const activeSkuKeys = ref<number[]>([]);
const skuTabs = reactive<Record<number, string>>({});
let prevPriceMode: "discount" | "fixed" = "discount";
let snapshots: Record<"discount" | "fixed", SkuPriceForm[]> = {
    discount: [],
    fixed: []
};
const form = reactive<PriceForm>({
    productId: props.productId,
    isMultiSpec: false,
    priceMode: "discount",
    calcMode: "spuMix",
    skus: []
});

const skuOptions = computed(() => form.skus.map((s) => ({ skuId: s.skuId, skuName: s.skuName })));

/** 扁平价格行 → 阶梯 */
const rowsToLadders = (rows: (PriceRow & { gradeName?: string })[]): Ladder[] =>
    rows.map((row) => ({ qty: row.tierMinQty, value: Number(row.priceMode === 1 ? row.discount : row.wholesalePrice) })).sort((a, b) => a.qty - b.qty);

const loadDetail = async () => {
    loading.value = true;
    try {
        const detail: WholesalePriceDetailVO = await getWholesalePriceDetail({ productId: props.productId });
        form.isMultiSpec = !!detail.isMultiSpec;
        form.priceMode = detail.priceMode === 2 ? "fixed" : "discount";
        prevPriceMode = form.priceMode;
        form.calcMode = detail.multiSpecPriceMode === 2 ? "skuLevel" : "spuMix";
        form.skus = (detail.skus || []).map((s) => {
            const general = rowsToLadders((s.priceRows || []).filter((r) => r.gradeId === 0));
            const gradeMap: Record<number, GradeLadder> = {};
            (s.priceRows || [])
                .filter((r) => r.gradeId > 0)
                .forEach((r) => {
                    if (!gradeMap[r.gradeId]) {
                        gradeMap[r.gradeId] = { gradeId: r.gradeId, gradeName: r.gradeName || "", ladders: [] };
                    }
                    gradeMap[r.gradeId].ladders.push({ qty: r.tierMinQty, value: Number(r.priceMode === 1 ? r.discount : r.wholesalePrice) });
                });
            Object.values(gradeMap).forEach((g) => g.ladders.sort((a, b) => a.qty - b.qty));
            return {
                skuId: s.skuId,
                skuName: form.isMultiSpec ? s.skuSpecText : t('统一规格'),
                retailPrice: Number(s.retailPrice),
                // 通用售价保留 1 个空档占位（接口无数据时不预填默认值，让用户自行录入）
                generalLadders: general.length > 0 ? general : [{ qty: undefined, value: undefined } as unknown as Ladder],
                gradeLadders: Object.values(gradeMap)
            } as SkuPriceForm;
        });
        // 单规格商品后端未配置时不下发 skus，前端补一条「统一规格」占位（skuId=0=SPU 兜底）
        if (!form.isMultiSpec && form.skus.length === 0) {
            form.skus = [
                {
                    skuId: 0,
                    skuName: t('统一规格'),
                    retailPrice: 0,
                    generalLadders: [{ qty: undefined, value: undefined } as unknown as Ladder],
                    gradeLadders: []
                }
            ];
        }
        // 默认展开首个 SKU/统一规格；有类型售价时优先展示类型售价 Tab
        form.skus.forEach((s, idx) => {
            skuTabs[idx] = hasConfiguredGradeLadders(s.gradeLadders) ? "grade" : "general";
        });
        activeSkuKeys.value = form.skus.map((s) => s.skuId);
        snapshots[form.priceMode] = cloneSkuPriceDraft(form.skus);
        snapshots[form.priceMode === "discount" ? "fixed" : "discount"] = [];
    } catch (error: any) {
        message.error(error.message || t('加载失败'));
        emit("close");
    } finally {
        loading.value = false;
    }
};

/** 批量套用：通用售价 */
const onApplyGeneral = (payload: { skuIds: number[]; ladders: Ladder[] }) => {
    form.skus.forEach((sku) => {
        if (payload.skuIds.includes(sku.skuId)) {
            sku.generalLadders = payload.ladders.map((l) => ({ qty: l.qty, value: l.value }));
        }
    });
    message.success(t('已套用通用售价'));
};

/** 批量套用：类型售价 */
const onApplyGrade = (payload: { skuIds: number[]; gradeLadders: GradeLadder[] }) => {
    form.skus.forEach((sku) => {
        if (payload.skuIds.includes(sku.skuId)) {
            sku.gradeLadders = payload.gradeLadders.map((g) => ({
                gradeId: g.gradeId,
                gradeName: g.gradeName,
                ladders: g.ladders.map((l) => ({ qty: l.qty, value: l.value }))
            }));
        }
    });
    message.success(t('已套用类型售价'));
};

// ===== 校验：行级错误，仿 el-form-item 标红 + 行下方提示 =====
/**
 * 错误存储：以稳定字符串 key 索引
 *   通用售价：`general:{skuId}:{rowIndex}`
 *   类型售价：`grade:{skuId}:{gradeId}:{rowIndex}`
 */
const errors = reactive<Record<string, LadderError>>({});
const onPriceModeChange = (val: "discount" | "fixed") => {
    const result = transitionSkuPriceMode({ previousMode: prevPriceMode, nextMode: val, currentSkus: form.skus, snapshots });
    form.skus = result.nextSkus;
    snapshots = result.nextSnapshots;
    prevPriceMode = result.nextPreviousMode;
    Object.keys(errors).forEach((key) => delete errors[key]);
};
const generalKey = (skuId: number, index: number) => `general:${skuId}:${index}`;
const gradeKey = (skuId: number, gradeId: number, index: number) => `grade:${skuId}:${gradeId}:${index}`;

/** 模板内：取某 SKU 通用售价的错误数组（按行 index 平铺） */
const getGeneralErrors = (sku: SkuPriceForm): (LadderError | undefined)[] => sku.generalLadders.map((_, i) => errors[generalKey(sku.skuId, i)]);

/** 模板内：取某 SKU 类型售价的错误（按 gradeId 分组，每组为行错误数组） */
const getGradeErrors = (sku: SkuPriceForm): Record<number, (LadderError | undefined)[]> => {
    const map: Record<number, (LadderError | undefined)[]> = {};
    sku.gradeLadders.forEach((g) => {
        map[g.gradeId] = g.ladders.map((_, i) => errors[gradeKey(sku.skuId, g.gradeId, i)]);
    });
    return map;
};

/** 子表通知：用户修改了某行某字段，清除该字段错误（红框/文案立刻消失，另一字段错误保留） */
const clearGeneralError = (skuId: number, index: number, field: "qty" | "value") => {
    const k = generalKey(skuId, index);
    const cur = errors[k];
    if (!cur) return;
    delete cur[field];
    if (!cur.qty && !cur.value) delete errors[k];
};
const clearGradeError = (skuId: number, gradeId: number, index: number, field: "qty" | "value") => {
    const k = gradeKey(skuId, gradeId, index);
    const cur = errors[k];
    if (!cur) return;
    delete cur[field];
    if (!cur.qty && !cur.value) delete errors[k];
};

// 阶梯组校验（isEmpty / validateLadderGroup）抽到 ./ladderValidate，与批量设置抽屉共用，保证规则一致

/** 工厂：返回一个绑定到具体 key 前缀的 merge 函数（同行 qty/value 各自独立保留与覆盖） */
const makeMerger = (keyOf: (i: number) => string) => (i: number, patch: LadderError) => {
    const k = keyOf(i);
    errors[k] = { ...errors[k], ...patch };
};

/**
 * 收集全表错误；返回 true=校验通过
 * @param silent true=不弹 toast、不自动展开 SKU、不切 Tab（用于失焦时的"安静校验"）
 */
const validateForm = (silent = false): boolean => {
    Object.keys(errors).forEach((k) => delete errors[k]);
    if (form.skus.length === 0) {
        if (!silent) message.error(t('该商品无可定价规格'));
        return false;
    }
    // 首个错误：定位（展开/切 tab）+ 完整文案（toast）+ DOM key（滚动定位）
    let firstErr: { skuIdx: number; tab: "general" | "grade"; message: string; key: string } | null = null;
    const markFirst = (skuIdx: number, tab: "general" | "grade", msg: string, key: string) => {
        if (!firstErr) firstErr = { skuIdx, tab, message: msg, key };
    };
    form.skus.forEach((sku, skuIdx) => {
        // 多规格才带规格名前缀，单规格「统一规格」不啰嗦
        const skuLabel = form.isMultiSpec ? t('规格「{skuName}」', { skuName: sku.skuName }) : "";
        const hasGrade = hasConfiguredGradeLadders(sku.gradeLadders);
        const generalEmpty = isLadderGroupEmpty(sku.generalLadders);
        // 通用售价与类型售价至少配置一项；已配类型售价时通用售价可留空
        if (!hasGrade && generalEmpty) {
            errors[generalKey(sku.skuId, 0)] = { qty: t('请配置通用售价或类型售价') };
            markFirst(skuIdx, "general", t('{skuLabel}请配置通用售价或类型售价', { skuLabel }), `${generalKey(sku.skuId, 0)}:qty`);
            return;
        }
        if (!generalEmpty) {
            const gMerger = makeMerger((i) => generalKey(sku.skuId, i));
            if (sku.generalLadders.length > 20) {
                // 段数上限：错误标在第一行，避免 20 个红框
                gMerger(0, { qty: t('阶梯不能超过 20 段') });
                markFirst(skuIdx, "general", t('{skuLabel}通用售价阶梯不能超过 20 段', { skuLabel }), `${generalKey(sku.skuId, 0)}:qty`);
            }
            const gErr = validateLadderGroup(sku.generalLadders, form.priceMode, sku.retailPrice, gMerger);
            if (gErr) markFirst(skuIdx, "general", t('{skuLabel}通用售价{msg}', { skuLabel, msg: gErr.msg }), `${generalKey(sku.skuId, gErr.index)}:${gErr.field}`);
        }
        // 类型售价可选；填了须合规
        sku.gradeLadders.forEach((g) => {
            if (g.ladders.length === 0) return;
            const merger = makeMerger((i) => gradeKey(sku.skuId, g.gradeId, i));
            if (g.ladders.length > 20) {
                merger(0, { qty: t('阶梯不能超过 20 段') });
                markFirst(skuIdx, "grade", t('{skuLabel}{gradeName}阶梯不能超过 20 段', { skuLabel, gradeName: g.gradeName }), `${gradeKey(sku.skuId, g.gradeId, 0)}:qty`);
            }
            const err = validateLadderGroup(g.ladders, form.priceMode, sku.retailPrice, merger);
            if (err) markFirst(skuIdx, "grade", `${skuLabel}${g.gradeName}${err.msg}`, `${gradeKey(sku.skuId, g.gradeId, err.index)}:${err.field}`);
        });
    });
    if (firstErr) {
        if (!silent) {
            // 自动展开首个有错 SKU 并切到对应 tab，确保红色提示可见（提交时才做，失焦时保持用户当前位置）
            const { skuIdx, tab, message: msg, key } = firstErr;
            const targetSku = form.skus[skuIdx];
            if (!activeSkuKeys.value.includes(targetSku.skuId)) {
                activeSkuKeys.value = [...activeSkuKeys.value, targetSku.skuId];
            }
            skuTabs[skuIdx] = tab;
            message.error(msg);
            // 等 collapse 展开动画 + tab 切换的 DOM 完成后再滚动（不能只 nextTick，el-collapse 有 ~300ms transition）
            scrollToFirstError(key);
        }
        return false;
    }
    return true;
};

/**
 * 把命中错误的输入框滚到视口中央 + 聚焦
 * 难点：错误可能位于「折叠 SKU 面板 + 非当前 Tab」里，el-collapse 有 ~300ms 高度动画。
 * 直接 scrollIntoView 容易被中间态坐标 + 浏览器自选滚动容器坑到。
 * 策略：
 *   1) rAF 轮询等元素具备可见布局（display 已撤、height > 0）
 *   2) 自己向上找到最近的可滚动祖先（这里是 .ant-drawer-body）
 *   3) 再轮询等 scroller.scrollHeight 连续 3 帧稳定 → collapse 动画结束
 *   4) 手动算 targetTop 并 clamp 到 [0, maxScrollTop]，scrollTo 平滑滚动
 *   5) 焦点给到 input；preventScroll 避免覆盖上面的 smooth 滚动
 */
const scrollToFirstError = async (key: string, timeoutMs = 1200) => {
    const sel = `[data-error-key="${key}"]`;
    const start = performance.now();
    const isLayoutReady = (el: HTMLElement | null) => !!el && el.offsetParent !== null && el.getBoundingClientRect().height > 0;
    let wrap = document.querySelector<HTMLElement>(sel);
    while (!isLayoutReady(wrap) && performance.now() - start < timeoutMs) {
        await new Promise<void>((r) => requestAnimationFrame(() => r()));
        wrap = document.querySelector<HTMLElement>(sel);
    }
    if (!isLayoutReady(wrap)) return;

    const scroller = findScrollableAncestor(wrap!);
    if (scroller) {
        // 等 collapse 高度动画跑完：scroller.scrollHeight 连续 3 帧稳定
        let lastSh = scroller.scrollHeight;
        let stable = 0;
        while (stable < 3 && performance.now() - start < timeoutMs) {
            await new Promise<void>((r) => requestAnimationFrame(() => r()));
            if (scroller.scrollHeight === lastSh) stable++;
            else {
                stable = 0;
                lastSh = scroller.scrollHeight;
            }
        }
        const wrapRect = wrap!.getBoundingClientRect();
        const scRect = scroller.getBoundingClientRect();
        const offset = wrapRect.top - scRect.top - (scroller.clientHeight - wrapRect.height) / 2;
        const maxScrollTop = scroller.scrollHeight - scroller.clientHeight;
        const targetTop = Math.max(0, Math.min(maxScrollTop, scroller.scrollTop + offset));
        scroller.scrollTo({ top: targetTop, behavior: "smooth" });
    } else {
        wrap!.scrollIntoView({ behavior: "smooth", block: "center" });
    }
    wrap!.querySelector<HTMLInputElement>("input")?.focus({ preventScroll: true });
};

/** 向上找最近的可滚动祖先（overflow-y 为 auto/scroll/overlay 且实际可滚动） */
const findScrollableAncestor = (el: HTMLElement): HTMLElement | null => {
    let cur: HTMLElement | null = el.parentElement;
    while (cur && cur !== document.body) {
        const oy = window.getComputedStyle(cur).overflowY;
        if ((oy === "auto" || oy === "scroll" || oy === "overlay") && cur.scrollHeight > cur.clientHeight) {
            return cur;
        }
        cur = cur.parentElement;
    }
    return null;
};

/** 表单态 → 扁平 priceRows */
const buildPriceRows = (): PriceRow[] => {
    const modeNum = form.priceMode === "discount" ? 1 : 2;
    return buildUniformPriceRows(form.skus.map((sku) => ({ ...sku, generalLadders: sku.generalLadders.filter((l) => !isLadderEmpty(l)) })), modeNum);
};

const onSubmit = async () => {
    if (!validateForm()) return;
    try {
        emit("update:confirmLoading", true);
        await saveWholesalePrice({
            productId: form.productId,
            multiSpecPriceMode: form.isMultiSpec && form.calcMode === "skuLevel" ? 2 : 1,
            priceRows: buildPriceRows()
        });
        message.success(t('保存成功'));
        emit("submitCallback", true);
    } catch (error: any) {
        // 5310 / 5312(含行号) / 5303 / 5304 由后端文案透传
        message.error(error.message || t('保存失败'));
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => onSubmit();

onMounted(async () => {
    try {
        gradeOptions.value = (await getGradeList({ status: 1, withUserCount: false })) || [];
    } catch (error: any) {
        message.error(error.message || t('等级加载失败'));
    }
    await loadDetail();
});

defineExpose({ onFormSubmit });
</script>
<style scoped lang="less">
.price-drawer {
    min-height: 400px;
}
.top-form {
    // label 内容 (含 tooltip 图标) 垂直居中
    .form-label {
        display: inline-flex;
        align-items: center;
    }
    .sku-form-item {
        :deep(.el-form-item__content) {
            display: block;
            line-height: normal;
            min-width: 0;
        }
        .multi-panel {
            width: 100%;
        }
    }
}
.tip-icon {
    color: #999;
    margin-left: 2px;
    vertical-align: middle;
    cursor: help;
}
.batch-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
}
.multi-panel {
    flex: 1;
    margin-top: 12px;
    :deep(.el-collapse) {
        border: none;
    }
    :deep(.el-collapse-item) {
        margin-bottom: 12px;
        border: none;
        background: transparent;
        &:last-child {
            margin-bottom: 0;
        }
    }
    :deep(.el-collapse-item__header) {
        background: #f7f8fa;
        padding-left: 20px;
        border-bottom: none;
        font-weight: 600;
        color: #303133;
        box-sizing: border-box;
        &.is-active {
            border-bottom: none;
        }
    }
    :deep(.el-collapse-item__wrap) {
        border-bottom: none;
        background: transparent;
    }
    :deep(.el-collapse-item__content) {
        padding: 12px 0 0 0;
    }
}
.custom-tabs {
    display: flex;
    gap: 4px;
    padding: 0 1px;
}
.custom-tab-item {
    height: 25px;
    line-height: 25px;
    padding: 0 16px;
    font-size: 12px;
    color: #303133;
    background: #fff;
    border: 1px solid #dcdfe6;
    border-bottom: none;
    border-radius: 4px 4px 0 0;
    cursor: pointer;
    box-sizing: border-box;
    transition: all 0.2s;
    user-select: none;
    &:hover {
        color: var(--tig-primary);
        border-color: var(--tig-primary);
    }
    &.active {
        color: var(--tig-primary);
        border-color: var(--tig-primary);
        background: rgba(20, 86, 240, 0.08);
        font-weight: 500;
    }
}
.custom-tab-content {
    margin-top: 0;
}
.sku-title {
    font-weight: 600;
    color: #303133;
}
.empty-tip {
    color: #909399;
    padding: 30px;
    text-align: center;
}
</style>
<style>
.tip-content {
    max-width: 360px;
    line-height: 20px;
}
.tip-content .tip-section + .tip-section {
    margin-top: 8px;
}
.tip-content .tip-title {
    font-weight: 600;
}
.tip-content .tip-divider {
    height: 1px;
    margin: 8px 0;
    background: rgba(0, 0, 0, 0.08);
}
</style>
