import { defineStore } from "pinia";
import { ref } from "vue";
import { getLocationByLocation, getLocationByIp } from "@/api/region";
import type { ByLocationData } from "@/types/region";
import { getStoreList, getStoreDetail } from "@/api/store/store";
import type { StoreListRecords } from "@/types/store/store";
import { useConfigStore } from "@/store/config";
import { isO2o } from "@/utils";

export const useRegionStore = defineStore("region", () => {
    const currentRegion = ref<ByLocationData>(uni.getStorageSync("currentRegion") ? JSON.parse(uni.getStorageSync("currentRegion")) : ({} as ByLocationData));

    // 当前选择的展示名（收货地址/地图选点的详细地址）；为空时回退展示 currentRegion.regionName
    const currentSelectedName = ref<string>(uni.getStorageSync("currentSelectedName") || "");

    // 当前选择的收货地址 id；0 表示非收货地址来源（城市选择/地图选点/定位），用于结算页精确匹配默认地址
    const currentSelectedAddressId = ref<number>(Number(uni.getStorageSync("currentSelectedAddressId")) || 0);

    const currentLocationInfo = ref(
        uni.getStorageSync("currentLocationInfo") ? JSON.parse(uni.getStorageSync("currentLocationInfo")) : { latitude: 0, longitude: 0 }
    );

    const setCurrent = ref(false);

    const realTimeRegion = ref<ByLocationData>({} as ByLocationData);

    const realTimeLocationInfo = ref({
        latitude: 0,
        longitude: 0
    });

    const nearestShopInfo = ref<StoreListRecords>({} as StoreListRecords);

    // 按定位距离算出的最近门店，供"最近门店"入口直达使用；
    // 与受进店规则控制的 nearestShopInfo 解耦，不影响头部展示
    const nearestShopByLocation = ref<StoreListRecords>({} as StoreListRecords);

    // 会话级恢复：H5 刷新会重建内存态，用 sessionStorage 兜底（关闭标签即清空）；
    // 小程序/App 靠进程内存，退出商城自动清空——产品要求下次进入需重新选门店，故不落 uni storage
    const readSessionShop = (): StoreListRecords => {
        // #ifdef H5
        const cached = sessionStorage.getItem("currentShop");
        if (cached) return JSON.parse(cached) as StoreListRecords;
        // #endif
        return {} as StoreListRecords;
    };

    // 用户手动选中的当前门店，区别于按距离自动计算的最近门店 nearestShopInfo；仅本次会话有效
    const currentShop = ref<StoreListRecords>(readSessionShop());

    const loading = ref(false);

    const setCurrentRegion = (val: ByLocationData) => {
        currentRegion.value = val;
        uni.setStorageSync("currentRegion", JSON.stringify(val));
        // 以城市为单位的选择重置展示名与收货地址 id；收货地址/地图选点流程随后单独写入
        setCurrentSelectedName("");
        setCurrentSelectedAddressId(0);
    };

    const setCurrentSelectedName = (val: string) => {
        currentSelectedName.value = val;
        uni.setStorageSync("currentSelectedName", val);
    };

    const setCurrentSelectedAddressId = (val: number) => {
        currentSelectedAddressId.value = Number(val) || 0;
        uni.setStorageSync("currentSelectedAddressId", currentSelectedAddressId.value);
    };

    const setCurrentLocation = (val: { latitude: number; longitude: number }) => {
        currentLocationInfo.value = val;
        uni.setStorageSync("currentLocationInfo", JSON.stringify(val));
    };

    const setCurrentShop = (val: StoreListRecords) => {
        currentShop.value = val;
        // #ifdef H5
        sessionStorage.setItem("currentShop", JSON.stringify(val));
        // #endif
    };

    const setRealTimeRegion = (val: ByLocationData) => {
        realTimeRegion.value = val;
    };

    const setRealTimeLocation = (val: { latitude: number; longitude: number }) => {
        realTimeLocationInfo.value = val;
        if (setCurrent.value) {
            setCurrentLocation(val);
        }
    };

    const setCurrentFlag = (val: boolean) => {
        setCurrent.value = val;
    };

    const getLocationByLocationData = async (location: string) => {
        try {
            loading.value = true;
            const res = await getLocationByLocation(location);

            setRealTimeRegion(res);

            if (setCurrent.value) {
                setCurrentRegion(res);
                getNearestShopId();

                setCurrentFlag(false);
            }

            loading.value = false;
        } catch (error) {
            console.error("通过gps获取城市定位失败:", error);
            await getLocationByIpData();
        }
    };

    const getLocationByIpData = async () => {
        try {
            const res = await getLocationByIp();
            if (res.centor) {
                const centor = res.centor.split(",");
                setCurrentLocation({ latitude: Number(centor[1]), longitude: Number(centor[0]) });
                setRealTimeLocation({ latitude: Number(centor[1]), longitude: Number(centor[0]) });
            }

            setRealTimeRegion(res);

            if (setCurrent.value) {
                setCurrentRegion(res);

                getNearestShopId();

                setCurrentFlag(false);
            }
        } catch (error) {
            console.error("通过ip获取城市定位失败:", error);
        } finally {
            loading.value = false;
        }
    };

    // 从原始值解析 shopId：为空/无效返回 null，合法数字（含自营门店 0）原样返回
    const parseShopId = (raw: unknown): number | null => {
        if (raw === undefined || raw === null || raw === "") return null;
        const num = Number(raw);
        return Number.isFinite(num) && num >= 0 ? num : null;
    };

    // 从 URL / 小程序启动参数中读取推广的 shopId（推广店铺规则用）
    // 返回 null 表示未携带 shopId；返回数字（含自营门店 0）表示带了有效 shopId
    const getPromotionShopId = (): number | null => {
        try {
            // #ifdef H5
            const search = new URLSearchParams(window.location.search);
            const raw = search.has("shopId") ? search.get("shopId") : search.has("shop_id") ? search.get("shop_id") : null;
            const idFromUrl = parseShopId(raw);
            if (idFromUrl !== null) return idFromUrl;
            // #endif
            // #ifdef MP || APP-PLUS || APP-HARMONY
            try {
                const opts: any = (uni as any).getLaunchOptionsSync ? (uni as any).getLaunchOptionsSync() : {};
                const query = opts?.query || opts?.referrerInfo?.extraData || {};
                const rawQuery = query.shopId ?? query.shop_id;
                const idFromQuery = parseShopId(rawQuery);
                if (idFromQuery !== null) return idFromQuery;
                // 小程序扫码进入，scene 里可能带 shopId：scene=shopId%3D0 → shopId=0
                if (opts?.scene) {
                    const decoded = decodeURIComponent(String(opts.scene));
                    const match = decoded.match(/shop[_]?[iI]d[=:](\d+)/);
                    if (match) return parseShopId(match[1]);
                }
            } catch {
                // 忽略：部分平台不支持
            }
            // #endif
        } catch (error) {
            console.warn("解析推广门店 shopId 失败:", error);
        }
        return null;
    };

    // 按规则进店结果：allowEnterHome=false 表示只绑定门店、留在原链接页，不跳门店首页
    type ShopResolveResult = { shop: StoreListRecords; allowEnterHome: boolean } | null;

    // 按规则进店：按 priority 升序遍历启用的规则，命中即返回
    const resolveShopByRules = async (): Promise<ShopResolveResult> => {
        const configStore = useConfigStore();
        const rules = [...(configStore.storeEntryFirstRules || [])]
            .filter((r) => Number(r?.enabled) === 1)
            .sort((a, b) => (Number(a.priority) || 0) - (Number(b.priority) || 0));
        for (const rule of rules) {
            const ruleType = Number(rule.ruleType);

            // 规则 1：买家进入推广店铺（从 URL / 小程序启动参数取 shopId）
            // 命中后只绑定当前门店，留在推广链接对应页面，不强制跳门店首页
            if (ruleType === 1) {
                const shopId = getPromotionShopId();
                // 仅当链接/启动参数确实带了 shopId（含自营门店 0）时才命中规则 1，否则交由后续规则处理
                if (shopId !== null) {
                    try {
                        const detail: any = await getStoreDetail({ shopId });
                        if (detail && detail.shopId >= 0) {
                            return { shop: detail as StoreListRecords, allowEnterHome: false };
                        }
                    } catch (e) {
                        console.warn("获取推广门店详情失败:", e);
                    }
                }
                continue;
            }

            // 规则 2：从总部进店，进入同城配送范围内距离最近的门店
            // 通过 deliveryType=5（同城配送）筛选支持同城配送的门店；命中即返回
            // 若接口返回空，交由后续规则 3 兜底（最近门店 / 指定门店）
            if (ruleType === 2) {
                try {
                    const result = await getStoreList({
                        regionId: currentRegion.value.regionId,
                        page: 1,
                        size: 1,
                        ...currentLocationInfo.value,
                        deliveryTypes: "4,5",
                        sortField: "distance",
                        sortOrder: "asc"
                    });
                    // 超出配送范围（inServiceRange 为 false）则不命中，继续走规则 3
                    const nearest = result.records?.[0];
                    if (nearest && nearest.inLocalDeliveryRange !== false) {
                        return { shop: nearest, allowEnterHome: true };
                    }
                } catch (e) {
                    console.warn("获取同城配送范围内门店失败:", e);
                }
                continue;
            }

            // 规则 3：无法识别身份时，按销售员气泡配置决定
            //  - salesmanEntryBubbleType === 2 且指定了 shopId：进入指定门店
            //  - 否则：进入距离最近的门店
            if (ruleType === 3) {
                const bubbleType = Number(configStore.salesmanEntryBubbleType) || 1;
                const bubbleShopId = Number(configStore.salesmanEntryShopId) || 0;
                if (bubbleType === 2 && bubbleShopId > 0) {
                    try {
                        const detail: any = await getStoreDetail({ shopId: bubbleShopId });
                        if (detail && detail.shopId) {
                            return { shop: detail as StoreListRecords, allowEnterHome: true };
                        }
                    } catch (e) {
                        console.warn("获取指定门店详情失败:", e);
                    }
                    continue;
                }
                try {
                    const result = await getStoreList({
                        regionId: currentRegion.value.regionId,
                        page: 1,
                        size: 1,
                        ...currentLocationInfo.value,
                        sortField: "distance",
                        sortOrder: "asc"
                    });
                    if (result.records?.length) {
                        return { shop: result.records[0], allowEnterHome: true };
                    }
                } catch (e) {
                    console.warn("获取最近门店失败:", e);
                }
                continue;
            }
        }
        return null;
    };

    // 将解析到的门店设为当前门店，并直接进入其门店首页
    const enterShopHome = (shop: StoreListRecords) => {
        if (!shop || shop.shopId === undefined || shop.shopId === null) return;
        setCurrentShop(shop);
        uni.reLaunch({ url: `/pages/store/index?id=${shop.shopId}` });
    };

    // 按定位距离预取最近门店，供"最近门店"入口直达；不受进店规则影响、不自动进店
    const prefetchNearestShopByLocation = async () => {
        try {
            const result = await getStoreList({
                regionId: currentRegion.value.regionId,
                page: 1,
                size: 1,
                ...currentLocationInfo.value,
                sortField: "distance",
                sortOrder: "asc"
            });
            nearestShopByLocation.value = result.records.length > 0 ? result.records[0] : ({} as StoreListRecords);
        } catch (error) {
            console.error("预取最近门店失败:", error);
        }
    };

    // autoEnter：按规则(1)/指定门店(2)解析到门店后，直接跳转对应门店首页
    const getNearestShopId = async (autoEnter = false) => {
        try {
            const configStore = useConfigStore();
            const firstType = Number(configStore.storeEntryFirstType) || 1;
            const designatedShopId = Number(configStore.storeEntryDesignatedShopId) || 0;

            // 按指定门店进网：直接取后台配置的指定门店详情作为"当前门店"
            if (firstType === 2 && designatedShopId > 0) {
                const detail: any = await getStoreDetail({ shopId: designatedShopId });
                nearestShopInfo.value = (detail as StoreListRecords) || ({} as StoreListRecords);
                if (autoEnter) enterShopHome(nearestShopInfo.value);
                return;
            }

            // 按规则进店：按 priority 顺序命中
            if (firstType === 1) {
                const matched = await resolveShopByRules();
                if (matched) {
                    nearestShopInfo.value = matched.shop;
                    // 推广链接（规则 1）：只绑定门店，留在原链接页；其余规则才允许跳门店首页
                    if (matched.allowEnterHome) {
                        if (autoEnter) enterShopHome(matched.shop);
                    } else {
                        setCurrentShop(matched.shop);
                    }
                    return;
                }
            }

            // 兜底：按距离取最近门店
            const result = await getStoreList({
                regionId: currentRegion.value.regionId,
                page: 1,
                size: 1,
                ...currentLocationInfo.value,
                sortField: "distance",
                sortOrder: "asc"
            });
            nearestShopInfo.value = result.records.length > 0 ? result.records[0] : ({} as StoreListRecords);
            // 按规则(1)/指定门店(2)：兜底取到最近门店时同样直接进店
            if (autoEnter && (firstType === 1 || firstType === 2)) enterShopHome(nearestShopInfo.value);
        } catch (error) {
            console.error("获取附近门店失败:", error);
        }
    };

    return {
        currentRegion,
        currentSelectedName,
        currentSelectedAddressId,
        currentLocationInfo,
        loading,
        nearestShopInfo,
        nearestShopByLocation,
        currentShop,
        realTimeRegion,
        realTimeLocationInfo,
        setCurrentFlag,
        setCurrentRegion,
        setCurrentSelectedName,
        setCurrentSelectedAddressId,
        setCurrentLocation,
        setCurrentShop,
        setRealTimeRegion,
        setRealTimeLocation,
        getLocationByLocationData,
        getLocationByIpData,
        getNearestShopId,
        prefetchNearestShopByLocation
    };
});
