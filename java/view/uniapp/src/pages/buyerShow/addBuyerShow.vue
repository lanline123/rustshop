<template>
    <template v-if="buyerShowEnabled === 1">
        <tig-layout :title="$t('新增买家秀')">
            <view class="buyerShowForm">
                <view class="formBox">
                    <up-form ref="formRef" label-position="top" :model="form" :rules="rules" label-width="auto">
                        <up-form-item class="formItem" :label="$t('上传图片/视频')" prop="fileList">
                            <UploadMedia v-model:media-list="form.fileList" :max-video-count="1" />
                        </up-form-item>
                        <up-form-item class="formItem" :label="$t('内容')" prop="content">
                            <up-textarea
                                v-model="form.content"
                                :placeholder="$t('请输入内容（必填）')"
                                maxlength="500"
                                count
                                :placeholder-style="{ backgroundColor: '#FAFAFA', padding: '0', color: '#9A9A9A', fontSize: '24rpx' }"
                            />
                        </up-form-item>
                        <up-form-item :label="$t('选择已购买的商品')" prop="rangeData">
                            <ProductList
                                v-model:products="products"
                                v-model:total="total"
                                :product-list-height="productListHeight"
                                @scrolltolower="reachBottom"
                            />
                        </up-form-item>
                    </up-form>
                </view>
            </view>
            <tig-fixed-placeholder height="120rpx" background-color="#fff">
                <view class="footer">
                    <tig-button type="error" @click="handleClick">{{ $t("发布") }}</tig-button>
                </view>
            </tig-fixed-placeholder>
            <tig-loadingpage v-model="isLoading" />
            <loading-box v-model="isLoadMore" :page="filterParams.page" :length="products.length" />
        </tig-layout>
    </template>
    <template v-else>
        <tig-layout :title="$t('新增买家秀')" :show-left="false" style="height: 100vh">
            <buyerShowCloseTip v-model:buyer-show-enabled="buyerShowEnabled" />
        </tig-layout>
    </template>
</template>

<script setup lang="ts">
/**
 * 发布买家秀页
 * - 上传图片/视频内容
 * - 编辑文字描述
 * - 关联已购商品
 */
import TigLayout from "@/components/tig/tig-layout/tig-layout.vue";
import TigFixedPlaceholder from "@/components/tig/tig-fixed-placeholder/tig-fixed-placeholder.vue";
import TigButton from "@/components/tig/tig-button/tig-button.vue";
import ProductList from "./src/ProductList.vue";
import UploadMedia from "@/components/upload/UploadMedia.vue";
import buyerShowCloseTip from "./src/buyerShowCloseTip.vue";
import { nextTick, ref, shallowRef, getCurrentInstance, watch } from "vue";
import { buyerShowAdd, getHistoryProduct } from "@/api/buyerShow/buyerShow";
import { useList } from "@/hooks";
import { redirect } from "@/utils";
import { getElementRect } from "@/utils";
import { pxToRpx } from "@/utils/format";
import { onLoad } from "@dcloudio/uni-app";
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n";
const configStore = useConfigStore();
const { t } = useI18n();

const buyerShowEnabled = ref<number | undefined>(1);
watch(
    () => configStore,
    (val) => {
        if (val.initLoading) {
            buyerShowEnabled.value = val.buyerShowEnabled;
        }
    },
    { deep: true, immediate: true }
);

const filterParams = {
    page: 1,
    size: 10
};
const {
    data: products,
    isLoading,
    isLoadMore,
    total,
    reachBottom
} = useList(getHistoryProduct, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    immediate: true,
    needReachBottom: false
});

const formRef = shallowRef();
// 定义媒体项接口
interface MediaItem {
    type: "image" | "video";
    videoUrl?: string;
    videoCover?: string;
    picUrl?: string;
    picId?: number;
    uploading?: boolean;
}

interface FormData {
    content: string;
    fileList: MediaItem[];
    rangeData: any[];
    buyerShowVideoList?: any[];
    imgList?: any[];
}

const rules: any[] = [];

const form = ref<FormData>({
    content: "",
    fileList: [],
    rangeData: []
});

const instance = getCurrentInstance();

const productListHeight = ref("");

// 获取元素尺寸
const getElementRectFormItem = () => {
    nextTick(async () => {
        const rects: any = await getElementRect(".formItem", instance, true);
        const safeRects = Array.isArray(rects) ? rects : rects ? [rects] : [];
        const height = safeRects.reduce((acc: any, cur: any) => {
            return acc + cur.height;
        }, 0);
        // 获取屏幕尺寸
        const screenHeight = uni.getSystemInfoSync().screenHeight;
        productListHeight.value = pxToRpx(screenHeight) - pxToRpx(height) - configStore.navHeight - 260 + "rpx";
        console.log(productListHeight.value);
    });
};

onLoad(() => {
    getElementRectFormItem();
});

// 点击发布
const handleClick = () => {
    form.value.rangeData = products.value.filter((item: any) => item.isChecked).map((item: any) => item.productId);
    if (form.value.fileList.some((item) => item.uploading)) {
        uni.showToast({ title: "文件上传中，请稍候", icon: "none" });
        return;
    } else if (!form.value.fileList.length) {
        uni.showToast({
            title: t("请上传图片/视频"),
            icon: "none"
        });
        return;
    } else if (!form.value.rangeData.length) {
        uni.showToast({
            title: t("请选择已购买的商品"),
            icon: "none"
        });
        return;
    } else if (!form.value.content) {
        uni.showToast({
            title: t("请输入内容"),
            icon: "none"
        });
        return;
    }
    // 视频固定排在素材序列首位，覆盖上传失败重试、中途删除等导致的顺序变动
    const orderedFileList = [
        ...(form.value.fileList?.filter((item) => item.type === "video") ?? []),
        ...(form.value.fileList?.filter((item) => item.type !== "video") ?? [])
    ];
    form.value.imgList = orderedFileList.filter((item) => item.type === "image");
    form.value.buyerShowVideoList = orderedFileList.filter((item) => item.type === "video");
    const query: any = {
        ...form.value
    };
    delete query.fileList;
    buyerShowAdd(query).then(() => {
        uni.showToast({
            title: t("发布成功"),
            icon: "none"
        });
        redirect({ url: "/pages/buyerShow/index" });
    });
};
</script>

<style scoped lang="scss">
.buyerShowForm {
    padding: 20rpx;
    .formBox {
        background: #fff;
        padding: 20rpx;
    }
}
.footer {
    padding: 20rpx;
}

:deep(.u-form-item__body__left) {
    font-weight: bold;
    font-size: 32rpx;
    color: #262626;
}

:deep(.u-textarea) {
    //padding: 0;
    padding: 10rpx;
    background: #fafafa;
}

:deep(.u-form-item__body__right) {
    margin-top: 15rpx;
}
:deep(.u-form-item__body) {
    padding-top: 0;
}

:deep(.u-form-item) {
    &:last-child {
        .u-form-item__body {
            padding-bottom: 0;
        }
    }
}
</style>
