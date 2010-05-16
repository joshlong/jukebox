package com.joshlong.jukebox2.musicbrainz.replication.bus;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * This class serves only to bootstrap the Spring Integration bus (and batch jobs)
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class Main {
    public static void main (String [] args) throws Throwable {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext( "bus.xml") ;
        classPathXmlApplicationContext.start();
    }
}
