import { CIRCLE_STYLE } from "./types";
import type { CircleEditHandle, LngLat, MapAdapter, NormalizedPoi, NormalizedRegeocode } from "./types";
import { MAP_PROVIDER_TIANDITU } from "@/utils/map/provider";
import { destinationPoint, distanceBetween, normalizeAdcode, parseLngLat } from "@/utils/map/geo";

/** 天地图无 CircleEditor，半径控制点摆在圆周的正东方向 */
const HANDLE_BEARING = 90;
/** 视野包围盒取圆周的正北、正东、正南、正西 */
const VIEWPORT_BEARINGS = [0, 90, 180, 270];
/** 拖拽控制点的最小半径，避免拖到 0 后无法再拉开 */
const MIN_CIRCLE_RADIUS = 10;
/** LocalSearch 的 onSearchComplete 在无结果时可能不回调，需超时兜底 */
const SEARCH_TIMEOUT = 10000;
const SEARCH_PAGE_CAPACITY = 10;

const HANDLE_ICON_SVG =
    '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16">' +
    '<circle cx="8" cy="8" r="6" fill="#ffffff" stroke="#2e9afc" stroke-width="2"/></svg>';
const HANDLE_ICON_URL = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(HANDLE_ICON_SVG)}`;

/** 圆心把手加十字并稍大一圈，与半径控制点区分开 */
const CENTER_HANDLE_ICON_SVG =
    '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18">' +
    '<circle cx="9" cy="9" r="7" fill="#ffffff" stroke="#2e9afc" stroke-width="2"/>' +
    '<path d="M9 5v8M5 9h8" stroke="#2e9afc" stroke-width="1.5"/></svg>';
const CENTER_HANDLE_ICON_URL = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(CENTER_HANDLE_ICON_SVG)}`;

const toText = (value: any): string => (value == null ? "" : String(value));

const toDistance = (value: any): number | null => {
    const num = Number(value);
    return Number.isFinite(num) ? num : null;
};

/**
 * 创建天地图适配器
 * @param T - 已加载的天地图命名空间
 */
