<template>
    <view class="tig-map">
        <!-- 原生 map：小程序恒走此分支；H5 / App 在 mapProvider 为 amap 时也走此分支 -->
        <map
            v-if="isNative"
            :id="mapId"
            class="tig-map__inner"
            :longitude="longitude"
            :latitude="latitude"
            :scale="scale"
            :markers="markers"
            :show-location="showLocation"
            :enable-zoom="true"
            :enable-scroll="true"
            :enable-3D="enable3D"
            :show-compass="showCompass"
            :enable-overlooking="enableOverlooking"
            :enable-rotate="enableRotate"
            @tap="onNativeTap"
            @markertap="onNativeMarkerTap"
            @regionchange="onNativeRegionChange"
            @updated="onNativeUpdated"
        >
            <slot :is-native="true" />
        </map>

        <!-- #ifdef H5 || APP-PLUS -->
        <!-- 天地图：DOM 容器由内联 renderjs 驱动。逻辑层数据合并为单个 payload 一次性下发 -->
        <view v-else class="tig-map__inner" :prop="renderPayload" :change:prop="tdtMap.onPayloadChange">
            <view :id="mapId" class="tig-map__canvas" />
            <slot :is-native="false" />
        </view>
        <!-- #endif -->
    </view>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { PropType } from "vue";
import { useConfigStore } from "@/store/config";
import { isNativeMapProvider } from "@/utils/mapProvider";

// renderjs 模块由 uni-app 编译期注入，TS 看不到它，仅为模板里的 tdtMap.onPayloadChange 补类型
declare module "vue" {
    interface ComponentCustomProperties {
        tdtMap: Record<string, (...args: any[]) => any>;
    }
}

export interface TigMapMarker {
    id: number;
    latitude: number;
    longitude: number;
    iconPath?: string;
    width?: number;
    height?: number;
    anchor?: { x: number; y: number };
    // 以下三项天地图分支不消费，仅透传给原生 map，保证小程序与 amap 分支行为零变化
    title?: string;
    callout?: Record<string, any>;
    joinCluster?: boolean;
    /**
     * 天地图分支专用：用 T.DivIcon 渲染的自定义 DOM 片段，原生分支忽略并继续走 iconPath。
     * 供原生端靠 canvas 合成图标 / callout 实现、而天地图端可直接用 DOM 表达的场景。
     * 动态节点拿不到 scoped 样式属性，样式必须内联；拼接前须自行转义外部文本。
     */
    html?: string;
    htmlSize?: { width: number; height: number };
    htmlAnchor?: { x: number; y: number };
}

export interface TigMapRegionChange {
    type: "begin" | "end";
    centerLocation: { latitude: number; longitude: number } | null;
}

/**
 * 统一地图组件
 *
 * 三个分支：
 * - 小程序端：原生 <map>（renderjs 在小程序不生效，天然回落）
 * - H5 / App 且 mapProvider = amap：原生 <map>
 * - H5 / App 且 mapProvider = tianditu：DOM 容器 + 内联 renderjs 驱动天地图 JS API
 *
 * 必须使用选项式 API：renderjs 的 $ownerInstance.callMethod() 只能调用 methods 中的方法，
 * vue3 的 script setup 不被支持（uni-app 官方限制）。
 */
