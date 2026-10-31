/**
 * PC 首页装修编辑器（对齐原版 /decorate/pc 独立编辑器）
 *
 * 数据契约（与原版逐字段一致，透传不裁剪）：
 * - data = { pageModule, moduleList }
 * - pageModule = { type:'page', module:{}, backgroundRepeat:'', backgroundSize:'', style:0,
 *                  headerStyle:1, backgroundColor?, backgroundImage?, homeStyleVersion?, pcHeader? }
 * - moduleList 项 = { moduleIndex: Date.now(), type, url:'pc', label, isShow, module:{...defaults} }
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

/* ===================== 模块契约注册表（原版 defaults 逐字段照抄） ===================== */

interface PicItem {
  picUrl: string;
  picThumb?: string;
  picTitle?: string;
  picDesc?: string;
  picLink?: { link?: string; title?: string };
}

const emptyPic = (): PicItem => ({ picUrl: '', picThumb: '', picTitle: '', picDesc: '', picLink: { link: '', title: '' } });

const bannerDefaults = () => ({
  bannerStyle: 1,
  showCat: 1,
  logo: { picUrl: '', picThumb: '' },
  picList: [] as PicItem[],
  picList2: [] as PicItem[],
  picList3: [] as PicItem[],
});

const productsDefaults = () => ({
  productSelectType: 1,
  productCategoryId: 1,
  productNumber: 8,
  productIds: [] as number[],
  productTag: '',
});

const grouponFrameDefaults = () => ({
  textColor: '', itemBackgroundColor: '', backgroundColor: '#ffffff',
  innerPadding: 0, itemHeight: 35, itemRadius: 3, boxRadius: 0, boxPadding: 10,
  leftAndRightPadding: 0, boxPaddingTop: 5, boxPaddingBottom: 5,
});

const grouponTitleDefaults = () => ({
  showTitle: 0, titleStyle: 1, titleAlign: 1,
  titleBackground: '', titleBackground2: '',
  titleBackgroundPic: { picUrl: '', picThumb: '' },
  titleRadius: 0, titleText: '标题内容', titleColor: '', titleWeight: 'normal',
  descText: '', descColor: '#aaaaaa', showMore: 0, moreType: 1,
  countdownLabel: '', moreLink: [], moreColor: '#aaaaaa',
  format: { titleBackground: '', titleRadius: '' },
});

interface SettingFieldDef {
  kind:
    | 'radio' | 'input' | 'textarea' | 'number' | 'slider'
    | 'color' | 'picList' | 'pic' | 'link' | 'linkList'
    | 'category' | 'categoryMulti' | 'products' | 'productIds' | 'groupon';
  key: string;
  label: string;
  hint?: string;
  options?: Array<{ label: string; value: number | string }>;
  max?: number;
  min?: number;
  numberMin?: number;
  numberStep?: number;
  showIf?: (m: Record<string, any>) => boolean;
}

interface ModuleDef {
  type: string;
  label: string;
  group: string;
  overseasOnly?: boolean;
  note?: string;
  defaults: () => Record<string, any>;
  settings: SettingFieldDef[];
  summary?: (m: Record<string, any>) => string;
}

