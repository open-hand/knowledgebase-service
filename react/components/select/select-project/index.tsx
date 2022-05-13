import React, { useMemo, forwardRef } from 'react';
import { map } from 'lodash';
import { Select } from 'choerodon-ui/pro';
import { SelectProps } from 'choerodon-ui/pro/lib/select/Select';
import { FlatSelect } from '@choerodon/components';
import useSelect, { SelectConfig } from '@choerodon/agile/lib/hooks/useSelect';
import { loadProject } from '@/api/knowledgebaseApi';

export interface SelectProductProps extends Partial<SelectProps> {
  flat?: boolean
  selected?: string[]
  menuType: 'project' | 'organization'
}

const SelectProject: React.FC<SelectProductProps> = forwardRef(
  ({
    flat, selected, menuType, ...otherProps
  }, ref: React.Ref<Select>) => {
    const config = useMemo((): SelectConfig => ({
      name: 'rangeProjectIds',
      textField: 'name',
      valueField: 'id',
      request: ({ filter, page }) => loadProject({
        page, filter, topProjectIds: selected, menuType,
      }),
      middleWare: (data) => {
        if (data && data.length) {
          return map(data, (item: { id: number, name: string }) => ({ ...item, id: String(item.id) }));
        }
        return data;
      },
      paging: true,
    }), [selected, menuType]);
    const props = useSelect(config);
    const Component = flat ? FlatSelect : Select;
    return (
      <Component
        ref={ref}
        multiple
        {...props}
        {...otherProps}
      />
    );
  },
);
export default SelectProject;
