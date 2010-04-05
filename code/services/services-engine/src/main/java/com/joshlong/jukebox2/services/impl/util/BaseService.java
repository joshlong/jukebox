package com.joshlong.jukebox2.services.impl.util;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.lang.StringUtils;
import org.jbpm.JbpmConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springmodules.workflow.jbpm31.JbpmTemplate;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BaseService extends HibernateDaoSupport implements ApplicationContextAware {

	protected ApplicationContext applicationContext;
	protected JbpmConfiguration jbpmConfiguration;
	protected JbpmTemplate jbpmTemplate;
	protected DataSource dataSource;
	protected JdbcTemplate jdbcTemplate;

	protected <T> T get(Class<T> t, Serializable s) {
		return (T) getHibernateTemplate().get(t, s);
	}

	protected String seoFriendlyUrl(String urlToNormalizeForSeoPurposes) {
		String urlLowerCasedAndTrimmed = StringUtils.defaultString(urlToNormalizeForSeoPurposes).toLowerCase().trim();
		StringBuilder arr = new StringBuilder();
		for (char c : urlLowerCasedAndTrimmed.toCharArray()) {
			if (Character.isWhitespace(c) || Character.isISOControl(c))
				arr.append(' ');
			if (Character.isLetterOrDigit(c))
				arr.append(c);
		}

		List<String> arrf = new ArrayList<String>();
		for (String c : arr.toString().split(" "))
			if (!StringUtils.isEmpty(c))
				arrf.add(c);

		return StringUtils.join(arrf.iterator(), "_").toLowerCase().trim();
	}

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.applicationContext = context;
		getHibernateTemplate().setCacheQueries(true);
		getHibernateTemplate().setQueryCacheRegion("org.hibernate.cache.StandardQueryCache");
	}

	/**/
	/**
	 * we use the double metaphone and not Soundex because Soundex is mapped to
	 * a given language - thus, a title with a tilde or accent grave in Spanish
	 * or French won't encode
	 * 
	 * @param input
	 *            any string
	 * @return r
	 */
	public String soundBasedCodeForString(String input) {
		String nstrng = "";
		input = StringUtils.defaultString(input);

		for (char c : input.toCharArray())
			if (Character.isLetterOrDigit(c))
				nstrng += c;

		return StringUtils.defaultString(new DoubleMetaphone().encode(nstrng));
	}


	/**
	 * http://www.ajaxonomy.com/2008/tutorials/making-the-most-of-java-5-
	 * exploring-generics
	 */
	protected <T> T firstOrNull(Collection c) {
		if (c == null || c.size() == 0) {
			return null;
		}
		return (T) c.iterator().next();
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public JbpmConfiguration getJbpmConfiguration() {
		return jbpmConfiguration;
	}

	public void setJbpmConfiguration(JbpmConfiguration jbpmConfiguration) {
		this.jbpmConfiguration = jbpmConfiguration;
	}

	public JbpmTemplate getJbpmTemplate() {
		return jbpmTemplate;
	}

	public void setJbpmTemplate(JbpmTemplate jbpmTemplate) {
		this.jbpmTemplate = jbpmTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

    @Override
    protected void initDao() throws Exception {

    }
}
