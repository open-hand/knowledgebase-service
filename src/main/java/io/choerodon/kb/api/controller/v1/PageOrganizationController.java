package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.PageInfoDTO;
import io.choerodon.kb.app.service.PageService;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/page")
public class PageOrganizationController {

    private PageService pageService;

    public PageOrganizationController(PageService pageService) {
        this.pageService = pageService;
    }

    /**
     * 校验是否为页面的创建者
     *
     * @param organizationId 组织id
     * @param id             页面id
     * @return Boolean
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "校验是否为页面的创建者")
    @GetMapping(value = "/{id}")
    public ResponseEntity<Boolean> checkPageCreate(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面ID", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(pageService.checkPageCreate(id), HttpStatus.OK);
    }

    /**
     * 查询文章的附件评论信息
     *
     * @param organizationId 组织id
     * @param id             页面ID
     * @return PageInfoDTO
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "查询文章的附件评论信息")
    @GetMapping(value = "/{id}/info")
    public ResponseEntity<PageInfoDTO> queryPageInfo(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面ID", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(pageService.queryPageInfo(id), HttpStatus.OK);
    }
}
