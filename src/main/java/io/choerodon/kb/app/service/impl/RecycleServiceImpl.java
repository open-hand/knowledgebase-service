package io.choerodon.kb.app.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.kb.api.vo.RecycleVO;
import io.choerodon.kb.api.vo.SearchDTO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.RecycleService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.assembler.KnowledgeBaseAssembler;
import io.choerodon.kb.infra.feign.vo.UserDO;
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
    private static final String TYPE_PAGE = "page";
    private static final String TYPE_TEMPLATE = "template";
    private static final String TYPE_BASE = "base";
    private static final String SEARCH_TYPE = "type";

    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private KnowledgeBaseAssembler knowledgeBaseAssembler;

    @Override
    public void restoreWorkSpaceAndPage(Long organizationId, Long projectId, String type, Long id) {

        if(TYPE_BASE.equals(type)){
            knowledgeBaseService.restoreKnowledgeBase(organizationId,projectId,id);
        }
        if(TYPE_PAGE.equals(type)){
            workSpaceService.restoreWorkSpaceAndPage(organizationId, projectId, id);
        }


    }

    @Override
    public void deleteWorkSpaceAndPage(Long organizationId, Long projectId, String type, Long id) {
        if (TYPE_BASE.equals(type)) {
            knowledgeBaseService.deleteKnowledgeBase(organizationId, projectId, id);
        }
        if (TYPE_PAGE.equals(type) || TYPE_TEMPLATE.equals(type)) {
            workSpaceService.deleteWorkSpaceAndPage(organizationId, projectId, id);
        }
    }


    @Override
    public PageInfo<RecycleVO> pageList(Long projectId, Long organizationId, Pageable pageable, SearchDTO searchDTO) {
        List<RecycleVO> recycleList = new ArrayList<>();
        if(!ObjectUtils.isEmpty(searchDTO.getSearchArgs())&&TYPE_BASE.equals(searchDTO.getSearchArgs().get(SEARCH_TYPE))){
            recycleList = knowledgeBaseMapper.queryAllDetele(organizationId,projectId,searchDTO);
            recycleList.forEach(e->e.setType(TYPE_BASE));
        }else if (!ObjectUtils.isEmpty(searchDTO.getSearchArgs())&&TYPE_PAGE.equals(searchDTO.getSearchArgs().get(SEARCH_TYPE))){
            recycleList= workSpaceMapper.queryAllDeleteOptions(organizationId, projectId,searchDTO);
            recycleList.forEach(e->e.setType(TYPE_PAGE));
        }else if (!ObjectUtils.isEmpty(searchDTO.getSearchArgs())&&TYPE_TEMPLATE.equals(searchDTO.getSearchArgs().get(SEARCH_TYPE))) {
            queryTemplate(projectId, organizationId, searchDTO, recycleList);
        } else {
            recycleList = knowledgeBaseMapper.queryAllDetele(organizationId,projectId,searchDTO);
            recycleList.forEach(e->e.setType(TYPE_BASE));
            List<RecycleVO>  recyclePageList= workSpaceMapper.queryAllDeleteOptions(organizationId, projectId,searchDTO);
            recyclePageList.forEach(e->e.setType(TYPE_PAGE));
            recycleList.addAll(recyclePageList);
            queryTemplate(projectId, organizationId, searchDTO, recycleList);
        }

        knowledgeBaseAssembler.handleUserInfo(recycleList);
        recycleList.sort(Comparator.comparing(RecycleVO::getLastUpdateDate).reversed());
        return PageInfoUtil.createPageFromList(recycleList, pageable);
    }

    private List<RecycleVO> queryTemplate(Long projectId, Long organizationId, SearchDTO searchDTO,List<RecycleVO> recycleList){
        List<RecycleVO> templates = new ArrayList<>();
        if(organizationId!=null&&projectId!=null){
            templates= workSpaceMapper.queryAllDeleteOptions(0L, projectId, searchDTO);
        }
        if(organizationId!=null&&projectId==null){
            templates = workSpaceMapper.queryAllDeleteOptions(organizationId, 0L, searchDTO);
        }
        templates.forEach(e -> e.setType(TYPE_TEMPLATE));
        recycleList.addAll(templates);
        return recycleList;
    }
}
