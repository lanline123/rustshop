import type { MapProvider } from "@/types/setting/map";

/** 经纬度元组 [经度, 纬度] */
export type LngLat = [number, number];

/** 适配器需要具备的能力，高德据此决定加载哪些插件，天地图内置全部能力 */
export type MapFeature = "controls" | "geocoder" | "search" | "infoWindow" | "circleEditor";

/** 归一化后的搜索结果，字段口径对齐高德 PlaceSearch 的 poi */
export interface NormalizedPoi {
    name: string;
    address: string;
    location: { lng: number; lat: number };
}

/**
 * 归一化后的逆地理结果
 * @description 天地图 pois 恒为单元素或空数组、aois 恒为空数组、citycode 恒为 null，
 * 消费方按数组遍历即可，无需区分服务商
 */
export interface NormalizedRegeocode {
    formattedAddress: string;
    addressComponent: {
        province: string;
        city: string;
        district: string;
        township: string;
        street: string;
        streetNumber: string;
        adcode: string;
        [key: string]: any;
    };
    pois: Array<{ name: string; address: string; distance: number | null }>;
    aois: Array<{ name: string; address: string }>;
}

/** 半径圆的编辑句柄 */
export interface CircleEditHandle {
    /** 外部改过半径后重新摆放控制点 */
    refresh(): void;
    /** 关闭编辑并清理控制点 */
    destroy(): void;
}

/**
 * 地图能力适配器
 * @description 屏蔽高德与天地图在命名、事件、坐标参数顺序上的差异，
 * 让业务组件只面向一套 API。所有坐标一律使用 [经度, 纬度] 元组
 */
export interface MapAdapter {
    provider: MapProvider;
    /** 底层 SDK 命名空间，仅供适配层内部与极少数特例使用 */
    sdk: any;

    createMap(el: HTMLElement, options: { center: LngLat; zoom: number }): any;
    destroyMap(map: any): void;
    resizeMap(map: any): void;
    addControls(map: any): void;
    setCenter(map: any, center: LngLat): void;
    setZoom(map: any, zoom: number): void;
    getViewState(map: any): { zoom: number; center: LngLat };

    createMarker(map: any, options: { position: LngLat; title?: string; onClick?: () => void }): any;
    removeMarker(map: any, marker: any): void;

    onMapClick(map: any, handler: (position: LngLat) => void): void;
    onViewChange(map: any, handler: () => void): void;

    openInfoWindow(map: any, position: LngLat, content: string): void;
    closeInfoWindow(map: any): void;

    reverseGeocode(position: LngLat): Promise<NormalizedRegeocode | null>;
    searchPlace(keyword: string, options?: { city?: string; map?: any }): Promise<NormalizedPoi[]>;

    createCircle(map: any, options: { center: LngLat; radius: number }): any;
    setCircleRadius(circle: any, radius: number): void;
    getCircleRadius(circle: any): number;
    fitCircleView(map: any, circle: any): void;
    enableCircleRadiusEdit(map: any, circle: any, onAdjust: (radius: number) => void): CircleEditHandle;
}

/** 圆形覆盖物的统一样式，两家服务商共用同一套视觉参数 */
export const CIRCLE_STYLE = {
    strokeColor: "#2e9afc",
    strokeWeight: 6,
    strokeOpacity: 0.2,
    fillColor: "#74b6fd",
    fillOpacity: 0.3
};
