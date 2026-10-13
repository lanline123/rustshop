<!--
 * @file 商品编辑页-一键批量翻译抽屉
 * @description
 *   把商品所有可翻译文字字段（名称/关键词/描述、属性/规格/附加服务、商品详情富文本）
 *   按分组以「原文·译文」两栏列出，支持选择目标语言、一键/逐字段 AI 翻译、编辑并一次性保存。
 *   仅跨境版（isOverseas）+ 编辑态使用。移植自 Vort-Admin BatchTranslateDialog 并适配 Tigshop 组件库与接口。
-->
<template>
    <div class="batch-translate" v-loading="loading">
        <!-- ============ 顶部：sticky 操作区 ============ -->
        <div class="batch-translate__header">
            <div class="batch-translate__toolbar">
                <!-- 源语言（固定简体中文） -->
                <div class="batch-translate__source-lang">
                    <span class="batch-translate__source-lang-text">简体中文(自动检测)</span>
                    <el-icon class="batch-translate__source-lang-caret"><ArrowDown /></el-icon>
                </div>

                <SvgIcon class="batch-translate__swap" name="multilingual-batch-translate" width="16" height="16" />

                <!-- 目标语言：点击切换当前编辑语言，× 移除 -->
                <div class="batch-translate__langs">
                    <div
                        v-for="loc in selectedLocales"
                        :key="loc.id"
                        class="batch-translate__lang"
                        :class="{
                            'batch-translate__lang--active': activeLocaleId === String(loc.id),
                            'batch-translate__lang--done': isLocaleTranslated(String(loc.id))
                        }"
                        @click="onLangTabClick(String(loc.id))"
                    >
                        <span class="batch-translate__lang-dot"></span>
                        <span class="batch-translate__lang-text">{{ loc.language }}</span>
                        <el-icon
                            v-if="selectedLocales.length > 1"
                            class="batch-translate__lang-close"
                            @click.stop="removeLocale(String(loc.id))"
                        >
                            <Close />
                        </el-icon>
                    </div>

                    <!-- + 添加语言 -->
                    <el-dropdown v-if="unselectedLocales.length" trigger="hover" placement="bottom">
                        <span class="batch-translate__lang-add-entry">+ 添加语言</span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item
                                    v-for="loc in unselectedLocales"
                                    :key="`add-${loc.id}`"
                                    @click="addLocale(String(loc.id))"
                                >
                                    + {{ loc.language }}
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>

                <span v-if="translating" class="batch-translate__progress">
                    <SvgIcon class="batch-translate__progress-spinner" name="multilingual-retranslate" width="14" height="14" />
                    翻译中{{ autoProgress.done }}/{{ autoProgress.total }}
                </span>

                <button type="button" class="batch-translate__action-btn" :disabled="translating" @click="handleAutoTranslateAll">
                    <img :src="aiIcon" class="batch-translate__action-btn-icon" alt="AI智能翻译" />
                    <span>一键翻译全部</span>
                </button>
            </div>
        </div>

        <!-- ============ 字段分区 ============ -->
        <div class="batch-translate__body">
            <template v-for="group in groupedFields" :key="group.key">
                <div v-if="isGroupVisible(group)" class="batch-translate__section">
                    <div class="batch-translate__section-title">
                        <span class="title-bar"></span>
                        {{ group.title }}
                    </div>

                    <!-- 商品详情：按 descItems 顺序混排（文本可编辑、图片/视频原样保留） -->
                    <div v-if="group.key === 'desc'" class="batch-translate__field-row">
                        <div class="batch-translate__field-label">商品详情</div>
                        <div class="batch-translate__field-pair">
                            <!-- 原文 -->
                            <div class="batch-translate__lang-col">
                                <div class="batch-translate__lang-tip">简体中文(原文)</div>
                                <div class="batch-translate__lang-box batch-translate__lang-box--source batch-translate__lang-box--desc">
                                    <template v-for="(item, idx) in descItems" :key="`src-${idx}`">
                                        <div
                                            v-if="(item.type === 'text' || item.type === 'html') && item.html"
                                            class="batch-translate__desc-text"
                                            v-html="item.html"
                                        />
                                        <div v-else-if="item.type === 'pic' && item.pic" class="batch-translate__desc-pic">
                                            <img :src="item.pic" alt="" />
                                        </div>
                                        <div v-else-if="item.type === 'video' && (item.videoUrl || item.cover)" class="batch-translate__desc-video">
                                            <video
                                                v-if="item.videoUrl"
                                                :src="item.videoUrl"
                                                :poster="item.cover || undefined"
                                                controls
                                                preload="metadata"
                                            />
                                            <img v-else-if="item.cover" :src="item.cover" alt="" />
                                        </div>
                                    </template>
                                </div>
                            </div>

                            <div class="batch-translate__swap-icon" aria-hidden="true">
                                <SvgIcon name="multilingual-batch-translate" width="20" height="20" />
                            </div>

                            <!-- 译文 -->
                            <div class="batch-translate__lang-col">
                                <div class="batch-translate__lang-tip">{{ activeTargetLangTip }}</div>
                                <div class="batch-translate__lang-box batch-translate__lang-box--target batch-translate__lang-box--desc">
                                    <template v-if="!activeLocaleId">
                                        <span class="batch-translate__placeholder">请先选择目标语言</span>
                                    </template>
                                    <template v-else>
                                        <template v-for="(item, idx) in descItems" :key="`tgt-${idx}`">
                                            <template v-if="(item.type === 'text' || item.type === 'html') && item.rowId && getDescRow(item.rowId)">
                                                <div
                                                    :key="`${item.rowId}-${activeLocaleId}`"
                                                    class="batch-translate__desc-html"
                                                    contenteditable="true"
                                                    spellcheck="false"
                                                    :ref="(el) => bindDescEditableRef(el, item.rowId!)"
                                                    @input="(e) => onDescEditableInput(e, item.rowId!)"
                                                />
                                            </template>
                                            <div v-else-if="item.type === 'pic' && item.pic" class="batch-translate__desc-pic">
                                                <img :src="item.pic" alt="" />
                                            </div>
                                            <div v-else-if="item.type === 'video' && (item.videoUrl || item.cover)" class="batch-translate__desc-video">
                                                <video
                                                    v-if="item.videoUrl"
                                                    :src="item.videoUrl"
                                                    :poster="item.cover || undefined"
                                                    controls
                                                    preload="metadata"
                                                />
                                                <img v-else-if="item.cover" :src="item.cover" alt="" />
                                            </div>
                                        </template>
                                    </template>
                                    <a
                                        v-if="activeLocaleId && group.fields.length"
                                        class="batch-translate__retranslate"
                                        :class="{ 'batch-translate__retranslate--disabled': descRetranslating }"
                                        @click="handleRetranslateDesc"
                                    >
                                        <SvgIcon class="batch-translate__retranslate-icon" name="multilingual-retranslate" width="14" height="14" />
                                        {{ descRetranslating ? "翻译中" : "重新翻译" }}
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 其他分组：通用字段行；超过阈值时默认收起 -->
                    <div
                        v-for="field in getVisibleFields(group)"
                        :key="field.uniqId"
                        class="batch-translate__field-row"
                        :class="{ 'batch-translate__field-row--indent': field.indent }"
                    >
                        <div class="batch-translate__field-label">{{ field.label }}</div>

                        <div class="batch-translate__field-pair">
                            <!-- 原文（只读） -->
                            <div class="batch-translate__lang-col">
                                <div class="batch-translate__lang-tip">简体中文(原文)</div>
                                <div class="batch-translate__lang-box batch-translate__lang-box--source">
                                    <div class="batch-translate__source-text">{{ field.translationName || "--" }}</div>
                                </div>
                            </div>

                            <div class="batch-translate__swap-icon" aria-hidden="true">
                                <SvgIcon name="multilingual-batch-translate" width="20" height="20" />
                            </div>

                            <!-- 译文（可编辑） -->
                            <div class="batch-translate__lang-col">
                                <div class="batch-translate__lang-tip">{{ activeTargetLangTip }}</div>
                                <div class="batch-translate__lang-box batch-translate__lang-box--target">
                                    <template v-if="!activeLocaleId">
                                        <span class="batch-translate__placeholder">请先选择目标语言</span>
                                    </template>
                                    <template v-else>
                                        <textarea
                                            v-model="field.translations[activeLocaleId]"
                                            class="batch-translate__target-input"
                                            rows="1"
                                            placeholder="请输入翻译结果"
                                            :ref="(el) => bindAutosizeTextarea(el)"
                                            @input="onAutosizeInput"
                                        />
                                    </template>
                                    <a
                                        v-if="activeLocaleId"
                                        class="batch-translate__retranslate"
                                        :class="{ 'batch-translate__retranslate--disabled': !!field.retranslating }"
                                        @click="handleRetranslate(field)"
                                    >
                                        <SvgIcon class="batch-translate__retranslate-icon" name="multilingual-retranslate" width="14" height="14" />
                                        {{ field.retranslating ? "翻译中" : "重新翻译" }}
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 字段超过阈值时的展开/收起 -->
                    <div
                        v-if="group.key !== 'desc' && group.fields.length > GROUP_COLLAPSE_THRESHOLD"
                        class="batch-translate__group-toggle-wrap"
                        @click="toggleGroup(group.key)"
                    >
                        <span class="batch-translate__group-toggle">
                            <span>{{ expandedGroups[group.key] ? "收起" : "展开" }}</span>
                            <i
                                class="batch-translate__group-toggle-caret"
                                :class="{ 'batch-translate__group-toggle-caret--up': expandedGroups[group.key] }"
                            />
                        </span>
                    </div>
                </div>
            </template>

            <div v-if="!loading && hasNoField" class="batch-translate__empty">暂无可翻译内容</div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, reactive, nextTick, watch, type ComponentPublicInstance } from "vue";
