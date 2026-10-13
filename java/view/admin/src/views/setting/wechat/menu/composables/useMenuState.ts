// 公众号菜单 - 编辑态状态机
// 提供 tree / selectedId / selectedNode 与一组操作方法。
// 字段名严格按 types/setting/wechat.d.ts：buttons / children / sortOrder / rule。

import { computed, ref } from "vue";
import type {
    SaveAndPublishDTO,
    WxMenuNode,
    WxMenuRule,
    WxMenuTree,
    WxMenuVO
} from "@/types/setting/wechat.d";
import { CHILD_MAX, ROOT_MAX, genNodeId, isTempId, migrateMenuClickRule } from "./useMenuValidators";

/** 在树中递归查找节点；同时返回所在数组与索引，便于删除/移动 */
interface FindResult {
    node: WxMenuNode;
    parent: WxMenuNode | null;
    siblings: WxMenuNode[];
    index: number;
    level: 1 | 2;
}

function findNode(tree: WxMenuTree, id: string | null): FindResult | null {
    if (!id) return null;
    const buttons = tree.buttons ?? [];
    for (let i = 0; i < buttons.length; i++) {
        const root = buttons[i];
        if (root.id === id) {
            return { node: root, parent: null, siblings: buttons, index: i, level: 1 };
        }
        const children = root.children ?? [];
        for (let j = 0; j < children.length; j++) {
            if (children[j].id === id) {
                return { node: children[j], parent: root, siblings: children, index: j, level: 2 };
            }
        }
    }
    return null;
}

function reindex(list: WxMenuNode[]) {
    list.forEach((it, idx) => {
        it.sortOrder = idx;
    });
}

function defaultRule(): WxMenuRule {
    // eventKey 留空，由后端 saveAndPublish 时统一分配
    return migrateMenuClickRule({ type: "click", msgType: 1, content: "" })!;
}

function migrateTree(tree: WxMenuTree): void {
    (tree.buttons ?? []).forEach((root) => {
        if (root.children?.length) {
            root.children.forEach((ch) => {
                if (ch.rule) ch.rule = migrateMenuClickRule(ch.rule);
            });
        } else if (root.rule) {
            root.rule = migrateMenuClickRule(root.rule);
        }
    });
}

