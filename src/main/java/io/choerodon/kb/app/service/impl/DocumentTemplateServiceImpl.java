package io.choerodon.kb.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.DocumentTemplateService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author zhaotianxin
 * @since 2020/1/2
 */
@Service
public class DocumentTemplateServiceImpl implements DocumentTemplateService {
    private static final String CUSTOM = "custom";

    private static final String SYS_PRESET = "sys_preset";
    @Autowired
    private WorkSpaceService workSpaceService;

    @Autowired
    private WorkSpaceMapper workSpaceMapper;

    @Autowired
    private BaseFeignClient baseFeignClient;

    @Override
    public DocumentTemplateInfoVO createTemplate(Long projectId, Long organizationId, PageCreateWithoutContentVO pageCreateVO) {
        WorkSpaceInfoVO workSpaceAndPage = workSpaceService.createWorkSpaceAndPage(0L, projectId, pageCreateVO);
        List<Long> userIds = new ArrayList<>();
        userIds.add(workSpaceAndPage.getCreatedBy());
        userIds.add(workSpaceAndPage.getPageInfo().getLastUpdatedBy());
        Map<Long, UserDO> users = findUsers(userIds);
        DocumentTemplateInfoVO documentTemplateInfoVO = new DocumentTemplateInfoVO();
        documentTemplateInfoVO.setCreatedBy(workSpaceAndPage.getCreatedBy());
        documentTemplateInfoVO.setLastUpdatedBy(workSpaceAndPage.getPageInfo().getLastUpdatedBy());
        documentTemplateInfoVO.setId(workSpaceAndPage.getId());
        documentTemplateInfoVO.setTitle(workSpaceAndPage.getPageInfo().getTitle());
        documentTemplateInfoVO.setDescription(workSpaceAndPage.getDescription());
        documentTemplateInfoVO.setObjectVersionNumber(workSpaceAndPage.getObjectVersionNumber());
        documentTemplateInfoVO.setCreationDate(workSpaceAndPage.getCreationDate());
        documentTemplateInfoVO.setLastUpdateDate(workSpaceAndPage.getPageInfo().getLastUpdateDate());
        documentTemplateInfoVO.setTemplateType(CUSTOM);
        return toTemplateInfoVO(users,documentTemplateInfoVO);
    }

    @Override
    public DocumentTemplateInfoVO updateTemplate(Long organizationId, Long projectId, Long id, String searchStr, PageUpdateVO pageUpdateVO) {
        WorkSpaceInfoVO workSpaceInfoVO = workSpaceService.updateWorkSpaceAndPage(0L, projectId, id, searchStr, pageUpdateVO);
        DocumentTemplateInfoVO documentTemplateInfoVO = new DocumentTemplateInfoVO();
        documentTemplateInfoVO.setCreatedBy(workSpaceInfoVO.getCreatedBy());
        documentTemplateInfoVO.setLastUpdatedBy(workSpaceInfoVO.getPageInfo().getLastUpdatedBy());
        documentTemplateInfoVO.setId(workSpaceInfoVO.getId());
        documentTemplateInfoVO.setTitle(workSpaceInfoVO.getPageInfo().getTitle());
        documentTemplateInfoVO.setDescription(workSpaceInfoVO.getDescription());
        documentTemplateInfoVO.setObjectVersionNumber(workSpaceInfoVO.getObjectVersionNumber());
        documentTemplateInfoVO.setCreationDate(workSpaceInfoVO.getCreationDate());
        documentTemplateInfoVO.setLastUpdateDate(workSpaceInfoVO.getPageInfo().getLastUpdateDate());
        documentTemplateInfoVO.setLastUpdatedUser(workSpaceInfoVO.getPageInfo().getLastUpdatedUser());
        documentTemplateInfoVO.setCreatedUser(workSpaceInfoVO.getCreateUser());
        documentTemplateInfoVO.setTemplateType(CUSTOM);
        return documentTemplateInfoVO;
    }

    @Override
    public PageInfo<DocumentTemplateInfoVO> listTemplate(Long organizationId, Long projectId,Long baseId, Pageable pageable, SearchVO searchVO) {
        PageInfo<DocumentTemplateInfoVO> pageInfo = PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize()).doSelectPageInfo(() -> workSpaceMapper.listDocumentTemplate(0L, projectId, baseId, searchVO));

        if (CollectionUtils.isEmpty(pageInfo.getList())) {
            return new PageInfo<DocumentTemplateInfoVO>();
        }
        List<DocumentTemplateInfoVO> list = pageInfo.getList();
        List<Long> userIds = new ArrayList<>();
        list.forEach(v -> {
            userIds.add(v.getCreatedBy());
            userIds.add(v.getLastUpdatedBy());
        });
        Map<Long, UserDO> users = findUsers(userIds);
        list.forEach(v -> toTemplateInfoVO(users,v));
        pageInfo.setList(list);
        return pageInfo;
    }

    private DocumentTemplateInfoVO toTemplateInfoVO(Map<Long, UserDO> users,DocumentTemplateInfoVO documentTemplateInfoVO){
        documentTemplateInfoVO.setCreatedUser(users.get(documentTemplateInfoVO.getCreatedBy()));
        documentTemplateInfoVO.setLastUpdatedUser(users.get(documentTemplateInfoVO.getLastUpdatedBy()));
        return documentTemplateInfoVO;
    }

    private Map<Long, UserDO> findUsers(List<Long> users){
        List<UserDO> usersDO = baseFeignClient.listUsersByIds(users.toArray(new Long[users.size()]), false).getBody();
        if(CollectionUtils.isEmpty(usersDO)){
            return new HashMap<>();
        }
        Map<Long, UserDO> collect = usersDO.stream().collect(Collectors.toMap(UserDO::getId, x -> x));
        return collect;
    }

}
