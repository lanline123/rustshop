/**
 * @file 专业版会员等级信息表单
 * @description 用于配置专业版会员等级体系的详细表单，包括模式切换、等级方案、成长值设置和保级规则。
 * @features
 * - 模式切换：成长值会员模式 或 消费会员模式
 * - 等级方案管理：支持最多10级会员等级配置
 * - 会员图标和卡面自定义
 * - 升级条件设置（成长值或消费金额）
 * - 会员权益配置（折扣、多倍积分、包邮、自定义权益）
 * - 成长值获取规则设置（完成下单、完善信息、绑定手机号）
 * - 保级规则配置（永久有效或周期性重新计算）
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <div class="title">{{ $t('模式切换') }}</div>
                <div class="mb20">
                    <RadioType
                        v-if="!loading"
                        width="250px"
                        v-model:modelValue="formState.rankType"
                        :radioList="[
                            {
                                key: 1,
                                title: '成长值会员',
                                tips: '根据用户的下单积累的成长值，自动升级到不同等级'
                            },
                            {
                                key: 2,
                                title: '消费会员',
                                tips: '根据用户的消费金额，自动升级到不同等级'
                            }
                        ]"
                    >
                    </RadioType>
                </div>

                <div class="title">{{ $t('等级方案') }}</div>
                <el-form ref="formRef" :rules="rules" :model="formState" label-width="auto">
                    <el-form-item prop="data" style="margin-bottom: 0">
                        <div class="flex mb10">
                            <div class="flex">
                                <el-button type="primary" :icon="Plus" link @click="addPreferential" :disabled="formState.data.length >= 10">
                                    {{ formState.data.length >= 10 ? $t('多添加10级') : `添加等级(${formState.data.length}/10)` }}
                                </el-button>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="data">
                        <el-table :data="formState.data" :loading="loading" row-key="userId">
                            <el-table-column :width="80" :label="$t('等级')" prop="level">
                                <template #default="{ row, $index }">
                                    <div class="level_txt">vip{{ row.rankLevel }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="88" :label="$t('会员图标')" prop="rankLogo" align="center">
                                <template #default="{ row, $index }">
                                    <el-tooltip effect="light" placement="bottom" :show-after="150">
                                        <template #content> {{ $t('点击修改') }} </template>
                                        <div>
                                            <FormAddGallery v-model:photo="row.rankLogo" :type="2">
                                                <el-image style="height: 20px" :src="row.rankLogo" />
                                            </FormAddGallery>
                                        </div>
                                    </el-tooltip>
                                </template>
                            </el-table-column>
                            <el-table-column :width="100" :label="$t('会员卡面')" prop="card">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'edit', data: row }"
                                        :maskClosable="false"
                                        :isDrawer="false"
                                        path="user/levelManagePro/src/SetCardType"
                                        :title="$t('会员卡面')"
                                        width="850px"
                                        @okCallback="changeCardType"
                                    >
                                        <div
                                            v-if="row.rankBg !== '' && row.rankCardType == 2"
                                            class="card_item"
                                            :style="{ backgroundImage: 'url(' + row.rankBg + ')' }"
                                        ></div>
                                        <div v-else class="card_item" :class="row.rankIco"></div>
                                    </DialogForm>
                                </template>
                            </el-table-column>
                            <el-table-column :width="200" :label="$t('会员名称*')" prop="rankName">
                                <template #default="{ row, $index }">
                                    <el-form-item :prop="'data.' + $index + '.rankName'" :rules="rules.rankName">
                                        <div class="flex flex-align-center">
                                            <TigInput v-model="row.rankName" style="width: 100px"></TigInput>
                                        </div>
                                    </el-form-item>
                                </template>
                            </el-table-column>
                            <el-table-column :width="210" :label="$t('升级条件{rankType1}*', { rankType1: formState.rankType == 1 ? '(成长值)' : '' })" prop="minGrowthPoints">
                                <template #default="{ row, $index }">
                                    <div v-if="$index === 0" class="flex flex-align-center flex-justify-between">
                                        <div class="info">
                                            <div v-if="row.minGrowthPoints === 0">{{ $t('无门槛') }}</div>
                                            <el-form-item v-else :prop="'data.' + $index + '.minGrowthPoints'" :rules="rules.minGrowthPoints">
                                                <div class="flex flex-align-center" v-if="formState.rankType == 1">
                                                    <TigInput type="integer" v-model="row.minGrowthPoints"  width="100px"></TigInput>&nbsp;{{ $t('成长值') }}
                                                </div>
                                                <div class="flex flex-align-center" v-if="formState.rankType == 2">
                                                    {{ $t('消费满') }}&nbsp;<TigInput type="decimal" v-model="row.minGrowthPoints"  width="100px"></TigInput>&nbsp;{{ config.config.amountUnit }}
                                                </div>
                                            </el-form-item>
                                        </div>
                                        <el-dropdown>
                                            <span class="el-dropdown-link">
                                                <el-icon class="icon-info" color="#333" size="14"><ArrowDown /></el-icon>
                                            </span>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <el-dropdown-item
                                                        @click="
                                                            () => {
                                                                row.minGrowthPoints = 0;
                                                            }
                                                        "
                                                        >{{ $t('无门槛') }}</el-dropdown-item
                                                    >
                                                    <el-dropdown-item
                                                        @click="
                                                            () => {
                                                                row.minGrowthPoints = '';
                                                            }
                                                        "
                                                        >{{ formState.rankType == 1 ? $t('达到x成长值') : $t('消费x元') }}</el-dropdown-item
                                                    >
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
                                    </div>
                                    <div v-else class="flex flex-align-center flex-justify-between">
                                        <div class="info">
                                            <el-form-item :prop="'data.' + $index + '.minGrowthPoints'" :rules="rules.minGrowthPoints">
                                                <div class="flex flex-align-center" v-if="formState.rankType == 1">
                                                    <TigInput type="integer" v-model="row.minGrowthPoints" width="100px"></TigInput>&nbsp;{{ $t('成长值') }}
                                                </div>
                                                <div class="flex flex-align-center" v-if="formState.rankType == 2">
                                                    {{ $t('消费满') }}&nbsp;<TigInput type="decimal" v-model="row.minGrowthPoints" width="100px"></TigInput>&nbsp;{{ config.config.amountUnit }}
                                                </div>
                                            </el-form-item>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column prop="rights" align="center">
                                <template #header>
                                    <div class="discount_cell">
                                        <div>{{ $t('会员权益') }}</div>
                                        <el-tooltip effect="light" placement="bottom" :show-after="150">
                                            <template #content> {{ $t('添加权益') }} </template>
                                            <div class="icon-info">
                                                <DialogForm
                                                    :params="{ act: 'add' }"
                                                    :maskClosable="false"
                                                    :isDrawer="false"
                                                    path="user/levelManagePro/src/AddRights"
                                                    :title="$t('自定义权益')"
                                                    width="450px"
                                                    @okCallback="addRights"
                                                >
                                                    <el-icon color="#333" size="14"><Plus /></el-icon>
                                                </DialogForm>
                                            </div>
                                        </el-tooltip>
                                    </div>
                                </template>
                                <template #default="{ row, $index }">
                                    <el-table-column prop="discount" :label="$t('折扣')" :width="120">
                                        <template #default="{ row, $index }">
                                            <el-form-item :prop="'data.' + $index + '.discount'" :rules="rules.discount">
                                                <div class="table-col">
                                                    <div class="show" v-if="row.discount > 0">{{ $t('{discount}折', { discount: row.discount }) }}</div>
                                                    <div class="hide">
                                                        <TigInput type="decimal" v-model="row.discount" width="90px">
                                                            <template #append>{{ $t('折') }}</template>
                                                        </TigInput>
                                                    </div>
                                                </div>
                                            </el-form-item>
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="rankPoint" :label="$t('多倍积分')" :width="120">
                                        <template #default="{ row, $index }">
                                            <el-form-item :prop="'data.' + $index + '.rankPoint'" :rules="rules.rankPoint">
                                                <div class="table-col">
                                                    <div class="show" v-if="row.rankPoint > 0">{{ $t('{rankPoint}倍', { rankPoint: row.rankPoint }) }}</div>
                                                    <div class="hide">
                                                        <TigInput type="decimal" v-model="row.rankPoint" width="90px" placeholder="1-50">
                                                            <template #append>{{ $t('倍') }}</template>
                                                        </TigInput>
                                                    </div>
                                                </div>
                                            </el-form-item>
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="freeShipping" :label="$t('包邮')">
                                        <template #default="{ row, $index }">
                                            <el-checkbox v-model="row.freeShipping" :true-value="1" :false-value="0" />
                                        </template>
                                    </el-table-column>
                                    <el-table-column v-for="(item, index) in formState.data[0].rights" :label="item.name" :key="index" :width="120">
                                        <template #header>
                                            <div class="flex flex-justify-between">
                                                <div>{{ item.name }}</div>
                                                <el-tooltip effect="light" placement="bottom" show-after="150">
                                                    <template #content> {{ $t('编辑权益') }} </template>
                                                    <DialogForm
                                                        :params="{ act: 'edit', data: item, rowIndex:index }"
                                                        :maskClosable="false"
                                                        :isDrawer="false"
                                                        path="user/levelManagePro/src/AddRights"
                                                        :title="$t('自定义权益')"
                                                        width="450px"
                                                        @okCallback="addRights"
                                                    >
                                                        <el-icon color="#666" size="14" style="margin-top: 5px"><Edit /></el-icon>
                                                    </DialogForm>
                                                </el-tooltip>
                                                <el-tooltip effect="light" placement="bottom" show-after="150">
                                                    <template #content> {{ $t('移除后需点击底部保存按钮生效') }} </template>
                                                    <div class="icon-info" @click="removeRight(index)">
                                                        <el-icon color="#666" size="13" style="margin-top: 5px"><Delete /></el-icon>
                                                    </div>
                                                </el-tooltip>
                                            </div>
                                        </template>
                                        <template #default="{ $index }">
                                            <el-form-item>
                                                <el-checkbox v-model="formState.data[$index].rights[index].isChecked" :true-value="1" :false-value="0" />
                                            </el-form-item>
                                        </template>
                                    </el-table-column>
                                </template>
                            </el-table-column>
                            <el-table-column fixed="right" :width="80" :label="$t('操作')" class="setBtn">
                                <template #default="{ row, $index }">
                                    <div v-if="$index === formState.data.length - 1 && formState.data.length > 1">
                                        <el-button type="primary" link size="mini" @click="onDel($index)">{{ $t('删除') }}</el-button>
                                    </div>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-form-item>
                </el-form>
                <template v-if="formState.rankType == 1">
                    <div class="title">{{ $t('成长值设置') }}</div>
                    <el-form ref="formRef2" :rules="rules" :model="formState" label-width="auto">
                        <el-form-item :label="$t('完成下单')" prop="buyOrder">
                            <div>
                                <el-radio-group v-model="formState.growUpSetting.buyOrder" class="itemWidth">
                                    <el-radio :value="1">{{ $t('是') }}</el-radio>
                                    <el-radio :value="0">{{ $t('否') }}</el-radio>
                                </el-radio-group>
                                <div class="flex align-center mt10">
                                    <div class="mr10">{{ $t('每完成') }}</div>
                                    <TigInput type="integer"
                                        width="100px"
                                        :disabled="formState.growUpSetting.buyOrder === 0"
                                        v-model="formState.growUpSetting.buyOrderNumber"
                                    />
                                    <div class="ml10 mr10">{{ $t('笔订单，获得') }}</div>
                                    <TigInput type="integer"
                                        width="100px"
                                        :disabled="formState.growUpSetting.buyOrder === 0"
                                        v-model="formState.growUpSetting.buyOrderGrowth"
                                    />
                                    <div class="ml10">{{ $t('成长值') }}</div>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('完善信息')" prop="evpi">
                            <div>
                                <el-radio-group v-model="formState.growUpSetting.evpi" class="itemWidth">
                                    <el-radio :value="1">{{ $t('是') }}</el-radio>
                                    <el-radio :value="0">{{ $t('否') }}</el-radio>
                                </el-radio-group>
                                <div class="flex align-center mt10">
                                    <div class="mr10">{{ $t('完成后赠送：') }}</div>
                                    <TigInput type="integer"
                                        width="100px"
                                        :disabled="formState.growUpSetting.evpi === 0"
                                        v-model="formState.growUpSetting.evpiGrowth"
                                    />
                                    <div class="ml10">{{ $t('成长值') }}</div>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('绑定手机号')" prop="bindPhone">
                            <div>
                                <el-radio-group v-model="formState.growUpSetting.bindPhone" class="itemWidth">
                                    <el-radio :value="1">{{ $t('是') }}</el-radio>
                                    <el-radio :value="0">{{ $t('否') }}</el-radio>
                                </el-radio-group>
                                <div class="flex align-center mt10">
                                    <div class="mr10">{{ $t('完成后赠送：') }}</div>
                                    <TigInput type="integer"
                                        width="100px"
                                        :disabled="formState.growUpSetting.bindPhone === 0"
                                        v-model="formState.growUpSetting.bindPhoneGrowth"
                                    />
                                    <div class="ml10">{{ $t('成长值') }}</div>
                                </div>
                            </div>
                        </el-form-item>
                    </el-form>
                </template>
                <div class="title">{{ $t('等级规则（保级规则）') }}</div>
                <el-form :model="formState.userRankConfig" label-width="auto">
                    <el-form-item prop="userRankConfig.type" :label="$t('等级有效期：')">
                        <div>
                            <el-radio-group v-model="formState.userRankConfig.type">
                                <el-radio :value="1" size="large">{{ $t('永久有效') }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t('获得等级后永久有效，不会降级') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="userRankConfig.type" label=" ">
                        <div>
                            <el-radio-group v-model="formState.userRankConfig.type">
                                <el-radio :value="2" size="large">
                                    <div class="flex flex-align-center">
                                        <div>{{ $t('获得等级') }}</div>
                                        <el-select
                                            v-model="formState.userRankConfig.rankAfterMonth"
                                            style="min-width: 90px; margin: 0 10px"
                                            @change="formState.userRankConfig.useMonth = formState.userRankConfig.rankAfterMonth"
                                        >
                                            <el-option :label="$t('1个月')" :value="1" />
                                            <el-option :label="$t('3个月')" :value="3" />
                                            <el-option :label="$t('6个月')" :value="6" />
                                            <el-option :label="$t('12个月')" :value="12" />
                                        </el-select>
                                        <div>{{ $t('后，按照近') }}</div>
                                        <el-select v-model="formState.userRankConfig.rankAfterMonth" style="min-width: 90px; margin: 0 10px" :disabled="true">
                                            <el-option :label="$t('1个月')" :value="1" />
                                            <el-option :label="$t('3个月')" :value="3" />
                                            <el-option :label="$t('6个月')" :value="6" />
                                            <el-option :label="$t('12个月')" :value="12" />
                                        </el-select>
                                        <div v-if="formState.rankType == 1">{{ $t('的成长值重新计算等级') }}</div>
                                        <div v-if="formState.rankType == 2">{{ $t('的累计消费重新计算等级') }}</div>
                                    </div>
                                </el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t('举例: 设置12个月，用户在2023.1.1成为/设置VIP2，那么在2024.1.1将重新根据用户在2023.1.1-2024.1.1之间的累计{rankType1}计算等级。', { rankType1: formState.rankType == 1 ? "成长值" : "消费金额" }) }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('累计时机：')">
                        <div>
                            <el-radio-group v-model="confirmReceipt" :disabled="true">
                                <el-radio :value="1" size="large">{{ $t('确认收货') }}</el-radio>
                            </el-radio-group>
                            <el-table :data="formState.data" :loading="loading" row-key="userId">
                                <el-table-column :width="80" :label="$t('会员等级')" prop="level">
                                    <template #default="{ row, $index }">
                                        <div class="level_txt">vip{{ row.rankLevel }}</div>
                                    </template>
                                </el-table-column>
                                <el-table-column :width="210" prop="minGrowthPoints">
                                    <template #header>
                                        <div class="flex">
                                            <div>{{ $t('保级条件') }}</div>
                                            <el-tooltip effect="light" placement="bottom" show-after="150">
                                                <template #content> {{ $t('保级条件同升级条件一致，暂不支持自定义') }} </template>
                                                <div class="icon-info">
                                                    <el-icon color="#666" size="14" style="margin-top: 5px; margin-left: 5px"><QuestionFilled /></el-icon>
                                                </div>
                                            </el-tooltip>
                                        </div>
                                    </template>
                                    <template #default="{ row, $index }">
                                        <div class="flex flex-align-center flex-justify-between">
                                            <div class="info">
                                                <div v-if="row.minGrowthPoints === 0 || row.minGrowthPoints === ''">{{ $t('无门槛') }}</div>
                                                <div v-else>
                                                    <div class="flex flex-align-center" v-if="formState.rankType == 1">{{ row.minGrowthPoints }}{{ $t('成长值') }}</div>
                                                    <div class="flex flex-align-center" v-if="formState.rankType == 2">
                                                        {{ $t('消费满') }}{{ row.minGrowthPoints }}{{ config.config.amountUnit }}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "./src/card.less";
import { Image } from "@/components/image";
import { Plus, ArrowDown, QuestionFilled, Delete, Edit } from "@element-plus/icons-vue";
import { RadioType } from "@/components/radio";
import { onMounted, ref, shallowRef, reactive, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { getUserRank, updateUserRank } from "@/api/user/levelManage";
import type { FormRules } from "element-plus";
import { UserFormStatePro } from "@/types/user/levelManage.d";
import { DialogForm } from "@/components/dialog";
import { FormAddGallery } from "@/components/gallery";
import { isNotEmpty } from "@/utils/util";
import { useConfigStore } from "@/store/config";
/**
 * @description 父组件回调事件
 * @event submitCallback - 表单提交成功后的回调
 * @event update:confirmLoading - 更新确认按钮加载状态
 * @event close - 关闭弹窗
 */
