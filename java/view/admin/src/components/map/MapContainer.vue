/**
 * @file 地图容器组件
 * @description 按后台配置的服务商加载高德或天地图，支持地点搜索、标记点、城市限制等功能
 * @features
 * - 按 mapProvider 动态加载对应地图 SDK
 * - 地点搜索与结果展示
 * - 支持城市范围限制
 * - 标记点管理
 * - 区域名称自动拼接搜索
 */
<template>
    <div class="map-page">
        <div class="map-content">
            <div class="map-card">
                <div v-if="$slots.toolbar" class="map-toolbar">
                    <slot name="toolbar"></slot>
                </div>
                <div id="container" class="map-container" ref="mapContainer" :style="{ height: mapHeightStyle }">
                    <div v-if="loading" class="loading-overlay">
                        <div class="spinner"></div>
                        <p>{{ $t('地图加载中，请稍候...') }}</p>
                    </div>
                </div>
                <div v-if="error" class="error-message"> {{ $t('地图加载失败，请检查网络连接或刷新页面重试。') }} <p>{{ $t('错误信息:{errorMessage}', { errorMessage }) }}</p>
                </div>
            </div>

            <!-- 自定义搜索结果的显示 -->
            <div v-if="searchResults.length > 0" class="custom-search-results">
                <div class="results-header">
                    <h3>{{ $t('搜索结果') }}</h3>
                </div>
                <div class="results-list">
                    <div v-for="(result, index) in searchResults" :key="index" class="result-item" @click="selectSearchResult(result)">
                        <div class="result-name">{{ result.name }}</div>
                        <div class="result-address">{{ result.address }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from "vue";
import { createMapAdapter } from "@/components/map/providers";
import { parseLngLat } from "@/utils/map/geo";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    /** 默认位置坐标（经度,纬度） */
    defaultPosition: { type: String, default: "" },
    /** 是否在弹窗中使用 */
    isDialog: Boolean,
    /** 区域名称数组，用于搜索前缀 */
    regionNames: { type: Array, default: () => [] },
    // 附近搜索（PlaceSearch.searchNearBy）是否自动渲染到地图上（可能会出现范围圈/结果点）
    // 默认保持原有行为：true（渲染）。表单选点等场景可传 false 禁用渲染。
    nearbySearchRenderOnMap: {
        type: Boolean,
        default: true
    },
    // 用户手动选点（点击地图/点击标记/选择搜索结果）时，是否在标记点上弹出地址信息（InfoWindow）
    // 默认 false：避免影响其它业务页面；需要时由业务方显式开启
    showInfoWindowOnManualAction: {
        type: Boolean,
        default: false
    }
});

/** 计算搜索前缀（区域名称拼接） */
const searchPrefix = computed(() => props.regionNames?.join("") || "");
/** 计算城市名称（用于搜索限制） */
const computedCity = computed(() => props.regionNames?.[2] || "");
/** 默认位置坐标 */
const defaultPosition = computed(() => {
    if(props.defaultPosition){
        return props.defaultPosition.split(",").map(Number);
    }
    return [116.397428, 39.90923];
});

const emit = defineEmits(["positionChange"]);

// 地图相关实例
let map = null;
/** 当前服务商的能力适配器，屏蔽高德与天地图的 API 差异 */
let adapter = null;
const mapContainer = ref(null);
let resizeObserver = null;
/** 当前搜索城市限制 */
const currentCity = ref("");

// 响应式状态
/** 加载状态 */
const loading = ref(true);
/** 错误状态 */
const error = ref(false);
/** 错误信息 */
const errorMessage = ref("");
/** 缩放级别 */
const zoomLevel = ref(10);
/** 地图中心点 */
const mapCenter = ref([116.397428, 39.90923].join(", "));
/** 选中的标记点 */
const selectedMarker = ref(null);
/** 搜索关键字 */
const searchKeyword = ref("");
/** 搜索结果列表 */
const searchResults = ref([]);
/** 最近搜索记录 */
const recentSearches = ref([]);
/** 标记点数组 */
const markers = ref([]);

