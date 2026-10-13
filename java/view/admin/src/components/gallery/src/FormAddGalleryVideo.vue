<template>
    <div class="gallery-pic">
        <div :class="'gallery-pic-select ' + (video.length > 0 ? 'have_image' : '') + (disabled ? 'disabled' : '')" v-if="!isMultiple && type == 1">
            <div class="item-img add-photo-btn">
                <template v-if="video.length > 0">
                    <div class="video-item-box-icon" @click.stop="openPreview(video[0])">
                        <i class="iconfont-admin icon-shipin"></i>
                    </div>
                    <Image v-if="video[0].videoCover" class="gallery-img" :src="imageFormat(video[0].videoCover)" />
                    <video v-if="!video[0].videoCover" :src="imageFormat(video[0].videoUrl)"></video>
                    <i v-show="!disabled" @click="removePic(0)" class="btn-del iconfont icon-cha"></i>
                </template>
                <DialogForm
                    v-else
                    type="galleryVideo"
                    class="item-bg"
                    @okCallback="onEdit"
                    :disabled="disabled"
                    :params="{ isMultiple: props.isMultiple }"
                    width="850px"
                    v-if="video.length == 0 && !disabled"
                >
                    <div
                        class="flex"
                        style="flex-direction: column; height: 80px; width: 80px; align-items: center; justify-content: center; line-height: 22px"
                    >
                        <SvgIcon name="product-add-video" width="20" height="20" />
                    </div>
                </DialogForm>
                <div
                    v-if="disabled"
                    class="flex"
                    style="flex-direction: column; height: 80px; width: 80px; align-items: center; justify-content: center; line-height: 22px"
                >
                    <SvgIcon name="product-add-video" width="20" height="20" />
                </div>
            </div>
            <div class="item-action" v-if="video.length > 0 && type == 1">
                <DialogForm type="galleryVideo" :disabled="disabled" class="item item-edit" @okCallback="onEdit" width="850px" :params="{ isMultiple: props.isMultiple }"
                    ><span class="iconfont icon-bianji1"></span>
                </DialogForm>
                <span @click="removeGallery" class="item item-remove iconfont icon-shanchu"></span>
            </div>
        </div>
        <div>
            <DialogForm type="galleryVideo" :disabled="disabled" @okCallback="onEdit" :params="{ isMultiple: props.isMultiple }" width="850px" v-if="type == 2">
                <div>
                    <slot></slot>
                </div>
            </DialogForm>
        </div>
        <div class="gallery-list-warp" v-if="isMultiple">
            <div>
                <draggable
                    class="gallery-list-ul"
                    item-key="picId"
                    :list="<any>videos"
                    ghost-class="ghost"
                    chosen-class="chosenClass"
                    animation="300"
                    @start=""
                    @end=""
                >
                    <template #item="{ element, index }">
                        <div class="item" data-id="img.imgId">
                            <div class="img flex flex-justify-center">
                                <DialogForm type="galleryVideo" :disabled="disabled" @okCallback="onEdit" width="850px" :params="{ isMultiple: props.isMultiple }">
                                    <a class="lyecs-dialogImage">
                                        <div class="video-item-box-icon" @click.stop="openPreview(element)">
                                            <i class="iconfont-admin icon-shipin"></i>
                                        </div>
                                        <Image v-if="element.videoCover" class="gallery-img" :big="element.videoCover" :src="imageFormat(element.videoCover)" />
                                        <video v-if="!element.videoCover" :src="imageFormat(element.videoUrl)"></video>
                                    </a>
                                </DialogForm>
                                <i v-show="!disabled" @click="removePic(index)" class="btn-del iconfont icon-cha"></i>
                            </div>
                        </div>
                    </template>

                    <template #footer>
                        <div v-if="disabled" class="item gallery-add-item add-gallery-photo-btn" draggable="false">
                            <i class="iconfont icon-tianjiatupian"></i>
                        </div>
                        <DialogForm v-else type="galleryVideo" :disabled="disabled" class="" @okCallback="onEdit" :params="{ isMultiple: props.isMultiple }" width="850px">
                            <div class="item gallery-add-item add-gallery-photo-btn" draggable="false">
                                <el-icon size="25"><Plus /></el-icon>
                            </div>
                        </DialogForm>
                    </template>
                </draggable>
            </div>
        </div>
        <el-dialog v-model="previewVisible" :title="previewVideo?.videoName || $t('视频预览')" width="760px" destroy-on-close>
            <video v-if="previewVideo" :src="imageFormat(previewVideo.videoUrl)" controls autoplay class="preview-player"></video>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref, toRefs } from "vue";
