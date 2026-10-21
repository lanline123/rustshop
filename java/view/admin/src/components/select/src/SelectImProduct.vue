/**
 * @file IM商品选择器组件
 * @description 用于客服聊天场景快速发送商品的选择器
 * @features
 * - 商品搜索和列表展示
 * - 最多选择3个商品同时发送
 * - 勾选/取消选择商品
 * - 已选商品预览
 * - 分页加载商品列表
 * - 直接发送商品消息到聊天
 */
<template>
    <div class="goods-search-container">
        <div class="goods-toolbar">
            <el-space>
                <TigInput width="190px" v-model="filterParams.keyword" :placeholder="$t('商品名称/货号')" @keyup.enter="loadFilter" />
                <el-button @click="loadFilter">{{ $t('搜索') }}</el-button>
            </el-space>
        </div>
        <div  class="goods-recommend-list">

            <template v-if="!loading">
                <template v-if="total>0">
                    <div v-for="item in filterState" :key="item.productId" class="product-item">
                        <div class="check-box">
                            <el-checkbox
                                v-model="item.check"
                                @change="onCheckboxChange(item)">
                            </el-checkbox>
                        </div>
                        <div class="image-box" @click="selectItem(item)">
                            <el-image fit="cover" class="image" :src="imageFormat(item.picThumb)"></el-image>
                        </div>
                        <div class="info-box" @click="selectItem(item)">
                            <div class="name">{{ item.productName }}</div>
                            <div class="price">{{ priceFormat(item.productPrice) }}</div>
                        </div>
                    </div>
                </template>
                <template v-else>
                    <el-empty :description="$t('暂无商品～')" />
                </template>
            </template>
            <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
        </div>
        <div class="goods-choosed-box">
            <div v-if="total > 0" class="page">
                <Pagination
                    v-model:page="filterParams.page"
                    v-model:size="filterParams.size"
                    :total="total"
                    @callback="loadFilter"
                    layout="slot, prev, next"
                    :background="false"
                />
            </div>
            <div class="bot">
                <div v-if="likeList.length === 0" class="goods-choosed-box-left">
                    <div>{{ $t('尚未选择商品') }}</div>
                </div>
                <div v-else class="goods-choosed-box-left-have">
                    <div>{{ $t('已选择') }}</div>
                    <div class="image-list" v-for="(item, index) in likeList" :key="item.productId">
                        <div class="im">
                            <el-image fit="cover" class="lg-image" :src="imageFormat(item.picThumb)"></el-image>
                            <div class="del-bb" @click.stop="removeFromLikeList(index)"></div>
                        </div>
                    </div>
                </div>
                <div>
                    <el-button @click="updateTo" :disabled="likeList.length === 0" type="primary">{{ $t('确定') }}</el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { onMounted, reactive, ref, inject } from "vue";
import { message } from "ant-design-vue";
import { imageFormat, priceFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { ProductFilterParams, ProductFilterState } from "@/types/product/product";
import { useConfigStore } from "@/store/config";
import { getProductList } from "@/api/product/product";
import { sendMessage } from "@/api/im/im";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    messageInfo: {
        type: Object,
        default: {}
    }
});
const loading = ref(true);
const total = ref<number>(0);
const config = useConfigStore();
const filterState = ref<ProductFilterState[]>([]);
const filterParams = reactive<ProductFilterParams>({
    page: 1,
    size: (config as any).get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: ""
});
const linkSelectData = defineModel("linkSelectData", { type: Array, default: [] });

const likeList = ref<ProductFilterState[]>([]);

const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getProductList({ ...filterParams });
        filterState.value = result.records;

        // 初始化每个商品的 check 状态
        filterState.value.forEach((item) => {
            // 判断该商品是否在 likeList 中
            item.check = likeList.value.some(likeItem => likeItem.productId === item.productId);
        });

        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const selectItem = (item: ProductFilterState) => {
    const index = likeList.value.findIndex(i => i.productId === item.productId);

    if (index !== -1) {
        likeList.value.splice(index, 1);
        item.check = false;
    } else if (likeList.value.length < 3) {
        likeList.value.push(item);
        item.check = true;
    } else {
        message.error(t('一次最多发送三个商品'));
    }
};

const onCheckboxChange = (item: ProductFilterState) => {
    if (item.check) {
        selectItem(item);
    } else {
        const index = likeList.value.findIndex(i => i.productId === item.productId);
        if (index !== -1) {
            likeList.value.splice(index, 1);
        }
    }
};

