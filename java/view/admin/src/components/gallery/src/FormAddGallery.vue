<template>
    <div class="gallery-pic">
        <div :class="'gallery-pic-select ' + (photo ? 'have_image' : '')" v-if="!isMultiple && type == 1">
            <div class="item-img add-photo-btn">
                <BusinessImg
                    v-if="pageType == 'overseas' && photo"
                    v-model:modelValue="photo"
                    :picThumb="photo"
                    :dataType="dataType"
                    @Edit="onEdit"
                    @Delete="removeGallery"
                >
                    <Image v-if="photo" class="gallery-img" :src="photo" :isLocal="isLocal" />
                </BusinessImg>
                <Image v-if="pageType !== 'overseas' && photo" class="gallery-img" :src="photo" :isLocal="isLocal" />
                <el-upload
                    v-if="!photo && directUpload"
                    class="item-bg"
                    :show-file-list="false"
                    :accept="directUploadAccept"
                    :before-upload="beforeDirectUpload"
                    :http-request="handleDirectUpload"
                    :disabled="disabled || directUploading"
                >
                    <div
                        class="flex"
                        style="flex-direction: column; height: 80px; width: 80px; align-items: center; justify-content: center; line-height: 22px"
                    >
                        <i class="iconfont icon-tianjiatupian"></i><span>{{ $t('添加图片') }}</span>
                    </div>
                </el-upload>
                <DialogForm v-else-if="!photo" type="gallery" class="item-bg" @okCallback="onEdit" :params="{ isMultiple: props.isMultiple }">
                    <div
                        class="flex"
                        style="flex-direction: column; height: 80px; width: 80px; align-items: center; justify-content: center; line-height: 22px"
                    >
                        <i class="iconfont icon-tianjiatupian"></i><span>{{ $t('添加图片') }}</span>
                    </div>
                </DialogForm>
                <p class="remove-btn" v-if="photo && !disabled" :class="{ left: isOverseas() }">
                    <i @click="removeGallery" class="iconfont icon-cha"></i>
                </p>
            </div>
            <div class="item-action" v-if="photo && type == 1 && !disabled">
                <el-upload
                    v-if="directUpload"
                    class="item item-edit"
                    :show-file-list="false"
                    :accept="directUploadAccept"
                    :before-upload="beforeDirectUpload"
                    :http-request="handleDirectUpload"
                    :disabled="disabled || directUploading"
                ><span class="iconfont icon-bianji1"></span></el-upload>
                <DialogForm v-else type="gallery" class="item item-edit" @okCallback="onEdit" :params="{ isMultiple: props.isMultiple }"
                    ><span class="iconfont icon-bianji1"></span>
                </DialogForm>
                <!-- <span @click="removeGallery" class="item item-remove iconfont icon-shanchu"></span> -->
            </div>
        </div>
        <div>
            <DialogForm type="gallery" @okCallback="onEdit" :params="{ isMultiple: props.isMultiple }" v-if="type == 2">
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
                    :list="<any>photos"
                    ghost-class="ghost"
                    chosen-class="chosenClass"
                    animation="300"
                    @start=""
                    @end=""
                >
                    <template #item="{ element, index }">
                        <div class="item" data-id="img.imgId">
                            <div class="img flex flex-justify-center">
                                <BusinessImg
                                    v-if="pageType == 'product'"
                                    v-model:modelValue="element.picUrl"
                                    :translationName="element.picOriginal || element.picUrl || element.picThumb"
                                    :picThumb="element.picThumb"
                                    :dataId="element.picId"
                                    :dataType="7"
                                    @Edit="onEdit"
                                    @Delete="removePic"
                                >
                                    <a class="tig-dialogImage">
                                        <Image class="gallery-img" :big="element.picUrl" :src="element.picThumb" :isLocal="isLocal" />
                                    </a>
                                </BusinessImg>
                                <a v-else class="tig-dialogImage">
                                    <Image class="gallery-img" :big="element.picUrl" :src="element.picThumb" :isLocal="isLocal" />
                                </a>
                                <i v-show="!disabled" @click="removePic(index)" class="btn-del iconfont icon-cha" :class="{ overseas: isOverseas() }"></i>
                            </div>
                        </div>
                    </template>

                    <template #footer>
                        <div v-if="disabled"></div>
                        <DialogForm v-else type="gallery" class="" @okCallback="onEdit" :params="{ isMultiple: props.isMultiple }">
                            <div class="item gallery-add-item add-gallery-photo-btn" draggable="false">
                                <el-icon size="25"><Plus /></el-icon>
                            </div>
                        </DialogForm>
                        <slot name="append"></slot>
                    </template>
                </draggable>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, toRefs } from "vue";
import type { Ref } from "vue";
import type { UploadProps, UploadRequestOptions } from "element-plus";
import { DialogForm } from "@/components/dialog";
import draggable from "vuedraggable";
import { Image } from "@/components/image";
import { imageFormat } from "@/utils/format";
import { Plus } from "@element-plus/icons-vue";
import BusinessImg from "@/components/multilingual/BusinessImg.vue";
import { isOverseas } from "@/utils/version";
import { message } from "ant-design-vue";
import { useI18n } from "vue-i18n"
import { isWithinImageUploadLimit } from "@/utils/imageUploadLimit";
const { t } = useI18n()
const dom: Ref<HTMLDivElement> = ref(null) as any;

