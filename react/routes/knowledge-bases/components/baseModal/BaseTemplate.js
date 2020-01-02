import React, { useEffect, useContext, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { observable, toJS } from 'mobx';
import { Table } from 'choerodon-ui/pro';
import CustomCheckBox from '../../../../components/CustomCheckBox';
import Context from './context';
import './BaseTemplate.less';

const { Column } = Table;

const BaseTemplate = observer(() => {
  const { baseTemplateDataSet } = useContext(Context);
  const [checkIdMap, setCheckIdMap] = useState(observable.map());
  
  const renderCheckBox = ({ value, text, name, record, dataSet }) => (
    !record.get('groupId') && (
    <CustomCheckBox
      checkedMap={checkIdMap}
      value={record.id}
      field="id"
      dataSource={dataSet.toData()}
      selection="single"
    />
    )
  );

  return (
    <div className="c7n-kb-baseTemplate">
      <div className="c7n-kb-baseTemplate-title">选择模板</div>
      <div className="c7n-kb-baseTemplate-table">
        <Table dataSet={baseTemplateDataSet} mode="tree" border={false} filter={false}>
          <Column name="check" renderer={renderCheckBox} width={70} style={{ display: 'flex', flexDirection: 'row-reverse' }} />
          <Column name="name" />
        </Table>
      </div>
    </div>
  ); 
});

export default BaseTemplate;
