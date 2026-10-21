<template>
    <tig-layout>
        <view class="sticky">
            <view class="material-menu">
                <view class="search-box">
                    <view class="status-bar" />
                    <view class="search-input-box">
                        <view class="input">
                            <searchInput
                                v-model="filterParams.keyword"
                                :placeholder="$t('请输入关键字')"
                                style="width: 100%"
                                @confirm="handleSearch"
                                @clear="handleSearchClear"
                            />
                        </view>
                    </view>
                </view>
                <view class="material-content">
                    <up-tabs
                        line-color="#5d53b7"
                        :inactive-style="{ color: '#969799' }"
                        key-name="categoryName"
                        :scrollable="categoryList.length > 5"
                        :list="categoryList"
                        @click="handleChangeList"
                    />
                </view>
            </view>
        </view>
        <view class="material-content">
            <view class="material-list">
                <block v-for="item in list" :key="item.id">
                    <view class="material-item">
                        <view class="material-item-content">
                            <view class="material-item-left">
                                <view class="item-logo">
                                    <text class="item-logo-bg" />
                                </view>
                            </view>
                            <view class="material-item-right">
                                <view class="item-name">{{ $t("分销小助手") }}</view>
                                <view class="item-content">
                                    <rich-text v-if="item.content" :nodes="item.content" />
                                </view>
                                <view v-if="item.pics" class="item-images">
                                    <block v-for="(img, index) in item.pics" :key="img.picId">
                                        <view class="image-box" @click="swiperImagePreview(index, item.pics)">
                                            <tig-image :src="img.picUrl" mode="widthFix" @longpress.stop="handleSave(img.picUrl)" />
                                        </view>
                                    </block>
                                </view>
                                <view class="item-tip">{{ $t("长按图片保存到相册") }}</view>
                                <view v-if="item.product" class="item-product">
                                    <view class="item-product-text">{{ $t("商品：") }}</view>
                                    <view class="item-product-img" @click="handleLink(`/pages/product/index?id=${item.productId}`)">
                                        <tig-image :src="pickPic(item.product)" />
                                    </view>
                                </view>
                                <view class="item-btn">
                                    <view class="item-sharenum" />
                                    <materialPopover>
                                        <view class="item-btn-content">
                                            <text class="round" />
                                            <text class="round" />
                                        </view>
                                        <template #content>
                                            <view class="popover-btns">
                                                <view class="popover-btns-item" @click="handleBtn('friend', item)">
                                                    <text class="popover-btns-item-icon icon-1" />
                                                    {{ $t("发朋友圈") }}</view
                                                >
                                                <view class="common-line" />
                                                <view class="popover-btns-item" @click="handleBtn('poster', item)"
                                                    ><text class="popover-btns-item-icon icon-2" /> {{ $t("生成海报") }}</view
                                                >
                                            </view>
                                        </template>
                                    </materialPopover>
                                </view>
                            </view>
                        </view>
                    </view>
                </block>
            </view>
        </view>
        <empty v-if="!isLoading && total === 0" :styles="{ width: '320rpx', height: '320rpx' }" />
        <tig-qrcode ref="qrcodeRef" :immediately-create="true" :value="link" :size="400" :margin="10" :show="false" @success="qrcodeSuccess" />
        <materialSaveCard v-model="showSaveCard" :current-pics="currentPics" />
        <materialChoosePoster v-if="showChoosePoster" v-model="showChoosePoster" :qrcode-path="qrcodePath" :current-data="currentData" />
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 推广素材库页
 * - 展示分销推广素材（图片、文案）
 * - 分类筛选和搜索素材
 * - 一键保存素材到相册
 */
import { computed, nextTick, reactive, ref } from "vue";
import { onLoad, onUnload } from "@dcloudio/uni-app";
import { materialCategoryList, materialList, materialDetail } from "@/api/salesman/salesman";
import type { MaterialCategoryItem, MaterialListFilterResult } from "@/types/salesman/salesman";
import searchInput from "./src/searchInput.vue";
import materialPopover from "./src/materialPopover.vue";
import materialSaveCard from "./src/materialSaveCard.vue";
import materialChoosePoster from "./src/materialChoosePoster.vue";
import empty from "@/pages/salesman/src/empty.vue";
import { useConfigStore } from "@/store/config";
import { imageFormat, pickPic } from "@/utils/format";
import { getDownloadFileInfo, copy, staticResource, saveImageToAlbum } from "@/utils";
import { useSaveTopBoxHeight, useList } from "@/hooks";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const { height, contentHeight, contentWidth, statusBarHeight } = useSaveTopBoxHeight(60);