export default defineComponent({
    name: "TigMap",
    props: {
        /** 原生 map 的 id / 天地图 DOM 容器 id，同页面多实例时必须区分 */
        mapId: { type: String, default: "tigMap" },
        longitude: { type: Number, default: 0 },
        latitude: { type: Number, default: 0 },
        scale: { type: Number, default: 16 },
        markers: { type: Array as PropType<TigMapMarker[]>, default: () => [] },
        /** 显示定位点。天地图分支下由逻辑层取点后自绘，无内置能力 */
        showLocation: { type: Boolean, default: false },
        // 以下四项天地图 JS API 无对应能力，仅透传给原生 map。
        // 保留透传而非直接删除，是为了让小程序端与 amap 分支行为零变化
        enable3D: { type: Boolean, default: false },
        showCompass: { type: Boolean, default: false },
        enableOverlooking: { type: Boolean, default: false },
        enableRotate: { type: Boolean, default: false }
    },
    emits: ["regionchange", "tap", "markertap", "updated"],
    data() {
        return {
            // renderjs 无 unmounted 生命周期，销毁与平移都只能靠逻辑层递增序号下发信号
            destroySeq: 0,
            panSeq: 0,
            panTarget: { latitude: 0, longitude: 0 },
            // 天地图无内置定位能力，且 renderjs 内不可用 uni API（C-B），故由逻辑层取点后下发自绘
            userLocation: null as { latitude: number; longitude: number } | null
        };
    },
    computed: {
        mapProvider(): string {
            return useConfigStore().mapProvider;
        },
        tiandituJsTk(): string {
            return useConfigStore().tiandituJsTk;
        },
        /** tk 缺失时回落原生分支，避免后端只切了 provider 却没下发 tk 时白屏 */
        isNative(): boolean {
            // 依赖上面两个 computed 以建立响应式追踪，实际判定收敛在 isNativeMapProvider
            void this.mapProvider;
            void this.tiandituJsTk;
            return isNativeMapProvider();
        },
        /** 下发给 renderjs 的完整数据，合并成单个对象避免多次跨层通信 */
        renderPayload(): Record<string, any> {
            return {
                tk: this.tiandituJsTk,
                mapId: this.mapId,
                center: { longitude: this.longitude, latitude: this.latitude },
                scale: this.scale,
                markers: this.markers,
                userLocation: this.showLocation ? this.userLocation : null,
                panSeq: this.panSeq,
                panTarget: this.panTarget,
                destroySeq: this.destroySeq
            };
        }
    },
    watch: {
        showLocation: "ensureUserLocation",
        isNative: "ensureUserLocation"
    },
    mounted() {
        this.ensureUserLocation();
    },
    beforeUnmount() {
        // 在 DOM 拆除前递增，给 renderjs 留出接收销毁信号的时机
        this.destroySeq += 1;
    },
    methods: {
        /**
         * 天地图分支下取一次系统定位供视图层画蓝点。
         * 与原生 map 的 show-location 有差异：原生是持续跟踪，此处是进页面取一次的静态点。
         * 坐标类型取 gcj02，与全局 marker 口径一致（坐标系问题本轮不在范围内）。
         */
        ensureUserLocation() {
            if (this.isNative || !this.showLocation || this.userLocation) return;
            uni.getLocation({
                type: "gcj02",
                success: (res) => {
                    this.userLocation = { latitude: res.latitude, longitude: res.longitude };
                },
                // 定位失败或用户拒权时不画蓝点，不阻断底图
                fail: () => {}
            });
        },

        /**
         * 归一化 regionchange。原生 map 的 begin/end 位于 e.detail.type，
         * 天地图 renderjs 回调则直接传递 type；优先读取 detail，兼容两种事件来源。
         */
        onNativeRegionChange(e: any) {
            const detail = e?.detail ?? {};
            const center = detail.centerLocation;
            this.$emit("regionchange", {
                type: detail.type ?? e?.type,
                centerLocation: center ? { latitude: center.latitude, longitude: center.longitude } : null
            } as TigMapRegionChange);
        },
        onNativeTap() {
            this.$emit("tap");
        },
        onNativeMarkerTap(e: any) {
            this.$emit("markertap", e?.detail?.markerId ?? e?.markerId);
        },
        onNativeUpdated() {
            this.$emit("updated");
        },

        // ---- 以下四个由 renderjs 通过 $ownerInstance.callMethod 回调，勿改名 ----
        onViewRegionChange(payload: TigMapRegionChange) {
            this.$emit("regionchange", payload);
        },
        onViewTap() {
            this.$emit("tap");
        },
        onViewMarkerTap(markerId: number) {
            this.$emit("markertap", markerId);
        },
        onViewUpdated() {
            this.$emit("updated");
        },

        /** 平移到指定坐标。调用方传的是已知坐标，不是现场取系统定位 */
        moveToLocation(latitude: number, longitude: number) {
            if (this.isNative) {
                uni.createMapContext(this.mapId, this).moveToLocation({ latitude, longitude } as any);
                return;
            }
            this.panTarget = { latitude, longitude };
            this.panSeq += 1;
        }
    }
});
</script>

