import React, {
  useMemo, useCallback, useEffect, createRef, useContext, useState, useImperativeHandle,
} from 'react';
import { observer } from 'mobx-react-lite';
import { observable, toJS } from 'mobx';
import {
  Modal, DataSet, Form, TextArea, Select, Table, TextField,
} from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/boot';
import PromptInput from '../../../../components/PromptInput';
import {
  createBase, createOrgBase, editBase, editOrgBase, getPageInfo, getOrgPageInfo,
} from '../../../../api/knowledgebaseApi';
import BaseModalDataSet from './BaseModalDataSet';
import BaseTemplateDataSet from './BaseTemplateDataSet';
import CustomCheckBox from '../../../../components/CustomCheckBox';
import TemplateViewer from '../../../../components/TemplateViewer';
import Context from './context';
import './index.less';
import './BaseTemplate.less';

const { Column } = Table;
const key = Modal.key();
export async function onOpenPreviewModal(docId, type) {
  if (docId) {
    const res = type === 'project' ? await getPageInfo(docId) : await getOrgPageInfo(docId);
    Modal.open({
      key: Modal.key(),
      title: `预览"${res.pageInfo.title}"`,
      keyboardClosable: true,
      children: (
        <TemplateViewer data={res} />
      ),
      cancelText: '关闭',
      footer: (okBtn, cancelBtn) => cancelBtn,
      style: { width: '12rem', height: '82.5%' },
      className: 'c7n-kb-basePreviewModal',
    });
  }
}

const BaseTemplate = observer((props) => {
  const { baseTemplateDataSet, type } = useContext(Context);
  const { baseTemplateRef } = props;
  const [checkIdMap] = useState(observable.map());

  const renderCheckBox = ({ record, dataSet }) => (
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

  const renderName = ({ text, record }) => {
    const docId = record.get('id');
    if (!record.get('parentId')) {
      return (
        <span style={{ fontWeight: 500 }}>{text}</span>
      );
    }
    return (
      <span className="c7n-kb-baseTemplate-table-canPreview" role="none" onClick={() => { onOpenPreviewModal(docId, type); }}>{text}</span>
    );
  };

  const getQueryFields = () => ({
    name: <TextField clearButton labelLayout="float" />,
  });

  useImperativeHandle(baseTemplateRef, () => ({
    checkIdMap,
  }));
  return (
    <div className="c7n-kb-baseTemplate">
      <div className="c7n-kb-baseTemplate-title">选择模板</div>
      <div className="c7n-kb-baseTemplate-table">
        <Table dataSet={baseTemplateDataSet} mode="tree" border={false} queryFields={getQueryFields()}>
          <Column name="check" renderer={renderCheckBox} width={100} style={{ display: 'flex', flexDirection: 'row-reverse' }} />
          <Column name="name" renderer={renderName} />
        </Table>
      </div>
    </div>
  );
});

const BaseModal = observer(({
  modal, initValue, submit, mode, onCallback, type,
}) => {
  const baseTemplateRef = createRef();
  const dataSet = useMemo(() => new DataSet(BaseModalDataSet({ initValue, type })), [initValue, type]);
  const baseTemplateDataSet = useMemo(() => new DataSet(BaseTemplateDataSet({ type })), [type]);
  const data = dataSet.toData()[0];
  const handleSubmit = useCallback(async () => {
    const {
      id, name, description, openRange, rangeProjectIds, objectVersionNumber,
    } = data;
    const { checkIdMap } = baseTemplateRef.current || {};
    if (mode === 'edit' && !dataSet.isModified()) {
      return true;
    }
    try {
      const validate = await dataSet.validate();
      // if (validate && !name) { // 创建知识库，没有更改Form表单，点击创建,validate会校验通过，让它关闭
      //   return true;
      // }
      if (dataSet.isModified() && (validate || (name && (openRange === 'range_private' || openRange === 'range_public') && (!rangeProjectIds || !rangeProjectIds.length)))) {
        const templateBaseId = checkIdMap && checkIdMap.size > 0 ? Object.keys(toJS(checkIdMap))[0] : null;
        const submitData = {
          templateBaseId, name, description: description ?? '', openRange, rangeProjectIds,
        };
        if (mode === 'edit') {
          submitData.id = id;
          submitData.objectVersionNumber = objectVersionNumber;
        }
        await submit(submitData);
        Choerodon.prompt(mode === 'edit' ? '设置成功' : '创建成功');
        onCallback();
        return true;
      }
      return false;
    } catch (error) {
      return false;
    }
  }, [data, mode, onCallback, submit]);

  useEffect(() => {
    modal.handleOk(handleSubmit);
  }, [modal, handleSubmit]);

  return (
    <Context.Provider value={{ baseTemplateDataSet }}>
      <Form dataSet={dataSet}>
        <PromptInput name="name" required maxLength={44} />
        <TextArea
          name="description"
          resize="vertical"
        />
        <Select name="openRange" />
        {data.openRange === 'range_project' && <Select name="rangeProjectIds" />}
      </Form>
      {
        mode === 'create' && (
          <BaseTemplate baseTemplateRef={baseTemplateRef} type={type} />
        )
      }
    </Context.Provider>
  );
});

export function openCreateBaseModal({ onCallBack, type }) {
  Modal.open({
    key,
    drawer: true,
    title: '创建知识库',
    children: (
      <BaseModal mode="create" submit={type === 'project' ? createBase : createOrgBase} onCallback={onCallBack} type={type} />
    ),
    okText: '创建',
    cancel: '取消',
    style: { width: '3.8rem' },
  });
}

export async function openEditBaseModal({ initValue, onCallBack, type }) {
  if (initValue.id) {
    Modal.open({
      key,
      drawer: true,
      title: '知识库设置',
      children: (
        <BaseModal mode="edit" submit={type === 'project' ? editBase : editOrgBase} initValue={initValue} onCallback={onCallBack} type={type} />
      ),
      okText: '保存',
      cancel: '取消',
      style: { width: '3.8rem' },
    });
  }
}
