<template>
    <div class="container">
        <div class="content_wrapper">
            <a-spin :spinning="loading">
                <div v-if="!loading" class="layout">
                    <div class="col-left">
                        <MenuTreeEditor
                            :tree="state.tree.value"
                            :selected-id="state.selectedId.value"
                            :error-id="errorId"
                            @select="onSelect"
                            @add-root="state.addRoot"
                            @add-child="(parentId) => state.addChild(parentId)"
                            @remove="(id) => state.removeNode(id)"
                            @move="(id, dir) => state.moveNode(id, dir)"
                        />
                    </div>
                    <div class="col-right">
                        <NodeForm
                            ref="nodeFormRef"
                            :node="state.selectedNode.value"
                            :level="currentLevel"
                            @update:node="onNodeUpdate"
                            @delete="onNodeDelete"
                        />
                    </div>
                </div>
                <div v-else class="loading-placeholder" />
            </a-spin>
        </div>

        <div
            class="selected-action-warp selected-warp-left"
            :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }"
        >
            <div class="selected-action">
                <el-button
                    type="primary"
                    size="large"
                    class="form-submit-btn"
                    :loading="saving"
                    :disabled="loading"
                    @click="onSaveAndPublish"
                > {{ $t('保存并发布') }} </el-button>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, onMounted, ref, shallowRef } from "vue";
import { message, Modal } from "ant-design-vue";
import MenuTreeEditor from "./components/MenuTreeEditor.vue";
import NodeForm from "./components/NodeForm.vue";
import { useMenuState } from "./composables/useMenuState";
import { validateTree } from "./composables/useMenuValidators";
import { getWechatMenu, saveAndPublishWechatMenu } from "@/api/setting/wechat";
import { useThemeStore } from "@/store/theme";
import type { WxMenuNode } from "@/types/setting/wechat.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const state = useMenuState();
const loading = ref<boolean>(true);
const saving = ref<boolean>(false);
const nodeFormRef = shallowRef<InstanceType<typeof NodeForm> | null>(null);

// 校验失败时定位到的节点 id：用于左侧预览区把选中框标红
const errorId = ref<string | null>(null);

const currentLevel = computed<1 | 2>(() => state.selectedLevel.value ?? 1);

async function fetchData(opts?: {
    silent?: boolean;
    preservePath?: [number] | [number, number] | null;
}) {
    // silent: 保存后的二次拉取走静默模式 —— 不切 loading，避免 v-if 把整页卸载重建造成抖动；
    // 用户可见反馈已由按钮上的 :loading="saving" 承担。
    const silent = !!opts?.silent;
    if (!silent) loading.value = true;
    try {
        const vo = await getWechatMenu();
        // 把 preservePath 透传进 loadFromVO，让 tree 替换与选中恢复在同一个同步块完成，
        // 避免中间一帧 selectedId=null 触发一级选中闪烁和二级菜单 transition 抖动。
        if (opts && "preservePath" in opts) {
            state.loadFromVO(vo ?? null, { preservePath: opts.preservePath });
        } else {
            state.loadFromVO(vo ?? null);
        }
        errorId.value = null;
    } catch (error: any) {
        message.error(error?.message || t('加载菜单失败'));
    } finally {
        if (!silent) loading.value = false;
    }
}

function onSelect(id: string) {
    state.selectedId.value = id;
}

function onNodeUpdate(node: WxMenuNode) {
    if (!state.selectedId.value) return;
    state.updateNode(state.selectedId.value, node);
    // 用户已在开始修复：清掉红框
    if (errorId.value === state.selectedId.value) {
        errorId.value = null;
    }
}

function onNodeDelete() {
    if (!state.selectedId.value) return;
    const id = state.selectedId.value;
    const displayName = state.selectedNode.value?.name?.trim() || t('该菜单');
    Modal.confirm({
        title: t('删除后，{displayName}菜单下设置的内容全部将被删除', { displayName }),
        okText: t('确定'),
        okType: "danger",
        cancelText: t('取消'),
        onOk() {
            state.removeNode(id);
            if (errorId.value === id) {
                errorId.value = null;
            }
        }
    });
}

async function onSaveAndPublish() {
    // 1) NodeForm 实时校验已自管，这里再触发一次锁定当前编辑项（含发送消息子表单）
    const validateFn = nodeFormRef.value?.validate;
    const formErr = typeof validateFn === "function" ? await validateFn() : null;
    if (formErr) {
        // 当前正在编辑的节点即问题节点
        errorId.value = state.selectedId.value;
        message.error(formErr);
        return;
    }

    // 2) 全树校验
    const errors = validateTree(state.tree.value);
    if (errors.length) {
        const first = errors[0];
        // 取第一条可定位的错误；若首条是树级错误（nodeId=null）则沿用它的文案
        const located = errors.find((e) => !!e.nodeId) ?? first;
        if (located.nodeId) {
            state.selectedId.value = located.nodeId;
            errorId.value = located.nodeId;
        } else {
            errorId.value = null;
        }
        message.error(first.message);
        return;
    }

    saving.value = true;
    try {
        const dto = state.toSaveDTO();
        const result = await saveAndPublishWechatMenu(dto);
        if (result?.published) {
            message.success(t('保存并发布成功'));
        } else {
            const reason = result?.errMsg || t('未知错误');
            message.warning(t('发布失败：{reason}，菜单未保存，请稍后重试', { reason }));
        }
        errorId.value = null;
        // 重拉前记录当前选中路径：新建节点的 id 会被服务端替换为稳定 id，
        // 按路径恢复才能保证选中不丢、不闪。
        // 路径直接透传到 loadFromVO，内部将 tree 替换与选中恢复放在同一同步块，
        // 避免中间一帧 selectedId=null 触发的闪烁与二级菜单 transition 抖动。
        const prevPath = state.getSelectedPath();
        await fetchData({ silent: true, preservePath: prevPath });
    } catch (error: any) {
        message.error(error?.message || t('保存失败'));
    } finally {
        saving.value = false;
    }
}

onMounted(fetchData);
</script>

<style lang="less" scoped>
.layout {
    display: grid;
    grid-template-columns: 380px minmax(0, 1fr);
    gap: 12px;
    align-items: stretch;
}

.col-left,
.col-right {
    min-width: 0;
}

.col-right {
    max-width: 720px;
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
}

.loading-placeholder {
    min-height: 360px;
}

@media (max-width: 1100px) {
    .layout {
        grid-template-columns: minmax(0, 1fr);
    }
}
</style>
