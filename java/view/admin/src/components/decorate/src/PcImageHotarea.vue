<template>
    <div class="dec-pic-group dec-pic-hotarea-group">
        <div class="dec-pic-group-list">
            <div v-for="(pic, index) in photos" class="dec-pic-group-item" data-index="{$smarty.foreach.picList.index}">
                <div class="dec-pic-group-item-con">
                    <div class="item-image">
                        <div class="item-image-con">
                            <img v-if="pic.picUrl" @click="onEditHotarea(index)" class="item-image-src" :src="imageFormat(pic.picUrl)" />
                            <div
                                v-if="pic.hotarea"
                                v-for="(hotarea, idx) in pic.hotarea"
                                @click="onEditHotarea(index)"
                                class="image-hotarea-link"
                                :style="{
                                    width: dealSize(hotarea.width) + 'px',
                                    height: dealSize(hotarea.height) + 'px',
                                    top: dealSize(hotarea.top) + 'px',
                                    left: dealSize(hotarea.left) + 'px'
                                }"
                            >
                                <span class="area_box_con_text">{{ hotarea.link?.name }}</span>
                            </div>

                            <DialogForm
                                type="gallery"
                                class=""
                                @okCallback="onEdit"
                                :params="{ isMultiple: false }"
                                :data="{ index: index }"
                                style="width: 100%"
                            >
                                <span class="change-image">{{ $t('更换图片') }}</span>
                            </DialogForm>
                        </div>
                    </div>
                    <div class="del-item" @click="removePic(index)"><i class="ico-decorate icon-dec-cha"></i></div>
                </div>
            </div>
        </div>
        <Modal v-model:open="visible" width="500px" :destroyOnClose="true" :title="$t('绘制热区')" class="noPadding">
            <div class="hotarea-edit-dialog-warp">
                <div class="hotarea-edit-dialog-con">
                    <div class="hotarea-edit-box-wrap">
                        <div ref="editBoxRef" class="hotarea-edit-box">
                            <img v-if="photos[editPicIndex].picUrl" class="bgImg" :src="imageFormat(photos[editPicIndex].picUrl)" />
                            <div class="hotarea-edit-box-area">
                                <VueDragResizeRotate
                                    v-for="(area, index) in hotarea"
                                    class=""
                                    :parent="true"
                                    :min-width="30"
                                    :min-height="30"
                                    :w="area.width"
                                    :h="area.height"
                                    :x="area.left"
                                    :y="area.top"
                                    @resizing="onResizing"
                                    @activated="onActivated(index)"
                                    @dragstop="onDragStop"
                                >
                                    <a-dropdown :trigger="['contextmenu']" style="width: 100%; height: 100%">
                                        <div class="area-bg">
                                            <span class="area_box_con_text">{{ area.link?.name || $t('右键编辑') }}</span>
                                        </div>
                                        <template #overlay>
                                            <a-menu>
                                                <DialogForm
                                                    :params="{ act: 'edit', id: 1, type: 'pc', excludeShopId: true }"
                                                    :data="{ index: index }"
                                                    isDrawer
                                                    path="link/LinkSelect"
                                                    style="width: 100%"
                                                    :title="$t('选择链接')"
                                                    width="700px"
                                                    @okCallback="onSelect"
                                                >
                                                    <div class="input-with-clear">
                                                        <a-menu-item>{{ $t('编辑链接') }}</a-menu-item>
                                                    </div>
                                                </DialogForm>
                                                <!-- <a-menu-item>复制区域</a-menu-item> -->
                                                <a-menu-item @click="onRemoveHotarea(index)">{{ $t('删除区域') }}</a-menu-item>
                                            </a-menu>
                                        </template>
                                    </a-dropdown>
                                </VueDragResizeRotate>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <template #footer>
                <a-button style="margin-right: 8px" @click="addHotarea">{{ $t('添加区域') }}</a-button>
                <a-button style="margin-right: 8px" type="primary" @click="onSave">{{ $t('保存') }}</a-button>
                <a-button @click="onCancel">{{ $t('取消') }}</a-button>
            </template>
        </Modal>
        <div class="dec-pic-add-con">
            <DialogForm type="gallery" class="" @okCallback="onAdd" :params="{ isMultiple: true }" style="width: 100%">
                <div class="dec-pic-add-btn"><i class="ico-decorate icon-dec-jia"></i>{{ $t('添加新热图') }}</div>
            </DialogForm>
        </div>
    </div>
