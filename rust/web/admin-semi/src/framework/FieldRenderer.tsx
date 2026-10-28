import { useEffect, useState } from 'react';
import { Form } from '@douyinfe/semi-ui';
import { apiGet } from '../api';
import type { FieldConfig, OptionItem } from './types';

interface FieldRendererProps {
  config: FieldConfig;
  formApi: any;
  values: Record<string, any>;
  mode: 'create' | 'edit';
  inputWidth?: number;
}

function RemoteSelect({
  config,
  common,
}: {
  config: FieldConfig;
  common: Record<string, any>;
}) {
  const [options, setOptions] = useState<OptionItem[]>(config.options || []);

  useEffect(() => {
    if (!config.optionsApi) return;
    let alive = true;
    apiGet<any>(config.optionsApi, { page: 1, size: 200, ...config.optionsParams })
      .then((result) => {
        if (!alive) return;
        const list = Array.isArray(result) ? result : result?.records || result?.list || [];
        const labelKey = config.optionLabel || 'label';
        const valueKey = config.optionValue || 'value';
        setOptions(
          list.map((item: Record<string, any>) => ({
            label: String(item[labelKey] ?? item.name ?? item.label ?? ''),
            value: item[valueKey] ?? item.id ?? item.value,
          })),
        );
      })
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, [config.optionsApi, config.optionLabel, config.optionValue, config.optionsParams]);

  return <Form.Select {...(common as any)} filter optionList={options} />;
}

export default function FieldRenderer({
  config,
  formApi,
  values,
  mode,
  inputWidth,
}: FieldRendererProps) {
  const width = config.width ?? config.inputWidth ?? inputWidth ?? 360;
  const required = config.required || config.requiredWhen?.(mode);
  const rules =
    config.rules ??
    (required ? [{ required: true, message: `请填写${config.label}` }] : undefined);
  const common = {
    field: config.field,
    label: config.label,
    placeholder: config.placeholder,
    disabled: config.disabled,
    rules,
    extraText: config.extraText,
    style: { width },
  };

  if (config.type === 'custom' && config.render) {
    return (
      <div className="framework-custom-field">
        <label className="framework-custom-label">{config.label}</label>
        <div className="framework-custom-content">{config.render({ formApi, values, mode })}</div>
      </div>
    );
  }

  switch (config.type) {
    case 'textarea':
      return <Form.TextArea {...common} autosize={{ minRows: 3, maxRows: 8 }} />;
    case 'number':
      return <Form.InputNumber {...common} min={0} style={{ width }} />;
    case 'select':
      if (config.optionsApi) return <RemoteSelect config={config} common={common} />;
      return <Form.Select {...common} filter showClear optionList={config.options || []} />;
    case 'radio':
      return <Form.RadioGroup {...common} type="button" options={config.options || []} />;
    case 'switch':
      return (
        <Form.Switch
          field={config.field}
          label={config.label}
          disabled={config.disabled}
          checkedText={config.checkedText}
          uncheckedText={config.uncheckedText}
        />
      );
    case 'date':
      return <Form.DatePicker {...common} type="date" />;
    default:
      return <Form.Input {...common} />;
  }
}