/**
 * 添加标记点
 * @param {number} lng - 经度
 * @param {number} lat - 纬度
 * @param {string} title - 标记标题
 */
const addMarker = (lng, lat, title) => {
    if (!map || !adapter) return null;
    return adapter.createMarker(map, {
        position: [lng, lat],
        title,
        onClick: () => getAddressByPosition(lng, lat, title, true, { source: "markerClick" })
    });
};

/** 清除全部标记点 */
const clearMarkers = () => {
    markers.value.forEach((marker) => adapter?.removeMarker(map, marker));
    markers.value = [];
};

/** 更新地图信息（缩放级别、中心点） */
const updateMapInfo = () => {
    if (!map || !adapter) return;
    const { zoom, center } = adapter.getViewState(map);
    zoomLevel.value = zoom;
    mapCenter.value = center.join(", ");
};

/** 初始化地图：按后台配置的服务商加载对应 SDK 并建图 */
const initMap = async () => {
    loading.value = true;
    error.value = false;

    try {
        adapter = await createMapAdapter({ features: ["controls", "geocoder", "search", "infoWindow"] });

        const center = parseLngLat(props.defaultPosition || mapCenter.value);
        if (!center) {
            error.value = true;
            errorMessage.value = t('无效的默认位置，请检查输入的坐标。');
            return;
        }

        map = adapter.createMap(mapContainer.value, { zoom: zoomLevel.value, center });
        adapter.addControls(map);

        if (props.defaultPosition) {
            const marker = addMarker(center[0], center[1], t('默认位置'));
            if (marker) markers.value.push(marker);
        }

        // 点击地图选点
        adapter.onMapClick(map, ([lng, lat]) => {
            clearMarkers();
            const marker = addMarker(lng, lat, t('选中位置'));
            if (marker) markers.value.push(marker);
            adapter.setCenter(map, [lng, lat]);
            getAddressByPosition(lng, lat, t('选中位置'), true, { source: "mapClick" });
        });

        adapter.onViewChange(map, updateMapInfo);

        if (typeof ResizeObserver !== "undefined" && mapContainer.value) {
            resizeObserver = new ResizeObserver(() => {
                adapter?.resizeMap(map);
            });
            resizeObserver.observe(mapContainer.value);
        }
    } catch (e) {
        console.error(e);
        error.value = true;
        errorMessage.value = e.message;
    } finally {
        loading.value = false;
    }
};

/**
 * 搜索地点
 * @param {string} keyword - 搜索关键字
 */
const search = (keyword) => {
    let finalKeyword = keyword || "";
    // 如果有搜索前缀，且关键词不包含该前缀，自动添加
    if (searchPrefix.value && finalKeyword && !finalKeyword.includes(searchPrefix.value)) {
        finalKeyword = searchPrefix.value + finalKeyword;
    }
    searchKeyword.value = finalKeyword;
    searchLocation();
};

/** 执行地点搜索 */
const searchLocation = async () => {
    if (!adapter || !map || !searchKeyword.value.trim()) return;

    try {
        const pois = await adapter.searchPlace(searchKeyword.value, { city: currentCity.value, map });
        searchResults.value = pois;

        // 保存到最近搜索
        if (!recentSearches.value.includes(searchKeyword.value)) {
            recentSearches.value.unshift(searchKeyword.value);
            // 只保留最近5个搜索
            if (recentSearches.value.length > 5) {
                recentSearches.value.pop();
            }
        }
        // 直接标记第一个搜索结果的坐标
        if (pois.length > 0) {
            markLocation(pois[0].location, pois[0].name);
        }
    } catch (e) {
        console.error("搜索失败:", e);
    }
};

/**
 * 标记位置
 * @param {Object} location - 位置对象（包含lng, lat）
 * @param {string} name - 位置名称
 */
