<template>
    <div class="menu-preview">
        <div class="menu-bar">
            <div class="kbd-slot">
                <div class="kbd-icon">
                    <span class="kbd-triangle"></span>
                    <span class="kbd-dots"> <i></i><i></i><i></i><i></i> <i></i><i></i><i></i><i></i> </span>
                </div>
            </div>

            <div
                v-for="top in topThree"
                :key="top.id"
                :ref="(el) => setCellRef(top.id, el)"
                class="menu-cell"
                :class="{
                    'is-selected': isCellSelected(top),
                    'is-error': !!errorId && errorId === top.id
                }"
                @click.stop="onTopClick(top)"
            >
                <transition name="fade">
                    <div
                        v-if="expandedTopId === top.id"
                        class="sub-panel"
                        :class="subPanelTailClass"
                    >
                        <div
                            v-for="child in currentChildren"
                            :key="child.id"
                            :ref="(el) => setSubItemRef(child.id, el)"
                            class="sub-item"
                            :class="{
                                'is-selected': selectedId === child.id,
                                'is-error': !!errorId && errorId === child.id
                            }"
                            @click.stop="onSubClick(child)"
                        >
                            {{ child.name || $t('子菜单名...') }}
                        </div>
                        <div
                            v-if="canAddChild"
                            class="sub-item sub-item--add"
                            @click.stop="onAddChild"
                        >
                            <span class="plus">+</span>
                            <span>{{ $t('添加') }}</span>
                        </div>
                    </div>
                </transition>
                <span class="cell-text">{{ top.name || $t('菜单名称') }}</span>
            </div>

            <template v-if="topThree.length === 0">
                <div class="menu-cell menu-cell--add-empty" @click.stop="onAddRoot">
                    <i class="iconfont-admin icon-menu5 plus"></i>
                    <span class="add-empty-text">{{ $t('添加菜单') }}</span>
                </div>
            </template>
            <template v-else-if="topThree.length < ROOT_MAX">
                <div class="menu-cell menu-cell--add" @click.stop="onAddRoot">
                    <i class="iconfont-admin icon-menu5 plus"></i>
                </div>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { WxMenuTree, WxMenuNode } from "@/types/setting/wechat.d";

const ROOT_MAX = 3;
const CHILD_MAX = 5;

interface Props {
    tree: WxMenuTree;
    selectedId?: string;
    /** 校验失败节点 id；与 selectedId 匹配时把选中框改为红色 */
    errorId?: string;
}

const props = withDefaults(defineProps<Props>(), {
    selectedId: "",
    errorId: ""
});

const emit = defineEmits<{
    (e: "select", nodeId: string): void;
    (e: "add-root"): void;
    (e: "add-child", parentId: string): void;
}>();

const topThree = computed<WxMenuNode[]>(() => (props.tree?.buttons ?? []).slice(0, ROOT_MAX));

/**
 * 当前展开浮层的一级菜单 id：
 *   - selectedId 本身是某个一级 → 即该一级
 *   - selectedId 是某个二级 → 取其父一级
 *   - 否则 null（不展开）
 */
const expandedTopId = computed<string | null>(() => {
    const selId = props.selectedId;
    if (!selId) return null;
    for (const top of topThree.value) {
        if (top.id === selId) return top.id;
        if ((top.children ?? []).some((c) => c.id === selId)) return top.id;
    }
    return null;
});

const currentChildren = computed<WxMenuNode[]>(() => {
    if (!expandedTopId.value) return [];
    const top = topThree.value.find((it) => it.id === expandedTopId.value);
    return (top?.children ?? []).slice(0, CHILD_MAX);
});

const canAddChild = computed(() => currentChildren.value.length < CHILD_MAX);

/**
 * 子菜单浮层"视觉最后一项"对应的节点 id：
 *   - 若还能继续添加（最后一项是"添加"按钮），返回空，箭头保持默认色
 *   - 否则取 currentChildren 的最后一项
 * 用于让面板箭头与最后一个真实子菜单的选中/错误描边融合成一体气泡。
 */
const tailNodeId = computed<string>(() => {
    if (canAddChild.value) return "";
    const list = currentChildren.value;
    return list.length ? list[list.length - 1].id : "";
});

const subPanelTailClass = computed<string>(() => {
    const id = tailNodeId.value;
    if (!id) return "";
    if (props.errorId && props.errorId === id) return "tail-is-error";
    if (props.selectedId && props.selectedId === id) return "tail-is-selected";
    return "";
});

const cellEls = new Map<string, HTMLElement>();
const subItemEls = new Map<string, HTMLElement>();

function setCellRef(id: string, el: unknown) {
    if (el instanceof HTMLElement) cellEls.set(id, el);
    else cellEls.delete(id);
}

function setSubItemRef(id: string, el: unknown) {
    if (el instanceof HTMLElement) subItemEls.set(id, el);
    else subItemEls.delete(id);
}

/**
 * 按节点 id 返回其对应的 DOM 元素（一级菜单格或二级子菜单项）。
 * 供父组件（编辑器）锚定浮动操作胶囊定位。
 */
