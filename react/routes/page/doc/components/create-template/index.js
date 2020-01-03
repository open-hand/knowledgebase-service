import React, {
  useEffect, useMemo, useCallback,
} from 'react';
import PropTypes from 'prop-types';
import {
  Modal, Form, DataSet, TextArea, TextField,
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
function CreateTemplate({
  modal, submit, onSubmit, apiGateway, baseId,
}) {
  const dataSet = useMemo(() => new DataSet(DataSetFactory({ apiGateway, baseId })), []);
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
    <Form dataSet={dataSet}>
      <TextField name="name" maxLength={44} />
      <TextArea name="description" />      
    </Form>
  );
}
CreateTemplate.propTypes = propTypes;
CreateTemplate.defaultProps = defaultProps;
const ObserverCreateDocModal = observer(CreateTemplate);
export default function openCreateTemplate({
  onCreate, apiGateway, baseId,
}) {
  Modal.open({
    title: '创建模板',
    key,
    okText: '创建',   
    children: <ObserverCreateDocModal onSubmit={onCreate} apiGateway={apiGateway} baseId={baseId} />,
  });
}
