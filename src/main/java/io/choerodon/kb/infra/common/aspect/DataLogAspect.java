package io.choerodon.kb.infra.common.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageCommentRepository;
import io.choerodon.kb.domain.kb.repository.PageLogRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.annotation.DataLog;
import io.choerodon.kb.infra.common.utils.TypeUtil;
import io.choerodon.kb.infra.dataobject.PageCommentDO;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.kb.infra.dataobject.PageLogDO;

/**
 * Created by Zenger on 2019/5/16.
 */
@Aspect
@Component
@Transactional(rollbackFor = Exception.class)
public class DataLogAspect {

    private static final String ERROR_UPDATE = "error.LogDataAspect.update";
    private static final String ERROR_METHOD_EXECUTE = "error.dataLog.methodExecute";

    @Autowired
    private PageLogRepository pageLogRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private PageCommentRepository pageCommentRepository;

    /**
     * 定义拦截规则：拦截Spring管理的后缀为RepositoryImpl的bean中带有@DataLog注解的方法。
     */
    @Pointcut("bean(*RepositoryImpl) && @annotation(io.choerodon.kb.infra.common.annotation.DataLog)")
    public void updateMethodPointcut() {
        throw new UnsupportedOperationException();
    }

    @Around("updateMethodPointcut()")
    public Object interceptor(ProceedingJoinPoint pjp) {
        Object result = null;
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        //获取被拦截的方法
        Method method = signature.getMethod();
        DataLog dataLog = method.getAnnotation(DataLog.class);
        //获取被拦截的方法名
        Object[] args = pjp.getArgs();
        if (dataLog != null && args != null) {
            if (dataLog.single()) {
                switch (dataLog.type()) {
                    case BaseStage.PAGE_CREATE:
                        result = handleCreatePageDataLog(pjp);
                        break;
                    case BaseStage.PAGE_UPDATE:
                        handleUpdatePageDataLog(args);
                        break;
                    case BaseStage.PAGE_DELETE:
                        handleDeletePageDataLog(args);
                        break;
                    case BaseStage.COMMENT_CREATE:
                        result = handleCreateCommentDataLog(args, pjp);
                        break;
                    case BaseStage.COMMENT_UPDATE:
                        handleUpdateCommentDataLog(args);
                        break;
                    case BaseStage.COMMENT_DELETE:
                        handleDeleteCommentDataLog(args);
                        break;
                    default:
                        break;
                }
            }
        } else {
            throw new CommonException(ERROR_UPDATE);
        }
        try {
            // 一切正常的情况下，继续执行被拦截的方法
            if (result == null) {
                result = pjp.proceed();
            }
        } catch (Throwable e) {
            throw new CommonException(ERROR_METHOD_EXECUTE, e);
        }
        return result;
    }

    private void handleDeleteCommentDataLog(Object[] args) {
        Long id = null;
        for (Object arg : args) {
            if (arg instanceof Long) {
                id = (Long) arg;
            }
        }
        PageCommentDO pageCommentDO = pageCommentRepository.selectById(id);
        if (pageCommentDO != null) {
            createDataLog(pageCommentDO.getPageId(),
                    BaseStage.DELETE_OPERATION,
                    BaseStage.COMMENT,
                    pageCommentDO.getComment(),
                    null,
                    TypeUtil.objToString(pageCommentDO.getId()),
                    null);
        }
    }

    private void handleUpdateCommentDataLog(Object[] args) {
        PageCommentDO pageCommentDO = null;
        for (Object arg : args) {
            if (arg instanceof PageCommentDO) {
                pageCommentDO = (PageCommentDO) arg;
            }
        }
        if (pageCommentDO.getId() != null) {
            PageCommentDO newPageComment = pageCommentRepository.selectById(pageCommentDO.getId());
            createDataLog(pageCommentDO.getPageId(),
                    BaseStage.UPDATE_OPERATION,
                    BaseStage.COMMENT,
                    pageCommentDO.getComment(),
                    newPageComment.getComment(),
                    TypeUtil.objToString(pageCommentDO.getId()),
                    TypeUtil.objToString(pageCommentDO.getId()));
        }
    }

    private Object handleCreateCommentDataLog(Object[] args, ProceedingJoinPoint pjp) {
        PageCommentDO pageCommentDO = null;
        Object result = null;
        for (Object arg : args) {
            if (arg instanceof PageCommentDO) {
                pageCommentDO = (PageCommentDO) arg;
            }
        }
        if (pageCommentDO != null) {
            try {
                result = pjp.proceed();
                pageCommentDO = (PageCommentDO) result;
                createDataLog(pageCommentDO.getPageId(),
                        BaseStage.CREATE_OPERATION,
                        BaseStage.COMMENT,
                        null,
                        pageCommentDO.getComment(),
                        null,
                        pageCommentDO.getId().toString());
            } catch (Throwable e) {
                throw new CommonException(ERROR_METHOD_EXECUTE, e);
            }
        }
        return result;
    }

    private void handleDeletePageDataLog(Object[] args) {
        Long pageId = null;
        for (Object arg : args) {
            if (arg instanceof Long) {
                pageId = (Long) arg;
            }
        }
        PageDO page = pageRepository.selectById(pageId);
        if (page != null) {
            createDataLog(page.getId(),
                    BaseStage.DELETE_OPERATION,
                    BaseStage.PAGE,
                    page.getTitle(),
                    null,
                    page.getId().toString(),
                    null);
        }
    }

    private void handleUpdatePageDataLog(Object[] args) {
        PageDO pageDO = null;
        Boolean flag = false;
        for (Object arg : args) {
            if (arg instanceof PageDO) {
                pageDO = (PageDO) arg;
            }
            if (arg instanceof Boolean) {
                flag = (Boolean) arg;
            }
        }
        if (pageDO.getId() != null) {
            PageDO page = pageRepository.selectById(pageDO.getId());
            String title = "";
            if (pageDO.getTitle() == null) {
                title = page.getTitle();
            } else {
                title = pageDO.getTitle();
            }
            if (flag) {
                createDataLog(pageDO.getId(),
                        BaseStage.UPDATE_OPERATION,
                        BaseStage.PAGE,
                        page.getTitle(),
                        title,
                        page.getId().toString(),
                        pageDO.getId().toString());
            }
        }
    }

    private Object handleCreatePageDataLog(ProceedingJoinPoint pjp) {
        Object result;
        try {
            result = pjp.proceed();
            PageDO pageDO = (PageDO) result;
            if (pageDO.getId() != null) {
                createDataLog(pageDO.getId(),
                        BaseStage.CREATE_OPERATION,
                        BaseStage.PAGE,
                        null,
                        pageDO.getTitle(),
                        null,
                        pageDO.getId().toString());
            }
        } catch (Throwable e) {
            throw new CommonException(ERROR_METHOD_EXECUTE, e);
        }
        return result;
    }

    private void createDataLog(Long pageId, String operation, String field, String oldString,
                               String newString, String oldValue, String newValue) {
        PageLogDO pageLogDO = new PageLogDO();
        pageLogDO.setPageId(pageId);
        pageLogDO.setOperation(operation);
        pageLogDO.setField(field);
        pageLogDO.setOldString(oldString);
        pageLogDO.setNewString(newString);
        pageLogDO.setOldValue(oldValue);
        pageLogDO.setNewValue(newValue);
        pageLogRepository.insert(pageLogDO);
    }
}