import { message } from "ant-design-vue";
import { ArrowDown, Close } from "@element-plus/icons-vue";
import aiIcon from "@/assets/ai-image/ai-icon.png";
import { getProduct } from "@/api/product/product";
import { getLocalesList } from "@/api/multilingual/languagesList";
import { updateTranslation, batchCreateTranslations } from "@/api/multilingual/translationContent";
import { getCreateTranslation } from "@/api/multilingual/currencyManagement";

defineOptions({ name: "ProductBatchTranslateDialog" });

const props = defineProps({
    /** 商品 ID */
    id: { type: [Number, String], default: 0 },
    isDialog: { type: Boolean, default: false }
});
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "closeConfirm"]);

// ===================== 类型 =====================
type FieldGroupKey = "basic" | "spec" | "attr" | "extra" | "desc";

interface LocaleItem {
    id: number | string;
    localeCode?: string;
    language?: string;
}

interface FieldRow {
    uniqId: string;
    groupKey: FieldGroupKey;
    label: string;
    kind: "text" | "editor";
    indent: boolean;
    /** 数据库 data_type（与商品表单内 BusinessData 一致：2 基本信息 / 8 属性规格 / 9 详情富文本） */
    dataType: number;
    /** 待翻译原文（保留原始字符串，作为原文签名） */
    translationName: string;
    /** 各目标语言译文，key = localeId */
    translations: Record<string, string>;
    retranslating?: boolean;
}

