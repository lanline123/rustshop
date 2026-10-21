import { CIRCLE_STYLE } from "./types";
import type { CircleEditHandle, LngLat, MapAdapter, NormalizedPoi, NormalizedRegeocode } from "./types";
import { MAP_PROVIDER_AMAP } from "@/utils/map/provider";

/** 高德部分字段在直辖市等场景下会返回空数组，统一压成字符串 */
const toText = (value: any): string => {
    if (Array.isArray(value)) return value.length ? String(value[0] ?? "") : "";
    return value == null ? "" : String(value);
};

const toDistance = (value: any): number | null => {
    const num = Number(value);
    return Number.isFinite(num) ? num : null;
};

/**
 * 创建高德适配器
 * @param AMap - 已加载的 AMap 命名空间
 */
export const createAmapAdapter = (AMap: any): MapAdapter => {
    let geocoder: any = null;
    let infoWindow: any = null;
    let placeSearch: any = null;
    let placeSearchMap: any = null;
    let placeSearchCity = "";

    const ensureGeocoder = () => {
        if (!geocoder) {
            geocoder = new AMap.Geocoder({ radius: 500, extensions: "all" });
        }
        return geocoder;
    };

    const ensurePlaceSearch = (map: any, city: string) => {
        if (!placeSearch || placeSearchMap !== map || placeSearchCity !== city) {
            placeSearch = new AMap.PlaceSearch({
                map,
                city: city || undefined,
                citylimit: !!city
            });
            placeSearchMap = map;
            placeSearchCity = city;
        }
        return placeSearch;
    };

    return {
        provider: MAP_PROVIDER_AMAP,
        sdk: AMap,

        createMap(el, { center, zoom }) {
            return new AMap.Map(el, { zoom, center, viewMode: "2D" });
        },

        destroyMap(map) {
            infoWindow = null;
            placeSearch = null;
            placeSearchMap = null;
            placeSearchCity = "";
            map?.destroy?.();
        },

        resizeMap(_map) {
            // 高德地图会自行响应容器尺寸变化；保留统一适配器入口以避免业务层判断服务商。
        },

        addControls(map) {
            map.addControl(new AMap.Scale());
            map.addControl(new AMap.ToolBar());
            map.addControl(new AMap.MapType());
        },

        setCenter(map, center) {
            map.setCenter(center);
        },

        setZoom(map, zoom) {
            map.setZoom(zoom);
        },

        getViewState(map) {
            const center = map.getCenter();
            return { zoom: map.getZoom(), center: [center.getLng(), center.getLat()] };
        },

        createMarker(map, { position, title, onClick }) {
            const marker = new AMap.Marker({ position, title });
            if (onClick) {
                marker.on("click", (e: any) => {
                    e?.domEvent?.stopPropagation?.();
                    onClick();
                });
            }
            map.add(marker);
            return marker;
        },

        removeMarker(_map, marker) {
            marker?.setMap?.(null);
        },

        onMapClick(map, handler) {
            map.on("click", (e: any) => handler([e.lnglat.getLng(), e.lnglat.getLat()]));
        },

        onViewChange(map, handler) {
            map.on("moveend", handler);
            map.on("zoomchange", handler);
        },

        openInfoWindow(map, position, content) {
            infoWindow?.close?.();
            infoWindow = new AMap.InfoWindow({
                content,
                offset: new AMap.Pixel(0, -40),
                closeWhenClickMap: true,
                autoMove: true
            });
            infoWindow.open(map, position);
        },

        closeInfoWindow() {
            infoWindow?.close?.();
            infoWindow = null;
        },

        reverseGeocode(position): Promise<NormalizedRegeocode | null> {
            return new Promise((resolve) => {
                ensureGeocoder().getAddress(position, (status: string, result: any) => {
                    const regeocode = status === "complete" ? result?.regeocode : null;
                    if (!regeocode) {
                        resolve(null);
                        return;
                    }
                    const component = regeocode.addressComponent || {};
                    resolve({
                        formattedAddress: toText(regeocode.formattedAddress),
                        addressComponent: {
                            ...component,
                            province: toText(component.province),
                            city: toText(component.city),
                            district: toText(component.district),
                            township: toText(component.township),
                            street: toText(component.street),
                            streetNumber: toText(component.streetNumber),
                            adcode: toText(component.adcode)
                        },
                        pois: (Array.isArray(regeocode.pois) ? regeocode.pois : []).map((poi: any) => ({
                            name: toText(poi?.name),
                            address: toText(poi?.address),
                            distance: toDistance(poi?.distance)
                        })),
                        aois: (Array.isArray(regeocode.aois) ? regeocode.aois : []).map((aoi: any) => ({
                            name: toText(aoi?.name),
                            address: toText(aoi?.address)
                        }))
                    });
                });
            });
        },

        searchPlace(keyword, options = {}): Promise<NormalizedPoi[]> {
            const { city = "", map = null } = options;
            return new Promise((resolve, reject) => {
                const search = ensurePlaceSearch(map, city);
                search.search(keyword, (status: string, result: any) => {
                    if (status !== "complete" || result?.info !== "OK") {
                        reject(new Error(result?.info || status));
                        return;
                    }
                    const pois = result?.poiList?.pois || [];
                    resolve(
                        pois.map((poi: any) => ({
                            name: toText(poi?.name),
                            address: toText(poi?.address),
                            location: { lng: Number(poi?.location?.lng), lat: Number(poi?.location?.lat) }
                        }))
                    );
                });
            });
        },

        createCircle(map, { center, radius }) {
            const circle = new AMap.Circle({
                center,
                radius,
                borderWeight: 3,
                strokeColor: CIRCLE_STYLE.strokeColor,
                strokeWeight: CIRCLE_STYLE.strokeWeight,
                strokeOpacity: CIRCLE_STYLE.strokeOpacity,
                strokeStyle: "dashed",
                strokeDasharray: [10, 10],
                fillColor: CIRCLE_STYLE.fillColor,
                fillOpacity: CIRCLE_STYLE.fillOpacity,
                zIndex: 50
            });
            map.add(circle);
            return circle;
        },

        setCircleRadius(circle, radius) {
            circle.setRadius(radius);
        },

        getCircleRadius(circle) {
            return circle.getRadius();
        },

        fitCircleView(map, circle) {
            map.setFitView([circle]);
        },

        enableCircleRadiusEdit(map, circle, onAdjust): CircleEditHandle {
            const editor = new AMap.CircleEditor(map, circle);
            editor.open();
            const notify = () => onAdjust(circle.getRadius());
            editor.on("adjust", notify);
            editor.on("end", notify);
            return {
                refresh() {
                    editor.close();
                    editor.open();
                },
                destroy() {
                    editor.close();
                }
            };
        }
    };
};