const configStore = useConfigStore();

const userInfo = uni.getStorageSync("userInfo");

interface IfilterParams {
    keyword: string;
    page: number;
    size: number;
    categoryId: number | null;
}
const filterParams = reactive<IfilterParams>({
    keyword: "",
    page: 1,
    size: 10,
    categoryId: null
});
const categoryList = ref<MaterialCategoryItem[]>([
    {
        categoryId: null,
        categoryName: t("全部")
    }
]);

const {
    data: list,
    isLoading,
    total,
    getList
} = useList<MaterialListFilterResult>(materialList, {
    params: filterParams,
    path: {
        dataKey: "records"
    }
});

const qrcodeRef = ref();
const qrcodePath = ref("");
const getMaterialCategoryList = async () => {
    try {
        const result = await materialCategoryList();
        categoryList.value = [...categoryList.value, ...result];
    } catch (err) {
        console.error(err);
    }
};
const getMaterialDetail = async (id: number) => {
    try {
        await materialDetail(id);
    } catch (error) {
        console.error(error);
    }
};

const handleChangeList = (item: AnyObject) => {
    filterParams.categoryId = item.categoryId;
    list.value = [];
    getList();
};
const handleSearch = () => {
    list.value = [];
    getList();
};
const handleSearchClear = () => {
    list.value = [];
    getList();
};

const handleLink = (url: string) => {
    uni.navigateTo({
        url: url
    });
};
const swiperImagePreview = (index: number, arr: any) => {
    const images = arr.map((item: any) => imageFormat(item.picUrl || ""));
    uni.previewImage({
        current: images[index],
        urls: images
    });
};
const handleSave = (src: string) => {
    // #ifndef  H5
    let srcFormat = imageFormat(src);
    uni.showModal({
        title: t("提示"),
        content: t("确定要保存图片吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                // #ifdef APP-PLUS||MP-WEIXIN
                const res = await getDownloadFileInfo(srcFormat);
                if (res.statusCode === 200) {
                    saveImageToAlbum(res.tempFilePath)
                        .then(() => {
                            uni.showToast({
                                title: t("保存成功"),
                                icon: "none"
                            });
                        })
                        .catch((err) => {
                            uni.showToast({
                                title: t("保存失败"),
                                icon: "none"
                            });
                            console.error(err);
                        });
                }
                // #endif
            }
        }
    });
    // #endif
};

const showSaveCard = ref(false);
const showChoosePoster = ref(false);
const currentPics = ref<any>([]);
const currentData = ref<AnyObject>({});
const handleBtn = (type: string, data: any) => {
    getMaterialDetail(data.id);
    switch (type) {
        case "poster":
            currentData.value = data;
            getLink(data);
            break;
        case "friend":
            copy(data.content, () => {
                showSaveCard.value = true;
                currentPics.value = data.pics;
            });
            break;
    }
};

const qrcodeSuccess = (tempFilePath: string) => {
    qrcodePath.value = tempFilePath;
    showChoosePoster.value = true;
};
const link = ref("");
const getLink = (data: AnyObject) => {
    link.value = data.productId
        ? `${configStore.baseInfo.h5Domain}/pages/product/index?id=${data.productId}&salesmanId=${userInfo?.salesman?.salesmanId}`
        : configStore.baseInfo.h5Domain;
    nextTick(() => {
        qrcodeRef.value?.drawQrcode();
    });
};

const icoImg = computed(() => {
    return `url(${imageFormat(configStore.baseInfo.icoImg)})`;
});

const materialFriendsBg = computed(() => {
    return `url(${staticResource("salesman/materialFriends.png")})`;
});

