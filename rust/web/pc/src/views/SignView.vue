<template>
  <div class="tig-sign-page">
    <div class="container member-layout">
      <MemberNav />

      <div class="info-row">
        <div class="content-card" v-loading="loading">
          <div class="sign-header">
            <div class="sign-title">
              <h2>每日签到</h2>
              <p>连续签到可获得更多积分奖励</p>
            </div>
            <div class="sign-summary">
              <div class="summary-item">
                <div class="value">{{ signInfo?.record ?? 0 }}</div>
                <div class="label">连续签到（天）</div>
              </div>
              <div class="summary-item">
                <div class="value">{{ signInfo?.signPoints ?? 0 }}</div>
                <div class="label">本次可得积分</div>
              </div>
            </div>
            <el-button type="primary" size="large" :disabled="signed || signing" :loading="signing" @click="doSign">
              {{ signed ? '今日已签到' : '立即签到' }}
            </el-button>
          </div>

          <!-- 签到天数奖励 -->
          <div class="sign-days">
            <div class="section-title">连续签到奖励</div>
            <div class="days-grid">
              <div
                v-for="day in signInfo?.days ?? []"
                :key="day.id"
                class="day-card"
                :class="{ done: Number(day.dayNum ?? 0) <= Number(signInfo?.record ?? 0) }"
              >
                <div class="day-name">{{ day.name || `第${day.dayNum}天` }}</div>
                <div class="day-points">+{{ day.points ?? 0 }} 积分</div>
              </div>
              <el-empty v-if="!signInfo?.days?.length" description="暂无签到奖励配置" :image-size="70" />
            </div>
          </div>

          <!-- 推荐商品 -->
          <div v-if="signInfo?.recommendGoods?.length" class="recommend">
            <div class="section-title">推荐好物</div>
            <div class="goods-grid">
              <div
                v-for="product in signInfo.recommendGoods"
                :key="product.productId"
                class="goods-card"
                @click="goProduct(product.productId)"
              >
                <img :src="imageUrl(product.picThumb || product.picUrl)" :alt="product.productName" />
                <div class="goods-name">{{ product.productName }}</div>
                <div class="goods-price">¥{{ formatPrice(product.productPrice ?? product.price) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 每日签到（接口：`GET /user/sign/index`、`GET /user/sign/sign`）。
 *
 * 原版 Nuxt PC 端未内置独立签到页，本页按会员中心统一视觉补齐；
 * 数据全部来自真实签到接口，未签到配置时展示空态。
 */
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { signApi } from '@/api';
import type { UserSignInfo } from '@/api/types';
import MemberNav from '@/components/MemberNav.vue';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, imageUrl } from '@/utils/format';

const router = useRouter();

const loading = ref(true);
const signing = ref(false);
const signInfo = ref<UserSignInfo | null>(null);
const signed = computed(() => Number(signInfo.value?.isSign ?? 0) === 1);

async function load() {
  loading.value = true;
  try {
    signInfo.value = await signApi.index();
  } catch {
    signInfo.value = null;
  } finally {
    loading.value = false;
  }
}

async function doSign() {
  signing.value = true;
  try {
    await signApi.sign();
    ElMessage.success('签到成功');
    await load();
  } finally {
    signing.value = false;
  }
}

function goProduct(productId: number) {
  if (productId) router.push(`/product/${productId}`);
}

onMounted(() => {
  applySeo({ title: '每日签到', noindex: true });
  load();
});
</script>

<style scoped lang="less">
.tig-sign-page {
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
  padding: 20px;
  min-height: 340px;
}

.sign-header {
  display: flex;
  align-items: center;
  gap: 40px;
  background: linear-gradient(120deg, #fff7f6, #ffffff 70%);
  border: 1px solid #ffdcdc;
  border-radius: 8px;
  padding: 20px 24px;

  .sign-title {
    flex: 1;
    h2 { font-size: 20px; font-weight: 800; margin-bottom: 6px; }
    p { color: #888; font-size: 13px; }
  }

  .sign-summary {
    display: flex;
    gap: 28px;
    .summary-item {
      text-align: center;
      .value { font-size: 26px; font-weight: 800; color: var(--tig-red); }
      .label { color: #888; font-size: 12px; margin-top: 4px; }
    }
  }
}

.section-title { font-size: 15px; font-weight: 700; margin: 22px 0 12px; }

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
}

.day-card {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 12px 8px;
  text-align: center;
  background: #fafafa;

  &.done { border-color: #ffdcdc; background: #fff7f6; }

  .day-name { font-size: 13px; color: #555; }
  .day-points { font-size: 12px; color: var(--tig-red); margin-top: 6px; }
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.goods-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;

  &:hover { box-shadow: 0 6px 14px rgba(0, 0, 0, 0.08); }

  img { width: 100%; height: 150px; object-fit: cover; }
  .goods-name { font-size: 12px; padding: 8px 10px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .goods-price { color: var(--tig-red); font-size: 15px; font-weight: 700; padding: 4px 10px 10px; }
}
</style>
