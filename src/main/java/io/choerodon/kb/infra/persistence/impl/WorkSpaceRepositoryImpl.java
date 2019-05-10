package io.choerodon.kb.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.entity.PageDetailE;
import io.choerodon.kb.domain.kb.entity.WorkSpaceE;
import io.choerodon.kb.domain.kb.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.dataobject.PageDetailDO;
import io.choerodon.kb.infra.dataobject.UserDO;
import io.choerodon.kb.infra.dataobject.WorkSpaceDO;
import io.choerodon.kb.infra.feign.UserFeignClient;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class WorkSpaceRepositoryImpl implements WorkSpaceRepository {

    private WorkSpaceMapper workSpaceMapper;
    private UserFeignClient userFeignClient;

    public WorkSpaceRepositoryImpl(WorkSpaceMapper workSpaceMapper,
                                   UserFeignClient userFeignClient) {
        this.workSpaceMapper = workSpaceMapper;
        this.userFeignClient = userFeignClient;
    }

    @Override
    public WorkSpaceE inset(WorkSpaceE workSpaceE) {
        WorkSpaceDO workSpaceDO = ConvertHelper.convert(workSpaceE, WorkSpaceDO.class);
        if (workSpaceMapper.insert(workSpaceDO) != 1) {
            throw new CommonException("error.work.space.insert");
        }
        return ConvertHelper.convert(workSpaceDO, WorkSpaceE.class);
    }

    @Override
    public WorkSpaceE update(WorkSpaceE workSpaceE) {
        WorkSpaceDO workSpaceDO = ConvertHelper.convert(workSpaceE, WorkSpaceDO.class);
        if (workSpaceMapper.updateByPrimaryKey(workSpaceDO) != 1) {
            throw new CommonException("error.work.space.update");
        }
        return ConvertHelper.convert(workSpaceDO, WorkSpaceE.class);
    }

    @Override
    public Boolean hasChildWorkSpace(String type, Long resourceId, Long parentId) {
        return workSpaceMapper.hasChildWorkSpace(type, resourceId, parentId);
    }

    @Override
    public String queryMaxRank(String type, Long resourceId, Long parentId) {
        return workSpaceMapper.queryMaxRank(type, resourceId, parentId);
    }

    @Override
    public WorkSpaceE selectById(Long id) {
        return ConvertHelper.convert(workSpaceMapper.selectByPrimaryKey(id), WorkSpaceE.class);
    }

    @Override
    public PageDetailE queryDetail(Long id) {
        PageDetailE pageDetailE = ConvertHelper.convert(workSpaceMapper.queryDetail(id), PageDetailE.class);
        Long[] ids = new Long[2];
        ids[0] = pageDetailE.getCreatedBy();
        ids[1] = pageDetailE.getLastUpdatedBy();
        List<UserDO> userDOList = userFeignClient.listUsersByIds(ids, false).getBody();
        String createName = "";
        String lastUpdatedName = "";
        for (UserDO userDO : userDOList) {
            if (ids[0].equals(userDO.getId())) {
                createName = userDO.getLoginName() + userDO.getRealName();
            }
            if (ids[1].equals(userDO.getId())) {
                lastUpdatedName = userDO.getLoginName() + userDO.getRealName();
            }
        }
        pageDetailE.setCreateName(createName);
        pageDetailE.setLastUpdatedName(lastUpdatedName);

        return pageDetailE;
    }

    @Override
    public PageDetailE queryReferenceDetail(Long id) {
        return ConvertHelper.convert(workSpaceMapper.queryReferenceDetail(id), PageDetailE.class);
    }

    @Override
    public void deleteByRoute(String route) {
        workSpaceMapper.deleteByRoute(route);
    }

    @Override
    public List<WorkSpaceE> workSpaceListByParentIds(Long resourceId, List<Long> parentIds, String type) {
        return ConvertHelper.convertList(workSpaceMapper.workSpaceListByParentIds(resourceId, parentIds, type), WorkSpaceE.class);
    }

    @Override
    public List<WorkSpaceE> workSpaceListByParentId(Long resourceId, Long parentId, String type) {
        return ConvertHelper.convertList(workSpaceMapper.workSpaceListByParentId(resourceId, parentId, type), WorkSpaceE.class);
    }
}
