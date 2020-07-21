import React, { Component } from 'react';
import { toJS } from 'mobx';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import { Modal, Table } from 'choerodon-ui';
import './DocMove.less';

@observer
class DocMove extends Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: true,
      confirmLoading: false,
      selectedRowKeys: [],
    };
  }

  componentDidMount() {
    this.initData();
  }

  initData = () => {
    const { store } = this.props;
    store.queryMoveTree().then(() => {
      const { moveTree } = store;
      this.setState({
        loading: false,
        selectedRowKeys: [moveTree[0] && moveTree[0].id],
      });
    });
  };

  getColumn = () => [
    {
      title: '文档位置',
      dataIndex: 'name',
      key: 'name',
    },
  ];

  onSelectChange = (selectedRowKeys) => {
    this.setState({
      selectedRowKeys,
    });
  };

  getCheckboxProps = (record) => {
    const { id } = this.props;
    return ({
      disabled: record.route.split('.').indexOf(String(id)) !== -1,
      name: record.name,
    });
  };

  moveDoc = () => {
    const { selectedRowKeys } = this.state;
    const { store, id, closeDocMove, refresh } = this.props;
    this.setState({
      confirmLoading: true,
    });
    store.moveWorkSpace(selectedRowKeys[0], {
      id,
      before: true,
      targetId: 0,
    }).then(() => {
      closeDocMove(id);
      refresh(id);
    }).catch(() => {
      this.setState({
        confirmLoading: false,
      });
    });
  };

  render() {
    const { loading, selectedRowKeys, confirmLoading } = this.state;
    const { moveVisible, store, closeDocMove } = this.props;
    const data = store.getMoveTree;
    const workSpace = store.getWorkSpace;
    const spaceData = workSpace[store.spaceCode].data;
    const rowSelection = {
      selectedRowKeys,
      onChange: this.onSelectChange,
      getCheckboxProps: this.getCheckboxProps,
      hideDefaultSelections: true,
      type: 'radio',
    };

    return (
      <Modal
        title="移动"
        visible={moveVisible}
        closable={false}
        onOk={this.moveDoc}
        onCancel={() => closeDocMove()}
        maskClosable={false}
        disableOk={selectedRowKeys.length === 0}
        confirmLoading={confirmLoading}
        className="c7n-doc-move"
      >
        <Table
          rowSelection={rowSelection}
          dataSource={data}
          columns={this.getColumn()}
          rowKey={record => record.id}
          pagination={false}
          loading={loading}
          filterBar={false}
          defaultExpandedRowKeys={[spaceData.rootId]}
          scroll={{ y: 400 }}
        />
      </Modal>
    );
  }
}

export default withRouter(injectIntl(DocMove));
