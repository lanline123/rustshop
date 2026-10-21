<template>
    <div class="container">
        <div class="content_wrapper decoratePgaeWrap">
            <div class="flex box-wrapper">
                <div class="left-wrapper">
                    <div class="left-box tab-name">
                        <div
                            @click="checkPage('investmentPromotionHomepage')"
                            :class="'left-cont tab-item ' + (type == 'investmentPromotionHomepage' ? 'current' : '')"
                        > {{ $t('招商首页') }} </div>
                        <div @click="checkPage('investmentPlan')" :class="'left-cont tab-item ' + (type == 'investmentPlan' ? 'current' : '')">{{ $t('招商计划') }}</div>
                        <div @click="checkPage('investmentCriteria')" :class="'left-cont tab-item ' + (type == 'investmentCriteria' ? 'current' : '')"> {{ $t('招商标准') }} </div>
                        <div @click="checkPage('settlingProcess')" :class="'left-cont tab-item ' + (type == 'settlingProcess' ? 'current' : '')"> {{ $t('入驻流程') }} </div>
                    </div>
                </div>
                <div v-if="!loading" class="bg-layout">
                    <template v-if="type == 'investmentPromotionHomepage'">
                        <div class="left-box" style="background-color: #ffffff">
                            <HomePage :module="module1"></HomePage>
                        </div>
                        <div class="right-box">
                            <div v-for="(item, index) in module1" class="message-item">
                                <div class="title">
                                    {{ item.title }}
                                </div>
                                <DialogForm
                                    :params="{ module: module1, decorateSn: 'investmentPromotionHomepage', index: index }"
                                    isDrawer
                                    path="adminMerchant/investment/src/InvestmentInfo"
                                    :title="$t('编辑描述')"
                                    width="600px"
                                >
                                    <el-button size="small" type="primary">{{ $t('编辑') }}</el-button>
                                </DialogForm>
                            </div>
                        </div>
                    </template>
                    <template v-if="type == 'investmentPlan'">
                        <div class="left-box">
                            <Plan :module="module2"></Plan>
                        </div>
                        <div class="right-box">
                            <div v-for="(item, index) in module2" class="message-item">
                                <div class="title">
                                    {{ item.title }}
                                </div>
                                <DialogForm
                                    :params="{ module: module2, decorateSn: 'investmentPlan', index: index }"
                                    isDrawer
                                    path="adminMerchant/investment/src/InvestmentInfo"
                                    :title="$t('编辑描述')"
                                    width="600px"
                                >
                                    <el-button size="small" type="primary">{{ $t('编辑') }}</el-button>
                                </DialogForm>
                            </div>
                        </div>
                    </template>
                    <template v-if="type == 'investmentCriteria'">
                        <div class="left-box">
                            <Standard :module="module3"></Standard>
                        </div>
                        <div class="right-box">
                            <div v-for="(item, index) in module3" class="message-item">
                                <div class="title">
                                    {{ item.title }}
                                </div>
                                <DialogForm
                                    :params="{ module: module3, decorateSn: 'investmentCriteria', index: index }"
                                    isDrawer
                                    path="adminMerchant/investment/src/InvestmentInfo"
                                    :title="$t('编辑描述')"
                                    width="600px"
                                >
                                    <el-button size="small" type="primary">{{ $t('编辑') }}</el-button>
                                </DialogForm>
                            </div>
                        </div>
                    </template>
                    <template v-if="type == 'settlingProcess'">
                        <div class="left-box" style="background-color: #ffffff">
                            <SettlingProcess :module="module4"></SettlingProcess>
                        </div>
                        <div class="right-box">
                            <div v-for="(item, index) in module4" class="message-item">
                                <div class="title">
                                    {{ item.title }}
                                </div>
                                <DialogForm
                                    :params="{ module: module4, decorateSn: 'settlingProcess', index: index }"
                                    isDrawer
                                    path="adminMerchant/investment/src/InvestmentInfo"
                                    :title="$t('编辑描述')"
                                    width="600px"
                                >
                                    <el-button size="small" type="primary">{{ $t('编辑') }}</el-button>
                                </DialogForm>
                            </div>
                        </div>
                    </template>
                </div>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/views/decorate/decorate/src/css/decorate.less";