function getNodeEl(id: string): HTMLElement | null {
    if (!id) return null;
    return cellEls.get(id) ?? subItemEls.get(id) ?? null;
}

defineExpose({ getNodeEl });

function isCellSelected(top: WxMenuNode): boolean {
    return !!props.selectedId && props.selectedId === top.id;
}

function onTopClick(top: WxMenuNode) {
    emit("select", top.id);
}

function onSubClick(child: WxMenuNode) {
    emit("select", child.id);
}

function onAddRoot() {
    emit("add-root");
}

function onAddChild() {
    const parentId = expandedTopId.value;
    if (!parentId) return;
    emit("add-child", parentId);
}
</script>

<style lang="less" scoped>
.menu-preview {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
}

.menu-bar {
    height: 48px;
    background: #fafafa;
    border-top: 1px solid #ddd;
    display: flex;
    position: relative;
    z-index: 1;
    border-radius: 0 0 8px 8px;

    .kbd-slot {
        width: 40px;
        flex: 0 0 40px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .menu-cell {
        position: relative;
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        border-left: 1px solid #ddd;
        cursor: pointer;
        user-select: none;
        font-size: 13px;
        color: #333;
        padding: 0 4px;
        transition:
            color 120ms ease,
            box-shadow 120ms ease;

        &:first-of-type {
            border-left: none;
        }

        &.is-selected {
            color: #1aad19;
            background: #07c1600d;
            box-shadow: inset 0 0 0 1px #1aad19;
        }

        &.is-error {
            color: #f56c6c;
            background: transparent;
            box-shadow: inset 0 0 0 1px #f56c6c;
        }

        &--add {
            color: #999;
            font-size: 20px;
            line-height: 1;

            &:hover {
                color: #1aad19;
            }
        }

        &--add-empty {
            flex: 1;
            margin: 4px 6px 4px 0;
            border: 1px dashed #1aad19;
            border-radius: 3px;
            color: #1aad19;
            gap: 2px;
            font-size: 13px;
            background: transparent;

            .plus {
                font-size: 13px;
            }

            .add-empty-text {
                letter-spacing: 1px;
            }
        }

        .cell-text {
            max-width: 100%;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .plus {
            font-style: normal;
            line-height: 1;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }
    }
}

.kbd-icon {
    position: relative;
    width: 22px;
    height: 22px;
    border: 1px solid #bbb;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;

    .kbd-triangle {
        position: absolute;
        top: 3px;
        left: 50%;
        transform: translateX(-50%);
        width: 0;
        height: 0;
        border-left: 3px solid transparent;
        border-right: 3px solid transparent;
        border-bottom: 3px solid #888;
    }

    .kbd-dots {
        display: grid;
        grid-template-columns: repeat(4, 2px);
        gap: 1px;
        margin-top: 4px;

        i {
            width: 2px;
            height: 2px;
            background: #888;
            border-radius: 50%;
            display: block;
        }
    }
}

.sub-panel {
    position: absolute;
    left: 50%;
    bottom: calc(100% + 8px);
    transform: translateX(-50%);
    width: 100px;
    background: #fff;
    border: 1px solid #dadde1;
    border-radius: 4px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    z-index: 2;

    &::before,
    &::after {
        content: "";
        position: absolute;
        left: 50%;
        width: 0;
        height: 0;
        pointer-events: none;
    }

    &::before {
        bottom: -7px;
        transform: translateX(-50%);
        border-left: 7px solid transparent;
        border-right: 7px solid transparent;
        border-top: 7px solid #dadde1;
        z-index: 1;
    }

    &::after {
        bottom: -5px;
        transform: translateX(-50%);
        border-left: 6px solid transparent;
        border-right: 6px solid transparent;
        border-top: 6px solid #fff;
        z-index: 2;
    }

    &.tail-is-selected::before {
        border-top-color: #1aad19;
    }

    &.tail-is-error::before {
        border-top-color: #f56c6c;
    }

    .sub-item {
        position: relative;
        padding: 10px 12px;
        font-size: 13px;
        color: #333;
        border-top: 1px solid #f0f0f0;
        cursor: pointer;
        text-align: center;
        line-height: 1.3;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        transition:
            color 120ms ease,
            background-color 120ms ease;

        &:first-child {
            border-top: none;
            border-top-left-radius: 4px;
            border-top-right-radius: 4px;
        }

        &:last-child {
            border-bottom-left-radius: 4px;
            border-bottom-right-radius: 4px;
        }

        &.is-selected {
            color: #1aad19;
            box-shadow: inset 0 0 0 1px #1aad19;
            background: #07c1600d;
        }

        &.is-error {
            color: #f56c6c;
            box-shadow: inset 0 0 0 1px #f56c6c;
            background: #fff;
        }

        &--add {
            color: #333;
            display: flex;
            align-items: baseline;
            justify-content: center;
            gap: 4px;

            .plus {
                line-height: 1;
                font-size: 16px;
            }

            &:hover {
                color: #1aad19;
            }
        }
    }
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 150ms ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>
