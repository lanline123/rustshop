<template>
    <el-form-item :label="label" :rules="[{ required: productType == 4 }]">
        <div class="goods-info-edit-row" style="width: 100%">
            <div class="goods-info-edit-warp" v-if="!disabled">
                <div class="goods-info-edit-act flex">
                    <DialogForm type="gallery" class="item-bg" @okCallback="addPic" :params="{ isMultiple: true }">
                        <a class="add-item info-pic-add"><i class="iconfont icon-tianjiatupian"></i>{{ $t('添加图片') }}</a>
                    </DialogForm>
                    <DialogForm type="galleryVideo" class="item-bg" @okCallback="addVideo" :params="{ isMultiple: true }">
                        <a class="add-item info-pic-add"><SvgIcon name="product-add-video" width="17" height="17" />{{ $t('添加视频') }}</a>
                    </DialogForm>
                    <a class="add-item info-text-add" @click="addText"><i class="iconfont icon-wenben" style="font-size: 14px"></i>{{ $t('添加文本') }}</a>
                    <span>{{ $t('点击按钮添加图片、视频或者文本，您还可以通过拖拽进行排序') }}</span>
                </div>
                <div class="goods-info-edit-con">
                    <draggable
                        :key="aiBadgeRefreshKey"
                        class="info-edit-con-list"
                        item-key=""
                        :list="<any>descArr"
                        ghost-class="ghost"
                        chosen-class="chosenClass"
                        animation="300"
                        @start=""
                        @end=""
                    >
                        <template #item="{ element, index }">
                            <div class="info-edit-list-item" draggable="false" style="">
                                <div class="edit-list-item-img" v-if="element.type == 'pic'">
                                    <BusinessImg
                                        v-model:modelValue="element.pic"
                                        :picThumb="element.pic"
                                        :dataType="7"
                                        @Delete="onDelete"
                                        :style="{ width: '100px', height: '100px' }"
                                        :edit="false"
                                        :data="{ index: index }"
                                        @Edit="editPic"
                                    >
                                        <a class="lyecs-dialogImage" draggable="false"><img :src="imageFormat(element.pic)" draggable="false" /> </a>
                                    </BusinessImg>
                                    <DialogForm type="gallery" class="item-bg" @okCallback="editPic" :params="{ isMultiple: false }" :data="{ index: index }">
                                        <div class="btn-edit">{{ $t('修改') }}</div>
                                    </DialogForm>
                                </div>
                                <div class="edit-list-item-img edit-list-item-video" v-if="element.type == 'video'">
                                    <BusinessImg
                                        v-if="element.cover"
                                        v-model:modelValue="element.cover"
                                        :picThumb="element.cover"
                                        :dataType="7"
                                        :style="{ width: '100px', height: '100px' }"
                                        :edit="false"
                                        :data="{ index: index }"
                                    >
                                        <a class="lyecs-dialogImage" draggable="false"><img :src="imageFormat(element.cover)" draggable="false" /></a>
                                    </BusinessImg>
                                    <a v-else-if="element.videoUrl" class="lyecs-dialogImage" draggable="false">
                                        <video :src="imageFormat(element.videoUrl)" draggable="false"></video>
                                    </a>
                                    <div class="video-play-icon"><i class="iconfont-admin icon-shipin"></i></div>
                                    <DialogForm
                                        type="galleryVideo"
                                        class="item-bg"
                                        @okCallback="editVideo"
                                        :params="{ isMultiple: false }"
                                        :data="{ index: index }"
                                    >
                                        <div class="btn-edit">{{ $t('修改') }}</div>
                                    </DialogForm>
                                </div>
                                <div class="edit-list-item-text" v-if="element.type == 'text'">
                                    <p><i class="iconfont icon-1fuwenben"></i></p>
                                    <p>{{ $t('文本') }}</p>
                                    <div class="btn-edit" @click="editText(index)">{{ $t('修改') }}</div>
                                </div>
                                <i class="btn-del iconfont icon-cha" @click="del(index)" :class="{ overseas: isOverseas() }"></i>
                            </div>
                        </template>
                    </draggable>
                </div>
                <Modal
                    v-model:open="visible"
                    :title="$t('添加文本')"
                    @ok="onOk()"
                    :destroyOnClose="true"
                    :width="800"
                    :zIndex="textModalZIndex"
                    :getContainer="mountOverlayToBody"
                    wrapClassName="product-desc-text-modal"
                    :maskStyle="{ zIndex: textModalZIndex }"
                    :wrapStyle="{ zIndex: textModalZIndex }"
                >
                    <div class="relative">
                        <Editor v-model:html="editorHtml.html" height="300px"></Editor>
                        <BusinessData v-model:modelValue="editorHtml.html" :dataType="9" type="editor"></BusinessData>
                    </div>
                </Modal>
            </div>
            <div class="goods-detail-preview-warp" v-if="previewHtml" v-html="previewHtml"></div>
            <div class="goods-detail-preview-warp" v-else>
                <div class="preview-warp">{{ $t('商品描述预览区域') }}</div>
            </div>
        </div>
        <div class="extra" v-if="productType == 4">{{ $t('此处填写内容，将在商品付费后展示') }}</div>
    </el-form-item>
