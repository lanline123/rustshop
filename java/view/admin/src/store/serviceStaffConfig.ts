// src/store/serviceStaffConfig.ts
import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { getIntraCityDeliveryConfig } from "@/api/setting/intraCityDelivery";
import { getHomeServiceConfig } from "@/api/setting/homeService";
import type { ShopLocalDeliveryConfigDetailVO } from "@/types/setting/intraCityDelivery";
import type { ShopHomeServiceConfigDetailVO } from "@/types/setting/homeService";
import type { DispatchMode } from "@/types/organize/config";

/** 门店配送服务配置状态 */
export interface DeliveryServiceState {
    /** 同城配送配置 */
    intraCityDelivery: ShopLocalDeliveryConfigDetailVO | null;
    /** 上门服务配置 */
    homeService: ShopHomeServiceConfigDetailVO | null;
}

/** 默认配置状态 */
const DEFAULT_STATE: DeliveryServiceState = {
    intraCityDelivery: null,
    homeService: null
};

export const useServiceConfigStore = defineStore(
    "serviceConfig",
    () => {
        // ===== State =====

        /** 同城配送配置 */
        const intraCityDeliveryConfig = ref<ShopLocalDeliveryConfigDetailVO | null>(null);

        /** 上门服务配置 */
        const homeServiceConfig = ref<ShopHomeServiceConfigDetailVO | null>(null);

        /** 同城配送配置是否已加载 */
        const intraCityDeliveryLoaded = ref(false);

        /** 上门服务配置是否已加载 */
        const homeServiceLoaded = ref(false);

        /** 加载状态 */
        const loading = ref(false);

        /** 错误信息 */
        const error = ref<string | null>(null);

        // ===== Getters - 同城配送 =====

        /** 是否开启同城配送 */
        const isIntraCityDeliveryEnabled = computed(() => {
            return intraCityDeliveryConfig.value?.enabled === 1;
        });

        /** 同城配送是否为派单模式（手动派单） */
        const isIntraCityDeliveryDispatchMode = computed(() => {
            return intraCityDeliveryConfig.value?.orderDispatchMode === 2;
        });

        /** 同城配送是否为抢单模式 */
        const isIntraCityDeliveryGrabMode = computed(() => {
            return intraCityDeliveryConfig.value?.orderDispatchMode === 1;
        });

        /** 同城配送派单模式值 */
        const intraCityDeliveryDispatchMode = computed<DispatchMode | undefined>(() => {
            return intraCityDeliveryConfig.value?.orderDispatchMode as DispatchMode | undefined;
        });

        // ===== Getters - 上门服务 =====

        /** 是否开启上门服务 */
        const isHomeServiceEnabled = computed(() => {
            return homeServiceConfig.value?.enabled === 1;
        });

        /** 上门服务是否为派单模式（手动派单） */
        const isHomeServiceDispatchMode = computed(() => {
            return homeServiceConfig.value?.orderDispatchMode === 2;
        });

        /** 上门服务是否为抢单模式 */
        const isHomeServiceGrabMode = computed(() => {
            return homeServiceConfig.value?.orderDispatchMode === 1;
        });

        /** 上门服务派单模式值 */
        const homeServiceDispatchModeValue = computed<DispatchMode | undefined>(() => {
            return homeServiceConfig.value?.orderDispatchMode as DispatchMode | undefined;
        });

        // ===== Getters - 通用 =====

        /** 是否已全部加载完成 */
        const allLoaded = computed(() => {
            return intraCityDeliveryLoaded.value && homeServiceLoaded.value;
        });

        // ===== Actions =====

        /**
         * 加载同城配送配置
         * @param force 是否强制刷新
         */
        async function loadIntraCityDeliveryConfig(force = false): Promise<ShopLocalDeliveryConfigDetailVO | null> {
            if (intraCityDeliveryLoaded.value && !force) {
                return intraCityDeliveryConfig.value;
            }

            loading.value = true;
            error.value = null;

            try {
                const result = await getIntraCityDeliveryConfig();
                intraCityDeliveryConfig.value = result;
                intraCityDeliveryLoaded.value = true;
                return result;
            } catch (err: any) {
                error.value = err.message || "加载同城配送配置失败";
                console.error("加载同城配送配置失败:", err);
                return null;
            } finally {
                loading.value = false;
            }
        }

        /**
         * 加载上门服务配置
         * @param force 是否强制刷新
         */
        async function loadHomeServiceConfig(force = false): Promise<ShopHomeServiceConfigDetailVO | null> {
            if (homeServiceLoaded.value && !force) {
                return homeServiceConfig.value;
            }

            loading.value = true;
            error.value = null;

            try {
                const result = await getHomeServiceConfig();
                homeServiceConfig.value = result;
                homeServiceLoaded.value = true;
                return result;
            } catch (err: any) {
                error.value = err.message || "加载上门服务配置失败";
                console.error("加载上门服务配置失败:", err);
                return null;
            } finally {
                loading.value = false;
            }
        }

        /**
         * 加载所有配置
         * @param force 是否强制刷新
         */
        async function loadAllConfig(force = false): Promise<void> {
            await Promise.all([loadIntraCityDeliveryConfig(force), loadHomeServiceConfig(force)]);
        }

        /**
         * 重置加载状态（用于强制下次重新加载）
         */
        function resetLoaded(): void {
            intraCityDeliveryLoaded.value = false;
            homeServiceLoaded.value = false;
        }

        /**
         * 重置同城配送配置加载状态
         */
        function resetIntraCityLoaded(): void {
            intraCityDeliveryLoaded.value = false;
        }

        /**
         * 重置上门服务配置加载状态
         */
        function resetHomeServiceLoaded(): void {
            homeServiceLoaded.value = false;
        }

        /**
         * 重置为默认状态
         */
        function resetToDefault(): void {
            intraCityDeliveryConfig.value = null;
            homeServiceConfig.value = null;
            intraCityDeliveryLoaded.value = false;
            homeServiceLoaded.value = false;
            error.value = null;
        }

        /**
         * 清除错误
         */
        function clearError(): void {
            error.value = null;
        }

        return {
            // State
            intraCityDeliveryConfig,
            homeServiceConfig,
            intraCityDeliveryLoaded,
            homeServiceLoaded,
            loading,
            error,

            // Getters - 同城配送
            isIntraCityDeliveryEnabled,
            isIntraCityDeliveryDispatchMode,
            isIntraCityDeliveryGrabMode,
            intraCityDeliveryDispatchMode,

            // Getters - 上门服务
            isHomeServiceEnabled,
            isHomeServiceDispatchMode,
            isHomeServiceGrabMode,
            homeServiceDispatchModeValue,

            // Getters - 通用
            allLoaded,

            // Actions
            loadIntraCityDeliveryConfig,
            loadHomeServiceConfig,
            loadAllConfig,
            resetLoaded,
            resetIntraCityLoaded,
            resetHomeServiceLoaded,
            resetToDefault,
            clearError
        };
    }
);

/** Store 类型导出（用于类型推断） */
export type ServiceConfigStore = ReturnType<typeof useServiceConfigStore>;
