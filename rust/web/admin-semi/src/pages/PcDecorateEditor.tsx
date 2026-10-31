/**
 * PC 首页装修编辑器（对齐原版 5.8.61 新版模块集 modules/pcHome，/decorate/pc 独立编辑器）
 *
 * 数据契约（与原版逐字段一致，透传不裁剪）：
 * - data = { pageModule, moduleList }
 * - pageModule = { type:'page', module:{}, backgroundRepeat:'', backgroundSize:'', style:0,
 *                  headerStyle:1, homeStyleVersion:'new'|'old', pcHeader?(仅新版) }
 * - moduleList 项 = { moduleIndex: Date.now(), type, url:'pc', label, isShow, module:{...defaults} }
 * - 新套 20 模块 kebab-case（pc-home-hero / pc-image-ad / ...），旧套 16 模块 camelCase 仅兼容历史数据
 * - 加载: GET decorate/decorate/detail?id&decorateType=2&localeId=0&parentId=0
 * - 保存: POST decorate/decorate/saveDraft | publish  body {id, decorateType:2, pageType:1, localeId:0, parentId:0, data}
 */
import { useEffect, useRef, useState, type ReactNode } from 'react';
import {
  Button,
  Input,
  InputNumber,
  Modal,
  Radio,
  RadioGroup,
  Select,
  Slider,
  Space,
  Spin,
  Switch,
  Tag,
  TextArea,
  Toast,
  Tooltip,
  Typography,
} from '@douyinfe/semi-ui';
import {
  IconArrowLeft,
  IconChevronDown,
  IconChevronUp,
  IconClose,
  IconCopy,
  IconDelete,
  IconPlus,
  IconRefresh,
} from '@douyinfe/semi-icons';
import { apiGet, apiPost } from '../api';
import './decorate-editor.css';

const { Text } = Typography;

/* ===================== 共享默认值构造（原版 _shared/types.ts 逐字段） ===================== */

interface PicItem {
  picId?: string;
  picUrl: string;
  picThumb?: string;
  picName?: string;
  picTitle?: string;
  picDesc?: string;
  picLink?: { link?: string; name?: string; title?: string };
}

const DEMO_PIC_1 = 'https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp';
const DEMO_PIC_2 = 'https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp';
const NOTICE_ICON = 'https://oss.vortmall.com/upload/c4ca4238/img/gallery/202508/1754895669bP3iJSUiXSBoRB1VQq.png';

const demoPic = (id: string, url: string, name: string, title: string): PicItem => ({
  picId: id,
  picUrl: url,
  picThumb: url,
  picName: name,
  picTitle: title,
  picLink: { link: '', name: '' },
});

const standardFrame = (extra: Record<string, unknown> = {}) => ({
  textColor: '',
  itemBackgroundColor: '',
  backgroundColor: '#ffffff',
  innerPadding: 0,
  itemHeight: 35,
  itemRadius: 3,
  boxRadius: 0,
  boxPadding: 10,
  leftAndRightPadding: 0,
  boxPaddingTop: 5,
  boxPaddingBottom: 5,
  ...extra,
});

const standardTitle = (extra: Record<string, unknown> = {}) => ({
  showTitle: 0,
  titleStyle: 1,
  titleAlign: 1,
  titleBackground: '',
  titleBackground2: '',
  titleBackgroundPic: { picUrl: '', picThumb: '' },
  titleRadius: 0,
  titlePaddingTop: 0,
  titlePaddingBottom: 0,
  titleText: '标题内容',
  titleColor: '',
  descText: '',
  descColor: '#aaaaaa',
  showMore: 0,
  moreType: 1,
  countdownLabel: '',
  moreLink: { link: '', title: '' },
  moreColor: '#aaaaaa',
  ...extra,
});

const productsDefaults = () => ({
  productSelectType: 1,
  productIds: [] as number[],
  productCategoryId: undefined as unknown as number,
  productTag: '',
  productNumber: 10,
});

/** 快捷导航演示项（原版 defaultItems 逐字段） */
const QUICK_NAV_ITEMS: PicItem[] = [
  ['1377', '1730957947YV2lncqLSOSg8OvpbK', '当日速达'],
  ['1384', '1730957948LcXRbvCuwlCHyHy7Y0', '休闲零食'],
  ['1378', '1730957947XV6cPNonZJurGcDfOP', '粮油米面'],
  ['1385', '17309579481I5gopEn7PkOneF0NU', '乳饮酒水'],
  ['1382', '1730957948Ouz6wPq8cQkgSjDCkF', '日用百货'],
  ['1380', '1730957947iOLEngIwvzxZDjhtQ6', '母婴用品'],
  ['1379', '1730957947lWqY7UySCwQ89qsq7f', '个人洗护'],
  ['1383', '1730957948vFCfxjt0TIROPOocWI', '纸品家清'],
  ['1376', '1730957947BswaPOfoOZnn8tDSX6', '护肤美容'],
  ['1381', '1730957947vIKQibO98m2q9oP8ct', '方便速食'],
].map(([picId, hash, picTitle]) => ({
  picId,
  picThumb: `https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/${hash}.png?x-oss-process=image/resize,m_pad,h_200,h_200`,
  picUrl: `https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/${hash}.png`,
  picName: picId,
  picTitle,
  picLink: { link: '', name: '' },
}));

const HERO_CATEGORIES: PicItem[] = [
  ['1377', '1730957947YV2lncqLSOSg8OvpbK', '女装 / 男装 / 内衣'],
  ['1384', '1730957948LcXRbvCuwlCHyHy7Y0', '鞋包 / 女靴 / 男鞋'],
  ['1378', '1730957947XV6cPNonZJurGcDfOP', '美妆 / 养护 / 美容'],
  ['1385', '17309579481I5gopEn7PkOneF0NU', '冰洗 / 空调 / 电视 / 厨卫大件'],
  ['1382', '1730957948Ouz6wPq8cQkgSjDCkF', '电脑 / 办公 / 文具用品'],
  ['1380', '1730957947iOLEngIwvzxZDjhtQ6', '食品 / 酒类 / 生鲜 / 特产'],
  ['1379', '1730957947lWqY7UySCwQ89qsq7f', '生活电器 / 厨房小电 / 个护健康'],
  ['1383', '1730957948vFCfxjt0TIROPOocWI', '医药 / 器械 / 保健品'],
  ['1376', '1730957947BswaPOfoOZnn8tDSX6', '配饰 / 手表 / 玉石'],
  ['1381', '1730957947vIKQibO98m2q9oP8ct', '家装 / 厨卫 / 家具'],
  ['1386', '1730957947YV2lncqLSOSg8OvpbK', '汽车 / 摩托 / 配件'],
].map(([picId, hash, picTitle]) => ({
  picId,
  picThumb: `https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/${hash}.png?x-oss-process=image/resize,m_pad,h_200,h_200`,
  picUrl: `https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/${hash}.png`,
  picName: picId,
  picTitle,
  picDesc: '',
  picLink: { link: '', name: '' },
}));

const HERO_DEMO_PRODUCTS = [
  '1732004872pw39D1oGqdAaZMpvyo',
  '1732004872f6EcZnbAqdW5kIGzxL',
  '1732004872o3s8aqPGdICr1DOCdB',
  '1732004872Mry8L9BWOCiawPhp72',
  '1732004872yPYr4zSj7AXrfw9oON',
  '1732004872NlX49KeGeYSN4Tw0wa',
].map((hash, i) => ({
  pic: {
    picUrl: `https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/${hash}.jpg`,
    picThumb: `https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/${hash}.jpg`,
  },
  price: ['¥88.8', '¥56.8', '¥48.8', '¥48.8', '¥32.0', '¥32.0'][i],
}));

const heroDemoModules = (): Array<Record<string, any>> => {
  const defs: Array<[string, string, string, string]> = [
    ['限时秒杀', '钜惠开秒', '#FF4E4E', '#E1251B'],
    ['商家严选', '品质好物', '#FF4E4E', '#E1251B'],
    ['严选臻品', '优中选优', '#6C8CFF', '#3A5BFF'],
    ['新品上市', '重磅上新', '#00C176', '#008856'],
    ['领券中心', '券', '#FF4E4E', '#E1251B'],
    ['组团拼单', '拼享低价', '#FF9000', '#FF5000'],
  ];
  const picks: number[][] = [[2, 3], [4, 5], [0, 1], [2, 3], [4, 5], [0, 1]];
  return defs.map(([moduleTitle, moduleSubTitle, a, b], i) => ({
    moduleTitle,
    moduleSubTitle,
    moduleSubTitleBgColorA: a,
    moduleSubTitleBgColorB: b,
    moduleSubTitleBgType: 'leftRight',
    showPrice: 1,
    link: { link: '', name: '' },
    products: picks[i].map((p) => ({ ...HERO_DEMO_PRODUCTS[p] })),
  }));
};

/* ===================== 设置字段定义 ===================== */

interface SettingFieldDef {
  kind:
    | 'radio' | 'switch' | 'select' | 'input' | 'textarea' | 'number' | 'slider'
    | 'color' | 'picList' | 'pic' | 'link' | 'linkList'
    | 'category' | 'categoryMulti' | 'products' | 'productIds' | 'groupon'
    | 'title' | 'container' | 'hotarea' | 'groupList' | 'heroCards';
  key: string;
  label: string;
  hint?: string;
  options?: Array<{ label: string; value: number | string }>;
  max?: number;
  min?: number;
  numberMin?: number;
  numberMax?: number;
  numberStep?: number;
  /** container 字段的子结构类型 */
  containerType?: 'frame' | 'buyBtn' | 'productColor' | 'groupTitle' | 'moduleStyle' | 'contentStyle';
  /** title 字段是否隐藏对齐选项（原版 hide-title-align） */
  titleHideAlign?: boolean;
  /** 字段写入后的附加行为（如切换拼团类型时清空活动） */
  onChangeExtra?: (moduleData: Record<string, any>, newValue: unknown) => Record<string, any>;
  showIf?: (m: Record<string, any>) => boolean;
}

interface ModuleDef {
  type: string;
  label: string;
  group: string;
  overseasOnly?: boolean;
  hiddenInLibrary?: boolean;
  legacy?: boolean;
  note?: string;
  defaults: () => Record<string, any>;
  settings: SettingFieldDef[];
  summary?: (m: Record<string, any>) => string;
}

/** 深层取值（'title.titleText' 形式） */
function getPath(obj: Record<string, any>, path: string): any {
  return path.split('.').reduce((acc, key) => (acc == null ? undefined : acc[key]), obj);
}

/** 深层写入（不可变） */
function setPath(obj: Record<string, any>, path: string, value: unknown): Record<string, any> {
  const keys = path.split('.');
  const clone: Record<string, any> = { ...obj };
  let cur = clone;
  for (let i = 0; i < keys.length - 1; i++) {
    const k = keys[i];
    cur[k] = { ...(cur[k] || {}) };
    cur = cur[k];
  }
  cur[keys[keys.length - 1]] = value;
  return clone;
}

const containerFields = (opts: { showInnerPadding?: boolean } = {}): SettingFieldDef[] => [
  { kind: 'color', key: 'frame.backgroundColor', label: '背景颜色' },
  { kind: 'slider', key: 'frame.boxRadius', label: '容器圆角', max: 30 },
  { kind: 'slider', key: 'frame.boxPadding', label: '左右边距', max: 30 },
  { kind: 'slider', key: 'frame.boxPaddingTop', label: '上边距', max: 30 },
  { kind: 'slider', key: 'frame.boxPaddingBottom', label: '下边距', max: 30 },
  ...(opts.showInnerPadding
    ? [{ kind: 'slider' as const, key: 'frame.leftAndRightPadding', label: '左右内边距', max: 30 }]
    : []),
];

const titleFields = (opts: { moreType?: boolean; verticalPadding?: boolean; hideAlign?: boolean } = {}): SettingFieldDef[] => [
  { kind: 'switch', key: 'title.showTitle', label: '显示标题' },
  { kind: 'radio', key: 'title.titleStyle', label: '标题栏风格', options: [{ label: '样式一', value: 1 }, { label: '样式二', value: 2 }, { label: '样式三', value: 3 }], showIf: (m) => Number(getPath(m, 'title.showTitle')) === 1 },
  { kind: 'radio', key: 'title.titleAlign', label: '标题位置', options: [{ label: '居左', value: 1 }, { label: '居中', value: 2 }, { label: '居右', value: 3 }], showIf: (m) => Number(getPath(m, 'title.showTitle')) === 1 && !opts.hideAlign },
  { kind: 'input', key: 'title.titleText', label: '标题内容', showIf: (m) => Number(getPath(m, 'title.showTitle')) === 1 },
  { kind: 'color', key: 'title.titleColor', label: '标题颜色', showIf: (m) => Number(getPath(m, 'title.showTitle')) === 1 },
  { kind: 'color', key: 'title.titleBackground', label: '标题背景色', showIf: (m) => Number(getPath(m, 'title.showTitle')) === 1 },
  { kind: 'slider', key: 'title.titleRadius', label: '标题圆角', max: 30, showIf: (m) => Number(getPath(m, 'title.showTitle')) === 1 },
  { kind: 'input', key: 'title.descText', label: '描述内容', showIf: (m) => Number(getPath(m, 'title.showTitle')) === 1 },
  { kind: 'color', key: 'title.descColor', label: '描述颜色', showIf: (m) => Number(getPath(m, 'title.showTitle')) === 1 },
  ...(opts.moreType
    ? [
        { kind: 'radio' as const, key: 'title.moreType', label: '右侧内容', options: [{ label: '更多链接', value: 1 }, { label: '倒计时', value: 2 }], showIf: (m: Record<string, any>) => Number(getPath(m, 'title.showTitle')) === 1 },
        { kind: 'input' as const, key: 'title.countdownLabel', label: '倒计时前缀文字', showIf: (m: Record<string, any>) => Number(getPath(m, 'title.showTitle')) === 1 && Number(getPath(m, 'title.moreType')) === 2 },
      ]
    : []),
  { kind: 'switch', key: 'title.showMore', label: '显示查看更多', showIf: (m) => Number(getPath(m, 'title.showTitle')) === 1 && (!opts.moreType || Number(getPath(m, 'title.moreType')) === 1) },
  { kind: 'link', key: 'title.moreLink', label: '更多链接', showIf: (m) => Number(getPath(m, 'title.showTitle')) === 1 && Number(getPath(m, 'title.showMore')) === 1 && (!opts.moreType || Number(getPath(m, 'title.moreType')) === 1) },
  ...(opts.verticalPadding
    ? [
        { kind: 'slider' as const, key: 'title.titlePaddingTop', label: '标题上内边距', max: 60 },
        { kind: 'slider' as const, key: 'title.titlePaddingBottom', label: '标题下内边距', max: 60 },
      ]
    : []),
];

/* ===================== 新版模块集（modules/pcHome 20 个，kebab-case） ===================== */

