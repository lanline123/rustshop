import { useEffect, useState } from 'react';
import { apiGet, clearToken, enterDemo, getToken, isDemo } from './api';
import { applyAppearance, type AppearanceSettings } from './theme';
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
import { RefundApplyList, WithdrawApplyList, RechargeOrderList, UserInvoiceList, OrderInvoiceList } from './pages/FinanceLists';
import Placeholder from './pages/Placeholder';
import { pageTitles } from './config/adminMenu';
import { configSections } from './config/configSections';
import { configHelp } from './config/configHelp';

type ConfigPageDef = {
  title: string;
  getApi: string;
  saveApi: string;
  sections?: Array<{ title: string; fields: string[] }>;
  rules?: Record<string, ConfigFieldRule>;
  fieldGroups?: ConfigFieldGroup[];
  controls?: Record<string, 'radio' | 'switch' | 'segmented' | 'color' | 'options'>;
  options?: Record<string, Array<{ label: string; value: string | number; color?: string }>>;
  hiddenFields?: string[];
  fieldHelp?: Record<string, string>;
};

const configPages: Record<string, ConfigPageDef> = {
  baseBasicManage: { title: '基础信息', getApi: '/setting/config/basicSettings', saveApi: '/setting/config/saveBasic' },
  baseProductManage: { title: '商品设置', getApi: '/setting/config/productSettings', saveApi: '/setting/config/saveProduct' },
  baseShoppingManage: { title: '交易设置', getApi: '/setting/config/shoppingSettings', saveApi: '/setting/config/saveShopping' },
  baseOrderManage: {
    title: '订单设置',
    getApi: '/setting/config/orderSettings',
    saveApi: '/setting/config/saveOrder',
    rules: {
      autoReturnGoods: { visibleWhen: { field: 'afterSalesReview', equals: 1 } },
      autoReturnGoodsDays: { disabledWhen: { field: 'autoReturnGoods', equals: 0 } },
    },
  },
  kefuSetting: { title: '客服设置', getApi: '/setting/config/kefuSettings', saveApi: '/setting/config/saveKefu' },
  globalSetting: {
    title: '全局设置',
    getApi: '/setting/config/globalSettings',
    saveApi: '/setting/config/saveGlobal',
    controls: {
      navTheme: 'segmented',
      primaryColor: 'color',
      layout: 'segmented',
      versionInfoHidden: 'radio',
      storageType: 'options',
    },
    options: {
      navTheme: [
        { label: '浅色', value: 'light' },
        { label: '深色', value: 'dark' },
      ],
      primaryColor: [
        { label: '蓝色', value: 'blue', color: '#1456f0' },
        { label: '红色', value: 'red', color: '#f5222d' },
        { label: '橙色', value: 'orange', color: '#faad14' },
        { label: '绿色', value: 'green', color: '#52c41a' },
        { label: '紫色', value: 'purple', color: '#722ed1' },
      ],
      layout: [
        { label: '默认', value: 'default' },
        { label: '侧边菜单', value: 'side' },
        { label: '顶部菜单', value: 'top' },
        { label: '混合菜单', value: 'mix' },
      ],
      storageType: [
        { label: '本地存储', value: 0 },
        { label: '阿里云 OSS', value: 1 },
        { label: '腾讯云 COS', value: 2 },
        { label: '七牛云 KODO', value: 3 },
        { label: 'AWS S3', value: 4 },
      ],
    },
    hiddenFields: ['storageSaveFullPath'],
    fieldHelp: {
      storageType: '开启云存储后，图片会直接通过接口上传至指定云存储，不会再保存在本地。',
      storageLocalUrl: '参考格式： https://oss.***.com/ ，域名结尾必须带斜杠 /',
      storageOssUrl: '参考格式： https://oss.***.com/ ，域名结尾必须带斜杠 /',
      storageCosUrl: '参考格式： https://oss.***.com/ ，域名结尾必须带斜杠 /',
      storageQiniuUrl: '参考格式： https://oss.***.com/ ，域名结尾必须带斜杠 /',
      storageS3Url: '参考格式： https://oss.***.com/ ，Bucket URL 结尾必须带斜杠 /',
      storageOssAccessKeyId: '编辑完成后，点击下方提交按钮确认修改，请确保填写正确的 AccessKeyId',
      storageOssAccessKeySecret: '编辑完成后，点击下方提交按钮确认修改，请确保填写正确的 AccessKeySecret',
      storageCosSecretId: '编辑完成后，点击下方提交按钮确认修改，请确保填写正确的 SecretId',
      storageCosSecretKey: '编辑完成后，点击下方提交按钮确认修改，请确保填写正确的 KeySecret',
      storageQiniuAccessKey: '编辑完成后，点击下方提交按钮确认修改，请确保填写正确的 AccessKey',
      storageQiniuSecretKey: '编辑完成后，点击下方提交按钮确认修改，请确保填写正确的 SecretKey',
      storageS3AccessKey: '编辑完成后，点击下方提交按钮确认修改，请确保填写正确的 Access key ID',
      storageS3SecretKey: '编辑完成后，点击下方提交按钮确认修改，请确保填写正确的 Secret access key',
    },
    rules: {
      storageLocalUrl: { visibleWhen: { field: 'storageType', equals: 0 } },
      storageOssUrl: { visibleWhen: { field: 'storageType', equals: 1 } },
      storageOssAccessKeyId: { visibleWhen: { field: 'storageType', equals: 1 } },
      storageOssAccessKeySecret: { visibleWhen: { field: 'storageType', equals: 1 } },
      storageOssBucket: { visibleWhen: { field: 'storageType', equals: 1 } },
      storageOssRegion: { visibleWhen: { field: 'storageType', equals: 1 } },
      storageCosUrl: { visibleWhen: { field: 'storageType', equals: 2 } },
      storageCosSecretId: { visibleWhen: { field: 'storageType', equals: 2 } },
      storageCosSecretKey: { visibleWhen: { field: 'storageType', equals: 2 } },
      storageCosBucket: { visibleWhen: { field: 'storageType', equals: 2 } },
      storageCosRegion: { visibleWhen: { field: 'storageType', equals: 2 } },
      storageQiniuUrl: { visibleWhen: { field: 'storageType', equals: 3 } },
      storageQiniuAccessKey: { visibleWhen: { field: 'storageType', equals: 3 } },
      storageQiniuSecretKey: { visibleWhen: { field: 'storageType', equals: 3 } },
      storageQiniuBucket: { visibleWhen: { field: 'storageType', equals: 3 } },
      storageQiniuRegion: { visibleWhen: { field: 'storageType', equals: 3 } },
      storageS3Url: { visibleWhen: { field: 'storageType', equals: 4 } },
      storageS3AccessKey: { visibleWhen: { field: 'storageType', equals: 4 } },
      storageS3SecretKey: { visibleWhen: { field: 'storageType', equals: 4 } },
      storageS3Bucket: { visibleWhen: { field: 'storageType', equals: 4 } },
      storageS3Region: { visibleWhen: { field: 'storageType', equals: 4 } },
      storageS3Endpoint: { visibleWhen: { field: 'storageType', equals: 4 } },
    },
  },
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
        icon: '微',
        fields: ['wechatAppId', 'wechatAppSecret', 'wechatServerToken', 'wechatServerSecret'],
        visibleWhen: { field: 'openWechatOauth', equals: 1 },
      },
    ],
  },
  storeAmapSetting: { title: '地图设置', getApi: '/setting/config/mapSettings', saveApi: '/setting/config/saveMapSettings' },
  personalizedManage: { title: '个性化设置', getApi: '/setting/config/getPersonalizedSetting', saveApi: '/setting/config/savePersonalizedSetting' },
  baseNoticeManage: { title: '通知设置', getApi: '/setting/config/notifySettings', saveApi: '/setting/config/saveNotify' },
};

