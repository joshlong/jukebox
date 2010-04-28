package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class DataTupleUnpacker implements InitializingBean {
    private Pattern pattern;
    private String unpackingRegex;

    @Required
    public void setUnpackingRegex(final String unpackingRegex) {
        this.unpackingRegex = StringUtils.defaultString(unpackingRegex).trim();
    }

    public static void main(String[] args) throws Throwable {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("musicbrainz-replication.xml");
        DataTupleUnpacker dataTupleUnpacker = applicationContext.getBean(DataTupleUnpacker.class);

        String[] examples = {"\"\"id\"='940342' \"gid\"='09d09e97-20b4-4a58-b04b-fbcee36149d0' \"name\"='Street Vibes 6' \"page\"='693737685' \"artist\"='1' \"type\"='4' \"modpending\"='0'\""};

        for (String e : examples) {
            System.out.println(dataTupleUnpacker.unpack(e));
        }
    }

    public Map<String, String> unpack(String data) throws Throwable {
        Matcher matcher = this.pattern.matcher(data);
        Map<String, String> kvs = new java.util.HashMap<String, String>();

        while (matcher.find()) {
            String k = StringUtils.defaultString(matcher.group(2));
            String v = StringUtils.defaultString(matcher.group(3));

            if (v.length() > 2) {
                if (v.startsWith("'")) {
                    v = v.substring(1);
                }

                if (v.endsWith("'")) {
                    v = v.substring(0, v.length() - 1);
                }
            }
            kvs.put(k, v);
        }

        return kvs;
    }

    public void afterPropertiesSet() throws Exception {
        this.pattern = Pattern.compile(this.unpackingRegex);
    }
}