const NEW_MODULE_DEFS: ModuleDef[] = [
  {
    type: 'pc-banner2', label: '轮播Banner2', group: '轮播横幅', overseasOnly: true,
    defaults: () => ({
      frame: { boxPadding: 10, boxPaddingTop: 0, boxPaddingBottom: 0, boxRadius: 8, backgroundColor: '#eeeeee' },
      bannerStyle: 1, showCat: 1, logo: { picUrl: '', picThumb: '' },
      picList: [demoPic('1', DEMO_PIC_1, '演示图片1', 'Banner1'), demoPic('2', DEMO_PIC_2, '演示图片2', 'Banner2')],
      picList2: [], picList3: [], number: 1, slideGap: 12,
    }),
    settings: [
      { kind: 'picList', key: 'picList', label: '轮播图', hint: '建议 1264*280' },
      ...containerFields(),
    ],
    summary: (m) => `${((m.picList || []) as PicItem[]).length} 图`,
  },
  {
    type: 'pc-group-banner', label: '分组轮播Banner', group: '轮播横幅', overseasOnly: true,
    defaults: () => ({
      frame: { backgroundColor: '#ffffff', boxPadding: 10, boxPaddingTop: 0, boxPaddingBottom: 0, boxRadius: 8, slideHeight: 195 },
      bannerStyle: 1, showCat: 1, logo: { picUrl: '', picThumb: '' },
      picList: [demoPic('1', DEMO_PIC_1, '演示图片1', 'Banner1'), demoPic('2', DEMO_PIC_2, '演示图片2', 'Banner2')],
      picList2: [], picList3: [], number: 3,
    }),
    settings: [
      { kind: 'picList', key: 'picList', label: 'Banner图', hint: '1180*940 / 750*479' },
      { kind: 'number', key: 'number', label: '一组显示的数量', min: 1, max: 3, hint: '最多 3 个' },
      ...containerFields(),
    ],
    summary: (m) => `每组 ${m.number || '-'} 个 · ${((m.picList || []) as PicItem[]).length} 图`,
  },
  {
    type: 'pc-image-ad', label: '图片广告', group: '轮播横幅',
    defaults: () => ({
      picType: 1,
      picList: [demoPic('1', DEMO_PIC_1, '演示图片1', '广告位1'), demoPic('2', DEMO_PIC_2, '演示图片2', '广告位2')],
      swiperPreView: 1, swiperPageColor: '#333333', imgPadding: 0, picPageType: 1, picRadioStyle: 1, isFluxWidth: 0,
      frame: standardFrame(),
      title: standardTitle(),
    }),
    settings: [
      { kind: 'radio', key: 'picType', label: '选择模板', options: [{ label: '一行一个', value: 1 }, { label: '轮播', value: 2 }, { label: '一行两个', value: 5 }, { label: '一行三个', value: 6 }, { label: '一行四个', value: 7 }] },
      { kind: 'picList', key: 'picList', label: '广告图' },
      { kind: 'radio', key: 'picPageType', label: '指示器样式', options: [{ label: '样式一', value: 1 }, { label: '样式二', value: 2 }, { label: '样式三', value: 3 }], showIf: (m) => Number(m.picType) === 2 },
      { kind: 'color', key: 'swiperPageColor', label: '指示器颜色', showIf: (m) => Number(m.picType) === 2 },
      { kind: 'select', key: 'swiperPreView', label: '轮播一行显示', options: [1, 2, 3, 4].map((n) => ({ label: `${n} 个`, value: n })), showIf: (m) => Number(m.picType) === 2 },
      { kind: 'slider', key: 'imgPadding', label: '图片边距', max: 30 },
      { kind: 'radio', key: 'picRadioStyle', label: '图片边角', options: [{ label: '直角', value: 1 }, { label: '圆角', value: 2 }] },
      { kind: 'title', key: 'title', label: '标题设置' },
      { kind: 'radio', key: 'isFluxWidth', label: '通宽排版', options: [{ label: '通宽', value: 1 }, { label: '居中', value: 0 }] },
      ...containerFields(),
    ],
    summary: (m) => `模板${m.picType} · ${((m.picList || []) as PicItem[]).length} 图`,
  },
  {
    type: 'pc-image-nav', label: '图文导航', group: '导航分类',
    defaults: () => ({
      navType: 1, navStyle: 1, rowNum: 4, colNum: 2, picList: [],
      backgroundColor: '#ffffff', imgPadding: 8, radioStyle: 1, picPageType: 1, swiperPageColor: '#333333',
      frame: standardFrame(),
      title: standardTitle(),
    }),
    settings: [
      { kind: 'radio', key: 'navType', label: '显示模式', options: [{ label: '固定', value: 1 }, { label: '滑动分页', value: 2 }] },
      { kind: 'radio', key: 'navStyle', label: '样式', options: [{ label: '图文', value: 1 }, { label: '仅图片', value: 2 }, { label: '仅文字', value: 3 }] },
      { kind: 'select', key: 'rowNum', label: '一行显示', options: [3, 4, 5, 6].map((n) => ({ label: `${n} 个`, value: n })) },
      { kind: 'select', key: 'colNum', label: '显示行数', options: [1, 2, 3].map((n) => ({ label: `${n} 行`, value: n })) },
      { kind: 'picList', key: 'picList', label: '导航项' },
      { kind: 'color', key: 'backgroundColor', label: '背景颜色' },
      { kind: 'slider', key: 'imgPadding', label: '元素边距', max: 30 },
      { kind: 'radio', key: 'radioStyle', label: '图片边角', options: [{ label: '直角', value: 1 }, { label: '圆角', value: 2 }], showIf: (m) => Number(m.navStyle) === 2 },
      { kind: 'radio', key: 'picPageType', label: '指示器样式', options: [{ label: '样式一', value: 1 }, { label: '样式二', value: 2 }], showIf: (m) => Number(m.navType) === 2 },
      { kind: 'color', key: 'swiperPageColor', label: '指示器颜色', showIf: (m) => Number(m.navType) === 2 },
      { kind: 'title', key: 'title', label: '标题设置' },
      ...containerFields(),
    ],
    summary: (m) => `${m.rowNum}×${m.colNum} · ${((m.picList || []) as PicItem[]).length} 项`,
  },
  {
    type: 'pc-image-square-ad', label: '图片魔方', group: '图片展示',
    defaults: () => ({
      picType: 1,
      picList: [demoPic('1', DEMO_PIC_1, '演示图片1', '魔方图1'), demoPic('2', DEMO_PIC_2, '演示图片2', '魔方图2')],
      imgPadding: 0,
      frame: standardFrame(),
      title: standardTitle(),
    }),
    settings: [
      { kind: 'radio', key: 'picType', label: '选择布局', options: [{ label: '一行两个', value: 1 }, { label: '一行三个', value: 2 }, { label: '一行四个', value: 3 }, { label: '左二右二', value: 4 }, { label: '左一右二', value: 5 }, { label: '左一右三', value: 6 }, { label: '上一下二', value: 7 }] },
      { kind: 'picList', key: 'picList', label: '魔方图', hint: '槽位数量由布局决定' },
      { kind: 'slider', key: 'imgPadding', label: '图片边距', max: 30 },
      { kind: 'title', key: 'title', label: '标题设置' },
      ...containerFields({ showInnerPadding: true }),
    ],
    summary: (m) => `布局${m.picType} · ${((m.picList || []) as PicItem[]).length} 图`,
  },
  {
    type: 'pc-image-hotarea', label: '热图绘制', group: '图片展示',
    defaults: () => ({
      picList: [] as PicItem[],
      imgPadding: 0,
      frame: standardFrame(),
      title: standardTitle(),
    }),
    settings: [
      { kind: 'picList', key: 'picList', label: '热图' },
      { kind: 'hotarea', key: 'hotarea', label: '热区（宽/高/上/左 为像素，基准 500px 宽）' },
      { kind: 'slider', key: 'imgPadding', label: '图片边距', max: 30 },
      { kind: 'title', key: 'title', label: '标题设置' },
      ...containerFields({ showInnerPadding: true }),
    ],
    summary: (m) => `${((m.picList || []) as PicItem[]).length} 图`,
  },
  {
    type: 'pc-home-quick-nav', label: '快捷导航', group: '导航分类',
    defaults: () => ({
      frame: { boxPadding: 0, boxPaddingTop: 0, boxPaddingBottom: 0, backgroundColor: '' },
      style: { fontSize: 14, textColor: '#333333', hoverColor: '#e1251b', containerAlign: 2, itemGap: 16, singleLine: 1, fontWeight: 700, lineHeight: 36, iconSize: 18, iconGap: 6 },
      items: QUICK_NAV_ITEMS,
    }),
    settings: [
      { kind: 'picList', key: 'items', label: '导航项', hint: '图标+文字+链接，可排序' },
      { kind: 'slider', key: 'style.fontSize', label: '字号', min: 12, max: 24 },
      { kind: 'radio', key: 'style.fontWeight', label: '文字粗细', options: [{ label: '常规', value: 400 }, { label: '加粗', value: 700 }] },
      { kind: 'slider', key: 'style.lineHeight', label: '行高', min: 12, max: 40 },
      { kind: 'color', key: 'style.textColor', label: '文字颜色' },
      { kind: 'color', key: 'style.hoverColor', label: '悬停颜色' },
      { kind: 'slider', key: 'style.iconSize', label: '图标尺寸', min: 12, max: 32 },
      { kind: 'slider', key: 'style.iconGap', label: '图文间距', min: 0, max: 16 },
      { kind: 'radio', key: 'style.containerAlign', label: '整体排布', options: [{ label: '居左', value: 1 }, { label: '居中', value: 2 }, { label: '居右', value: 3 }] },
      { kind: 'slider', key: 'style.itemGap', label: '项目间距', min: 8, max: 60 },
      { kind: 'radio', key: 'style.singleLine', label: '单行模式', options: [{ label: '单行', value: 1 }, { label: '自动换行', value: 0 }] },
      ...containerFields(),
    ],
    summary: (m) => `${((m.items || []) as PicItem[]).length} 项`,
  },
  {
    type: 'pc-category-nav', label: '分类导航', group: '导航分类',
    defaults: () => ({
      frame: { boxPadding: 0, boxPaddingTop: 0, boxPaddingBottom: 0, boxRadius: 8, backgroundColor: '#f7f7f7', boxHeight: 328 },
      titleName: '热门分类', showTitle: 1, displayNum: 6, backgroundColor: '#f7f7f7',
    }),
    settings: [
      { kind: 'switch', key: 'showTitle', label: '显示标题' },
      { kind: 'input', key: 'titleName', label: '标题', showIf: (m) => Number(m.showTitle) === 1 },
      { kind: 'slider', key: 'displayNum', label: '一屏显示数量', min: 1, max: 10 },
      { kind: 'color', key: 'backgroundColor', label: '背景颜色' },
      ...containerFields(),
    ],
    summary: (m) => `${m.titleName || ''} · 每屏 ${m.displayNum}`,
  },
  {
    type: 'pc-category-nav3', label: '分类导航3', group: '导航分类', overseasOnly: true,
    defaults: () => ({
      frame: { boxPadding: 0, boxPaddingTop: 0, boxPaddingBottom: 0, boxRadius: 8, backgroundColor: 'rgba(0, 0, 0, 0.02)', boxHeight: 352 },
      titleText: '热门推荐', showMore: 1, showMoreText: '更多', moreLink: { link: '', name: '' },
      imageBackgroundColor: '', picList: [], backgroundColor: 'rgba(0, 0, 0, 0.02)',
    }),
    settings: [
      { kind: 'picList', key: 'picList', label: '图片卡片', hint: '建议 180*180' },
      { kind: 'color', key: 'imageBackgroundColor', label: '图片背景色' },
      { kind: 'input', key: 'titleText', label: '标题内容' },
      { kind: 'switch', key: 'showMore', label: '显示查看更多' },
      { kind: 'input', key: 'showMoreText', label: '更多文字', showIf: (m) => Number(m.showMore) === 1 },
      { kind: 'link', key: 'moreLink', label: '更多链接', showIf: (m) => Number(m.showMore) === 1 },
      { kind: 'color', key: 'backgroundColor', label: '模块背景颜色' },
    ],
    summary: (m) => `${m.titleText || ''} · ${((m.picList || []) as PicItem[]).length} 卡`,
  },
  {
    type: 'pc-product', label: '商品', group: '商品',
    defaults: () => ({
      style: 1, goodsStyle: 1, goodsRadioStyle: 1, textAlign: 1, textWeight: 1, goodsNameRow: 2,
      goodsNamePadding: 1, showName: 1, showBrief: 1, showPrice: 1, goodsPadding: 5, buyBtnStyle: 1,
      backgroundColor: '', boxRadius: 0, innerPadding: 0, boxPadding: 10, boxPaddingTop: 5,
      boxPaddingBottom: 5, imgPadding: 1, waterfall: 0, swiperPageColor: '',
      frame: standardFrame(),
      title: standardTitle(),
      products: productsDefaults(),
    }),
    settings: [
      { kind: 'radio', key: 'style', label: '展示样式', options: [{ label: '一行四个', value: 1 }, { label: '一行五个', value: 2 }] },
      { kind: 'slider', key: 'goodsPadding', label: '商品边距', max: 30 },
      { kind: 'products', key: 'products', label: '商品设置' },
      { kind: 'title', key: 'title', label: '标题设置' },
      ...containerFields({ showInnerPadding: true }),
    ],
    summary: (m) => `样式${Number(m.style) === 2 ? 5 : 4}列 · ${((m.products?.productIds || []) as number[]).length} 商品`,
  },
  {
    type: 'pc-product-group', label: '商品分组', group: '商品',
    defaults: () => ({
      productSource: 'products',
      productGroups: ['为你推荐', '热卖爆款', '折扣专区', '预售专场'].map((title, gi) => ({
        sourceType: 'products',
        title,
        titlePic: gi === 0 ? { picUrl: DEMO_PIC_1, picThumb: DEMO_PIC_1 } : { picUrl: '', picThumb: '' },
        titleLink: { link: '', name: '' },
        bannerPic: { picUrl: '', picThumb: '' },
        bannerLink: { link: '', name: '' },
        productList: Array.from({ length: 4 }, (_, i) => ({
          productId: 0,
          productSn: '',
          productName: '商品标题（后台自动调用读取）',
          productDesc: '',
          productPrice: [88.8, 56.8, 48.8, 32.0][i % 4],
          marketPrice: [98.8, 68.88, 69.9, 39.0][i % 4],
          picThumb: HERO_DEMO_PRODUCTS[(gi + i) % 6].pic.picThumb,
        })),
      })),
      groupList: [],
      productCardStyle: 1, listStyle: 4, cardPadding: 0, cardRadius: 4,
      showProductName: 1, productNameRow: 1, productNameWeight: 400, productNameSize: 16,
      showProductPrice: 1, productPriceMarginTop: 10, productPriceWeight: 400, productPriceSize: 28,
      crossedOutPrice: 1, showSales: 1, showShopTag: 1,
      buyBtnStyle: { showBtn: 1, btnType: 1, text: '', textColor: '#ffffff', iconColor: '#ffffff', paddingLeftRight: 3 },
      productColor: { infoLeftRightPadding: 10, backgroundColor: '#ffffff', titleColor: '#19191b', priceColor: '', crossedOutPriceColor: '#999999' },
      groupTitle: { marginLeftRight: 5, showTitle: 1, bigTitleFontSize: 14, titleColor: '#444444', activeTitleColor: '#FF0F23', activeTitleBgColor: '#ffebf1', titleMinWidth: 80, titlePaddingTopBottom: 8, titlePaddingLeftRight: 12, titleRadius: 4, splitLineColor: 'transparent' },
      moduleStyle: { boxPaddingTop: 5, boxPaddingBottom: 5, boxPadding: 10, backgroundColor: '' },
      contentStyle: { paddingTop: 0, paddingBottom: 0, paddingLeftRight: 0, boxRadius: 0, backgroundColor: '', backgroundPic: { picUrl: '', picThumb: '' }, backgroundPicFillType: 'cover', boxShadow: 0 },
    }),
    settings: [
      { kind: 'groupList', key: 'productGroups', label: '分组列表' },
      { kind: 'radio', key: 'productCardStyle', label: '商品样式', options: [{ label: '扁平', value: 1 }, { label: '卡片', value: 2 }, { label: '描边', value: 3 }] },
      { kind: 'radio', key: 'listStyle', label: '展示样式', options: [{ label: '一行四个', value: 4 }, { label: '一行五个', value: 6 }, { label: '一行六个', value: 7 }] },
      { kind: 'slider', key: 'cardPadding', label: '商品内部边距', max: 10 },
      { kind: 'slider', key: 'cardRadius', label: '商品圆角半径', max: 20 },
      { kind: 'switch', key: 'showProductName', label: '商品标题' },
      { kind: 'radio', key: 'productNameRow', label: '标题行数', options: [{ label: '1 行', value: 1 }, { label: '2 行', value: 2 }], showIf: (m) => Number(m.showProductName) === 1 },
      { kind: 'radio', key: 'productNameWeight', label: '标题粗细', options: [{ label: '常规', value: 400 }, { label: '加粗', value: 700 }], showIf: (m) => Number(m.showProductName) === 1 },
      { kind: 'slider', key: 'productNameSize', label: '标题大小', min: 12, max: 16, showIf: (m) => Number(m.showProductName) === 1 },
      { kind: 'switch', key: 'showProductPrice', label: '商品价格' },
      { kind: 'slider', key: 'productPriceMarginTop', label: '价格上间距', max: 30, showIf: (m) => Number(m.showProductPrice) === 1 },
      { kind: 'radio', key: 'productPriceWeight', label: '价格粗细', options: [{ label: '常规', value: 400 }, { label: '加粗', value: 700 }], showIf: (m) => Number(m.showProductPrice) === 1 },
      { kind: 'slider', key: 'productPriceSize', label: '价格大小', min: 12, max: 40, showIf: (m) => Number(m.showProductPrice) === 1 },
      { kind: 'switch', key: 'crossedOutPrice', label: '划线价' },
      { kind: 'switch', key: 'showSales', label: '已售' },
      { kind: 'switch', key: 'showShopTag', label: '商品店铺' },
      { kind: 'container', key: 'buyBtnStyle', label: '购买按钮', containerType: 'buyBtn' },
      { kind: 'container', key: 'productColor', label: '商品颜色', containerType: 'productColor' },
      { kind: 'container', key: 'groupTitle', label: '分组标题', containerType: 'groupTitle' },
      { kind: 'container', key: 'moduleStyle', label: '模块样式', containerType: 'moduleStyle' },
      { kind: 'container', key: 'contentStyle', label: '内容样式', containerType: 'contentStyle' },
    ],
    summary: (m) => `${((m.productGroups || []) as Array<Record<string, any>>).length} 组 · 样式${m.listStyle}`,
  },
  {
    type: 'pc-recommend-product', label: '推荐商品', group: '商品', hiddenInLibrary: true,
    defaults: () => ({
      frame: { boxPadding: 10, boxPaddingTop: 10, boxPaddingBottom: 50, boxRadius: 0, backgroundColor: 'transparent', itemGap: 16, columns: 5 },
      titleName: '猜你喜欢',
      products: { ...productsDefaults(), productNumber: 20 },
    }),
    settings: [
      { kind: 'input', key: 'titleName', label: '标题' },
      { kind: 'products', key: 'products', label: '商品设置', numberMax: 50 },
      ...containerFields({ showInnerPadding: true }),
    ],
    summary: (m) => `${m.titleName || ''} · ${((m.products?.productIds || []) as number[]).length} 商品`,
  },
  {
    type: 'pc-home-coupon', label: '优惠券', group: '营销活动',
    note: '优惠券内容由营销-优惠券"首页显示"决定',
    defaults: () => ({
      showTitle: 0, title: '每日领券', desc: '天天优惠领不停', colorStyle: 0,
      itemBackgroundColor: '#ffffff', backgroundColor: '#ffffff',
      itemPadding: 10, boxRadius: 8, boxPadding: 10, boxPaddingTop: 5, boxPaddingBottom: 5,
    }),
    settings: [
      { kind: 'switch', key: 'showTitle', label: '显示标题栏' },
      { kind: 'input', key: 'title', label: '标题内容', showIf: (m) => Number(m.showTitle) === 1 },
      { kind: 'textarea', key: 'desc', label: '描述内容', showIf: (m) => Number(m.showTitle) === 1 },
      { kind: 'slider', key: 'itemPadding', label: '组件边距', max: 30 },
      { kind: 'slider', key: 'boxRadius', label: '圆角', max: 30 },
      { kind: 'slider', key: 'boxPadding', label: '左右边距', max: 30 },
      { kind: 'slider', key: 'boxPaddingTop', label: '上边距', max: 30 },
      { kind: 'slider', key: 'boxPaddingBottom', label: '下边距', max: 30 },
    ],
    summary: () => '展示"首页显示"的优惠券',
  },
  {
    type: 'pc-home-seckill', label: '秒杀', group: '营销活动',
    defaults: () => ({
      frame: { boxPadding: 0, boxPaddingTop: 0, boxPaddingBottom: 0, boxRadius: 0 },
      seckillTitle: '全场优惠限时9折起',
      title: standardTitle({ showTitle: 1, titleText: '全场优惠限时9折起', titleColor: '#1a1a1a', moreType: 2, countdownLabel: '倒计时', moreColor: '#1a1a1a' }),
      goodsGap: 10,
    }),
    settings: [
      { kind: 'slider', key: 'goodsGap', label: '商品间距', max: 40 },
      { kind: 'title', key: 'title', label: '标题设置', hint: '秒杀商品由营销-秒杀决定' },
      ...containerFields({ showInnerPadding: true }),
    ],
    summary: (m) => String(getPath(m, 'title.titleText') || m.seckillTitle || ''),
  },
  {
    type: 'pc-home-groupon', label: '拼团', group: '营销活动',
    defaults: () => ({
      style: 4, groupType: 1, cardStyle: 1, showName: 1, showPrice: 1, textAlign: 1, goodsPadding: 0,
      backgroundColor: '#ffffff', boxRadius: 0, innerPadding: 0, boxPadding: 0, boxPaddingTop: 5, boxPaddingBottom: 5,
      title: standardTitle({ showTitle: 0, titlePaddingTop: 20, titlePaddingBottom: 20, titleText: '拼团', titleColor: '#2a3145', moreType: 2, countdownLabel: '距结束', moreColor: '#1a1a1a' }),
      buyBtnStyle: 0,
      grouponActivityItem: null as unknown as Record<string, any>,
    }),
    settings: [
      { kind: 'radio', key: 'style', label: '选择模板', options: [{ label: '一行四个', value: 2 }, { label: '一行五个', value: 3 }, { label: '一行六个', value: 4 }, { label: '横向滑动', value: 6 }] },
      {
        kind: 'radio', key: 'groupType', label: '拼团类型', options: [{ label: '普通拼团', value: 1 }, { label: '阶梯拼团', value: 2 }],
        onChangeExtra: (m) => ({ ...m, grouponActivityItem: null }), // 原版：切换类型清空已选活动
      },
      { kind: 'groupon', key: 'grouponActivityItem', label: '拼团活动' },
      { kind: 'radio', key: 'cardStyle', label: '商品卡片样式', options: [{ label: '样式一', value: 1 }, { label: '样式二', value: 2 }] },
      { kind: 'radio', key: 'textAlign', label: '文本对齐', options: [{ label: '左对齐', value: 1 }, { label: '居中对齐', value: 2 }] },
      { kind: 'switch', key: 'showName', label: '显示商品名称' },
      { kind: 'switch', key: 'showPrice', label: '显示商品价格' },
      { kind: 'slider', key: 'goodsPadding', label: '商品边距', max: 50 },
      { kind: 'title', key: 'title', label: '标题设置', titleHideAlign: true },
      { kind: 'color', key: 'backgroundColor', label: '背景颜色' },
      { kind: 'slider', key: 'boxRadius', label: '圆角', max: 30 },
      { kind: 'slider', key: 'innerPadding', label: '内边距', max: 30 },
      { kind: 'slider', key: 'boxPadding', label: '左右边距', max: 30 },
      { kind: 'slider', key: 'boxPaddingTop', label: '上边距', max: 30 },
      { kind: 'slider', key: 'boxPaddingBottom', label: '下边距', max: 30 },
    ],
    summary: (m) => (m.grouponActivityItem ? m.grouponActivityItem.name : '未选择活动'),
  },
  {
    type: 'pc-home-hero', label: '营销通栏', group: '营销活动',
    defaults: () => ({
      frame: { boxPadding: 0, boxPaddingTop: 0, boxPaddingBottom: 0, backgroundColor: '' },
      left: {
        title: '全部商品分类',
        showCount: 11,
        categories: HERO_CATEGORIES,
        backgroundColor: '#ffffff',
        textColor: '#333333',
      },
      center: {
        banners: [demoPic('1', DEMO_PIC_1, '演示 Banner 1', '限时直降'), demoPic('2', DEMO_PIC_2, '演示 Banner 2', '新品上新')],
        modules: heroDemoModules(),
      },
      right: {
        user: { avatar: { picUrl: 'https://oss.vortmall.com/upload/c4ca4238/2026/02/09/9be6e26cd04b449a8e843df6f790d601.png', picThumb: 'https://oss.vortmall.com/upload/c4ca4238/2026/02/09/9be6e26cd04b449a8e843df6f790d601.png', picName: 'avatar' }, nickname: '欢迎来商城', welcomeText: '登录后更多精彩' },
        userCardBackground: '#ffffff',
        userCardGradientColorA: '#FFEDE1',
        userCardGradientColorB: '#FCF9F8',
        userCardGradientType: 'upDown',
        premiumMember: { show: false, title: '会员', description: '', link: { path: 'base', label: '权益卡中心', name: '权益卡中心', link: 'member/premiumMember/index' } },
        stats: [
          { label: '优惠券', value: '85' },
          { label: '余额', value: '257.58' },
          { label: '积分', value: '544' },
        ],
        shortcuts: [1, 2, 3, 4].map((n) => ({ picId: `s${n}`, picThumb: '', picUrl: '', picName: `shop_${n}`, picTitle: '进入店铺', picLink: { link: '', name: '' } })),
        shopsTitle: '店铺街',
        shopsSubTitle: 'HOT',
        shopsSubTitleBg: '#FF4E4E',
        shopsMoreText: '查看全部',
        shopsMoreLink: { link: '', name: '' },
        shopsTextColor: '#a06a38',
      },
      style: {
        height: 336, gap: 16, leftWidth: 220, rightWidth: 260,
        panelBackgroundColor: '#ffffff', panelRadius: 8, panelShadow: 1,
        titleFontSize: 14, titleColor: '#333333', mutedColor: '#999999',
        priceColor: '#e1251b', bannerRadius: 12, cardRadius: 12,
      },
    }),
    settings: [
      { kind: 'color', key: 'left.backgroundColor', label: '左侧分类背景' },
      { kind: 'color', key: 'left.textColor', label: '左侧分类文字色' },
      { kind: 'number', key: 'left.showCount', label: '分类显示条数', min: 1, max: 20 },
      { kind: 'picList', key: 'center.banners', label: '中间轮播图' },
      { kind: 'heroCards', key: 'center.modules', label: '频道卡位(6个)' },
      { kind: 'input', key: 'right.user.nickname', label: '未登录欢迎语' },
      { kind: 'input', key: 'right.user.welcomeText', label: '未登录提示语' },
      { kind: 'switch', key: 'right.premiumMember.show', label: '显示付费会员卡' },
      { kind: 'input', key: 'right.premiumMember.title', label: '会员卡标题', showIf: (m) => m.right?.premiumMember?.show === true },
      { kind: 'input', key: 'right.premiumMember.description', label: '会员卡描述', showIf: (m) => m.right?.premiumMember?.show === true },
      { kind: 'link', key: 'right.premiumMember.link', label: '会员卡链接', showIf: (m) => m.right?.premiumMember?.show === true },
      { kind: 'input', key: 'right.shopsTitle', label: '店铺街标题' },
      { kind: 'input', key: 'right.shopsSubTitle', label: '店铺街角标文字' },
      { kind: 'color', key: 'right.shopsSubTitleBg', label: '角标背景色' },
      { kind: 'picList', key: 'right.shortcuts', label: '快捷入口(≤4)' },
      { kind: 'input', key: 'right.shopsMoreText', label: '查看全部文字' },
      { kind: 'link', key: 'right.shopsMoreLink', label: '查看全部链接' },
      { kind: 'color', key: 'right.shopsTextColor', label: '店铺街文字色' },
      { kind: 'number', key: 'style.height', label: '整体高度', min: 200, max: 600 },
      { kind: 'slider', key: 'style.panelRadius', label: '面板圆角', max: 30 },
      { kind: 'color', key: 'style.panelBackgroundColor', label: '面板背景色' },
      { kind: 'slider', key: 'style.gap', label: '列间距', max: 30 },
      { kind: 'slider', key: 'style.titleFontSize', label: '标题字号', min: 12, max: 20 },
      { kind: 'color', key: 'style.titleColor', label: '标题颜色' },
      { kind: 'color', key: 'style.mutedColor', label: '弱化文字色' },
      { kind: 'color', key: 'style.priceColor', label: '价格颜色' },
      { kind: 'slider', key: 'style.bannerRadius', label: '轮播圆角', max: 30 },
      { kind: 'slider', key: 'style.cardRadius', label: '卡片圆角', max: 30 },
    ],
    summary: (m) => `三列通栏 · ${((m.center?.banners || []) as PicItem[]).length} 轮播`,
  },
  {
    type: 'pc-notice', label: '公告', group: '辅助元素',
    defaults: () => ({
      text: '公告内容',
      icoPic: { picUrl: NOTICE_ICON, picThumb: NOTICE_ICON },
      frame: standardFrame({ textColor: '#555555', itemBackgroundColor: '#fff8e6', itemRadius: 4 }),
    }),
    settings: [
      { kind: 'input', key: 'text', label: '公告内容' },
      { kind: 'pic', key: 'icoPic', label: '公告图标' },
      { kind: 'color', key: 'frame.textColor', label: '文字颜色' },
      { kind: 'color', key: 'frame.itemBackgroundColor', label: '组件背景' },
      { kind: 'slider', key: 'frame.itemRadius', label: '组件圆角', max: 50 },
      { kind: 'slider', key: 'frame.itemHeight', label: '组件高度', min: 20, max: 80 },
      ...containerFields(),
    ],
    summary: (m) => String(m.text || '').slice(0, 20),
  },
  {
    type: 'pc-title-ad', label: '文本标题', group: '辅助元素',
    defaults: () => ({
      title: standardTitle({ showTitle: 1, titleText: '标题内容', titleColor: '#2a3145', moreColor: '#969799' }),
      frame: standardFrame(),
    }),
    settings: [
      { kind: 'radio', key: 'title.titleStyle', label: '标题栏风格', options: [{ label: '样式一', value: 1 }, { label: '样式二', value: 2 }] },
      { kind: 'radio', key: 'title.titleAlign', label: '标题位置', options: [{ label: '居左', value: 1 }, { label: '居中', value: 2 }] },
      { kind: 'color', key: 'title.titleBackground', label: '标题背景色' },
      { kind: 'slider', key: 'title.titleRadius', label: '标题圆角', max: 30 },
      { kind: 'input', key: 'title.titleText', label: '标题内容', onChangeExtra: (m) => setPath(m, 'title.showTitle', 1) },
      { kind: 'color', key: 'title.titleColor', label: '标题颜色' },
      { kind: 'input', key: 'title.descText', label: '描述内容' },
      { kind: 'color', key: 'title.descColor', label: '描述颜色' },
      { kind: 'switch', key: 'title.showMore', label: '显示查看更多' },
      { kind: 'color', key: 'title.moreColor', label: '更多文字颜色' },
      { kind: 'link', key: 'title.moreLink', label: '更多链接', showIf: (m) => Number(getPath(m, 'title.showMore')) === 1 },
      ...containerFields({ showInnerPadding: true }),
    ],
    summary: (m) => String(getPath(m, 'title.titleText') || ''),
  },
  {
    type: 'pc-divider', label: '分割线', group: '辅助元素',
    defaults: () => ({
      lineType: 2, lineStyle: 1,
      frame: standardFrame({ itemHeight: 10, itemRadius: 0 }),
    }),
    settings: [
      { kind: 'radio', key: 'lineType', label: '分割类型', options: [{ label: '空白', value: 1 }, { label: '分割线', value: 2 }] },
      { kind: 'radio', key: 'lineStyle', label: '选择样式', options: [{ label: '实线', value: 1 }, { label: '虚线', value: 2 }, { label: '点状线', value: 3 }], showIf: (m) => Number(m.lineType) === 2 },
      { kind: 'slider', key: 'frame.itemHeight', label: '组件高度', min: 0, max: 100 },
      ...containerFields(),
    ],
    summary: (m) => (Number(m.lineType) === 1 ? '空白' : '分割线'),
  },
  {
    type: 'pc-spacer', label: '空白辅助', group: '辅助元素',
    defaults: () => ({
      frame: { boxPadding: 0, boxPaddingTop: 0, boxPaddingBottom: 0, boxRadius: 0 },
      backgroundColor: '#ffffff',
      blankHeight: 30,
    }),
    settings: [
      { kind: 'color', key: 'backgroundColor', label: '背景颜色' },
      { kind: 'slider', key: 'blankHeight', label: '高度', min: 0, max: 200 },
    ],
    summary: (m) => `高 ${m.blankHeight}px`,
  },
];

