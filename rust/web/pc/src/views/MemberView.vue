<template>
  <div class="tig-member-legacy-redirect">
    <el-result icon="info" title="页面已升级" sub-title="正在跳转到新的个人中心..." />
  </div>
</template>

<script setup lang="ts">
/**
 * 旧版会员中心页（`/member` 已由 ProfileView 接管）。
 *
 * 该组件保留仅为避免旧路由被硬编码引用时 404；挂载后立即以 replace 方式
 * 跳转到新的个人中心路由，不再包含任何演示数据或占位交互。
 */
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

onMounted(() => {
  router.replace('/member');
});
</script>

<style scoped>
.tig-member-legacy-redirect {
  padding: 80px 0;
}
</style>
