package io.github.kamarias.lock;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/4 9:38
 */
public class ZooKeeperDistributedLock extends AbstractDistributedLock {

    private final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperDistributedLock.class);

    private final ZooKeeper zooKeeper;

    private static final String ROOT_PATH = "/distributedLock";

    private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<>();

    private static final ThreadLocal<String> LOCK_PATH = new ThreadLocal<>();

    public ZooKeeperDistributedLock(ZooKeeper zooKeeper) {
        super();
        // 创建分布式锁根节点
        try {
            if (zooKeeper.exists(ROOT_PATH, false) == null){
                zooKeeper.create(ROOT_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("set zookeeper root node occurred an exception", e);
        }
        this.zooKeeper = zooKeeper;
    }

    @Override
    public boolean lock(String key, long expire, int retryTimes, long sleepMillis) {
        boolean result = setZookeeperLock(key);
        // 如果获取锁失败,按照传入的重试次数进行重试
        while ((!result) && retryTimes-- > 0) {
            try {
                LOGGER.debug("lock failed, retrying..." + retryTimes);
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                LOGGER.warn("thread={} is interrupt", Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                return false;
            }
            result = setZookeeperLock(key);
        }
        return result;
    }

    /**
     * 释放
     * @param key redis key
     * @return 释放锁结果
     */
    @Override
    public boolean releaseLock(String key) {
        try {
            THREAD_LOCAL.set(THREAD_LOCAL.get() - 1);
            if (THREAD_LOCAL.get() == 0) {
                this.zooKeeper.delete(LOCK_PATH.get(), 0);
            }
            return true;
        } catch (InterruptedException | KeeperException e) {
            LOGGER.error("release zookeeper lock occurred an exception", e);
            return false;
        }finally {
            THREAD_LOCAL.remove();
            LOCK_PATH.remove();
        }
    }

    /**
     * 加锁逻辑
     * @param key 锁key
     * @return  加锁结果
     */
    private boolean setZookeeperLock(String key) {
        // 新锁需要使用key来设置跟节点
        try {
            if (THREAD_LOCAL.get() == null || THREAD_LOCAL.get() == 0) {
                LOCK_PATH.set(zooKeeper.create(ROOT_PATH + "/" + key + "-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("set zookeeper root node occurred an exception", e);
        }


        Integer flag = THREAD_LOCAL.get();
        if (flag != null && flag > 0) {
            THREAD_LOCAL.set(flag + 1);
            return true;
        }
        try {
            String preNode = getPreNode(LOCK_PATH.get());
            // 如果该节点没有前一个节点，说明该节点时最小节点，放行执行业务逻辑
            if (StringUtils.isEmpty(preNode)) {
                THREAD_LOCAL.set(1);
                return true;
            }
            CountDownLatch countDownLatch = new CountDownLatch(1);
            if (this.zooKeeper.exists(ROOT_PATH + preNode, event -> countDownLatch.countDown()) == null) {
                THREAD_LOCAL.set(1);
                return true;
            }
            countDownLatch.await();
            THREAD_LOCAL.set(1);
            return true;
        } catch (Exception e) {
            LOGGER.error("set zookeeper root node occurred an exception", e);
        }
        return false;
    }

    /**
     * 获取zookeeper根路径下的最小节点
     *
     * @param lockPath zookeeper锁的根路径
     * @return 最小节点路径
     */
    @SuppressWarnings("all")
    private String getPreNode(String lockPath) {
        try {
            // 获取当前节点的序列化号
            Long curSerial = Long.valueOf(StringUtils.substringAfterLast(lockPath, "-"));
            // 获取根路径下的所有序列化子节点
            List<String> nodes = zooKeeper.getChildren(ROOT_PATH, false);
            // 判空
            if (CollectionUtils.isEmpty(nodes)) {
                return null;
            }
            // 获取前一个节点
            Long flag = 0L;
            String preNode = null;
            for (String node : nodes) {
                // 获取每个节点的序列化号
                Long serial = Long.valueOf(StringUtils.substringAfterLast(node, "-"));
                if (serial < curSerial && serial > flag) {
                    flag = serial;
                    preNode = node;
                }
            }
            return preNode;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("get zookeeper root node least occurred an exception", e);
        }
        return null;
    }

}
