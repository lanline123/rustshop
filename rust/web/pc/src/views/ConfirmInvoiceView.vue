<template>
  <div class="tig-member-sub-page">
    <div class="container member-layout">
      <MemberNav />

      <div class="info-row">
        <div class="content-card">
          <div class="confirm-content">
            <p class="confirm-title">申请开具增值税专用发票确认书</p>
            <p>
              根据国家税法及发票管理相关规定，任何单位和个人不得要求他人为自己开具与实际经营业务情况不符的增值税专用发票包括并不限于：
            </p>
            <p>1）在没有货物采购或者没有接受应税劳务的情况下要求他人为自己开具增值税专用发票；</p>
            <p>
              2）虽有货物采购或者接受应税劳务但要求他人为自己开具数量或金额与实际情况不符的增值税专用发票，否则属于“虚开增值税专用发票”。
            </p>
            <p>我已充分了解上述各项相关国家税法和发票管理规定，并确认仅就我司实际购买商品或服务索取发票。</p>
            <p>如我司未按国家相关规定申请开具或使用增值税专用发票，由我司自行承担相应法律后果。</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 增票资质确认书（对齐原版 `pages/member/userInvoice/confirmInfo.vue`）。
 * 纯静态协议页，从增票资质页勾选协议处打开。
 */
import MemberNav from '@/components/MemberNav.vue';
import { applySeo } from '@/composables/useSeo';

applySeo({ title: '增票资质确认书', noindex: true });
</script>

<style scoped lang="less">
.tig-member-sub-page {
  background: #f5f6fa;
  padding: 20px 0 50px;
}

.member-layout {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.info-row { flex: 1; min-width: 0; }

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 28px 32px;
  min-height: 340px;
}

.confirm-content {
  color: #666;
  font-size: 14px;
  line-height: 2.2;

  .confirm-title {
    font-size: 18px;
    font-weight: 700;
    text-align: center;
    color: #333;
    margin-bottom: 18px;
  }
}
</style>