interface DescItem {
    type?: string;
    html?: string;
    pic?: string;
    videoUrl?: string;
    cover?: string;
    rowId?: string;
}

// ===================== 状态 =====================
const loading = ref(true);
const translating = ref(false);
const saving = ref(false);
const allLocales = ref<LocaleItem[]>([]);
const selectedLocaleIds = ref<string[]>([]);
const activeLocaleId = ref<string>("");
const fieldRows = ref<FieldRow[]>([]);
const descItems = ref<DescItem[]>([]);
const autoProgress = reactive({ done: 0, total: 0 });

/** 存在跨分组共享译文的行（同名原文），编辑时需要同步刷新孪生输入框的高度 */
const hasSharedRows = ref(false);

const GROUP_COLLAPSE_THRESHOLD = 8;
const expandedGroups = reactive<Record<string, boolean>>({});

// ===================== 计算属性 =====================
const productId = computed(() => String(props.id || ""));

const selectedLocales = computed<LocaleItem[]>(() =>
    allLocales.value.filter((loc) => loc.id && selectedLocaleIds.value.includes(String(loc.id)))
);
const unselectedLocales = computed<LocaleItem[]>(() =>
    allLocales.value.filter((loc) => loc.id && !selectedLocaleIds.value.includes(String(loc.id)))
);

const groupedFields = computed(() => {
    const groups: Array<{ key: FieldGroupKey; title: string; fields: FieldRow[] }> = [
        { key: "basic", title: "基本信息", fields: [] },
        { key: "spec", title: "商品规格", fields: [] },
        { key: "attr", title: "商品属性", fields: [] },
        { key: "extra", title: "附加服务", fields: [] },
        { key: "desc", title: "商品详情", fields: [] }
    ];
    for (const row of fieldRows.value) {
        const target = groups.find((g) => g.key === row.groupKey);
        if (target) target.fields.push(row);
    }
    return groups;
});

const hasNoField = computed(() => fieldRows.value.length === 0 && descItems.value.length === 0);

const activeTargetLangTip = computed(() => {
    const locale = selectedLocales.value.find((l) => String(l.id) === activeLocaleId.value);
    return `${locale?.language || ""}(译文)`;
});

const isGroupVisible = (group: { key: FieldGroupKey; fields: FieldRow[] }) => {
    if (group.key === "desc") return descItems.value.length > 0;
    return group.fields.length > 0;
};

const toggleGroup = (key: FieldGroupKey) => {
    expandedGroups[key] = !expandedGroups[key];
};
const getVisibleFields = (group: { key: FieldGroupKey; fields: FieldRow[] }): FieldRow[] => {
    if (group.key === "desc") return [];
    if (group.fields.length <= GROUP_COLLAPSE_THRESHOLD) return group.fields;
    return expandedGroups[group.key] ? group.fields : group.fields.slice(0, GROUP_COLLAPSE_THRESHOLD);
};

const onLangTabClick = (localeId: string) => {
    activeLocaleId.value = localeId;
};

// ===================== 初始化 =====================
onMounted(() => {
    init();
});

const getErrorMessage = (error: unknown, fallback: string): string => {
    const msg = (error as { message?: string })?.message;
    return msg || fallback;
};

const init = async () => {
    if (!productId.value) {
        message.warning("缺少商品 ID");
        emit("close");
        return;
    }
    loading.value = true;
    try {
        const [productDetail, localesResult] = await Promise.all([
            getProduct("detail", { id: productId.value }),
            getLocalesList({ isEnabled: 1, size: -1 })
        ]);

        const records = ((localesResult as { records?: LocaleItem[] })?.records || []).filter((loc) => !!loc.id);
        allLocales.value = records;
        selectedLocaleIds.value = records.map((loc) => String(loc.id));
        activeLocaleId.value = selectedLocaleIds.value[0] ?? "";

        fieldRows.value = buildFieldRows(productDetail as unknown as Record<string, unknown>);
        await loadExistingTranslations();
    } catch (error: unknown) {
        message.error(getErrorMessage(error, "加载商品详情失败"));
        emit("close");
    } finally {
        loading.value = false;
    }
};

