import { useState } from 'react';
import { clearToken, enterDemo, getToken, isDemo } from './api';
import AdminLayout, { type PageKey } from './layout/AdminLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Orders from './pages/Orders';
import Members from './pages/Members';
import Content from './pages/Content';
import Brands from './pages/Brands';
import ProductGroups from './pages/ProductGroups';
import Categories from './pages/Categories';
import ProductServices from './pages/ProductServices';
import Comments from './pages/Comments';
import AttributesTemplates from './pages/AttributesTemplates';
import FriendLinks from './pages/FriendLinks';
import Regions from './pages/Regions';
import LogisticsCompanies from './pages/LogisticsCompanies';
import AppVersion from './pages/AppVersion';
import AdminLogs from './pages/AdminLogs';
import AdminUsers from './pages/AdminUsers';
import Roles from './pages/Roles';
import AgreementPage from './pages/AgreementPage';
import AccountManage from './pages/AccountManage';
import TaxRates from './pages/TaxRates';
import ShippingTypes from './pages/ShippingTypes';
import ShippingSettingsPage from './pages/ShippingSettings';
import AutoConfigPage, { type ConfigFieldGroup, type ConfigFieldRule } from './pages/AutoConfigPage';
import LogisticsSettings from './pages/LogisticsSettings';
import PayLogs from './pages/PayLogs';
import RefundLogs from './pages/RefundLogs';
import BalanceLogs from './pages/BalanceLogs';
import StaffWalletLogs from './pages/StaffWalletLogs';
import BuyerShow from './pages/BuyerShow';
import IntegralLogs from './pages/IntegralLogs';
import Placeholder from './pages/Placeholder';
import { pageTitles } from './config/adminMenu';
import { configSections } from './config/configSections';

type ConfigPageDef = {
  title: string;
  getApi: string;
  saveApi: string;
  sections?: Array<{ title: string; fields: string[] }>;
  rules?: Record<string, ConfigFieldRule>;
  fieldGroups?: ConfigFieldGroup[];
  controls?: Record<string, 'radio' | 'switch'>;
  hiddenFields?: string[];
};

const configPages: Record<string, ConfigPageDef> = {
  baseBasicManage: { title: '基础信息', getApi: '/setting/config/basicSettings', saveApi: '/setting/config/saveBasic' },
  baseProductManage: { title: '商品设置', getApi: '/setting/config/productSettings', saveApi: '/setting/config/saveProduct' },
  baseShoppingManage: { title: '交易设置', getApi: '/setting/config/shoppingSettings', saveApi: '/setting/config/saveShopping' },
  baseOrderManage: { title: '订单设置', getApi: '/setting/config/orderSettings', saveApi: '/setting/config/saveOrder' },
  kefuSetting: { title: '客服设置', getApi: '/setting/config/kefuSettings', saveApi: '/setting/config/saveKefu' },
  globalSetting: { title: '全局设置', getApi: '/setting/config/globalSettings', saveApi: '/setting/config/saveGlobal' },
  loginSetting: {
    title: '登录设置',
    getApi: '/setting/config/loginSettings',
    saveApi: '/setting/config/saveLogin',
    controls: {
      openWechatOauth: 'radio',
      openWechatPcLogin: 'radio',
      wechatRegisterBindPhone: 'radio',
      openWechatRegister: 'radio',
      wechatShareNeedLogin: 'radio',
      facebookLoginOn: 'radio',
      googleLoginOn: 'radio',
      twitterLoginOn: 'radio',
    },
    rules: {
      openWechatPcLogin: {
        disabledWhen: { field: 'openWechatOauth', equals: 0 },
      },
      wechatRegisterBindPhone: {
        disabledWhen: {
          any: [
            { field: 'openWechatOauth', equals: 0 },
            { field: 'openWechatPcLogin', equals: 0 },
          ],
        },
      },
      openWechatRegister: {
        disabledWhen: { field: 'openWechatOauth', equals: 0 },
      },
      facebookClientId: { visibleWhen: { field: 'facebookLoginOn', equals: 1 } },
      facebookClientSecret: { visibleWhen: { field: 'facebookLoginOn', equals: 1 } },
      googleClientId: { visibleWhen: { field: 'googleLoginOn', equals: 1 } },
      googleClientSecret: { visibleWhen: { field: 'googleLoginOn', equals: 1 } },
      twitterClientId: { visibleWhen: { field: 'twitterLoginOn', equals: 1 } },
      twitterClientSecret: { visibleWhen: { field: 'twitterLoginOn', equals: 1 } },
    },
    fieldGroups: [
      {
        title: '公众号授权配置',
        description: '配置微信公众号授权登录所需的 AppID、密钥与服务器校验信息。',
        tag: '微信',
        fields: ['wechatAppId', 'wechatAppSecret', 'wechatServerToken', 'wechatServerSecret'],
        visibleWhen: { field: 'openWechatOauth', equals: 1 },
      },
      {
        title: 'Facebook 登录',
        description: '开启 Facebook 登录后，填写对应应用的 Client ID 与 Secret。',
        tag: 'Facebook',
        fields: ['facebookLoginOn', 'facebookClientId', 'facebookClientSecret'],
      },
      {
        title: 'Google 登录',
        description: '开启 Google 登录后，填写对应 OAuth 应用的 Client ID 与 Secret。',
        tag: 'Google',
        fields: ['googleLoginOn', 'googleClientId', 'googleClientSecret'],
      },
      {
        title: 'Twitter 登录',
        description: '开启 Twitter 登录后，填写对应应用的 Client ID 与 Secret。',
        tag: 'Twitter',
        fields: ['twitterLoginOn', 'twitterClientId', 'twitterClientSecret'],
      },    ],
  },
  storeAmapSetting: { title: '地图设置', getApi: '/setting/config/mapSettings', saveApi: '/setting/config/saveMapSettings' },
  personalizedManage: { title: '个性化设置', getApi: '/setting/config/getPersonalizedSetting', saveApi: '/setting/config/savePersonalizedSetting' },
  baseNoticeManage: { title: '通知设置', getApi: '/setting/config/notifySettings', saveApi: '/setting/config/saveNotify' },
};

