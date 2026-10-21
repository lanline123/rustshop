<template>
  <div class="tig-schema-form" v-loading="loading">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-width="schema.labelWidth || '160px'"
      label-position="right"
      class="schema-el-form"
    >
      <div v-for="(section, sIdx) in schema.sections" :key="sIdx" class="form-section tig-card">
        <div v-if="section.title" class="tig-page-title">
          <span>{{ section.title }}</span>
          <span v-if="section.description" class="section-desc">{{ section.description }}</span>
        </div>

        <el-row :gutter="24">
          <template v-for="field in section.fields" :key="field.prop">
            <el-col
              v-if="!field.hidden || !field.hidden(formData)"
              :span="field.colSpan || 24"
            >
              <el-form-item :label="field.label" :prop="field.prop">
                <!-- Input / Text / Password -->
                <el-input
                  v-if="!field.type || field.type === 'input'"
                  v-model="formData[field.prop]"
                  :placeholder="field.placeholder || `请输入${field.label}`"
                  :disabled="field.disabled"
                  style="max-width: 480px"
                  clearable
                />

                <el-input
                  v-else-if="field.type === 'password'"
                  v-model="formData[field.prop]"
                  type="password"
                  show-password
                  :placeholder="field.placeholder || `请输入${field.label}`"
                  style="max-width: 480px"
                />

                <!-- Textarea -->
                <el-input
                  v-else-if="field.type === 'textarea'"
                  v-model="formData[field.prop]"
                  type="textarea"
                  :rows="3"
                  :placeholder="field.placeholder || `请输入${field.label}`"
                  style="max-width: 560px"
                />

                <!-- Number -->
                <el-input-number
                  v-else-if="field.type === 'number'"
                  v-model="formData[field.prop]"
                  :min="field.min"
                  :max="field.max"
                  :step="field.step || 1"
                  :disabled="field.disabled"
                />

                <!-- Switch -->
                <el-switch
                  v-else-if="field.type === 'switch'"
                  v-model="formData[field.prop]"
                  :active-value="1"
                  :inactive-value="0"
                  :disabled="field.disabled"
                />

                <!-- Slider -->
                <el-slider
                  v-else-if="field.type === 'slider'"
                  v-model="formData[field.prop]"
                  :min="field.min || 0"
                  :max="field.max || 100"
                  :step="field.step || 1"
                  style="max-width: 400px"
                />

                <!-- Color Picker -->
                <el-color-picker
                  v-else-if="field.type === 'color'"
                  v-model="formData[field.prop]"
                />

                <!-- Radio Group -->
                <el-radio-group
                  v-else-if="field.type === 'radio'"
                  v-model="formData[field.prop]"
                  :disabled="field.disabled"
                >
                  <el-radio
                    v-for="opt in field.options"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </el-radio>
                </el-radio-group>

                <!-- Checkbox Group -->
                <el-checkbox-group
                  v-else-if="field.type === 'checkbox'"
                  v-model="formData[field.prop]"
                  :disabled="field.disabled"
                >
                  <el-checkbox
                    v-for="opt in field.options"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </el-checkbox>
                </el-checkbox-group>

                <!-- Select -->
                <el-select
                  v-else-if="field.type === 'select'"
                  v-model="formData[field.prop]"
                  :placeholder="field.placeholder || `请选择${field.label}`"
                  style="max-width: 480px"
                  clearable
                >
                  <el-option
                    v-for="opt in field.options"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>

                <!-- Cascader (省市区/多级联动) -->
                <el-cascader
                  v-else-if="field.type === 'cascader'"
                  v-model="formData[field.prop]"
                  :options="optionsOf(field)"
                  :props="field.cascaderProps || { emitPath: false }"
                  :placeholder="field.placeholder || '请选择分类/地区'"
                  style="max-width: 480px"
                  clearable
                />

                <!-- Date / Datetime -->
                <el-date-picker
                  v-else-if="field.type === 'date' || field.type === 'datetime'"
                  v-model="formData[field.prop]"
                  :type="field.type === 'datetime' ? 'datetime' : 'date'"
                  :placeholder="field.placeholder || '选择日期时间'"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />

                <!-- Single Image Upload -->
                <ImageUpload
                  v-else-if="field.type === 'image'"
                  v-model="formData[field.prop]"
                  :tip="field.extra"
                />

                <!-- Multi-image Upload (相册) -->
                <div v-else-if="field.type === 'image-list'" class="image-list-wrapper">
                  <div class="image-list-grid">
                    <div
                      v-for="(img, idx) in (formData[field.prop] || [])"
                      :key="idx"
                      class="image-item"
                    >
                      <img :src="resolveImageUrl(img)" />
                      <el-icon class="del-btn" @click="removeImage(field.prop, idx)"><Delete /></el-icon>
                    </div>
                    <ImageUpload
                      v-if="(formData[field.prop] || []).length < (field.max || 9)"
                      model-value=""
                      @update:model-value="(val) => appendImage(field.prop, val)"
                    />
                  </div>
                  <div v-if="field.extra" class="field-extra-tip">{{ field.extra }}</div>
                </div>

                <!-- Extra Tip -->
                <div v-if="field.extra && field.type !== 'image' && field.type !== 'image-list'" class="field-extra-tip">
                  {{ field.extra }}
                </div>
              </el-form-item>
            </el-col>
          </template>
        </el-row>
      </div>

      <!-- Action Footer -->
      <div class="form-action-footer tig-card" v-if="schema.apiSave || schema.apiCreate || schema.apiUpdate || showSubmit">
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="handleSubmit"
          class="submit-btn"
        >
          {{ schema.submitText || (props.mode === 'edit' ? '保存修改' : '确定新增') }}
        </el-button>
        <el-button size="large" @click="handleReset">重置</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import type { FormInstance } from 'element-plus';
