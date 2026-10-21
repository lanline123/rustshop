<template>
    <div :class="'module-ad-con module-store_info ad-store_style__' + module.style">
        <div class="module-ad-content" v-if="module">
            <div class="cap-store-banner">
                <div class="cap-store-banner__cover" :style="'background-image: url(' + backgroundImg + ');'">
                    <div class="cap-store-banner__cover-mask"></div>
                </div>

                <div class="cap-store-nav-warp">
                    <div class="cap-store-nav-con">
                        <div class="store-search-con">
                            <div id="search">
                                <i class="iconfont-h5 icon-sousuo"></i>
                                <input type="span" class="search-input" value="" :placeholder="$t('搜商品')" />
                            </div>
                        </div>
                        <div class="store-nav-con">
                            <div class="store-nav-item active">
                                <a class="store-nav-item-a">{{ $t('精选') }}</a>
                            </div>
                            <div class="store-nav-item">
                                <a class="store-nav-item-a">{{ $t('商品') }}</a>
                            </div>
                            <div class="store-nav-item">
                                <a class="store-nav-item-a">{{ $t('分类') }}</a>
                            </div>
                        </div>
                        <div class="store-all-cat-con" style="display: none">
                            <span class="module_ico module-ico-fenlei"></span>
                        </div>
                    </div>
                </div>

                <div class="cap-store-banner__inner">
                    <div class="cap-store-banner__content">
                        <div class="cap-store-banner__logo">
                            <img
                                src="https://oss.tigshop.com/img/gallery/202406/17181844953OiwzdtLSXKLeU9oi7.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
                            />
                        </div>
                        <div class="cap-store-banner__right-content">
                            <h3 class="cap-store-banner__right-content-title--middle">{{ $t('Apple智慧生活专卖店') }}</h3>
                            <div class="cap-store-banner__sum-content">
                                <a>
                                    <div class="cap-store-banner__sum-content-total">{{ $t('全部商品 20') }}</div>
                                </a>
                                <a>
                                    <div class="cap-store-banner__sum-content-total">{{ $t('新品 12') }}</div>
                                </a>
                            </div>
                        </div>
                        <div class="store-collect-button-con">
                            <div class="store-collect-button">
                                <span class="module_ico module-ico-31shoucangxuanzhong"></span> {{ $t('收藏') }} </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="module-ad-empty empty-image_ad" v-else>
            <div class="image-empty-bg"></div>
            <div class="desc">{{ $t('店铺信息') }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref, Ref } from "vue";
import { imageFormat } from "@/utils/format";
import { ShopInfo, ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/";
import img1 from "@/views/decorate/decorate/src/img/bg/1.jpg";
import img2 from "@/views/decorate/decorate/src/img/bg/2.jpg";
import img3 from "@/views/decorate/decorate/src/img/bg/3.jpg";
import img4 from "@/views/decorate/decorate/src/img/bg/4.jpg";
import img5 from "@/views/decorate/decorate/src/img/bg/5.jpg";
import img6 from "@/views/decorate/decorate/src/img/bg/6.jpg";
import img7 from "@/views/decorate/decorate/src/img/bg/7.jpg";
import img8 from "@/views/decorate/decorate/src/img/bg/8.jpg";
import img9 from "@/views/decorate/decorate/src/img/bg/9.jpg";
import img10 from "@/views/decorate/decorate/src/img/bg/10.jpg";
const imgList = ref([img1, img2, img3, img4, img5, img6, img7, img8, img9, img10]);
// 在modules加入要使用的模块
const module = defineModel<ModuleType & ShopInfo>("module") as Ref<ModuleType & ShopInfo>;
const defaultModule = ref({
    customPic: {
        picUrl: "",
        picThumb: ""
    },
    backgroundDefault: 1,
    style: 1,
    frame: defaultFrame
});
mergeDefaultModule(module.value, defaultModule.value);
const backgroundImg = computed(() => {
    let url;
    if (module.value.backgroundDefault == 0 && module.value.customPic.picUrl) {
        url = imageFormat(module.value.customPic.picUrl);
    } else {
        url = imgList.value[module.value.backgroundDefault - 1];
    }
    return url;
});
</script>
