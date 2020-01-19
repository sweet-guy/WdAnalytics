package com.wdcloud.analytics.greendao.gen;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.wdcloud.wdanalytics.bean.CrashAnalyticsBean;
import com.wdcloud.wdanalytics.bean.EventBean;

import com.wdcloud.analytics.greendao.gen.CrashAnalyticsBeanDao;
import com.wdcloud.analytics.greendao.gen.EventBeanDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig crashAnalyticsBeanDaoConfig;
    private final DaoConfig eventBeanDaoConfig;

    private final CrashAnalyticsBeanDao crashAnalyticsBeanDao;
    private final EventBeanDao eventBeanDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        crashAnalyticsBeanDaoConfig = daoConfigMap.get(CrashAnalyticsBeanDao.class).clone();
        crashAnalyticsBeanDaoConfig.initIdentityScope(type);

        eventBeanDaoConfig = daoConfigMap.get(EventBeanDao.class).clone();
        eventBeanDaoConfig.initIdentityScope(type);

        crashAnalyticsBeanDao = new CrashAnalyticsBeanDao(crashAnalyticsBeanDaoConfig, this);
        eventBeanDao = new EventBeanDao(eventBeanDaoConfig, this);

        registerDao(CrashAnalyticsBean.class, crashAnalyticsBeanDao);
        registerDao(EventBean.class, eventBeanDao);
    }
    
    public void clear() {
        crashAnalyticsBeanDaoConfig.clearIdentityScope();
        eventBeanDaoConfig.clearIdentityScope();
    }

    public CrashAnalyticsBeanDao getCrashAnalyticsBeanDao() {
        return crashAnalyticsBeanDao;
    }

    public EventBeanDao getEventBeanDao() {
        return eventBeanDao;
    }

}
