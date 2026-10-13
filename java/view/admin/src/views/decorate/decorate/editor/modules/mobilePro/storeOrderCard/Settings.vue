<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>点单卡片</h3>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <!-- 模块样式：选择模板 -->
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">选择模板</div>
                    <div class="value">{{ selectLabel.picType[module.picType] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picType" @change="onPicTypeChange">
                        <el-tooltip effect="light" placement="bottom" content="一行一个" :show-after="100">
                            <el-radio-button :value="1"><i class="ico-decorate icon-dec-up2end"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="一行两个" :show-after="100">
                            <el-radio-button :value="2"><i class="ico-decorate icon-dec-cuberow"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="左一右二" :show-after="100">
                            <el-radio-button :value="3"><i class="ico-decorate icon-dec-cubeto"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="一行三个" :show-after="100">
                            <el-radio-button :value="4"><i class="ico-decorate icon-dec-cuberow2"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="一行四个" :show-after="100">
                            <el-radio-button :value="5"><i class="ico-decorate icon-dec-cuberow1"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="两行五个" :show-after="100">
                            <el-radio-button :value="6"><i class="ico-decorate icon-dec-hybrid"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <!-- 卡片风格：图文模式 / 图片模式 -->
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">卡片风格</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group v-model="module.cardStyle">
                        <el-radio :value="1">图文模式</el-radio>
                        <el-radio :value="2">图片模式</el-radio>
                    </el-radio-group>
                </div>
            </div>
            <!-- 卡片布局预览（点击选中卡片） -->
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <div class="store-order-card-preview" :class="'preview-type-' + module.picType">
                        <template v-if="module.picType === 3">
                            <div class="preview-row">
                                <div class="preview-item preview-item-large" :class="{ selected: picIndex === 0 }" @click="onPicChange(0)">
                                    <CardPreview :item="module.cardList[0]" :card-style="module.cardStyle" size="medium"></CardPreview>
                                </div>
                                <div class="preview-col">
                                    <div class="preview-item" :class="{ selected: picIndex === 1 }" @click="onPicChange(1)">
                                        <CardPreview :item="module.cardList[1]" :card-style="module.cardStyle" size="small"></CardPreview>
                                    </div>
                                    <div class="preview-item" :class="{ selected: picIndex === 2 }" @click="onPicChange(2)">
                                        <CardPreview :item="module.cardList[2]" :card-style="module.cardStyle" size="small"></CardPreview>
                                    </div>
                                </div>
                            </div>
                        </template>
                        <template v-else-if="module.picType === 6">
                            <div class="preview-row">
                                <div
                                    v-for="i in 2"
                                    :key="'top-' + i"
                                    class="preview-item"
                                    :class="{ selected: picIndex === i - 1 }"
                                    @click="onPicChange(i - 1)"
                                >
                                    <CardPreview :item="module.cardList[i - 1]" :card-style="module.cardStyle" size="medium"></CardPreview>
                                </div>
                            </div>
                            <div class="preview-row">
                                <div
                                    v-for="i in 3"
                                    :key="'bot-' + i"
                                    class="preview-item"
                                    :class="{ selected: picIndex === i + 1 }"
                                    @click="onPicChange(i + 1)"
                                >
                                    <CardPreview :item="module.cardList[i + 1]" :card-style="module.cardStyle" size="small"></CardPreview>
                                </div>
                            </div>
                        </template>
                        <template v-else>
                            <div class="preview-row">
                                <div
                                    v-for="(item, index) in module.cardList as any[]"
                                    :key="index"
                                    class="preview-item"
                                    :class="{ selected: picIndex === index }"
                                    @click="onPicChange(index)"
                                >
                                    <CardPreview :item="item" :card-style="module.cardStyle" :size="module.picType === 1 ? 'large' : 'small'"></CardPreview>
                                </div>
                            </div>
                        </template>
                    </div>
                </div>
            </div>
            <!-- 当前选中卡片编辑区 -->
            <div class="dec-edit-group dec-edit-group-block store-order-card-editor" v-if="currentCard">
                <div class="dec-edit-group-con">
                    <div class="editor-row">
                        <span class="editor-label">链接：</span>
                        <div class="editor-value">
                            <SelectLink v-model="currentCard.link" decorateType="mobile"></SelectLink>
                        </div>
                    </div>
                    <template v-if="module.cardStyle === 1">
                        <div class="editor-row">
                            <span class="editor-label">标题：</span>
                            <div class="editor-value">
                                <el-input v-model="currentCard.title" placeholder="请输入标题" maxlength="10" show-word-limit></el-input>
                            </div>
                        </div>
                        <div class="editor-row">
                            <span class="editor-label">描述：</span>
                            <div class="editor-value">
                                <el-input v-model="currentCard.desc" placeholder="请输入描述" maxlength="20" show-word-limit></el-input>
                            </div>
                        </div>
                    </template>
                    <div class="editor-row editor-row-image">
                        <span class="editor-label">图片：</span>
                        <div class="editor-value">
                            <div class="card-pic-box">
                                <DialogForm type="gallery" @okCallback="onPicSelected" :params="{ isMultiple: false }">
                                    <div class="card-pic-inner" :class="{ 'has-image': !!(currentCard.picThumb || currentCard.picUrl) }">
                                        <img v-if="currentCard.picThumb || currentCard.picUrl" :src="productImage(currentCard)" />
                                        <template v-else>
                                            <i class="ico-decorate icon-dec-jia"></i>
                                        </template>
                                        <span v-if="currentCard.picThumb || currentCard.picUrl" class="card-pic-change">更换图片</span>
                                    </div>
                                </DialogForm>
                            </div>
                            <div class="editor-tip">建议尺寸 160*160 像素</div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 图片模式下未配置提示 -->
            <div class="store-order-card-warning" v-if="hasEmptyPicCard">存在未配置的点单卡片</div>
            <div class="dec-divider-line"></div>
            <!-- 卡片样式 -->
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">卡片样式</div>
                    <div class="value">{{ selectLabel.boxStyle[module.boxStyle || ""] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.boxStyle">
                        <el-radio-button :value="1">无边白底</el-radio-button>
                        <el-radio-button :value="2">卡片投影</el-radio-button>
                        <el-radio-button :value="3">描边白底</el-radio-button>
                        <el-radio-button :value="4">无边透明底</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <!-- 背景颜色（模块整体背景，默认透明） -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">背景颜色</div>
                    <div class="value">{{ module.backgroundColor || "透明色" }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.backgroundColor = ''">重置</a>
                            <SelectColor v-model:color="module.backgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 卡片背景（默认白色）；无边白底 / 无边透明底 时卡片背景固定，隐藏此设置 -->
            <div class="dec-edit-group" v-if="![1, 4].includes(Number(module.boxStyle))">
                <div class="dec-edit-group-title">
                    <div class="label">卡片背景</div>
                    <div class="value">{{ module.cardBackground || "#ffffff" }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.cardBackground = '#ffffff'">重置</a>
                            <SelectColor v-model:color="module.cardBackground"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 卡片间距：卡片与卡片之间的间隙 -->
            <div class="dec-edit-group" v-if="Number(module.picType) !== 1">
                <div class="dec-edit-group-title">
                    <div class="label">卡片间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.cardGap" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <!-- 通用框架设置：圆角、内边距、左右边距、上下边距（背景色由内容设置里的 module.backgroundColor 单独控制，此处隐藏；上边距允许负值以整体上移） -->
            <CommonFrameEdit v-model="module.frame" hide-bg allow-negative-top split-inner-padding></CommonFrameEdit>
            <!-- 图片边距：在卡片内边距之外，单独控制图片四周留白 -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片上边距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.imagePaddingTop" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片下边距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.imagePaddingBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片左右边距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.imagePaddingLeftRight" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref, computed, onMounted, defineComponent, h } from "vue";
import { productImage } from "@/utils/format";
import { SelectColor, SelectLink } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import Tabs from "@/components/tabs/Index.vue";
import { defaultFrame, mergeDefaultModule } from "@/views/decorate/decorate/src/modules/";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import { moduleFrameDefault, LENGTH_MAP, buildDefaultCard } from "./index";
import type { StoreOrderCardModule } from "./types";

const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");

const selectLabel = ref<any>({
    picType: {
        1: "一行一个",
        2: "一行两个",
        3: "左一右二",
        4: "一行三个",
        5: "一行四个",
        6: "两行五个"
    },
    boxStyle: {
        1: "无边白底",
        2: "卡片投影",
        3: "描边白底",
        4: "无边透明底"
    }
});

// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as StoreOrderCardModule);
const picIndex = ref<number>(0);

// 历史数据仅有单个 innerPadding 时，用它回填左右/上下内边距
const migrateInnerPadding = (frame: any) => {
    if (!frame || typeof frame !== "object") return;
    if (frame.innerPaddingLeftRight === undefined || frame.innerPaddingLeftRight === null) {
        frame.innerPaddingLeftRight = frame.innerPadding ?? 10;
    }
    if (frame.innerPaddingTopBottom === undefined || frame.innerPaddingTopBottom === null) {
        frame.innerPaddingTopBottom = frame.innerPadding ?? 10;
    }
};

// 扩展设置里的图片边距挂在 frame 上，onMounted 已保证 frame 存在
const frame = computed<Record<string, any>>(() => module.value.frame || {});

const currentCard = computed(() => module.value.cardList?.[picIndex.value]);

// 检测未配置的卡片：图片模式必须配图；图文模式下图/标题/描述至少填一个
const hasEmptyPicCard = computed(() => {
    const list = (module.value.cardList || []) as any[];
    if (Number(module.value.cardStyle) === 2) {
        return list.some((c: any) => !c?.picUrl && !c?.picThumb);
    }
    return list.some((c: any) => !c?.picUrl && !c?.picThumb && !c?.title && !c?.desc);
});

onMounted(() => {
    if (!module.value.cardList || module.value.cardList.length === 0) {
        module.value.cardList = [];
        onPicTypeChange(module.value.picType || 2);
    }
    if (!module.value.frame || typeof module.value.frame !== "object") {
        module.value.frame = { ...defaultFrame, ...moduleFrameDefault };
    } else {
        migrateInnerPadding(module.value.frame);
        mergeDefaultModule(module.value.frame, { ...defaultFrame, ...moduleFrameDefault });
    }
});

const onPicTypeChange = (e: number) => {
    picIndex.value = 0;
    const target: number = LENGTH_MAP[e] || 2;
    const list: any[] = module.value.cardList || [];
    const now = list.length;
    if (target > now) {
        for (let i = 0; i < target - now; i++) list.push(buildDefaultCard());
    } else if (target < now) {
        list.splice(target);
    }
    module.value.cardList = list;
};

const onPicChange = (index: number) => {
    picIndex.value = index;
};

const onPicSelected = (result: any) => {
    if (!Array.isArray(result) || !result[0]) return;
    const pic = result[0];
    const card = module.value.cardList?.[picIndex.value];
    if (!card) return;
    card.picId = pic.picId ?? 0;
    card.picUrl = pic.picUrl ?? "";
    card.picThumb = pic.picThumb ?? "";
    card.picName = pic.picName ?? "";
};

// 卡片缩略预览子组件（局部使用，避免污染公共组件）
const CardPreview = defineComponent({
    props: {
        item: { type: Object, default: () => ({}) },
        cardStyle: { type: Number, default: 1 },
        size: { type: String, default: "small" }
    },
    setup(props) {
        return () => {
            const isImageMode = props.cardStyle === 2;
            const hasPic = !!(props.item?.picUrl || props.item?.picThumb);
            if (isImageMode) {
                return h("div", { class: ["card-preview-image", "size-" + props.size] }, [
                    hasPic
                        ? h("img", { src: productImage(props.item) })
                        : h("div", { class: "card-preview-placeholder" }, props.size === "large" ? "328*280像素" : "")
                ]);
            }
            return h("div", { class: ["card-preview-textimg", "size-" + props.size] }, [
                h("div", { class: "card-preview-text" }, [
                    h("div", { class: "card-preview-title" }, props.item?.title || "标题"),
                    h("div", { class: "card-preview-desc" }, props.item?.desc || "描述文案")
                ]),
                h("div", { class: "card-preview-img" }, [
                    hasPic ? h("img", { src: productImage(props.item) }) : h("div", { class: "card-preview-placeholder" })
                ])
            ]);
        };
    }
});
</script>
<style lang="less" scoped>
.store-order-card-preview {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .preview-row {
        display: flex;
        gap: 8px;
    }

    .preview-col {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8px;
    }

    .preview-item {
        flex: 1;
        border: 1px solid #ebedf0;
        border-radius: 4px;
        cursor: pointer;
        overflow: hidden;
        min-height: 60px;
        background-color: #f7f8fa;
        display: flex; // 让内部 CardPreview 能撑满高度，配合内部 justify-content 居中
        flex-direction: column;

        // 选中态参考图片魔方（image-cube）：浅蓝底 + 蓝色边框，覆盖内部卡片背景
        &.selected {
            border-color: #155bd4;
            background-color: #eef2ff;

            :deep(.card-preview-textimg),
            :deep(.card-preview-image) {
                background: #eef2ff;
            }
        }

        &.preview-item-large {
            flex: 1;
        }
    }
}

// 卡片预览渲染样式 — 默认垂直居中，size-large 水平布局
.store-order-card-preview :deep(.card-preview-textimg) {
    display: flex;
    flex-direction: column-reverse; // dom 顺序 text→img，视觉上 img 在上、text 在下
    align-items: center;
    justify-content: center;
    gap: 4px;
    padding: 10px;
    background: #ffffff;
    min-height: 84px;
    box-sizing: border-box;
    text-align: center;
    flex: 1; // 撑满 preview-item 高度，让 justify-content: center 相对整卡居中

    .card-preview-text {
        min-width: 0;
        width: 100%;
    }
    .card-preview-title {
        font-size: 13px;
        color: #333;
        font-weight: 500;
        line-height: 1.4;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .card-preview-desc {
        font-size: 11px;
        color: #999;
        margin-top: 2px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .card-preview-img {
        width: 44px;
        height: 44px;
        flex-shrink: 0;
        border: 1px solid #eee;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;
        img {
            max-width: 100%;
            max-height: 100%;
        }
    }

    // 一行一个：水平布局，文字左 + 图标右
    &.size-large {
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        text-align: left;
        gap: 8px;
        min-height: 90px;

        .card-preview-text {
            flex: 1;
            width: auto;
        }
        .card-preview-title {
            font-size: 14px;
        }
        .card-preview-img {
            width: 60px;
            height: 60px;
        }
    }

    // 中等尺寸（一行两个 / 两行五个上排 / 左一右二左侧）
    &.size-medium {
        .card-preview-img {
            width: 50px;
            height: 50px;
        }
    }
}

// 左一右二左侧大卡：视觉更大（图更大、字更大），与画布 View 保持一致
.store-order-card-preview .preview-item-large :deep(.card-preview-textimg) {
    gap: 6px;

    .card-preview-img {
        width: 80px;
        height: 80px;
    }
    .card-preview-title {
        font-size: 16px;
    }
    .card-preview-desc {
        font-size: 13px;
    }
}

// 左一右二左侧大卡的图片模式：不锁 1:1，撑满格子实际高度
.store-order-card-preview .preview-item-large :deep(.card-preview-image) {
    aspect-ratio: auto;
    flex: 1;
    height: 100%;
}

.store-order-card-preview :deep(.card-preview-image) {
    background: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    min-height: 60px;
    aspect-ratio: 1 / 1; // 图片模式 160:160 正方形
    img {
        width: 100%;
        height: 100%;
        object-fit: contain;
    }
    .card-preview-placeholder {
        color: #999;
        font-size: 12px;
    }
}

.store-order-card-editor {
    background-color: #f7f8fa;
    padding: 12px 10px;
    border-radius: 4px;
    margin: 0 10px;

    .editor-row {
        display: flex;
        align-items: center;
        margin-bottom: 10px;

        &:last-child {
            margin-bottom: 0;
        }

        .editor-label {
            color: #969799;
            font-size: 14px;
            flex-shrink: 0;
        }

        .editor-value {
            flex: 1;
            min-width: 0;
        }
    }

    .editor-row-image {
        align-items: flex-start;

        .editor-label {
            padding-top: 6px;
        }
    }

    .editor-tip {
        color: #999;
        font-size: 12px;
        margin-top: 4px;
    }
}

.store-order-card-warning {
    color: #f56c6c;
    font-size: 12px;
    padding: 8px 10px 0;
}

.card-pic-box {
    display: flex;
    align-items: flex-end;
    gap: 10px;

    .card-pic-inner {
        position: relative;
        width: 60px;
        height: 60px;
        background: #fff;
        border: 1px solid #e0e0e0;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;

        i {
            color: var(--tig-primary, #155bd4);
            font-size: 20px;
        }

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .card-pic-change {
            position: absolute;
            left: 0;
            right: 0;
            bottom: 0;
            height: 20px;
            line-height: 20px;
            text-align: center;
            color: #fff;
            font-size: 12px;
            background-color: rgba(0, 0, 0, 0.5);
            opacity: 0;
            transition: opacity 0.2s;
        }

        &.has-image:hover .card-pic-change {
            opacity: 1;
        }
    }

    .card-pic-reset {
        font-size: 12px;
        color: var(--tig-primary, #155bd4);
        cursor: pointer;

        &:hover {
            text-decoration: underline;
        }
    }
}
</style>
