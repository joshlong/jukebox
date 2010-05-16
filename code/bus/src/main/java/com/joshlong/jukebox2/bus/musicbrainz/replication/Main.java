package com.joshlong.jukebox2.bus.musicbrainz.replication;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * This class serves only to bootstrap the Spring Integration bus
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class Main {
    public static void main (String [] args) throws Throwable {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext( "bus.xml") ;
        classPathXmlApplicationContext.start();
    }
}