const config: any = useConfigStore();
/** 确认收货标识，固定为1 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const confirmReceipt = ref(1);
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
/**
 * @property {number} id - 等级ID（编辑时使用）
 * @property {string} act - 操作类型：'add'-新增, 'detail'-编辑
 * @property {boolean} isDialog - 是否在弹窗中显示
 */
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
/** 主表单引用 */
const formRef = shallowRef();
/** 成长值设置表单引用 */
const formRef2 = shallowRef();
/** 加载状态 */
const loading = ref<boolean>(true);
/** 当前路由查询参数 */
const query = useRouter().currentRoute.value.query;
/** 当前操作类型，从props或路由中获取 */
const action = ref<string>(props.isDialog ? props.act : String(query.act));
/** 当前等级ID，从props或路由中获取 */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
/** 操作类型，'create'为新增，'update'为编辑 */
const operation = action.value === "add" ? "create" : "update";
/** 默认会员名称映射表 */
const nameList: { [key: number]: string } = {
    1: t('黄金会员'),
    2: t('铂金会员'),
    3: t('黑金会员'),
    4: t('钻石会员'),
    5: t('至尊会员')
};
/**
 * @description 表单数据状态
 * @property {number} rankType - 等级类型：1-成长值会员，2-消费会员
 * @property {Object} userRankConfig - 等级规则配置
 * @property {Object} growUpSetting - 成长值获取设置
 * @property {Array} data - 会员等级列表数据
 */
