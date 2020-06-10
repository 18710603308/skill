package com.cjl.skill.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cjl.skill.cache.LocalCache;
import com.cjl.skill.util.ConstantPrefixUtil;

@Configuration
public class ZookeeperConfig {
	
	@Bean
	public CuratorFramework zookeeperClient() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181",
                5000, 5000, retryPolicy);
        client.start();
        
        /* Curator之nodeCache一次注册，N次监听 */
		// 为节点添加watcher
		// 监听数据节点的变更，会触发事件
		TreeCache treeCache = new TreeCache(client, ConstantPrefixUtil.ZK_SOLD_OUT_PRODUCT_ROOT_PATH);
		// buildInitial: 初始化的时候获取node的值并且缓存
		try {
			treeCache.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		treeCache.getListenable().addListener(new TreeCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
				ChildData eventData = event.getData();
				if(eventData==null) {
					return;
				}
				String full = eventData.getPath();
				String id = full.substring(full.lastIndexOf("/")+1);
                switch (event.getType()) {
                    case NODE_ADDED:
                    	LocalCache.SOLD_OUT_FLAGS.put(id, true);
                    	System.out.println("node add :     "+"full path: "+full+" id : "+id);
                        break;
                    case NODE_REMOVED:
                    	LocalCache.SOLD_OUT_FLAGS.remove(id);
                    	System.out.println("node removed :     "+"full path: "+full+" id : "+id);
                        break;
                    default:
                        break;
                }
			}
		});
        return client;
	}
}
