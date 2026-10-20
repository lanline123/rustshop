export type FormFieldType =
  | 'input'
  | 'password'
  | 'textarea'
  | 'number'
  | 'radio'
  | 'checkbox'
  | 'select'
  | 'switch'
  | 'date'
  | 'datetime'
  | 'image'
  | 'image-list'
  | 'color'
  | 'slider'
  | 'cascader';

export interface FormOption {
  label: string;
  value: any;
  disabled?: boolean;
  children?: FormOption[];
}

export interface FormFieldSchema {
  prop: string;
  label: string;
  type?: FormFieldType;
  placeholder?: string;
  defaultValue?: any;
  rules?: any[];
  options?: FormOption[];
  min?: number;
  max?: number;
  step?: number;
  disabled?: boolean;
  extra?: string; // 帮助说明文字
  colSpan?: number; // 栅格占比 (默认 24)
  hidden?: (form: any) => boolean;
}

export interface FormSection {
  title?: string;
  description?: string;
  fields: FormFieldSchema[];
}

export interface FormSchema {
  title?: string;
  apiGet?: string;      // 获取配置/数据的接口，如 /setting/config/basicSettings
  apiSave?: string;     // 保存配置/数据的接口，如 /setting/config/saveBasic
  sections: FormSection[];
  labelWidth?: string;
}

export interface TableColumnSchema {
  prop: string;
  label: string;
  width?: number | string;
  minWidth?: number | string;
  type?: 'text' | 'tag' | 'image' | 'money' | 'datetime' | 'switch' | 'custom';
  sortable?: boolean;
  align?: 'left' | 'center' | 'right';
  tagMap?: Record<string | number, { text: string; type: 'success' | 'warning' | 'danger' | 'info' | 'primary' }>;
  switchApi?: string; // 行内开关快速切换接口
}

export interface TableSearchField {
  prop: string;
  label: string;
  type?: 'input' | 'select' | 'daterange';
  placeholder?: string;
  options?: FormOption[];
}

export interface TableAction {
  label: string;
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
  icon?: string;
  onClick: (row: any) => void;
  show?: (row: any) => boolean;
}

export interface TableBatchAction {
  label: string;
  type?: 'primary' | 'danger' | 'warning' | 'default';
  onClick: (selectedRows: any[], refresh: () => void) => void;
}

export interface TableSchema {
  title: string;
  apiList: string;        // 分页获取接口
  apiDelete?: string;     // 删除接口
  primaryKey?: string;    // 主键，默认 id
  searchFields?: TableSearchField[];
  columns: TableColumnSchema[];
  actions?: TableAction[];
  batchActions?: TableBatchAction[];
  formSchema?: FormSchema;// 新增/编辑弹窗用的 Schema
  exportable?: boolean;   // 是否支持导出
}