const markLocation = (location, name) => {
    if (!map) {
        console.error("地图未加载，无法添加标记");
        return;
    }
    const marker = addMarker(location.lng, location.lat, name);
    if (marker) {
        markers.value.push(marker);
        selectedMarker.value = {
            title: name,
            position: [location.lng, location.lat],
            time: new Date().toLocaleTimeString()
        };
    }
};

// 处理逆地理编码结果，拼接 POI 名称 + 详细地址（与 o2o-pc 一致的策略）
const normalizeRegeoResult = (regeocode) => {
    const addressComponent = regeocode?.addressComponent || {};
    const formattedAddress = regeocode?.formattedAddress || "";
    const pois = Array.isArray(regeocode?.pois) ? regeocode.pois : [];
    const aois = Array.isArray(regeocode?.aois) ? regeocode.aois : [];

    let poiName = "";
    let poiAddress = "";

    if (pois.length > 0) {
        const sortedPois = [...pois].sort((a, b) => {
            const da = typeof a?.distance === "number" ? a.distance : Number.POSITIVE_INFINITY;
            const db = typeof b?.distance === "number" ? b.distance : Number.POSITIVE_INFINITY;
            return da - db;
        });
        const nearestPoi = sortedPois.find((poi) => typeof poi?.distance === "number" && poi.distance <= 80) || sortedPois[0];
        poiName = nearestPoi?.name || "";
        poiAddress = nearestPoi?.address || "";
    }

    if (!poiAddress && aois.length > 0) {
        poiAddress = aois[0]?.address || aois[0]?.name || "";
    }

    if (!poiAddress) {
        const township = addressComponent?.township || "";
        const street = addressComponent?.street || "";
        const streetNumber = addressComponent?.streetNumber || "";
        poiAddress = `${township}${street}${streetNumber}`.trim();
    }

    if (!poiAddress && formattedAddress) {
        const province = addressComponent?.province || "";
        const city = addressComponent?.city || "";
        const district = addressComponent?.district || "";
        let temp = formattedAddress;
        if (province) temp = temp.replace(new RegExp(`^${province}`), "");
        if (city && city !== province) temp = temp.replace(new RegExp(`^${city}`), "");
        if (district) temp = temp.replace(new RegExp(`^${district}`), "");
        poiAddress = temp.replace(/^[省市区县\s]+/, "").trim() || formattedAddress;
    }

    const combinedAddress = [poiName, poiAddress].filter(Boolean).join(" ").trim();
    return {
        poiName,
        poiAddress,
        combinedAddress: combinedAddress || formattedAddress || poiName || poiAddress || ""
    };
};

// 显示信息窗口（InfoWindow）
const showInfoWindow = (lng, lat, content) => {
    if (!map || !adapter) return;
    if (!props.showInfoWindowOnManualAction) return;
    adapter.openInfoWindow(map, [lng, lat], content);
};

const reverseGeocode = (lng, lat) => {
    if (!adapter) return Promise.resolve(null);
    return adapter.reverseGeocode([lng, lat]);
};


// 使用逆地理编码获取详细地址，并向父组件抛出（与 o2o-pc 对齐）
// meta：可携带来源信息（例如 poiSelect），供业务侧避免二次覆盖选择状态
const getAddressByPosition = async (lng, lat, fallbackTitle = t('选中位置'), isUserManualAction = false, meta = null) => {
    const regeocode = await reverseGeocode(lng, lat);
    if (!regeocode) {
        emit("positionChange", {
            position: [lng, lat],
            name: fallbackTitle,
            address: fallbackTitle,
            formattedAddress: fallbackTitle,
            isUserManualAction,
            ...(meta ? { meta } : {})
        });
        return;
    }

    const parsed = normalizeRegeoResult(regeocode);
    const markerTitle = parsed.combinedAddress || fallbackTitle;
    const addressComponent = regeocode?.addressComponent || {};

    selectedMarker.value = {
        title: markerTitle,
        position: [lng, lat],
        time: new Date().toLocaleTimeString()
    };

    // 用户手动操作：显示信息窗口
    if (isUserManualAction) {
        const fullAddress = parsed.combinedAddress || regeocode?.formattedAddress || markerTitle;
        if (fullAddress) showInfoWindow(lng, lat, fullAddress);
    }

    emit("positionChange", {
        position: [lng, lat],
        name: parsed.poiName || markerTitle,
        address: parsed.combinedAddress || regeocode?.formattedAddress || markerTitle,
        poiName: parsed.poiName,
        poiAddress: parsed.poiAddress,
        formattedAddress: regeocode?.formattedAddress || "",
        isUserManualAction,
        ...(meta ? { meta } : {}),
        // 兼容不同调用方：保留完整 addressComponent，同时提供常用字段（含 adcode）
        addressComponent: {
            ...addressComponent,
            province: addressComponent?.province || "",
            city: addressComponent?.city || "",
            district: addressComponent?.district || "",
            adcode: addressComponent?.adcode || ""
        }
    });
};