</template>
<script setup lang="ts">
import { reactive, ref, toRefs, watch } from "vue";
import { DialogForm } from "@/components/dialog";
import { cloneDeep } from "lodash";
import { Modal } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import VueDragResizeRotate from "@gausszhou/vue3-drag-resize-rotate";
import { ModuleType, ModuleImageHotareaType, ModuleImageHotareaPicListType, HotareaType, LinkType } from "@/types/decorate/decorate.d";

const props = defineProps({
    photos: {
        type: Array,
        default: [] // {picId,picThumb,picUrl,picName}
    },
    defaultValue: {
        type: Object
    }
});

const emit = defineEmits<{
    "update:photos": [value: ModuleImageHotareaPicListType[]];
}>();

const hotImgWidth = 328;
const visible = ref(false);
// 动态解析props
const photos = ref(<ModuleImageHotareaPicListType[]>props.photos);

// 监听 props.photos 的变化，同步到内部 photos
watch(
    () => props.photos,
    (newVal) => {
        photos.value = <ModuleImageHotareaPicListType[]>newVal;
    },
    { deep: true }
);

// 监听 photos 的变化，同步到父组件（实现 v-model）
watch(
    photos,
    (newVal) => {
        emit("update:photos", newVal);
    },
    { deep: true }
);

const onEdit = (result: any, data: any) => {
    Object.assign(photos.value[data.index], result[0]);
};
const onAdd = (result: any) => {
    photos.value.push(...result);
};
const removePic = (index: number) => {
    photos.value.splice(index, 1);
};
// 编辑热图
const editPicIndex = ref(0);
const originalHotarea = ref<HotareaType[]>([]); // 保存原始数据，用于取消时恢复
const onEditHotarea = (index: number) => {
    visible.value = true;
    editPicIndex.value = index;
    const currentHotarea = photos.value[editPicIndex.value].hotarea || [];
    hotarea.value = cloneDeep(currentHotarea); //深拷贝，用于编辑
    originalHotarea.value = cloneDeep(currentHotarea); // 保存原始数据
};
// 操作热图
const hotarea = ref(<HotareaType[]>[]);
const hotareaIndex = ref(0);
const editBoxRef = ref<HTMLElement | null>(null);
const isSaved = ref(false); // 标记是否通过保存按钮关闭

// 实时同步 hotarea 的变化到 photos，实现预览效果
watch(
    hotarea,
    (newVal) => {
        if (visible.value && photos.value[editPicIndex.value]) {
            // 深拷贝避免引用问题，实时更新预览
            photos.value[editPicIndex.value].hotarea = cloneDeep(newVal);
        }
    },
    { deep: true }
);

// 监听弹窗关闭，如果关闭时没有保存，恢复原始数据
watch(visible, (newVal, oldVal) => {
    // 当弹窗从打开变为关闭时
    if (oldVal && !newVal && !isSaved.value) {
        // 如果不是通过保存按钮关闭的（点击取消或遮罩层），恢复原始数据
        if (photos.value[editPicIndex.value]) {
            photos.value[editPicIndex.value].hotarea = cloneDeep(originalHotarea.value);
        }
    }
    // 重置保存标志
    if (!newVal) {
        isSaved.value = false;
    }
});

// 新增热区默认尺寸 / 避让扫描步长（参考 Vort HotareaEditor：自动找空白位放置，避免新增热区全堆在左上角重叠）
const DEFAULT_HOTAREA_SIZE = 100;
const HOTAREA_SCAN_STEP = 20;
const HOTAREA_PADDING = 10;

// 两个矩形是否相交
const isOverlap = (a: { left: number; top: number; width: number; height: number }, b: HotareaType) => {
    const bw = b.width ?? 0;
    const bh = b.height ?? 0;
    const bl = b.left ?? 0;
    const bt = b.top ?? 0;
    return !(a.left + a.width <= bl || bl + bw <= a.left || a.top + a.height <= bt || bt + bh <= a.top);
};