// ===================== 字段扁平化 =====================
/** 译文在后端的唯一键：同一 (dataType, translationName) 对应同一条译文记录 */
const translationStoreKey = (row: Pick<FieldRow, "dataType" | "translationName">): string => `${row.dataType}|${row.translationName}`;

const buildFieldRows = (detail: Record<string, unknown>): FieldRow[] => {
    const rows: FieldRow[] = [];
    let counter = 0;
    const push = (groupKey: FieldGroupKey, label: string, dataType: number, value: string | undefined | null, indent = false) => {
        // 判空用 trim；translationName 保留原始字符串（含首尾空白），与字段级 BusinessData 落库的原文签名一致
        const raw = (value ?? "").toString();
        if (!raw.trim()) return;
        rows.push({
            uniqId: `row-${counter++}`,
            groupKey,
            label,
            kind: "text",
            indent,
            dataType,
            translationName: raw,
            translations: {}
        });
    };

    // ---- 基本信息（dataType=2） ----
    push("basic", "商品名称", 2, detail.productName as string);
    push("basic", "搜索关键词", 2, detail.keywords as string);
    push("basic", "商品描述", 2, detail.productBrief as string);

    // ---- 属性 / 规格 / 附加服务（dataType=8） ----
    const attrList = (detail.attrList || {}) as {
        normal?: Array<{ attrName?: string; attrList?: Array<{ attrValue?: string }> }>;
        spe?: Array<{ attrName?: string; attrList?: Array<{ attrValue?: string }> }>;
        extra?: Array<{ attrName?: string; attrList?: Array<{ attrValue?: string }> }>;
    };

    (attrList.normal || []).forEach((group) => {
        push("attr", "属性名", 8, group.attrName);
        (group.attrList || []).forEach((item) => push("attr", "属性值", 8, item.attrValue, true));
    });
    (attrList.spe || []).forEach((group) => {
        push("spec", "规格名", 8, group.attrName);
        (group.attrList || []).forEach((item) => push("spec", "规格值", 8, item.attrValue, true));
    });
    (attrList.extra || []).forEach((group) => {
        push("extra", "附加服务名", 8, group.attrName);
        (group.attrList || []).forEach((item) => push("extra", "附加服务值", 8, item.attrValue, true));
    });

    // ---- 商品详情（dataType=9，文本+图片+视频混排；只翻译文本段） ----
    const descArr = (detail.productDescArr || []) as Array<{
        type?: string;
        pic?: string;
        html?: string;
        videoUrl?: string;
        cover?: string;
    }>;
    const descItemsLocal: DescItem[] = [];
    descArr.forEach((item) => {
        if ((item.type === "text" || item.type === "html") && item.html) {
            const rawHtml = (item.html ?? "").toString();
            if (!rawHtml.trim()) {
                descItemsLocal.push({ type: item.type, html: item.html });
                return;
            }
            const uniqId = `row-${counter++}`;
            rows.push({
                uniqId,
                groupKey: "desc",
                label: "商品详情",
                kind: "editor",
                indent: false,
                dataType: 9,
                translationName: rawHtml,
                translations: {}
            });
            descItemsLocal.push({ type: item.type, html: item.html, rowId: uniqId });
        } else if (item.type === "pic" && item.pic) {
            descItemsLocal.push({ type: "pic", pic: item.pic });
        } else if (item.type === "video" && (item.videoUrl || item.cover)) {
            descItemsLocal.push({ type: "video", videoUrl: item.videoUrl, cover: item.cover });
        }
    });
    descItems.value = descItemsLocal;

    // 去重：组内同一 (dataType, translationName) 只保留一条；desc 段落保持顺序不去重。
    // 跨分组的同名原文（如属性名与规格名都叫「颜色」）两处都要展示，但后端按
    // (dataType, translationName) 唯一存储，所以复用同一份 translations 引用保证译文联动。
    const seenInGroup = new Set<string>();
    const translationStore = new Map<string, Record<string, string>>();
    const result: FieldRow[] = [];
    rows.forEach((r) => {
        if (r.groupKey === "desc") {
            result.push(r);
            return;
        }
        const groupKey = `${r.groupKey}|${r.dataType}|${r.translationName}`;
        if (seenInGroup.has(groupKey)) return;
        seenInGroup.add(groupKey);

        const storeKey = translationStoreKey(r);
        const shared = translationStore.get(storeKey);
        if (shared) {
            r.translations = shared;
            hasSharedRows.value = true;
        } else {
            translationStore.set(storeKey, r.translations);
        }
        result.push(r);
    });
    return result;
};

