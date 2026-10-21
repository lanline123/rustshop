<template>
  <div class="tig-schema-page-view">
    <SchemaForm
      v-if="schemaType === 'form' && currentSchema"
      :schema="currentSchema"
    />
    <SchemaTable
      v-else-if="schemaType === 'table' && currentSchema"
      :schema="currentSchema"
    />
    <el-empty v-else description="页面 Schema 正在准备中..." />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import SchemaForm from '@/components/schema/SchemaForm.vue';
import SchemaTable from '@/components/schema/SchemaTable.vue';

const route = useRoute();

const schemaType = computed(() => route.meta?.schemaType as 'form' | 'table');
const currentSchema = computed(() => route.meta?.schema as any);
</script>

<style scoped>
.tig-schema-page-view {
  width: 100%;
}
</style>