import type { Ref } from "vue";
import { DialogForm } from "@/components/dialog";
import draggable from "vuedraggable";
import { Image } from "@/components/image";
import { imageFormat } from "@/utils/format";
import { Plus } from "@element-plus/icons-vue";
const dom: Ref<HTMLDivElement> = ref(null) as any;
interface VideoItem {
    videoCover?: string;
    videoUrl?: string;
    videoName?: string;
}
const props = defineProps({
    video: {
        type: Array as () => VideoItem[],
        default: [] // {picId,picThumb,picUrl,picName}
    },
    videos: {
        type: Array as () => VideoItem[],
        default: [] // {picId,picThumb,picUrl,picName}
    },
    isMultiple: {
        type: Boolean,
        default: false
    },
    disabled: {
        type: Boolean,
        default: false
    },
    type: {
        type: Number,
        default: 1
    }
});
// 动态解析props
const { video, videos, isMultiple, disabled } = toRefs(props);
const previewVisible = ref(false);
const previewVideo = ref<VideoItem | null>(null);

const emit = defineEmits(["update:video", "update:videos"]);
const onEdit = (result: any) => {
    if (isMultiple.value == true) {
        emit("update:videos", videos.value.concat(result));
    } else {
        emit("update:video", result);
    }
};

const removePic = (index: number) => {
    if (isMultiple.value == true) {
        videos.value.splice(<any>index, 1);
        emit("update:videos", videos.value);
    } else {
        emit("update:video", []);
    }
};
const removeGallery = () => {
    emit("update:videos", []);
};
const openPreview = (item: VideoItem) => {
    if (!item?.videoUrl) return;
    previewVideo.value = item;
    previewVisible.value = true;
};
</script>

<style lang="less" scoped>
// 相册选择
.gallery-pic {
    width: 100%;
}
.preview-player {
    width: 100%;
    max-height: 520px;
    background: #000;
}
.gallery-pic-select {
    margin-bottom: 10px;
    position: relative;
    width: 80px;
    height: 80px;
    border: 1px dashed #ddd;
    vertical-align: middle;
    transition: all 0.3s;
    &.disabled {
        background-color: #f5f6f9;
        &:hover {
            border-style: dashed;
            border-color: #f5f6f9;
        }
        border-style: dashed;
        border-color: #f5f6f9;
    }
}

.gallery-pic-select.have_image {
    border-style: solid;
}

.gallery-pic-select:hover {
    border-style: dashed;
    border-color: #155bd4;
}

.gallery-pic-select .ant-image-img {
    display: inline-block;
    vertical-align: middle;
    height: auto;
    width: auto;
    max-width: 80px;
    max-height: 80px;
    cursor: pointer;
}

.gallery-pic-select a {
    display: flex;
    vertical-align: middle;
    margin-right: 8px;
    align-items: center;
}

.gallery-pic-select a.add-photo-direct-btn {
    display: none;
}

.gallery-pic-select a.add-photo-btn {
    background: #fff;
    border-color: #ddd;
    color: #333;
    position: absolute;
    background: rgba(0, 0, 0, 0.2);
    height: 30px;
    display: none;
}

.gallery-pic-select .item-img {
    display: block;
    width: 100%;
    height: 100%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    .video-item-box-icon {
        position: absolute;
        z-index: 4;
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        color: #fff;
        .iconfont-admin {
            font-size: 30px;
        }
    }
    video {
        width: 100%;
        height: 100%;
    }
    .btn-del {
        position: absolute;
        cursor: pointer;
        right: -10px;
        top: -10px;
        color: #fff;
        background: #dbdbdb;
        border-radius: 50%;
        z-index: 10;
        width: 18px;
        height: 18px;
        text-align: center;
        line-height: 18px;
        font-size: 12px;
        display: none;
    }
    &:hover {
        .btn-del {
            display: block;
        }
    }
}
.lyecs-dialogImage {
    position: relative;
    video {
        width: 100%;
        height: 100%;
    }
    .video-item-box-icon {
        position: absolute;
        z-index: 4;
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        color: #fff;
        .iconfont-admin {
            font-size: 30px;
        }
    }
}

