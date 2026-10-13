<template>
    <TotalContainer :module="module.frame" :class="'module-ad-con module-image_hotarea'">
        <Title :module="module.title"></Title>
        <!-- 占位显示：没有图片时 -->
        <div v-if="!module.picList || module.picList.length === 0" class="module-ad-empty empty-image_hotarea">
            <div class="image-empty-bg"></div>
            <div class="desc">{{ $t('点击添加图片') }}</div>
        </div>
        <!-- 有图片时显示图片列表 -->
        <template v-else-if="module.picList">
            <div class="image-hotarea-warp" :style="frameFormat.innerPadding">
                <div class="image-ad-con" ref="containerRef">
                    <template v-for="item in module.picList" :key="item.picId">
                        <div class="image-ad-item">
                            <div class="item-content" :style="format.imgPadding">
                                <div class="item-content-hotatrea">
                                    <div class="item-img-a">
                                        <el-image class="item-img" fit="contain" :src="imageFormat(item.picUrl)" />
                                    </div>
                                    <template v-if="item.hotarea">
                                        <div v-for="(subItem, index) in item.hotarea" :key="index" class="image-hotarea-link" :style="subItem.position">
                                            <span class="area_box_con_text">{{ subItem.link?.name }}</span>
                                        </div>
                                    </template>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
        </template>
    </TotalContainer>
</template>

<script lang="ts" setup>
//热图绘制页面组件
import { computed, ref, onMounted, onUnmounted, watch, Ref } from "vue";
import { TotalContainer } from "@/components/decorate/index";
import Title from "@/views/decorate/decorate/src/modules/pcCustomPage/components/Title.vue";
import { imageFormat, urlFormat } from "@/utils/format";
import { ModuleType, ModuleImageHotareaType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/";

// 使用 defineModel 实现双向绑定，确保设置生效
const module = defineModel<ModuleType & ModuleImageHotareaType>("module", { default: () => ({}) }) as Ref<ModuleType & ModuleImageHotareaType>;

// 合并默认值，确保 frame 和 title 有初始值
const defaultModule = ref({
    picList: [],
    imgPadding: 0,
    frame: defaultFrame,
    title: defaultTitle
});
mergeDefaultModule(module.value, defaultModule.value);

const { frame } = module.value;

// 格式化 frame 设置
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});

const format = computed(() => {
    return {
        imgPadding: "padding:" + (module.value.imgPadding || 0) + "px;"
    };
});

// 处理热区位置计算（参考移动端实现）
const containerRef = ref<HTMLDivElement>();
const containerWidth = ref(0);

const updateWidth = () => {
    if (containerRef.value) {
        containerWidth.value = containerRef.value.offsetWidth;
    }
    module.value.picList?.forEach((pics) => {
        if (pics.hotarea) {
            pics.hotarea.forEach((picHotarea) => {
                picHotarea.position =
                    "width:" +
                    resize(picHotarea.width ?? 0) +
                    "px;height:" +
                    resize(picHotarea.height ?? 0) +
                    "px;top:" +
                    resize(picHotarea.top ?? 0) +
                    "px;left:" +
                    resize(picHotarea.left ?? 0) +
                    "px";
            });
        }
    });
};

const resize = (size: number) => {
    return (containerWidth.value / 500) * size; // 500为绘制区域原始宽度px
};

// 监听容器宽度和模块数据的变化
watch(() => module.value, updateWidth, { immediate: true, deep: true });
onMounted(async () => {
    updateWidth();
    window.addEventListener("resize", updateWidth);
});
onUnmounted(() => {
    window.removeEventListener("resize", updateWidth);
});
</script>

<style lang="less" scoped>
.image-hotarea-warp {
    box-sizing: border-box;
    width: 100%;

    .image-ad-con {
        width: 100%;
    }

    .image-ad-item {
        width: 100%;
        position: relative;
        margin-bottom: 10px;

        &:last-child {
            margin-bottom: 0;
        }

        .item-content {
            width: 100%;
            position: relative;

            .item-content-hotatrea {
                position: relative;
                width: 100%;
                display: flex;

                .item-img-a {
                    width: 100%;
                    position: relative;
                    background: #f2f2f2;
                    box-sizing: border-box;

                    .item-img {
                        width: 100%;
                        height: auto;
                        display: block;
                    }
                }

                .image-hotarea-link {
                    position: absolute;
                    cursor: pointer;
                    z-index: 10;
                    transition: opacity 0.3s;

                    &:hover {
                        opacity: 0.8;
                    }

                    .area_box_con_text {
                        display: none;
                    }
                }
            }
        }
    }
}

/* 占位图片样式 */
.module-ad-empty {
    padding-top: 30px;
    padding-bottom: 50px;
    text-align: center;
    background-color: #f2f4f6;

    .image-empty-bg {
        background: url("@/views/decorate/decorate/src/img/empty-img-bg.png") no-repeat center center;
        background-size: 100% auto;
        width: 100px;
        margin: 0 auto 10px;
        height: 100px;
    }

    .desc {
        text-align: center;
        color: #888;
        font-size: 14px;
    }
}
</style>