<script module="tdtMap" lang="renderjs">
/**
 * 天地图渲染层（视图层）
 *
 * 约束：
 * - 仅内联可用，不能抽独立模块 import，故 SDK 加载逻辑在此重复一份
 * - 不可使用任何 uni API，定位 / 图片信息等一律由逻辑层下发
 * - 无 unmounted 生命周期，销毁靠逻辑层递增 destroySeq 触发
 * - H5 端逻辑层与视图层同环境，本模块以 mixin 方式并入组件，故成员统一加 tdt 前缀避免与逻辑层重名
 */
const SDK_URL = "https://api.tianditu.gov.cn/api";
const SDK_VERSION = "4.0";
// 脚本 onload 后天地图仍可能异步补齐命名空间，须轮询等待
const READY_TIMEOUT = 15000;
const READY_INTERVAL = 50;
// 判定「当前中心点是否就是自己刚设过去的那个」，1e-6 度约 0.1 米
const SAME_POINT_EPS = 1e-6;

/** 动态注入天地图 SDK，并发去重挂在 window 上；失败清缓存以允许重试 */
function loadTiandituSdk(tk) {
    if (window.T && window.T.Map) return Promise.resolve(window.T);
    if (window.__tigTdtPending) return window.__tigTdtPending;

    window.__tigTdtPending = new Promise(function (resolve, reject) {
        const script = document.createElement("script");
        script.type = "text/javascript";
        script.async = true;
        script.src = SDK_URL + "?v=" + SDK_VERSION + "&tk=" + encodeURIComponent(tk);
        script.onload = function () {
            const startedAt = Date.now();
            const tick = function () {
                if (window.T && window.T.Map) {
                    resolve(window.T);
                    return;
                }
                if (Date.now() - startedAt >= READY_TIMEOUT) {
                    reject(new Error("天地图 SDK 加载超时"));
                    return;
                }
                setTimeout(tick, READY_INTERVAL);
            };
            tick();
        };
        script.onerror = function () {
            reject(new Error("天地图 SDK 加载失败"));
        };
        document.head.appendChild(script);
    })["catch"](function (error) {
        window.__tigTdtPending = null;
        throw error;
    });

    return window.__tigTdtPending;
}

