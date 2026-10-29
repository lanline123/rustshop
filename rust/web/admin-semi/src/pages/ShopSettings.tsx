import type { ReactNode } from 'react';
import { Checkbox } from '@douyinfe/semi-ui';
import AutoConfigPage, {
  type ConfigControlType,
  type ConfigFieldRule,
  type ConfigRendererContext,
} from './AutoConfigPage';

// 旧版为反向勾选：勾选"商品默认审核通过"时 shopProductNeedCheck=0（免审核），不勾选=1（需审核）。
const invertedCheckRenderer = (field: string, label: string) =>
  ({ value, update }: ConfigRendererContext) => (
    <Checkbox checked={Number(value) === 0} onChange={(next) => update(field, next ? 0 : 1)}>
      {label}
    </Checkbox>
  );

const def: {
  title: string;
  getApi: string;
  saveApi: string;
  sections: Array<{ title: string; fields: string[] }>;
  labels: Record<string, string>;
  controls: Record<string, ConfigControlType>;
  options: Record<string, Array<{ label: string; value: string | number; color?: string }>>;
  rules: Record<string, ConfigFieldRule>;
  fieldHelp: Record<string, string>;
  hiddenFields: string[];
  defaults: Record<string, unknown>;
  renderers: Record<string, (context: ConfigRendererContext) => ReactNode>;
} = {
  title: '店铺设置',
  getApi: '/setting/config/shopSettings',
  saveApi: '/setting/config/saveShop',
  sections: [
    {
      title: '基本设置',
      fields: ['shopProductNeedCheck', 'maxRecommendProductCount', 'maxSubAdministrator', 'defaultShopName'],
    },
  ],
  labels: {
    shopProductNeedCheck: '店铺商品审核',
    maxRecommendProductCount: '推荐商品数量',
    maxSubAdministrator: '最大管理员数',
    defaultShopName: '店铺名称',
  },
  controls: {},
  options: {},
  rules: {},
  fieldHelp: {
    shopProductNeedCheck: '店铺发布商品的时候是否需要审核才能显示商品',
    maxRecommendProductCount: '商城中新品、推荐列表显示的商品数量',
    maxSubAdministrator: '当前店铺可配置的最大子管理员数',
    defaultShopName: '当商品没有归属店铺时商品卡片展示的自定义店铺名称，默认：官方自营店',
  },
  hiddenFields: [],
  defaults: {
    shopProductNeedCheck: 1,
    maxSubAdministrator: 5,
  },
  renderers: {
    shopProductNeedCheck: invertedCheckRenderer('shopProductNeedCheck', '商品默认审核通过'),
  },
};

export default function ShopSettings() {
  return (
    <AutoConfigPage
      title={def.title}
      getApi={def.getApi}
      saveApi={def.saveApi}
      sections={def.sections}
      labels={def.labels}
      controls={def.controls}
      options={def.options}
      rules={def.rules}
      fieldHelp={def.fieldHelp}
      hiddenFields={def.hiddenFields}
      defaults={def.defaults}
      renderers={def.renderers}
    />
  );
}
