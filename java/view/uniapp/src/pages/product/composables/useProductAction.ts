import { ref, nextTick, type Ref } from "vue";
import { getGrouponRecordList } from "@/api/groupon/groupon";
import type { GrouponRecordListParams, GrouponRecordItem } from "@/types/groupon/groupon";

/** 规格弹窗组件实例（仅用到的方法） */
export interface SpecificationPopupInstance {
    handleShowPopup(): void;
}

/**
 * 商品操作逻辑 Composable
 *
 * @description 管理商品详情页的交互操作（分享、规格弹窗、拼团弹窗与参团、拼团记录拉取）
 * 逻辑与 pages/product/index.vue 保持一致
 */
export interface UseProductActionOptions {
    /** 规格弹窗组件 ref */
    specificationRef?: Ref<SpecificationPopupInstance | null | undefined>;
}

export interface UseProductActionReturn {
    /** 显示分享弹窗 */
    showShare: Ref<boolean>;
    /** 显示更多拼团弹窗 */
    showMoreGroupon: Ref<boolean>;
    /** 拼团开团模式（true 开团 / false 单独购买） */
    grouponEnable: Ref<boolean>;
    /** 拼团记录信息 */
    grouponRecordInfo: Ref<{ grouponPromotionRecordId: number; minGroupNum: number }>;
    /** 拼团记录列表 */
    groupList: Ref<GrouponRecordItem[]>;
    /** 首屏展示的拼团记录（最多 2 条） */
    twoGroupList: Ref<GrouponRecordItem[]>;
    /** 拼团记录总数 */
    totalGroupCount: Ref<number>;
    /** 处理分享 */
    handleShare: () => void;
    /** 单独购买 / 加购：打开规格弹窗（不开团） */
    showSpecification: () => void;
    /** 开团：打开规格弹窗 */
    showGrouponPopup: () => void;
    /** 显示更多拼团列表 */
    handleShowMore: () => void;
    /** 加入现有拼团 */
    handleJoinGroup: (item: GrouponRecordItem) => void;
    /** 拉取当前商品的拼团记录 */
    getGrouponRecords: (productId: number) => Promise<void>;
    /** 设置规格弹窗 ref */
    setSpecificationRef: (instance: SpecificationPopupInstance | null) => void;
}

export function useProductAction(options: UseProductActionOptions = {}): UseProductActionReturn {
    const { specificationRef: externalSpecificationRef } = options;

    const _specificationRef = ref<SpecificationPopupInstance | null>(null);
    const specificationRef = externalSpecificationRef || _specificationRef;

    const showShare = ref(false);
    const showMoreGroupon = ref(false);
    const grouponEnable = ref(true);
    const grouponRecordInfo = ref<{ grouponPromotionRecordId: number; minGroupNum: number }>({
        grouponPromotionRecordId: 0,
        minGroupNum: 0
    });

    const groupList = ref<GrouponRecordItem[]>([]);
    const twoGroupList = ref<GrouponRecordItem[]>([]);
    const totalGroupCount = ref(0);

    const handleShare = (): void => {
        showShare.value = true;
    };

    const showSpecification = (): void => {
        if (specificationRef.value) {
            grouponEnable.value = false;
            specificationRef.value.handleShowPopup();
        }
    };

    const showGrouponPopup = (): void => {
        if (specificationRef.value) {
            grouponEnable.value = true;
            grouponRecordInfo.value.minGroupNum = 0;
            grouponRecordInfo.value.grouponPromotionRecordId = 0;
            specificationRef.value.handleShowPopup();
        }
    };

    const handleShowMore = (): void => {
        showMoreGroupon.value = true;
    };

    const handleJoinGroup = (item: GrouponRecordItem): void => {
        showMoreGroupon.value = false;
        nextTick(() => {
            grouponRecordInfo.value.grouponPromotionRecordId = item.grouponPromotionRecordId;
            grouponRecordInfo.value.minGroupNum = item.minGroupNum;
            grouponEnable.value = true;
            specificationRef.value?.handleShowPopup();
        });
    };

    const getGrouponRecords = async (productId: number): Promise<void> => {
        const params: GrouponRecordListParams & { excludeEnded: boolean } = {
            page: 1,
            size: 10,
            joinStatus: 1,
            dateType: 0,
            productId,
            excludeEnded: true
        };
        try {
            const res = await getGrouponRecordList(params);
            if (res.records) {
                groupList.value = res.records;
                totalGroupCount.value = res.total;
                if (groupList.value && groupList.value.length) {
                    twoGroupList.value = groupList.value.length > 1 ? groupList.value.slice(0, 2) : groupList.value;
                }
            }
        } catch (error) {
            console.log(error);
        }
    };

    const setSpecificationRef = (instance: SpecificationPopupInstance | null): void => {
        _specificationRef.value = instance;
    };

    return {
        showShare,
        showMoreGroupon,
        grouponEnable,
        grouponRecordInfo,
        groupList,
        twoGroupList,
        totalGroupCount,
        handleShare,
        showSpecification,
        showGrouponPopup,
        handleShowMore,
        handleJoinGroup,
        getGrouponRecords,
        setSpecificationRef
    };
}

export default useProductAction;
