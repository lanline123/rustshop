import { ref, computed, type Ref } from "vue";
import { getProductDetail as fetchProductDetail } from "@/api/product/product";
import { getExchangeDetail } from "@/api/exchange/exchange";
import { getActiveStyle } from "@/api/product/productStyle";
import { getShopDetail } from "@/api/shop/shop";
import { getStoreDetail } from "@/api/store/store";
import { useRegionStore } from "@/store/region";
import { useConfigStore } from "@/store/config";
import { productImage } from "@/utils/format";
import { isO2o } from "@/utils";
import { resolveWholesaleShopId } from "@/utils/wholesale";
import { setPageMeta, setJsonLd, extractDescription, clearJsonLd } from "@/utils/seo";
import type { ProductActiveStyleVO } from "@/types/product/productStyle";
import type { PicList, ProductItem, AttrList, SkuList, ServiceList, RankDetail, DescArr, SkuPromotion, VideoList } from "@/types/product/product";
import type { ShopDetailItem } from "@/types/shop/shop";
import type { StoreDetailData } from "@/types/store/store";

/**
 * 商品详情数据获取和管理 Composable
 *
 * @description 拉取商品 / 兑换详情并落地各字段，处理门店参数、店铺与门店详情、
 * 活动样式、SEO（H5）、周期购 / 次卡回填与加载时序（detailLoaded + specLoaded 双闸）
 * 逻辑与 pages/product/index.vue 的 __getProductDetail 保持一致
 */
export interface UseProductDetailOptions {
    /** 商品类型：默认普通商品，exchange 为积分兑换商品 */
    type?: Ref<string>;
    /** 详情加载完成后的回调（页面用于 handleLog 等；回传原始响应，供页面按需消费如批发字段） */
    onDetailLoaded?: (result?: any) => void;
}

