import React, { useMemo, useCallback, useEffect, createRef } from 'react';
import { observer } from 'mobx-react-lite';
import { toJS } from 'mobx';
import { Button, Modal, DataSet, Form, TextArea, Select } from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/master';
import PromptInput from '../../../../components/PromptInput';
import DocViewer from '../../../../components/DocViewer';
import { getBaseInfo, createBase, editBase } from '../../../../api/knowledgebaseApi';
import BaseModalDataSet from './BaseModalDataSet';
import BaseTemplateDataSet from './BaseTemplateDataSet';
import BaseTemplate from './BaseTemplate';
import Context from './context';
import PreviewModalStore from './PreviewModalStore';

const key = Modal.key();

const BaseModal = observer(({ modal, initValue, submit, mode, onCallback }) => {
  const baseTemplateRef = createRef();
  const dataSet = useMemo(() => new DataSet(BaseModalDataSet({ initValue })), [initValue]);
  const baseTemplateDataSet = useMemo(() => new DataSet(BaseTemplateDataSet()), []);
  const data = dataSet.toData()[0];
  const handleSubmit = useCallback(async () => {
    const {
      name, description, openRange, rangeProjectIds, ...rest
    } = data;
    const { checkIdMap } = baseTemplateRef.current || {};
    console.log(toJS(checkIdMap));
    console.log(Object.keys(checkIdMap)[0]);
    console.log(data);
    console.log(name, description, openRange, rangeProjectIds);
    if (mode === 'edit' && !dataSet.isModified()) {
      return true;
    }
    try {
      const validate = await dataSet.validate();
      if (dataSet.isModified() && (validate || (name && (openRange === 'range_private' || openRange === 'range_public') && (!rangeProjectIds || !rangeProjectIds.length)))) {
        console.log('校验通过啦');
        const templateBaseId = checkIdMap && checkIdMap.size > 0 ? Object.keys(checkIdMap)[0] : null;
        const result = await submit({ templateBaseId, ...data });
        onCallback(result);
        return true;
      }
      return false;
    } catch (error) {
      Choerodon.prompt(error.message);
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
        />
        <Select name="openRange" />
        {data.openRange === 'range_project' && <Select name="rangeProjectIds" />}
      </Form>
      <BaseTemplate baseTemplateRef={baseTemplateRef} />
    </Context.Provider>
  );
});

export function openCreateBaseModal() {
  Modal.open({
    key,
    drawer: true,
    title: '创建知识库',
    children: (
      <BaseModal mode="create" submit={createBase} onCallback={() => {}} />
    ),
    okText: '创建',
    cancel: '取消',
    style: { width: '3.8rem' },
  });
}

export async function openEditBaseModal({ baseId }) {
  if (baseId) {
    const initValue = await getBaseInfo(baseId);
    Modal.open({
      key,
      drawer: true,
      title: '知识库设置',
      children: (
        <BaseModal mode="edit" submit={editBase} initValue={initValue} onCallback={() => {}} />
      ),
      okText: '保存',
      cancel: '取消',
      style: { width: '3.8rem' },
    });
  }
}

export async function onOpenPrevievModal(docId) {
  if (docId) {
    const store = useMemo(() => new PreviewModalStore(), []);
    const { getMode: mode, getDoc: data } = store;

    Modal.open({
      key,
      drawer: true,
      title: '预览mmmm',
      children: (
        <DocViewer
          readOnly
          fullScreen={false}
          data={data}
          store={store}
        />
      ),
      okText: '关闭',
      footer: (okBtn, cancelBtn) => okBtn,
      style: { width: '12rem' },
    });
  }
}
