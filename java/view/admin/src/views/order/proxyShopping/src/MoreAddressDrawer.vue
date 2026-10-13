<template>
    <div class="more-address-drawer" v-loading="loading">
        <perfect-scrollbar
            v-if="!loading && addressList.length > 0"
            class="list-ps"
            v-infinite-scroll="onLoadMore"
            :infinite-scroll-disabled="!hasMore || loadingMore || loading"
            :infinite-scroll-distance="80"
            @ps-y-reach-end="onLoadMore"
        >
            <div class="list-inner">
                <div
                    v-for="addr in addressList"
                    :key="addr.addressId || formatAddress(addr)"
                    class="address-card"
                    :class="{ active: isActiveAddress(addr) }"
                    @click="onSelect(addr)"
                >
                    <div class="addr-col-icon">
                        <el-icon class="addr-ico"><Location /></el-icon>
                    </div>
                    <div class="addr-col-content">
                        <div class="addr-line1">
                            <span class="addr-region">{{ getRegionText(addr) }}</span>
                        </div>
                        <div class="addr-line2">{{ getFullAddressText(addr) }}</div>
                        <div class="addr-line3">
                            <span class="addr-consignee">{{ addr?.consignee || "-" }}</span>
                            <span class="addr-mobile">{{ maskMobile(addr?.mobile || addr?.telephone || "") }}</span>
                        </div>
                    </div>
                </div>

                <div class="load-more-tip" v-if="loadingMore">{{ $t('加载中...') }}</div>
                <div class="load-more-tip" v-else-if="!hasMore && addressList.length > 0">{{ $t('没有更多了') }}</div>
            </div>
        </perfect-scrollbar>

        <div v-else-if="!loading" class="empty">
            <div class="empty-bg"></div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { Location } from "@element-plus/icons-vue";
import { getProxyShoppingAddressList } from "@/api/proxyShopping/address";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    userId: number;
    selectedAddressId?: number;
    /** DialogForm 会透传 visible，用于抽屉反复打开时重置状态 */
    visible?: boolean;
}>();

const emit = defineEmits<{
    submitCallback: [Record<string, any>, boolean?];
    okType: [boolean];
}>();

// DialogForm 会向内容组件透传 confirmLoading 的 v-model，这里接住避免告警
defineModel("confirmLoading", {
    type: Boolean,
    default: false
});

const PAGE_SIZE = 15;

const loading = ref(false);
const loadingMore = ref(false);
const addressList = ref<any[]>([]);
const total = ref(0);
const page = ref(1);
const lastPageCount = ref(0);
const selectedId = ref<number>(0);

const hasMore = computed(() => {
    const t = Number(total.value || 0);
    if (t > 0) return addressList.value.length < t;
    // 无 total 时：用“本页返回数量”做兜底判断
    return lastPageCount.value === PAGE_SIZE;
});

const isActiveAddress = (addr: any) => Number(addr?.addressId || 0) > 0 && Number(addr?.addressId || 0) === Number(selectedId.value || 0);

const formatAddress = (addr: any) => {
    if (!addr) return "-";
    const region = addr.regionName || (Array.isArray(addr.regionNames) ? addr.regionNames.join("") : "");
    const detail = addr.address || "";
    const consignee = addr.consignee || "";
    const mobile = addr.mobile || addr.telephone || "";
    const head = [region, detail].filter(Boolean).join(" ");
    const tail = [consignee, mobile].filter(Boolean).join(" ");
    return [head, tail].filter(Boolean).join("，");
};

const getRegionText = (addr: any) => {
    if (!addr) return "-";
    return addr.regionName || (Array.isArray(addr.regionNames) ? addr.regionNames.join("") : "") || "-";
};

const getFullAddressText = (addr: any) => {
    return addr?.address || "";
};

const maskMobile = (mobile: string) => {
    const s = String(mobile || "").trim();
    if (!s) return "-";
    if (s.length < 7) return s;
    return `${s.slice(0, 3)}****${s.slice(-4)}`;
};

const parseListFromRes = (addrRes: any) => {
    return (
        (Array.isArray(addrRes) ? addrRes : null) ||
        (Array.isArray(addrRes?.records) ? addrRes.records : null) ||
        (Array.isArray(addrRes?.list) ? addrRes.list : null) ||
        []
    );
};

