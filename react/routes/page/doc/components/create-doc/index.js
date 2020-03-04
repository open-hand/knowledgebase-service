import React, {
  useEffect, useMemo, useCallback, Fragment,
} from 'react';
import PropTypes from 'prop-types';
import {
  Modal, Form, DataSet, TextField, Table,
} from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import SmartTooltip from '../../../../../components/SmartTooltip';
import PromptInput from '../../../../../components/PromptInput';
import { onOpenPreviewModal } from '../../../../knowledge-bases/components/baseModal';
import DataSetFactory from './dataSet';
import TemplateDataSetFactory from '../template/dataSet';
import './index.less';

const key = Modal.key();
const { Column } = Table;
const propTypes = {
  initValue: PropTypes.shape({}),
  onSubmit: PropTypes.func.isRequired,
};
const defaultProps = {
  initValue: {},
};
function CreateDoc({
  modal, submit, onSubmit, pageStore,
}) {
  const { apiGateway, baseId } = pageStore;
  const templateDataSet = useMemo(() => new DataSet(TemplateDataSetFactory({ pageStore, selection: 'single' })), []);
  const dataSet = useMemo(() => new DataSet(DataSetFactory({ apiGateway, baseId, templateDataSet })), []);
  const handleSubmit = useCallback(async () => {
    const data = dataSet.toData()[0];
    try {
      const validate = await dataSet.validate();
      if (dataSet.isModified() && validate) {
        const record = templateDataSet.selected[0];
        const template = record ? record.get('id') : undefined;
        const result = await onSubmit({ ...data, template });
        return true;
      }
      return false;
    } catch (error) {
      Choerodon.prompt(error.message);
      return false;
    }
  }, [dataSet, onSubmit, submit]);
  useEffect(() => {
    modal.handleOk(handleSubmit);
  }, [modal, handleSubmit]);
  function handlePreview(record) {
    const id = record.get('id');
    onOpenPreviewModal(id);
  }
  function renderName({ text, record }) {
    return (
      <SmartTooltip title={text} placement="topLeft">
        <span
          className="link"
          onClick={() => handlePreview(record)}
        >
          {text}
        </span>
      </SmartTooltip>
    );
  }

  return (
    <Fragment>
      <Form dataSet={dataSet}>
        <PromptInput name="title" required maxLength={44} />
      </Form>
      <React.Fragment>
        <span
          style={{
            display: 'block',
            marginBottom: 8,
            fontSize: 16,
            fontWeight: 500,
            color: 'rgba(0, 0, 0, 0.8)',
          }}
        >
          选择模板
        </span>
        <Table dataSet={templateDataSet} className="c7n-create-doc-table">        
          <Column name="title" renderer={renderName} />
          <Column
            name="description"
            className="text-gray"
            renderer={({ text }) => <SmartTooltip title={text} placement="topLeft">{text}</SmartTooltip>}
          />
          <Column name="templateType" className="text-gray" renderer={({ text }) => (text === 'custom' ? '用户自定义' : '系统预置')} />
        </Table>
      </React.Fragment>
    </Fragment>
  );
}
CreateDoc.propTypes = propTypes;
CreateDoc.defaultProps = defaultProps;
const ObserverCreateDocModal = observer(CreateDoc);
export default function openCreateDoc({
  onCreate, pageStore,
}) {
  Modal.open({
    title: '创建文档',
    key,
    drawer: true,
    style: {
      width: 780,
    },
    children: <ObserverCreateDocModal mode="create" onSubmit={onCreate} pageStore={pageStore} />,
  });
}