const props = defineProps({
    photo: {
        type: String,
        default: ""
    },
    photos: {
        type: Array,
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
    },
    pageType: {
        type: String,
        default: ""
    },
    dataType: {
        type: Number,
        default: 0
    },
    // 图片数量限制
    maxCount: {
        type: Number
    },
    // 是否使用本地图片
    isLocal: {
        type: Boolean,
        default: false
    },
    directUpload: {
        type: Boolean,
        default: false
    },
    directUploadRequest: {
        type: Function,
        default: undefined
    },
    directUploadMaxSize: {
        type: Number,
        default: 0
    },
    directUploadAccept: {
        type: String,
        default: "image/jpeg,image/png,image/bmp"
    }
});
// 动态解析props
const { photo, isMultiple, disabled } = toRefs(props);

const photos = defineModel<any>("photos", {
    type: Array,
    default: []
});

const emit = defineEmits(["update:photo", "update:photos", "change", "uploading-change"]);
const directUploading = ref(false);

const beforeDirectUpload: UploadProps["beforeUpload"] = (file) => {
    if (props.directUploadMaxSize > 0 && !isWithinImageUploadLimit(file, props.directUploadMaxSize)) {
        message.warning(t('{name} 超过 {imageUploadMaxSize}MB 限制', { name: file.name, imageUploadMaxSize: props.directUploadMaxSize }));
        return false;
    }
    return true;
};

const handleDirectUpload = async (options: UploadRequestOptions) => {
    if (!props.directUploadRequest) return;
    directUploading.value = true;
    emit("uploading-change", true);
    try {
        const url = await props.directUploadRequest(options.file);
        if (typeof url !== "string" || !url) throw new Error(t("图片上传失败"));
        emit("update:photo", url);
        emit("change", [url]);
        options.onSuccess(url);
    } catch (error: any) {
        message.error(error?.message || t("图片上传失败"));
        options.onError(error);
    } finally {
        directUploading.value = false;
        emit("uploading-change", false);
    }
};
const onEdit = (result: any) => {
    if (props.maxCount && photos.value?.length + result.length > props.maxCount) {
        message.error(t('图片数量不能超过{maxCount}张', { maxCount: props.maxCount }));
        return;
    }
    if (isMultiple.value) {
        let _photos = photos.value || [];
        const next = _photos.concat(result);
        emit("update:photos", next);
        emit("change", next.map((item: any) => item.picUrl || item.picThumb).filter(Boolean));
    } else {
        emit("update:photo", result[0].picUrl);
        emit("change", [result[0].picUrl].filter(Boolean));
    }
};

const removePic = (index: number) => {
    photos.value.splice(<any>index, 1);
};
const removeGallery = () => {
    emit("update:photo", "");
};
// 相册预览功能
const previewVisible = ref(false);
const previewImage = ref("");
const handleCancel = () => {
    previewImage.value = "";
};
const previewPhoto = (v: any) => {
    previewVisible.value = true;
    previewImage.value = v;
};
</script>

<style lang="less" scoped>
// 相册选择
.gallery-pic {
    width: 100%;
}
.gallery-pic-select {
    // margin-bottom: 10px;
    position: relative;
    width: 80px;
    height: 80px;
    border: 1px dashed #ddd;
    vertical-align: middle;
    transition: all 0.3s;
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

.gallery-pic-select .add-photo-btn {
    background: #fff;
    border-color: #ddd;
    color: #333;
    // background: rgba(0, 0, 0, 0.2);
    height: 30px;
    display: none;
    .remove-btn {
        display: none;
        position: absolute;
        top: -10px;
        right: -10px;
        background-color: #f5f7fa;
        width: 20px;
        height: 20px;
        border-radius: 50%;
        text-align: center;
        line-height: 18px;
        i {
            font-size: 12px;
        }
    }
    .left {
        left: -10px !important;
        right: auto !important;
        z-index: 223;
    }
}

.gallery-pic-select .item-img {
    display: block;
    width: 100%;
    height: 100%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f6f9;
}

.tig-dialogImage {
    width: 80px;
    height: 80px;
}

.gallery-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
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

.gallery-pic-select .add-photo-btn:hover {
    // background: #fff;
    border-color: var(--tig-primary);
    color: var(--tig-primary);
    .remove-btn {
        display: block;
    }
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
    width: 80px;
    height: 80px;
    margin-right: 10px;
    margin-bottom: 10px;
    position: relative;
    // border: 1px solid #eee;
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
    width: 80px;
    height: 80px;
    border: 1px solid #eee;
}

.gallery-list-warp .item .img a {
    display: flex;
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

.gallery-list-warp .item .btn-del.overseas {
    left: -10px !important;
    right: auto !important;
    z-index: 223;
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
