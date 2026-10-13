<template>
    <div class="menu-tree-editor">
        <div ref="previewAreaRef" class="preview-area">
            <PhonePreview
                ref="phonePreviewRef"
                mode="menu"
                :tree="tree"
                :selected-id="selectedId ?? ''"
                :error-id="errorId ?? ''"
                @select="onPreviewSelect"
                @add-root="onAddRoot"
                @add-child="onAddChildRequest"
            />

            <div ref="rootOpBarRef" class="op-bar op-bar--root" :class="{ 'is-visible': rootVisible }" :style="rootBarStyle">
                <span v-if="canMoveUp" class="op-btn" :title="$t('左移')" @click="onMove(-1)">
                    <el-icon><ArrowLeft /></el-icon>
                </span>
                <span class="op-btn is-danger" :title="$t('删除')" @click="onRemoveClick">
                    <el-icon><Delete /></el-icon>
                </span>
                <span v-if="canMoveDown" class="op-btn" :title="$t('右移')" @click="onMove(1)">
                    <el-icon><ArrowRight /></el-icon>
                </span>
            </div>

            <div ref="subOpBarRef" class="op-bar op-bar--sub" :class="{ 'is-visible': subVisible }" :style="subBarStyle">
                <span v-if="canMoveUp" class="op-btn" :title="$t('上移')" @click="onMove(-1)">
                    <el-icon><ArrowUp /></el-icon>
                </span>
                <span class="op-btn is-danger" :title="$t('删除')" @click="onRemoveClick">
                    <el-icon><Delete /></el-icon>
                </span>
                <span v-if="canMoveDown" class="op-btn" :title="$t('下移')" @click="onMove(1)">
                    <el-icon><ArrowDown /></el-icon>
                </span>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch, type CSSProperties } from "vue";
import { ElIcon, ElMessageBox } from "element-plus";
import { Modal } from "ant-design-vue";
import { ArrowDown, ArrowLeft, ArrowRight, ArrowUp, Delete } from "@element-plus/icons-vue";
import PhonePreview from "@/components/PhonePreview/index.vue";
import type { WxMenuNode, WxMenuTree } from "@/types/setting/wechat.d";
import { CHILD_MAX, ROOT_MAX, validateRule } from "../composables/useMenuValidators";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface Props {
    tree: WxMenuTree;
    selectedId: string | null;
    /** 校验失败定位到的节点 id，用于在预览区把选中框标红；null/"" 时无红框 */
    errorId?: string | null;
}

const props = defineProps<Props>();

const emit = defineEmits<{
    (e: "select", id: string): void;
    (e: "add-root"): void;
    (e: "add-child", parentId: string): void;
    (e: "remove", id: string): void;
    (e: "move", id: string, dir: -1 | 1): void;
}>();

interface Located {
    node: WxMenuNode;
    siblings: WxMenuNode[];
    index: number;
    level: 1 | 2;
}

const located = computed<Located | null>(() => {
    const id = props.selectedId;
    if (!id) return null;
    const buttons = props.tree.buttons ?? [];
    for (let i = 0; i < buttons.length; i++) {
        const root = buttons[i];
        if (root.id === id) {
            return { node: root, siblings: buttons, index: i, level: 1 };
        }
        const children = root.children ?? [];
        for (let j = 0; j < children.length; j++) {
            if (children[j].id === id) {
                return { node: children[j], siblings: children, index: j, level: 2 };
            }
        }
    }
    return null;
});

const selectedNode = computed<WxMenuNode | null>(() => located.value?.node ?? null);
const selectedLevel = computed<1 | 2 | null>(() => located.value?.level ?? null);

const canMoveUp = computed(() => !!located.value && located.value.index > 0);
const canMoveDown = computed(() => !!located.value && located.value.index < located.value.siblings.length - 1);

const previewAreaRef = ref<HTMLElement | null>(null);
const phonePreviewRef = ref<InstanceType<typeof PhonePreview> | null>(null);
const rootOpBarRef = ref<HTMLElement | null>(null);
const subOpBarRef = ref<HTMLElement | null>(null);

/** 胶囊到锚点的间距（px） */
const ROOT_BAR_GAP = 12;
const SUB_BAR_GAP = 10;

/**
 * pos 与 visible 解耦：
 * 删除节点 / 失去选中 / DOM 销毁时，仅把 visible 切 false（CSS 隐藏胶囊），
 * 但保留 pos 不变 —— reference 元素位置稳定，popconfirm 的 popper 才不会被带跑到 (0,0)。
 */
const rootBarPos = ref<{ left: number; top: number }>({ left: 0, top: 0 });
const subBarPos = ref<{ left: number; top: number }>({ left: 0, top: 0 });
const rootVisible = ref(false);
const subVisible = ref(false);

const rootBarStyle = computed<CSSProperties>(() => ({
    left: `${rootBarPos.value.left}px`,
    top: `${rootBarPos.value.top}px`
}));

const subBarStyle = computed<CSSProperties>(() => ({
    left: `${subBarPos.value.left}px`,
    top: `${subBarPos.value.top}px`
}));

/**
 * 将菜单树关键信息序列化为指纹字符串，用于 watch 触发位置重算。
 * 覆盖：一级数量/顺序/id、二级数量/顺序/id。
 */
function treeFingerprint(): string {
    const buttons = props.tree.buttons ?? [];
    return buttons
        .map((b) => {
            const cs = b.children ?? [];
            return `${b.id}[${cs.map((c) => c.id).join(",")}]`;
        })
        .join("|");
}

