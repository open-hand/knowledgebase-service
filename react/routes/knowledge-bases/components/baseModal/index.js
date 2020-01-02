import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Button, Modal, DataSet, Form, TextArea, Select } from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/master';
import PromptInput from '../../../../components/PromptInput';
import { getBaseInfo, createBase, editBase } from '../../../../api/knowledgebaseApi';
import BaseModalDataSet from './BaseModalDataSet';
import BaseTemplateDataSet from './BaseTemplateDataSet';
import BaseTemplate from './BaseTemplate';
import Context from './context';

const key = Modal.key();

const BaseModal = observer(({ modal, initValue, submit, mode, onCallback }) => {
  const dataSet = useMemo(() => new DataSet(BaseModalDataSet({ initValue })), [initValue]);
  const baseTemplateDataSet = useMemo(() => new DataSet(BaseTemplateDataSet()), []);
  const data = dataSet.toData()[0];
  console.log(data);

  const handleSubmit = useCallback(async () => {
    const {
      name, description, range, projectId, ...rest
    } = data;
    console.log(data);
    console.log(name, description, range, projectId);
    if (mode === 'edit' && !dataSet.isModified()) {
      return true;
    }
    try {
      const validate = await dataSet.validate();
      if (dataSet.isModified() && (validate || (name && (range === 'private' || range === 'allProjects') && (!projectId || !projectId.length)))) {
        console.log('验证通过啦');
        const result = await submit(data);
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
        <Select name="range" />
        {data.range === 'designatedProject' && <Select name="projectId" />}
      </Form>
      <BaseTemplate />
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
