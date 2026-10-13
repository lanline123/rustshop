<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>图片魔方</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">选择模板</div>
                        <div class="value">{{ picTypeLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group flex-nowrap" v-model="module.picType" @change="onPicTypeChange">
                            <el-tooltip effect="light" placement="bottom" content="一行两个" :show-after="100">
                                <el-radio-button :value="1"><i class="ico-decorate icon-dec-cuberow"></i></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="一行三个" :show-after="100">
                                <el-radio-button :value="2"><i class="ico-decorate icon-dec-cuberow2"></i></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="一行四个" :show-after="100">
                                <el-radio-button :value="3"><i class="ico-decorate icon-dec-cuberow1"></i></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="左二右二" :show-after="100">
                                <el-radio-button :value="4"><i class="ico-decorate icon-dec-cube"></i></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="左一右二" :show-after="100">
                                <el-radio-button :value="5"><i class="ico-decorate icon-dec-cubeto"></i></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="左一右三" :show-after="100">
                                <el-radio-button :value="6"><i class="ico-decorate icon-dec-cubeto1"></i></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="上一下二" :show-after="100">
                                <el-radio-button :value="7"><i class="ico-decorate icon-dec-cube-upto"></i></el-radio-button>
                            </el-tooltip>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="title">选择布局</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-square-group">
                            <div class="dec-square-item dec-square-item-empty" v-if="picType === 1 || picType === 2 || picType === 3">
                                <div class="square-item-row">
                                    <div
                                        class="square-item-box"
                                        v-for="(item, index) in picList"
                                        :key="index"
                                        :class="{ selected: picIndex === index }"
                                        @click="selectSlot(index)"
                                    >
                                        <img v-if="item.picThumb" :src="imageFormat(item.picThumb || '')" />
                                    </div>
                                </div>
                            </div>
                            <div class="dec-square-item dec-square-item-4 dec-square-item-empty" v-else-if="picType === 4 && picList.length === 4">
                                <div class="square-item-row">
                                    <div class="square-item-col">
                                        <div class="square-item-box" :class="{ selected: picIndex === 0 }" @click="selectSlot(0)">
                                            <img v-if="picList[0].picThumb" :src="imageFormat(picList[0].picThumb || '')" />
                                        </div>
                                        <div class="square-item-box" :class="{ selected: picIndex === 1 }" @click="selectSlot(1)">
                                            <img v-if="picList[1].picThumb" :src="imageFormat(picList[1].picThumb || '')" />
                                        </div>
                                    </div>
                                    <div class="square-item-col">
                                        <div class="square-item-box" :class="{ selected: picIndex === 2 }" @click="selectSlot(2)">
                                            <img v-if="picList[2].picThumb" :src="imageFormat(picList[2].picThumb || '')" />
                                        </div>
                                        <div class="square-item-box" :class="{ selected: picIndex === 3 }" @click="selectSlot(3)">
                                            <img v-if="picList[3].picThumb" :src="imageFormat(picList[3].picThumb || '')" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="dec-square-item dec-square-item-5 dec-square-item-empty" v-else-if="picType === 5 && picList.length === 3">
                                <div class="square-item-row">
                                    <div class="square-item-col">
                                        <div class="square-item-box square-item-box-col" :class="{ selected: picIndex === 0 }" @click="selectSlot(0)">
                                            <img v-if="picList[0].picThumb" :src="imageFormat(picList[0].picThumb || '')" />
                                        </div>
                                    </div>
                                    <div class="square-item-col">
                                        <div class="square-item-box" :class="{ selected: picIndex === 1 }" @click="selectSlot(1)">
                                            <img v-if="picList[1].picThumb" :src="imageFormat(picList[1].picThumb || '')" />
                                        </div>
                                        <div class="square-item-box" :class="{ selected: picIndex === 2 }" @click="selectSlot(2)">
                                            <img v-if="picList[2].picThumb" :src="imageFormat(picList[2].picThumb || '')" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="dec-square-item dec-square-item-empty" v-else-if="picType === 7 && picList.length === 3">
                                <div class="square-item-col">
                                    <div class="square-item-row">
                                        <div class="square-item-box" :class="{ selected: picIndex === 0 }" @click="selectSlot(0)">
                                            <img v-if="picList[0].picThumb" :src="imageFormat(picList[0].picThumb || '')" />
                                        </div>
                                    </div>
                                    <div class="square-item-row">
                                        <div class="square-item-box" :class="{ selected: picIndex === 1 }" @click="selectSlot(1)">
                                            <img v-if="picList[1].picThumb" :src="imageFormat(picList[1].picThumb || '')" />
                                        </div>
                                        <div class="square-item-box" :class="{ selected: picIndex === 2 }" @click="selectSlot(2)">
                                            <img v-if="picList[2].picThumb" :src="imageFormat(picList[2].picThumb || '')" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="dec-square-item dec-square-item-7 dec-square-item-empty" v-else-if="picType === 6 && picList.length === 4">
                                <div class="square-item-row">
                                    <div class="square-item-col">
                                        <div class="square-item-box square-item-box-col" :class="{ selected: picIndex === 0 }" @click="selectSlot(0)">
                                            <img v-if="picList[0].picThumb" :src="imageFormat(picList[0].picThumb || '')" />
                                        </div>
                                    </div>
                                    <div class="square-item-col">
                                        <div class="square-item-col">
                                            <div class="square-item-row">
                                                <div class="square-item-box" :class="{ selected: picIndex === 1 }" @click="selectSlot(1)">
                                                    <img v-if="picList[1].picThumb" :src="imageFormat(picList[1].picThumb || '')" />
                                                </div>
                                            </div>
                                            <div class="square-item-row">
                                                <div class="square-item-box" :class="{ selected: picIndex === 2 }" @click="selectSlot(2)">
                                                    <img v-if="picList[2].picThumb" :src="imageFormat(picList[2].picThumb || '')" />
                                                </div>
                                                <div class="square-item-box" :class="{ selected: picIndex === 3 }" @click="selectSlot(3)">
                                                    <img v-if="picList[3].picThumb" :src="imageFormat(picList[3].picThumb || '')" />
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
                        <div class="title">添加图片</div>
                        <div class="value">固定 {{ requiredPicCount }} 张</div>
                    </div>
                    <div class="dec-edit-group-desc">
                        <div class="pic-change-desc">提示：先选模板确定格子数量，再点上方格子逐个上传图片。</div>
                    </div>
                    <div class="dec-edit-group-con" v-if="picList.length > 0">
                        <PicList :is-multiple="false" :show-text="true" :show-desc="false" v-model:photo="currentPhoto" />
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">图片边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider v-model="module.imgPadding" :max="30" show-input :show-input-controls="false" size="small" input-size="default" />
                        </div>
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">标题设置</div>
                </div>
                <ModuleTitleSettings :model-value="module.title" @update:model-value="(v) => (module.title = v)" />
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">容器设置</div>
                </div>
                <ModuleContainerSettings :model-value="module.frame" show-inner-padding @update:model-value="(v) => (module.frame = v)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 图片魔方模块 - 设置组件
 * 交互：选择模板决定格子数量 → 在「选择布局」里点格子选中槽位 → 单图 PicList 编辑该槽位
 * 数据：直连 store.activeModuleData 原地修改（与移动端新套图片魔方一致，公共子组件零改造复用）
 */
import { ref, computed, onMounted } from "vue";
import { imageFormat } from "@/utils/format";
import { PicList } from "@/components/decorate";
import { SettingsTabs, ModuleContainerSettings, ModuleTitleSettings } from "../_shared";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import { LAYOUT_PIC_COUNT } from "./types";
import type { PcImageSquareAdModule } from "./types";
import type { PicListItem } from "../_shared/types";

defineOptions({ name: "PcImageSquareAdSettings" });

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcImageSquareAdModule);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

const picIndex = ref(0);

const picType = computed(() => Number(module.value.picType ?? 1));
const picList = computed<PicListItem[]>(() => module.value.picList ?? []);
const requiredPicCount = computed(() => LAYOUT_PIC_COUNT[picType.value] ?? 2);

function createEmptyPic(): PicListItem {
    return { picId: "0", picUrl: "", picThumb: "", picName: "", picTitle: "", picLink: { link: "", name: "" } };
}

/** 把 picList 原地补齐/裁剪到指定数量（布局切换 / 旧数据数量不符时维持固定格子数） */
function resizePicList(count: number) {
    // 仅图片魔方模块可执行，防止异步加载竞态污染其他模块 picList
    if (store.activeModule?.type !== "pc-image-square-ad") return;
    if (!Array.isArray(module.value.picList)) module.value.picList = [];
    const list = module.value.picList;
    if (list.length < count) {
        while (list.length < count) list.push(createEmptyPic());
    } else if (list.length > count) {
        list.splice(count);
    }
}

function onPicTypeChange(value: string | number | boolean | undefined) {
    picIndex.value = 0;
    resizePicList(LAYOUT_PIC_COUNT[Number(value)] ?? 2);
}

function selectSlot(index: number) {
    picIndex.value = index;
}

// 单图 PicList 编辑当前选中槽位：换图/清空走 setter 整体替换，标题/链接走 getter 返回的真实对象原地改
const currentPhoto = computed<PicListItem>({
    get: () => picList.value[picIndex.value] ?? createEmptyPic(),
    set: (val) => {
        if (Array.isArray(module.value.picList)) module.value.picList[picIndex.value] = val;
    }
});

onMounted(() => {
    resizePicList(requiredPicCount.value);
    if (picIndex.value >= picList.value.length) picIndex.value = 0;
});

const picTypeLabels: Record<number, string> = {
    1: "一行两个",
    2: "一行三个",
    3: "一行四个",
    4: "左二右二",
    5: "左一右二",
    6: "左一右三",
    7: "上一下二"
};

const picTypeLabel = computed(() => picTypeLabels[picType.value] || "一行两个");
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}

.dec-radio-group {
    .ico-decorate {
        font-size: 18px;
    }
}
</style>