.gallery-img {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.gallery-pic {
    :deep(.el-image__inner) {
        max-width: 100% !important;
        max-height: 100% !important;
    }
}

.gallery-pic-select .item-action {
    position: absolute;
    right: 0;
    bottom: 0;
    height: 20px;
    line-height: 20px;
    color: #fff;
    align-content: flex-end;
    width: 100%;
    opacity: 0;
    transition: all 0.3s;
    visibility: hidden;
    display: flex;
    background: rgba(0, 0, 0, 0.6);
    z-index: 5;
}
.gallery-pic-select .item-action :deep(.item) {
    display: inline-flex;
    width: 33%;
    align-content: center;
    justify-content: center;
    color: #fff;
    cursor: pointer;

    :first-child {
        margin-left: auto;
    }

    :hover {
        color: #fff;
    }

    .item-edit {
        font-size: 17px;
    }

    .item-remove {
        font-size: 14px;
    }
}

.gallery-pic-select.have_image:hover :deep(.item-action) {
    display: flex;
    opacity: 1;
    visibility: visible;
}

.gallery-pic-select .item-bg span {
    display: block;
    font-size: 12px;
    color: #999;
}

.gallery-pic-select .item-bg i {
    font-size: 20px;
    text-align: center;
    color: #999;
}

.gallery-pic-select .remove-photo-btn {
    display: none;
}

.gallery-pic-select a.add-photo-btn:hover {
    background: #fff;
    border-color: var(--tig-primary);
    color: var(--tig-primary);
}

.gallery-pic-select a i {
    margin-right: 5px;
}

/*商品相册*/
.gallery-list-warp {
    padding: 0;
    display: flex;
    flex-wrap: nowrap;
}

.gallery-list-warp ul {
    display: flex;
    flex-wrap: wrap;
}

.gallery-list-warp .item {
    width: 90px;
    height: 90px;
    margin-right: 10px;
    margin-bottom: 10px;
    position: relative;
}

.gallery-list-warp .gallery-add-item {
    width: 80px;
    height: 80px;
    //border: 1px dashed #ddd;
    //margin-right: 10px;
    //margin-bottom: 10px;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    background-color: #f5f6f9;
    cursor: pointer;
}

.gallery-list-warp .gallery-add-item :deep(.dialog-link) {
    width: 100%;
    height: 100%;
    align-items: center;
    justify-content: center;
}

.gallery-list-warp .gallery-add-item i {
    font-size: 22px;
    color: #969799;
}

.gallery-list-warp .gallery-add-item:hover {
    border-color: #155bd4;
    color: #155bd4;
}

.gallery-list-warp .gallery-list-ul {
    display: flex;
    flex-wrap: wrap;
}

.gallery-list-warp .item.add_photo a {
    background: url("../images/bg-addImage.png") no-repeat center center;
    width: 100%;
    height: 100%;
    display: block;
}

.gallery-list-warp .item.add_photo a:hover {
    background-color: #f2f2f2;
}

.gallery-list-warp .item .img {
    border: 1px solid #eee;
}

.gallery-list-warp .item .img a {
    display: flex;
    width: 80px;
    height: 80px;
    align-items: center;
    justify-content: center;
}

.gallery-list-warp .item .img img {
    width: 100%;
    height: auto;
}

.gallery-list-warp .item .desc {
    line-height: 25px;
    text-align: center;
    height: 25px;
}

.gallery-list-warp .item .desc input {
    width: 90px;
    padding: 0 6px;
    height: 20px;
    line-height: 20px;
    border-color: #eee;
}

.gallery-list-warp .item .btn-del {
    position: absolute;
    cursor: pointer;
    right: -10px;
    top: -10px;
    color: #fff;
    background: #dbdbdb;
    border-radius: 50%;
    z-index: 2;
    width: 18px;
    height: 18px;
    text-align: center;
    line-height: 18px;
    font-size: 12px;
    display: none;
}

.gallery-list-warp .item:hover .img {
    border: 1px dashed #155bd4;
}

.gallery-list-warp .item:hover .btn-del {
    display: block;
}

.gallery-list-warp .item .btn-del:hover {
    background: #333;
}
</style>