const fetchAddressPage = async (pageNo: number) => {
    const uid = Number(props.userId || 0);
    if (!uid) {
        addressList.value = [];
        total.value = 0;
        lastPageCount.value = 0;
        return;
    }

    try {
        const addrRes: any = await getProxyShoppingAddressList({ userId: uid, page: pageNo, size: PAGE_SIZE } as any);
        const list = parseListFromRes(addrRes);
        lastPageCount.value = Array.isArray(list) ? list.length : 0;
        const t = Number(addrRes?.total ?? addrRes?.data?.total ?? 0);
        if (t > 0) total.value = t;

        // 追加并去重（以 addressId 为主）
        const next = [...(addressList.value || []), ...(list || [])];
        const seen = new Map<string, any>();
        for (const item of next) {
            const id = Number(item?.addressId || 0);
            const k = id > 0 ? `id:${id}` : `fmt:${formatAddress(item)}`;
            if (!seen.has(k)) seen.set(k, item);
        }
        addressList.value = Array.from(seen.values());

        // 如果接口没给 total，就用当前已加载长度兜底（避免一直“有更多”）
        if (!(Number(total.value || 0) > 0) && lastPageCount.value < PAGE_SIZE) {
            total.value = addressList.value.length;
        }
    } catch (e: any) {
        addressList.value = [];
        total.value = 0;
        lastPageCount.value = 0;
        message.error(e?.message || t('获取地址列表失败'));
    }
};

const resetStateWhenOpen = async () => {
    selectedId.value = Number(props.selectedAddressId || 0);
    page.value = 1;
    total.value = 0;
    lastPageCount.value = 0;
    addressList.value = [];
    loading.value = true;
    try {
        await fetchAddressPage(1);
    } finally {
        loading.value = false;
    }
    await nextTick();
    emit("okType", Number(selectedId.value || 0) > 0);
};

watch(
    () => props.visible,
    (val) => {
        if (val) resetStateWhenOpen();
    },
    { immediate: true }
);

watch(
    () => props.selectedAddressId,
    (val) => {
        // 抽屉复用时同步外部选中态（不主动加载）
        selectedId.value = Number(val || 0);
        emit("okType", Number(selectedId.value || 0) > 0);
    }
);

const onSelect = (addr: any) => {
    const id = Number(addr?.addressId || 0);
    if (!id) return;
    selectedId.value = id;
    emit("okType", true);
};

const onLoadMore = async () => {
    if (loading.value || loadingMore.value) return;
    if (!hasMore.value) return;
    loadingMore.value = true;
    try {
        page.value += 1;
        await fetchAddressPage(page.value);
    } finally {
        loadingMore.value = false;
    }
};

const onFormSubmit = async () => {
    const id = Number(selectedId.value || 0);
    if (!id) {
        emit("okType", false);
        message.warning(t('请选择收货地址'));
        return;
    }
    const selected = (addressList.value || []).find((x: any) => Number(x?.addressId || 0) === id) || { addressId: id };
    emit("submitCallback", { ...(selected as any) }, true);
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.more-address-drawer {
    height: 100%;
    display: flex;
    flex-direction: column;
}
.header {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    margin-bottom: 10px;
    .title {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
    }
    .sub {
        font-size: 12px;
        color: #909399;
    }
}
.list-ps {
    flex: 1;
    height: 100%;
    min-height: 0;
    overflow-x: hidden;
}
.list-inner {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
    padding-right: 6px;
    box-sizing: border-box;
    width: 100%;
}
.more-address-drawer :deep(.ps__rail-x) {
    display: none !important;
}
.more-address-drawer :deep(.ps__thumb-x) {
    display: none !important;
}
.address-card {
    position: relative;
    border: 1px solid #dfe1e8;
    border-radius: 3px;
    padding: 18px 12px 12px;
    cursor: pointer;
    background: #fff;
    display: flex;
    gap: 10px;
    align-items: flex-start;
    transition:
        border-color 0.18s ease,
        box-shadow 0.18s ease,
        transform 0.18s ease;
}
.address-card.active {
    border: 1px solid var(--el-color-primary) !important;
    background: #fff;
}
.address-card.active::after {
    content: "";
    position: absolute;
    bottom: 0;
    right: 0;
    width: 0;
    height: 0;
    border-left: 30px solid transparent;
    border-bottom: 30px solid var(--el-color-primary);
}
.address-card.active::before {
    content: "\2713";
    position: absolute;
    right: 5px;
    bottom: 3px;
    color: #fff;
    z-index: 2;
    font-size: 12px;
    font-weight: 700;
    line-height: 1;
}
.address-card:hover {
    border-color: var(--el-color-primary);
}
.addr-col-icon {
    flex: 0 0 18px;
    padding-top: 2px;
    display: flex;
    align-items: flex-start;
    justify-content: center;
}
.addr-col-content {
    flex: 1;
    min-width: 0;
}
.addr-ico {
    color: #101010;
    font-size: 17px;
    flex: 0 0 auto;
}
.addr-line1 {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #6a6a6a;
    margin-bottom: 10px;
    .addr-region {
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
}
.addr-line2 {
    color: #101010;
    margin-bottom: 10px;
    line-height: 18px;
    min-height: 18px;
    word-break: break-all;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.addr-line3 {
    display: flex;
    gap: 10px;
    color: #909399;
    font-size: 12px;
    .addr-consignee {
        color: #606266;
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    .addr-mobile {
        flex: 0 0 auto;
        color: #606266;
    }
}
.load-more-tip {
    grid-column: 1 / -1;
    text-align: center;
    color: #909399;
    font-size: 12px;
    padding: 8px 0;
}
.empty {
    height: 200px;
}
</style>