const formState = ref<UserFormStatePro>({
    rankType: 1, // 等级类型 1: 成长值 2: 消费
    userRankConfig: {
        type: 1,
        rankAfterMonth: 12,
        useMonth: 12
    },
    growUpSetting: {
        buyOrder: 1,
        buyOrderNumber: 1,
        buyOrderGrowth: 1,
        evpi: 1,
        evpiGrowth: 1,
        bindPhone: 1,
        bindPhoneGrowth: 1
    },
    data: [
        {
            rankLevel: 1, // 会员等级
            rankName: t('黄金会员'), // 会员名称
            rankLogo: "https://oss.tigshop.com/img/gallery/202501/1735803176Yh9mCaE2r9ebXK3bGm.png", // 会员logo
            rankCardType: 1, // 会员卡类型  1: 背景色 2：自定义图片
            rankIco: "card1", // 会员卡图片
            rankBg: "", // 会员卡背景色
            minGrowthPoints: 0, // 升级条件 0: 无门槛 只有列表第一个可以等于0
            discount: "", // 会员权益 折扣
            rankPoint: "", // 会员权益 多倍积分
            freeShipping: 0, // 会员权益 包邮 1: 包邮 0: 不包邮
            rights: []
        }
    ]
});
/**
 * @description 验证升级条件（成长值/消费金额）
 * @param rule 验证规则
 * @param value 当前值
 * @param callback 回调函数
 * @param index 当前行索引
 */