/* ===================== 旧版模块集（camelCase，仅兼容历史数据编辑） ===================== */

const LEGACY_MODULE_DEFS: ModuleDef[] = [
  { type: 'pcBanner', label: '轮播Banner', group: '旧版', legacy: true, defaults: () => ({ bannerStyle: 1, showCat: 1, logo: { picUrl: '', picThumb: '' }, picList: [], picList2: [], picList3: [] }), settings: [{ kind: 'radio', key: 'bannerStyle', label: '风格', options: [{ label: '默认风格', value: 1 }, { label: '简约风格', value: 2 }] }, { kind: 'picList', key: 'picList', label: '轮播大图' }], summary: (m) => `${((m.picList || []) as PicItem[]).length} 图` },
  { type: 'pcBanner2', label: '轮播Banner2', group: '旧版', legacy: true, defaults: () => ({ bannerStyle: 1, showCat: 1, logo: { picUrl: '', picThumb: '' }, picList: [], picList2: [], picList3: [] }), settings: [{ kind: 'picList', key: 'picList', label: '轮播图' }], summary: (m) => `${((m.picList || []) as PicItem[]).length} 图` },
  { type: 'pcGroupBanner', label: '分组轮播Banner', group: '旧版', legacy: true, defaults: () => ({ bannerStyle: 1, showCat: 1, logo: { picUrl: '', picThumb: '' }, picList: [], picList2: [], picList3: [], number: undefined as unknown as number }), settings: [{ kind: 'picList', key: 'picList', label: 'Banner图' }, { kind: 'number', key: 'number', label: '一组显示数量', min: 1, max: 3 }], summary: (m) => `每组 ${m.number || '-'}` },
  { type: 'pcImageAd', label: '通宽广告图', group: '旧版', legacy: true, defaults: () => ({ picList: [], boxPaddingTop: 10, boxPaddingBottom: 10 }), settings: [{ kind: 'picList', key: 'picList', label: '广告图' }, { kind: 'slider', key: 'boxPaddingTop', label: '上边距', max: 30 }, { kind: 'slider', key: 'boxPaddingBottom', label: '下边距', max: 30 }], summary: (m) => `${((m.picList || []) as PicItem[]).length} 图` },
  { type: 'pcCustomAd1', label: '自定义模块一', group: '旧版', legacy: true, defaults: () => ({ picList: [], picList2: [], picList3: [] }), settings: [{ kind: 'picList', key: 'picList', label: '左侧大图' }, { kind: 'picList', key: 'picList2', label: '中间每日推荐' }, { kind: 'picList', key: 'picList3', label: '右侧热销精品' }], summary: () => '三列自定义' },
  { type: 'pcCustomAd2', label: '自定义模块二', group: '旧版', legacy: true, defaults: () => ({ linkList: [], picList: [], picList2: [], picList3: [] }), settings: [{ kind: 'picList', key: 'picList', label: '左侧大图' }, { kind: 'linkList', key: 'linkList', label: '左侧链接' }, { kind: 'picList', key: 'picList2', label: '中间广告图' }, { kind: 'picList', key: 'picList3', label: '右侧广告图' }], summary: () => '三列自定义' },
  { type: 'pcCatProductNormal', label: '分类商品-窄', group: '旧版', legacy: true, defaults: () => ({ categoryId: 0, title: '', shortTitle: '', enTitle: '', color: '', color2: '', logo: { picUrl: '', picThumb: '' }, picList: [], picList2: [] }), settings: [{ kind: 'category', key: 'categoryId', label: '商品分类' }, { kind: 'input', key: 'title', label: '标题' }, { kind: 'picList', key: 'picList', label: '左侧轮播图' }, { kind: 'picList', key: 'picList2', label: '右侧四小图' }], summary: (m) => String(m.title || '') },
  { type: 'pcCatProductWide', label: '分类商品-宽', group: '旧版', legacy: true, defaults: () => ({ categoryId: 0, title: '', shortTitle: '', enTitle: '', color: '', color2: '', logo: { picUrl: '', picThumb: '' }, picList: [], picList2: [], productIds: [], linkList: [] }), settings: [{ kind: 'categoryMulti', key: 'categoryId', label: '商品分类' }, { kind: 'input', key: 'title', label: '标题' }, { kind: 'productIds', key: 'productIds', label: '热门商品' }, { kind: 'picList', key: 'picList', label: '左侧轮播图' }], summary: (m) => String(m.title || '') },
  { type: 'pcCatProductSimple', label: '分类商品-简约', group: '旧版', legacy: true, defaults: () => ({ baseStyle: 1, title: '', products: { productSelectType: 1, productCategoryId: 1, productNumber: 8, productIds: [], productTag: '' }, moreLink: '', picList: [] }), settings: [{ kind: 'radio', key: 'baseStyle', label: '风格', options: [{ label: '风格一', value: 1 }, { label: '风格二', value: 2 }] }, { kind: 'input', key: 'title', label: '标题' }, { kind: 'products', key: 'products', label: '商品设置' }], summary: (m) => String(m.title || '') },
  { type: 'pcRecommendProduct', label: '推荐商品-瀑布流', group: '旧版', legacy: true, defaults: () => ({ titleName: '', products: { productNumber: 50, productIds: [] } }), settings: [{ kind: 'input', key: 'titleName', label: '标题' }, { kind: 'products', key: 'products', label: '商品设置', numberMin: 20, numberStep: 10 }], summary: (m) => String(m.titleName || '') },
  { type: 'pcSeckill', label: '秒杀模块', group: '旧版', legacy: true, defaults: () => ({ seckillTitle: '全场优惠限时9折起' }), settings: [{ kind: 'input', key: 'seckillTitle', label: '活动标题' }], summary: (m) => String(m.seckillTitle || '') },
  { type: 'pcCoupon', label: '优惠券模块', group: '旧版', legacy: true, note: '内容由营销-优惠券"首页显示"决定', defaults: () => ({}), settings: [], summary: () => '优惠券' },
  { type: 'pcGroupon', label: '拼团', group: '旧版', legacy: true, defaults: () => ({ style: 2, goodsStyle: 1, goodsRadioStyle: 1, textAlign: 1, textWeight: 1, goodsNameRow: 2, goodsNamePadding: 1, showName: 1, showBrief: 1, showPrice: 1, goodsPadding: 5, buyBtnStyle: 1, backgroundColor: '', boxRadius: 0, innerPadding: 0, boxPadding: 10, boxPaddingTop: 5, boxPaddingBottom: 5, frame: { textColor: '', itemBackgroundColor: '', backgroundColor: '#ffffff', innerPadding: 0, itemHeight: 35, itemRadius: 3, boxRadius: 0, boxPadding: 10, leftAndRightPadding: 0, boxPaddingTop: 5, boxPaddingBottom: 5 }, title: { showTitle: 0, titleStyle: 1, titleAlign: 1, titleBackground: '', titleBackground2: '', titleBackgroundPic: { picUrl: '', picThumb: '' }, titleRadius: 0, titleText: '标题内容', titleColor: '', titleWeight: 'normal', descText: '', descColor: '#aaaaaa', showMore: 0, moreType: 1, countdownLabel: '', moreLink: [], moreColor: '#aaaaaa', format: { titleBackground: '', titleRadius: '' } }, groupType: 1, grouponActivityItem: undefined as unknown as Record<string, any>, cardStyle: 1 }), settings: [{ kind: 'radio', key: 'style', label: '模板', options: [{ label: '一行四个', value: 2 }, { label: '一行五个', value: 3 }, { label: '横向滑动', value: 6 }] }, { kind: 'groupon', key: 'grouponActivityItem', label: '拼团活动' }, { kind: 'color', key: 'backgroundColor', label: '背景颜色' }], summary: (m) => (m.grouponActivityItem ? m.grouponActivityItem.name : '未选择活动') },
  { type: 'pcCategoryNav', label: '分类导航', group: '旧版', legacy: true, defaults: () => ({ bannerStyle: 1, showCat: 1, logo: { picUrl: '', picThumb: '' }, picList: [], picList2: [], picList3: [] }), settings: [{ kind: 'radio', key: 'rowNum', label: '每屏显示数量', options: [2, 3, 4, 5, 6].map((n) => ({ label: `${n}个`, value: n })) }, { kind: 'picList', key: 'picList', label: '导航图组' }], summary: (m) => `每屏 ${m.rowNum || '-'}` },
  { type: 'pcCategoryNav3', label: '分类导航3', group: '旧版', legacy: true, defaults: () => ({ bannerStyle: 1, showCat: 1, logo: { picUrl: '', picThumb: '' }, picList: [], picList2: [], picList3: [], imageBackgroundColor: undefined as unknown as string, titleText: undefined as unknown as string, showMore: undefined as unknown as number, showMoreText: undefined as unknown as string, moreLink: undefined as unknown as Record<string, any> }), settings: [{ kind: 'picList', key: 'picList', label: '图片卡片' }, { kind: 'color', key: 'imageBackgroundColor', label: '图片背景色' }, { kind: 'input', key: 'titleText', label: '标题内容' }, { kind: 'switch', key: 'showMore', label: '查看更多' }], summary: (m) => String(m.titleText || '') },
  { type: 'pcCatWide', label: '分类模块-宽', group: '旧版', legacy: true, defaults: () => ({ catIds: [] as number[] }), settings: [{ kind: 'categoryMulti', key: 'catIds', label: '已选分类' }], summary: (m) => `${((m.catIds || []) as number[]).length} 分类` },
];

