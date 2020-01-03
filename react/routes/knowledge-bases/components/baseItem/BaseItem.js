import React, { useContext } from 'react';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { Dropdown, Button, Menu } from 'choerodon-ui';
import { Choerodon, stores } from '@choerodon/boot';
import { Modal } from 'choerodon-ui/pro';
import { openEditBaseModal } from '../baseModal';
import SmartTooltip from '../../../../components/SmartTooltip';
import { moveToBin } from '../../../../api/knowledgebaseApi';
import Store from '../../stores';
import './BaseItem.less';

const { AppState } = stores;

const BaseItem = observer((props) => {
  const { knowledgeHomeStore, binTableDataSet, type } = useContext(Store);
  const { item, baseType, history } = props;
  const onDeleteBase = () => {
    moveToBin(item.id).then(() => {
      knowledgeHomeStore.axiosProjectBaseList();
      binTableDataSet.query();
    }).catch(() => {
      Choerodon.prompt('移到回收站失败');
    });
  };

  const handleMenuClick = (key) => {
    switch (key) {
      case 'edit': {
        openEditBaseModal({ initValue: item, onCallBack: knowledgeHomeStore.axiosProjectBaseList });
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
    <Menu onClick={({ key }) => { handleMenuClick(key); }}>
      <Menu.Item key="edit">
        设置知识库
      </Menu.Item>
      <Menu.Item key="delete">
        删除知识库
      </Menu.Item>
    </Menu>
  );

  const handleClickBase = () => {
    const urlParams = AppState.currentMenuType;
    history.push(`/knowledge/${type}/doc/${item.id}?type=${urlParams.type}&id=${urlParams.id}&name=${encodeURIComponent(urlParams.name)}&organizationId=${urlParams.organizationId}&orgId=${urlParams.organizationId}`);
  };

  return (
    <div className="c7n-kb-baseItem" role="none" onClick={handleClickBase}>
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
            <span className="c7n-kb-baseItem-mainContent-baseName">
              <SmartTooltip title={item.name} width="160px">
                {item.name}
              </SmartTooltip>
            </span>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div className="c7n-kb-baseItem-mainContent-rangeLabel">{item.openRange === 'range_private' ? '私' : '公'}</div>
              {
                type === baseType && (
                  <div className="c7n-kb-baseItem-mainContent-more" role="none" onClick={(e) => e.stopPropagation()}>
                    <Dropdown overlay={menu} trigger="click">
                      <Button shape="circle" icon="more_vert" />
                    </Dropdown>
                  </div>
                )
              }
            </div>
          </div>
          <div className="c7n-kb-baseItem-mainContent-updatePerson">
              更新人
          </div>
        </div>
        <div>
          <div className="c7n-kb-baseItem-mainContent-desTitle">知识库简介</div>
          <div className="c7n-kb-baseItem-mainContent-des">
            <SmartTooltip title={item.description} width="200px">
              {item.description || '暂无'}
            </SmartTooltip>
          </div>
        </div>
      </div>
      <div />
    </div>
  );
});

export default withRouter(BaseItem);
