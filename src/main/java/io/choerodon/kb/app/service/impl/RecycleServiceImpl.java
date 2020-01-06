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
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.RecycleService;
import io.choerodon.kb.app.service.WorkSpaceService;
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
    private static final String SEARCH_TYPE_PAGE= "page";
    private static final String SEARCH_TYPE_BASE= "base";
    private static final String SEARCH_TYPE= "type";

    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private WorkSpaceService workSpaceService;


    @Override
    public void restoreWorkSpaceAndPage(Long organizationId, Long projectId,String type,Long id) {

        if(type.equals(SEARCH_TYPE_BASE)){
            knowledgeBaseService.restoreKnowledgeBase(organizationId,projectId,id);
        }
        if(type.equals(SEARCH_TYPE_PAGE)){
            workSpaceService.restoreWorkSpaceAndPage(organizationId, projectId, id);
        }
    }

    @Override
    public void deleteWorkSpaceAndPage(Long organizationId, Long projectId,String type,Long id) {
        if(SEARCH_TYPE_BASE.equals(type)){
            knowledgeBaseService.deleteKnowledgeBase(organizationId,projectId,id);
        }
        if(SEARCH_TYPE_PAGE.equals(type)){
            workSpaceService.deleteWorkSpaceAndPage(organizationId, projectId, id);
        }
    }


    @Override
    public PageInfo<RecycleVO> pageList(Long projectId, Long organizationId, Pageable pageable, SearchDTO searchDTO) {
        List<RecycleVO> recycleList = null;
        if(!ObjectUtils.isEmpty(searchDTO.getSearchArgs())&&searchDTO.getSearchArgs().get(SEARCH_TYPE).equals(SEARCH_TYPE_BASE)){
            recycleList = knowledgeBaseMapper.queryAllDetele(organizationId,projectId,searchDTO);
            recycleList.forEach(e->e.setType(SEARCH_TYPE_BASE));
        }else if (!ObjectUtils.isEmpty(searchDTO.getSearchArgs())&&searchDTO.getSearchArgs().get(SEARCH_TYPE).equals(SEARCH_TYPE_PAGE)){
            recycleList= workSpaceMapper.queryAllDeleteOptions(organizationId, projectId,searchDTO);
            recycleList.forEach(e->e.setType(SEARCH_TYPE_PAGE));
        }else {
            recycleList = knowledgeBaseMapper.queryAllDetele(organizationId,projectId,searchDTO);
            recycleList.forEach(e->e.setType(SEARCH_TYPE_BASE));
            List<RecycleVO>  recyclePageList= workSpaceMapper.queryAllDeleteOptions(organizationId, projectId,searchDTO);
            recyclePageList.forEach(e->e.setType(SEARCH_TYPE_PAGE));
            recycleList.addAll(recyclePageList);
        }
        recycleList.sort(Comparator.comparing(RecycleVO::getLastUpdateDate).reversed());
        return PageInfoUtil.createPageFromList(recycleList, pageable);
    }
}