export default {
    data() {
        return {
            tdt: null,
            tdtInstance: null,
            tdtElId: "",
            tdtOwner: null,
            tdtOverlays: [],
            // SDK 异步加载期间到达的 payload 必须留存，否则初始化会用过期数据
            tdtPending: null,
            tdtLoading: false,
            tdtDestroyed: false,
            tdtLastPanSeq: 0,
            tdtLastDestroySeq: 0,
            tdtLastMarkerKey: "",
            tdtLocationMarker: null,
            tdtLastLocationKey: "",
            tdtLastScale: 0,
            /** 最近一次程序化设置的中心点，用于过滤自身触发的 moveend */
            tdtSelfMovedTo: null
        };
    },
    methods: {
        onPayloadChange(payload, oldPayload, ownerInstance) {
            if (!payload) return;
            if (ownerInstance) this.tdtOwner = ownerInstance;

            // 销毁信号优先于一切
            if (payload.destroySeq > this.tdtLastDestroySeq) {
                this.tdtLastDestroySeq = payload.destroySeq;
                this.tdtDestroy();
                return;
            }
            if (!payload.tk) return;

            // 先留存最新 payload：SDK 加载是异步的，这期间的 prop 变更若不记下就会永久丢失
            this.tdtPending = payload;
            if (!this.tdtInstance) {
                this.tdtInit();
                return;
            }
            this.tdtApply(payload);
        },

        tdtCall(name, arg) {
            if (this.tdtOwner && this.tdtOwner.callMethod) this.tdtOwner.callMethod(name, arg);
        },

        tdtInit() {
            if (this.tdtLoading) return;
            this.tdtLoading = true;
            const self = this;

            loadTiandituSdk(this.tdtPending.tk)
                .then(function (T) {
                    self.tdtLoading = false;
                    if (self.tdtDestroyed) return;

                    // 取加载完成时刻的最新 payload，而不是发起加载时的那一份
                    const payload = self.tdtPending;
                    const el = document.getElementById(payload.mapId);
                    if (!el) return;

                    const map = new T.Map(el);
                    map.centerAndZoom(new T.LngLat(payload.center.longitude, payload.center.latitude), payload.scale);

                    map.addEventListener("movestart", function () {
                        self.tdtEmitRegion("begin");
                    });
                    map.addEventListener("moveend", function () {
                        self.tdtEmitRegion("end");
                    });
                    map.addEventListener("click", function () {
                        self.tdtCall("onViewTap");
                    });

                    self.tdt = T;
                    self.tdtInstance = map;
                    self.tdtElId = payload.mapId;
                    self.tdtLastScale = payload.scale;
                    self.tdtSelfMovedTo = { lng: payload.center.longitude, lat: payload.center.latitude };
                    self.tdtRenderMarkers(payload.markers);
                    self.tdtRenderLocation(payload.userLocation);
                    self.tdtCall("onViewUpdated");
                })
                ["catch"](function () {
                    self.tdtLoading = false;
                });
        },

        tdtApply(payload) {
            const T = this.tdt;
            const map = this.tdtInstance;

            if (payload.panSeq > this.tdtLastPanSeq) {
                this.tdtLastPanSeq = payload.panSeq;
                const target = payload.panTarget || {};
                this.tdtSelfMovedTo = { lng: target.longitude, lat: target.latitude };
                map.panTo(new T.LngLat(target.longitude, target.latitude));
            } else if (!this.tdtIsSelfMoved(payload.center.longitude, payload.center.latitude)) {
                this.tdtSelfMovedTo = { lng: payload.center.longitude, lat: payload.center.latitude };
                // 天地图无 setCenter，传当前 zoom 走 centerAndZoom 以只改中心点
                map.centerAndZoom(new T.LngLat(payload.center.longitude, payload.center.latitude), map.getZoom());
            }

            if (payload.scale !== this.tdtLastScale) {
                this.tdtLastScale = payload.scale;
                map.setZoom(payload.scale);
            }

            this.tdtRenderMarkers(payload.markers);
            this.tdtRenderLocation(payload.userLocation);
        },

        tdtIsSelfMoved(lng, lat) {
            const last = this.tdtSelfMovedTo;
            if (!last) return false;
            return Math.abs(last.lng - lng) < SAME_POINT_EPS && Math.abs(last.lat - lat) < SAME_POINT_EPS;
        },

        /**
         * 程序化移动同样会触发 moveend，若原样上报会与逻辑层下发的 center 形成回环，
         * 故先比对当前中心点是否就是自己刚设过去的那个点
         */
        tdtEmitRegion(type) {
            const map = this.tdtInstance;
            if (!map) return;
            const center = map.getCenter();
            const lng = Number(center.getLng());
            const lat = Number(center.getLat());
            if (type === "end" && this.tdtIsSelfMoved(lng, lat)) {
                this.tdtSelfMovedTo = null;
                return;
            }
            this.tdtCall("onViewRegionChange", { type: type, centerLocation: { latitude: lat, longitude: lng } });
        },

        tdtRenderMarkers(markers) {
            const T = this.tdt;
            const map = this.tdtInstance;
            const self = this;
            const list = markers || [];

            const key = JSON.stringify(list);
            if (key === this.tdtLastMarkerKey) return;
            this.tdtLastMarkerKey = key;

            this.tdtOverlays.forEach(function (marker) {
                map.removeOverLay(marker);
            });
            this.tdtOverlays = [];

            list.forEach(function (item) {
                if (!item || !item.latitude || !item.longitude) return;

                const options = {};
                if (item.html) {
                    const hw = (item.htmlSize && item.htmlSize.width) || 24;
                    const hh = (item.htmlSize && item.htmlSize.height) || 24;
                    const hax = item.htmlAnchor ? item.htmlAnchor.x : 0.5;
                    const hay = item.htmlAnchor ? item.htmlAnchor.y : 1;
                    options.icon = new T.DivIcon({
                        html: item.html,
                        iconSize: new T.Point(hw, hh),
                        iconAnchor: new T.Point(hw * hax, hh * hay),
                        className: "tig-map-divicon"
                    });
                } else if (item.iconPath) {
                    const w = item.width || 24;
                    const h = item.height || 24;
                    // uni-app 的 anchor 是相对图标尺寸的比例，天地图的 iconAnchor 是像素偏移
                    const ax = item.anchor ? item.anchor.x : 0.5;
                    const ay = item.anchor ? item.anchor.y : 1;
                    options.icon = new T.Icon({
                        iconUrl: item.iconPath,
                        iconSize: new T.Point(w, h),
                        iconAnchor: new T.Point(w * ax, h * ay)
                    });
                }

                const marker = new T.Marker(new T.LngLat(item.longitude, item.latitude), options);
                map.addOverLay(marker);
                marker.addEventListener("click", function (e) {
                    if (e && e.originalEvent && e.originalEvent.stopPropagation) e.originalEvent.stopPropagation();
                    self.tdtCall("onViewMarkerTap", item.id);
                });
                self.tdtOverlays.push(marker);
            });
        },

        /**
         * 定位蓝点。天地图无内置定位图层，用 T.DivIcon 纯 DOM 自绘。
         * 动态创建的节点拿不到 scoped 样式属性，故样式必须内联。
         */
        tdtRenderLocation(location) {
            const T = this.tdt;
            const map = this.tdtInstance;

            const key = location ? location.longitude + "," + location.latitude : "";
            if (key === this.tdtLastLocationKey) return;
            this.tdtLastLocationKey = key;

            if (this.tdtLocationMarker) {
                map.removeOverLay(this.tdtLocationMarker);
                this.tdtLocationMarker = null;
            }
            if (!location) return;

            const size = 22;
            const icon = new T.DivIcon({
                iconSize: new T.Point(size, size),
                iconAnchor: new T.Point(size / 2, size / 2),
                className: "tig-map-locdot",
                html:
                    '<div style="width:14px;height:14px;margin:4px;border-radius:50%;background:#1a73e8;' +
                    'border:2px solid #fff;box-sizing:border-box;box-shadow:0 0 0 4px rgba(26,115,232,.18)"></div>'
            });
            const marker = new T.Marker(new T.LngLat(location.longitude, location.latitude), { icon: icon });
            map.addOverLay(marker);
            this.tdtLocationMarker = marker;
        },

        /** 天地图没有 map.destroy()，只能先清覆盖物再清空容器 DOM */
        tdtDestroy() {
            this.tdtDestroyed = true;
            const map = this.tdtInstance;
            this.tdtOverlays = [];
            this.tdtLocationMarker = null;
            this.tdtInstance = null;
            this.tdtLastMarkerKey = "";
            this.tdtLastLocationKey = "";
            if (map) {
                try {
                    map.clearOverLays();
                } catch (e) {
                    // 实例已随 DOM 一并卸载时忽略
                }
            }
            const el = this.tdtElId ? document.getElementById(this.tdtElId) : null;
            if (el) el.innerHTML = "";
        }
    }
};
</script>

<style lang="scss" scoped>
.tig-map {
    width: 100%;
    height: 100%;
}

.tig-map__inner {
    position: relative;
    width: 100%;
    height: 100%;
}

/* 天地图分支下 slot 内容要浮在底图之上，故底图脱流铺满，叠放次序交给 DOM 顺序。
   z-index: 0 不可省——天地图内部的 tdt-pane 带 z-index 200~700，
   若此处为 auto 则不成层叠上下文，那些 pane 会与 slot 内容同台比较并盖住悬浮导航 */
.tig-map__canvas {
    position: absolute;
    top: 0;
    left: 0;
    z-index: 0;
    width: 100%;
    height: 100%;
}
</style>
