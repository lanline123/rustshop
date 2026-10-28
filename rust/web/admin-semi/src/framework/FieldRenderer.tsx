import { useEffect, useState } from 'react';
import { Form, Switch } from '@douyinfe/semi-ui';
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

  return (
    <Form.Select
      {...(common as any)}
      multiple={config.multiple}
      filter
      optionList={options}
    />
  );
}

function SwitchField({
  config,
  formApi,
  values,
}: {
  config: FieldConfig;
  formApi: any;
  values: Record<string, any>;
}) {
  const normalize = (value: any) => value === true || Number(value) === 1;
  const [checked, setChecked] = useState(normalize(values[config.field]));

  useEffect(() => {
    setChecked(normalize(values[config.field]));
  }, [values[config.field]]);

  return (
    <div className="framework-switch-field">
      <label className="framework-custom-label">{config.label}</label>
      <div className="framework-switch-control">
        <Switch
          checked={checked}
          disabled={config.disabled}
          onChange={(next) => {
            setChecked(next);
            formApi?.setValue(config.field, next ? 1 : 0);
          }}
        />
        <span className={checked ? 'framework-switch-on' : 'framework-switch-off'}>
          {checked ? config.checkedText || '已开启' : config.uncheckedText || '已关闭'}
        </span>
      </div>
    </div>
  );
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
      return <Form.RadioGroup {...common} options={config.options || []} />;
    case 'switch':
      return <SwitchField config={config} formApi={formApi} values={values} />;
    case 'date':
      return <Form.DatePicker {...common} type="date" />;
    default:
      return <Form.Input {...common} />;
  }
}
