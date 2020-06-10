package com.cjl.skill;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@MapperScan("com.cjl.skill.mapper")*/
public class ZkTest {
	@Test
	public void testZkCreate() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181",
                5000, 5000, retryPolicy);
        client.start();
        client.create().creatingParentContainersIfNeeded() //递归创建所需父节点
        .withMode(CreateMode.PERSISTENT) // 创建类型为持久节点
        .forPath("/testzk/nodeA", "init".getBytes()); // 目录及内容
	}
	
	@Test
	public void testZkWatcher() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181",
                5000, 5000, retryPolicy);
        client.start();
        /*Curator之nodeCache一次注册，N次监听*/
        //为节点添加watcher
        //监听数据节点的变更，会触发事件
        final NodeCache nodeCache = new NodeCache(client,"/testzk/nodeA");
        //buildInitial: 初始化的时候获取node的值并且缓存
        nodeCache.start(true);
        
        if(nodeCache.getCurrentData() != null){
            System.out.println("节点的初始化数据为："+new String(nodeCache.getCurrentData().getData()));
        }else{
            System.out.println("节点初始化数据为空。。。");
        }

        //注册监听器
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() throws Exception {
                //获取当前数据
                String data = new String(nodeCache.getCurrentData().getData());
                System.out.println("节点路径为："+nodeCache.getCurrentData().getPath()+" 数据: "+data);
            }
        });
        
        Thread.sleep(60000);
	}
	
	/*
	 * public static void main(String[] args) { String path = "/ss/11"; String id =
	 * path.substring(path.lastIndexOf("/")+1); System.out.println(id); }
	 */
}
