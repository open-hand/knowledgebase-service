import React, {
  useEffect, useMemo, useCallback,
} from 'react';
import PropTypes from 'prop-types';
import {
  Modal, Form, DataSet, TextArea, DateTimePicker, Select, Radio, TextField,
} from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import DataSetFactory from './dataSet';

const key = Modal.key();

const propTypes = {
  initValue: PropTypes.shape({}),
  onSubmit: PropTypes.func.isRequired,
};
const defaultProps = {
  initValue: {},
};
function CreateDoc({
  modal, submit, onSubmit, apiGateway, repoId,
}) {
  const dataSet = useMemo(() => new DataSet(DataSetFactory({ apiGateway, repoId })), []);
  const handleSubmit = useCallback(async () => {
    const data = dataSet.toData()[0];
    try {
      const validate = await dataSet.validate();
      if (dataSet.isModified() && validate) {
        const result = await onSubmit(data);  
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
    <Form dataSet={dataSet}>
      <TextField name="name" required maxLength={44} />
      <Select
        name="template"
        searchable
        searchMatcher="param"
      />
    </Form>
  );
}
CreateDoc.propTypes = propTypes;
CreateDoc.defaultProps = defaultProps;
const ObserverCreateDocModal = observer(CreateDoc);
export default function openCreateDoc({
  onCreate, apiGateway, repoId,
}) {
  Modal.open({
    title: '创建文档',
    key,
    drawer: true,
    style: {
      width: 340,
    },
    children: <ObserverCreateDocModal mode="create" onSubmit={onCreate} apiGateway={apiGateway} repoId={repoId} />,
  });
}