/**
 * 选择搜索结果
 * @param {Object} result - 搜索结果项
 */
const selectSearchResult = (result) => {
    if (!map || !adapter) return;

    // 清除之前的标记点
    clearMarkers();

    // 移动到搜索结果位置
    adapter.setCenter(map, [result.location.lng, result.location.lat]);
    adapter.setZoom(map, 15);

    // 添加标记
    const marker = addMarker(result.location.lng, result.location.lat, result.name);
    // 用户选择搜索结果：视为用户手动操作
    getAddressByPosition(result.location.lng, result.location.lat, result.name || t('选中位置'), true, { source: "searchResult" });

    // 添加新标记点到 markers 数组
    if (marker) markers.value.push(marker);
};

/** 清除搜索结果和标记点 */
const clearSearch = () => {
    searchResults.value = [];
    searchKeyword.value = "";
    clearMarkers();
    selectedMarker.value = null;
    adapter?.closeInfoWindow(map);
};

onMounted(() => {
    initMap();
});

onUnmounted(() => {
    resizeObserver?.disconnect();
    resizeObserver = null;
    if (map && adapter) {
        adapter.destroyMap(map);
    }
    map = null;
    adapter = null;
});

/**
 * 设置搜索城市限制（供父组件调用）
 * @param {string} city - 城市名称
 */
const setCity = (city) => {
    currentCity.value = city || "";
};

// 监听 regionNames 变化，自动更新城市限制
watch(
    computedCity,
    (city) => {
        if (city && city !== currentCity.value) {
            currentCity.value = city;
        }
    },
    { immediate: true }
);

// 监听 defaultPosition 变化，移动地图到默认位置
watch(defaultPosition, (newVal) => {
    if (newVal && map && adapter) {
        // 清除之前的标记点
        clearMarkers();
        // 添加新的标记点
        adapter.setCenter(map, [newVal[0], newVal[1]]);
        const marker = addMarker(newVal[0], newVal[1], t('默认位置'));
        if (marker) markers.value.push(marker);
    }
});

defineExpose({
    search,
    clearSearch,
    setCity
});
</script>

<style scoped lang="less">
.map-page {
    padding-bottom: 20px;
    color: #333;
}

.map-card {
    position: relative;
}

.map-toolbar {
    position: absolute;
    left: 12px;
    top: 12px;
    z-index: 10;
    width: 70%;
    pointer-events: none;

    > * {
        pointer-events: auto;
    }
}

