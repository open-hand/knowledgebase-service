package io.choerodon.kb.app.service.assembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.github.pagehelper.PageInfo;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.WorkSpaceRecentVO;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.PageUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

    public void docheage(List<WorkSpaceRecentVO> workSpaceRecentVOS, Long projectId) {
        if (!CollectionUtils.isEmpty(workSpaceRecentVOS)) {
            WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
            workSpaceDTO.setProjectId(projectId);
            Map<Long, String> map = workSpaceMapper.select(workSpaceDTO).stream().collect(Collectors.toMap(WorkSpaceDTO::getId, WorkSpaceDTO::getName));
            List<Long> lastUpdatedBys = new ArrayList<>();
            workSpaceRecentVOS.forEach(work -> {
                Long lastUpdatedBy = work.getLastUpdatedBy();
                lastUpdatedBys.add(lastUpdatedBy);
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
            Map<Long, UserDO> userDOMap = baseFeignClient.listUsersByIds(lastUpdatedBys.toArray(new Long[lastUpdatedBys.size()]), false).getBody().stream().collect(Collectors.toMap(UserDO::getId, x -> x));
            workSpaceRecentVOS.forEach(e -> e.setLastUpdatedUser(userDOMap.get(e.getLastUpdatedBy())));
        }
    }

}
