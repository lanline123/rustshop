<template>
    <div style="width: 100%">
        <div class="gallery-list-warp">
            <div class="dec-pic-group">
                <div class="dec-pic-group-list">
                    <draggable
                        class="gallery-list-ul"
                        item-key=""
                        :list="photos"
                        ghost-class="ghost"
                        chosen-class="chosenClass"
                        animation="300"
                        @start=""
                        @end=""
                    >
                        <template #item="{ element, index }">
                            <div class="dec-pic-group-item">
                                <div class="dec-pic-group-item-con" :class="{ 'has-item-index': showItemIndex }">
                                    <div v-if="showItemIndex" class="dec-pic-group-item-title">第【{{ index + 1 }}】项</div>
                                    <div class="item-image" v-if="showImage">
                                        <div class="item-image-con" v-if="element.picThumb">
                                            <BusinessImg
                                                v-if="pageType == 'overseas'"
                                                v-model:modelValue="element.picUrl"
                                                :picThumb="element.picThumb"
                                                :dataId="element.picId"
                                                :dataType="dataType"
                                                @Edit="onEdit"
                                            >
                                                <img class="item-image-src" :src="imageFormat(element.picThumb)" />
                                            </BusinessImg>
                                            <img v-else class="item-image-src" :src="imageFormat(element.picThumb)" />
                                            <DialogForm type="gallery" class="" @okCallback="onEdit" :params="{ isMultiple: false }" :data="{ index: index }">
                                                <span class="change-image">{{ $t("更换图片") }}</span>
                                            </DialogForm>
                                        </div>
                                    </div>
                                    <div class="item-info">
                                        <div class="item-info-item item-info-title" v-if="showText">
                                            <span class="lable">{{ $t("标题") }}</span>
                                            <TigInput width="100%" :placeholder="$t('建议10个字以内，可不填')" v-model="element.picTitle"></TigInput>
                                        </div>
                                        <div class="item-info-item item-info-title" v-if="showDesc">
                                            <span class="lable">{{ $t("描述") }}</span>
                                            <TigInput width="100%" :placeholder="$t('可不填')" v-model="element.picDesc"></TigInput>
                                        </div>
                                        <div class="item-info-item item-info-link" v-if="showLink">
                                            <span class="lable">{{ $t("链接") }}</span>
                                            <div class="lyecs-link-select">
                                                <SelectLink
                                                    v-model="element.picLink"
                                                    :disabled="element.type == 'default'"
                                                    :decorateType="decorateType"
                                                ></SelectLink>
                                            </div>
                                        </div>
                                        <div class="item-info-item item-info-color" v-if="showColor">
                                            <span class="lable">{{ $t("背景色") }}</span>
                                            <div class="lyecs-link-select">
                                                <div class="flex flex-align-center">
                                                    <div><SelectColor v-model:color="element.gradientColorA"></SelectColor></div>
                                                    <div style="margin: 0 10px">-</div>
                                                    <div><SelectColor v-model:color="element.gradientColorB"></SelectColor></div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div v-if="isMultiple == true" class="del-item" @click="removePic(index)"><i class="ico-decorate icon-dec-cha"></i></div>
                                </div>
                            </div>
                        </template>
                    </draggable>
                </div>
                <div class="dec-pic-group-list" v-if="isMultiple == false">
                    <div class="dec-pic-group-item">
                        <div class="dec-pic-group-item-con">
                            <div class="item-image" v-if="showImage">
                                <!-- 无图片时显示方框+加号样式 -->
                                <DialogForm v-if="!photo.picThumb" type="gallery" class="" @okCallback="onEdit" :params="{ isMultiple: false }">
                                    <div class="pic-add-box">
                                        <a href="javascript:;"><i class="ico-decorate icon-dec-jia"></i></a>
                                    </div>
                                </DialogForm>
                                <!-- 有图片时显示原有样式 -->
                                <div class="item-image-con" v-else>
                                    <BusinessImg
                                        v-if="pageType == 'overseas'"
                                        v-model:modelValue="photo.picUrl"
                                        :picThumb="photo.picThumb"
                                        :dataId="photo.picId"
                                        :dataType="dataType"
                                        @Edit="onEdit"
                                    >
                                        <img class="item-image-src" :src="imageFormat(photo.picThumb)" />
                                    </BusinessImg>
                                    <img v-if="pageType != 'overseas'" class="item-image-src" :src="imageFormat(photo.picThumb)" />
                                    <DialogForm type="gallery" class="" @okCallback="onEdit" :params="{ isMultiple: false }">
                                        <span class="change-image">{{ $t("更换图片") }}</span>
                                    </DialogForm>
                                </div>
                            </div>
                            <div class="item-info">
                                <div class="item-info-item item-info-title" v-if="showText">
                                    <span class="lable">{{ $t("标题") }}</span>
                                    <TigInput width="100%" :placeholder="$t('建议10个字以内，可不填')" v-model="photo.picTitle"></TigInput>
                                </div>
                                <div class="item-info-item item-info-title" v-if="showDesc">
                                    <span class="lable">{{ $t("描述") }}</span>
                                    <TigInput width="100%" :placeholder="$t('可不填')" v-model="photo.picDesc"></TigInput>
                                </div>
                                <div class="item-info-item item-info-link" v-if="showLink">
                                    <span class="lable">{{ $t("链接") }}</span>
                                    <div class="lyecs-link-select">
                                        <SelectLink v-model="photo.picLink" :decorateType="decorateType"></SelectLink>
                                    </div>
                                </div>
                                <div class="item-info-item item-info-color" v-if="showColor">
                                    <span class="lable">{{ $t("背景色") }}</span>
                                    <div class="lyecs-link-select">
                                        <div class="flex flex-align-center">
                                            <div><SelectColor v-model:color="photo.gradientColorA"></SelectColor></div>
                                            <div style="margin: 0 10px">-</div>
                                            <div><SelectColor v-model:color="photo.gradientColorB"></SelectColor></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="del-item" @click="removePhoto()"><i class="ico-decorate icon-dec-cha"></i></div>
                        </div>
                    </div>
                </div>
                <div class="dec-pic-add-con" v-if="isMultiple == true && showAddButton && (maxCount === 0 || (photos?.length ?? 0) < maxCount)">
                    <DialogForm v-if="showImage" type="gallery" class="" @okCallback="onAdd" :params="{ isMultiple: isMultiple }" style="width: 100%">
                        <a class="dec-pic-add-btn"><i class="ico-decorate icon-dec-jia"></i>{{ title }}</a>
                    </DialogForm>
                    <a v-else @click="onAdd" class="dec-pic-add-btn"><i class="ico-decorate icon-dec-jia"></i>{{ title }}</a>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import BusinessImg from "@/components/multilingual/BusinessImg.vue";