const removeFromLikeList = (index: number) => {
    const removedItem = likeList.value.splice(index, 1)[0];
    const filterIndex = filterState.value.findIndex(item => item.productId === removedItem.productId);
    if (filterIndex !== -1) {
        filterState.value[filterIndex].check = false;
    }
};

const updateTo = () => {
    sendSelectedItems();
};

// const emits = defineEmits(['selectedItems']);

const sendSelectedItems = () => {
    // emits('selectedItems', likeList.value);
    likeList.value.forEach((item: any) => {
        senMsg(item);
    });
};
const receiveValueFromGrandChild: any = inject("receiveValueFromGrandChild");
const emit = defineEmits(["sendMessage"]);
const senMsg = async (item:any) => {
    try {
        let info = {};
        Object.assign(info, props.messageInfo, {
            content: {
                messageType: "custom",
                product: item || ""
            }
        });
        const result = await sendMessage(info);
        emit("sendMessage", result);
        receiveValueFromGrandChild(result);
        likeList.value = [];
        filterState.value.forEach(item => item.check = false);
    } catch (error: any) {
        console.log(error);
    } finally {
    }
};

onMounted(() => {
    loadFilter();
});
</script>
<style scoped lang="less">

.goods-search-container {
    padding-top: 10px;
    box-sizing: border-box;
    min-width: 276px;
    max-width: 276px;
    height: calc(100vh - 50px);
    display: flex;
    flex-direction: column;


    .goods-toolbar {
        display: flex;
        padding: 0 12px 6px;
        height: 40px;
        box-sizing: border-box;
    }

    .goods-recommend-list {
        flex: 1;
        box-sizing: border-box;
        overflow-y: auto;

        display: flex;
        flex-direction: column;

        .product-item {
            display: flex;
            cursor: pointer;
            flex-direction: row;
            gap: 12px;
            width: 100%;
            border-bottom: 1px solid #e5e5e5;
            align-items: center;
            box-sizing: border-box;
            padding: 12px 0;

            .check-box {
                width: 30px;
                display: flex;
                align-items: center;
                justify-content: center;

            }

            .image-box {
                min-width: 63px;
                min-height: 63px;
                max-width: 63px;
                max-height: 63px;
                border: 1px solid #f5f5f5;


                .image {
                    width: 100%;
                    height: 100%;
                }
            }

            .info-box {
                height: 64px;
                flex-direction: column;
                display: flex;
                justify-content: space-between;
                flex-grow: 1; /* 占据剩余空间 */
                font-size: 12px;

                .name {
                    line-height: 1.2; /* 调整行高以适应三行的高度 */
                    max-width: 140px;
                    display: -webkit-box; /* 使用弹性盒子布局 */
                    -webkit-line-clamp: 3; /* 设置显示的行数为3 */
                    -webkit-box-orient: vertical; /* 设置垂直方向的弹性盒子 */
                    overflow: hidden;
                    text-overflow: ellipsis;
                }


                .price {
                    color: #f00;
                    margin-bottom: 4px;
                }
            }
        }

    }

    .goods-choosed-box {
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-top: 1px solid #e5e5e5;
        flex-direction: column;

        .page {
            height: 40px;
            display: flex;
            justify-content: center;
            align-items: center;

        }

        .bot {
            width: 100%;
            display: flex;
            border-top: 1px solid #e5e5e5;
            line-height: 56px;
            box-sizing: border-box;
            min-height: 56px;
            padding: 0 12px;
            justify-content: space-between;

            .goods-choosed-box-left {
                display: flex;
                align-items: center;
                font-size: 12px;
                color: #999;
            }

            .goods-choosed-box-left-have {
                display: flex;
                align-items: center;
                font-size: 12px;
                gap: 8px;

                .image-list {
                    display: flex;
                    align-items: center;

                    .im:hover .del-bb {
                        visibility: visible;
                    }

                    .im {
                        position: relative;
                        min-width: 32px;
                        min-height: 32px;
                        max-width: 32px;
                        max-height: 32px;

                        .lg-image {
                            width: 100%;
                            height: 100%;
                            cursor: pointer;
                        }

                        .del-bb {
                            position: absolute;
                            top: -6px;
                            right: -6px;
                            width: 12px;
                            height: 12px;
                            background-color: black;
                            color: white;
                            border-radius: 50%;
                            text-align: start;
                            font-size: 10px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            cursor: pointer;
                            visibility: hidden;

                        }

                        .del-bb:before {
                            content: "×";
                        }
                    }


                }

            }

        }
    }
}

</style>
