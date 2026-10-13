<template>
    <div class="dec-picSelect-con">
        <div class="dec-pic-select">
            <div class="item-image">
                <div class="item-image-con" v-if="!photo.picThumb">
                    <DialogForm type="gallery" class="item-bg" @okCallback="onEdit" :params="{ isMultiple: false }" v-if="!photo.picThumb">
                        <div
                            class="flex"
                            style="flex-direction: column; line-height: 25px; width: 60px; height: 60px; align-items: center; justify-content: center"
                        >
                            <i class="iconfont icon-tianjiatupian" style="font-size: 20px; color: #888"></i>
                        </div>
                    </DialogForm>
                </div>
                <div class="item-image-con" v-if="photo.picThumb">
                    <DialogForm type="gallery" class="" @okCallback="onEdit" :params="{ isMultiple: false }">
                        <img class="item-image-src" :src="imageFormat(photo.picThumb)" />
                        <span class="change-image">{{ $t('更换图片') }}</span>
                    </DialogForm>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { imageFormat } from "@/utils/format";
import { AnyColumn } from "element-plus/es/components/table-v2/src/common";
const emit = defineEmits(["onChange"]);
// 动态解析props
const photo = defineModel<any>("photo", { type: Object, default: { picUrl: "", picThumb: "" } });
const onEdit = (result: AnyColumn) => {
    Object.assign(photo.value, result[0]);
    emit("onChange", photo.value);
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
.dec-picSelect-con {
    display: flex;
    align-items: center;
}
.dec-reset {
    height: 32px;
    line-height: 32px;
    margin-right: 10px;
    cursor: pointer;
    font-size: 14px;
}

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