import { ref, onMounted } from "vue";
import { getDecorateDiscrete, updateDecorateDiscrete } from "@/api/decorate/decorateDiscrete";
import { message } from "ant-design-vue";
import SettlingProcess from "@/views/adminMerchant/investment/src/SettlingProcess.vue";
import { DialogForm } from "@/components/dialog";
import Standard from "@/views/adminMerchant/investment/src/Standard.vue";
import Plan from "@/views/adminMerchant/investment/src/Plan.vue";
import HomePage from "@/views/adminMerchant/investment/src/HomePage.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const type = ref("investmentPromotionHomepage");
const loading = ref(true);
const module1: any = ref([
    {
        title: t('招商Banner设置'),
        images: [],
        content: ""
    },
    {
        title: t('平台模式流程图设置'),
        content: "",
        image: ""
    },
    {
        title: t('自营模式流程图设置'),
        content: "",
        image: ""
    }
]);
const module2: any = ref([
    {
        title: "",
        content: ""
    }
]);
const module3: any = ref([
    {
        title: "",
        content: ""
    },
    {
        title: "",
        content: ""
    },
    {
        title: "",
        content: ""
    }
]);
const module4: any = ref([
    {
        size: "1",
        align: "1",
        title: "",
        content: ""
    },
    {
        size: "0",
        align: "2",
        title: "",
        content: ""
    },
    {
        size: "0",
        align: "1",
        title: "",
        content: ""
    },
    {
        size: "2",
        align: "2",
        title: "",
        content: ""
    },
    {
        size: "4",
        align: "1",
        title: "",
        content: ""
    },
    {
        size: "3",
        align: "1",
        title: "",
        content: ""
    },
    {
        size: "2",
        align: "2",
        title: "",
        content: ""
    },
    {
        size: "5",
        align: "1",
        title: "",
        content: ""
    },
    {
        size: "0",
        align: "2",
        title: "",
        content: ""
    }
]);
// 表单通过验证后提交
onMounted(() => {
    // 获取详情数据
    fetchDecorateDiscrete();
});
const checkPage = (val: string) => {
    type.value = val;
    fetchDecorateDiscrete();
};
const fetchDecorateDiscrete = async () => {
    try {
        loading.value = true;
        const result = await getDecorateDiscrete({
            decorateSn: type.value
        });
        if (result) {
            if (type.value == "investmentPromotionHomepage") {
                Object.assign(module1.value, result?.data);
            }
            if (type.value == "investmentPlan") {
                Object.assign(module2.value, result?.data);
            }
            if (type.value == "investmentCriteria") {
                Object.assign(module3.value, result?.data);
            }
            if (type.value == "settlingProcess") {
                let temp = (result?.data)?result?.data:[]
                for (let i = 0; i < temp.length; i++) {
                    module4.value[i].title = temp[i].title;
                    module4.value[i].content = temp[i].content;
                }
                // Object.assign(module4.value, result?.data);
            }
        } else {
            Object.assign(module1.value, []);
            Object.assign(module2.value, []);
            Object.assign(module3.value, []);
            Object.assign(module4.value, []);
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
</script>
<style lang="less" scoped>
.bg-layout {
    display: flex;
    flex-direction: row;
    gap: 20px;

    flex: 1 1 0;
    min-width: 0;
    .left-box {
        flex: 1 1 auto;
        min-width: 0;
        overflow-y: auto;
        background-color: #f5f5f5;
        &::-webkit-scrollbar {
            display: none;
        }
    }

    .right-box {
        display: flex;
        flex-direction: column;
        flex-shrink: 1;
        width: auto;
        max-width: 320px;
        flex: 0 0 320px;
        min-width: 0;
        .message-item {
            display: flex;
            padding-bottom: 20px;
            box-sizing: border-box;
            align-items: center;
            justify-content: space-between;
            gap: 10px;
            border-bottom: 1px solid #eee;
            margin-bottom: 30px;
            min-width: 0;

            .title {
                flex: 1 1 auto;
                min-width: 0;
                font-weight: 700;
                font-size: 14px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
        }
    }
}

@media only screen and (max-width: 767px) {
    .decoratePgaeWrap .box-wrapper .left-box .left-cont {
        padding: 20px 0;
    }
    .bg-layout {
        flex-direction: column-reverse;
        .left-box {
            width: 80%;
        }
    }
}
</style>
