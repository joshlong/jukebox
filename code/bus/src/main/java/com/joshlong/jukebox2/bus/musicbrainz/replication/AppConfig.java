package com.joshlong.jukebox2.bus.musicbrainz.replication;

import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.ftp.*;
import org.springframework.scheduling.TaskScheduler;

import java.io.File;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@Configuration
public class AppConfig {
    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private DefaultFTPClientFactory ftpClientFactory ;

    @Bean
    public Resource inboundReplicationDirectory() {
        File homeDir = new File(SystemUtils.getUserHome(), "bus/musicbrainz_replication/new/");

        if (!homeDir.exists()) {
            homeDir.mkdirs();
        }

        FileSystemResource homeDirResource = new FileSystemResource(homeDir);
        return homeDirResource;
    }
/*

    @Bean
    FTPClientFactory ftpClientFactory() throws Throwable {
        DefaultFTPClientFactory defaultFTPClientFactory = new DefaultFTPClientFactory();
        defaultFTPClientFactory.setHost();
        return defaultFTPClientFactory ;
    }
*/

    @Bean
    public FTPClientPool ftpClientPool() throws Throwable {
        return new QueuedFTPClientPool(ftpClientFactory );
    }

    @Bean
    public FtpFileSource ftpFileSource() throws Throwable {

        FtpFileSource ftpFileSource = new FtpFileSource();
        ftpFileSource.setClientPool(ftpClientPool());
        ftpFileSource.setLocalWorkingDirectory(inboundReplicationDirectory());
        ftpFileSource.setTaskScheduler(taskScheduler);

        return ftpFileSource;
    }
/*
    @Bean
    public FtpInboundSynchronizer ftpInboundSynchronizer()
        throws Throwable {
        DefaultFTPClientFactory defaultFTPClientFactory = new DefaultFTPClientFactory();
        QueuedFTPClientPool queuedFTPClientPool = new QueuedFTPClientPool(defaultFTPClientFactory);
        FtpInboundSynchronizer ftpInboundSynchronizer = new FtpInboundSynchronizer();
        ftpInboundSynchronizer.setClientPool(queuedFTPClientPool);

        return ftpInboundSynchronizer;
    }*/
}
