package qeorm;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by ashen on 2017-2-6.
 */
@Aspect
public class TransactionalManager {
    @Autowired
    SqlSession sqlSession;
    private Logger logger = LoggerFactory.getLogger(TransactionalManager.class);

    @Around("@within(qeorm.annotation.Transactional)")
    public Object transactionalKlass(ProceedingJoinPoint point) throws Throwable {
        return transactional(point);
    }

    @Around("@annotation(qeorm.annotation.Transactional)")
    public Object transactional(ProceedingJoinPoint point) throws Throwable {
        String key=point.getTarget().getClass().getCanonicalName() + ":" + point.getSignature().getName();
        logger.info("开始事务："+key);
        try {
            sqlSession.beginTransaction();
            Object ret = point.proceed();
            sqlSession.commit();
            logger.info("提交事务："+key);
            return ret;
        } catch (Throwable throwable) {
            sqlSession.rollback();
            logger.info("回滚事务："+key);
            throw throwable;
        }
    }

    @Around("@within(qeorm.annotation.Rollback)")
    public Object rollbackKlass(ProceedingJoinPoint point) throws Throwable {
        return rollback(point);
    }

    @Around("@annotation(qeorm.annotation.Rollback)")
    public Object rollback(ProceedingJoinPoint point) throws Throwable {
        String key=point.getTarget().getClass().getCanonicalName() + ":" + point.getSignature().getName();
        logger.info("开始事务："+key);
        try {
            sqlSession.beginTransaction();
            Object ret = point.proceed();
            return ret;
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            sqlSession.rollback();
            logger.info("回滚事务："+key);
        }
    }
}
