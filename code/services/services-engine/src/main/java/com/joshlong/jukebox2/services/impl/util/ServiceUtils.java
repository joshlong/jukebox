package com.joshlong.jukebox2.services.impl.util;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class ServiceUtils implements InitializingBean {
    private HibernateTemplate hibernateTemplate;

    @Required
    public void setHibernateTemplate(final HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    public void afterPropertiesSet() throws Exception {
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setQueryCacheRegion("org.hibernate.cache.StandardQueryCache");
    }

    protected <T> T get(Class<T> t, Serializable s) {
        return (T) hibernateTemplate.get(t, s);
    }

    protected String generateBookmarkableFriendlyUrl(String urlToNormalizeForSeoPurposes) {
        String urlLowerCasedAndTrimmed = StringUtils.defaultString(urlToNormalizeForSeoPurposes).toLowerCase().trim();
        StringBuilder arr = new StringBuilder();

        for (char c : urlLowerCasedAndTrimmed.toCharArray()) {
            if (Character.isWhitespace(c) || Character.isISOControl(c)) {
                arr.append(' ');
            }

            if (Character.isLetterOrDigit(c)) {
                arr.append(c);
            }
        }

        List<String> arrf = new ArrayList<String>();

        for (String c : arr.toString().split(" ")) {
            if (!StringUtils.isEmpty(c)) {
                arrf.add(c);
            }
        }

        return StringUtils.join(arrf.iterator(), "_").toLowerCase().trim();
    }

    /**
     * we use the double metaphone and not Soundex because Soundex is mapped to a given language - thus, a title with a
     * tilde or accent grave in Spanish or French won't encode
     *
     * @param input any string
     *
     * @return r
     */
    public String soundBasedCodeForString(String input) {
        StringBuilder sb = new StringBuilder();
        String rinput = StringUtils.defaultString(input);

        for (char c : rinput.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }

        return StringUtils.defaultString(new DoubleMetaphone().encode(sb.toString()));
    }

    @SuppressWarnings("unchecked")
    protected <T> T firstOrNull(Collection c) {
        if ((c == null) || (c.size() == 0)) {
            return null;
        }

        return (T) c.iterator().next();
    }
}
