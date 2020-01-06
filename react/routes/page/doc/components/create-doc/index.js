import React, {
  useEffect, useMemo, useCallback, Fragment,
} from 'react';
import PropTypes from 'prop-types';
import {
  Modal, Form, DataSet, TextField, Table,
} from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import DataSetFactory from './dataSet';
import TemplateDataSetFactory from '../template/dataSet';

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
    try {
      const validate = await dataSet.validate();
      if (validate) {
        await dataSet.submit();
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

  return (
    <Fragment>
      <Form dataSet={dataSet}>
        <TextField name="name" required maxLength={44} />      
      </Form>
      <Table dataSet={templateDataSet}>
        <Column name="title" />
        <Column name="description" />
      </Table>
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