export const PC_MODULE_DEFS: ModuleDef[] = [
  {
    type: 'pcBanner', label: '轮播Banner', group: '轮播广告',
    defaults: bannerDefaults,
    settings: [
      { kind: 'radio', key: 'bannerStyle', label: '风格', options: [{ label: '默认风格', value: 1 }, { label: '简约风格', value: 2 }] },
      { kind: 'picList', key: 'picList', label: '轮播大图', hint: '建议 1180*940 / 750*479' },
      { kind: 'picList', key: 'picList2', label: '右侧轮播小图', hint: '380*300，建议 3-6 个', showIf: (m) => m.bannerStyle === 1 },
      { kind: 'picList', key: 'picList3', label: '右侧会员栏图标', hint: '60*60，最多 6 个', showIf: (m) => m.bannerStyle === 1 },
      { kind: 'radio', key: 'showCat', label: '显示分类栏', options: [{ label: '显示', value: 1 }, { label: '不显示', value: 0 }], showIf: (m) => m.bannerStyle === 2 },
    ],
    summary: (m) => `风格${m.bannerStyle === 1 ? '默认' : '简约'} · ${((m.picList || []) as PicItem[]).length} 图`,
  },
  {
    type: 'pcBanner2', label: '轮播Banner2', group: '轮播广告', overseasOnly: true,
    defaults: bannerDefaults,
    settings: [
      { kind: 'picList', key: 'picList', label: '轮播图', hint: '建议 1264*280' },
    ],
    summary: (m) => `${((m.picList || []) as PicItem[]).length} 图`,
  },
  {
    type: 'pcGroupBanner', label: '分组轮播Banner', group: '轮播广告', overseasOnly: true,
    defaults: () => ({ ...bannerDefaults(), number: undefined as unknown as number }),
    settings: [
      { kind: 'picList', key: 'picList', label: 'Banner图', hint: '1180*940 / 750*479' },
      { kind: 'number', key: 'number', label: '一组显示数量', min: 1, max: 3, hint: '最多显示 3 个' },
    ],
    summary: (m) => `每组 ${m.number || '-'} 个`,
  },
  {
    type: 'pcImageAd', label: '通宽广告图', group: '轮播广告',
    defaults: () => ({ picList: [] as PicItem[], boxPaddingTop: 10, boxPaddingBottom: 10 }),
    settings: [
      { kind: 'picList', key: 'picList', label: '广告图', hint: '单图占屏宽 100% 建议 750px；多图为轮播' },
      { kind: 'slider', key: 'boxPaddingTop', label: '上边距', max: 30 },
      { kind: 'slider', key: 'boxPaddingBottom', label: '下边距', max: 30 },
    ],
    summary: (m) => `${((m.picList || []) as PicItem[]).length} 图 · 边距 ${m.boxPaddingTop}/${m.boxPaddingBottom}`,
  },
  {
    type: 'pcCustomAd1', label: '自定义模块一', group: '轮播广告',
    defaults: () => ({ picList: [] as PicItem[], picList2: [] as PicItem[], picList3: [] as PicItem[] }),
    settings: [
      { kind: 'picList', key: 'picList', label: '左侧大图', hint: '540*800 / 270*400' },
      { kind: 'picList', key: 'picList2', label: '中间每日推荐', hint: '首张 500*720，其余 180*180' },
      { kind: 'picList', key: 'picList3', label: '右侧热销精品', hint: '首张 420*420' },
    ],
    summary: (m) => `${((m.picList || []) as PicItem[]).length}/${((m.picList2 || []) as PicItem[]).length}/${((m.picList3 || []) as PicItem[]).length} 图`,
  },
  {
    type: 'pcCustomAd2', label: '自定义模块二', group: '轮播广告',
    defaults: () => ({ linkList: [], picList: [] as PicItem[], picList2: [] as PicItem[], picList3: [] as PicItem[] }),
    settings: [
      { kind: 'picList', key: 'picList', label: '左侧大图(单图)', hint: '540*800 / 270*400' },
      { kind: 'linkList', key: 'linkList', label: '左侧链接', hint: '超过 6 个不显示' },
      { kind: 'picList', key: 'picList2', label: '中间广告图', hint: '首张 848*460，其余 300*300' },
      { kind: 'picList', key: 'picList3', label: '右侧广告图', hint: '200*200，最多 6 张' },
    ],
    summary: (m) => `${((m.picList || []) as PicItem[]).length} 图 · ${((m.linkList || []) as unknown[]).length} 链接`,
  },
  {
    type: 'pcCatProductNormal', label: '分类商品-窄', group: '商品楼层',
    defaults: () => ({
      categoryId: 0, title: '', shortTitle: '', enTitle: '', color: '', color2: '',
      logo: { picUrl: '', picThumb: '' }, picList: [] as PicItem[], picList2: [] as PicItem[],
    }),
    settings: [
      { kind: 'category', key: 'categoryId', label: '商品分类' },
      { kind: 'input', key: 'title', label: '标题名称' },
      { kind: 'input', key: 'shortTitle', label: '短标题' },
      { kind: 'input', key: 'enTitle', label: '英文标题' },
      { kind: 'color', key: 'color', label: '主颜色' },
      { kind: 'color', key: 'color2', label: '副颜色(渐变)' },
      { kind: 'picList', key: 'picList', label: '左侧轮播图', hint: '492*764，建议 2 张以上' },
      { kind: 'picList', key: 'picList2', label: '右侧四小图', hint: '492*764，超过 4 张不显示' },
    ],
    summary: (m) => `${m.title || '未命名'} · ${((m.picList || []) as PicItem[]).length} 图`,
  },
  {
    type: 'pcCatProductWide', label: '分类商品-宽', group: '商品楼层',
    defaults: () => ({
      categoryId: 0, title: '', shortTitle: '', enTitle: '', color: '', color2: '',
      logo: { picUrl: '', picThumb: '' }, picList: [] as PicItem[], picList2: [] as PicItem[],
      productIds: [] as number[], linkList: [],
    }),
    settings: [
      { kind: 'categoryMulti', key: 'categoryId', label: '商品分类(多选)' },
      { kind: 'input', key: 'title', label: '标题名称' },
      { kind: 'input', key: 'shortTitle', label: '短标题' },
      { kind: 'input', key: 'enTitle', label: '英文标题' },
      { kind: 'color', key: 'color', label: '主颜色' },
      { kind: 'color', key: 'color2', label: '副颜色(渐变)' },
      { kind: 'picList', key: 'picList', label: '左侧轮播图', hint: '492*764' },
      { kind: 'linkList', key: 'linkList', label: '轮播下方链接', hint: '默认显示 1 个，超过 2 个不显示' },
      { kind: 'picList', key: 'picList2', label: '中间广告图', hint: '260*260，超过 6 张不显示' },
      { kind: 'productIds', key: 'productIds', label: '右侧热门商品' },
    ],
    summary: (m) => `${m.title || '未命名'} · 商品 ${((m.productIds || []) as number[]).length}`,
  },
  {
    type: 'pcCatProductSimple', label: '分类商品-简约', group: '商品楼层',
    defaults: () => ({
      baseStyle: 1, title: '', products: productsDefaults(), moreLink: '', picList: [] as PicItem[],
    }),
    settings: [
      { kind: 'radio', key: 'baseStyle', label: '风格', options: [{ label: '风格一', value: 1 }, { label: '风格二', value: 2 }] },
      { kind: 'input', key: 'title', label: '标题' },
      { kind: 'link', key: 'moreLink', label: '更多链接' },
      { kind: 'picList', key: 'picList', label: '左侧大图', hint: '540*800 / 270*400', showIf: (m) => m.baseStyle === 2 },
      { kind: 'products', key: 'products', label: '商品设置' },
    ],
    summary: (m) => `${m.title || '未命名'} · 风格${m.baseStyle} · ${((m.products?.productIds || []) as number[]).length} 商品`,
  },
  {
    type: 'pcRecommendProduct', label: '推荐商品-瀑布流', group: '商品楼层',
    defaults: () => ({ titleName: '', products: { productNumber: 50, productIds: [] as number[] } }),
    settings: [
      { kind: 'input', key: 'titleName', label: '标题名称' },
      { kind: 'products', key: 'products', label: '商品设置', numberMin: 20, numberStep: 10 },
    ],
    summary: (m) => `${m.titleName || '未命名'} · ${((m.products?.productIds || []) as number[]).length} 商品`,
  },
  {
    type: 'pcSeckill', label: '秒杀模块', group: '营销活动',
    defaults: () => ({ seckillTitle: '全场优惠限时9折起' }),
    settings: [
      { kind: 'input', key: 'seckillTitle', label: '活动标题', hint: '秒杀商品由营销-秒杀决定' },
    ],
    summary: (m) => m.seckillTitle || '未设置标题',
  },
  {
    type: 'pcCoupon', label: '优惠券模块', group: '营销活动',
    note: '请前往 营销->优惠券->"首页显示"，用于在首页展示所推荐的优惠券',
    defaults: () => ({}),
    settings: [],
    summary: () => '展示"首页显示"的优惠券',
  },
  {
    type: 'pcGroupon', label: '拼团', group: '营销活动',
    defaults: () => ({
      style: 2, goodsStyle: 1, goodsRadioStyle: 1, textAlign: 1, textWeight: 1,
      goodsNameRow: 2, goodsNamePadding: 1, showName: 1, showBrief: 1, showPrice: 1,
      goodsPadding: 5, buyBtnStyle: 1, backgroundColor: '',
      boxRadius: 0, innerPadding: 0, boxPadding: 10, boxPaddingTop: 5, boxPaddingBottom: 5,
      frame: grouponFrameDefaults(), title: grouponTitleDefaults(),
      groupType: 1, grouponActivityItem: undefined as unknown as Record<string, any>, cardStyle: 1,
    }),
    settings: [
      { kind: 'radio', key: 'style', label: '模板', options: [{ label: '一行四个', value: 2 }, { label: '一行五个', value: 3 }, { label: '横向滑动', value: 6 }] },
      { kind: 'groupon', key: 'grouponActivityItem', label: '拼团活动' },
      { kind: 'radio', key: 'cardStyle', label: '卡片样式', options: [{ label: '样式一', value: 1 }, { label: '样式二', value: 2 }] },
      { kind: 'radio', key: 'goodsStyle', label: '商品卡样式', options: [{ label: '标准', value: 1 }, { label: '投影', value: 2 }, { label: '描边', value: 3 }, { label: '透明底', value: 4 }] },
      { kind: 'radio', key: 'goodsRadioStyle', label: '圆角', options: [{ label: '直角', value: 1 }, { label: '圆角', value: 2 }] },
      { kind: 'radio', key: 'textAlign', label: '文字对齐', options: [{ label: '左对齐', value: 1 }, { label: '居中对齐', value: 2 }] },
      { kind: 'radio', key: 'goodsNameRow', label: '名称行数', options: [{ label: '1 行', value: 1 }, { label: '2 行', value: 2 }] },
      { kind: 'slider', key: 'goodsPadding', label: '商品边距', max: 50 },
      { kind: 'color', key: 'backgroundColor', label: '背景颜色' },
      { kind: 'slider', key: 'boxRadius', label: '圆角', max: 30 },
      { kind: 'slider', key: 'innerPadding', label: '内边距', max: 30 },
      { kind: 'slider', key: 'boxPadding', label: '左右边距', max: 30 },
      { kind: 'slider', key: 'boxPaddingTop', label: '上边距', max: 30 },
      { kind: 'slider', key: 'boxPaddingBottom', label: '下边距', max: 30 },
    ],
    summary: (m) => (m.grouponActivityItem ? m.grouponActivityItem.name || `活动#${m.grouponActivityItem.grouponId}` : '未选择活动'),
  },
  {
    type: 'pcCategoryNav', label: '分类导航', group: '分类导航',
    defaults: bannerDefaults,
    settings: [
      { kind: 'radio', key: 'rowNum', label: '每屏显示数量', options: [2, 3, 4, 5, 6].map((n) => ({ label: `${n}个`, value: n })) },
      { kind: 'picList', key: 'picList', label: '导航图组' },
    ],
    summary: (m) => `每屏 ${m.rowNum || '-'} 个`,
  },
  {
    type: 'pcCategoryNav3', label: '分类导航3', group: '分类导航',
    defaults: () => ({
      ...bannerDefaults(),
      imageBackgroundColor: undefined as unknown as string,
      titleText: undefined as unknown as string,
      showMore: undefined as unknown as number,
      showMoreText: undefined as unknown as string,
      moreLink: undefined as unknown as Record<string, any>,
    }),
    settings: [
      { kind: 'picList', key: 'picList', label: '添加一组' },
      { kind: 'color', key: 'imageBackgroundColor', label: '图片背景色' },
      { kind: 'input', key: 'titleText', label: '标题内容' },
      { kind: 'radio', key: 'showMore', label: '查看更多', options: [{ label: '显示', value: 1 }, { label: '不显示', value: 0 }] },
      { kind: 'input', key: 'showMoreText', label: '更多文字', showIf: (m) => Number(m.showMore) === 1 },
      { kind: 'link', key: 'moreLink', label: '更多链接', showIf: (m) => Number(m.showMore) === 1 },
    ],
    summary: (m) => m.titleText || `${((m.picList || []) as PicItem[]).length} 项`,
  },
  {
    type: 'pcCatWide', label: '分类模块-宽', group: '分类导航',
    defaults: () => ({ catIds: [] as number[] }),
    settings: [
      { kind: 'categoryMulti', key: 'catIds', label: '已选分类' },
    ],
    summary: (m) => `${((m.catIds || []) as number[]).length} 个分类`,
  },
];

