package io.choerodon.kb.app.service.assembler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.api.vo.WorkSpaceRecentVO;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@Component
public class KnowledgeBaseAssembler {
    private static final String RANGE_PROJECT= "range_project";

    @Autowired
    private WorkSpaceMapper workSpaceMapper;

    @Autowired
    private BaseFeignClient baseFeignClient;

    @Autowired
    private ModelMapper modelMapper;

    public KnowledgeBaseInfoVO dtoToInfoVO(KnowledgeBaseDTO knowledgeBaseDTO){
        KnowledgeBaseInfoVO knowledgeBaseInfoVO = new KnowledgeBaseInfoVO();
        modelMapper.map(knowledgeBaseDTO,knowledgeBaseInfoVO);
        if(RANGE_PROJECT.equals(knowledgeBaseDTO.getOpenRange())){
            Long[] map = modelMapper.map(knowledgeBaseDTO.getRangeProject().split(","), new TypeToken<Long[]>() {
            }.getType());
            knowledgeBaseInfoVO.setRangeProjectIds(Arrays.asList(map));
        }
        // 查询最近更新的用户名
        return knowledgeBaseInfoVO;
    }

    public void docheage(List<KnowledgeBaseListVO> knowledgeBaseListVOList, Long organizationId, Long projectId) {
        List<Long> baseIds = knowledgeBaseListVOList.stream().map(KnowledgeBaseListVO::getId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(baseIds)){
            return;
        }
        List<WorkSpaceRecentVO> querylatestWorkSpace = workSpaceMapper.querylatest(organizationId, projectId, baseIds);
        Map<Long, List<WorkSpaceRecentVO>> collect = querylatestWorkSpace.stream().collect(Collectors.groupingBy(WorkSpaceRecentVO::getBaseId));
        if (CollectionUtils.isEmpty(querylatestWorkSpace)) {
            return;
        }
        knowledgeBaseListVOList.forEach(e -> {
            for (Map.Entry<Long, List<WorkSpaceRecentVO>> map : collect.entrySet()) {
                if (e.getId().equals(map.getKey())) {
                    e.setWorkSpaceRecents(map.getValue());
                }
            }
        });
        //处理route
        if (!CollectionUtils.isEmpty(knowledgeBaseListVOList)) {
            WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
            workSpaceDTO.setProjectId(projectId);
            Map<Long, String> map = workSpaceMapper.select(workSpaceDTO).stream().collect(Collectors.toMap(WorkSpaceDTO::getId, WorkSpaceDTO::getName));
            List<Long> userIds = querylatestWorkSpace.stream().map(WorkSpaceRecentVO::getLastUpdatedBy).collect(Collectors.toList());
            Map<Long, UserDO> userDOMap = baseFeignClient.listUsersByIds(userIds.toArray(new Long[userIds.size()]), false).getBody().stream().collect(Collectors.toMap(UserDO::getId, x -> x));
            knowledgeBaseListVOList.forEach(e -> {
                if (!CollectionUtils.isEmpty(e.getWorkSpaceRecents())) {
                    e.getWorkSpaceRecents().forEach(work -> {
                        work.setLastUpdatedUser(userDOMap.get(work.getLastUpdatedBy()));
                        StringBuffer sb = new StringBuffer();
                        String[] split = work.getRoute().split("\\.");
                        List<String> route = Arrays.asList(split);
                        if (split.length > 1) {
                            for (String id : route) {
                                sb.append(map.get(Long.valueOf(id)));
                                sb.append("-");
                            }
                            sb.replace(sb.length() - 1, sb.length(), "");
                        } else {
                            sb = new StringBuffer(work.getTitle());
                        }
                        work.setUpdateworkSpace(sb.toString());
                    });
                }
            });
        }
    }

}
