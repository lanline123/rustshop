/**
 * @file 商品服务编辑页面
 * @description 用于添加或编辑商品附加服务信息，如"七天无理由退换"、"正品保障"等。
 * @features
 * - 服务名称和简介编辑
 * - 服务图标上传
 * - 排序设置
 * - 是否默认开启设置
 */
<template>
  <div class="container">
    <div class="content_wrapper">
      <div class="lyecs-form-table">
        <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
          <el-form-item :rules="[{ required: true, message: '名称不能为空!' }]" :label="$t('名称')" prop="productServiceName">
            <TigInput classType="tig-form-input" v-model="formState.productServiceName"></TigInput>
          </el-form-item>
          <el-form-item :label="$t('简介')">
            <TigInput classType="tig-form-input"
                v-model="formState.productServiceDesc"
                :rows="2"
                :placeholder="$t('请输入简介')"
                type="textarea"
            />
          </el-form-item>
          <el-form-item :label="$t('图标')">
            <FormAddGallery v-model:photo="formState.icoImg"/>
            <div class="extra">{{ $t('请统一上传比例为2:1的图片，宽200px，高100px（高清）') }}</div>
          </el-form-item>
          <el-form-item :label="$t('排序')">
            <TigInput classType="tig-form-input" type="integer" v-model="formState.sortOrder"/>
          </el-form-item>
          <el-form-item :label="$t('是否默认')" >
            <el-radio-group v-model="formState.defaultOn">
              <el-radio :value="1">{{ $t('是') }}</el-radio>
              <el-radio :value="0">{{ $t('否') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
            <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t('提交') }}</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted } from "vue"
import { useRouter } from 'vue-router'
import { message } from "ant-design-vue";
import { FormAddGallery } from '@/components/gallery'
import { ProductServicesFormState } from '@/types/product/productServices';
import { getProductServices, updateProductServices } from "@/api/product/productServices";

/** 父组件回调事件 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

/**
 * @property {number} id - 商品服务ID，编辑时使用
 * @property {string} act - 操作类型：'add' 新增，'detail' 编辑
 * @property {boolean} isDialog - 是否在弹窗中显示
 */
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ''
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === 'add' ? 'create' : 'update';
/** 表单引用 */
const formRef = shallowRef();
/**
 * @property {string} productServiceName - 服务名称
 * @property {string} productServiceDesc - 服务简介
 * @property {string} icoImg - 服务图标
 * @property {number} sortOrder - 排序值
 * @property {number} defaultOn - 是否默认开启 (1: 是, 0: 否)
 */
const formState = ref<ProductServicesFormState>({
  sortOrder: 50,
  defaultOn:1
});

onMounted(() => {
  if (action.value === "detail") {
        // 获取详情数据
        fetchProductServicesList();
    } else {
        loading.value = false;
    }
});
/**
 * @description 获取商品服务详情
 * 用于编辑模式下回填表单数据
 */
const fetchProductServicesList = async () => {
    try {
        const result = await getProductServices(action.value, { id: id.value });
        Object.assign(
          formState.value,
          result
        )
    } catch (error:any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};


/**
 * @description 表单验证通过后提交商品服务数据
 * 根据operation决定是新增还是更新操作
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        const result = await updateProductServices(operation, { id: id.value, ...formState.value });
        emit('submitCallback', result);
        message.success(t('操作成功'));
    } catch (error:any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};
/**
 * @description 表单提交入口，供父组件调用
 */
const onFormSubmit = () => {
    onSubmit()
};

defineExpose({ onFormSubmit });
</script>
