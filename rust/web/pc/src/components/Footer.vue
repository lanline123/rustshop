<template>
  <footer class="tig-pc-footer">
    <!-- Service Promises -->
    <div class="promises-bar">
      <div class="container flex-between">
        <div class="promise-item flex-align-center" v-for="p in promises" :key="p.title">
          <div class="p-icon">{{ p.icon }}</div>
          <div class="p-text">
            <h4>{{ p.title }}</h4>
            <p>{{ p.desc }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Help Links -->
    <div class="help-links-bar container flex-between">
      <div class="help-col" v-for="col in helpLinks" :key="col.title">
        <div class="help-title">{{ col.title }}</div>
        <ul>
          <li v-for="lk in col.items" :key="lk"><a href="javascript:;">{{ lk }}</a></li>
        </ul>
      </div>

      <div class="contact-col">
        <div class="hotline-title">官方客服热线</div>
        <div class="hotline-phone">{{ servicePhone || '暂未配置' }}</div>
        <div class="hotline-time">工作时间：09:00 - 18:00</div>
        <button class="online-btn">在线客服咨询</button>
      </div>
    </div>

    <!-- Copyright & ICP -->
    <div class="copyright-bar">
      <div class="container">
        <p class="copyright-text">
          &copy; 2026 TigShop 综合商城系统
        </p>
        <p class="icp-text" v-if="icpNumber">
          <a :href="icpLink" target="_blank" rel="noopener noreferrer">{{ icpNumber }}</a>
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import request from '@/utils/request';

const servicePhone = ref('');
const icpNumber = ref('');
const icpLink = ref('https://beian.miit.gov.cn');

const promises = [
  { icon: '优', title: '品质甄选', desc: '官方自营 品牌直采保障' },
  { icon: '极', title: '极速物流', desc: '顺丰顺达 极速配送送达' },
  { icon: '退', title: '售后无忧', desc: '7天无理由 快速极速退款' },
  { icon: '帮', title: '专属客服', desc: '专业指导 贴心全程伴随' }
];

const helpLinks = [
  { title: '购物指南', items: ['购物流程', '会员注册', '常见问题', '联系客服'] },
  { title: '配送方式', items: ['包邮政策', '配送时效', '验货签收', '物流追踪'] },
  { title: '支付方式', items: ['微信支付', '支付宝', '余额支付', '分期付款'] },
  { title: '售后服务', items: ['售后政策', '退款说明', '返修退换', '取消订单'] },
  { title: '商家服务', items: ['商家入驻', '商户后台', '服务协议', '规则中心'] }
];

/** `GET /api/common/config/base` 返回的站点配置（只取页脚用到的字段）。 */
interface SiteConfig {
  kefuPhone?: string;
  shopIcpNo?: string;
  shopIcpNoUrl?: string;
  shopName?: string;
}

async function loadSiteConfig() {
  try {
    // 注意：正确路径是 `/common/config/base`，不是 `/common/config/basic`（后者在契约中不存在）
    const res = await request<SiteConfig>({
      url: '/common/config/base',
      method: 'get'
    });
    if (res) {
      // 字段名对齐 Rust `assemble_base`：kefuPhone / shopIcpNo（不是 servicePhone / icpNumber）
      servicePhone.value = res.kefuPhone || '';
      icpNumber.value = res.shopIcpNo || '';
      icpLink.value = res.shopIcpNoUrl || 'https://beian.miit.gov.cn';
    }
  } catch (err) {
    // 配置缺失时保持空值，页脚正常渲染
  }
}

onMounted(() => {
  loadSiteConfig();
});
</script>

<style scoped lang="less">
.tig-pc-footer {
  background: #ffffff;
  border-top: 1px solid var(--tig-border-color);
  margin-top: 40px;
}

.promises-bar {
  border-bottom: 1px solid #ebebeb;
  padding: 30px 0;

  .promise-item {
    gap: 14px;
    .p-icon {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      border: 2px solid var(--tig-red);
      color: var(--tig-red);
      font-weight: 900;
      font-size: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .p-text {
      h4 { font-size: 16px; color: var(--tig-text-main); margin-bottom: 4px; }
      p { font-size: 12px; color: var(--tig-text-secondary); }
    }
  }
}

.help-links-bar {
  padding: 40px 0;
  border-bottom: 1px solid #ebebeb;

  .help-col {
    .help-title {
      font-size: 15px;
      font-weight: 600;
      color: var(--tig-text-main);
      margin-bottom: 14px;
    }
    ul {
      list-style: none;
      li {
        margin-bottom: 8px;
        font-size: 12px;
        color: var(--tig-text-regular);
        a:hover { color: var(--tig-red); }
      }
    }
  }

  .contact-col {
    text-align: right;
    .hotline-title { font-size: 14px; color: var(--tig-text-regular); margin-bottom: 6px; }
    .hotline-phone { font-size: 26px; font-weight: 800; color: var(--tig-red); margin-bottom: 4px; }
    .hotline-time { font-size: 12px; color: var(--tig-text-secondary); margin-bottom: 12px; }
    .online-btn {
      background: var(--tig-red);
      color: #fff;
      border: none;
      border-radius: 4px;
      padding: 8px 20px;
      font-size: 13px;
      cursor: pointer;
    }
  }
}

.copyright-bar {
  background: #f7f7f7;
  padding: 20px 0;
  text-align: center;
  font-size: 12px;
  color: var(--tig-text-secondary);
  line-height: 2;

  .sep { margin: 0 8px; color: #ccc; }
}
</style>
