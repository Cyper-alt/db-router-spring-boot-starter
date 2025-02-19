package cn.bugstack.middleware.db.router;

import cn.bugstack.middleware.db.router.annotation.DB;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: 数据路由切面，通过自定义注解的方式，拦截被切面的方法，进行数据库路由
 * @author: 小傅哥，微信：fustack
 * @date: 2021/9/22
 * @github: https://github.com/fuzhengwei
 * @Copyright: 公众号：bugstack虫洞栈 | 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Aspect
public class DBJoinPoint {

    private Logger logger = LoggerFactory.getLogger(DBJoinPoint.class);

    @Around("@within(db)")
    public Object db(ProceedingJoinPoint jp, DB db) throws Throwable {
        String dbName = db.key();
        if (StringUtils.isBlank(dbName)) {
            throw new RuntimeException("annotation DS key is null！");
        }
        // 路由属性
        DBContextHolder.setDBName(dbName);
        // 返回结果
        try {
            return jp.proceed();
        } finally {
            DBContextHolder.clearDBName();
        }
    }

}