.header {
    text-align: center;
    margin-bottom: 30px;
    padding: 20px;
    background: linear-gradient(135deg, #2466e9 0%, #1a4fbf 100%);
    color: white;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(36, 102, 233, 0.2);
}

.header h1 {
    font-size: 2.2rem;
    margin-bottom: 10px;
}

.subtitle {
    font-size: 1.1rem;
    opacity: 0.9;
}

.search-container {
    margin-bottom: 20px;
    background: white;
    padding: 20px;
    border-radius: 5px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.search-box {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
}

.search-input {
    flex: 1;
    padding: 12px 15px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 16px;
    transition: border-color 0.3s;
}

.search-input:focus {
    outline: none;
    border-color: #2466e9;
    box-shadow: 0 0 0 2px rgba(36, 102, 233, 0.2);
}

.search-btn {
    padding: 12px 20px;
    background: #2466e9;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 5px;
    transition: background 0.3s;
    z-index: 1; /* 确保按钮在其他元素之上 */
}

.search-btn:hover {
    background: #1a57c9;
}

.custom-search-results {
    width: 250px;
    border: 1px solid #eaeef2;
    border-radius: 6px;
    overflow: hidden;
    .results-list {
        height: 450px;
        overflow: auto;
    }
}

.results-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 15px;
    background: #f9fafc;
    border-bottom: 1px solid #eaeef2;
}

.results-header h3 {
    margin: 0;
    font-size: 16px;
    color: #2466e9;
}

.clear-btn {
    background: none;
    border: none;
    color: #666;
    cursor: pointer;
    font-size: 14px;
}

.clear-btn:hover {
    color: #2466e9;
}

.result-item {
    line-height: 18px;
    padding: 10px 15px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background 0.2s;
}

.result-item:hover {
    background: #f0f7ff;
}

.result-item:last-child {
    border-bottom: none;
}

.result-name {
    font-weight: 500;
    margin-bottom: 5px;
    color: #333;
}

.result-address {
    font-size: 14px;
    color: #666;
}

.map-content {
    display: flex;
    gap: 20px;
}

.map-card {
    width: 500px;
    height: 500px;
    background: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.map-container {
    width: 100%;
    height: 500px;
    position: relative;
    /* 天地图 4.0 为 Leaflet 内核，内部图层 z-index 达 400~1000，需自成层叠上下文，否则会盖住 .map-toolbar */
    z-index: 0;
}

.loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    flex-direction: column;
    gap: 15px;
}

.spinner {
    width: 50px;
    height: 50px;
    border: 5px solid #e8e8e8;
    border-top: 5px solid #2466e9;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}

.error-message {
    background: #ffebee;
    color: #d32f2f;
    padding: 15px;
    border-radius: 8px;
    margin: 15px;
}

.controls {
    padding: 20px;
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
    background: #f9fafc;
    border-top: 1px solid #eaeef2;
}

.btn {
    padding: 10px 18px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 500;
    transition: all 0.2s ease;
}

.btn-primary {
    background: #2466e9;
    color: white;
}

.btn-primary:hover {
    background: #1a57c9;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(36, 102, 233, 0.3);
}

.btn-outline {
    background: transparent;
    border: 1px solid #2466e9;
    color: #2466e9;
}

.btn-outline:hover {
    background: #f0f5ff;
}

.info-panel {
    background: white;
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.info-panel h2 {
    margin-bottom: 15px;
    color: #2466e9;
    font-size: 1.4rem;
}

.info-content {
    line-height: 1.6;
}

.marker-info {
    background: #f0f7ff;
    padding: 15px;
    border-radius: 8px;
    margin-top: 15px;
    border-left: 4px solid #2466e9;
}

.recent-searches {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px solid #eee;
}

.recent-searches h3 {
    font-size: 16px;
    color: #2466e9;
    margin-bottom: 10px;
}

.recent-searches ul {
    list-style: none;
    padding: 0;
}

.recent-searches li {
    padding: 8px 12px;
    background: #f9f9f9;
    border-radius: 4px;
    margin-bottom: 5px;
    cursor: pointer;
    transition: background 0.2s;
}

.recent-searches li:hover {
    background: #f0f7ff;
}

.footer {
    text-align: center;
    margin-top: 40px;
    padding: 20px;
    color: #666;
    font-size: 0.9rem;
}

@media (min-width: 768px) {
    .map-content {
        flex-direction: row;
    }

    .map-card {
        flex: 2;
    }

    .info-panel {
        flex: 1;
    }
}

@media (max-width: 767px) {
    .map-container {
        height: 400px;
    }

    .controls {
        flex-direction: column;
    }

    .btn {
        width: 100%;
    }

    .search-box {
        flex-direction: column;
    }
}
</style>