import { ElMessage } from 'element-plus';
import { Delete } from '@element-plus/icons-vue';
import request from '@/utils/request';
import type { FormSchema, FormFieldSchema, FormOption } from './types';
import ImageUpload from '../widgets/ImageUpload.vue';

const props = withDefaults(
  defineProps<{
    schema: FormSchema;
    initialData?: Record<string, any>;
    showSubmit?: boolean;
    /** create：新增；edit：编辑（提交走 apiUpdate，并回填详情）。 */
    mode?: 'create' | 'edit';
  }>(),
  {
    showSubmit: true,
    mode: 'create'
  }
);

const emit = defineEmits<{
  (e: 'success', data: any): void;
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);
const submitting = ref(false);
const formData = reactive<Record<string, any>>({});
/** 异步加载的字段选项（optionsLoader 结果）。 */
const dynamicOptions = reactive<Record<string, FormOption[]>>({});

function optionsOf(field: FormFieldSchema): FormOption[] {
  return field.options || dynamicOptions[field.prop] || [];
}

async function initDynamicOptions() {
  for (const sec of props.schema.sections) {
    for (const f of sec.fields) {
      if (!f.optionsLoader) continue;
      try {
        dynamicOptions[f.prop] = await f.optionsLoader();
      } catch (err) {
        // 下拉数据加载失败不阻塞表单；错误提示由请求基座统一处理
        dynamicOptions[f.prop] = [];
      }
    }
  }
}

const formRules = computed(() => {
  const rulesMap: Record<string, any[]> = {};
  for (const sec of props.schema.sections) {
    for (const f of sec.fields) {
      if (f.rules && f.rules.length > 0) {
        rulesMap[f.prop] = f.rules;
      }
    }
  }
  return rulesMap;
});

function resolveImageUrl(url: string) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  if (url.startsWith('/static')) return url;
  return `/static/${url}`;
}

function appendImage(prop: string, url: string) {
  if (!url) return;
  if (!Array.isArray(formData[prop])) {
    formData[prop] = [];
  }
  formData[prop].push(url);
}

function removeImage(prop: string, idx: number) {
  if (Array.isArray(formData[prop])) {
    formData[prop].splice(idx, 1);
  }
}

function initDefaults() {
  for (const sec of props.schema.sections) {
    for (const f of sec.fields) {
      if (f.defaultValue !== undefined) {
        formData[f.prop] = f.defaultValue;
      } else if (f.type === 'checkbox' || f.type === 'image-list') {
        formData[f.prop] = [];
      } else if (f.type === 'switch') {
        formData[f.prop] = 1;
      } else {
        formData[f.prop] = '';
      }
    }
  }
}

async function loadData() {
  const pk = props.schema.primaryKey || 'id';
  // 编辑：先回填列表行，再按 apiDetail 拉完整详情（列表行通常缺图片/描述等字段）
  if (props.initialData) {
    Object.assign(formData, props.initialData);
  }
  if (props.mode === 'edit' && props.schema.apiDetail) {
    const id = props.initialData?.[pk];
    if (id !== undefined && id !== null) {
      loading.value = true;
      try {
        const res = await request({
          url: props.schema.apiDetail,
          method: 'get',
          params: { [pk]: id }
        });
        if (res && typeof res === 'object') {
          Object.assign(formData, res);
        }
      } catch (err: any) {
        // 错误提示由请求基座统一处理
      } finally {
        loading.value = false;
      }
    }
    return;
  }
  if (props.initialData) return;
  if (!props.schema.apiGet) return;

  loading.value = true;
  try {
    const res = await request({
      url: props.schema.apiGet,
      method: 'get'
    });
    if (res && typeof res === 'object') {
      Object.assign(formData, res);
    }
  } catch (err: any) {
  } finally {
    loading.value = false;
  }
}

async function handleSubmit() {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    const url =
      props.mode === 'edit'
        ? props.schema.apiUpdate || props.schema.apiSave
        : props.schema.apiCreate || props.schema.apiSave;

    if (!url) {
      emit('success', { ...formData });
      return;
    }

    const payload: Record<string, any> = { ...formData };
    if (props.mode === 'edit') {
      const pk = props.schema.primaryKey || 'id';
      const id = props.initialData?.[pk];
      if (id !== undefined) payload[pk] = id;
    }

    submitting.value = true;
    try {
      await request({
        url,
        method: 'post',
        data: payload
      });
      ElMessage.success(props.mode === 'edit' ? '保存成功' : '新增成功');
      emit('success', payload);
    } catch (err) {
    } finally {
      submitting.value = false;
    }
  });
}

function handleReset() {
  formRef.value?.resetFields();
  loadData();
}

onMounted(async () => {
  await initDynamicOptions();
  initDefaults();
  await loadData();
});
</script>

<style scoped lang="less">
.tig-schema-form {
  position: relative;
}

.section-desc {
  font-size: 13px;
  color: var(--tig-text-secondary);
  font-weight: normal;
  margin-left: 12px;
}

.field-extra-tip {
  font-size: 12px;
  color: var(--tig-text-secondary);
  line-height: 1.6;
  margin-top: 4px;
}

.image-list-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  width: 90px;
  height: 90px;
  border-radius: 6px;
  border: 1px solid var(--tig-border-color);
  position: relative;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .del-btn {
    position: absolute;
    top: 4px;
    right: 4px;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    padding: 2px;
    border-radius: 50%;
    cursor: pointer;
    font-size: 12px;
  }
}

.form-action-footer {
  position: sticky;
  bottom: 0;
  z-index: 10;
  display: flex;
  justify-content: flex-start;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);

  .submit-btn {
    padding: 0 32px;
  }
}
</style>