// ===================== 加载已有译文 =====================
const loadExistingTranslations = async () => {
    if (!fieldRows.value.length) return;
    // 跨分组同名原文共享译文，按唯一键取一行请求即可
    const uniqRows = new Map<string, FieldRow>();
    fieldRows.value.forEach((row) => {
        const key = translationStoreKey(row);
        if (!uniqRows.has(key)) uniqRows.set(key, row);
    });
    await Promise.all(
        Array.from(uniqRows.values()).map(async (row) => {
            try {
                const res = (await getCreateTranslation({
                    dataType: row.dataType,
                    translationName: row.translationName
                })) as { items?: Array<{ localeId?: number | string; translationValue?: string }> };
                (res.items || []).forEach((item) => {
                    if (item.localeId != null) {
                        row.translations[String(item.localeId)] = item.translationValue || "";
                    }
                });
            } catch {
                // 单条失败不影响其他字段
            }
        })
    );
    await syncDescEditableDom();
    await refreshAllAutosize();
};

// ===================== 目标语言 标签增删 =====================
const removeLocale = (localeId: string) => {
    if (translating.value) {
        message.warning("正在翻译中，请稍后操作");
        return;
    }
    if (selectedLocaleIds.value.length <= 1) {
        message.warning("至少保留一种目标语言");
        return;
    }
    selectedLocaleIds.value = selectedLocaleIds.value.filter((id) => id !== localeId);
    if (activeLocaleId.value === localeId) {
        activeLocaleId.value = selectedLocaleIds.value[0] ?? "";
    }
};

const addLocale = (localeId: string) => {
    if (translating.value) {
        message.warning("正在翻译中，请稍后操作");
        return;
    }
    if (!selectedLocaleIds.value.includes(localeId)) {
        selectedLocaleIds.value = [...selectedLocaleIds.value, localeId];
    }
    if (!activeLocaleId.value) {
        activeLocaleId.value = localeId;
    }
};

// ===================== 一键翻译全部（所有已选语言 × 所有空字段） =====================
const handleAutoTranslateAll = async () => {
    if (!selectedLocales.value.length) {
        message.warning("请先选择目标语言");
        return;
    }
    if (!fieldRows.value.length) {
        message.warning("暂无可翻译内容");
        return;
    }

    type Task = { row: FieldRow; locale: LocaleItem };
    const tasks: Task[] = [];
    // 跨分组同名原文共享译文，同一 (原文, 语言) 只需翻译一次
    const queued = new Set<string>();
    for (const locale of selectedLocales.value) {
        if (!locale.localeCode) continue;
        for (const row of fieldRows.value) {
            if (row.translations[String(locale.id)]?.trim()) continue;
            const taskKey = `${translationStoreKey(row)}|${locale.id}`;
            if (queued.has(taskKey)) continue;
            queued.add(taskKey);
            tasks.push({ row, locale });
        }
    }
    if (!tasks.length) {
        message.info("已选语言下所有字段均已翻译");
        return;
    }

    translating.value = true;
    autoProgress.done = 0;
    autoProgress.total = tasks.length;
    emit("closeConfirm", true, "正在翻译中，关闭后将中断当前翻译，是否确认关闭？");

    let failCount = 0;
    try {
        for (const task of tasks) {
            try {
                task.row.translations[String(task.locale.id)] = await translateRichTextRow(task.row, task.locale.localeCode!);
            } catch (err) {
                failCount += 1;
                console.warn("[BatchTranslateDialog] translate fail", err);
            } finally {
                autoProgress.done += 1;
            }
        }
        if (failCount > 0) {
            message.warning(`翻译完成：成功 ${tasks.length - failCount} 条，失败 ${failCount} 条`);
        } else {
            message.success("翻译完成");
        }
    } finally {
        translating.value = false;
        emit("closeConfirm", false);
        await syncDescEditableDom();
        await refreshAllAutosize();
    }
};

// ===================== 富文本纯文本互转 =====================
const htmlToPlainText = (html: string): string => {
    if (!html) return "";
    const tmp = document.createElement("div");
    tmp.innerHTML = html;
    return (tmp.textContent || tmp.innerText || "").replace(/[\r\n]+/g, "\n").trim();
};

const wrapPlainTextAsHtml = (plain: string): string => {
    if (!plain) return "";
    const cleaned = plain.replace(/<\s*\/?\s*[a-zA-Z][\w-]*\s*[^>]*>/g, "").trim();
    if (!cleaned) return "";
    return cleaned
        .split(/\n+/)
        .map((line) => `<p>${line.trim()}</p>`)
        .filter((line) => line !== "<p></p>")
        .join("");
};

/** 富文本字段（dataType=9）：传纯文本翻译 → 结果包回 HTML；普通字段直接翻译 */
const translateRichTextRow = async (row: FieldRow, localeCode: string): Promise<string> => {
    const plain = row.dataType === 9 ? htmlToPlainText(row.translationName) : row.translationName;
    const result = (await updateTranslation({ code: localeCode, text: plain })) as { translation?: string };
    const raw = result.translation || "";
    return row.dataType === 9 ? wrapPlainTextAsHtml(raw) : raw;
};

// ===================== 商品详情段落辅助 =====================
const descRetranslating = ref(false);
const descEditableRefs = new Map<string, HTMLDivElement>();