Object.entries(configPages).forEach(([key, page]) => {
  page.sections = configSections[key];
});

const knownPages = new Set(['dashboard', 'products', 'brandManage', 'categoryManage', 'productGroupManage', 'productServicesManage', 'commentManage', 'productAttributesTplManage', 'friendLinksManage', 'regionManage', 'logisticsCompanyManage', 'appVersionManage', 'adminLogManage', 'adminUserManage', 'adminRoleManage', 'serviceAgreement', 'privacyAgreement', 'afterSaleService', 'accountManage', 'taxRateManage', 'shippingTypeManage', 'logisticsManage', 'logisticsCompanyConfigManage', 'payLogManage', 'refundLogManage', 'userBalanceLogManage', 'staffFundWalletLogManage', 'buyerShowManage', 'integralLogManage', 'baseBasicManage', 'baseProductManage', 'baseShoppingManage', 'baseOrderManage', 'kefuSetting', 'globalSetting', 'loginSetting', 'storeAmapSetting', 'personalizedManage', 'baseNoticeManage', 'orders', 'members', 'content']);

if (new URLSearchParams(window.location.search).get('demo') === '1') {
  enterDemo();
}

function initialPage(): PageKey {
  const value = new URLSearchParams(window.location.search).get('page');
  return value && pageTitles[value] ? value : 'dashboard';
}

export default function App() {
  const [authed, setAuthed] = useState(() => Boolean(getToken() || isDemo()));
  const [page, setPage] = useState<PageKey>(initialPage);

  const changePage = (next: PageKey) => {
    setPage(next);
    const url = new URL(window.location.href);
    url.searchParams.set('page', next);
    window.history.replaceState(null, '', url);
  };

  if (!authed) {
    return <Login onSuccess={() => setAuthed(true)} />;
  }

  return (
    <AdminLayout
      page={page}
      onPageChange={changePage}
      onLogout={() => {
        clearToken();
        setAuthed(false);
      }}
    >
      {page === 'dashboard' && <Dashboard onNavigate={changePage} />}
      {page === 'products' && <Products />}
      {page === 'brandManage' && <Brands />}
      {page === 'productGroupManage' && <ProductGroups />}
      {page === 'categoryManage' && <Categories />}
      {page === 'productServicesManage' && <ProductServices />}
      {page === 'commentManage' && <Comments />}
      {page === 'productAttributesTplManage' && <AttributesTemplates />}
      {page === 'friendLinksManage' && <FriendLinks />}
      {page === 'regionManage' && <Regions />}
      {page === 'logisticsCompanyManage' && <LogisticsCompanies />}
      {page === 'appVersionManage' && <AppVersion />}
      {page === 'adminLogManage' && <AdminLogs />}
      {page === 'adminUserManage' && <AdminUsers />}
      {page === 'adminRoleManage' && <Roles />}
      {page === 'serviceAgreement' && (
        <AgreementPage code="termsOfService" title="服务协议" description="配置后台登录服务协议内容与启用状态。" />
      )}
      {page === 'privacyAgreement' && (
        <AgreementPage code="privacyPolicy" title="隐私政策" description="配置后台登录隐私政策内容与启用状态。" />
      )}
      {page === 'accountManage' && <AccountManage />}
      {page === 'taxRateManage' && <TaxRates />}
      {page === 'shippingTypeManage' && <ShippingTypes />}
      {page === 'logisticsManage' && <ShippingSettingsPage />}
      {page === 'logisticsCompanyConfigManage' && <LogisticsSettings />}
      {page === 'payLogManage' && <PayLogs />}
      {page === 'refundLogManage' && <RefundLogs />}
      {page === 'userBalanceLogManage' && <BalanceLogs />}
      {page === 'staffFundWalletLogManage' && <StaffWalletLogs />}
      {page === 'buyerShowManage' && <BuyerShow />}
      {page === 'integralLogManage' && <IntegralLogs />}
      {configPages[page] && <AutoConfigPage {...configPages[page]} />}
      {page === 'afterSaleService' && (
        <AgreementPage code="afterSalesService" title="售后服务" description="配置售后服务协议内容与启用状态。" />
      )}
      {page === 'orders' && <Orders />}
      {page === 'members' && <Members />}
      {page === 'content' && <Content />}
      {!knownPages.has(page) && <Placeholder page={page} />}
    </AdminLayout>
  );
}