import { reactive, ref, toRefs, computed } from "vue";
import { SelectColor } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import draggable from "vuedraggable";
import { SelectLink } from "@/components/select";
import { imageFormat } from "@/utils/format";

const props = defineProps({
    isMultiple: {
        type: Boolean,
        default: false
    },
    showDesc: {
        type: Boolean,
        default: false
    },
    title: {
        type: String,
        default: "添加图片"
    },
    showText: {
        type: Boolean,
        default: true
    },
    showImage: {
        type: Boolean,
        default: true
    },
    showColor: {
        type: Boolean,
        default: false
    },
    showLink: {
        type: Boolean,
        default: true
    },
    showAddButton: {
        type: Boolean,
        default: true
    },
    showItemIndex: {
        type: Boolean,
        default: false
    },
    backgroundColor: {
        type: Array,
        default: () => []
    },
    decorateType: {
        type: String,
        default: ""
    },
    pageType: {
        type: String,
        default: ""
    },
    dataType: {
        type: Number,
        default: 0
    },
    maxCount: {
        type: Number,
        default: 0
    }
});
// 动态解析props
const photos = defineModel<any>("photos", { type: Array, default: [] });
const photo = defineModel<any>("photo", { type: Object, default: {} });
const { isMultiple } = toRefs(props);

const onEdit = (result: any, data: any) => {
    if (isMultiple.value) {
        Object.assign(photos.value[data.index], result[0]);
    } else {
        photo.value = result[0];
    }
};
const onAdd = (result: any) => {
    if (!props.showImage) {
        let obj = {
            picTitle: "",
            picDesc: "",
            picLink: {
                link: "",
                name: ""
            }
        };
        photos.value = [...photos.value, obj];
    } else if (props.showColor) {
        let obj = {
            picTitle: "",
            picDesc: "",
            gradientColorA: props.backgroundColor[0],
            gradientColorB: props.backgroundColor[1]
        };
        const newItems = result.map((item: any) => {
            return { ...item, ...obj };
        });
        photos.value = [...photos.value, ...newItems];
    } else {
        let obj = {
            picTitle: "",
            picDesc: ""
        };
        const newItems = result.map((item: any) => {
            return { ...item, ...obj };
        });
        photos.value = [...photos.value, ...newItems];

        console.log(photos.value, "photos.value");
    }
};
const removePic = (index: number) => {
    // 使用新数组触发 v-model setter。部分装修配置通过 computed v-model
    // 对数据做兼容归一化，直接 splice 只会修改 getter 返回的临时数组，
    // 不会真正写回配置，表现为点击删除无效。
    photos.value = photos.value.filter((_: any, itemIndex: number) => itemIndex !== index);
};
const removePhoto = () => {
    photo.value = {
        picId: "",
        picThumb: "",
        picTitle: "",
        picName: "",
        picDesc: "",
        picLink: {
            label: "",
            name: "",
            data: {
                name: "",
                path: ""
            }
        },
        gradientColorA: "",
        gradientColorB: ""
    };
};
</script>

<style lang="less" scoped>
.dec-pic-group-item-con.has-item-index {
    flex-wrap: wrap;
}

.dec-pic-group-item-title {
    flex: 0 0 100%;
    box-sizing: border-box;
    padding: 2px 12px 4px;
    color: #323233;
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
}

.pic-add-box {
    width: 60px;
    height: 60px;
    background: #fff;
    border: 1px solid #e0e0e0;
    // box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    a {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
        color: var(--tig-primary);
        font-size: 20px;
    }
}
</style>
