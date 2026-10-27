import { Form } from '@douyinfe/semi-ui';
import type { FieldConfig } from './types';

interface FieldRendererProps {
  config: FieldConfig;
  formApi: any;
  values: Record<string, any>;
}

export default function FieldRenderer({ config, formApi, values }: FieldRendererProps) {
  const width = config.width ?? 360;
  const rules =
    config.rules ??
    (config.required ? [{ required: true, message: `请填写${config.label}` }] : undefined);
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
        <div className="framework-custom-content">{config.render({ formApi, values })}</div>
      </div>
    );
  }

  switch (config.type) {
    case 'textarea':
      return <Form.TextArea {...common} autosize={{ minRows: 3, maxRows: 8 }} />;
    case 'number':
      return <Form.InputNumber {...common} min={0} style={{ width }} />;
    case 'select':
      return (
        <Form.Select
          {...common}
          filter
          showClear
          optionList={config.options || []}
        />
      );
    case 'radio':
      return <Form.RadioGroup {...common} type="button" options={config.options || []} />;
    case 'switch':
      return <Form.Switch field={config.field} label={config.label} disabled={config.disabled} />;
    case 'date':
      return <Form.DatePicker {...common} type="date" />;
    default:
      return <Form.Input {...common} />;
  }
}
