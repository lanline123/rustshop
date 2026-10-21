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
                                <input type="span" class="search-input" value="" placeholder="搜商品" />
                            </div>
                        </div>
                        <div class="store-nav-con">
                            <div class="store-nav-item active">
                                <a class="store-nav-item-a">精选</a>
                            </div>
                            <div class="store-nav-item">
                                <a class="store-nav-item-a">商品</a>
                            </div>
                            <div class="store-nav-item">
                                <a class="store-nav-item-a">分类</a>
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
                            <h3 class="cap-store-banner__right-content-title--middle">Apple智慧生活专卖店</h3>
                            <div class="cap-store-banner__sum-content">
                                <a>
                                    <div class="cap-store-banner__sum-content-total">全部商品 20</div>
                                </a>
                                <a>
                                    <div class="cap-store-banner__sum-content-total">新品 12</div>
                                </a>
                            </div>
                        </div>
                        <div class="store-collect-button-con">
                            <div class="store-collect-button">
                                <span class="module_ico module-ico-31shoucangxuanzhong"></span>
                                收藏
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="module-ad-empty empty-image_ad" v-else>
            <div class="image-empty-bg"></div>
            <div class="desc">店铺信息</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from "vue";
import { imageFormat } from "@/utils/format";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
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
import { shopInfoDefaults } from "./index";
import type { ShopInfoModule } from "./types";
const imgList = ref([img1, img2, img3, img4, img5, img6, img7, img8, img9, img10]);
const props = defineProps<{ module: ShopInfoModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, shopInfoDefaults);
const backgroundImg = computed(() => {
    let url;
    if (module.backgroundDefault == 0 && module.customPic.picUrl) {
        url = imageFormat(module.customPic.picUrl);
    } else {
        url = imgList.value[module.backgroundDefault - 1];
    }
    return url;
});
</script>