const getDescRow = (rowId: string | undefined): FieldRow | undefined => {
    if (!rowId) return undefined;
    return fieldRows.value.find((r) => r.uniqId === rowId);
};

const bindDescEditableRef = (el: Element | ComponentPublicInstance | null, rowId: string) => {
    if (el instanceof HTMLDivElement) {
        descEditableRefs.set(rowId, el);
        const row = getDescRow(rowId);
        if (row && activeLocaleId.value) {
            const html = row.translations[activeLocaleId.value] || "";
            if (el.innerHTML !== html) el.innerHTML = html;
        } else if (el.innerHTML) {
            el.innerHTML = "";
        }
    } else {
        descEditableRefs.delete(rowId);
    }
};

const onDescEditableInput = (e: Event, rowId: string) => {
    if (!activeLocaleId.value) return;
    const row = getDescRow(rowId);
    if (!row) return;
    const target = e.target as HTMLDivElement;
    row.translations[activeLocaleId.value] = target.innerHTML;
};

const syncDescEditableDom = async () => {
    await nextTick();
    descEditableRefs.forEach((el, rowId) => {
        if (document.activeElement === el) return;
        const row = getDescRow(rowId);
        if (!row) return;
        const html = row.translations[activeLocaleId.value] || "";
        if (el.innerHTML !== html) el.innerHTML = html;
    });
};

const handleRetranslateDesc = async () => {
    if (descRetranslating.value) return;
    if (!activeLocaleId.value) {
        message.warning("请先选择目标语言");
        return;
    }
    const locale = selectedLocales.value.find((l) => String(l.id) === activeLocaleId.value);
    if (!locale?.localeCode) return;
    const descRows = fieldRows.value.filter((r) => r.groupKey === "desc");
    if (!descRows.length) return;

    descRetranslating.value = true;
    let failCount = 0;
    try {
        for (const row of descRows) {
            try {
                row.translations[activeLocaleId.value] = await translateRichTextRow(row, locale.localeCode);
            } catch (err) {
                failCount += 1;
                console.warn("[BatchTranslateDialog] desc retranslate fail", err);
            }
        }
        if (failCount > 0) {
            message.warning(`翻译完成：失败 ${failCount} 条`);
        } else {
            message.success("翻译完成");
        }
    } finally {
        descRetranslating.value = false;
        await syncDescEditableDom();
    }
};

// ===================== 单条重新翻译 =====================
const handleRetranslate = async (row: FieldRow) => {
    if (!activeLocaleId.value) {
        message.warning("请先选择目标语言");
        return;
    }
    if (row.retranslating) return;
    const locale = selectedLocales.value.find((l) => String(l.id) === activeLocaleId.value);
    if (!locale?.localeCode) return;
    row.retranslating = true;
    try {
        row.translations[activeLocaleId.value] = await translateRichTextRow(row, locale.localeCode);
        await refreshAllAutosize();
    } catch (err) {
        message.error(getErrorMessage(err, "翻译失败"));
    } finally {
        row.retranslating = false;
    }
};

// ===================== textarea 自适应高度 =====================
const autosizeTextareas = new Set<HTMLTextAreaElement>();

const resizeTextarea = (el: HTMLTextAreaElement) => {
    el.style.height = "auto";
    el.style.height = `${el.scrollHeight}px`;
};

const bindAutosizeTextarea = (el: Element | ComponentPublicInstance | null) => {
    if (el instanceof HTMLTextAreaElement) {
        autosizeTextareas.add(el);
        nextTick(() => resizeTextarea(el));
    }
};

const onAutosizeInput = (e: Event) => {
    const el = e.target as HTMLTextAreaElement;
    if (el) resizeTextarea(el);
    if (hasSharedRows.value) refreshAllAutosize();
};

const refreshAllAutosize = async () => {
    await nextTick();
    autosizeTextareas.forEach((el) => {
        if (el.isConnected) resizeTextarea(el);
        else autosizeTextareas.delete(el);
    });
};

watch(activeLocaleId, () => {
    refreshAllAutosize();
    syncDescEditableDom();
});

// 判断某目标语言下所有可翻译字段是否都已有译文（tab 前小圆点变绿）
const isLocaleTranslated = (localeId: string): boolean => {
    if (!localeId || !fieldRows.value.length) return false;
    return fieldRows.value.every((row) => {
        const val = row.translations[localeId];
        if (typeof val !== "string") return false;
        const plain = row.dataType === 9 ? htmlToPlainText(val) : val;
        return plain.trim().length > 0;
    });
};