const validateMinGrowthPoints = (rule: any, value: any, callback: any, index: number) => {
    if (value === undefined || value === null || value === "") {
        callback(new Error(t('请输入')));
        return;
    }
    const numericValue = Number(value);
    if (isNaN(numericValue) || numericValue <= 0) {
        callback(new Error(t('请输入有效的数字')));
        return;
    }
    if (index > 0) {
        const previousValue = Number(formState.value.data[index - 1].minGrowthPoints);
        if (numericValue <= previousValue) {
            callback(new Error(t('需大于上一级的{rankType1}({previousValue})', { rankType1: formState.value.rankType == 1 ? t('成长值') : t('消费金额'), previousValue })));
            return;
        }
    }
    callback();
};
/**
 * @description 验证折扣值，需大于0小于10
 */
const validateDiscount = (rule: any, value: any, callback: any) => {
    if (value !== '' && (Number(value) < 0 || Number(value) > 9.9)) {
        callback(new Error(t('需大于0小于10')));
        return;
    }
    callback();
};
/**
 * @description 验证积分倍数，需大于0小于50
 */
const validatePoint = (rule: any, value: any, callback: any) => {
    if (value !== '' && (Number(value) < 0 || Number(value) > 50)) {
        callback(new Error(t('需大于0小于50')));
        return;
    }
    callback();
};
/**
 * @description 验证完成下单获取成长值的配置
 */