Object.entries(configPages).forEach(([key, page]) => {
  page.sections = configSections[key];
  page.fieldHelp = { ...configHelp[key], ...page.fieldHelp };
});

const knownPages = new Set(['dashboard', 'products', 'brandManage', 'categoryManage', 'productGroupManage', 'productServicesManage', 'commentManage', 'productAttributesTplManage', 'friendLinksManage', 'regionManage', 'logisticsCompanyManage', 'appVersionManage', 'adminLogManage', 'adminUserManage', 'adminRoleManage', 'serviceAgreement', 'privacyAgreement', 'afterSaleService', 'accountManage', 'taxRateManage', 'shippingTypeManage', 'logisticsManage', 'logisticsCompanyConfigManage', 'payLogManage', 'refundLogManage', 'userBalanceLogManage', 'staffFundWalletLogManage', 'buyerShowManage', 'integralLogManage', 'refundApplyManage', 'userWithdrawApplyManage', 'userRechargeOrderManage', 'userInvoiceManage', 'orderInvoiceManage', 'baseBasicManage', 'baseProductManage', 'baseShoppingManage', 'baseOrderManage', 'kefuSetting', 'globalSetting', 'loginSetting', 'storeAmapSetting', 'personalizedManage', 'baseNoticeManage', 'orders', 'members', 'content']);

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
  const [appearance, setAppearance] = useState<AppearanceSettings>({
    navTheme: 'light',
    primaryColor: 'blue',
    layout: 'default',
  });

  const applyAndStore = (values: AppearanceSettings) => {
    const next = {
      navTheme: values.navTheme || 'light',
      primaryColor: values.primaryColor || 'blue',
      layout: values.layout || 'default',
      adminLightLogo: values.adminLightLogo || '',
    };
    setAppearance(next);
    applyAppearance(next);
  };

  useEffect(() => {
    if (!authed) return;
    apiGet<AppearanceSettings>('/setting/config/globalSettings')
      .then((result) => applyAndStore(result || {}))
      .catch(() => applyAppearance({}));
  }, [authed]);

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
      layout={appearance.layout}
      logo={appearance.adminLightLogo}
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
      {page === 'refundApplyManage' && <RefundApplyList />}
      {page === 'userWithdrawApplyManage' && <WithdrawApplyList />}
      {page === 'userRechargeOrderManage' && <RechargeOrderList />}
      {page === 'userInvoiceManage' && <UserInvoiceList />}
      {page === 'orderInvoiceManage' && <OrderInvoiceList />}
      {configPages[page] && (
        <AutoConfigPage
          {...configPages[page]}
          onSaved={page === 'globalSetting' ? (values) => applyAndStore(values) : undefined}
        />
      )}
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