const MODULE_DEF_MAP = new Map(PC_MODULE_DEFS.map((d) => [d.type, d]));

/** 页面级固定模块（原版 ensureFixed：PC 首页固定模块集合） */
const FIXED_MODULE_TYPES: Array<{ type: string; label: string; defaults: () => Record<string, any> }> = [
  { type: 'pcHeaderModule', label: '页头(导航+搜索)', defaults: () => ({}) },
  { type: 'pcFooterModule', label: '页脚', defaults: () => ({}) },
];

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
  const [activeIndex, setActiveIndex] = useState<number>(-1); // -2=页面设置 -1=无 0..n=模块
  const [panelTab, setPanelTab] = useState<'module' | 'page' | 'pcHeader'>('module');
  const dirtyRef = useRef(false);
  const decorateIdRef = useRef(decorateId);

  const markDirty = () => {
    dirtyRef.current = true;
  };

  /* ---- 加载 ---- */
  useEffect(() => {
    let alive = true;
    setLoading(true);
    apiGet<{
      decorateId?: number;
      decorateTitle?: string;
      isHome?: number;
      data?: { pageModule?: Record<string, any>; moduleList?: EditorModule[] } | null;
      draftData?: { pageModule?: Record<string, any>; moduleList?: EditorModule[] } | null;
    }>('/decorate/decorate/detail', { id: decorateId, decorateType: 2, localeId: 0, parentId: 0 })
      .then((res) => {
        if (!alive) return;
        if (res?.decorateId) decorateIdRef.current = res.decorateId;
        setTitle(res?.decorateTitle || '');
        const source = res?.data || {};
        setPageModule({
          type: 'page', module: {}, backgroundRepeat: '', backgroundSize: '',
          style: 0, headerStyle: 1,
          ...source.pageModule,
        });
        const list = Array.isArray(source.moduleList) ? source.moduleList : [];
        // 兼容旧数据：补齐信封字段
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

  /* ---- 离开提醒 ---- */
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

  const getSaveData = () => ({
    pageModule: { ...pageModule },
    moduleList: moduleList.map((m) => ({ ...m })),
  });

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

  /* ---- 模块操作 ---- */
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

  const removeModule = (index: number) => {
    const item = moduleList[index];
    if (item?.fixed) {
      Toast.warning('固定模块不可删除');
      return;
    }
    setModuleList((prev) => prev.filter((_, i) => i !== index));
    if (activeIndex === index) setActiveIndex(-1);
    markDirty();
  };

  const copyModule = (index: number) => {
    const item = moduleList[index];
    if (item?.fixed) {
      Toast.warning('固定模块不可复制');
      return;
    }
    const clone: EditorModule = {
      ...JSON.parse(JSON.stringify(item)),
      moduleIndex: Date.now(),
    };
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
      {/* 顶部工具栏 */}
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
        {/* 左侧模块库 */}
        <div style={{ width: 220, background: '#fff', borderRight: '1px solid var(--semi-color-border)', overflowY: 'auto', padding: 12, flexShrink: 0 }}>
          <Text strong style={{ display: 'block', marginBottom: 10 }}>模块库</Text>
          {['轮播广告', '商品楼层', '营销活动', '分类导航'].map((group) => (
            <div key={group} style={{ marginBottom: 14 }}>
              <Text type="tertiary" size="small" style={{ display: 'block', margin: '6px 0' }}>{group}</Text>
              {PC_MODULE_DEFS.filter((d) => d.group === group).map((def) => (
                <Tooltip key={def.type} content={def.note || `添加「${def.label}」`} position="right">
                  <div
                    className="pc-decorate-lib-item"
                    onClick={() => addModule(def)}
                  >
                    <span>{def.label}</span>
                    {def.overseasOnly && <Tag size="small" color="cyan">跨境</Tag>}
                  </div>
                </Tooltip>
              ))}
            </div>
          ))}
        </div>

        {/* 中间画布 */}
        <div style={{ flex: 1, overflowY: 'auto', padding: 20 }}>
          <div style={{ maxWidth: 1200, margin: '0 auto' }}>
            {/* 页面设置入口 */}
            <div
              className="pc-decorate-canvas-bar"
              onClick={() => { setPanelTab('page'); setActiveIndex(-1); }}
            >
              页面设置（背景颜色 / 背景图片）
            </div>
            {/* PC头部装修入口 */}
            <div
              className="pc-decorate-canvas-bar"
              onClick={() => { setPanelTab('pcHeader'); setActiveIndex(-1); }}
            >
              PC头部装修（LOGO / 搜索 / 主导航 / 顶部菜单 / 悬浮条）
            </div>

            {/* 固定模块提示 */}
            {FIXED_MODULE_TYPES.map((f) => (
              <div key={f.type} className="pc-decorate-module-card" style={{ opacity: 0.65, cursor: 'default' }}>
                <div className="pc-decorate-module-card__title">
                  <Text strong>{f.label}</Text>
                  <Tag size="small" color="grey">固定模块</Tag>
                </div>
                <div className="pc-decorate-module-card__preview">由头部装修与系统配置控制</div>
              </div>
            ))}

            {/* 动态模块列表 */}
            {moduleList.length === 0 && (
              <div style={{ padding: 60, textAlign: 'center', color: 'var(--semi-color-text-2)' }}>
                暂无模块，请从左侧模块库添加
              </div>
            )}
            {moduleList.map((item, index) => {
              const def = MODULE_DEF_MAP.get(item.type);
              const isActive = activeIndex === index;
              return (
                <div
                  key={item.moduleIndex}
                  className={`pc-decorate-module-card${isActive ? ' pc-decorate-module-card--active' : ''}${item.isShow ? '' : ' pc-decorate-module-card--hidden'}`}
                  draggable
                  onDragStart={(e) => e.dataTransfer.setData('text/pc-decorate-index', String(index))}
                  onDragOver={(e) => e.preventDefault()}
                  onDrop={(e) => {
                    e.preventDefault();
                    const from = Number(e.dataTransfer.getData('text/pc-decorate-index'));
                    if (!Number.isNaN(from)) dropModule(from, index);
                  }}
                  onClick={() => { setActiveIndex(index); setPanelTab('module'); }}
                >
                  <div className="pc-decorate-module-card__title">
                    <Text strong>{item.label}</Text>
                    <Text type="tertiary" size="small">{def?.summary ? def.summary(item.module) : item.type}</Text>
                    <div style={{ flex: 1 }} />
                    <div style={{ display: 'flex', gap: 2, alignItems: 'center' }} onClick={(e) => e.stopPropagation()}>
                      <Button
                        size="small" theme="borderless" icon={<IconChevronUp />}
                        disabled={index === 0}
                        onClick={() => moveModule(index, -1)}
                      />
                      <Button
                        size="small" theme="borderless" icon={<IconChevronDown />}
                        disabled={index === moduleList.length - 1}
                        onClick={() => moveModule(index, 1)}
                      />
                      <Button size="small" theme="borderless" icon={<IconCopy />} onClick={() => copyModule(index)} />
                      <Button
                        size="small" theme="borderless"
                        type={item.isShow ? 'primary' : 'tertiary'}
                        onClick={() => toggleShow(index)}
                      >
                        {item.isShow ? '显示' : '隐藏'}
                      </Button>
                      <Button size="small" theme="borderless" type="danger" icon={<IconDelete />} onClick={() => removeModule(index)} />
                    </div>
                  </div>
                  <div className="pc-decorate-module-card__preview">
                    {def?.note ? (
                      <span style={{ color: 'var(--semi-color-warning)' }}>{def.note}</span>
                    ) : (
                      <>
                        {def?.label || item.type}
                        {def?.summary ? ` —— ${def.summary(item.module)}` : ''}
                      </>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* 右侧设置面板 */}
        <div style={{ width: 360, background: '#fff', borderLeft: '1px solid var(--semi-color-border)', overflowY: 'auto', padding: 16, flexShrink: 0 }}>
          {panelTab === 'page' && (
            <PageSettingsPanel
              pageModule={pageModule}
              onChange={(next) => { setPageModule(next); markDirty(); }}
            />
          )}
          {panelTab === 'pcHeader' && (
            <PcHeaderPanel
              pageModule={pageModule}
              onChange={(next) => { setPageModule(next); markDirty(); }}
            />
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

/* ===================== 页面设置 ===================== */

function PageSettingsPanel({
  pageModule,
  onChange,
}: {
  pageModule: Record<string, any>;
  onChange: (next: Record<string, any>) => void;
}) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
      <Text strong>页面设置</Text>
      <div>
        <Text type="tertiary" size="small">背景颜色</Text>
        <input
          type="color"
          value={pageModule.backgroundColor || '#ffffff'}
          onChange={(e) => onChange({ ...pageModule, backgroundColor: e.target.value })}
          style={{ width: '100%', height: 32, border: '1px solid var(--semi-color-border)', borderRadius: 4, cursor: 'pointer' }}
        />
      </div>
      <div>
        <Text type="tertiary" size="small">背景图片 URL（留空不设置）</Text>
        <Input
          value={typeof pageModule.backgroundImage === 'string' ? pageModule.backgroundImage : pageModule.backgroundImage?.picUrl || ''}
          onChange={(v) => onChange({ ...pageModule, backgroundImage: v ? { picUrl: v, picThumb: v } : { picUrl: '', picThumb: '' } })}
          placeholder="https://..."
        />
      </div>
      <Text type="tertiary" size="small">
        背景作用于整个首页画布；模块各自的背景在其设置面板内配置。
      </Text>
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
      <Row2 label="头部风格">
        <RadioGroup value={header.styleType} onChange={(e: any) => set('styleType', Number(e.target.value))}>
          <Radio value={1}>简约风</Radio>
          <Radio value={2}>跨境风</Radio>
        </RadioGroup>
      </Row2>
      <Row2 label="LOGO 图片 URL">
        <Input value={header.logoPic?.picUrl || ''} onChange={(v) => set('logoPic', { ...header.logoPic, picUrl: v, picThumb: v })} placeholder="https://..." />
      </Row2>
      <Row2 label="LOGO 宽/高">
        <Space>
          <InputNumber value={header.logoWidth} onChange={(v) => set('logoWidth', v ?? 100)} style={{ width: 90 }} />
          <InputNumber value={header.logoHeight} onChange={(v) => set('logoHeight', v ?? 25)} style={{ width: 90 }} />
        </Space>
      </Row2>
      <Row2 label="搜索框样式">
        <RadioGroup value={header.searchStyle} onChange={(e: any) => set('searchStyle', Number(e.target.value))}>
          <Radio value={1}>样式一</Radio>
          <Radio value={2}>样式二</Radio>
          <Radio value={3}>样式三</Radio>
        </RadioGroup>
      </Row2>
      <Row2 label="显示商品分类">
        <Switch checked={Number(header.showCategory) === 1} onChange={(v) => set('showCategory', v ? 1 : 0)} />
      </Row2>
      <Row2 label="显示首页分类">
        <Switch checked={Number(header.showHomeCategory) === 1} onChange={(v) => set('showHomeCategory', v ? 1 : 0)} />
      </Row2>
      <Row2 label="分类距顶部距离">
        <InputNumber value={header.categoryMarginTop} onChange={(v) => set('categoryMarginTop', v ?? 0)} style={{ width: 120 }} />
      </Row2>
      <Row2 label="显示主导航">
        <Switch checked={Number(header.showMainNav) === 1} onChange={(v) => set('showMainNav', v ? 1 : 0)} />
      </Row2>
      <Row2 label="主导航上/下内边距">
        <Space>
          <InputNumber value={header.mainNavPaddingTop} onChange={(v) => set('mainNavPaddingTop', v ?? 0)} style={{ width: 90 }} />
          <InputNumber value={header.mainNavPaddingBottom} onChange={(v) => set('mainNavPaddingBottom', v ?? 0)} style={{ width: 90 }} />
        </Space>
      </Row2>
      <Row2 label="顶部菜单栏">
        <Switch checked={Number(header.showTopMenuBar) === 1} onChange={(v) => set('showTopMenuBar', v ? 1 : 0)} />
      </Row2>
      <Row2 label="地区文字" showIf={Number(header.showTopMenuBar) === 1}>
        <Input value={header.topRegionText || ''} onChange={(v) => set('topRegionText', v)} />
      </Row2>
      <Row2 label="购物车图标 URL">
        <Input value={header.cartPic?.picUrl || ''} onChange={(v) => set('cartPic', { ...header.cartPic, picUrl: v, picThumb: v })} placeholder="https://..." />
      </Row2>
      <Row2 label="悬浮优惠条">
        <Switch checked={Number(header.showBenefitBar) === 1} onChange={(v) => set('showBenefitBar', v ? 1 : 0)} />
      </Row2>
      <Row2 label="优惠条背景色" showIf={Number(header.showBenefitBar) === 1}>
        <input
          type="color"
          value={header.benefitBarBackgroundColor || '#ffffff'}
          onChange={(e) => set('benefitBarBackgroundColor', e.target.value)}
          style={{ width: '100%', height: 30, border: '1px solid var(--semi-color-border)', borderRadius: 4 }}
        />
      </Row2>
      <Text type="tertiary" size="small">
        其余细节项（顶部消息/订单/站点导航/会员中心菜单等）按契约原样透传，可由后端或 C 端扩展。
      </Text>
    </div>
  );
}

function Row2({ label, children, showIf = true }: { label: string; children: ReactNode; showIf?: boolean }) {
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
        const value = moduleData[field.key];
        const set = (v: unknown) => onChange({ ...moduleData, [field.key]: v });
        return (
          <div key={field.key}>
            <Text type="tertiary" size="small" style={{ display: 'block', marginBottom: 4 }}>
              {field.label}
              {field.hint ? `（${field.hint}）` : ''}
            </Text>
            <FieldControl field={field} value={value} onChange={set} />
          </div>
        );
      })}
    </div>
  );
}

/* ---- 分类选择（product/category/getAllCategory 扁平化） ---- */

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

/* ---- 商品选择弹窗 ---- */

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
  const [pickedRows, setPickedRows] = useState<ProductRow[]>([]);

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
    setPickedRows((prev) => {
      const exists = prev.find((r) => r.productId === row.productId);
      if (exists) return prev.filter((r) => r.productId !== row.productId);
      if (!multiple) return [row];
      return [...prev, row];
    });
  };

  return (
    <Modal
      title="选择商品"
      visible={visible}
      width={720}
      onOk={() => onOk(picked, pickedRows)}
      onCancel={onCancel}
      okText="确定"
      cancelText="取消"
    >
      <Space style={{ marginBottom: 12, width: '100%' }}>
        <Input
          value={keyword}
          onChange={setKeyword}
          onEnterPress={load}
          placeholder="输入商品名称搜索"
          style={{ width: 260 }}
          showClear
          onClear={() => setKeyword('')}
        />
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
            {(row.picThumb || row.picUrl) && (
              <img src={row.picThumb || row.picUrl} alt="" style={{ width: 40, height: 40, objectFit: 'cover', borderRadius: 4 }} />
            )}
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

/* ---- 拼团活动选择 ---- */

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
    <Modal
      title="选择拼团活动"
      visible={visible}
      width={640}
      onOk={() => onOk(picked)}
      onCancel={onCancel}
      okText="确定"
      cancelText="取消"
    >
      <div style={{ maxHeight: 380, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 8 }}>
        {loading && <Spin />}
        {!loading && rows.length === 0 && <Text type="tertiary">暂无拼团活动</Text>}
        {rows.map((row) => {
          const id = row.grouponId ?? row.id;
          const name = row.name || row.title || `活动#${id}`;
          const groupType = row.groupType;
          const isPicked = picked && (picked.grouponId ?? picked.id) === id;
          return (
            <div
              key={id}
              onClick={() => setPicked({ grouponId: id, name, groupType, ...row })}
              style={{
                padding: 10, borderRadius: 6, cursor: 'pointer',
                border: `1px solid ${isPicked ? 'var(--semi-color-primary)' : 'var(--semi-color-border)'}`,
              }}
            >
              <div>{name}</div>
              <Text type="tertiary" size="small">{groupType === 2 ? '阶梯拼团' : '普通拼团'}</Text>
            </div>
          );
        })}
      </div>
    </Modal>
  );
}

/* ---- 字段控件 ---- */

function PicListEditor({ value, onChange }: { value: PicItem[]; onChange: (next: PicItem[]) => void }) {
  const list = Array.isArray(value) ? value : [];
  const update = (next: PicItem[]) => onChange(next);
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      {list.map((item, index) => (
        <div key={index} style={{ border: '1px solid var(--semi-color-border)', borderRadius: 6, padding: 8, display: 'flex', flexDirection: 'column', gap: 6 }}>
          <div style={{ display: 'flex', gap: 8, alignItems: 'flex-start' }}>
            {item.picUrl ? (
              <img src={item.picUrl} alt="" style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 4, flexShrink: 0 }} />
            ) : (
              <div style={{ width: 48, height: 48, borderRadius: 4, background: 'var(--semi-color-fill-0)', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--semi-color-text-2)', fontSize: 16 }}>?</div>
            )}
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 6 }}>
              <Input
                value={item.picUrl}
                onChange={(v) => update(list.map((it, i) => (i === index ? { ...it, picUrl: v, picThumb: v } : it)))}
                placeholder="图片 URL"
                size="small"
              />
              <Input
                value={item.picTitle || ''}
                onChange={(v) => update(list.map((it, i) => (i === index ? { ...it, picTitle: v } : it)))}
                placeholder="图片标题(可选)"
                size="small"
              />
              <Input
                value={item.picLink?.link || ''}
                onChange={(v) => update(list.map((it, i) => (i === index ? { ...it, picLink: { ...it.picLink, link: v, title: it.picLink?.title || '' } } : it)))}
                placeholder="跳转链接(可选)"
                size="small"
              />
            </div>
            <Space spacing={0}>
              <Button size="small" theme="borderless" icon={<IconChevronUp />} disabled={index === 0} onClick={() => {
                const next = [...list];
                [next[index - 1], next[index]] = [next[index], next[index - 1]];
                update(next);
              }} />
              <Button size="small" theme="borderless" icon={<IconChevronDown />} disabled={index === list.length - 1} onClick={() => {
                const next = [...list];
                [next[index + 1], next[index]] = [next[index], next[index + 1]];
                update(next);
              }} />
              <Button size="small" theme="borderless" type="danger" icon={<IconClose />} onClick={() => update(list.filter((_, i) => i !== index))} />
            </Space>
          </div>
        </div>
      ))}
      <Button size="small" icon={<IconPlus />} onClick={() => update([...list, emptyPic()])}>
        添加图片
      </Button>
    </div>
  );
}