export const createTiandituAdapter = (T: any): MapAdapter => {
    /** 天地图无 map.destroy()，重建地图时需要手动清空原容器 */
    const containers = new WeakMap<object, HTMLElement>();

    let geocoder: any = null;

    const toLngLat = ([lng, lat]: LngLat) => new T.LngLat(lng, lat);
    const fromLngLat = (lngLat: any): LngLat => [Number(lngLat.getLng()), Number(lngLat.getLat())];

    const handleIcon = () =>
        new T.Icon({
            iconUrl: HANDLE_ICON_URL,
            iconSize: new T.Point(16, 16),
            iconAnchor: new T.Point(8, 8)
        });

    const centerHandleIcon = () =>
        new T.Icon({
            iconUrl: CENTER_HANDLE_ICON_URL,
            iconSize: new T.Point(18, 18),
            iconAnchor: new T.Point(9, 9)
        });

    /**
     * 天地图逆地理回调在不同版本下可能是包装对象（getXxx 方法）或原始 JSON，此处两种都兼容
     */
    const extractRegeocode = (result: any): { formattedAddress: string; component: any } | null => {
        if (!result) return null;
        const status = typeof result.getStatus === "function" ? result.getStatus() : (result.status ?? result.result?.status);
        if (status != null && String(status) !== "0") return null;

        const component =
            (typeof result.getAddressComponent === "function" ? result.getAddressComponent() : null) ||
            result.addressComponent ||
            result.result?.addressComponent;
        if (!component) return null;

        const formattedAddress =
            (typeof result.getAddress === "function" ? result.getAddress() : "") ||
            result.formatted_address ||
            result.result?.formatted_address ||
            component.address ||
            "";

        return { formattedAddress: toText(formattedAddress), component };
    };

    const extractPois = (result: any): NormalizedPoi[] => {
        const raw = (typeof result?.getPois === "function" ? result.getPois() : result?.pois) || [];
        return (Array.isArray(raw) ? raw : [])
            .map((poi: any) => {
                const position = parseLngLat(poi?.lonlat);
                if (!position) return null;
                return {
                    name: toText(poi?.name),
                    address: toText(poi?.address),
                    location: { lng: position[0], lat: position[1] }
                };
            })
            .filter(Boolean) as NormalizedPoi[];
    };

    return {
        provider: MAP_PROVIDER_TIANDITU,
        sdk: T,

        createMap(el, { center, zoom }) {
            const map = new T.Map(el);
            map.centerAndZoom(toLngLat(center), zoom);
            map.enableDrag?.();
            map.enableScrollWheelZoom?.();
            containers.set(map, el);
            return map;
        },

        destroyMap(map) {
            if (!map) return;
            map.clearOverLays?.();
            const el = containers.get(map);
            if (el) {
                el.innerHTML = "";
                containers.delete(map);
            }
        },

        resizeMap(map) {
            map?.checkResize?.();
        },

        addControls(map) {
            map.addControl(new T.Control.Zoom());
            map.addControl(new T.Control.Scale());
            map.addControl(new T.Control.MapType());
        },

        setCenter(map, center) {
            // 天地图无 setCenter，只有 panTo / centerAndZoom；传入当前 zoom 以只改中心点
            map.centerAndZoom(toLngLat(center), map.getZoom());
        },

        setZoom(map, zoom) {
            map.setZoom(zoom);
        },

        getViewState(map) {
            return { zoom: map.getZoom(), center: fromLngLat(map.getCenter()) };
        },

        createMarker(map, { position, title, onClick }) {
            const marker = new T.Marker(toLngLat(position), { title });
            map.addOverLay(marker);
            if (onClick) {
                marker.addEventListener("click", (e: any) => {
                    e?.originalEvent?.stopPropagation?.();
                    onClick();
                });
            }
            return marker;
        },

        removeMarker(map, marker) {
            map?.removeOverLay?.(marker);
        },

        onMapClick(map, handler) {
            map.addEventListener("click", (e: any) => {
                if (!e?.lnglat) return;
                handler(fromLngLat(e.lnglat));
            });
        },

        onViewChange(map, handler) {
            map.addEventListener("moveend", handler);
            map.addEventListener("zoomend", handler);
        },

        openInfoWindow(map, position, content) {
            const infoWindow = new T.InfoWindow(content, { offset: new T.Point(0, -30), autoPan: true });
            map.openInfoWindow(infoWindow, toLngLat(position));
        },

        closeInfoWindow(map) {
            map?.closeInfoWindow?.();
        },

        reverseGeocode(position): Promise<NormalizedRegeocode | null> {
            return new Promise((resolve) => {
                if (!geocoder) geocoder = new T.Geocoder();
                geocoder.getLocation(toLngLat(position), (result: any) => {
                    const parsed = extractRegeocode(result);
                    if (!parsed) {
                        resolve(null);
                        return;
                    }
                    const { formattedAddress, component } = parsed;
                    const poiName = toText(component.poi);
                    resolve({
                        formattedAddress,
                        addressComponent: {
                            ...component,
                            province: toText(component.province),
                            city: toText(component.city),
                            district: toText(component.county),
                            township: toText(component.town),
                            street: toText(component.road),
                            // 天地图逆地理不返回门牌号
                            streetNumber: "",
                            adcode: normalizeAdcode(component.county_code || component.city_code || component.province_code),
                            towncode: normalizeAdcode(component.town_code),
                            // 天地图无 citycode，恒为空
                            citycode: ""
                        },
                        // 天地图逆地理对一个 POI 只给名称、地址、距离，且恒为单元素
                        pois: poiName
                            ? [{ name: poiName, address: toText(component.address), distance: toDistance(component.poi_distance) }]
                            : [],
                        // 天地图无 aois 对等能力，恒为空数组
                        aois: []
                    });
                });
            });
        },

        searchPlace(keyword, options = {}): Promise<NormalizedPoi[]> {
            const { map = null } = options;
            return new Promise((resolve) => {
                let settled = false;
                let timeoutId: number | null = null;
                const finish = (pois: NormalizedPoi[]) => {
                    if (settled) return;
                    settled = true;
                    if (timeoutId != null) window.clearTimeout(timeoutId);
                    resolve(pois);
                };
                timeoutId = window.setTimeout(() => {
                    finish([]);
                }, SEARCH_TIMEOUT);
                // 每次请求使用独立的 LocalSearch，避免 SDK 回调覆盖并发请求的 resolver。
                const search = new T.LocalSearch(map, {
                    pageCapacity: SEARCH_PAGE_CAPACITY,
                    onSearchComplete: (result: any) => finish(extractPois(result))
                });
                // 天地图无 citylimit，城市限定由调用方在关键词前拼接区域名实现
                search.setQueryType?.(1);
                search.search(keyword);
            });
        },

        createCircle(map, { center, radius }) {
            const circle = new T.Circle(toLngLat(center), radius, {
                color: CIRCLE_STYLE.strokeColor,
                weight: CIRCLE_STYLE.strokeWeight,
                opacity: CIRCLE_STYLE.strokeOpacity,
                fillColor: CIRCLE_STYLE.fillColor,
                fillOpacity: CIRCLE_STYLE.fillOpacity,
                lineStyle: "dashed"
            });
            map.addOverLay(circle);
            return circle;
        },

        setCircleRadius(circle, radius) {
            circle.setRadius(radius);
        },

        getCircleRadius(circle) {
            return Number(circle.getRadius());
        },

        fitCircleView(map, circle) {
            const center = fromLngLat(circle.getCenter());
            const radius = Number(circle.getRadius());
            map.setViewport(VIEWPORT_BEARINGS.map((bearing) => toLngLat(destinationPoint(center, bearing, radius))));
        },

        enableCircleRadiusEdit(map, circle, onAdjust): CircleEditHandle {
            let center = fromLngLat(circle.getCenter());

            // 先添加圆心把手，让它落在半径控制点下层：半径很小时两者会重叠，
            // 此时优先保证半径可调，因为半径会随配置落库、圆心不会
            // 注意：天地图 Marker 的拖拽 handler 在 onAdd 时才创建，draggable 选项会在入图时自动开启拖拽，
            // 不可在 addOverLay 之前调 enableDragging()（内部读 handler 会抛 TypeError）
            const centerHandle = new T.Marker(toLngLat(center), {
                icon: centerHandleIcon(),
                draggable: true
            });
            map.addOverLay(centerHandle);

            const radiusHandle = new T.Marker(toLngLat(destinationPoint(center, HANDLE_BEARING, Number(circle.getRadius()))), {
                icon: handleIcon(),
                draggable: true
            });
            map.addOverLay(radiusHandle);

            /** 把控制点吸附回圆周正东方向 */
            const layoutHandle = (radius: number) => {
                radiusHandle.setLngLat(toLngLat(destinationPoint(center, HANDLE_BEARING, radius)));
            };

            const readRadius = () => Math.max(MIN_CIRCLE_RADIUS, distanceBetween(center, fromLngLat(radiusHandle.getLngLat())));

            /** 圆心跟手走，圆本体与半径控制点整体平移，半径保持不变 */
            const moveCenter = () => {
                center = fromLngLat(centerHandle.getLngLat());
                circle.setCenter(toLngLat(center));
                layoutHandle(Number(circle.getRadius()));
            };
            centerHandle.addEventListener("drag", moveCenter);
            centerHandle.addEventListener("dragend", moveCenter);

            // 拖拽过程中不回写控制点位置，避免与拖拽手势打架，松手后再吸附回正东
            radiusHandle.addEventListener("drag", () => {
                const radius = readRadius();
                circle.setRadius(radius);
                onAdjust(radius);
            });
            radiusHandle.addEventListener("dragend", () => {
                const radius = readRadius();
                circle.setRadius(radius);
                layoutHandle(radius);
                onAdjust(radius);
            });

            return {
                refresh() {
                    // 从圆本体重读圆心，兼容外部直接改过圆心的情况
                    center = fromLngLat(circle.getCenter());
                    centerHandle.setLngLat(toLngLat(center));
                    layoutHandle(Number(circle.getRadius()));
                },
                destroy() {
                    map.removeOverLay?.(centerHandle);
                    map.removeOverLay?.(radiusHandle);
                }
            };
        }
    };
};
