import React, { useContext } from 'react';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { Dropdown, Button, Menu } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import queryString from 'querystring';
import { Choerodon, stores } from '@choerodon/boot';
import useFormatMessage from '@/hooks/useFormatMessage';
import { openEditBaseModal } from '../baseModal';
import SmartTooltip from '../../../../components/SmartTooltip';
import UserHead from '../../../../components/UserHead';
import { moveToBin, orgMoveToBin } from '../../../../api/knowledgebaseApi';
import Store from '../../stores';
import './BaseItem.less';

const { AppState } = stores;

const BaseItem = observer((props) => {
  const { knowledgeHomeStore, binTableDataSet, type } = useContext(Store);
  const {
    item, baseType, history, className,
  } = props;
  const formatMessage = useFormatMessage('knowledge.baseItem');

  const onDeleteBase = () => {
    if (type === 'project') {
      moveToBin(item.id).then(() => {
        knowledgeHomeStore.axiosProjectBaseList();
        binTableDataSet.query();
      }).catch(() => {
        Choerodon.prompt(formatMessage({ id: 'move_recycle_failed' }));
      });
    } else {
      orgMoveToBin(item.id).then(() => {
        knowledgeHomeStore.axiosOrgBaseList();
        binTableDataSet.query();
      }).catch(() => {
        Choerodon.prompt(formatMessage({ id: 'move_recycle_failed' }));
      });
    }
  };

  const openDeletePromptModal = () => {
    Modal.open({
      title: formatMessage({ id: 'delete_title' }),
      children: formatMessage({ id: 'delete_des' }, { name: item.name }),
      onOk: onDeleteBase,
    });
  };

  const handleMenuClick = (key) => {
    switch (key) {
      case 'edit': {
        openEditBaseModal({ initValue: item, onCallBack: type === 'project' ? knowledgeHomeStore.axiosProjectBaseList : knowledgeHomeStore.axiosOrgBaseList, type });
        break;
      }
      case 'delete': {
        openDeletePromptModal();
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
        {formatMessage({ id: 'setting' })}
      </Menu.Item>
      <Menu.Item key="delete">
        {formatMessage({ id: 'delete' })}
      </Menu.Item>
    </Menu>
  );

  const handleClickBase = () => {
    const urlParams = AppState.currentMenuType;
    history.push(`/knowledge/${type}/doc/${item.id}?${queryString.stringify({ baseName: item.name, ...urlParams })}`);
  };

  let rangeLabel = '私';

  if (item.openRange !== 'range_private') {
    if (baseType === 'project' || (baseType === 'organization' && type === 'organization')) {
      rangeLabel = '公';
    } else if (item.projectId) {
      rangeLabel = (
        <span style={{ display: 'flex', overflow: 'hidden' }}>
          <span style={{ whiteSpace: 'noWrap' }}>
            {formatMessage({ id: 'project' })}
            ：
          </span>
          <SmartTooltip title={item.source}>
            {item.source}
          </SmartTooltip>
        </span>
      );
    } else {
      rangeLabel = (
        <span style={{ display: 'flex', overflow: 'hidden' }}>
          <span style={{ whiteSpace: 'noWrap' }}>
            {formatMessage({ id: 'organization' })}
            ：
          </span>
          <SmartTooltip title={item.source}>
            {item.source}
          </SmartTooltip>
        </span>
      );
    }
  }

  return (
    <div className={`${className || ''} c7n-kb-baseItem`} role="none" onClick={handleClickBase}>
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
          <div style={{
            marginBottom: 7, display: 'flex', justifyContent: 'space-between', alignItems: 'center', overflow: 'hidden',
          }}
          >
            <span className="c7n-kb-baseItem-mainContent-baseName">
              <SmartTooltip title={item.name}>
                {item.name}
              </SmartTooltip>
            </span>
            <div style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center', overflow: 'hidden',
            }}
            >
              <div className="c7n-kb-baseItem-mainContent-rangeLabel">{rangeLabel}</div>
              {
                type === baseType && (
                  <div className="c7n-kb-baseItem-mainContent-more" role="none" onClick={(e) => { e.stopPropagation(); }}>
                    <Dropdown overlay={menu} trigger="click">
                      <Button shape="circle" icon="more_vert" />
                    </Dropdown>
                  </div>
                )
              }
            </div>
          </div>
          <div className="c7n-kb-baseItem-mainContent-updatePerson">
            {
              item.workSpaceRecents && item.workSpaceRecents.length > 0 && item.workSpaceRecents.slice(0, 5).map((recent) => (
                <UserHead
                  user={recent.lastUpdatedUser}
                  extraToolTip={(
                    <span>
                      {formatMessage({ id: 'update_in' }, { name: recent.updateworkSpace })}
                      {recent.lastUpdateDate}
                    </span>
                  )}
                  hiddenText
                  size={24}
                />
              ))
            }
          </div>
        </div>
        <div>
          <div className="c7n-kb-baseItem-mainContent-desTitle">
            {formatMessage({ id: 'introduction' })}
          </div>
          <div className="c7n-kb-baseItem-mainContent-des">
            <SmartTooltip title={item.description} width="200px">
              {item.description || formatMessage({ id: 'no_introduction' })}
            </SmartTooltip>
          </div>
        </div>
      </div>
    </div>
  );
});

export default withRouter(BaseItem);