</template>
<script setup lang="ts">
import BusinessData from "@/components/multilingual/BusinessData.vue";
import BusinessImg from "@/components/multilingual/BusinessImg.vue";
import { ref, onMounted, computed, watch } from "vue";
import { DialogForm } from "@/components/dialog";
import { Editor } from "@/components/editor";
import { Modal, message } from "ant-design-vue";
import { DRAWER_NESTED_Z_INDEX, mountOverlayToBody } from "@/utils/overlayZIndex";
import draggable from "vuedraggable";
import { imageFormat } from "@/utils/format";
import { isOverseas } from "@/utils/version";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    descArr: { type: [Object, Array], default: [] },
    disabled: { type: Boolean, default: false },
    productType: { type: Number, default: 1 },
    label: { type: String, default: "商品详情" }
});
const descArr: any = ref(props.descArr || []);

const visible = ref(false);
const textModalZIndex = DRAWER_NESTED_Z_INDEX;
const editorHtml = ref({
    html: ""
});
const editTextIndex = ref<number>(0);
const emit = defineEmits(["update:descArr"]);

/** 翻译应用后强制详情图列表重新查询翻译标记 */
const aiBadgeRefreshKey = ref(0);

// 编辑页常见是异步回填：父组件 props 更新时，同步刷新本地可编辑数据
watch(
    () => props.descArr,
    (val) => {
        descArr.value = val || [];
    },
    { immediate: true, deep: true }
);
onMounted(() => {
    emit("update:descArr", descArr);
});
const addPic = (result: any) => {
    for (let idx in result) {
        descArr.value.push({
            pic: result[idx].picUrl,
            type: "pic"
        });
    }
};
const editPic = (result: any, data: any) => {
    descArr.value[editTextIndex.value] = {
        pic: result[0].picUrl,
        type: "pic"
    };
};
const addVideo = (result: any) => {
    for (let idx in result) {
        descArr.value.push({
            cover: result[idx].videoCover,
            videoUrl: result[idx].videoUrl,
            type: "video"
        });
    }
};
const editVideo = (result: any, data: any) => {
    descArr.value[data.index] = {
        cover: result[0].videoCover,
        videoUrl: result[0].videoUrl,
        type: "video"
    };
};
const previewHtml = computed(() => {
    let htmlArr = [];
    for (let idx in descArr.value) {
        if (descArr.value[idx].type == "pic") {
            htmlArr.push('<div class="desc-pic-item"><img src="' + imageFormat(descArr.value[idx].pic) + '"></div>');
        } else if (descArr.value[idx].type == "video") {
            const videoUrl = descArr.value[idx].videoUrl;
            const cover = descArr.value[idx].cover;
            if (videoUrl) {
                htmlArr.push(`
                    <div class="desc-video-item">
                        <video src="${imageFormat(videoUrl)}" controls preload="metadata" style="width:100%;display:block;"${cover ? ` poster="${imageFormat(cover)}"` : ""}></video>
                    </div>
                `);
            } else if (cover) {
                htmlArr.push(`
                    <div class="desc-video-item">
                        <div class="desc-video-item-wrap">
                            <i class="iconfont-admin icon-shipin"></i>
                        </div>
                        <img src="${imageFormat(cover)}">
                    </div>
                `);
            }
        } else {
            htmlArr.push(descArr.value[idx].html);
        }
    }
    return htmlArr.join("<div data-division=1></div>");
});
const addText = () => {
    visible.value = true;
    editorHtml.value.html = "";
};
const editText = (index: number) => {
    editorHtml.value.html = descArr.value[index].html;
    editTextIndex.value = index + 1;
    visible.value = true;
};
const del = (index: number) => {
    descArr.value.splice(index, 1);
};
const onDelete = (index: number) => {
    descArr.value.splice(index, 1);
};
const onOk = () => {
    if (editTextIndex.value > 0) {
        descArr.value[editTextIndex.value - 1] = {
            html: editorHtml.value.html,
            type: "text"
        };
    } else {
        descArr.value.push({
            html: editorHtml.value.html,
            type: "text"
        });
    }
    editTextIndex.value = 0;
    visible.value = false;
};
</script>

