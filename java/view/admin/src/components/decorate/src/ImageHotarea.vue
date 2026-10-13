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
                        <div class="hotarea-edit-box">
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
                                                    :params="{ act: 'edit', id: 1, type:'mobile', excludeShopId: true }"
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
                <a-button @click="visible = false">{{ $t('取消') }}</a-button>
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
import { reactive, ref, toRefs } from "vue";
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
const hotImgWidth = 328;
const visible = ref(false);
// 动态解析props
const photos = ref(<ModuleImageHotareaPicListType[]>props.photos);
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
const onEditHotarea = (index: number) => {
    visible.value = true;
    editPicIndex.value = index;
    hotarea.value = cloneDeep(photos.value[editPicIndex.value].hotarea) || []; //深拷贝，用于存保存
};
// 操作热图
const hotarea = ref(<HotareaType[]>[]);
const hotareaIndex = ref(0);
const addHotarea = () => {
    hotarea.value.push({
        height: 100,
        width: 100,
        left: 10,
        top: 10
    });
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
    visible.value = false;
    photos.value[editPicIndex.value].hotarea = hotarea.value;
    console.log("editPicIndex.value", editPicIndex.value);
    console.log("photos.value", photos.value);
};
const dealSize = (size: number) => {
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
