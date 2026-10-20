<template>
  <div class="tig-mall-layout">
    <Header />
    <main class="mall-main-content">
      <router-view />
    </main>
    <Footer />
  </div>
</template>

<script setup lang="ts">
import Header from '@/components/Header.vue';
import Footer from '@/components/Footer.vue';
</script>

<style scoped>
.tig-mall-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.mall-main-content {
  flex: 1;
}
</style>