const materialPicBg = computed(() => {
    return `url(${staticResource("salesman/materialPic.png")})`;
});

onLoad(async () => {
    await getMaterialCategoryList();
    getList();
});

// 关闭预览图片
onUnload(() => {
    // #ifdef  H5 || APP
    uni.closePreviewImage();
    // #endif
});
</script>
<style>
page {
    background-color: #fff;
}
</style>
<style lang="scss" scoped>
.material-menu {
    background-color: #fff;
    z-index: 88;
    position: relative;
    box-shadow: 0 0 10px rgba(125, 126, 128, 0.16);
    .search-box {
        background-color: #fff;
        height: v-bind("height + 'px'");
        .status-bar {
            height: v-bind("statusBarHeight + 'px'");
        }
        .search-input-box {
            height: v-bind("contentHeight + 'px'");
            width: v-bind("contentWidth");
            display: flex;
            padding: 0 25rpx;
            .input {
                display: flex;
                align-items: center;
                width: 100%;
            }
        }
    }
}

.material-content {
    .material-list {
        padding: 0 20rpx;
        font-size: 26rpx;
        .material-item {
            padding-top: 30rpx;
            &::after {
                content: "";
                display: block;
                height: 1rpx;
                width: 100%;
                background-color: #f0f0f0;
                margin-top: 20rpx;
            }
            .material-item-content {
                display: flex;

                .material-item-left {
                    .item-logo {
                        width: 60rpx;
                        height: 60rpx;
                        overflow: hidden;
                        border-radius: 50%;
                        box-shadow: 0 0 10px rgba(125, 126, 128, 0.16);

                        .item-logo-bg {
                            width: 100%;
                            height: 100%;
                            background-size: 75%;
                            background-position: center;
                            background-image: v-bind(icoImg);
                            background-repeat: no-repeat;
                            display: inline-block;
                        }
                    }
                }
                .material-item-right {
                    display: flex;
                    flex-direction: column;
                    margin-left: 20rpx;
                    flex: 1;
                    .item-name {
                        font-size: 30rpx;
                        color: #566483;
                        font-weight: 500;
                    }
                    .item-content {
                        padding-top: 15rpx;
                        line-height: 35rpx;
                        white-space: break-spaces;
                    }

                    .item-images {
                        display: grid;
                        grid-template-columns: repeat(3, 200rpx);
                        gap: 15rpx;
                        padding-top: 20rpx;

                        .image-box {
                            overflow: hidden;
                            border-radius: 8rpx;
                        }
                    }

                    .item-tip {
                        color: #969799;
                        font-size: 22rpx;
                        padding-top: 6rpx;
                    }

                    .item-product {
                        color: #999;
                        padding-top: 25rpx;
                        display: flex;

                        .item-product-img {
                            width: 150rpx;
                            height: 150rpx;
                            overflow: hidden;
                            border-radius: 5rpx;
                            box-shadow: 0 0 10px rgba(125, 126, 128, 0.16);
                        }
                    }

                    .item-btn {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        padding-top: 30rpx;
                        padding-bottom: 15rpx;
                        color: #999;

                        .item-sharenum {
                            // font-size: 22rpx;
                        }

                        .item-btn-content {
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            width: 64rpx;
                            height: 40rpx;
                            background-color: #f2f3f5;
                            position: relative;

                            .round {
                                background-color: #586b93;
                                width: 8rpx;
                                height: 8rpx;
                                border-radius: 50%;

                                &:first-child {
                                    margin-right: 8rpx;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
.popover-btns {
    width: auto;
    display: flex;
    align-items: center;

    .popover-btns-item {
        padding: 0 20rpx;
        min-width: 148rpx;
        display: flex;
        align-items: center;
        .popover-btns-item-icon {
            width: 44rpx;
            height: 36rpx;
            padding-right: 8rpx;
            display: inline-block;
            background: 50% / contain no-repeat;

            &.icon-1 {
                background-image: v-bind(materialFriendsBg);
            }

            &.icon-2 {
                background-image: v-bind(materialPicBg);
            }
        }
    }

    .common-line {
        width: 2rpx;
        height: 32rpx;
        background-color: #000;
    }
}
</style>
