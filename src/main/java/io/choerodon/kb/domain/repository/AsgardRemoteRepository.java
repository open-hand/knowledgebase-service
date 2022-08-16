package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.infra.feign.vo.SagaInstanceDetails;

/**
 *  Asgard服务远程资源库
 * @author gaokuo.dai@zknow.com 2022-08-15
 */
public abstract class AsgardRemoteRepository {
    public abstract List<SagaInstanceDetails> queryByRefTypeAndRefIds(String refType, List<String> refIds, String sagaCode);
}