/**
 * 根据当前选中节点，计算浮动操作胶囊在 preview-area 坐标系下的目标位置。
 * 一级菜单 → 以被选中格为锚点，水平居中对齐、位于其下方（+ROOT_BAR_GAP）
 * 二级菜单 → 以被选中子项为锚点，垂直居中对齐、位于其右侧（+SUB_BAR_GAP）
 */
function updatePositions() {
    const area = previewAreaRef.value;
    const preview = phonePreviewRef.value;
    if (!area || !preview) {
        rootVisible.value = false;
        subVisible.value = false;
        return;
    }

    const id = props.selectedId;
    const level = selectedLevel.value;
    if (!id || !level) {
        rootVisible.value = false;
        subVisible.value = false;
        return;
    }

    const el = preview.getNodeEl(id);
    if (!el) {
        // DOM 尚未挂载或已销毁：仅隐藏胶囊，pos 保留上次值，
        // 防止 reference 元素瞬移到 (0,0) 把 popconfirm 的 popper 带跑
        rootVisible.value = false;
        subVisible.value = false;
        return;
    }

    const elRect = el.getBoundingClientRect();
    const areaRect = area.getBoundingClientRect();

    if (level === 1) {
        rootBarPos.value = {
            left: elRect.left + elRect.width / 2 - areaRect.left,
            top: elRect.bottom - areaRect.top + ROOT_BAR_GAP
        };
        rootVisible.value = true;
        subVisible.value = false;
    } else {
        subBarPos.value = {
            left: elRect.right - areaRect.left + SUB_BAR_GAP,
            top: elRect.top + elRect.height / 2 - areaRect.top
        };
        subVisible.value = true;
        rootVisible.value = false;
    }
}

watch(
    [() => props.selectedId, treeFingerprint],
    () => {
        nextTick(updatePositions);
    },
    { immediate: true, flush: "post" }
);

let resizeObserver: ResizeObserver | null = null;
let rafId: number | null = null;

function scheduleUpdate() {
    if (rafId !== null) cancelAnimationFrame(rafId);
    rafId = requestAnimationFrame(() => {
        rafId = null;
        updatePositions();
    });
}

onMounted(() => {
    nextTick(updatePositions);
    window.addEventListener("resize", scheduleUpdate);
    if (typeof ResizeObserver !== "undefined" && previewAreaRef.value) {
        resizeObserver = new ResizeObserver(() => scheduleUpdate());
        resizeObserver.observe(previewAreaRef.value);
    }
});

onBeforeUnmount(() => {
    window.removeEventListener("resize", scheduleUpdate);
    if (rafId !== null) cancelAnimationFrame(rafId);
    resizeObserver?.disconnect();
    resizeObserver = null;
});

function onPreviewSelect(id: string) {
    emit("select", id);
}

function onAddRoot() {
    emit("add-root");
}

function onMove(dir: -1 | 1) {
    if (!selectedNode.value) return;
    emit("move", selectedNode.value.id, dir);
}

function onRemoveClick() {
    if (!selectedNode.value || !selectedLevel.value) return;
    const id = selectedNode.value.id;
    const displayName = selectedNode.value.name?.trim() || t('该菜单');
    Modal.confirm({
        title: t('删除后，{displayName}菜单下设置的内容全部将被删除', { displayName }),
        okText: t('确定'),
        okType: "danger",
        cancelText: t('取消'),
        onOk() {
            emit("remove", id);
        }
    });
}

/**
 * 子菜单添加拦截：
 * 当父级一级菜单当前仍为叶子（无 children）且 rule 已通过有效性校验时，
 * addChild 会清掉 parent.rule —— 向用户弹确认框再继续。
 */
async function onAddChildRequest(parentId: string) {
    const buttons = props.tree.buttons ?? [];
    const parent = buttons.find((b) => b.id === parentId);
    if (!parent) return;

    const isLeaf = !parent.children || parent.children.length === 0;
    const hasValidRule = isLeaf && parent.rule && validateRule(parent.rule) === null;

    if (hasValidRule) {
        try {
            await ElMessageBox.confirm(t('添加子菜单后，当前一级菜单的跳转规则将不再生效，确定添加？'), t('提示'), {
                confirmButtonText: t('确定添加'),
                cancelButtonText: t('取消'),
                type: "warning"
            });
        } catch {
            return;
        }
    }

    emit("add-child", parentId);
}
</script>

<style lang="less" scoped>
.menu-tree-editor {
    background: #fff;
    border-radius: 4px;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.preview-area {
    position: relative;
    display: flex;
    justify-content: center;
    min-height: 580px;
}

.op-bar {
    position: absolute;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px;
    background: #fff;
    border-radius: 22px;
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
    border: 1px solid #eceef1;
    opacity: 0;
    visibility: hidden;
    pointer-events: none;
    z-index: 5;

    &--root {
        transform: translateX(-50%);
    }

    &--sub {
        flex-direction: column;
        // padding: 10px 5px;
        transform: translateY(-50%);
    }

    &.is-visible {
        opacity: 1;
        visibility: visible;
        pointer-events: auto;
    }
}

.op-btn {
    width: 28px;
    height: 28px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    color: #555;
    cursor: pointer;
    font-size: 16px;

    &:hover {
        background: #f2f3f5;
        color: #222;
    }

    &.is-danger:hover {
        background: #ffeaea;
        color: #f56c6c;
    }
}
</style>
