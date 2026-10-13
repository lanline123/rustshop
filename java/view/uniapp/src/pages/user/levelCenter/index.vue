<template>
    <tig-layout :title="$t('会员中心')">
        <view class="level_container">
            <topcard :list="cardList" :user-level="userLevel" />
            <div class="bottom_wapper" :class="{ task_mode: userInfo.rankType === 2 }">
                <!-- 任务卡片 -->
                <taskcard v-if="userInfo.rankType === 2" />
                <!-- <taskcard></taskcard> -->
                <!-- 权益说明 -->
                <leveldesc :rank-config="rankConfig" :list="allList" />
            </div>
        </view>
        <recommend />
        <tig-copyright />
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 会员等级中心
 * - 展示当前会员等级及升级进度
 * - rankType=2 时显示任务升级模式（完成任务获得成长值）
 * - 展示各等级权益说明
 */
import { reactive, ref } from "vue";
import { getUserLevelList } from "@/api/user/levelCenter";
import { useConfigStore } from "@/store/config";
import recommend from "@/components/recommend/index.vue";
import topcard from "./src/topCard.vue";
import taskcard from "./src/taskCard.vue";
import leveldesc from "./src/levelDesc.vue";
import { useUserStore } from "@/store/user";
const { userInfo } = useUserStore();

const cardList = reactive([]);
const allList = reactive([]);
const userLevel = reactive({
    rankLevel: "1"
});
const rankConfig = ref({});

const configStore = useConfigStore();

const __getUserLevelList = async () => {
    try {
        let result = await getUserLevelList();
        // console.log(result);
        // 过滤出大于等于当前等级会员列表
        let list = result?.item?.filter((item) => {
            return parseInt(item?.rankLevel) >= parseInt(userInfo.rankLevel);
        });
        rankConfig.value = result.rankConfig;
        Object.assign(cardList, list);
        Object.assign(allList, result?.item);
    } catch (error) {}
};

__getUserLevelList();
</script>
<style scoped lang="scss">
page {
    height: 100%;
}

.level_container {
    display: flex;
    flex-direction: column;
    height: 100%;
    // background-image: url(../../../static/images/user/vip_bg.png);
    background-image: url(https://oss.tigshop.com/img/gallery/202502/1739845582HFfsl3dr4o7dvrmgL7.png);
    background-repeat: no-repeat;
    background-size: 100% auto;
    .bottom_wapper {
        flex: 1;
        padding: 24rpx;
        background: #f8f8f8;
        &.task_mode {
            border-radius: 40rpx 40rpx 0 0;
        }
    }
}
.shop-company {
    color: #999;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx 0;
    font-size: 24rpx;
}
</style>
