/**
 * @file 赠品选择器组件
 * @description 用于营销活动中选择赠品的弹窗选择器
 * @features
 * - 弹窗选择赠品
 * - 支持单选和多选模式
 * - 已选赠品列表展示
 * - 支持设置赠品数量
 * - 显示赠品库存信息
 */
<template>
    <div class="lyecs-goods-select-group">
        <el-space>
            <DialogForm v-if="giftId == 0" isDrawer @okCallback="onOk" :title="$t('选择赠品')" width="700px" path="promotion/productGift/src/SelectGift" :params="{ selectedIds: gifts, isMultiple: props.isMultiple }">
                <a>{{ $t('选择赠品') }}</a>
            </DialogForm>
            <span v-if="isMultiple && gifts.length > 0" class="ml10">{{ $t('已选择') }} <b>{{ gifts.length }}</b> {{ $t('个赠品') }}</span>
            <el-button v-if="isMultiple && gifts.length > 0" @click="clear">{{ $t('清空') }}</el-button>
        </el-space>
        <div class="lyecs-goods-selected-con" v-if="isMultiple && gifts.length > 0">
            <div class="goods-selected-list">
                <div class="goods-selected-list-tr goods-selected-list-th">
                    <div class="col col1">{{ $t('赠品名称') }}</div>
                    <div class="col col2">{{ $t('商品信息') }}</div>
                    <div class="col col2">{{ $t('赠品库存') }}</div>
                    <div class="col col2">{{ $t('赠品数量') }}</div>
                    <div class="col col3">{{ $t('操作') }}</div>
                </div>
                <template v-for="(item, key) in gifts">
                    <div class="goods-selected-list-tr">
                        <div class="col col1">{{ item.giftName }}</div>
                        <div class="col col2 goods-info">
                            <img width="50" height="50" :src="imageFormat(item.productInfo?.picThumb)">
                            <span>{{ item.productInfo?.productName }}</span>
                        </div>
                        <div class="col col2">{{ item.giftStock }}</div>
                        <div class="col col2 goods-info">
                            <el-input-number size="small" v-model="item.num" :min="1" :max="item.giftStock" @change="changeNum(item)" />
                        </div>
                        <div class="col col3">
                            <a class="del-btn" @click="del(key)">{{ $t('删除') }}</a>
                        </div>
                    </div>
                </template>
            </div>
        </div>
        <DialogForm v-if="!isMultiple && giftName !== ''" isDrawer @okCallback="onOk" :title="$t('选择赠品')" width="600px" path="promotion/productGift/src/SelectGift" :params="{ selectedIds: gifts, isMultiple: props.isMultiple }">
            <div class="flex flex-align-center cursor-pointer">
                <!-- <div class="pic">
                    <img :src="imageFormat(productInfo?.picThumb)" style="width: 40px;">
                </div> -->
                <div class="gift_info">
                    <div class="giftName">{{ giftName }}</div>
                    <div class="giftStock">{{ $t('库存:{giftStock}', { giftStock }) }}</div>
                </div>
            </div>
        </DialogForm>
    </div>
</template>
<script setup lang="ts">
import { ref, toRefs, onMounted, reactive } from "vue"
import { DialogForm } from '@/components/dialog'
import { message } from 'ant-design-vue'
import { ProductGiftFilterState } from '@/types/promotion/productGift.d';
import { imageFormat } from "@/utils/format";
interface GiftsForm {
    giftName?: string,
    giftId?: number,
    num?: number
}
// 传值
const props = defineProps({
    // 单选还是多选
    isMultiple: {
        type: Boolean,
        default: true
    }
})
const gifts = defineModel<ProductGiftFilterState[]>("gifts", { type: Array, default: [] });
const giftId = defineModel<number>("giftId", { type: Number, default: 0 });
const giftStock = defineModel<number>("giftStock", { type: Number, default: 0 });
const giftName = defineModel<string>("giftName", { type: String, default: '' });
const productInfo = ref<any>({})
const emit = defineEmits(['update:gifts'])
const ids = ref<any>([])
onMounted(() => {
    // emit('update:gifts', gifts.value)
});
// 商品列表
const total = ref(0);
const onOk = (e:any) => {
    if(props.isMultiple){
        gifts.value = []
        e.map((item:any)=> {
            item.num = 1;
            gifts.value.push(item)
        })
        total.value = gifts.value.length
    }else{
        gifts.value = e
        giftId.value = e[0].giftId
        giftName.value = e[0].giftName
        giftStock.value = e[0].giftStock
        productInfo.value = e[0].productInfo
    }
}
// 清空
const clear = () => {
    gifts.value = [];
    ids.value = [];
}
// 删除
const del = (key: any) => {
    gifts.value.splice(<any>key, 1)
    ids.value.splice(<any>key, 1)
}
// 修改数量
const changeNum = (item:any) => {
    gifts.value.map(info => {
        if (info.giftId === item.giftId) {
            info.num = item.num
        }
    })
}

</script>

<style lang="less" scoped>
.all_brand {
    padding: 8px;
}

.all_brand ul {
    display: flex;
    flex-wrap: wrap;
    width: 290px;
}

.all_brand li a {
    padding: 2px 7px;
    display: inline-block;
    border-radius: 3px;
    line-height: 20px;
}

.all_brand li a:hover {
    background: #f4f4f4;
}

/*商品选择器*/
.lyecs-goods-select-group {
    margin-bottom: 0;
    width: 100%;
}

.lyecs-goods-select-group .lyecs-goods-selected-con {
    max-width: 710px;
    position: relative;
    padding-top: 50px;
    margin-top: 10px;
}

.lyecs-goods-select-group .clear-btn {
    margin-left: 20px;
}

.lyecs-goods-select-group .desc {
    margin-left: 20px;
    color: #999;
}

.lyecs-goods-select-group .goods-selected-list {
    margin-bottom: 20px;
    max-height: 550px;
    overflow-y: auto;
    min-width: 400px
}

.lyecs-goods-select-group .goods-selected-list-tr {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    border-bottom: 1px solid #f0f0f0;
}

.lyecs-goods-select-group .goods-selected-list-th {
    color: #333;
    font-weight: bold;
    position: absolute;
    top: 0;
    width: 100%;
    background: #fff;
    height: 50px;
}

.lyecs-goods-select-group .goods-selected-list-tr .col {
    padding: 10px;
}


.lyecs-goods-select-group .goods-selected-list-tr .col1 {
    width: 100px;
}

.lyecs-goods-select-group .goods-selected-list-tr .col2 {
    flex: 1;
}

.lyecs-goods-select-group .goods-selected-list-tr .col3 {
    width: 80px;
}

.lyecs-goods-select-group .goods-selected-list-tr .goods-info {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}

.lyecs-goods-select-group .goods-selected-list-tr .goods-info img {
    margin-right: 10px;
}
.gift_info{
    margin-left: 5px;
    font-size: 12px;
    color: #999;
}
</style>
