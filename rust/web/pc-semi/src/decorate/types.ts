/** 与原版 Java Admin/PC 共用的装修 JSON 契约。 */

export interface DecoratePic {
  picId?: string | number;
  picUrl?: string;
  picThumb?: string;
  picName?: string;
  picTitle?: string;
  picDesc?: string;
  picLink?: { link?: string; name?: string; label?: string };
}

export interface DecorateTitle {
  showTitle?: number;
  title?: string;
  subTitle?: string;
  titleColor?: string;
  textAlign?: number;
  titleSize?: number;
}

export interface DecorateFrame {
  boxPadding?: number;
  boxPaddingTop?: number;
  boxPaddingBottom?: number;
  boxRadius?: number;
  backgroundColor?: string;
}

export type ModuleFrameLike = DecorateFrame;

export interface PcHeaderConfig {
  logoPic?: DecoratePic;
  logoWidth?: number;
  showMainNav?: number;
  mainNav?: Array<{ title?: string; link?: string; name?: string }>;
  navColor?: string;
  backgroundColor?: string;
  [key: string]: unknown;
}

export interface PageModule {
  type?: string;
  style?: number;
  backgroundColor?: string;
  backgroundImage?: DecoratePic;
  backgroundRepeat?: number;
  backgroundSize?: number;
  homeStyleVersion?: 'new' | 'old';
  pcHeader?: PcHeaderConfig;
  [key: string]: unknown;
}

export interface DecorateModule<T = Record<string, any>> {
  type: string;
  url?: string;
  label?: string;
  moduleIndex?: number | string;
  isShow?: boolean;
  fixed?: boolean;
  module: T;
}

export interface DecorateData {
  decorateId?: number;
  moduleList: DecorateModule[];
  pageModule?: PageModule | null;
  isHome?: number;
}

export interface HomeDecorateResponse extends DecorateData {}
