import type { ReactNode } from 'react';

export interface OptionItem {
  label: string;
  value: string | number;
}

export type FieldType =
  | 'input'
  | 'textarea'
  | 'number'
  | 'select'
  | 'radio'
  | 'switch'
  | 'date'
  | 'custom';

export interface FieldConfig {
  field: string;
  label: string;
  type?: FieldType;
  options?: OptionItem[];
  required?: boolean;
  placeholder?: string;
  extraText?: string;
  width?: number | string;
  span?: number;
  rules?: Array<Record<string, unknown>>;
  disabled?: boolean;
  initValue?: unknown;
  render?: (ctx: { formApi: any; values: Record<string, any> }) => ReactNode;
}

export interface FormSectionConfig {
  key: string;
  title: string;
  fields: FieldConfig[];
  description?: string;
}

export interface FormDrawerConfig {
  title?: string;
  addText?: string;
  width?: number;
  labelWidth?: number;
  labelPosition?: 'left' | 'top';
  sections: FormSectionConfig[];
  createApi: string;
  updateApi?: string;
  detailApi?: string;
  deleteApi?: string;
  idField?: string;
  toPayload?: (values: Record<string, any>, mode: 'create' | 'edit') => Record<string, any>;
}

export interface FilterConfig {
  field: string;
  label: string;
  type?: 'input' | 'select' | 'number' | 'date';
  options?: OptionItem[];
  placeholder?: string;
  width?: number | string;
}

export interface ColumnConfig<T = any> {
  title: ReactNode;
  dataIndex?: string;
  key?: string;
  width?: number;
  render?: (value: any, record: T, index: number) => ReactNode;
}

export interface ListPageProps<T = any> {
  api: string;
  rowKey: string;
  columns: ColumnConfig<T>[];
  filters?: FilterConfig[];
  query?: Record<string, unknown>;
  pageSize?: number;
  drawer?: FormDrawerConfig;
  toolbarExtra?: ReactNode;
  cardTitle?: ReactNode;
  onRowClick?: (record: T) => void;
  rowActions?: (record: T) => ReactNode;
}