const ALL_MODULE_DEFS = [...NEW_MODULE_DEFS, ...LEGACY_MODULE_DEFS];
const MODULE_DEF_MAP = new Map(ALL_MODULE_DEFS.map((d) => [d.type, d]));
const LEGACY_TYPES = new Set(LEGACY_MODULE_DEFS.map((d) => d.type));

const LIB_GROUPS = ['轮播横幅', '导航分类', '图片展示', '商品', '营销活动', '辅助元素'];

/** 模块库原版元数据：工具栏排序 order + iconfont 图标类（原版 index.ts 注册值） */
const LIB_META: Record<string, { order: number; icon: string }> = {
  'pc-banner2': { order: 1, icon: 'icon-banner' },
  'pc-group-banner': { order: 2, icon: 'icon-banner' },
  'pc-home-hero': { order: 4, icon: 'icon-category_nav' },
  'pc-category-nav': { order: 4, icon: 'icon-category_nav' },
  'pc-category-nav3': { order: 5, icon: 'icon-category_nav' },
  'pc-home-quick-nav': { order: 6, icon: 'icon-category_nav' },
  'pc-image-ad': { order: 10, icon: 'icon-image_nav' },
  'pc-image-nav': { order: 11, icon: 'icon-image_nav' },
  'pc-image-square-ad': { order: 12, icon: 'icon-image_square_ad' },
  'pc-image-hotarea': { order: 13, icon: 'icon-image_hotarea' },
  'pc-product': { order: 14, icon: 'icon-product' },
  'pc-product-group': { order: 15, icon: 'icon-product_grouping' },
  'pc-divider': { order: 60, icon: 'icon-white_line' },
  'pc-spacer': { order: 70, icon: 'icon-white_blank' },
  'pc-notice': { order: 72, icon: 'icon-notice' },
  'pc-title-ad': { order: 75, icon: 'icon-title_ad' },
  'pc-home-coupon': { order: 82, icon: 'icon-coupon' },
  'pc-home-groupon': { order: 83, icon: 'icon-seckill' },
  'pc-home-seckill': { order: 84, icon: 'icon-seckill' },
};

/* ===================== 编辑器组件 ===================== */

interface EditorModule {
  moduleIndex: number;
  type: string;
  url: string;
  label: string;
  isShow: boolean;
  fixed?: boolean;
  module: Record<string, any>;
}

interface PcDecorateEditorProps {
  decorateId: number;
  onExit?: () => void;
}

export default function PcDecorateEditor({ decorateId, onExit }: PcDecorateEditorProps) {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState<'draft' | 'publish' | null>(null);
  const [title, setTitle] = useState('');
  const [pageModule, setPageModule] = useState<Record<string, any>>({});
  const [moduleList, setModuleList] = useState<EditorModule[]>([]);
  const [activeIndex, setActiveIndex] = useState<number>(-1);
  const [panelTab, setPanelTab] = useState<'module' | 'page' | 'pcHeader'>('module');
  const [libOpen, setLibOpen] = useState(true);
  const dirtyRef = useRef(false);
  const decorateIdRef = useRef(decorateId);

  const markDirty = () => {
    dirtyRef.current = true;
  };

  useEffect(() => {
    let alive = true;
    setLoading(true);
    apiGet<{
      decorateId?: number;
      decorateTitle?: string;
      data?: { pageModule?: Record<string, any>; moduleList?: EditorModule[] } | null;
      draftData?: { pageModule?: Record<string, any>; moduleList?: EditorModule[] } | null;
    }>('/decorate/decorate/detail', { id: decorateId, decorateType: 2, localeId: 0, parentId: 0 })
      .then((res) => {
        if (!alive) return;
        if (res?.decorateId) decorateIdRef.current = res.decorateId;
        setTitle(res?.decorateTitle || '');
        const source = res?.data || {};
        const pm: Record<string, any> = {
          type: 'page', module: {}, backgroundRepeat: '', backgroundSize: '',
          style: 0, headerStyle: 1,
          ...source.pageModule,
        };
        // 原版 commonPage dealDefault 语义：未设置时落到默认值
        if (pm.backgroundRepeat === undefined || pm.backgroundRepeat === '') pm.backgroundRepeat = 1;
        if (pm.backgroundSize === undefined || pm.backgroundSize === '') pm.backgroundSize = 1;
        if (typeof pm.title !== 'string') pm.title = '';
        // 新版头部装修默认值合并（原版 mergePcHeaderDefaults 语义）
        pm.pcHeader = { ...PC_HEADER_DEFAULTS, ...(pm.pcHeader || {}) };
        if (!pm.homeStyleVersion) pm.homeStyleVersion = 'new';
        setPageModule(pm);
        const list = Array.isArray(source.moduleList) ? source.moduleList : [];
        setModuleList(list.map((item, idx) => ({
          moduleIndex: item.moduleIndex ?? Date.now() + idx,
          type: item.type,
          url: item.url || 'pc',
          label: item.label || MODULE_DEF_MAP.get(item.type)?.label || item.type,
          isShow: item.isShow !== false,
          fixed: item.fixed,
          module: item.module || {},
        })));
      })
      .catch((e) => Toast.error(e?.message || '加载装修数据失败'))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [decorateId]);

  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => {
      if (!dirtyRef.current) return;
      e.preventDefault();
      e.returnValue = '';
    };
    window.addEventListener('beforeunload', handler);
    return () => window.removeEventListener('beforeunload', handler);
  }, []);

  const buildPayload = (data: Record<string, any>) => ({
    id: decorateIdRef.current,
    decorateType: 2,
    pageType: 1,
    localeId: 0,
    parentId: 0,
    data,
  });

  const getSaveData = () => {
    const hasLegacy = moduleList.some((m) => LEGACY_TYPES.has(m.type));
    const homeStyleVersion: 'new' | 'old' = hasLegacy ? 'old' : 'new';
    const pm: any = { ...pageModule, homeStyleVersion };
    if (homeStyleVersion !== 'new') {
      delete pm.pcHeader; // 原版契约：旧版不携带头部装修
    }
    return { pageModule: pm, moduleList: moduleList.map((m) => ({ ...m })) };
  };

  const handleSave = async (mode: 'draft' | 'publish') => {
    if (!decorateIdRef.current) {
      Toast.warning('缺少装修页面 ID');
      return;
    }
    setSaving(mode);
    try {
      await apiPost(mode === 'draft' ? '/decorate/decorate/saveDraft' : '/decorate/decorate/publish', buildPayload(getSaveData()));
      Toast.success(mode === 'draft' ? '草稿已保存' : '发布成功');
      dirtyRef.current = false;
    } catch (e: any) {
      Toast.error(e?.message || '保存失败');
    } finally {
      setSaving(null);
    }
  };

  const handleRename = async () => {
    if (!title.trim()) {
      Toast.warning('页面名称不能为空');
      return;
    }
    try {
      await apiPost('/decorate/decorate/updateField', { id: decorateIdRef.current, field: 'decorateTitle', val: title.trim() });
      Toast.success('页面名称修改成功');
    } catch (e: any) {
      Toast.error(e?.message || '修改失败');
    }
  };

  /** 切换首页装修版本（原版 onSwitchHomeVersion：清空另一套模块，旧版还会清掉头部装修） */
  const switchHomeVersion = (target: 'new' | 'old') => {
    const toNew = target === 'new';
    const clearCount = toNew
      ? moduleList.filter((m) => LEGACY_TYPES.has(m.type)).length
      : moduleList.filter((m) => !LEGACY_TYPES.has(m.type)).length;
    const clearLabel = toNew ? '旧版' : '新版';
    const targetLabel = toNew ? '新版' : '旧版';
    const clearsHeader = !toNew; // 切旧版时 pcHeader 头部装修数据会随保存一并清空
    const clearParts: string[] = [];
    if (clearCount > 0) clearParts.push(`${clearCount} 个${clearLabel}模块`);
    if (clearsHeader) clearParts.push('头部装修数据');
    Modal.confirm({
      title: '切换首页装修版本',
      content: `切换到${targetLabel}后，将清空当前画布上的${clearParts.join('和') || '无关内容'}；一经预览、保存草稿或发布，将无法恢复。确认切换？`,
      okText: '确认切换',
      cancelText: '取消',
      onOk: () => {
        setModuleList((prev) => prev.filter((m) => (LEGACY_TYPES.has(m.type) ? !toNew : toNew)));
        setPageModule((prev) => {
          const next: any = { ...prev, homeStyleVersion: target };
          if (target === 'old') delete next.pcHeader;
          return next;
        });
        markDirty();
        Toast.success(`已切换到${targetLabel}首页`);
      },
    });
  };

  const addModule = (def: ModuleDef) => {
    const item: EditorModule = {
      moduleIndex: Date.now(),
      type: def.type,
      url: 'pc',
      label: def.label,
      isShow: true,
      module: def.defaults(),
    };
    setModuleList((prev) => [...prev, item]);
    setActiveIndex(-1);
    setPanelTab('module');
    markDirty();
    Toast.success(`已添加「${def.label}」`);
  };

  /** 拖拽入画布：插入到目标位置（原版 vuedraggable clone 行为） */
  const addModuleAt = (def: ModuleDef, index: number) => {
    const item: EditorModule = {
      moduleIndex: Date.now(),
      type: def.type,
      url: 'pc',
      label: def.label,
      isShow: true,
      module: def.defaults(),
    };
    setModuleList((prev) => [...prev.slice(0, index), item, ...prev.slice(index)]);
    setActiveIndex(index);
    setPanelTab('module');
    markDirty();
    Toast.success(`已添加「${def.label}」`);
  };

  const removeModule = (index: number) => {
    setModuleList((prev) => prev.filter((_, i) => i !== index));
    if (activeIndex === index) setActiveIndex(-1);
    markDirty();
  };

  const copyModule = (index: number) => {
    const clone: EditorModule = { ...JSON.parse(JSON.stringify(moduleList[index])), moduleIndex: Date.now() };
    setModuleList((prev) => [...prev.slice(0, index + 1), clone, ...prev.slice(index + 1)]);
    markDirty();
  };

  const moveModule = (index: number, dir: -1 | 1) => {
    const target = index + dir;
    if (target < 0 || target >= moduleList.length) return;
    setModuleList((prev) => {
      const next = [...prev];
      [next[index], next[target]] = [next[target], next[index]];
      return next;
    });
    if (activeIndex === index) setActiveIndex(target);
    markDirty();
  };

  const toggleShow = (index: number) => {
    setModuleList((prev) => prev.map((m, i) => (i === index ? { ...m, isShow: !m.isShow } : m)));
    markDirty();
  };

  const dropModule = (from: number, to: number) => {
    if (from === to) return;
    setModuleList((prev) => {
      const next = [...prev];
      const [moved] = next.splice(from, 1);
      next.splice(to, 0, moved);
      return next;
    });
    markDirty();
  };

  const updateActiveModule = (updater: (m: Record<string, any>) => Record<string, any>) => {
    setModuleList((prev) => prev.map((m, i) => (i === activeIndex ? { ...m, module: updater(m.module) } : m)));
    markDirty();
  };

  const activeModule = activeIndex >= 0 && activeIndex < moduleList.length ? moduleList[activeIndex] : null;
  const activeDef = activeModule ? MODULE_DEF_MAP.get(activeModule.type) : null;

  if (loading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh', background: '#f2f3f5' }}>
        <Spin size="large" tip="加载装修数据..." />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', background: '#f2f3f5', overflow: 'hidden' }}>
      <div
        style={{
          display: 'flex', alignItems: 'center', gap: 12, padding: '10px 16px',
          background: '#fff', borderBottom: '1px solid var(--semi-color-border)', flexShrink: 0,
        }}
      >
        <Button
          icon={<IconArrowLeft />}
          onClick={() => {
            if (dirtyRef.current && !window.confirm('有未保存的修改，确定离开？')) return;
            if (onExit) onExit();
            else window.location.href = `${window.location.origin}/?page=pcDecorateManage`;
          }}
        >
          返回列表
        </Button>
        <Space>
          <Input
            value={title}
            onChange={(v) => setTitle(v)}
            onBlur={handleRename}
            onEnterPress={handleRename}
            style={{ width: 280 }}
          />
          {dirtyRef.current && <Tag color="orange">未保存</Tag>}
          {getSaveData().pageModule.homeStyleVersion === 'new' ? <Tag color="green">新版装修</Tag> : <Tag color="grey">旧版兼容</Tag>}
        </Space>
        <div style={{ flex: 1 }} />
        <Button icon={<IconRefresh />} onClick={() => window.open(`${window.location.origin}/?page=home`, '_blank')}>
          打开商城首页
        </Button>
        <Button theme="light" loading={saving === 'draft'} onClick={() => handleSave('draft')}>
          保存草稿
        </Button>
        <Button theme="solid" type="primary" loading={saving === 'publish'} onClick={() => handleSave('publish')}>
          保存并发布
        </Button>
      </div>

      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        {/* 左侧模块库（对齐原版 ModuleToolbar：单折叠分组 + 图标卡片网格 + 拖拽入画布） */}
        <div className="pc-decorate-lib">
          <div className="pc-decorate-lib__inside">
            <div className="pc-decorate-lib__head" onClick={() => setLibOpen((v) => !v)}>
              <span>PC首页模块</span>
              <span className={`pc-decorate-lib__arrow${libOpen ? ' pc-decorate-lib__arrow--open' : ''}`}>▾</span>
            </div>
            {libOpen && (
              <div className="pc-decorate-lib__list">
                {NEW_MODULE_DEFS.filter((d) => !d.hiddenInLibrary)
                  .slice()
                  .sort((a, b) => (LIB_META[a.type]?.order ?? 999) - (LIB_META[b.type]?.order ?? 999))
                  .map((def) => (
                    <Tooltip key={def.type} content={def.note || `${def.label}（点击添加，可拖入画布指定位置）`} position="right">
                      <div
                        className="pc-decorate-lib__item"
                        onClick={() => addModule(def)}
                        draggable
                        onDragStart={(e) => {
                          e.dataTransfer.setData('text/pc-decorate-lib', def.type);
                          e.dataTransfer.effectAllowed = 'copy';
                        }}
                      >
                        <span className="pc-decorate-lib__pic">
                          <i className={`iconfont-admin ${LIB_META[def.type]?.icon || 'icon-category_nav'}`} />
                        </span>
                        <span className="pc-decorate-lib__content" title={def.label}>{def.label}</span>
                      </div>
                    </Tooltip>
                  ))}
              </div>
            )}
            <div style={{ height: 50 }} />
          </div>
        </div>

        {/* 中间画布 */}
        <div style={{ flex: 1, overflowY: 'auto', padding: 20 }}>
          <div style={{ maxWidth: 1200, margin: '0 auto' }}>
            <div className="pc-decorate-canvas-bar" onClick={() => { setPanelTab('page'); setActiveIndex(-1); }}>
              页面设置（背景颜色 / 背景图片）
            </div>
            <div className="pc-decorate-canvas-bar" onClick={() => { setPanelTab('pcHeader'); setActiveIndex(-1); }}>
              PC头部装修（LOGO / 搜索 / 主导航 / 顶部菜单 / 悬浮条）
            </div>

            {moduleList.length === 0 && (
              <div style={{ padding: 60, textAlign: 'center', color: 'var(--semi-color-text-2)' }}>
                暂无模块，请从左侧模块库添加
              </div>
            )}
            {moduleList.map((item, index) => {
              const isActive = activeIndex === index;
              const isLegacy = LEGACY_TYPES.has(item.type);
              return (
                <div
                  key={item.moduleIndex}
                  className={`pc-decorate-module-card${isActive ? ' pc-decorate-module-card--active' : ''}${item.isShow ? '' : ' pc-decorate-module-card--hidden'}`}
                  draggable
                  onDragStart={(e) => e.dataTransfer.setData('text/pc-decorate-index', String(index))}
                  onDragOver={(e) => e.preventDefault()}
                  onDrop={(e) => {
                    e.preventDefault();
                    const libType = e.dataTransfer.getData('text/pc-decorate-lib');
                    if (libType) {
                      const def = MODULE_DEF_MAP.get(libType);
                      if (def) addModuleAt(def, index);
                      return;
                    }
                    const from = Number(e.dataTransfer.getData('text/pc-decorate-index'));
                    if (!Number.isNaN(from)) dropModule(from, index);
                  }}
                  onClick={() => { setActiveIndex(index); setPanelTab('module'); }}
                >
                  <div className="pc-decorate-module-card__title">
                    <Text strong>{item.label}</Text>
                    {isLegacy && <Tag size="small" color="grey">旧版模块</Tag>}
                    {!item.isShow && <Tag size="small" color="amber">已隐藏</Tag>}
                    <div style={{ flex: 1 }} />
                    <div style={{ display: 'flex', gap: 2, alignItems: 'center' }} onClick={(e) => e.stopPropagation()}>
                      <Button size="small" theme="borderless" icon={<IconChevronUp />} disabled={index === 0} onClick={() => moveModule(index, -1)} />
                      <Button size="small" theme="borderless" icon={<IconChevronDown />} disabled={index === moduleList.length - 1} onClick={() => moveModule(index, 1)} />
                      <Button size="small" theme="borderless" icon={<IconCopy />} onClick={() => copyModule(index)} />
                      <Button size="small" theme="borderless" type="danger" icon={<IconDelete />} onClick={() => removeModule(index)} />
                    </div>
                  </div>
                  <div className="pc-decorate-canvas-preview">
                    <ModulePreview type={item.type} data={item.module} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* 右侧设置面板 */}
        <div style={{ width: 380, background: '#fff', borderLeft: '1px solid var(--semi-color-border)', overflowY: 'auto', padding: 16, flexShrink: 0 }}>
          {panelTab === 'page' && (
            <PageSettingsPanel
              pageModule={pageModule}
              legacyCount={moduleList.filter((m) => LEGACY_TYPES.has(m.type)).length}
              newCount={moduleList.filter((m) => !LEGACY_TYPES.has(m.type)).length}
              onChange={(next) => { setPageModule(next); markDirty(); }}
              onSwitchVersion={(target) => switchHomeVersion(target)}
            />
          )}
          {panelTab === 'pcHeader' && (
            <PcHeaderPanel pageModule={pageModule} onChange={(next) => { setPageModule(next); markDirty(); }} />
          )}
          {panelTab === 'module' && (
            activeModule && activeDef ? (
              <ModuleSettingsPanel
                def={activeDef}
                moduleData={activeModule.module}
                onChange={(next) => updateActiveModule(() => next)}
              />
            ) : (
              <Text type="tertiary">请在画布中选择一个模块，或切换到「页面设置 / PC头部装修」</Text>
            )
          )}
        </div>
      </div>
    </div>
  );
}

/* ===================== 画布预览（轻量真实结构渲染） ===================== */

function PreviewTitle({ title }: { title?: Record<string, any> }) {
  if (!title || Number(title.showTitle) !== 1) return null;
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: Number(title.titleAlign) === 2 ? 'center' : 'space-between', padding: '10px 12px' }}>
      <div>
        <div style={{ fontSize: 16, fontWeight: 700, color: title.titleColor || '#333' }}>{title.titleText || ''}</div>
        {title.descText ? <div style={{ fontSize: 12, color: title.descColor || '#aaa' }}>{title.descText}</div> : null}
      </div>
      {Number(title.showMore) === 1 && Number(title.moreType) === 1 && (
        <span style={{ fontSize: 12, color: title.moreColor || '#999' }}>{getPath(title, 'moreLink.title') || getPath(title, 'moreLink.name') || '查看更多'} ›</span>
      )}
      {Number(title.moreType) === 2 && (
        <span style={{ fontSize: 12, color: title.moreColor || '#999' }}>{title.countdownLabel || '倒计时'} 00:00:00</span>
      )}
    </div>
  );
}

