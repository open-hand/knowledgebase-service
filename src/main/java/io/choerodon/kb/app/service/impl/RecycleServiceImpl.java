package io.choerodon.kb.app.service.impl;

import java.util.Comparator;
import java.util.List;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import io.choerodon.kb.api.vo.RecycleVO;
import io.choerodon.kb.api.vo.SearchDTO;
import io.choerodon.kb.app.service.RecycleService;
import io.choerodon.kb.infra.mapper.KnowledgeBaseMapper;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.PageInfoUtil;

/**
 * @author: 25499
 * @date: 2020/1/3 10:25
 * @description:
 */
@Service
public class RecycleServiceImpl implements RecycleService {
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public void deleteWorkSpaceAndPage(Long organizationId, Long projectId, Long workspaceId) {

    }

    @Autowired

    private static final String SEARCH_TYPE_PAGE= "page";
    private static final String SEARCH_TYPE_BASE= "base";
    private static final String SEARCH_TYPE= "type";

    private WorkSpaceMapper workSpaceMapper;

    @Override
    public void restoreWorkSpaceAndPage(Long organizationId, Long projectId, Long workspaceId) {

    }

    @Override
    public PageInfo<RecycleVO> pageList(Long projectId, Long organizationId, Pageable pageable, SearchDTO searchDTO) {
        List<RecycleVO> recycleList = null;
        if(!ObjectUtils.isEmpty(searchDTO)&&searchDTO.getSearchArgs().get(SEARCH_TYPE).equals(SEARCH_TYPE_BASE)){
            recycleList = knowledgeBaseMapper.queryAllDetele(null,projectId,searchDTO);
            recycleList.forEach(e->e.setType(SEARCH_TYPE_BASE));
        }else if (!ObjectUtils.isEmpty(searchDTO)&&searchDTO.getSearchArgs().get(SEARCH_TYPE).equals(SEARCH_TYPE_PAGE)){
            recycleList= workSpaceMapper.queryAllDeleteOptions(organizationId, projectId,searchDTO);
            recycleList.forEach(e->e.setType(SEARCH_TYPE_PAGE));
        }else {
            recycleList = knowledgeBaseMapper.queryAllDetele(null,projectId,searchDTO);
            recycleList.forEach(e->e.setType(SEARCH_TYPE_BASE));
            List<RecycleVO>  recyclePageList= workSpaceMapper.queryAllDeleteOptions(organizationId, projectId,searchDTO);
            recyclePageList.forEach(e->e.setType(SEARCH_TYPE_PAGE));
            recycleList.addAll(recyclePageList);
        }
        recycleList.sort(Comparator.comparing(RecycleVO::getLastUpdateDate).reversed());
        return PageInfoUtil.createPageFromList(recycleList, pageable);
    }
}