const validateBuyOrder = (rule: any, value: any, callback: any) => {
    let growUpSetting = formState.value.growUpSetting;
    if (growUpSetting.buyOrder == 1) {
        if (growUpSetting.buyOrderNumber < 1 || !growUpSetting.buyOrderNumber) {
            callback(new Error(t('请输入')));
            return;
        }
        if (growUpSetting.buyOrderGrowth < 1 || !growUpSetting.buyOrderNumber) {
            callback(new Error(t('请输入')));
            return;
        }
    }
    callback();
};
/**
 * @description 验证完善信息获取成长值的配置
 */
const validateEvpi = (rule: any, value: any, callback: any) => {
    let growUpSetting = formState.value.growUpSetting;
    if (growUpSetting.evpi == 1) {
        if (growUpSetting.evpiGrowth < 1 || !growUpSetting.evpiGrowth) {
            callback(new Error(t('请输入')));
            return;
        }
    }
    callback();
};
/**
 * @description 验证绑定手机号获取成长值的配置
 */
const validateBindPhone = (rule: any, value: any, callback: any) => {
    let growUpSetting = formState.value.growUpSetting;
    if (growUpSetting.bindPhone == 1) {
        if (growUpSetting.bindPhoneGrowth < 1 || !growUpSetting.bindPhoneGrowth) {
            callback(new Error(t('请输入')));
            return;
        }
    }
    callback();
};
interface RuleForm {
    rankName: string;
    minGrowthPoints: number;
    discount: number;
    rankPoint: number;
    buyOrder: number;
    evpi: number;
    bindPhone: number;
}
const rules = reactive<FormRules<RuleForm>>({
    rankName: [{ required: true, message: t('请输入'), trigger: "blur" }],
    discount: [{ required: true, validator: validateDiscount, trigger: "blur" }],
    rankPoint: [{ required: true, validator: validatePoint, trigger: "blur" }],
    minGrowthPoints: [
        {
            required: true,
            validator: (rule: any, value, callback) => {
                const regex = /\.(\d+)\./;
                const match: any = rule?.field.match(regex);
                const rowIndex = match ? match[1] : undefined;

                validateMinGrowthPoints(rule, value, callback, rowIndex);
            },
            trigger: "blur"
        }
    ],
    buyOrder: [{ required: true, validator: validateBuyOrder, trigger: "blur" }],
    evpi: [{ required: true, validator: validateEvpi, trigger: "blur" }],
    bindPhone: [{ required: true, validator: validateBindPhone, trigger: "blur" }]
});
/**
 * @description 修改会员卡面类型的回调
 * @param data 修改后的卡面数据
 */
