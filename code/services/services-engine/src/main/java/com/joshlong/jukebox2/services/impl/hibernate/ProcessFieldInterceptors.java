package com.joshlong.jukebox2.services.impl.hibernate;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * @author jlong Code designed to guarantee that certain fields are always updated/saved/etc
 */
public class ProcessFieldInterceptors extends EmptyInterceptor implements ApplicationContextAware {

    // http://java.dzone.com/articles/using-a-hibernate-interceptor-*/

    @Override
    public boolean onFlushDirty(Object o,
                                Serializable serializable,
                                Object[] objects,
                                Object[] objects1,
                                String[] strings,
                                Type[] types) {
        setValue(objects, strings, "dateModified", new Date());
        return true;
    }

    @Override
    public boolean onSave(Object o, Serializable serializable, Object[] objects, String[] strings, Type[] types) {
        Date now = new Date();
        setValue(objects, strings, "dateCreated", now);
        setValue(objects, strings, "dateModified", now);
        return true;
    }

    // btw, its worth mentioning that Intellij has the industry wide best
    // support for groovy

    private void setValue(Object[] state, String[] props, String propertyToSet, Object val) {
        int indxOfPropertyNameInPropertyNamesArr = Arrays.asList(props).indexOf(propertyToSet);
        if (indxOfPropertyNameInPropertyNamesArr >= 0) {
            state[indxOfPropertyNameInPropertyNamesArr] = val;
        }

    }

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