// ===================== 保存（仅已选目标语言，按 translationName+dataType 分组落库） =====================
const handleSave = async () => {
    if (saving.value) return;
    if (!selectedLocaleIds.value.length) {
        message.warning("请先选择目标语言");
        return;
    }
    const selectedSet = new Set(selectedLocaleIds.value);

    // 按 (dataType, translationName) 分组，组内聚合各语言译文
    const groupMap = new Map<string, { translationName: string; dataType: number; items: Array<{ localeId: string; translationValue: string }> }>();
    for (const row of fieldRows.value) {
        for (const [localeId, val] of Object.entries(row.translations)) {
            if (!localeId || !selectedSet.has(localeId)) continue;
            const value = val ?? "";
            if (!value.trim()) continue;
            const key = translationStoreKey(row);
            let group = groupMap.get(key);
            if (!group) {
                group = { translationName: row.translationName, dataType: row.dataType, items: [] };
                groupMap.set(key, group);
            }
            // 跨分组同名原文会命中同一个 group，同一语言只保留一条，避免 payload 出现重复 item
            if (group.items.some((item) => item.localeId === localeId)) continue;
            group.items.push({ localeId, translationValue: value });
        }
    }

    const payload = Array.from(groupMap.values()).filter((g) => g.items.length > 0);
    if (payload.length === 0) {
        message.warning("暂无可保存的翻译内容");
        return;
    }

    saving.value = true;
    emit("update:confirmLoading", true);
    try {
        const res = (await batchCreateTranslations(payload)) as { failCount?: number; successCount?: number };
        if (res?.failCount && res.failCount > 0) {
            message.warning(`保存完成：成功 ${res.successCount ?? 0} 条，失败 ${res.failCount} 条`);
        } else {
            message.success("操作成功");
        }
        emit("submitCallback");
    } catch (error: unknown) {
        message.error(getErrorMessage(error, "操作失败"));
    } finally {
        saving.value = false;
        emit("update:confirmLoading", false);
    }
};

defineExpose({ onFormSubmit: handleSave });
</script>