const changeCardType = (data: any) => {
    console.log(data);
};
/**
 * @description 移除指定索引的自定义权益
 * @param index 权益索引
 */
const removeRight = (index: any) => {
    formState.value.data.forEach((item) => {
        item.rights.splice(index, 1);
    });
};
/**
 * @description 获取会员等级详情数据，用于编辑模式下回填表单
 */
const fetchUser = async () => {
    try {
        loading.value = true;
        const result = await getUserRank(action.value);
        console.log(result);
        result.userRankList.forEach((item: any) => {
            if(item.rights == null || !item.rights){
                item.rights = [];
            }
        })
        formState.value.data = result.userRankList;
        formState.value.rankType = result.rankType || 1;
        formState.value.userRankConfig = result.userRankConfig.data;
        if(isNotEmpty(result.growUpSetting)){
            formState.value.growUpSetting = result.growUpSetting;
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
/** VIP等级图标列表 */
const vipIcons = [
    "https://oss.tigshop.com/img/gallery/202501/1737524324ro1DJxNm3aQowZKnPU.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324ZUrDWMFqTPMBTCOlhg.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324ptLNB2WynLJBBwSHHj.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324hhmoz188M0ejyXEBhT.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324O1BXVeurAyDMj9QG8f.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324T7eKXTpMiCeVQOyFb6.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324bUufpiVaGcaUad1CWd.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324NhZbUn45wj1eSaIbjY.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324bv68MBsoQXa2rLUO9i.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324urqONY3QDjaTqW7d56.png"
];

/**
 * @description 添加新的会员等级（最多10级）
 * 验证表单后添加一个新的等级配置项
 */
const addPreferential = async () => {
    await formRef.value.validate();
    if (formState.value.data.length <= 10) {
        let rights = [];
        if (formState.value.data.length > 0) {
            rights = JSON.parse(JSON.stringify(formState.value.data[0].rights));
        }
        rights.map((item: any) => {
            item.isChecked = 0;
        });
        let data = {
            rankLevel: formState.value.data.length + 1, // 会员等级
            rankName: nameList[formState.value.data.length + 1], // 会员名称
            rankLogo: vipIcons[formState.value.data.length], // 会员logo
            rankCardType: 1, // 会员卡类型  1: 背景色 2：自定义图片
            rankIco: "card" + (formState.value.data.length + 1), // 会员卡图片
            rankBg: "", // 会员卡背景色
            minGrowthPoints: "", // 升级条件 0: 无门槛 只有列表第一个可以等于0
            discount: "", // 会员权益 折扣
            rankPoint: "", // 会员权益 多倍积分
            freeShipping: 0, // 会员权益 包邮 1: 包邮 0: 不包邮
            rights: rights // 权益列表
        };
        formState.value.data.push(data);
        await formRef.value.validate();
    }
};
/**
 * @description 添加或编辑自定义权益
 * @param data 权益数据，包含row、act、index
 */
const addRights = (data: any) => {
    const {row, act, index} = data;
    if(act == "add"){
        formState.value.data.forEach((item: any) => {
            item.rights.push(JSON.parse(JSON.stringify(row)));
        });
    }
    if(act == "edit"){
        formState.value.data.forEach((item: any) => {
            item.rights[index] = JSON.parse(JSON.stringify(row));
            item.rights[index].isChecked = 0;
        });
    }
};
/**
 * @description 删除指定索引的会员等级
 * @param index 等级索引
 */
const onDel = (index: number) => {
    formState.value.data.splice(index, 1);
};
onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchUser();
    } else {
        loading.value = false;
    }
});