function PicGrid({ pics, cols, gap = 8, radius = 4, ratio }: { pics: PicItem[]; cols: number; gap?: number; radius?: number; ratio?: string }) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: `repeat(${cols}, 1fr)`, gap, padding: 8 }}>
      {pics.map((p, i) => (
        <div key={i} style={{ aspectRatio: ratio || '3 / 1', borderRadius: radius, overflow: 'hidden', background: '#f2f3f5' }}>
          {p.picUrl ? <img src={p.picUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : null}
        </div>
      ))}
    </div>
  );
}

function ProductPlaceholderGrid({ count, cols }: { count: number; cols: number }) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: `repeat(${cols}, 1fr)`, gap: 8, padding: 8 }}>
      {Array.from({ length: Math.min(count, 20) }, (_, i) => (
        <div key={i} style={{ background: '#fff', borderRadius: 6, overflow: 'hidden', border: '1px solid #f0f0f0' }}>
          <div style={{ aspectRatio: '1 / 1', background: '#f7f8fa', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#c0c4cc', fontSize: 20 }}>?</div>
          <div style={{ padding: 6 }}>
            <div style={{ height: 10, borderRadius: 3, background: '#eef1f6', marginBottom: 4 }} />
            <div style={{ height: 10, width: '55%', borderRadius: 3, background: '#fdecea' }} />
          </div>
        </div>
      ))}
    </div>
  );
}

