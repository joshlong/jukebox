/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.ftp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * FTPClientPool implementation based on a Queue. This implementation has a default pool size of 5, but this is
 * configurable with a constructor argument. This implementation pools released clients, but gives no guarantee to the
 * number of clients open at the same time.
 *
 * @author Iwein Fuld
 */
public class QueuedFTPClientPool implements FTPClientPool {

    private static final Log log = LogFactory.getLog(QueuedFTPClientPool.class);

    private static final int DEFAULT_POOL_SIZE = 5;

    private final Queue<FTPClient> pool;

    private final FTPClientFactory factory;

    public QueuedFTPClientPool(FTPClientFactory factory) {
        this(DEFAULT_POOL_SIZE, factory);
    }

    /**
     * @param maxPoolSize the maximum size of the pool
     */
    public QueuedFTPClientPool(int maxPoolSize, FTPClientFactory factory) {
        Assert.notNull(factory);
        this.factory = factory;
        pool = new ArrayBlockingQueue<FTPClient>(maxPoolSize);
    }

    /**
     * Returns an active FTPClient connected to the configured server. When no
     * clients are available in the queue a new client is created with the
     * factory.
     * <p>
     * It is possible that released clients are disconnected by the remote
     * server (@see {@link FTPClient#sendNoOp()}. In this case getClient is
     * called recursively to obtain a client that is still alive. For this
     * reason large pools are not recommended in poor networking conditions.
     */
    public FTPClient getClient() throws SocketException, IOException {
        FTPClient client = pool.poll();
        if (client == null) {
            client = factory.getClient();
        }
        return prepareClient(client);
    }

    /**
     * Prepares the client before it is returned through
     * <code>getClient()</code>. The default implementation will check the
     * connection using a noOp and replace the client with a new one if it
     * encounters a problem.
     * <p/>
     * In more exotic environments subclasses can override this method to
     * implement their own preparation strategy.
     * @param client the unprepared client
     * @return
     * @throws SocketException
     * @throws IOException
     */
    protected FTPClient prepareClient(FTPClient client) throws SocketException,
                                                               IOException {
        return isClientAlive(client) ? client : getClient();
    }

    private boolean isClientAlive(FTPClient client) {
        try {
            if (client.sendNoOp()) {
                return true;
            }
        }
        catch (IOException e) {
            log.warn("Client [" + client + "] discarded: ", e);
        }
        return false;
    }

    public void releaseClient(FTPClient client) {
        if (client != null && !pool.offer(client)) {
            try {
                client.disconnect();
            }
            catch (IOException e) {
                log.warn("Error disconnecting ftpclient", e);
            }
        }
    }
}
