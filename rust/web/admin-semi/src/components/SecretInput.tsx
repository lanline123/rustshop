import { useState } from 'react';
import { Button, Input } from '@douyinfe/semi-ui';
import { IconEyeClosed, IconEyeOpened } from '@douyinfe/semi-icons';

interface SecretInputProps {
  value?: string;
  placeholder?: string;
  onChange: (value: string) => void;
}

export default function SecretInput({ value = '', placeholder, onChange }: SecretInputProps) {
  const [visible, setVisible] = useState(false);
  return (
    <div className="secret-input">
      <Input
        value={value}
        placeholder={placeholder}
        mode={visible ? undefined : 'password'}
        onChange={onChange}
      />
      <Button
        type="tertiary"
        theme="borderless"
        icon={visible ? <IconEyeClosed /> : <IconEyeOpened />}
        onClick={() => setVisible((current) => !current)}
      />
    </div>
  );
}
