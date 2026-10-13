<!--随机获取头像和名称-->
<template>
    <div v-if="props.act === 'detail'" style="width: 100%;box-sizing: border-box">
        <div class="limitUserRankBox detail" v-if="formState.buyerPhoto || formState.buyerName">
            <Image style="width: 80px;height: 80px;" :big="formState.buyerPhoto" :src="imageFormat(formState.buyerPhoto)" />
            <div>{{ formState.buyerName }}</div>
        </div>
        <div v-else>--</div>
    </div>
    <div class="limitUserRankBox" v-else>
        <FormAddGallery v-if="!loading" v-model:photo="formState.buyerPhoto"></FormAddGallery>
        <TigInput  v-model="formState.buyerName" />
        <a class="btn-link no-select" @click="getBuyerInfoData">{{ $t('更换') }}</a>
    </div>
</template>

<script setup lang="ts">
import { FormAddGallery } from "@/components/gallery";
import { buyerShowListFormState } from "@/types/promotion/buyerShowListings";
const formState = defineModel<buyerShowListFormState>("formState", { default: {} });
import { message } from "ant-design-vue";
import { debounce } from '@/utils/util'
import { getBuyerInfo } from "@/api/promotion/buyShow";
import { defineProps, watch, ref } from "vue";
import { imageFormat } from "@/utils/format";
import { Image } from "@/components/image";

const props = defineProps({
    act: {
        type: String,
        default: ""
    }
});

const loading = ref(false);


// 使用防抖的获取买家信息函数
const getBuyerInfoData = debounce(async () => {
    try {
        const result = await getBuyerInfo({});
        formState.value = {
            ...formState.value,
            ...result
        }
    } catch (error: any) {
        message.error(error.message);
    }
}, 300); // 300ms 防抖延迟

watch(()=>props.act, (newVal) => {
    if(newVal == 'add') {
        getBuyerInfoData();
    }
}, { immediate: true, deep: true })

</script>

<style scoped lang="less">
.limitUserRankBox{
    //flex: 1;
    display: flex;
    gap: 16px;
    border-radius: 2px;
    padding: 10px;
    align-items: center;
    background: #f5f7fa;
    width: 100%;
    box-sizing: border-box;
    //padding: 20px;
}
.detail{
    border: none;
}
/deep/ .gallery-pic{
    width: 100px;
}
/deep/ .add-photo-btn, .have_image{
    border-radius: 50%;
}

.no-select {
    user-select: none; /* 标准语法 */
    -webkit-user-select: none; /* Safari */
    -moz-user-select: none; /* Firefox */
    -ms-user-select: none; /* IE10+/Edge */
    cursor: pointer;
}
</style>
