import React from 'react';
import { observer } from 'mobx-react-lite';
import { Dropdown, Button, Menu } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { openEditBaseModal } from '../baseModal';
import './BaseItem.less';

const editModal = Modal.key();

const BaseItem = observer((props) => {
  const { item } = props;

  const onDeleteBase = () => {
    console.log('deleteBase');
  };

  const handleMenuClick = (key, base) => {
    switch (key) {
      case 'edit': {
        openEditBaseModal({ baseId: item && item.id });
        break;
      }
      case 'delete': {
        onDeleteBase();
        break;
      }
      default: {
        break;
      }
    }
  };

  const menu = (
    <Menu onClick={({ key }) => { handleMenuClick(key, item); }}>
      <Menu.Item key="edit">
        设置知识库
      </Menu.Item>
      <Menu.Item key="delete">
        删除知识库
      </Menu.Item>
    </Menu>
  );
  return (
    <div className="c7n-kb-baseItem">
      <svg width="240" height="190" viewBox="-13 -13 240 190">
        <path
          className="c7n-kb-baseItem-trapezoidSvg"
          d="
            M 0,0
            L 55,0
            C 60,8 68,10, 70,12
            L 214,12
            L 214,164
            L 0,164
            Z
          "
        />
      </svg>
      <div className="c7n-kb-baseItem-mainContent">
        <div>
          <div style={{ marginBottom: 7, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className="c7n-kb-baseItem-mainContent-baseName">项目管理</span>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div className="c7n-kb-baseItem-mainContent-rangeLabel">私</div>
              <div className="c7n-kb-baseItem-mainContent-more">
                <Dropdown overlay={menu} trigger="click">
                  <Button shape="circle" icon="more_vert" />
                </Dropdown>
              </div>
            </div>
          </div>
          <div className="c7n-kb-baseItem-mainContent-updatePerson">
                    更新人
          </div>
        </div>
        <div>
          <div className="c7n-kb-baseItem-mainContent-desTitle">知识库简介</div>
          <div className="c7n-kb-baseItem-mainContent-des">
                    我会知识库简介我是知识库简介
          </div>
        </div>
      </div>
      <div />
    </div>
  );
});

export default BaseItem;