/**
 * @description 表单验证通过后提交会员等级配置
 * 验证主表单和成长值设置表单（如果是成长值模式）
 */
const onSubmit = async () => {
    await formRef.value.validate();
    if(formState.value.rankType == 1){
        await formRef2.value.validate();
    }
    try {
        emit("update:confirmLoading", true);
        const result = await updateUserRank(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
/**
 * @description 表单提交入口，供父组件调用
 */
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    font-size: 16px;
    margin-bottom: 20px;
}
.lyecs-form-table {
    .level_txt {
        font-weight: 600;
        color: #ed6a18;
        font-size: 14px;
    }
    .discount_cell {
        display: flex;
        justify-content: center;
        position: relative;
        .icon-info {
            position: absolute;
            top: 50%;
            transform: translateY(-40%);
            right: 10px;
            cursor: pointer;
        }
    }
    .card_item {
        border: 1px solid #fff;
        border-radius: 3px;
        width: 56px;
        height: 30px;
        overflow: visible;
        background-repeat: no-repeat;
        background-position: 0% 0%;
        background-size: cover;
        &:hover {
            border: 1px solid var(--tig-primary-color);
        }
    }
    :deep(.el-table) {
        th.el-table__cell {
            background-color: #fff;
            padding: 8px 0;
        }
        .cell {
            overflow: visible !important;
        }
        .el-form-item__error {
            position: absolute !important;
            z-index: 99;
            top: 32px !important;
        }
        .el-form-item {
            margin-bottom: 5px;
        }
        .el-input-group__append,
        .el-input-group__prepend {
            padding: 0 10px;
        }
    }
    .table-col {
        width: 100%;
        height: 30px;
        .show {
            display: block;
        }
        .hide {
            display: none;
        }
        &:hover {
            .show {
                display: none;
            }
            .hide {
                display: block;
            }
        }
        &:focus-within .hide {
            display: block;
        }
        &:focus-within .show {
            display: none;
        }
    }
}
</style>