<style lang="less" scoped>
.goods-info-edit-row {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
}
/*商品内容编辑*/
.goods-info-edit-warp {
    flex: 1;
    padding: 15px;
    border: 1px solid #eee;
    box-sizing: border-box;
}

.goods-info-edit-act {
    margin-bottom: 10px;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px 0;
}

.goods-info-edit-act :deep(.add-item) {
    transition: all 0.3s;
    margin-right: 10px;
    display: flex;
    align-items: center;
    background: rgba(0, 0, 0, 0.06);
    border: 0;
    padding: 5px 20px;
    word-break: keep-all;
    color: #333;
}

.goods-info-edit-act :deep(.add-item:hover) {
    background: rgba(0, 0, 0, 0.1);
    color: #333;
}

.goods-info-edit-act :deep(.add-item i,
svg) {
    padding-right: 3px;
}
.goods-info-edit-act :deep(.add-item svg) {
    padding-top: 1px;
}

.goods-info-edit-act span {
    color: #999;
    padding-left: 10px;
}

.info-edit-list-ai {
    display: inline-flex;
    align-self: flex-end;
    align-items: flex-end;
    margin-top: 6px;
    margin-bottom: 10px;
}

.info-edit-con-list {
    display: flex;
    flex-wrap: wrap;
    min-height: 120px;
}

.info-edit-con-list .info-edit-list-item {
    width: 100px;
    height: 100px;
    border: 1px solid #eee;
    margin-right: 10px;
    margin-bottom: 10px;
    position: relative;
    margin-top: 6px;
    cursor: pointer;
}

.info-edit-con-list .info-edit-list-item .edit-list-item-img a {
    width: 100px;
    height: 100px;
    align-items: center;
    justify-content: center;
    display: flex;
}

.info-edit-con-list .info-edit-list-item .edit-list-item-img img {
    width: auto;
    height: auto;
    max-width: 100%;
    max-height: 100%;
    display: block;
}

.info-edit-con-list .info-edit-list-item .edit-list-item-video {
    .video-play-icon {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        pointer-events: none;
        z-index: 1;
        i {
            font-size: 30px;
            color: rgba(255, 255, 255, 0.85);
            text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
        }
    }
    video {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}

.info-edit-con-list .info-edit-list-item .edit-list-item-text {
    text-align: center;
    height: 100px;
}

.info-edit-con-list .info-edit-list-item .edit-list-item-text i {
    font-size: 26px;
    padding-top: 26px;
    display: inline-block;
    padding-bottom: 10px;
    color: #666;
}

.info-edit-con-list .info-edit-list-item .btn-del {
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

.info-edit-con-list .info-edit-list-item .btn-del.overseas {
    left: -10px !important;
    right: auto !important;
    z-index: 223;
}

.info-edit-con-list .info-edit-list-item:hover {
    border: 1px dashed #155bd4;
}

.info-edit-con-list .info-edit-list-item:hover .btn-del {
    display: block;
}

.info-edit-con-list .info-edit-list-item .btn-del:hover {
    background: #333;
}

.info-edit-con-list .info-edit-list-item :deep(.btn-edit) {
    display: none;
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 24px;
    background: rgba(0, 0, 0, 0.6);
    font-size: 14px;
    line-height: 24px;
    color: #fff;
    text-align: center;
}

.info-edit-con-list .info-edit-list-item:hover :deep(.btn-edit) {
    display: block;
}

.goods-detail-preview-warp {
    width: 375px;
    overflow-y: auto;
    max-height: 750px;
    min-height: 660px;
    border: 1px dashed #eee;
    .preview-warp {
        width: 375px;
        max-height: 750px;
        min-height: 660px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        color: #999;
        background-color: #f7f8fa;
    }
}
.goods-detail-preview-warp {
    :deep(.desc-video-item) {
        position: relative;
        .desc-video-item-wrap {
            position: absolute;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.2);
            z-index: 10;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            color: #fff;
            i {
                font-size: 50px;
                color: #fff;
            }
        }
        img {
            max-width: 100%;
            display: block;
            width: 100%;
        }
    }
    :deep(img) {
        max-width: 100%;
        display: block;
        width: 100%;
    }
}

@media (max-width: 768px) {
    .goods-detail-preview-warp {
        width: 100%;
        max-width: 320px;
        min-height: 480px;
        max-height: 600px;
        margin: 0 auto;
        box-sizing: border-box;
        
        .preview-warp {
            width: 100%;
            min-height: 480px;
            max-height: 600px;
        }
    }
}
</style>
<style lang="less">
.product-desc-text-modal {
    z-index: 2000 !important;
}
</style>
