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
  /** `el-cascader` 的 props 透传（如需 `{ emitPath: false, checkStrictly: true }`）。 */
  cascaderProps?: Record<string, any>;
  min?: number;
  max?: number;
  step?: number;
  disabled?: boolean;
  extra?: string; // 帮助说明文字
  colSpan?: number; // 栅格占比 (默认 24)
  hidden?: (form: any) => boolean;
  /** 异步下拉/级联选项加载器（如分类树、品牌列表），表单挂载时调用。 */
  optionsLoader?: () => Promise<FormOption[]>;
}

export interface FormSection {
  title?: string;
  description?: string;
  fields: FormFieldSchema[];
}

export interface FormSchema {
  title?: string;
  /** 主键字段名（弹窗新增/编辑回填用），默认 `id`。 */
  primaryKey?: string;
  apiGet?: string;      // 获取配置/数据的接口，如 /setting/config/basicSettings
  apiSave?: string;     // 保存配置/数据的接口，如 /setting/config/saveBasic
  /** 新增接口（弹窗「新增」提交时优先于 apiSave）。 */
  apiCreate?: string;
  /** 编辑接口（弹窗「编辑」提交时优先于 apiSave）。 */
  apiUpdate?: string;
  /** 编辑时拉取完整详情的接口（GET，参数 `?<primaryKey>=id`）。 */
  apiDetail?: string;
  submitText?: string;
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
  apiDelete?: string;     // 删除接口（单条 OperateDTO { id }）
  /** 批量删除接口（BatchDTO { ids, type: 'del' }）；不设则退回 apiDelete。 */
  apiBatchDelete?: string;
  primaryKey?: string;    // 主键，默认 id
  searchFields?: TableSearchField[];
  columns: TableColumnSchema[];
  actions?: TableAction[];
  /** 自定义「新增」按钮回调（不配 formSchema 时用于打开专用编辑抽屉）。 */
  onCreate?: () => void;
  createLabel?: string;
  batchActions?: TableBatchAction[];
  /** 工具栏自定义按钮（如订单导出）。 */
  extraButtons?: Array<{
    label: string;
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'default';
    onClick: () => void;
  }>;
  formSchema?: FormSchema;// 新增/编辑弹窗用的 Schema
  exportable?: boolean;   // 是否支持导出
}