export function useMenuState() {
    const tree = ref<WxMenuTree>({ buttons: [] });
    const selectedId = ref<string | null>(null);

    const selectedNode = computed<WxMenuNode | null>(() => {
        const found = findNode(tree.value, selectedId.value);
        return found ? found.node : null;
    });

    /**
     * 选中节点的层级；未选中或找不到时返回 null（视图层据此决定 NodeForm 的 level）
     */
    const selectedLevel = computed<1 | 2 | null>(() => {
        const found = findNode(tree.value, selectedId.value);
        return found ? found.level : null;
    });

    /**
     * 加载服务端数据到编辑态。
     *  - 默认行为（初次加载）：清空选中。
     *  - 传入 opts.preservePath 时（含 null），按路径在同一个同步块内恢复选中；
     *    这样 tree 替换与 selectedId 恢复会被 Vue 合并为一次渲染，
     *    避免"保存后静默刷新"过程中因 selectedId 瞬时为 null 造成的一级选中闪烁
     *    和二级菜单 sub-panel 的 transition leave/enter 抖动。
     */
    function loadFromVO(
        vo: WxMenuVO | null | undefined,
        opts?: { preservePath?: [number] | [number, number] | null }
    ) {
        const next = vo?.tree?.buttons ? vo.tree : { buttons: [] };
        // 深拷贝，避免后续编辑直接污染 props 引用
        tree.value = JSON.parse(JSON.stringify(next));
        // 兜底为没有 id 的旧数据补齐，保证编辑稳定 key
        (tree.value.buttons ?? []).forEach((root) => {
            if (!root.id) root.id = genNodeId();
            (root.children ?? []).forEach((child) => {
                if (!child.id) child.id = genNodeId();
            });
        });
        migrateTree(tree.value);
        if (opts && "preservePath" in opts) {
            selectByPath(opts.preservePath ?? null);
        } else {
            selectedId.value = null;
        }
    }

    function addRoot() {
        const buttons = tree.value.buttons ?? (tree.value.buttons = []);
        if (buttons.length >= ROOT_MAX) return;
        const node: WxMenuNode = {
            id: genNodeId(),
            name: "菜单名称",
            sortOrder: buttons.length,
            rule: defaultRule()
        };
        buttons.push(node);
        reindex(buttons);
        selectedId.value = node.id;
    }

    function addChild(parentId: string) {
        const found = findNode(tree.value, parentId);
        if (!found || found.level !== 1) return;
        const parent = found.node;
        if (!parent.children) parent.children = [];
        if (parent.children.length >= CHILD_MAX) return;
        // 二级菜单存在时一级 rule 不再生效，按规则清掉以免提交无谓字段
        if (parent.rule) parent.rule = undefined;
        const node: WxMenuNode = {
            id: genNodeId(),
            name: "子菜单名",
            sortOrder: parent.children.length,
            rule: defaultRule()
        };
        parent.children.push(node);
        reindex(parent.children);
        selectedId.value = node.id;
    }

    function removeNode(id: string) {
        const found = findNode(tree.value, id);
        if (!found) return;
        const wasSelected = selectedId.value === id;
        const parent = found.parent;
        const level = found.level;

        found.siblings.splice(found.index, 1);
        reindex(found.siblings);
        // 删二级后若 parent 已无任何 children，给一级补一个默认 rule
        if (level === 2 && parent && (parent.children?.length ?? 0) === 0) {
            parent.children = undefined;
            if (!parent.rule) parent.rule = defaultRule();
        }

        if (!wasSelected) return;

        // 删除被选中项后：把焦点平移到相邻菜单，避免胶囊消失
        //  - 删二级：优先选父级剩余的第一个二级；若父级已退化为叶子则选父一级
        //  - 删一级：优先选剩余的第一个一级；全空时才清空
        if (level === 2) {
            if (parent?.children && parent.children.length > 0) {
                selectedId.value = parent.children[0].id;
            } else if (parent) {
                selectedId.value = parent.id;
            } else {
                selectedId.value = null;
            }
        } else {
            const buttons = tree.value.buttons ?? [];
            selectedId.value = buttons.length > 0 ? buttons[0].id : null;
        }
    }

    function moveNode(id: string, dir: -1 | 1) {
        const found = findNode(tree.value, id);
        if (!found) return;
        const target = found.index + dir;
        if (target < 0 || target >= found.siblings.length) return;
        const [moved] = found.siblings.splice(found.index, 1);
        found.siblings.splice(target, 0, moved);
        reindex(found.siblings);
    }

    function updateNode(id: string | null, patch: Partial<WxMenuNode>) {
        if (!id) return;
        const found = findNode(tree.value, id);
        if (!found) return;
        Object.assign(found.node, patch);
    }

    /**
     * 获取当前选中节点的路径（按索引），用于跨一次数据重载后恢复选中。
     *  - 一级节点：[rootIndex]
     *  - 二级节点：[rootIndex, childIndex]
     *  - 未选中或找不到：null
     * 之所以不保留 id 直接复原：新建节点在保存后 id 会被服务端替换为稳定 id，
     * 按 id 恢复会命中不到，只有路径（索引）在“保存 -> 重拉”前后稳定。
     */
    function getSelectedPath(): [number] | [number, number] | null {
        const found = findNode(tree.value, selectedId.value);
        if (!found) return null;
        if (found.level === 1) return [found.index];
        const buttons = tree.value.buttons ?? [];
        const rootIdx = buttons.findIndex((b) => b === found.parent);
        if (rootIdx < 0) return null;
        return [rootIdx, found.index];
    }

    /**
     * 按路径恢复选中；路径越界时做最近可用项兜底：
     *  - 目标二级缺失但父一级存在 -> 选中父一级
     *  - 目标一级缺失但仍有一级节点 -> 选中第一个一级
     *  - 其它情况 -> 清空选中
     */
    function selectByPath(path: [number] | [number, number] | null) {
        if (!path) {
            selectedId.value = null;
            return;
        }
        const buttons = tree.value.buttons ?? [];
        const root = buttons[path[0]];
        if (!root) {
            selectedId.value = buttons.length > 0 ? buttons[0].id : null;
            return;
        }
        if (path.length === 1) {
            selectedId.value = root.id;
            return;
        }
        const child = (root.children ?? [])[path[1]];
        selectedId.value = child ? child.id : root.id;
    }

    /**
     * 转换为提交后端的 DTO：
     *  - tmp_ 前缀的临时 id 改为 undefined，由后端落库后回填稳定 id
     *  - 服务端 id 原样保留以便编辑态延续
     *  - 含子菜单的一级节点剔除 rule
     *  - sortOrder 重新编号确保连续
     */
    function toSaveDTO(): SaveAndPublishDTO {
        const cloneNode = (n: WxMenuNode, level: 1 | 2): WxMenuNode => {
            const out: WxMenuNode = {
                id: isTempId(n.id) ? "" : n.id,
                name: (n.name ?? "").trim(),
                sortOrder: n.sortOrder ?? 0
            };
            if (level === 1 && Array.isArray(n.children) && n.children.length > 0) {
                out.children = n.children.map((c, i) => {
                    const child = cloneNode(c, 2);
                    child.sortOrder = i;
                    return child;
                });
            } else if (n.rule) {
                out.rule = { ...n.rule };
            }
            // 后端 DTO 中 id 字段通常允许 null/缺省；这里临时 id 用空串，调用 JSON.stringify 时无歧义
            if (out.id === "") delete (out as { id?: string }).id;
            return out;
        };

        const buttons = (tree.value.buttons ?? []).map((b, i) => {
            const node = cloneNode(b, 1);
            node.sortOrder = i;
            return node;
        });

        return { tree: { buttons } };
    }

    return {
        tree,
        selectedId,
        selectedNode,
        selectedLevel,
        loadFromVO,
        addRoot,
        addChild,
        removeNode,
        moveNode,
        updateNode,
        toSaveDTO,
        getSelectedPath,
        selectByPath
    };
}

export type MenuState = ReturnType<typeof useMenuState>;