<style lang="less" scoped>
.batch-translate {
    --bt-primary: var(--el-color-primary, #155bd4);
    width: 100%;
    background: #fff;
    min-height: 100%;

    // 抽屉 body 的内边距已置零（调用方 bodyStyle），改由本组件承担：
    // sticky top:0 吸附的是滚动容器内容区上沿，容器若保留 padding，顶部会露出未被白底覆盖的穿透带
    &__header {
        position: sticky;
        top: 0;
        z-index: 10;
        background: #fff;
        padding: 40px 40px 16px;
    }

    &__toolbar {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 18px 14px;
        border-radius: 8px;
        background-color: #f0f4fd;
    }

    &__action-btn {
        flex-shrink: 0;
        display: inline-flex;
        align-items: center;
        gap: 6px;
        height: 32px;
        padding: 0 12px;
        background: transparent;
        border: 1px solid var(--bt-primary);
        border-radius: 6px;
        color: var(--bt-primary);
        font-size: 13px;
        line-height: 1;
        cursor: pointer;

        &:hover:not(:disabled) {
            background: #fff;
        }
        &:disabled {
            cursor: not-allowed;
            opacity: 0.6;
        }
    }

    &__action-btn-icon {
        width: 16px;
        height: 16px;
        display: block;
    }

    &__source-lang {
        flex-shrink: 0;
        display: inline-flex;
        align-items: center;
        gap: 10px;
        height: 32px;
        padding: 0 10px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        background-color: #fff;
        font-size: 13px;
        line-height: 1;
        color: rgba(0, 0, 0, 0.65);
    }

    &__source-lang-caret {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.3);
    }

    &__swap {
        flex-shrink: 0;
        color: rgba(0, 0, 0, 0.6);
    }

    &__langs {
        flex: 1;
        min-width: 0;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 10px;
    }

    // 目标语言：兼具「切换编辑语言」与「移除」两种操作
    &__lang {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        height: 32px;
        padding: 0 10px;
        border-radius: 4px;
        background-color: #f8fbff;
        font-size: 13px;
        line-height: 1;
        color: rgba(0, 0, 0, 0.45);
        cursor: pointer;
        user-select: none;
        transition:
            color 0.2s,
            background-color 0.2s;

        &:hover:not(.batch-translate__lang--active) {
            background-color: #fff;
        }

        &--done {
            color: rgba(0, 0, 0, 0.85);

            .batch-translate__lang-dot {
                background: #22c55e;
            }
        }

        &--active {
            background-color: var(--el-color-primary-light-9, #eef2fe);
            color: var(--bt-primary);
        }
    }

    &__lang-dot {
        display: inline-block;
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #c9cdd4;
        flex-shrink: 0;
    }

    &__lang-close {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.25);

        &:hover {
            color: rgba(0, 0, 0, 0.6);
        }
    }

    &__lang-add-entry {
        cursor: pointer;
        color: var(--bt-primary);
        font-size: 13px;
        line-height: 32px;

        &:hover {
            opacity: 0.8;
        }
    }

    &__progress {
        flex-shrink: 0;
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: rgba(0, 0, 0, 0.45);
    }

    &__progress-spinner {
        color: inherit;
        animation: batch-translate-spin 0.8s linear infinite;
    }

    &__body {
        padding: 16px 40px 40px;
    }

    &__section {
        background: #fff;
        border-radius: 4px;
        padding: 16px;
        margin-bottom: 16px;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
    }

    &__section-title {
        display: flex;
        align-items: center;
        font-size: 14px;
        font-weight: 600;
        background-color: #f5f7fa;
        padding: 12px 10px;
        margin-bottom: 20px;

        .title-bar {
            width: 3px;
            height: 14px;
            background-color: var(--bt-primary);
            margin-right: 5px;
        }
    }

    &__field-row {
        display: flex;
        align-items: flex-start;
        gap: 16px;
        margin-bottom: 16px;

        &--indent {
            .batch-translate__field-label {
                margin-left: 60px;
            }
            .batch-translate__field-pair > .batch-translate__lang-col:first-child {
                max-width: calc(50% - 60px);
            }
        }
    }

    &__field-label {
        flex: 0 0 65px;
        font-size: 13px;
        font-weight: 600;
        color: rgba(0, 0, 0, 0.85);
        line-height: 22px;
        text-align: right;
    }

    &__field-pair {
        flex: 1;
        min-width: 0;
        display: flex;
        align-items: stretch;
        gap: 12px;
    }

    &__lang-col {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
    }

    &__lang-tip {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.45);
        line-height: 22px;
        margin-bottom: 6px;
    }

    &__lang-box {
        position: relative;
        flex: 1;
        min-height: 80px;
        padding: 12px 16px 28px;
        border-radius: 6px;
        font-size: 13px;
        line-height: 1.6;
        color: rgba(0, 0, 0, 0.85);
        word-break: break-word;
        white-space: pre-wrap;

        &--source {
            background-color: #f5f7fa;
        }
        &--target {
            background-color: #fff;
            border: 1px solid #e5e6eb;
        }
    }

    &__swap-icon {
        flex: 0 0 24px;
        align-self: stretch;
        display: flex;
        align-items: center;
        justify-content: center;
        padding-top: 28px;
        color: rgba(0, 0, 0, 0.45);
    }

    &__target-input {
        width: 100%;
        min-height: 24px;
        border: none;
        outline: none;
        background: transparent;
        resize: none;
        overflow: hidden;
        font-size: 13px;
        line-height: 1.6;
        color: rgba(0, 0, 0, 0.85);
        font-family: inherit;
        padding: 0;
    }

    &__retranslate {
        position: absolute;
        right: 12px;
        bottom: 8px;
        font-size: 12px;
        color: rgba(0, 0, 0, 0.65);
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        gap: 4px;

        &:hover {
            color: var(--bt-primary);
        }
        &--disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
    }

    &__retranslate-icon {
        margin-right: 2px;
    }

    &__source-text {
        white-space: pre-wrap;
        color: rgba(0, 0, 0, 0.85);
        font-size: 13px;
        line-height: 1.6;
    }

    &__lang-box--desc {
        padding: 16px;
        min-height: 320px;
    }

    &__desc-text {
        font-size: 13px;
        line-height: 1.7;
        color: rgba(0, 0, 0, 0.85);
        white-space: pre-wrap;
        word-break: break-word;
        margin-bottom: 12px;

        :deep(p) {
            margin: 0 0 8px;
        }
        :deep(img) {
            max-width: 100%;
            display: block;
            margin: 8px 0;
            border-radius: 4px;
        }
    }

    &__desc-pic {
        margin: 8px 0;

        img {
            max-width: 100%;
            display: block;
            border-radius: 4px;
        }
    }

    &__desc-video {
        margin: 8px 0;

        video,
        img {
            max-width: 100%;
            display: block;
            border-radius: 4px;
            background: #000;
        }
    }

    &__desc-html {
        min-height: 64px;
        margin-bottom: 12px;
        padding: 8px 10px;
        font-size: 13px;
        line-height: 1.7;
        color: rgba(0, 0, 0, 0.85);
        word-break: break-word;
        outline: none;
        border: 1px dashed transparent;
        border-radius: 4px;
        transition: border-color 0.2s;

        &:hover {
            border-color: #e5e6eb;
        }
        &:focus {
            border-color: var(--bt-primary);
            background-color: #fff;
        }
        :deep(p) {
            margin: 0 0 8px;
        }
        :deep(img) {
            max-width: 100%;
            display: block;
            margin: 8px 0;
            border-radius: 4px;
        }
    }

    &__lang-box--desc.batch-translate__lang-box--target {
        padding-bottom: 36px;
    }

    &__placeholder {
        color: rgba(0, 0, 0, 0.35);
        font-size: 13px;
    }

    &__empty {
        text-align: center;
        padding: 60px 0;
        color: rgba(0, 0, 0, 0.45);
        font-size: 14px;
    }

    &__group-toggle-wrap {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: 8px;
        padding: 6px 0;
        cursor: pointer;
        user-select: none;
    }

    &__group-toggle {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 8px;
        font-size: 13px;
        color: var(--bt-primary);
        cursor: pointer;

        &:hover {
            opacity: 0.8;
        }
    }

    // 三角形箭头：用 border 画，天然继承文字色（currentColor）
    &__group-toggle-caret {
        display: inline-block;
        width: 0;
        height: 0;
        border-left: 4px solid transparent;
        border-right: 4px solid transparent;
        border-top: 5px solid currentColor;
        transition: transform 0.2s ease;

        &--up {
            transform: rotate(180deg);
        }
    }
}

@keyframes batch-translate-spin {
    to {
        transform: rotate(360deg);
    }
}
</style>