export function useProductDetail(options: UseProductDetailOptions = {}) {
    const { type = ref(""), onDetailLoaded } = options;

    const regionStore = useRegionStore();
    const configStore = useConfigStore();

    // 基础数据
    const productId = ref<string>("");
    const specificationId = ref<number>(0);
    const skuId = ref<number>(0);
    const product = ref<ProductItem>({
        productId: 0,
        productStock: 0
    });

    // 详情数据
    const picList = ref<PicList[]>([]);
    const videoList = ref<VideoList[]>([]);
    const attrList = ref<AttrList>({} as AttrList);
    const skuList = ref<SkuList[]>([]);
    const descArr = ref<DescArr[]>([]);
    const serviceList = ref<ServiceList[]>([]);
    const rankDetail = ref<RankDetail>({});
    const checkedValue = ref<string[]>([]);

    // 价格 / 库存 / 促销
    const productPrice = ref<string>("0");
    const productStock = ref<number>(0);
    const exchangeIntegral = ref(0);
    const promotionList = ref<SkuPromotion[]>([]);

    // 店铺 / 门店
    const shopId = ref<number>(-1);
    const storeInfo = ref<StoreDetailData>({} as StoreDetailData);
    const shopInfo = ref<ShopDetailItem>({} as ShopDetailItem);

    // 周期购字段
    const cycleInfo = ref<any>({});
    const isCycleBuy = ref(false);
    const isSubCard = ref(false);

    // 活动样式
    const activityStyleData = ref<ProductActiveStyleVO | null>(null);
    const activityBgImage = computed(() => activityStyleData.value?.activityBackgroundImage || "");

    // 加载状态
    const loadEndStatus = ref(true);
    const detailLoaded = ref(false);
    const specLoaded = ref(false);
    const failLoad = ref(false);

    /**
     * 获取活动样式
     */
    const fetchActivityStyle = async (pid: number): Promise<void> => {
        try {
            const result = await getActiveStyle(pid);
            activityStyleData.value = result;
        } catch {
            activityStyleData.value = null;
        }
    };

    /**
     * 获取门店详情（O2O）
     */
    const getStoreDetailData = async (id: number): Promise<void> => {
        try {
            const result = await getStoreDetail({ shopId: id });
            storeInfo.value = result;
        } catch (error) {
            console.error("Error fetching store details:", error);
        }
    };

    /**
     * 获取店铺信息（客服入口 / 店铺相关）
     */
    const getShopInfo = async (id: number): Promise<void> => {
        try {
            const result = await getShopDetail(id);
            shopInfo.value = result;
        } catch (error: any) {
            console.error(error);
        }
    };

    /**
     * 获取商品详情
     * O2O 场景优先使用指定门店 ID，否则使用最近门店 ID；支持普通商品与积分兑换两种模式
     * @param id 商品 ID 或包含 sn 的对象
     */
    const getProductDetail = async (id: string | number | object): Promise<void> => {
        try {
            loadEndStatus.value = true;
            detailLoaded.value = false;
            let result: any = {};

            let params: any = {
                shopId: null,
                nearestShopId: null
            };

            if (typeof id === "object") {
                params = { ...params, ...id };
            } else {
                params.id = id;
            }
            if (shopId.value > -1) {
                params.shopId = shopId.value;
            } else {
            }

            switch (type.value) {
                case "exchange":
                    if (typeof id === "object") return;
                    result = await getExchangeDetail(params);
                    exchangeIntegral.value = result.exchangeInfo.exchangeIntegral;
                    productStock.value = Number(result.exchangeInfo?.productStock ?? 0);
                    productPrice.value = result.exchangeInfo.discountsPrice || "0";
                    specificationId.value = result.exchangeInfo.id;
                    break;
                default:
                    result = await fetchProductDetail(params);
                    productStock.value = Number(result.item?.productStock ?? 0);
                    productPrice.value = result.item.productPrice || "0";
                    specificationId.value = result.item.productId;
            }
            productId.value = String(result.item.productId ?? "");
            product.value = result.item;
            attrList.value = result.attrList;
            picList.value = result.picList;
            videoList.value = result.videoList;
            rankDetail.value = result.rankDetail;
            skuList.value = result.skuList;
            checkedValue.value = result.checkedValue;
            descArr.value = result.descArr;
            serviceList.value = result.serviceList;
            uni.setNavigationBarTitle({
                title: result.item.productName
            });
            // #ifdef H5
            const shopTitle = configStore.baseInfo.shopTitle;
            const productTitle = `${result.item.productName} - ${shopTitle}`;
            const productDesc = extractDescription(result.item.productBrief || result.item.productDesc || result.item.productName);
            const productImageUrl = productImage(result.item);

            setPageMeta({
                title: productTitle,
                description: productDesc,
                keywords: result.item.productName,
                image: productImageUrl,
                type: "product"
            });

            setJsonLd({
                "@context": "https://schema.org",
                "@type": "Product",
                name: result.item.productName,
                description: productDesc,
                image: productImageUrl,
                url: window.location.href,
                offers: {
                    "@type": "Offer",
                    price: result.item.productPrice,
                    priceCurrency: "CNY",
                    availability: result.item.productStock > 0 ? "https://schema.org/InStock" : "https://schema.org/OutOfStock"
                }
            });
            // #endif

            // 路由门店只用于请求详情；后续业务统一使用详情接口最终解析出的门店。
            const detailShopId = result.item?.shopId ?? result.shopId;
            let finalShopId = typeof detailShopId === "number" && Number.isFinite(detailShopId) ? detailShopId : -1;
            // 未指定门店时不要把页面哨兵值 -1 传入批发门店解析，避免误判商品失效。
            finalShopId = resolveWholesaleShopId(detailShopId, shopId.value === -1 ? undefined : shopId.value);
            shopId.value = finalShopId;

            if (finalShopId > -1) {
                getShopInfo(finalShopId);
            }

            fetchActivityStyle(result.item.productId);

            // 数据已落地，允许展示“已售罄”遮罩（最终是否售罄由库存判断决定）
            detailLoaded.value = true;

            onDetailLoaded?.(result);

            // 只有“规格组件加载完成 + 详情接口落地完成”同时满足，才结束 loading
            if (specLoaded.value) loadEndStatus.value = false;
        } catch (error: any) {
            console.error(error);
            failLoad.value = true;
            loadEndStatus.value = false;
        } finally {
            if (failLoad.value) {
                loadEndStatus.value = false;
            } else if (detailLoaded.value && specLoaded.value) {
                loadEndStatus.value = false;
            } else {
                loadEndStatus.value = true;
            }
        }
    };

    /**
     * 规格组件加载完成回调，隐藏加载页
     */
    const getLoadStatus = (): void => {
        specLoaded.value = true;
        if (detailLoaded.value) {
            loadEndStatus.value = false;
        }
    };

    return {
        // 基础数据
        productId,
        specificationId,
        skuId,
        product,
        // 详情数据
        picList,
        videoList,
        attrList,
        skuList,
        descArr,
        serviceList,
        rankDetail,
        checkedValue,
        // 价格 / 库存 / 促销
        productPrice,
        productStock,
        exchangeIntegral,
        promotionList,
        // 店铺 / 门店
        shopId,
        storeInfo,
        shopInfo,
        // 周期购
        cycleInfo,
        isCycleBuy,
        isSubCard,
        // 活动样式
        activityStyleData,
        activityBgImage,
        // 状态
        loadEndStatus,
        detailLoaded,
        specLoaded,
        failLoad,
        // 方法
        getProductDetail,
        getStoreDetailData,
        getShopInfo,
        fetchActivityStyle,
        getLoadStatus,
        // 兑换清理（H5 SEO）
        clearJsonLd
    };
}

export default useProductDetail;