function FieldControl({
  field,
  value,
  onChange,
}: {
  field: SettingFieldDef;
  value: unknown;
  onChange: (v: unknown) => void;
}) {
  const categoryOptions = useCategoryOptions();
  const [pickerVisible, setPickerVisible] = useState(false);
  const [grouponVisible, setGrouponVisible] = useState(false);

  switch (field.kind) {
    case 'radio':
      return (
        <RadioGroup value={(value ?? field.options?.[0]?.value) as string | number | undefined} onChange={(e: any) => onChange(Number(e.target.value))}>
          {(field.options || []).map((o) => (
            <Radio key={String(o.value)} value={o.value}>{o.label}</Radio>
          ))}
        </RadioGroup>
      );
    case 'input':
      return <Input value={String(value ?? '')} onChange={(v) => onChange(v)} />;
    case 'number':
      return (
        <InputNumber
          value={typeof value === 'number' ? value : undefined}
          onChange={(v) => onChange(v ?? undefined)}
          min={field.min}
          max={field.max}
          style={{ width: 160 }}
        />
      );
    case 'slider':
      return (
        <Slider
          value={typeof value === 'number' ? value : 0}
          min={0}
          max={field.max ?? 30}
          step={1}
          onChange={(v) => onChange(typeof v === 'number' ? v : (v as number[])[0])}
        />
      );
    case 'color':
      return (
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <input
            type="color"
            value={String(value || '#ffffff')}
            onChange={(e) => onChange(e.target.value)}
            style={{ width: 46, height: 30, border: '1px solid var(--semi-color-border)', borderRadius: 4, cursor: 'pointer' }}
          />
          <Button size="small" onClick={() => onChange('')}>重置</Button>
          <Text type="tertiary" size="small">{String(value || '未设置')}</Text>
        </div>
      );
    case 'picList':
      return <PicListEditor value={(value as PicItem[]) || []} onChange={(v) => onChange(v)} />;
    case 'pic':
      return <PicListEditor value={value ? [value as PicItem] : []} onChange={(v) => onChange(v[0] || emptyPic())} />;
    case 'link': {
      const link = (value && typeof value === 'object' ? value : { link: '', title: '' }) as { link?: string; title?: string };
      return (
        <div style={{ display: 'flex', gap: 6 }}>
          <Input value={link.link || ''} onChange={(v) => onChange({ ...link, link: v, title: link.title || '' })} placeholder="链接地址" />
          <Input value={link.title || ''} onChange={(v) => onChange({ ...link, link: link.link || '', title: v })} placeholder="名称" style={{ width: 110 }} />
        </div>
      );
    }
    case 'linkList': {
      const list = (Array.isArray(value) ? value : []) as Array<{ link?: { link?: string; title?: string }; linkTitle?: string }>;
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          {list.map((item, index) => (
            <div key={index} style={{ display: 'flex', gap: 6 }}>
              <Input
                value={item.link?.link || ''}
                onChange={(v) => onChange(list.map((it, i) => (i === index ? { ...it, link: { ...it.link, link: v, title: it.link?.title || '' } } : it)))}
                placeholder="链接地址"
              />
              <Input
                value={item.linkTitle || ''}
                onChange={(v) => onChange(list.map((it, i) => (i === index ? { ...it, linkTitle: v, link: { ...it.link, link: it.link?.link || '' } } : it)))}
                placeholder="名称"
                style={{ width: 110 }}
              />
              <Button size="small" theme="borderless" type="danger" icon={<IconClose />} onClick={() => onChange(list.filter((_, i) => i !== index))} />
            </div>
          ))}
          <Button size="small" icon={<IconPlus />} onClick={() => onChange([...list, { link: { link: '', title: '' }, linkTitle: '' }])}>
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
          onChange={(v) => onChange(v ? Number(v) : 0)}
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
          onChange={(v) => onChange(((v as unknown as number[]) || []).map(Number))}
        />
      );
    }
    case 'productIds': {
      const ids = (Array.isArray(value) ? value : []) as number[];
      return (
        <div>
          <Button icon={<IconPlus />} onClick={() => setPickerVisible(true)}>
            选择商品（已选 {ids.length}）
          </Button>
          {ids.length > 0 && (
            <div style={{ marginTop: 6, display: 'flex', flexWrap: 'wrap', gap: 4 }}>
              {ids.map((id) => <Tag key={id} size="small">#{id}</Tag>)}
            </div>
          )}
          {pickerVisible && (
            <ProductPickerModal
              visible
              selectedIds={ids}
              onOk={(nextIds) => { onChange(nextIds); setPickerVisible(false); }}
              onCancel={() => setPickerVisible(false)}
            />
          )}
        </div>
      );
    }
    case 'products': {
      const products = (value && typeof value === 'object' ? value : productsDefaults()) as Record<string, any>;
      const setProducts = (next: Record<string, any>) => onChange(next);
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <div>
            <Text type="tertiary" size="small" style={{ display: 'block', marginBottom: 4 }}>选品方式</Text>
            <RadioGroup
              value={products.productSelectType ?? 1}
              onChange={(e: any) => setProducts({ ...products, productSelectType: Number(e.target.value) })}
            >
              <Radio value={1}>选商品</Radio>
              <Radio value={2}>按分类</Radio>
              <Radio value={3}>按标签</Radio>
            </RadioGroup>
          </div>
          {Number(products.productSelectType) === 1 && (
            <div>
              <Button icon={<IconPlus />} onClick={() => setPickerVisible(true)}>
                选择商品（已选 {((products.productIds || []) as number[]).length}）
              </Button>
              {pickerVisible && (
                <ProductPickerModal
                  visible
                  selectedIds={(products.productIds || []) as number[]}
                  onOk={(nextIds) => { setProducts({ ...products, productIds: nextIds }); setPickerVisible(false); }}
                  onCancel={() => setPickerVisible(false)}
                />
              )}
            </div>
          )}
          {Number(products.productSelectType) === 2 && (
            <Select
              value={products.productCategoryId || undefined}
              optionList={categoryOptions}
              showClear
              filter
              placeholder="选择分类"
              onChange={(v) => setProducts({ ...products, productCategoryId: v ? Number(v) : 0 })}
            />
          )}
          {Number(products.productSelectType) === 3 && (
            <Input
              value={products.productTag || ''}
              onChange={(v) => setProducts({ ...products, productTag: v })}
              placeholder="商品标签"
            />
          )}
          {(Number(products.productSelectType) === 2 || Number(products.productSelectType) === 3) && (
            <div>
              <Text type="tertiary" size="small" style={{ display: 'block', marginBottom: 4 }}>商品数量</Text>
              <InputNumber
                value={products.productNumber ?? 8}
                onChange={(v) => setProducts({ ...products, productNumber: v ?? 8 })}
                min={field.numberMin ?? 1}
                step={field.numberStep ?? 1}
                style={{ width: 140 }}
              />
            </div>
          )}
        </div>
      );
    }
    case 'groupon': {
      const item = value as Record<string, any> | undefined | null;
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          {item ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Tag color="blue">{item.name || `活动#${item.grouponId}`}</Tag>
              <Button size="small" theme="borderless" type="danger" onClick={() => onChange(null)}>移除</Button>
            </div>
          ) : (
            <Text type="tertiary" size="small">未选择活动（发布前必须选择）</Text>
          )}
          <Button icon={<IconPlus />} onClick={() => setGrouponVisible(true)}>
            选择拼团活动
          </Button>
          {grouponVisible && (
            <GrouponPickerModal
              visible
              current={item || null}
              onOk={(next) => {
                onChange(next);
                setGrouponVisible(false);
              }}
              onCancel={() => setGrouponVisible(false)}
            />
          )}
        </div>
      );
    }
    default:
      return null;
  }
}
