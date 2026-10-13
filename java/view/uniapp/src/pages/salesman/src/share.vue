<template>
    <view>
        <shareCard
            v-model="showShareCard"
            :link="link"
            :qrcode-path="qrcodePath"
            :current-data="currentData"
            @show-save-image-text-card="handleShowSaveImageTextCard"
            @show-share-qrcode="handleShowShareQrcode"
        />
        <saveImageTextCard v-model="showSaveImageTextCard" :current-data="currentData" />
        <shareQrcode v-model="showShareQrcode" :qrcode-path="qrcodePath" :link="link" />
        <tig-qrcode v-if="showShareCard" ref="qrcodeRef" :immediately-create="true" :value="link" :margin="10" :show="false" @success="qrcodeSuccess" />
    </view>
</template>

<script setup lang="ts">
/**
 * 分享弹窗组件
 * - 提供多种分享方式选择
 * - 生成分享图片、二维码
 * - 复制分享链接
 */
import { computed, ref } from "vue";
import shareCard from "./shareCard.vue";
import saveImageTextCard from "./saveImageTextCard.vue";
import shareQrcode from "./shareQrcode.vue";
import { useConfigStore } from "@/store/config";

const configStore = useConfigStore();

const userInfo = uni.getStorageSync("userInfo");

const props = defineProps({
    showShareCard: {
        type: Boolean,
        default: false
    },
    currentData: {
        type: Object,
        default: () => {}
    }
});
const emit = defineEmits(["update:showShareCard"]);
const qrcodeRef = ref();
const showShareCard = computed({
    get() {
        if (props.showShareCard) {
            qrcodeRef.value?.drawQrcode();
        }
        return props.showShareCard;
    },
    set(val) {
        emit("update:showShareCard", val);
    }
});

const link = computed(() => {
    let domain = configStore.baseInfo.h5Domain;

    // #ifdef H5
    domain = domain ? domain : `${window.location.origin}/mobile`;
    // #endif

    let url = `${domain}/pages/product/index?id=${props.currentData.productId}&salesmanId=${userInfo?.salesman?.salesmanId}`;
    const shopId = props.currentData.salesmanProduct?.shopId ?? props.currentData.shopId;
    // shopId 不存在或为 0 时统一补 shopId=0，代表自营
    url += `&shopId=${shopId || 0}`;
    return url;
});

const showSaveImageTextCard = ref(false);

const handleShowSaveImageTextCard = () => {
    emit("update:showShareCard", false);
    showSaveImageTextCard.value = true;
};

const showShareQrcode = ref(false);
const handleShowShareQrcode = () => {
    emit("update:showShareCard", false);
    showShareQrcode.value = true;
};

const qrcodePath = ref("");
const qrcodeSuccess = (val: string) => {
    qrcodePath.value = val;
};
</script>

<style lang="scss" scoped></style>
