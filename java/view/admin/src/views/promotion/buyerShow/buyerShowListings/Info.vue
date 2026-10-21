<template>
    <div class="container" :class="{'container-detail': props.act === 'detail'}">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="rangeData" :label="$t('关联商品')" :rules="[{ required: true, message: '请选择关联商品!', trigger: 'change' }]">
                        <SelectProduct :act="props.act" v-model:ids="formState.rangeData" :isSelf="props.isSelf"></SelectProduct>
                    </el-form-item>
                    <el-form-item prop="buyerName" :label="$t('买家信息')" :rules="[{ required: true, message: '请选择买家信息!' }]">
                        <BuyerInfo v-model:formState="formState" :act="props.act"></BuyerInfo>
                    </el-form-item>
                    <el-form-item prop="content" :label="$t('文字内容')" :rules="[{ required: true, message: '请输入文字内容!' }]">
                        <div v-if="props.act === 'detail'">{{  formState.content  }}</div>
                        <TigInput style="width: 100%;"  v-else classType="tig-form-input" type="textarea" :placeholder="$t('请输入')" v-model="formState.content" maxlength="500" show-word-limit />
                    </el-form-item>
                    <el-form-item :label="$t('视频内容')" prop="productVideoInfo">
                        <div class="flex" v-if="props.act === 'detail'">
                            <div class="video-preview" v-if="formState.buyerShowVideoList?.length">
                                <video v-for="(item, index) in formState.buyerShowVideoList" :key="index" :src="imageFormat(item.videoUrl)" controls></video>
                            </div>
                            <div v-else>--</div>
                        </div>
                        <div class="flex-column" v-else>
                            <FormAddGalleryVideo v-if="!loading" v-model:video="formState.buyerShowVideoList" :disabled="examine == 1 || type !== ''"></FormAddGalleryVideo>
                            <div class="extra">{{ $t('文字内容选填，视频或图片内容至少上传一项') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('商品图片')" prop="imgList">
                        <div v-if="props.act === 'detail'">
                            <div class="imgList" v-if="formState.imgList?.length">
                                <Image style="width: 80px;height: 80px;" v-for="(element, index) in formState.imgList" :key="index" class="gallery-img" :big="element.picUrl" :src="imageFormat(element.picThumb)" />
                            </div>
                            <div v-else>--</div>
                        </div>
                        <div class="flex-column" v-else>
                            <FormAddGallery v-if="!loading" v-model:photos="formState.imgList" :maxCount="8" :disabled="examine == 1 || type !== ''" :isMultiple="true" pageType="product"></FormAddGallery>
                            <div class="extra">{{ $t('最多支持8张图片') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="groupData" :label="$t('所属分组')">
                        <div v-if="props.act === 'detail'" style="width: 100%;">
                            <div v-if="formState.groupData">
                                <div>{{ `共${formState.groupData.length}个分组` }}</div>
                                <div class="groupDataBox">
                                    <div class="groupName" v-for="(item, index) in buyerShowGroupList" :key="index">
                                        {{item}}
                                    </div>
                                </div>
                            </div>
                            <div v-else>--</div>
                        </div>
                        <div v-else style="width: 100%;">
                            <div v-if="formState.groupData?.length && buyerShowGroupList.length" style="width: 100%;">
                                <div class="flex">
                                    {{ `共${formState.groupData.length}个分组` }}
                                    <DialogForm
                                        isDrawer
                                        :title="$t('修改分组')"
                                        width="600px"
                                        path="promotion/buyerShow/buyerShowListings/groupInfo"
                                        @okCallback="okCallback"
                                        :params="{ groupData: formState.groupData }"
                                    >
                                        <a class="btn-link mr10 ml5 cursor-pointer">{{ $t('修改') }}</a>
                                    </DialogForm>
                                    <a class="btn-link cursor-pointer" @click="formState.groupData = []">{{ $t('清除') }}</a>
                                </div>
                                <div class="groupDataBox">
                                    <div class="groupName" v-for="(item, index) in buyerShowGroupList" :key="index">
                                        {{item}}
                                        <el-icon @click="delGroup(index)"><Close /></el-icon>
                                    </div>
                                </div>
                            </div>
                            <DialogForm
                                v-else
                                isDrawer
                                :title="$t('修改分组')"
                                width="600px"
                                path="promotion/buyerShow/buyerShowListings/groupInfo"
                                style="width: 100%"
                                @okCallback="okCallback"
                                :params="{ groupData: formState.groupData }"
                            >
                                <el-button>{{ $t('选择分组') }}</el-button>
                            </DialogForm>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <div v-if="props.act === 'detail'">{{  formState.sortOrder  }}</div>
                        <el-input-number
                             v-else
                            v-model="formState.sortOrder"
                            :min="0"
                            controls-position="right"
                        />
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted, watch } from "vue";
import { useRouter } from 'vue-router'
import { message } from "ant-design-vue";
import { buyerShowListFormState } from '@/types/promotion/buyerShowListings';
import { getBuyerShowDetail, updateBuyerShow } from "@/api/promotion/buyShow";
import { SelectProduct } from "@/components/select";
import { FormAddGallery, FormAddGalleryVideo } from "@/components/gallery";
import { DialogForm } from "@/components/dialog";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
import { Close } from "@element-plus/icons-vue";
import BuyerInfo from './src/BuyerInfo.vue'
import { imageFormat } from "@/utils/format";
import { Image } from "@/components/image";

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    //是否自营商品
    isSelf: {
        type: Number,
        default: 0
    },
    examine: {
        type: Number,
        default: 0
    },
    isDialog: {
        type: Boolean,
        default: true
    },
    loading: {
        type: Boolean,
        default: false
    },
    type: {
        type: String,
        default: ""
    },
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === 'add' ? 'create' : 'update';
const formRef = shallowRef();
const formState = ref<buyerShowListFormState>({
    sortOrder: 0
});

watch(() => props.act, (val) => {
    if(val == 'add') {
        formState.value = {
            sortOrder: 0,
            rangeData: []
        };
    }
}, {deep: true, immediate: true});
const buyerShowGroupList = ref<any>([]);

const okCallback = (result: any) => {
    formState.value.groupData = result.map((item: any) => item.buyerShowGroupId);
    buyerShowGroupList.value = result.map((item: any) => item.groupName);
};

const delGroup = (index: number) => {
    buyerShowGroupList.value.splice(index, 1);
    formState.value.groupData?.splice(index, 1);
}

const fetchLive = async () => {
    try {
        loading.value = false;
        const result: any = await getBuyerShowDetail({ buyerShowId: id.value });
        Object.assign(
            formState.value,
            {
                ...result,
                buyerShowVideoList: result.videoList,
                groupData: result.groupIdList
            }
        )
        buyerShowGroupList.value = result.groupNameList;
        loading.value = true;
    } catch (error: any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};


// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        const params: any = { buyerShowId: id.value, ...formState.value };
        if(operation === 'create') {
            delete params.buyerShowId;
        } else if(operation === 'update') {
            params.imgList.forEach((item: any) => {
                item.picUrl = item.picOriginal || item.picUrl;
            });
        }
        emit('update:confirmLoading', true);
        const result = await updateBuyerShow(operation, params);
        emit('submitCallback', result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit()
};

defineExpose({ onFormSubmit });

watch(()=>formState.value.rangeData, (val: any) => {
    val?.length > 0 ? formRef.value.clearValidate('rangeData') : formRef.value.validateField('rangeData');
}, {deep: true})

onMounted(() => {
    if (action.value === "detail" || action.value === "edit") {
        // 获取详情数据
        fetchLive();
    } else {
        loading.value = false;
    }
});
</script>

<style lang="less" scoped>

.groupDataBox{
    width: 100%;
    background: #f7f8fa;
    border-radius: 2px;
    padding: 15px;
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    align-items: center;
    gap: 10px;
    box-sizing: border-box;
    .groupName{
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        background-color: rgb(255, 255, 255);
        color: rgb(38, 38, 38);
        border-color: rgb(217, 217, 217);
        min-width: 140px;
        text-align: center;
        .el-icon{
            cursor: pointer;
            transform: translateY(3px);
        }
    }
}

.video-preview {
    width: 280px;
    height: 160px;
    //border: 1px solid #ddd;
    //padding: 5px;
    position: relative;
    video {
        width: 100%;
        height: 100%;
    }
}

.imgList{
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.container-detail{
    :deep(.el-form-item){
        margin-bottom: 30px;
    }
}
:deep(.gallery-pic) {
    .multilingual-icon {
        display: none;
    }
}

</style>
