import React, { useEffect, useContext, useState, useImperativeHandle } from 'react';
import { observer } from 'mobx-react-lite';
import { observable, toJS } from 'mobx';
import { Table } from 'choerodon-ui/pro';
import CustomCheckBox from '../../../../components/CustomCheckBox';
import { onOpenPrevievModal } from './index';
import Context from './context';
import './BaseTemplate.less';

const { Column } = Table;

const BaseTemplate = observer((props) => {
  const { baseTemplateDataSet } = useContext(Context);
  const { baseTemplateRef } = props;
  const [checkIdMap, setCheckIdMap] = useState(observable.map());

  const renderCheckBox = ({ value, text, name, record, dataSet }) => (
    !record.get('parentId') && (
      <CustomCheckBox
        checkedMap={checkIdMap}
        value={record.get('id')}
        field="id"
        dataSource={dataSet.toData()}
        selection="single"
      />
    )
  );

  const renderName = ({ value, text, name, record, dataSet }) => {
    const docId = record.get('id');
    if (!record.get('parentId')) {
      return (
        <span style={{ fontWeight: 500 }}>{text}</span>
      );
    } else {
      return (
        <span className="c7n-kb-baseTemplate-table-canPreview" role="none" onClick={() => { onOpenPrevievModal(docId); }}>{text}</span>
      );
    }
  };

  useImperativeHandle(baseTemplateRef, () => ({
    checkIdMap,
  }));
  
  return (
    <div className="c7n-kb-baseTemplate">
      <div className="c7n-kb-baseTemplate-title">选择模板</div>
      <div className="c7n-kb-baseTemplate-table">
        <Table dataSet={baseTemplateDataSet} mode="tree" border={false} filter={false}>
          <Column name="check" renderer={renderCheckBox} width={70} style={{ display: 'flex', flexDirection: 'row-reverse' }} />
          <Column name="name" renderer={renderName} />
        </Table>
      </div>
    </div>
  ); 
});

export default BaseTemplate;