const addHotarea = () => {
    const rect = editBoxRef.value?.getBoundingClientRect();
    const boxW = rect && rect.width > 0 ? rect.width : 500;
    const boxH = rect && rect.height > 0 ? rect.height : 500;
    const maxLeft = boxW - DEFAULT_HOTAREA_SIZE - HOTAREA_PADDING;
    const maxTop = boxH - DEFAULT_HOTAREA_SIZE - HOTAREA_PADDING;

    // 逐行扫描空白处放置新热区，避开已有热区
    let placed: HotareaType | null = null;
    for (let y = HOTAREA_PADDING; y <= maxTop && !placed; y += HOTAREA_SCAN_STEP) {
        for (let x = HOTAREA_PADDING; x <= maxLeft; x += HOTAREA_SCAN_STEP) {
            const candidate = { left: x, top: y, width: DEFAULT_HOTAREA_SIZE, height: DEFAULT_HOTAREA_SIZE };
            if (!hotarea.value.some((item) => isOverlap(candidate, item))) {
                placed = candidate;
                break;
            }
        }
    }

    // 找不到空位则级联偏移兜底
    if (!placed) {
        const offset = hotarea.value.length * HOTAREA_SCAN_STEP;
        const safeWidth = Math.max(maxLeft - HOTAREA_PADDING, HOTAREA_SCAN_STEP);
        const safeHeight = Math.max(maxTop - HOTAREA_PADDING, HOTAREA_SCAN_STEP);
        placed = {
            width: DEFAULT_HOTAREA_SIZE,
            height: DEFAULT_HOTAREA_SIZE,
            left: HOTAREA_PADDING + (offset % safeWidth),
            top: HOTAREA_PADDING + (offset % safeHeight)
        };
    }

    hotarea.value.push(placed);
    hotareaIndex.value = hotarea.value.length - 1;
};
const onActivated = (index: number) => {
    hotareaIndex.value = index;
};
const onDragStop = (x: string, y: string) => {
    hotarea.value[hotareaIndex.value].left = parseInt(x);
    hotarea.value[hotareaIndex.value].top = parseInt(y);
};
const onResizing = (x: string, y: string, width: string, height: string) => {
    hotarea.value[hotareaIndex.value].left = parseInt(x);
    hotarea.value[hotareaIndex.value].top = parseInt(y);
    hotarea.value[hotareaIndex.value].width = parseInt(width);
    hotarea.value[hotareaIndex.value].height = parseInt(height);
};
const onRemoveHotarea = (index: number) => {
    hotarea.value.splice(index, 1);
};

const onSave = () => {
    // 标记为已保存
    isSaved.value = true;
    // 确保最终保存（虽然 watch 已经实时更新了，这里再次确认）
    photos.value[editPicIndex.value].hotarea = cloneDeep(hotarea.value);
    visible.value = false;
    console.log("editPicIndex.value", editPicIndex.value);
    console.log("photos.value", photos.value);
};

// 取消编辑时恢复原始数据
const onCancel = () => {
    // 不设置 isSaved，让 watch 处理恢复逻辑
    visible.value = false;
};
const dealSize = (size: number | undefined) => {
    if (size === undefined || size === null) return 0;
    return (size / 500) * hotImgWidth;
};
const onSelect = (e: LinkType, data: any) => {
    hotareaIndex.value = data.index;
    hotarea.value[hotareaIndex.value].link = e;
    console.log(hotarea.value);
};
</script>

<style lang="less" scoped>
.item-image {
    margin: 0;
    position: relative;
}

.item-image .item-image-con {
    position: relative;
    width: 60px;
    height: 60px;
    border: 1px solid #e5e5e5;
    text-align: center;
    background: #f2f2f2;
    cursor: pointer;
    transition: all 0.3s;
}

.item-image .item-image-con img {
    box-sizing: border-box;
    vertical-align: bottom;
    height: 100%;
    width: 100%;
    -o-object-fit: contain;
    object-fit: contain;
}

.item-image .item-image-con .change-image {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 20px;
    line-height: 20px;
    font-size: 12px;
    color: #fff;
    background: rgba(0, 0, 0, 0.5);
    cursor: pointer;
}

.item-image .dec-pic-reset {
    width: 100%;
    text-align: center;
    top: auto;
    line-height: 25px;
}

.item-image-con :deep(.item-bg) {
    display: flex;
    align-items: center;
    height: 100%;
    justify-content: center;
    cursor: pointer;
}
</style>
