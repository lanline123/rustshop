<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('模块图片广告') }}</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('选择模板') }}</div>
                <div class="value">{{ selectLabel.picType[module.picType] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.picType" @change="onPicTypeChange">
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行两个')" :show-after="100">
                        <el-radio-button :value="1"><i class="ico-decorate icon-dec-cuberow"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行三个')" :show-after="100">
                        <el-radio-button :value="2"><i class="ico-decorate icon-dec-cuberow2"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行四个')" :show-after="100">
                        <el-radio-button :value="3"><i class="ico-decorate icon-dec-cuberow1"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('左二右二')" :show-after="100">
                        <el-radio-button :value="4"><i class="ico-decorate icon-dec-cube"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('左一右二')" :show-after="100">
                        <el-radio-button :value="5"><i class="ico-decorate icon-dec-cubeto"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('左一右三')" :show-after="100">
                        <el-radio-button :value="6"><i class="ico-decorate icon-dec-cubeto1"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('上一下二')" :show-after="100">
                        <el-radio-button :value="7"><i class="ico-decorate icon-dec-cube-upto"></i></el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('选择布局') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-square-group">
                    <div class="dec-square-item dec-square-item-empty" v-if="module.picType == 1 || module.picType == 2 || module.picType == 3">
                        <div class="square-item-row">
                            <div
                                class="square-item-box"
                                v-for="(item, index) in module.picList"
                                :class="{ selected: picIndex == index }"
                                @click="onPicChange(item, index)"
                            >
                                <img v-if="item.picThumb" :src="imageFormat(item.picThumb || '')" />
                            </div>
                        </div>
                    </div>
                    <div class="dec-square-item dec-square-item-4 dec-square-item-empty" v-if="module.picType == 4 && module.picList.length == 4">
                        <div class="square-item-row">
                            <div class="square-item-col">
                                <div class="square-item-box" :class="{ selected: picIndex == 0 }" @click="onPicChange(module.picList[0].picThumb, 0)">
                                    <img v-if="module.picList[0].picThumb" :src="imageFormat(module.picList[0].picThumb || '')" />
                                </div>
                                <div class="square-item-box" :class="{ selected: picIndex == 1 }" @click="onPicChange(module.picList[1].picThumb, 1)">
                                    <img v-if="module.picList[1].picThumb" :src="imageFormat(module.picList[1].picThumb || '')" />
                                </div>
                            </div>
                            <div class="square-item-col">
                                <div class="square-item-box" :class="{ selected: picIndex == 2 }" @click="onPicChange(module.picList[2].picThumb, 2)">
                                    <img v-if="module.picList[2].picThumb" :src="imageFormat(module.picList[2].picThumb || '')" />
                                </div>
                                <div class="square-item-box" :class="{ selected: picIndex == 3 }" @click="onPicChange(module.picList[3].picThumb, 3)">
                                    <img v-if="module.picList[3].picThumb" :src="imageFormat(module.picList[3].picThumb || '')" />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="dec-square-item dec-square-item-5 dec-square-item-empty" v-if="module.picType == 5 && module.picList.length == 3">
                        <div class="square-item-row">
                            <div class="square-item-col">
                                <div
                                    class="square-item-box square-item-box-col"
                                    :class="{ selected: picIndex == 0 }"
                                    @click="onPicChange(module.picList[0].picThumb, 0)"
                                >
                                    <img v-if="module.picList[0].picThumb" :src="imageFormat(module.picList[0].picThumb || '')" />
                                </div>
                            </div>
                            <div class="square-item-col">
                                <div class="square-item-box" :class="{ selected: picIndex == 1 }" @click="onPicChange(module.picList[1].picThumb, 1)">
                                    <img v-if="module.picList[1].picThumb" :src="imageFormat(module.picList[1].picThumb || '')" />
                                </div>
                                <div class="square-item-box" :class="{ selected: picIndex == 2 }" @click="onPicChange(module.picList[2].picThumb, 2)">
                                    <img v-if="module.picList[2].picThumb" :src="imageFormat(module.picList[2].picThumb || '')" />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="dec-square-item dec-square-item-empty" v-if="module.picType == 7 && module.picList.length == 3">
                        <div class="square-item-col">
                            <div class="square-item-row">
                                <div class="square-item-box" :class="{ selected: picIndex == 0 }" @click="onPicChange(module.picList[0].picThumb, 0)">
                                    <img v-if="module.picList[0].picThumb" :src="imageFormat(module.picList[0].picThumb || '')" />
                                </div>
                            </div>
                            <div class="square-item-row">
                                <div class="square-item-box" :class="{ selected: picIndex == 1 }" @click="onPicChange(module.picList[1].picThumb, 1)">
                                    <img v-if="module.picList[1].picThumb" :src="imageFormat(module.picList[1].picThumb || '')" />
                                </div>
                                <div class="square-item-box" :class="{ selected: picIndex == 2 }" @click="onPicChange(module.picList[2].picThumb, 2)">
                                    <img v-if="module.picList[2].picThumb" :src="imageFormat(module.picList[2].picThumb || '')" />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="dec-square-item dec-square-item-7 dec-square-item-empty" v-if="module.picType == 6 && module.picList.length == 4">
                        <div class="square-item-row">
                            <div class="square-item-col">
                                <div
                                    class="square-item-box square-item-box-col"
                                    :class="{ selected: picIndex == 0 }"
                                    @click="onPicChange(module.picList[0].picThumb, 0)"
                                >
                                    <img v-if="module.picList[0].picThumb" :src="imageFormat(module.picList[0].picThumb || '')" />
                                </div>
                            </div>
                            <div class="square-item-col">
                                <div class="square-item-col">
                                    <div class="square-item-row">
                                        <div class="square-item-box" :class="{ selected: picIndex == 1 }" @click="onPicChange(module.picList[1].picThumb, 1)">
                                            <img v-if="module.picList[1].picThumb" :src="imageFormat(module.picList[1].picThumb || '')" />
                                        </div>
                                    </div>
                                    <div class="square-item-row">
                                        <div class="square-item-box" :class="{ selected: picIndex == 2 }" @click="onPicChange(module.picList[2].picThumb, 2)">
                                            <img v-if="module.picList[2].picThumb" :src="imageFormat(module.picList[2].picThumb || '')" />
                                        </div>
                                        <div class="square-item-box" :class="{ selected: picIndex == 3 }" @click="onPicChange(module.picList[3].picThumb, 3)">
                                            <img v-if="module.picList[3].picThumb" :src="imageFormat(module.picList[3].picThumb || '')" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('添加图片') }}</div>
            </div>
            <div class="dec-edit-group-con" v-if="module.picList && module.picList.length > 0">
                <PicList :isMultiple="false" v-model:photo="module.picList[picIndex]" decorateType="pc"></PicList>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('图片边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.imgPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
        <CommonTitleEdit v-model="module.title" decorateType="pc"></CommonTitleEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { imageFormat } from "@/utils/format";
import { PicList } from "@/components/decorate";
import { ref, onMounted, nextTick, watchEffect } from "vue";
import CommonTitleEdit from "../../src/commonTitle/Edit.vue";
import CommonFrameEdit from "../../src/commonFrame/Edit.vue";
import { ModuleType, ModuleImageType } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const selectLabel = ref<any>({
    picType: {
        1: t('一行两个'),
        2: t('一行三个'),
        3: t('一行四个'),
        4: t('左二右二'),
        5: t('左一右二'),
        6: t('左一右三'),
        7: t('上一下二')
    },
    picPageType: {
        1: t('样式一'),
        2: t('样式二'),
        3: t('样式三 ')
    }
});
const module = defineModel<ModuleType & ModuleImageType>("module", { default: () => ({}) });
const picIndex = ref<number>(0);
onMounted(() => {
    // 初始化 picList 为数组（如果为空或未定义）
    if (!module.value.picList || !Array.isArray(module.value.picList)) {
        module.value.picList = [];
    }
    // 如果 picList 为空，则根据当前 picType 初始化
    if (module.value.picList.length < 1) {
        onPicTypeChange(module.value.picType || 1);
    }
});
const onPicTypeChange = (e: any) => {
    picIndex.value = 0;
    let obj = {
        picId: 0,
        picThumb: "",
        picUrl: "",
        picName: ""
    };
    const lengthMap: any = {
        1: 2,
        2: 3,
        5: 3,
        7: 3,
        3: 4,
        4: 4,
        6: 4
    };
    let length: number = lengthMap[e] || 2;
    let now: number = module.value.picList.length;
    if (length >= now) {
        module.value.picList.push(...Array.from({ length: length - now }, () => obj));
    } else {
        console.log(now - 1, now - length);
        module.value.picList.splice(-(now - length));
    }
};
const onPicChange = (item: any, index: number) => {
    picIndex.value = index;
};

watchEffect(()=>{
    if (module.value?.picList?.length < 1) {
        onPicTypeChange(1);
    }
})
</script>
