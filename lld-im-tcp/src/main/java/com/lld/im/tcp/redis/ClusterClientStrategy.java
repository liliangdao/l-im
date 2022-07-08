package com.lld.im.tcp.redis;

import com.lld.im.codec.config.BootstrapConfig;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * redisson集群客户端策略
 *
 * @author zhaopeng
 */
public class ClusterClientStrategy implements ClientStrategy {

    @Override
    public RedissonClient getRedissonClient(BootstrapConfig.RedisConfig redisConfig) {
        Config config = new Config();
        String[] nodes = redisConfig.getCluster().getNodes().split(",");
        List<String> newNodes = new ArrayList(nodes.length);
        Arrays.stream(nodes).forEach((index) -> newNodes.add(index.startsWith("redis://") ? index : "redis://" + index));

        ClusterServersConfig serverConfig = config.useClusterServers()
                .addNodeAddress(newNodes.toArray(new String[0]))
                .setTimeout(redisConfig.getTimeout())
                .setConnectTimeout(redisConfig.getPoolConnTimeout())
                .setScanInterval(redisConfig.getCluster().getScanInterval())
                .setReadMode(getReadMode(redisConfig.getCluster().getReadMode()))
                .setRetryAttempts(redisConfig.getCluster().getRetryAttempts())
                .setMasterConnectionPoolSize(redisConfig.getCluster().getMasterConnectionPoolSize())
                .setSlaveConnectionPoolSize(redisConfig.getCluster().getSlaveConnectionPoolSize())
                .setRetryInterval(redisConfig.getCluster().getRetryInterval());

        if (StringUtils.isNotBlank(redisConfig.getPassword())) {
            serverConfig.setPassword(redisConfig.getPassword());
        }
        return Redisson.create(config);
    }

    /**
     * 读取模式
     *
     * @param readMode 模式
     * @return {@link ReadMode}
     */
    private ReadMode getReadMode(String readMode) {
        if (StringUtils.isBlank(readMode)) {
            return ReadMode.SLAVE;
        }

        if (readMode.equals(RedisReadMode.SLAVE)) {
            return ReadMode.SLAVE;
        } else if (readMode.equals(RedisReadMode.MASTER)) {
            return ReadMode.MASTER;
        } else if (readMode.equals(RedisReadMode.MASTER_SLAVE)) {
            return ReadMode.MASTER_SLAVE;
        }
        return ReadMode.SLAVE;
    }


}
