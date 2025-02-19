package cn.bugstack.middleware.db.router.strategy.impl;

import cn.bugstack.middleware.db.router.DBContextHolder;
import cn.bugstack.middleware.db.router.DBRouterConfig;
import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.bugstack.middleware.db.router.util.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @description: 一致性哈希路由
 * @author: Chen
 * @date: 2025/02/18
 * @github: https://github.com/Cyper-alt
 */
public class DBRouterStrategyConsistentHash implements IDBRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DBRouterStrategyConsistentHash.class);

    private DBRouterConfig dbRouterConfig;

    private ConsistentHashSelector consistentHashSelector;

    public DBRouterStrategyConsistentHash(DBRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
        this.consistentHashSelector = new ConsistentHashSelector(dbRouterConfig.getDbCount(), dbRouterConfig.getTbCount());
    }

    @Override
    public void doRouter(String dbKeyAttr) {
        String dbKey = consistentHashSelector.get(dbKeyAttr);
        String[] dbKeyArr = dbKey.split("_");
        int dbIdx = Integer.parseInt(dbKeyArr[0]);
        int tbIdx = Integer.parseInt(dbKeyArr[1]);

        // 设置到 ThreadLocal；关于 ThreadLocal 的使用场景和源码介绍；https://bugstack.cn/md/java/interview/2020-09-23-%E9%9D%A2%E7%BB%8F%E6%89%8B%E5%86%8C%20%C2%B7%20%E7%AC%AC12%E7%AF%87%E3%80%8A%E9%9D%A2%E8%AF%95%E5%AE%98%EF%BC%8CThreadLocal%20%E4%BD%A0%E8%A6%81%E8%BF%99%E4%B9%88%E9%97%AE%EF%BC%8C%E6%88%91%E5%B0%B1%E6%8C%82%E4%BA%86%EF%BC%81%E3%80%8B.html
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
        logger.debug("数据库路由 dbIdx：{} tbIdx：{}",  dbIdx, tbIdx);
    }

    @Override
    public void setDBKey(int dbIdx) {
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
    }

    @Override
    public void setTBKey(int tbIdx) {
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear(){
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }


    class ConsistentHashSelector {

        class Node {
            private static final int VIRTUAL_NODE_NO_PER_NODE = 100;
            private final String name;
            private final List<Integer> virtualNodeHashes = new ArrayList<>(VIRTUAL_NODE_NO_PER_NODE);

            public Node(String name) {
                Objects.requireNonNull(name);
                this.name = name;
                initVirtualNodes();
            }


            private void initVirtualNodes() {
                String virtualNodeKey;
                for (int i = 1; i <= VIRTUAL_NODE_NO_PER_NODE; i++) {
                    virtualNodeKey = name + "#" + i;
                    virtualNodeHashes.add(HashUtils.hashcode(virtualNodeKey));
                }
            }

            public List<Integer> getVirtualNodeHashes() {
                return virtualNodeHashes;
            }

            public String getName() {
                return name;
            }
        }


        private final TreeMap<Integer, Node> hashRing = new TreeMap<>();

        public List<Node> nodeList = new ArrayList<>();


        ConsistentHashSelector(int DbCount, int TbCount) {
            for(int i = 0; i < DbCount; i++) {
                for(int j = 0; j < TbCount; j++) {
                    String name = new StringBuilder(String.valueOf(i)).append("_").append(j)
                            .toString();
                    addNode(name);
                }
            }
        }
        /**
         * 增加节点
         * 每增加一个节点，就会在闭环上增加给定虚拟节点
         * 例如虚拟节点数是2，则每调用此方法一次，增加两个虚拟节点，这两个节点指向同一Node
         * @param name
         */
        public void addNode(String name) {
            Objects.requireNonNull(name);
            Node node = new Node(name);
            nodeList.add(node);
            for (Integer virtualNodeHash : node.getVirtualNodeHashes()) {
                hashRing.put(virtualNodeHash, node);
            }
        }

        /**
         * 移除节点
         * @param node
         */
        public void removeNode(Node node){
            nodeList.remove(node);
        }

        /**
         * 获取节点名称
         * 先找到对应的虚拟节点，然后映射到物理节点
         * @param key
         * @return
         */
        public String get(Object key) {
            Node node = findMatchNode(key);
            return node.name;
        }

        /**
         *  获得一个最近的顺时针节点
         * @param key 为给定键取Hash，取得顺时针方向上最近的一个虚拟节点对应的实际节点
         *      * @return 节点对象
         * @return
         */
        private Node findMatchNode(Object key) {
            Map.Entry<Integer, Node> entry = hashRing.ceilingEntry(HashUtils.hashcode(key));
            if (entry == null) {
                entry = hashRing.firstEntry();
            }
            return entry.getValue();
        }
    }

}
