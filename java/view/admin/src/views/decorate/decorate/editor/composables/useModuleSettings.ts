/**
 * 模块设置通用逻辑 Composable（移植自 Vort-Admin useModuleSettings）
 *
 * 与 Vort 的差异：
 * - useDebounceFn(@vueuse/core) 改为 lodash-es 的 debounce（项目未引入 @vueuse/core）
 * - store 路径指向 Tigshop 的 decorateEditor
 *
 * 供新版四件套模块的 Settings.vue 使用：本地 localData 绑定表单，
 * 防抖/节流写回 store（与旧 v-model 直改 store 的范式并存，互不影响）。
 */

import { ref, watch, onBeforeUnmount, type Ref } from "vue";
import { debounce } from "lodash";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import { deepMerge } from "../utils/object";

export interface UseModuleSettingsOptions {
    /** 防抖时间（毫秒） */
    debounceMs?: number;
    /** 节流时间（毫秒），用于拖拽等高频场景的实时同步 */
    throttleMs?: number;
    /** 判定为"连续高频更新"的时间阈值（毫秒） */
    continuousMs?: number;
    /** 是否在挂载时自动初始化 */
    autoInit?: boolean;
    /** 是否深度监听 Store 变化 */
    deep?: boolean;
}

export interface UseModuleSettingsReturn<T extends object> {
    /** 本地数据（用于绑定表单） */
    localData: Ref<T>;
    /** 更新单个字段（控件 emit 类型宽泛，在具体模块上以 T[K] 为准） */
    updateField: <K extends keyof T>(key: K, value: unknown) => void;
    /** 更新多个字段 */
    updateFields: (fields: Partial<T>) => void;
    /** 更新嵌套字段（如 frame.boxPadding） */
    updateNestedField: <K extends keyof T>(parentKey: K, childKey: keyof NonNullable<T[K]>, value: unknown) => void;
    /** 立即同步到 Store（不等待防抖） */
    syncToStore: () => void;
    /** 从 Store 重新初始化 */
    initLocalData: () => void;
    /** Store 实例 */
    store: ReturnType<typeof useDecorateEditorStore>;
}

export function useModuleSettings<T extends object>(defaults: T, options: UseModuleSettingsOptions = {}): UseModuleSettingsReturn<T> {
    const { debounceMs = 300, throttleMs = 50, continuousMs = 60, autoInit = true, deep = false } = options;

    const store = useDecorateEditorStore();

    // 本地数据，避免频繁触发 Store 更新
    const localData = ref<T>({ ...defaults }) as Ref<T>;

    /** 绑定模块 index，防止切换后 patch 写错对象 */
    let boundModuleIndex: number | null = null;

    /** 完整替换同步 */
    function syncToStore() {
        if (boundModuleIndex !== null && store.activeModule?.moduleIndex !== boundModuleIndex) return;
        store.replaceActiveModuleData(localData.value as Record<string, unknown>);
    }

    /** 局部同步（只更新指定字段，性能更好） */
    function patchToStore(patch: Record<string, unknown>) {
        if (boundModuleIndex !== null && store.activeModule?.moduleIndex !== boundModuleIndex) return;
        store.patchActiveModuleData(patch);
    }

    const debouncedPatch = debounce(patchToStore, debounceMs);

    // 节流局部同步（leading 立即执行 + trailing 补最终值），用于 Slider/颜色拖拽等高频场景
    let throttleTimer: ReturnType<typeof setTimeout> | null = null;
    let pendingThrottlePatch: Record<string, unknown> | null = null;

    function cancelPendingPatches() {
        debouncedPatch.cancel();
        if (throttleTimer) {
            clearTimeout(throttleTimer);
            throttleTimer = null;
        }
        pendingThrottlePatch = null;
    }

    function initLocalData() {
        cancelPendingPatches();
        boundModuleIndex = store.activeModule?.moduleIndex ?? null;
        if (store.activeModuleData) {
            const storeData = store.activeModuleData as Partial<T>;
            localData.value = deepMerge(defaults, storeData);
        } else {
            localData.value = { ...defaults };
        }
    }

    function throttledPatch(patch: Record<string, unknown>) {
        if (!throttleTimer) {
            patchToStore(patch);
            throttleTimer = setTimeout(() => {
                throttleTimer = null;
                if (pendingThrottlePatch) {
                    const p = pendingThrottlePatch;
                    pendingThrottlePatch = null;
                    patchToStore(p);
                }
            }, throttleMs);
        } else {
            pendingThrottlePatch = { ...(pendingThrottlePatch ?? {}), ...patch };
        }
    }

    // 记录每个字段上一次更新时间，用于识别连续高频更新
    const lastUpdateAtMap = new Map<string, number>();

    function isContinuousUpdate(key: string) {
        const now = Date.now();
        const last = lastUpdateAtMap.get(key) ?? 0;
        lastUpdateAtMap.set(key, now);
        return now - last <= continuousMs;
    }

    function updateField<K extends keyof T>(key: K, value: unknown) {
        const next = value as T[K];
        localData.value[key] = next;
        boundModuleIndex = store.activeModule?.moduleIndex ?? null;
        const patch = { [key]: next } as Record<string, unknown>;
        // 默认走防抖（输入框、选择器等）；连续高频更新时叠加节流实时同步
        debouncedPatch(patch);
        if (isContinuousUpdate(String(key))) {
            throttledPatch(patch);
        }
    }

    function updateFields(fields: Partial<T>) {
        Object.assign(localData.value, fields);
        debouncedPatch(fields as Record<string, unknown>);
    }

    function updateNestedField<K extends keyof T>(parentKey: K, childKey: keyof NonNullable<T[K]>, value: unknown) {
        const parent = localData.value[parentKey];
        if (parent && typeof parent === "object") {
            (parent as Record<string, unknown>)[childKey as string] = value;
            store.patchNestedField(parentKey as string, childKey as string, value);
        }
    }

    if (autoInit) {
        initLocalData();
    }

    if (deep) {
        watch(
            () => store.activeModuleData,
            (newData) => {
                if (newData) {
                    localData.value = deepMerge(defaults, newData as Partial<T>);
                }
            },
            { deep: true }
        );
    }

    onBeforeUnmount(() => {
        cancelPendingPatches();
    });

    return {
        localData,
        updateField,
        updateFields,
        updateNestedField,
        syncToStore,
        initLocalData,
        store
    };
}

export default useModuleSettings;