function ModulePreview({ type, data }: { type: string; data: Record<string, any> }) {
  const m = data || {};
  switch (type) {
    case 'pc-banner2':
      return <PicGrid pics={(m.picList || []) as PicItem[]} cols={1} ratio="1264 / 280" radius={Number(getPath(m, 'frame.boxRadius')) || 0} />;
    case 'pc-group-banner':
      return <PicGrid pics={(m.picList || []).slice(0, Number(m.number) || 3) as PicItem[]} cols={Number(m.number) || 3} ratio="750 / 479" radius={Number(getPath(m, 'frame.boxRadius')) || 0} />;
    case 'pc-image-ad': {
      const t = Number(m.picType);
      if (t === 2) return <PicGrid pics={(m.picList || []) as PicItem[]} cols={1} ratio="1500 / 350" />;
      const cols = t === 5 ? 2 : t === 6 ? 3 : t === 7 ? 4 : 1;
      return <PicGrid pics={(m.picList || []) as PicItem[]} cols={cols} ratio={cols === 1 ? '1500 / 350' : '1 / 1'} />;
    }
    case 'pc-image-nav': {
      const pics = (m.picList || []) as PicItem[];
      const style = Number(m.navStyle);
      return (
        <div style={{ padding: 8, background: m.backgroundColor || '#fff' }}>
          <div style={{ display: 'grid', gridTemplateColumns: `repeat(${m.rowNum || 4}, 1fr)`, gap: m.imgPadding ?? 8 }}>
            {pics.slice(0, (m.rowNum || 4) * (m.colNum || 2)).map((p, i) => (
              <div key={i} style={{ textAlign: 'center' }}>
                {style !== 3 && p.picUrl ? <img src={p.picUrl} alt="" style={{ width: 44, height: 44, borderRadius: Number(m.radioStyle) === 2 ? '50%' : 6, objectFit: 'cover' }} /> : null}
                {style !== 2 && <div style={{ fontSize: 12, marginTop: 2 }}>{p.picTitle}</div>}
              </div>
            ))}
          </div>
        </div>
      );
    }
    case 'pc-image-square-ad': {
      const t = Number(m.picType);
      const cols = t === 1 ? 2 : t === 2 ? 3 : t === 3 ? 4 : t === 4 ? 2 : t === 5 ? 3 : t === 6 ? 4 : 2;
      return <PicGrid pics={(m.picList || []) as PicItem[]} cols={cols} ratio="1 / 1" />;
    }
    case 'pc-image-hotarea': {
      const pics = (m.picList || []) as Array<PicItem & { hotarea?: Array<Record<string, any>> }>;
      const first = pics[0];
      return (
        <div style={{ padding: 8 }}>
          <div style={{ position: 'relative', background: '#f2f3f5', borderRadius: 4, overflow: 'hidden', minHeight: 120 }}>
            {first?.picUrl ? <img src={first.picUrl} alt="" style={{ width: '100%', display: 'block' }} /> : <div style={{ padding: 40, textAlign: 'center', color: '#c0c4cc' }}>热图未设置</div>}
            {(first?.hotarea || []).map((h, i) => (
              <div key={i} style={{ position: 'absolute', border: '1px dashed #e1251b', background: 'rgba(225,37,27,0.12)', left: `${(Number(h.left) / 500) * 100}%`, top: `${(Number(h.top) / 500) * 100}%`, width: `${(Number(h.width) / 500) * 100}%`, height: `${(Number(h.height) / 500) * 100}%` }} />
            ))}
          </div>
        </div>
      );
    }
    case 'pc-home-quick-nav':
      return (
        <div style={{ display: 'flex', justifyContent: Number(getPath(m, 'style.containerAlign')) === 1 ? 'flex-start' : Number(getPath(m, 'style.containerAlign')) === 3 ? 'flex-end' : 'center', flexWrap: Number(getPath(m, 'style.singleLine')) === 1 ? 'nowrap' : 'wrap', gap: Number(getPath(m, 'style.itemGap')) ?? 16, padding: 12, overflow: 'hidden' }}>
          {((m.items || []) as PicItem[]).map((p, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: Number(getPath(m, 'style.iconGap')) ?? 6, fontSize: Number(getPath(m, 'style.fontSize')) || 14, fontWeight: Number(getPath(m, 'style.fontWeight')) === 700 ? 700 : 400, color: getPath(m, 'style.textColor') || '#333', whiteSpace: 'nowrap' }}>
              {p.picUrl ? <img src={p.picUrl} alt="" style={{ width: Number(getPath(m, 'style.iconSize')) || 18, height: Number(getPath(m, 'style.iconSize')) || 18 }} /> : null}
              {p.picTitle}
            </div>
          ))}
        </div>
      );
    case 'pc-category-nav':
      return (
        <div style={{ padding: 12, background: m.backgroundColor || '#f7f7f7', borderRadius: 8 }}>
          {Number(m.showTitle) === 1 && <div style={{ fontWeight: 700, marginBottom: 8 }}>{m.titleName}</div>}
          <div style={{ display: 'flex', gap: 12, overflow: 'hidden' }}>
            {Array.from({ length: Math.min(Number(m.displayNum) || 6, 10) }, (_, i) => (
              <div key={i} style={{ textAlign: 'center', flexShrink: 0 }}>
                <div style={{ width: 64, height: 64, borderRadius: '50%', background: '#ececec' }} />
                <div style={{ fontSize: 12, marginTop: 4, color: '#888' }}>热门分类</div>
              </div>
            ))}
          </div>
        </div>
      );
    case 'pc-category-nav3':
      return (
        <div style={{ padding: 12, background: m.backgroundColor || 'rgba(0,0,0,0.02)', borderRadius: 8 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
            <b>{m.titleText}</b>
            {Number(m.showMore) === 1 && <span style={{ fontSize: 12, color: '#999' }}>{m.showMoreText || '更多'} ›</span>}
          </div>
          <div style={{ display: 'flex', gap: 10, overflow: 'hidden' }}>
            {((m.picList || []) as PicItem[]).slice(0, 8).map((p, i) => (
              <div key={i} style={{ width: 90, flexShrink: 0, textAlign: 'center' }}>
                <div style={{ aspectRatio: '1/1', borderRadius: 8, overflow: 'hidden', background: m.imageBackgroundColor || '#fff' }}>
                  {p.picUrl ? <img src={p.picUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : null}
                </div>
                {p.picTitle ? <div style={{ fontSize: 12, marginTop: 4 }}>{p.picTitle}</div> : null}
              </div>
            ))}
          </div>
        </div>
      );
    case 'pc-product': {
      const ids = (m.products?.productIds || []) as number[];
      const count = Number(m.products?.productSelectType) === 1 ? ids.length || Number(m.products?.productNumber) || 10 : Number(m.products?.productNumber) || 10;
      return (
        <div style={{ background: m.backgroundColor || '#fff' }}>
          <PreviewTitle title={m.title} />
          <ProductPlaceholderGrid count={count} cols={Number(m.style) === 2 ? 5 : 4} />
        </div>
      );
    }
    case 'pc-product-group': {
      const groups = (m.productGroups || []) as Array<Record<string, any>>;
      return (
        <div style={{ padding: 8 }}>
          <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
            {groups.map((g, i) => (
              <span key={i} style={{ fontSize: 13, padding: '4px 10px', borderRadius: 4, background: i === 0 ? getPath(m, 'groupTitle.activeTitleBgColor') || '#ffebf1' : '#f5f5f5', color: i === 0 ? getPath(m, 'groupTitle.activeTitleColor') || '#FF0F23' : '#444' }}>
                {g.title}
              </span>
            ))}
          </div>
          <ProductPlaceholderGrid count={Number(m.listStyle) === 4 ? 4 : Number(m.listStyle) === 6 ? 5 : 6} cols={Number(m.listStyle) === 4 ? 4 : Number(m.listStyle) === 6 ? 5 : 6} />
        </div>
      );
    }
    case 'pc-recommend-product':
      return (
        <div>
          {m.titleName ? <div style={{ padding: '10px 12px', fontWeight: 700 }}>{m.titleName}</div> : null}
          <ProductPlaceholderGrid count={Number(m.products?.productNumber) || 20} cols={Number(getPath(m, 'frame.columns')) || 5} />
        </div>
      );
    case 'pc-divider':
      return Number(m.lineType) === 1 ? (
        <div style={{ height: Number(getPath(m, 'frame.itemHeight')) || 10 }} />
      ) : (
        <div style={{ padding: '5px 10px' }}>
          <div style={{ borderTopWidth: 1, borderTopStyle: Number(m.lineStyle) === 2 ? 'dashed' : Number(m.lineStyle) === 3 ? 'dotted' : 'solid', borderTopColor: '#e5e9f0' }} />
        </div>
      );
    case 'pc-spacer':
      return <div style={{ height: Number(m.blankHeight) || 30, background: m.backgroundColor || '#fff' }} />;
    case 'pc-notice':
      return (
        <div style={{ padding: '5px 10px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, background: getPath(m, 'frame.itemBackgroundColor') || '#fff8e6', borderRadius: Number(getPath(m, 'frame.itemRadius')) || 4, height: Number(getPath(m, 'frame.itemHeight')) || 35, padding: '0 10px', color: getPath(m, 'frame.textColor') || '#555' }}>
            {m.icoPic?.picUrl ? <img src={m.icoPic.picUrl} alt="" style={{ width: 16, height: 16 }} /> : null}
            <span style={{ fontSize: 13 }}>{m.text || '点击添加通知公告'}</span>
          </div>
        </div>
      );
    case 'pc-title-ad':
      return <PreviewTitle title={{ ...(m.title || {}), showTitle: 1 }} />;
    case 'pc-home-coupon':
      return (
        <div style={{ padding: m.boxPadding ?? 10 }}>
          {Number(m.showTitle) === 1 && (
            <div style={{ marginBottom: 8 }}>
              <b>{m.title}</b>
              <div style={{ fontSize: 12, color: '#999' }}>{m.desc}</div>
            </div>
          )}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 8 }}>
            {['5', '8', '5', '8'].map((v, i) => (
              <div key={i} style={{ background: '#fff1f0', border: '1px solid #ffccc7', borderRadius: m.boxRadius ?? 8, padding: 10, textAlign: 'center' }}>
                <div style={{ color: '#e1251b', fontWeight: 700 }}>{i % 2 === 0 ? `¥${v}` : `${v}折`}</div>
                <div style={{ fontSize: 11, color: '#999' }}>满可用</div>
              </div>
            ))}
          </div>
        </div>
      );
    case 'pc-home-seckill':
      return (
        <div>
          <PreviewTitle title={m.title} />
          <div style={{ display: 'flex', gap: Number(m.goodsGap) ?? 10, padding: 8, overflow: 'hidden' }}>
            {Array.from({ length: 5 }, (_, i) => (
              <div key={i} style={{ flexShrink: 0, width: 110 }}>
                <div style={{ aspectRatio: '1/1', background: '#f7f8fa', borderRadius: 6 }} />
                <div style={{ fontSize: 12, color: '#e1251b', marginTop: 4 }}>¥9.9</div>
              </div>
            ))}
          </div>
        </div>
      );
    case 'pc-home-groupon':
      return (
        <div style={{ background: m.backgroundColor || '#fff' }}>
          <PreviewTitle title={m.title} />
          <div style={{ padding: 8, display: 'grid', gridTemplateColumns: `repeat(${Number(m.style) === 6 ? 4 : Number(m.style) || 4}, 1fr)`, gap: Number(m.goodsPadding) ?? 0 }}>
            {Array.from({ length: Number(m.style) === 6 ? 4 : 8 }, (_, i) => (
              <div key={i}>
                <div style={{ aspectRatio: '1/1', background: '#f7f8fa', borderRadius: 6 }} />
                <div style={{ fontSize: 12, marginTop: 4 }}>拼团商品</div>
                <div style={{ fontSize: 12, color: '#e1251b' }}>¥9.9</div>
              </div>
            ))}
          </div>
        </div>
      );
    case 'pc-home-hero': {
      const style = m.style || {};
      const left = m.left || {};
      const center = m.center || {};
      const right = m.right || {};
      const banners = (center.banners || []) as PicItem[];
      const cards = (center.modules || []) as Array<Record<string, any>>;
      return (
        <div style={{ display: 'flex', gap: style.gap ?? 16, padding: 10, background: style.panelBackgroundColor || '#fff' }}>
          <div style={{ width: style.leftWidth ?? 220, flexShrink: 0, background: left.backgroundColor || '#fff', borderRadius: style.panelRadius ?? 8, border: '1px solid #f0f0f0', overflow: 'hidden' }}>
            <div style={{ padding: '10px 12px', fontWeight: 700, fontSize: 14, color: style.titleColor || '#333' }}>{left.title || '全部商品分类'}</div>
            {(left.categories || []).slice(0, 8).map((c: PicItem, i: number) => (
              <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '6px 12px', fontSize: 12, color: left.textColor || '#333' }}>
                {c.picUrl ? <img src={c.picUrl} alt="" style={{ width: 18, height: 18 }} /> : null}
                <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{c.picTitle}</span>
              </div>
            ))}
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ borderRadius: style.bannerRadius ?? 12, overflow: 'hidden', aspectRatio: '1500/350', background: '#f2f3f5' }}>
              {banners[0]?.picUrl ? <img src={banners[0].picUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : null}
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10, marginTop: 10 }}>
              {cards.slice(0, 6).map((c, i) => (
                <div key={i} style={{ border: '1px solid #f0f0f0', borderRadius: style.cardRadius ?? 12, padding: 8 }}>
                  <div style={{ fontSize: 13, fontWeight: 700 }}>{c.moduleTitle}</div>
                  <div style={{ fontSize: 11, color: '#fff', background: `linear-gradient(90deg, ${c.moduleSubTitleBgColorA}, ${c.moduleSubTitleBgColorB})`, display: 'inline-block', borderRadius: 3, padding: '0 4px', marginTop: 2 }}>{c.moduleSubTitle}</div>
                  <div style={{ display: 'flex', gap: 6, marginTop: 6 }}>
                    {(c.products || []).slice(0, 2).map((p: any, j: number) => (
                      <div key={j} style={{ flex: 1 }}>
                        <div style={{ aspectRatio: '1/1', background: '#f7f8fa', borderRadius: 6, overflow: 'hidden' }}>
                          {p.pic?.picUrl ? <img src={p.pic.picUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : null}
                        </div>
                        {Number(c.showPrice) === 1 ? <div style={{ fontSize: 11, color: style.priceColor || '#e1251b' }}>{p.price}</div> : null}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
          <div style={{ width: style.rightWidth ?? 260, flexShrink: 0 }}>
            <div style={{ borderRadius: style.panelRadius ?? 8, border: '1px solid #f0f0f0', padding: 12, background: `linear-gradient(${getPath(m, 'right.userCardGradientType') === 'leftRight' ? '90deg' : '180deg'}, ${getPath(m, 'right.userCardGradientColorA') || '#FFEDE1'}, ${getPath(m, 'right.userCardGradientColorB') || '#FCF9F8'})` }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                {getPath(m, 'right.user.avatar.picUrl') ? <img src={getPath(m, 'right.user.avatar.picUrl')} alt="" style={{ width: 36, height: 36, borderRadius: '50%' }} /> : null}
                <div>
                  <div style={{ fontSize: 13, fontWeight: 700 }}>{getPath(m, 'right.user.nickname')}</div>
                  <div style={{ fontSize: 11, color: style.mutedColor || '#999' }}>{getPath(m, 'right.user.welcomeText')}</div>
                </div>
              </div>
              <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
                {((m.right?.stats || []) as Array<{ label: string; value: string }>).map((s, i) => (
                  <div key={i} style={{ fontSize: 11, color: '#666' }}>
                    <b>{s.value}</b>
                    <div style={{ color: '#999' }}>{s.label}</div>
                  </div>
                ))}
              </div>
            </div>
            <div style={{ marginTop: 10, fontSize: 13, fontWeight: 700 }}>
              {getPath(m, 'right.shopsTitle')}
              {getPath(m, 'right.shopsSubTitle') ? <span style={{ fontSize: 10, color: '#fff', background: getPath(m, 'right.shopsSubTitleBg') || '#FF4E4E', borderRadius: 3, padding: '0 4px', marginLeft: 6 }}>{getPath(m, 'right.shopsSubTitle')}</span> : null}
            </div>
            <div style={{ display: 'flex', gap: 10, marginTop: 6, color: getPath(m, 'right.shopsTextColor') || '#a06a38', fontSize: 12 }}>
              {((m.right?.shortcuts || []) as PicItem[]).map((s, i) => (
                <div key={i} style={{ textAlign: 'center' }}>
                  <div style={{ width: 34, height: 34, borderRadius: 8, background: '#f7f8fa' }} />
                  <div style={{ marginTop: 2 }}>{s.picTitle}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      );
    }
    default:
      return <span>{MODULE_DEF_MAP.get(type)?.summary ? MODULE_DEF_MAP.get(type)!.summary!(data) : type}</span>;
  }
}

/* ===================== 页面设置（对齐原版 commonPage/Edit.vue） ===================== */

function PageSettingsPanel({
  pageModule,
  legacyCount,
  newCount,
  onChange,
  onSwitchVersion,
}: {
  pageModule: Record<string, any>;
  legacyCount: number;
  newCount: number;
  onChange: (next: Record<string, any>) => void;
  onSwitchVersion: (target: 'new' | 'old') => void;
}) {
  const currentVersion = pageModule.homeStyleVersion === 'old' ? 'old' : 'new';
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
      <Text strong>页面设置</Text>

      <div>
        <Text type="tertiary" size="small">页面标题（可不填，默认为页面名称）</Text>
        <Input value={pageModule.title || ''} onChange={(v) => onChange({ ...pageModule, title: v })} placeholder="请输入页面标题" />
      </div>

      {/* 头部风格仅服务旧版头部；新版 PC 首页走 pcHeader，故新版时隐藏（原版同逻辑） */}
      {currentVersion === 'old' && (
        <div>
          <Text type="tertiary" size="small">头部风格</Text>
          <RadioGroup
            value={Number(pageModule.headerStyle) === 2 ? 2 : 1}
            onChange={(e: any) => onChange({ ...pageModule, headerStyle: Number(e.target.value) })}
            type="button"
          >
            <Radio value={1}>默认风格</Radio>
            <Radio value={2}>简约风格</Radio>
          </RadioGroup>
        </div>
      )}

      <div>
        <Text type="tertiary" size="small">标题栏背景颜色</Text>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <input
            type="color"
            value={pageModule.titleBackgroundColor || '#ffffff'}
            onChange={(e) => onChange({ ...pageModule, titleBackgroundColor: e.target.value })}
            style={{ width: 46, height: 30, border: '1px solid var(--semi-color-border)', borderRadius: 4, cursor: 'pointer' }}
          />
          <Button size="small" onClick={() => onChange({ ...pageModule, titleBackgroundColor: '' })}>重置</Button>
        </div>
      </div>

      <div>
        <Text type="tertiary" size="small">页面背景颜色</Text>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <input
            type="color"
            value={pageModule.backgroundColor || '#ffffff'}
            onChange={(e) => onChange({ ...pageModule, backgroundColor: e.target.value })}
            style={{ width: 46, height: 30, border: '1px solid var(--semi-color-border)', borderRadius: 4, cursor: 'pointer' }}
          />
          <Button size="small" onClick={() => onChange({ ...pageModule, backgroundColor: '' })}>重置</Button>
        </div>
      </div>

      <div>
        <Text type="tertiary" size="small">背景图</Text>
        <Input
          value={typeof pageModule.backgroundImage === 'string' ? pageModule.backgroundImage : pageModule.backgroundImage?.picUrl || ''}
          onChange={(v) => onChange({ ...pageModule, backgroundImage: v ? { picUrl: v, picThumb: v } : { picUrl: '', picThumb: '' } })}
          placeholder="https://..."
        />
      </div>

      <div>
        <Text type="tertiary" size="small">背景图显示</Text>
        <RadioGroup
          value={Number(pageModule.backgroundRepeat) || 1}
          onChange={(e: any) => onChange({ ...pageModule, backgroundRepeat: Number(e.target.value) })}
          type="button"
        >
          <Radio value={1}>不平铺</Radio>
          <Radio value={2}>平铺</Radio>
          <Radio value={3}>纵向平铺</Radio>
          <Radio value={4}>横向平铺</Radio>
        </RadioGroup>
      </div>

      <div>
        <Text type="tertiary" size="small">背景图填充大小</Text>
        <RadioGroup
          value={Number(pageModule.backgroundSize) || 1}
          onChange={(e: any) => onChange({ ...pageModule, backgroundSize: Number(e.target.value) })}
          type="button"
        >
          <Radio value={1}>横向100%</Radio>
          <Radio value={2}>纵向100%</Radio>
          <Radio value={3}>100%填充</Radio>
        </RadioGroup>
      </div>

      <div style={{ borderTop: '1px solid var(--semi-color-border)', paddingTop: 12 }}>
        <Text type="tertiary" size="small">首页装修版本</Text>
        <RadioGroup
          value={currentVersion}
          onChange={(e: any) => onSwitchVersion(e.target.value as 'new' | 'old')}
          type="button"
          style={{ marginTop: 4 }}
        >
          <Radio value="new">新版首页</Radio>
          <Radio value="old">旧版首页</Radio>
        </RadioGroup>
        <div style={{ marginTop: 8, display: 'flex', flexDirection: 'column', gap: 4 }}>
          <Text type="tertiary" size="small"><b>新版首页：</b>支持可视化网格、高级轮播等新模块，顶部带 PC 头部实时预览，排版更现代。当前 {newCount} 个模块。</Text>
          <Text type="tertiary" size="small"><b>旧版首页：</b>使用传统全宽占位模块，无头部预览，适合维护旧有页面。当前 {legacyCount} 个模块。</Text>
        </div>
      </div>
    </div>
  );
}

/* ===================== PC 头部装修（pageModule.pcHeader，契约透传） ===================== */

const PC_HEADER_DEFAULTS: Record<string, any> = {
  styleType: 1,
  isWhite: 1,
  showTopMenuBar: 1,
  topMenuTextColor: '',
  topMenuTextHoverColor: '',
  showTopRegion: 1,
  topRegionText: '中国江西',
  showTopUserMenu: 1,
  showTopMemberCenter: 1,
  memberCenterItems: [
    { picTitle: '我的足迹', picDesc: '', picLink: { link: '/member/browsingHistory/list', name: '我的足迹', label: '我的足迹' } },
    { picTitle: '我的优惠券', picDesc: '', picLink: { link: '/member/coupon/list', name: '我的优惠券', label: '我的优惠券' } },
  ],
  showTopMemberName: 1,
  showTopMessage: 1,
  showTopOrders: 1,
  showTopSiteNav: 1,
  showTopAd: 1,
  topAdPic: { picUrl: '', picThumb: '' },
  logoPic: { picUrl: '', picThumb: '' },
  logoAlign: 1,
  logoWidth: 100,
  logoHeight: 25,
  rightCartShow: 1,
  cartStyle: 1,
  cartPic: { picUrl: '', picThumb: '' },
  topMenuCartShow: 1,
  searchStyle: 1,
  searchColorType: 1,
  searchColor: '',
  showCategory: 1,
  showHomeCategory: 1,
  categoryMarginTop: 0,
  showMainNav: 1,
  mainNavPaddingTop: 0,
  mainNavPaddingBottom: 0,
  showBenefitBar: 1,
  benefitBarBackgroundColor: '',
  benefitTitleColor: '',
  benefitDescColor: '',
  benefitNoticeGroups: [],
};

function PcHeaderPanel({
  pageModule,
  onChange,
}: {
  pageModule: Record<string, any>;
  onChange: (next: Record<string, any>) => void;
}) {
  const header: Record<string, any> = { ...PC_HEADER_DEFAULTS, ...(pageModule.pcHeader || {}) };
  const set = (key: string, value: unknown) => {
    onChange({ ...pageModule, pcHeader: { ...header, [key]: value } });
  };
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
      <Text strong>PC头部装修</Text>
      <HeaderRow label="头部风格">
        <RadioGroup value={header.styleType} onChange={(e: any) => set('styleType', Number(e.target.value))}>
          <Radio value={1}>简约风</Radio>
          <Radio value={2}>跨境风</Radio>
        </RadioGroup>
      </HeaderRow>
      <HeaderRow label="LOGO 图片 URL">
        <Input value={header.logoPic?.picUrl || ''} onChange={(v) => set('logoPic', { ...header.logoPic, picUrl: v, picThumb: v })} placeholder="https://..." />
      </HeaderRow>
      <HeaderRow label="LOGO 宽/高">
        <Space>
          <InputNumber value={header.logoWidth} onChange={(v) => set('logoWidth', v ?? 100)} style={{ width: 90 }} />
          <InputNumber value={header.logoHeight} onChange={(v) => set('logoHeight', v ?? 25)} style={{ width: 90 }} />
        </Space>
      </HeaderRow>
      <HeaderRow label="搜索框样式">
        <RadioGroup value={header.searchStyle} onChange={(e: any) => set('searchStyle', Number(e.target.value))}>
          <Radio value={1}>样式一</Radio>
          <Radio value={2}>样式二</Radio>
          <Radio value={3}>样式三</Radio>
        </RadioGroup>
      </HeaderRow>
      <HeaderRow label="显示商品分类">
        <Switch checked={Number(header.showCategory) === 1} onChange={(v) => set('showCategory', v ? 1 : 0)} />
      </HeaderRow>
      <HeaderRow label="显示首页分类">
        <Switch checked={Number(header.showHomeCategory) === 1} onChange={(v) => set('showHomeCategory', v ? 1 : 0)} />
      </HeaderRow>
      <HeaderRow label="分类距顶部距离">
        <InputNumber value={header.categoryMarginTop} onChange={(v) => set('categoryMarginTop', v ?? 0)} style={{ width: 120 }} />
      </HeaderRow>
      <HeaderRow label="显示主导航">
        <Switch checked={Number(header.showMainNav) === 1} onChange={(v) => set('showMainNav', v ? 1 : 0)} />
      </HeaderRow>
      <HeaderRow label="主导航上/下内边距">
        <Space>
          <InputNumber value={header.mainNavPaddingTop} onChange={(v) => set('mainNavPaddingTop', v ?? 0)} style={{ width: 90 }} />
          <InputNumber value={header.mainNavPaddingBottom} onChange={(v) => set('mainNavPaddingBottom', v ?? 0)} style={{ width: 90 }} />
        </Space>
      </HeaderRow>
      <HeaderRow label="顶部菜单栏">
        <Switch checked={Number(header.showTopMenuBar) === 1} onChange={(v) => set('showTopMenuBar', v ? 1 : 0)} />
      </HeaderRow>
      <HeaderRow label="地区文字" showIf={Number(header.showTopMenuBar) === 1}>
        <Input value={header.topRegionText || ''} onChange={(v) => set('topRegionText', v)} />
      </HeaderRow>
      <HeaderRow label="购物车图标 URL">
        <Input value={header.cartPic?.picUrl || ''} onChange={(v) => set('cartPic', { ...header.cartPic, picUrl: v, picThumb: v })} placeholder="https://..." />
      </HeaderRow>
      <HeaderRow label="悬浮优惠条">
        <Switch checked={Number(header.showBenefitBar) === 1} onChange={(v) => set('showBenefitBar', v ? 1 : 0)} />
      </HeaderRow>
      <HeaderRow label="优惠条背景色" showIf={Number(header.showBenefitBar) === 1}>
        <input
          type="color"
          value={header.benefitBarBackgroundColor || '#ffffff'}
          onChange={(e) => set('benefitBarBackgroundColor', e.target.value)}
          style={{ width: '100%', height: 30, border: '1px solid var(--semi-color-border)', borderRadius: 4 }}
        />
      </HeaderRow>
      <Text type="tertiary" size="small">其余细节项（顶部消息/订单/站点导航/会员中心菜单等）按契约原样透传。</Text>
    </div>
  );
}

function HeaderRow({ label, children, showIf = true }: { label: string; children: ReactNode; showIf?: boolean }) {
  if (!showIf) return null;
  return (
    <div>
      <Text type="tertiary" size="small" style={{ display: 'block', marginBottom: 4 }}>{label}</Text>
      {children}
    </div>
  );
}

/* ===================== 模块设置面板（字段驱动） ===================== */

function ModuleSettingsPanel({
  def,
  moduleData,
  onChange,
}: {
  def: ModuleDef;
  moduleData: Record<string, any>;
  onChange: (next: Record<string, any>) => void;
}) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
      <Text strong>{def.label}设置</Text>
      {def.note && <Text type="tertiary" size="small">{def.note}</Text>}
      {def.settings.length === 0 && <Text type="tertiary">该模块无可配置项</Text>}
      {def.settings.map((field) => {
        if (field.showIf && !field.showIf(moduleData)) return null;
        return (
          <div key={field.key}>
            {field.kind !== 'title' && field.kind !== 'container' && (
              <Text type="tertiary" size="small" style={{ display: 'block', marginBottom: 4 }}>
                {field.label}
                {field.hint ? `（${field.hint}）` : ''}
              </Text>
            )}
            <FieldControl field={field} moduleData={moduleData} onChange={onChange} />
          </div>
        );
      })}
    </div>
  );
}

/* ===================== 字段控件 ===================== */

let categoryCache: Array<{ label: string; value: number }> | null = null;

function useCategoryOptions() {
  const [options, setOptions] = useState<Array<{ label: string; value: number }>>(categoryCache || []);
  useEffect(() => {
    if (categoryCache) return;
    apiGet<any[]>('/product/category/getAllCategory')
      .then((tree) => {
        const flat: Array<{ label: string; value: number }> = [];
        const traverse = (nodes: any[], prefix = '') => {
          nodes.forEach((n) => {
            const label = prefix ? `${prefix}└─ ${n.categoryName}` : n.categoryName;
            flat.push({ label, value: n.categoryId });
            if (Array.isArray(n.children) && n.children.length > 0) traverse(n.children, `${prefix}    `);
          });
        };
        traverse(Array.isArray(tree) ? tree : []);
        categoryCache = flat;
        setOptions(flat);
      })
      .catch(() => setOptions([]));
  }, []);
  return options;
}

interface ProductRow {
  productId: number;
  productName?: string;
  productPrice?: number | string;
  picUrl?: string;
  picThumb?: string;
}

function ProductPickerModal({
  visible,
  selectedIds,
  multiple = true,
  onOk,
  onCancel,
}: {
  visible: boolean;
  selectedIds: number[];
  multiple?: boolean;
  onOk: (ids: number[], rows: ProductRow[]) => void;
  onCancel: () => void;
}) {
  const [keyword, setKeyword] = useState('');
  const [rows, setRows] = useState<ProductRow[]>([]);
  const [loading, setLoading] = useState(false);
  const [picked, setPicked] = useState<number[]>(selectedIds);

  useEffect(() => {
    if (visible) setPicked(selectedIds);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visible]);

  const load = () => {
    setLoading(true);
    apiGet<{ records?: ProductRow[]; list?: ProductRow[] }>('/product/product/list', {
      keyword: keyword || undefined,
      page: 1,
      size: 20,
      productType: 1,
    })
      .then((res) => setRows(res.records || res.list || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (visible) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visible]);

  const toggle = (row: ProductRow) => {
    setPicked((prev) => {
      if (prev.includes(row.productId)) return prev.filter((id) => id !== row.productId);
      if (!multiple) return [row.productId];
      return [...prev, row.productId];
    });
  };

  return (
    <Modal
      title="选择商品"
      visible={visible}
      width={720}
      onOk={() => onOk(picked, [])}
      onCancel={onCancel}
      okText="确定"
      cancelText="取消"
    >
      <Space style={{ marginBottom: 12, width: '100%' }}>
        <Input value={keyword} onChange={setKeyword} onEnterPress={load} placeholder="输入商品名称搜索" style={{ width: 260 }} showClear onClear={() => setKeyword('')} />
        <Button onClick={load}>搜索</Button>
        <Text type="tertiary" size="small">已选 {picked.length} 个</Text>
      </Space>
      <div style={{ maxHeight: 380, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 8 }}>
        {loading && <Spin />}
        {!loading && rows.length === 0 && <Text type="tertiary">暂无数据</Text>}
        {rows.map((row) => (
          <div
            key={row.productId}
            onClick={() => toggle(row)}
            style={{
              display: 'flex', alignItems: 'center', gap: 10, padding: 8,
              border: `1px solid ${picked.includes(row.productId) ? 'var(--semi-color-primary)' : 'var(--semi-color-border)'}`,
              borderRadius: 6, cursor: 'pointer',
            }}
          >
            {(row.picThumb || row.picUrl) && <img src={row.picThumb || row.picUrl} alt="" style={{ width: 40, height: 40, objectFit: 'cover', borderRadius: 4 }} />}
            <div style={{ flex: 1 }}>
              <div>{row.productName || `商品#${row.productId}`}</div>
              <Text type="tertiary" size="small">¥{row.productPrice ?? '-'}</Text>
            </div>
            {picked.includes(row.productId) && <Tag color="blue">已选</Tag>}
          </div>
        ))}
      </div>
    </Modal>
  );
}

function GrouponPickerModal({
  visible,
  current,
  onOk,
  onCancel,
}: {
  visible: boolean;
  current?: Record<string, any> | null;
  onOk: (item: Record<string, any> | null) => void;
  onCancel: () => void;
}) {
  const [rows, setRows] = useState<Array<Record<string, any>>>([]);
  const [loading, setLoading] = useState(false);
  const [picked, setPicked] = useState<Record<string, any> | null>(current || null);

  useEffect(() => {
    if (!visible) return;
    setLoading(true);
    apiGet<{ records?: Array<Record<string, any>>; list?: Array<Record<string, any>> }>('/promotion/groupon/list', { page: 1, size: 20 })
      .then((res) => setRows(res.records || res.list || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, [visible]);

  return (
    <Modal title="选择拼团活动" visible={visible} width={640} onOk={() => onOk(picked)} onCancel={onCancel} okText="确定" cancelText="取消">
      <div style={{ maxHeight: 380, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 8 }}>
        {loading && <Spin />}
        {!loading && rows.length === 0 && <Text type="tertiary">暂无拼团活动</Text>}
        {rows.map((row) => {
          const id = row.grouponPromotionId ?? row.grouponId ?? row.id;
          const name = row.name || row.title || `活动#${id}`;
          const isPicked = picked && (picked.grouponPromotionId ?? picked.grouponId ?? picked.id) === id;
          return (
            <div
              key={id}
              onClick={() => setPicked({ grouponPromotionId: id, name, groupType: row.groupType, startTime: row.startTime, endTime: row.endTime })}
              style={{
                padding: 10, borderRadius: 6, cursor: 'pointer',
                border: `1px solid ${isPicked ? 'var(--semi-color-primary)' : 'var(--semi-color-border)'}`,
              }}
            >
              <div>{name}</div>
              <Text type="tertiary" size="small">{Number(row.groupType) === 2 ? '阶梯拼团' : '普通拼团'}</Text>
            </div>
          );
        })}
      </div>
    </Modal>
  );
}

function PicListEditor({ value, onChange }: { value: PicItem[]; onChange: (next: PicItem[]) => void }) {
  const list = Array.isArray(value) ? value : [];
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      {list.map((item, index) => (
        <div key={index} style={{ border: '1px solid var(--semi-color-border)', borderRadius: 6, padding: 8, display: 'flex', gap: 8 }}>
          {item.picUrl ? (
            <img src={item.picUrl} alt="" style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 4, flexShrink: 0 }} />
          ) : (
            <div style={{ width: 48, height: 48, borderRadius: 4, background: 'var(--semi-color-fill-0)', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--semi-color-text-2)', fontSize: 16 }}>?</div>
          )}
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 6 }}>
            <Input value={item.picUrl} onChange={(v) => onChange(list.map((it, i) => (i === index ? { ...it, picUrl: v, picThumb: v } : it)))} placeholder="图片 URL" size="small" />
            <Input value={item.picTitle || ''} onChange={(v) => onChange(list.map((it, i) => (i === index ? { ...it, picTitle: v } : it)))} placeholder="文字标题(可选)" size="small" />
            <Input
              value={item.picLink?.link || ''}
              onChange={(v) => onChange(list.map((it, i) => (i === index ? { ...it, picLink: { ...it.picLink, link: v, name: it.picLink?.name || '' } } : it)))}
              placeholder="跳转链接(可选)"
              size="small"
            />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Button size="small" theme="borderless" icon={<IconChevronUp />} disabled={index === 0} onClick={() => {
              const next = [...list];
              [next[index - 1], next[index]] = [next[index], next[index - 1]];
              onChange(next);
            }} />
            <Button size="small" theme="borderless" icon={<IconChevronDown />} disabled={index === list.length - 1} onClick={() => {
              const next = [...list];
              [next[index + 1], next[index]] = [next[index], next[index + 1]];
              onChange(next);
            }} />
            <Button size="small" theme="borderless" type="danger" icon={<IconClose />} onClick={() => onChange(list.filter((_, i) => i !== index))} />
          </div>
        </div>
      ))}
      <Button size="small" icon={<IconPlus />} onClick={() => onChange([...list, { picUrl: '', picThumb: '', picName: '', picTitle: '', picLink: { link: '', name: '' } }])}>
        添加图片
      </Button>
    </div>
  );
}

/** 命名容器（非 frame）的字段集：pc-product-group 的扩展设置 */
const CONTAINER_VARIANTS: Record<string, SettingFieldDef[]> = {
  buyBtn: [
    { kind: 'switch', key: 'showBtn', label: '显示按钮' },
    { kind: 'radio', key: 'btnType', label: '按钮样式', options: [{ label: '样式一', value: 1 }, { label: '样式二', value: 2 }], showIf: (m) => Number(m.showBtn) === 1 },
    { kind: 'input', key: 'text', label: '按钮文字', showIf: (m) => Number(m.showBtn) === 1 },
    { kind: 'color', key: 'textColor', label: '文字颜色', showIf: (m) => Number(m.showBtn) === 1 },
    { kind: 'color', key: 'iconColor', label: '图标颜色', showIf: (m) => Number(m.showBtn) === 1 },
    { kind: 'slider', key: 'paddingLeftRight', label: '左右内边距', max: 10, showIf: (m) => Number(m.showBtn) === 1 },
  ],
  productColor: [
    { kind: 'slider', key: 'infoLeftRightPadding', label: '信息区左右内边距', max: 30 },
    { kind: 'color', key: 'backgroundColor', label: '背景颜色' },
    { kind: 'color', key: 'titleColor', label: '标题颜色' },
    { kind: 'color', key: 'priceColor', label: '价格颜色' },
    { kind: 'color', key: 'crossedOutPriceColor', label: '划线价颜色' },
  ],
  groupTitle: [
    { kind: 'slider', key: 'marginLeftRight', label: '左右间距', max: 30 },
    { kind: 'switch', key: 'showTitle', label: '显示分组标题' },
    { kind: 'select', key: 'bigTitleFontSize', label: '标题大小', options: [{ label: '14', value: 14 }, { label: '16', value: 16 }, { label: '18', value: 18 }], showIf: (m) => Number(m.showTitle) === 1 },
    { kind: 'color', key: 'titleColor', label: '默认颜色', showIf: (m) => Number(m.showTitle) === 1 },
    { kind: 'color', key: 'activeTitleColor', label: '选中颜色', showIf: (m) => Number(m.showTitle) === 1 },
    { kind: 'color', key: 'activeTitleBgColor', label: '选中背景色', showIf: (m) => Number(m.showTitle) === 1 },
    { kind: 'slider', key: 'titleMinWidth', label: '标题最小宽度', min: 0, max: 200, showIf: (m) => Number(m.showTitle) === 1 },
    { kind: 'slider', key: 'titlePaddingTopBottom', label: '上下内边距', max: 30, showIf: (m) => Number(m.showTitle) === 1 },
    { kind: 'slider', key: 'titlePaddingLeftRight', label: '左右内边距', max: 30, showIf: (m) => Number(m.showTitle) === 1 },
    { kind: 'slider', key: 'titleRadius', label: '圆角', max: 30, showIf: (m) => Number(m.showTitle) === 1 },
    { kind: 'color', key: 'splitLineColor', label: '分割线颜色', showIf: (m) => Number(m.showTitle) === 1 },
  ],
  moduleStyle: [
    { kind: 'slider', key: 'boxPaddingTop', label: '上边距', max: 30 },
    { kind: 'slider', key: 'boxPaddingBottom', label: '下边距', max: 30 },
    { kind: 'slider', key: 'boxPadding', label: '左右边距', max: 30 },
    { kind: 'color', key: 'backgroundColor', label: '背景颜色' },
  ],
  contentStyle: [
    { kind: 'slider', key: 'paddingTop', label: '上内边距', max: 30 },
    { kind: 'slider', key: 'paddingBottom', label: '下内边距', max: 30 },
    { kind: 'slider', key: 'paddingLeftRight', label: '左右内边距', max: 30 },
    { kind: 'slider', key: 'boxRadius', label: '圆角', max: 30 },
    { kind: 'color', key: 'backgroundColor', label: '背景颜色' },
    { kind: 'input', key: 'backgroundPic.picUrl', label: '背景图 URL' },
    { kind: 'switch', key: 'boxShadow', label: '阴影' },
  ],
};

function FieldControl({
  field,
  moduleData,
  onChange,
}: {
  field: SettingFieldDef;
  moduleData: Record<string, any>;
  onChange: (next: Record<string, any>) => void;
}) {
  const categoryOptions = useCategoryOptions();
  const [pickerVisible, setPickerVisible] = useState(false);
  const [grouponVisible, setGrouponVisible] = useState(false);
  const value = getPath(moduleData, field.key);

  const set = (v: unknown) => {
    let next = setPath(moduleData, field.key, v);
    if (field.onChangeExtra) next = field.onChangeExtra(next, v);
    onChange(next);
  };

  switch (field.kind) {
    case 'radio':
      return (
        <RadioGroup value={(value ?? field.options?.[0]?.value) as string | number | undefined} onChange={(e: any) => set(Number(e.target.value))}>
          {(field.options || []).map((o) => (
            <Radio key={String(o.value)} value={o.value}>{o.label}</Radio>
          ))}
        </RadioGroup>
      );
    case 'switch':
      return <Switch checked={Number(value) === 1} onChange={(v) => set(v ? 1 : 0)} />;
    case 'select':
      return (
        <Select
          value={(value ?? field.options?.[0]?.value) as string | number | undefined}
          optionList={(field.options || []).map((o) => ({ label: o.label, value: o.value }))}
          style={{ width: 160 }}
          onChange={(v) => set(v)}
        />
      );
    case 'input':
      return <Input value={String(value ?? '')} onChange={(v) => set(v)} />;
    case 'textarea':
      return <TextArea value={String(value ?? '')} onChange={(v: string) => set(v)} rows={2} maxLength={500} />;
    case 'number':
      return (
        <InputNumber
          value={typeof value === 'number' ? value : undefined}
          onChange={(v) => set(v ?? undefined)}
          min={field.min}
          max={field.max}
          style={{ width: 160 }}
        />
      );
    case 'slider':
      return (
        <Slider
          value={typeof value === 'number' ? value : 0}
          min={field.min ?? 0}
          max={field.max ?? 30}
          step={1}
          onChange={(v) => set(typeof v === 'number' ? v : (v as number[])[0])}
        />
      );
    case 'color':
      return (
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <input
            type="color"
            value={String(value || '#ffffff')}
            onChange={(e) => set(e.target.value)}
            style={{ width: 46, height: 30, border: '1px solid var(--semi-color-border)', borderRadius: 4, cursor: 'pointer' }}
          />
          <Button size="small" onClick={() => set('')}>重置</Button>
          <Text type="tertiary" size="small">{String(value || '未设置')}</Text>
        </div>
      );
    case 'picList':
      return <PicListEditor value={(value as PicItem[]) || []} onChange={(v) => set(v)} />;
    case 'pic':
      return <PicListEditor value={value && (value as PicItem).picUrl ? [value as PicItem] : []} onChange={(v) => set(v[0] || { picUrl: '', picThumb: '' })} />;
    case 'link': {
      const link = (value && typeof value === 'object' ? value : { link: '', title: '' }) as Record<string, any>;
      return (
        <div style={{ display: 'flex', gap: 6 }}>
          <Input value={link.link || ''} onChange={(v) => set({ ...link, link: v })} placeholder="链接地址" />
          <Input value={link.title || link.name || ''} onChange={(v) => set({ ...link, title: v, name: v })} placeholder="名称" style={{ width: 110 }} />
        </div>
      );
    }
    case 'linkList': {
      const list = (Array.isArray(value) ? value : []) as Array<{ link?: { link?: string; title?: string }; linkTitle?: string }>;
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          {list.map((item, index) => (
            <div key={index} style={{ display: 'flex', gap: 6 }}>
              <Input value={item.link?.link || ''} onChange={(v) => set(list.map((it, i) => (i === index ? { ...it, link: { ...it.link, link: v } } : it)))} placeholder="链接地址" />
              <Input value={item.linkTitle || ''} onChange={(v) => set(list.map((it, i) => (i === index ? { ...it, linkTitle: v } : it)))} placeholder="名称" style={{ width: 110 }} />
              <Button size="small" theme="borderless" type="danger" icon={<IconClose />} onClick={() => set(list.filter((_, i) => i !== index))} />
            </div>
          ))}
          <Button size="small" icon={<IconPlus />} onClick={() => set([...list, { link: { link: '', title: '' }, linkTitle: '' }])}>
            添加链接
          </Button>
        </div>
      );
    }
    case 'category':
      return (
        <Select
          value={typeof value === 'number' && value > 0 ? value : undefined}
          optionList={categoryOptions}
          showClear
          filter
          style={{ width: '100%' }}
          placeholder="选择商品分类"
          onChange={(v) => set(v ? Number(v) : 0)}
        />
      );
    case 'categoryMulti': {
      const arr = Array.isArray(value) ? value : typeof value === 'number' && value > 0 ? [value] : [];
      return (
        <Select
          value={arr as unknown as string[]}
          optionList={categoryOptions}
          multiple
          showClear
          filter
          style={{ width: '100%' }}
          placeholder="选择商品分类(可多选)"
          onChange={(v) => set(((v as unknown as number[]) || []).map(Number))}
        />
      );
    }
    case 'productIds': {
      const ids = (Array.isArray(value) ? value : []) as number[];
      return (
        <div>
          <Button icon={<IconPlus />} onClick={() => setPickerVisible(true)}>选择商品（已选 {ids.length}）</Button>
          {ids.length > 0 && (
            <div style={{ marginTop: 6, display: 'flex', flexWrap: 'wrap', gap: 4 }}>
              {ids.map((id) => <Tag key={id} size="small">#{id}</Tag>)}
            </div>
          )}
          {pickerVisible && (
            <ProductPickerModal visible selectedIds={ids} onOk={(nextIds) => { set(nextIds); setPickerVisible(false); }} onCancel={() => setPickerVisible(false)} />
          )}
        </div>
      );
    }
    case 'products': {
      const products = (value && typeof value === 'object' ? value : productsDefaults()) as Record<string, any>;
      const setProducts = (next: Record<string, any>) => set(next);
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <div>
            <Text type="tertiary" size="small" style={{ display: 'block', marginBottom: 4 }}>选品方式</Text>
            <RadioGroup value={products.productSelectType ?? 1} onChange={(e: any) => setProducts({ ...products, productSelectType: Number(e.target.value) })}>
              <Radio value={1}>选商品</Radio>
              <Radio value={2}>按分类</Radio>
              <Radio value={3}>按标签</Radio>
            </RadioGroup>
          </div>
          {Number(products.productSelectType) === 1 && (
            <div>
              <Button icon={<IconPlus />} onClick={() => setPickerVisible(true)}>选择商品（已选 {((products.productIds || []) as number[]).length}）</Button>
              {pickerVisible && (
                <ProductPickerModal visible selectedIds={(products.productIds || []) as number[]} onOk={(nextIds) => { setProducts({ ...products, productIds: nextIds }); setPickerVisible(false); }} onCancel={() => setPickerVisible(false)} />
              )}
            </div>
          )}
          {Number(products.productSelectType) === 2 && (
            <Select value={products.productCategoryId || undefined} optionList={categoryOptions} showClear filter placeholder="选择分类" onChange={(v) => setProducts({ ...products, productCategoryId: v ? Number(v) : 0 })} />
          )}
          {Number(products.productSelectType) === 3 && (
            <Input value={products.productTag || ''} onChange={(v) => setProducts({ ...products, productTag: v })} placeholder="商品标签" />
          )}
          <div>
            <Text type="tertiary" size="small" style={{ display: 'block', marginBottom: 4 }}>商品数量</Text>
            <InputNumber
              value={products.productNumber ?? 10}
              onChange={(v) => setProducts({ ...products, productNumber: v ?? 10 })}
              min={field.numberMin ?? 1}
              max={field.numberMax}
              style={{ width: 140 }}
            />
          </div>
        </div>
      );
    }
    case 'groupon': {
      const item = value as Record<string, any> | undefined | null;
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          {item ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Tag color="blue">{item.name || `活动#${item.grouponPromotionId}`}</Tag>
              <Button size="small" theme="borderless" type="danger" onClick={() => set(null)}>移除</Button>
            </div>
          ) : (
            <Text type="tertiary" size="small">未选择活动</Text>
          )}
          <Button icon={<IconPlus />} onClick={() => setGrouponVisible(true)}>选择拼团活动</Button>
          {grouponVisible && (
            <GrouponPickerModal visible current={item || null} onOk={(next) => { set(next); setGrouponVisible(false); }} onCancel={() => setGrouponVisible(false)} />
          )}
        </div>
      );
    }
    case 'title': {
      const titleData = (value && typeof value === 'object' ? value : standardTitle()) as Record<string, any>;
      return (
        <div style={{ border: '1px solid var(--semi-color-border)', borderRadius: 8, padding: 12, display: 'flex', flexDirection: 'column', gap: 12 }}>
          <Text strong size="small">{field.label}</Text>
          {titleFields({ moreType: true, verticalPadding: Object.prototype.hasOwnProperty.call(titleData, 'titlePaddingTop'), hideAlign: field.titleHideAlign }).map((f) => {
            if (f.showIf && !f.showIf({ title: titleData })) return null;
            return (
              <div key={f.key}>
                <Text type="tertiary" size="small" style={{ display: 'block', marginBottom: 4 }}>{f.label}{f.hint ? `（${f.hint}）` : ''}</Text>
                <FieldControl field={{ ...f, key: f.key.replace('title.', '') }} moduleData={titleData} onChange={(next) => set(next)} />
              </div>
            );
          })}
        </div>
      );
    }
    case 'container': {
      if (field.containerType && field.containerType !== 'frame') {
        const group = (value && typeof value === 'object' ? value : {}) as Record<string, any>;
        return (
          <div style={{ border: '1px solid var(--semi-color-border)', borderRadius: 8, padding: 12, display: 'flex', flexDirection: 'column', gap: 12 }}>
            <Text strong size="small">{field.label}</Text>
            {(CONTAINER_VARIANTS[field.containerType] || []).map((f) => {
              if (f.showIf && !f.showIf(group)) return null;
              return (
                <div key={f.key}>
                  <Text type="tertiary" size="small" style={{ display: 'block', marginBottom: 4 }}>{f.label}</Text>
                  <FieldControl field={f} moduleData={group} onChange={(next) => set(next)} />
                </div>
              );
            })}
          </div>
        );
      }
      const frame = (getPath(moduleData, field.key) && typeof getPath(moduleData, field.key) === 'object' ? getPath(moduleData, field.key) : standardFrame()) as Record<string, any>;
      return (
        <div style={{ border: '1px solid var(--semi-color-border)', borderRadius: 8, padding: 12, display: 'flex', flexDirection: 'column', gap: 12 }}>
          <Text strong size="small">{field.label || '容器设置'}</Text>
          {containerFields().map((f) => (
            <div key={f.key}>
              <Text type="tertiary" size="small" style={{ display: 'block', marginBottom: 4 }}>{f.label}</Text>
              <FieldControl field={{ ...f, key: f.key.replace('frame.', '') }} moduleData={frame} onChange={(next) => set(next)} />
            </div>
          ))}
        </div>
      );
    }
    case 'heroCards': {
      const cards = (Array.isArray(value) ? value : []) as Array<Record<string, any>>;
      const updateCard = (index: number, updater: (c: Record<string, any>) => Record<string, any>) => {
        set(cards.map((c, i) => (i === index ? updater(c) : c)));
      };
      const labelColors = (prefix: string, label: string) => [
        { kind: 'color' as const, key: `${prefix}BgColorA`, label: `${label}色A` },
        { kind: 'color' as const, key: `${prefix}BgColorB`, label: `${label}色B` },
      ];
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {cards.map((card, ci) => (
            <div key={ci} style={{ border: '1px solid var(--semi-color-border)', borderRadius: 8, padding: 10, display: 'flex', flexDirection: 'column', gap: 8 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                <Tag size="small" color="blue">卡位 {ci + 1}</Tag>
                <Input
                  value={card.moduleTitle || ''}
                  onChange={(v) => updateCard(ci, (c) => ({ ...c, moduleTitle: v }))}
                  placeholder="卡片标题"
                  size="small"
                />
              </div>
              <div style={{ display: 'flex', gap: 6 }}>
                <Input value={card.moduleSubTitle || ''} onChange={(v) => updateCard(ci, (c) => ({ ...c, moduleSubTitle: v }))} placeholder="子标题" size="small" />
                <input
                  type="color"
                  value={card.moduleSubTitleBgColorA || '#ff4e4e'}
                  onChange={(e) => updateCard(ci, (c) => ({ ...c, moduleSubTitleBgColorA: e.target.value }))}
                  style={{ width: 34, height: 28, border: '1px solid var(--semi-color-border)', borderRadius: 4, cursor: 'pointer' }}
                  title="子标题背景色A"
                />
                <input
                  type="color"
                  value={card.moduleSubTitleBgColorB || '#e1251b'}
                  onChange={(e) => updateCard(ci, (c) => ({ ...c, moduleSubTitleBgColorB: e.target.value }))}
                  style={{ width: 34, height: 28, border: '1px solid var(--semi-color-border)', borderRadius: 4, cursor: 'pointer' }}
                  title="子标题背景色B"
                />
                <Select
                  value={card.moduleSubTitleBgType || 'leftRight'}
                  optionList={[{ label: '左右渐变', value: 'leftRight' }, { label: '上下渐变', value: 'upDown' }, { label: '对角渐变', value: 'diagonal' }]}
                  style={{ width: 110 }}
                  onChange={(v) => updateCard(ci, (c) => ({ ...c, moduleSubTitleBgType: v }))}
                />
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                <Text type="tertiary" size="small">显示价格</Text>
                <Switch checked={Number(card.showPrice) === 1} onChange={(v) => updateCard(ci, (c) => ({ ...c, showPrice: v ? 1 : 0 }))} />
                <Input
                  value={card.link?.link || ''}
                  onChange={(v) => updateCard(ci, (c) => ({ ...c, link: { ...c.link, link: v, name: c.link?.name || '' } }))}
                  placeholder="跳转链接(可选)"
                  size="small"
                  style={{ flex: 1 }}
                />
              </div>
              {((card.products || []) as Array<Record<string, any>>).map((p, pi) => (
                <div key={pi} style={{ borderTop: '1px dashed var(--semi-color-border)', paddingTop: 6, display: 'flex', flexDirection: 'column', gap: 4 }}>
                  <Text type="tertiary" size="small">商品槽 {pi + 1}</Text>
                  <Input
                    value={p.pic?.picUrl || ''}
                    onChange={(v) => updateCard(ci, (c) => ({
                      ...c,
                      products: (c.products || []).map((x: any, xi: number) => (xi === pi ? { ...x, pic: { ...(x.pic || {}), picUrl: v, picThumb: v } } : x)),
                    }))}
                    placeholder="商品图 URL"
                    size="small"
                  />
                  <div style={{ display: 'flex', gap: 6 }}>
                    <Input value={p.price || ''} onChange={(v) => updateCard(ci, (c) => ({ ...c, products: (c.products || []).map((x: any, xi: number) => (xi === pi ? { ...x, price: v } : x)) }))} placeholder="价格如 ¥88.8" size="small" />
                    <Input value={p.label || ''} onChange={(v) => updateCard(ci, (c) => ({ ...c, products: (c.products || []).map((x: any, xi: number) => (xi === pi ? { ...x, label: v } : x)) }))} placeholder="角标文字(可选)" size="small" />
                  </div>
                  {p.label ? (
                    <div style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
                      {labelColors('label', '角标').map((lc) => (
                        <input
                          key={lc.key}
                          type="color"
                          value={String((p as Record<string, any>)[lc.key.replace('label', 'label')] || '#ff9000')}
                          onChange={(e) => updateCard(ci, (c) => ({
                            ...c,
                            products: (c.products || []).map((x: any, xi: number) => (xi === pi ? { ...x, [lc.key]: e.target.value } : x)),
                          }))}
                          style={{ width: 34, height: 26, border: '1px solid var(--semi-color-border)', borderRadius: 4, cursor: 'pointer' }}
                          title={lc.label}
                        />
                      ))}
                    </div>
                  ) : null}
                </div>
              ))}
            </div>
          ))}
        </div>
      );
    }
    case 'hotarea': {
      const pics = (getPath(moduleData, 'picList') || []) as Array<PicItem & { hotarea?: Array<Record<string, any>> }>;
      const updatePics = (nextPics: Array<PicItem & { hotarea?: Array<Record<string, any>> }>) => {
        onChange(setPath(moduleData, 'picList', nextPics));
      };
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {pics.length === 0 && <Text type="tertiary" size="small">请先在上方添加热图</Text>}
          {pics.map((pic, pi) => {
            const hotareas = pic.hotarea || [];
            const updateHotareas = (next: Array<Record<string, any>>) => {
              updatePics(pics.map((p, i) => (i === pi ? { ...p, hotarea: next } : p)));
            };
            return (
              <div key={pi} style={{ border: '1px solid var(--semi-color-border)', borderRadius: 6, padding: 8 }}>
                <Text strong size="small">图 {pi + 1} 的热区</Text>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 6 }}>
                  {hotareas.map((h, index) => (
                    <div key={index} style={{ display: 'flex', gap: 4, alignItems: 'center', flexWrap: 'wrap' }}>
                      <InputNumber value={h.width} onChange={(v) => updateHotareas(hotareas.map((x, i) => (i === index ? { ...x, width: v ?? 0 } : x)))} placeholder="宽" style={{ width: 70 }} />
                      <InputNumber value={h.height} onChange={(v) => updateHotareas(hotareas.map((x, i) => (i === index ? { ...x, height: v ?? 0 } : x)))} placeholder="高" style={{ width: 70 }} />
                      <InputNumber value={h.top} onChange={(v) => updateHotareas(hotareas.map((x, i) => (i === index ? { ...x, top: v ?? 0 } : x)))} placeholder="上" style={{ width: 70 }} />
                      <InputNumber value={h.left} onChange={(v) => updateHotareas(hotareas.map((x, i) => (i === index ? { ...x, left: v ?? 0 } : x)))} placeholder="左" style={{ width: 70 }} />
                      <Input value={h.link?.link || ''} onChange={(v) => updateHotareas(hotareas.map((x, i) => (i === index ? { ...x, link: { ...x.link, link: v } } : x)))} placeholder="链接" style={{ flex: 1, minWidth: 100 }} />
                      <Button size="small" theme="borderless" type="danger" icon={<IconClose />} onClick={() => updateHotareas(hotareas.filter((_, i) => i !== index))} />
                    </div>
                  ))}
                  <Button size="small" icon={<IconPlus />} onClick={() => updateHotareas([...hotareas, { width: 100, height: 60, top: 0, left: 0, link: { link: '', name: '' } }])}>
                    添加热区
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      );
    }
    case 'groupList': {
      const groups = (Array.isArray(value) ? value : []) as Array<Record<string, any>>;
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {groups.map((g, index) => (
            <div key={index} style={{ border: '1px solid var(--semi-color-border)', borderRadius: 6, padding: 8, display: 'flex', flexDirection: 'column', gap: 6 }}>
              <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                <Input value={g.title || ''} onChange={(v) => set(groups.map((x, i) => (i === index ? { ...x, title: v } : x)))} placeholder="分组标题" />
                <Button size="small" theme="borderless" type="danger" icon={<IconClose />} onClick={() => set(groups.filter((_, i) => i !== index))} />
              </div>
              <RadioGroup value={g.sourceType || 'products'} onChange={(e: any) => set(groups.map((x, i) => (i === index ? { ...x, sourceType: e.target.value } : x)))}>
                <Radio value="products">选商品</Radio>
                <Radio value="productGroup">商品分组</Radio>
              </RadioGroup>
              {(g.sourceType || 'products') === 'products' ? (
                <div>
                  <Button size="small" icon={<IconPlus />} onClick={() => setPickerVisible(true)}>选择商品（已选 {((g.productIds || []) as number[]).length}）</Button>
                  {pickerVisible && (
                    <ProductPickerModal visible selectedIds={(g.productIds || []) as number[]} onOk={(nextIds) => { set(groups.map((x, i) => (i === index ? { ...x, productIds: nextIds } : x))); setPickerVisible(false); }} onCancel={() => setPickerVisible(false)} />
                  )}
                </div>
              ) : (
                <InputNumber value={g.productGroupId} onChange={(v) => set(groups.map((x, i) => (i === index ? { ...x, productGroupId: v ?? undefined } : x)))} placeholder="商品分组 ID" style={{ width: 160 }} />
              )}
            </div>
          ))}
          <Button size="small" icon={<IconPlus />} onClick={() => set([...groups, { sourceType: 'products', title: `分组${groups.length + 1}`, titlePic: { picUrl: '', picThumb: '' }, titleLink: { link: '', name: '' }, bannerPic: { picUrl: '', picThumb: '' }, bannerLink: { link: '', name: '' }, productIds: [], productList: [] }])}>
            添加分组
          </Button>
        </div>
      );
    }
    default:
      return null;
  }
}
