package com.wenchanter.solr.platform.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;

import com.wenchanter.solr.platform.util.Config;

public class SolrFactory {

	/**
	 * 存放读写服务器
	 */
	private final static HashMap<String, SolrServer> serverMap = new HashMap<String, SolrServer>();
	/**
	 * 存放关于solr的配置
	 */
	public final static Map<String, Object> config = Config.getSolrConfig();

	static Logger logger = Logger.getLogger(SolrFactory.class);
	
	/**
	 * 获取写cloud server
	 *
	 * @param collection
	 * @return
	 */
	public static synchronized CloudSolrServer getUpdateCloudServer(
			String collection) {
		CloudSolrServer cloudSolrServer = (CloudSolrServer) serverMap
				.get(collection + "_update");
		if (cloudSolrServer == null) {
			try {
				Map<?, ?> collectionConfig = (Map<?, ?>) config.get(collection);
				cloudSolrServer = new CloudSolrServer(
						(String) collectionConfig.get("zkHost"));
				cloudSolrServer.setDefaultCollection(collection);
				cloudSolrServer.setZkClientTimeout(2000);
				cloudSolrServer.setZkConnectTimeout(1000);
				cloudSolrServer.connect();
				serverMap.put(collection + "_update", cloudSolrServer);
			} catch (Exception e) {
				logger.error("getUpdateCloudServer err...", e);
				e.printStackTrace();
			}
		}

		return cloudSolrServer;
	}

	/**
	 * 获取读lb server
	 *
	 * @param core
	 * @return
	 */
	public static synchronized SolrServer getLBServer(String collection) {
		LBHttpSolrServer lbServer = (LBHttpSolrServer) serverMap.get(collection
				+ "_lb");

		if (lbServer == null) {
			DefaultHttpClient httpClient = new DefaultHttpClient(
					new ThreadSafeClientConnManager());
			// UsernamePasswordCredentials creds = new
			// UsernamePasswordCredentials(
			// (String) config.get("user"), (String) config.get("pass"));
			// httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
			// creds);
			try {
				Map<?, ?> collectionConfig = (Map<?, ?>) config.get(collection);
				List<?> lbServers_obj = (List<?>)collectionConfig
						.get("lbServers");
				String[] lbServers_str = new String[lbServers_obj.size()];
				for (int i = 0; i < lbServers_obj.size(); i++) {
					lbServers_str[i] = (String) lbServers_obj.get(i);
				}
				lbServer = new LBHttpSolrServer(httpClient, lbServers_str);
				// 每30s检查一次故障节点的状态，如果恢复，则加入到负载均衡。
				lbServer.setAliveCheckInterval(1 * 1000);
				lbServer.setConnectionTimeout(60 * 1000);
				lbServer.setSoTimeout(60 * 1000);
				serverMap.put(collection + "_lb", lbServer);
			} catch (Exception e) {
				logger.error("getLBServer err...", e);
				e.printStackTrace();
			}
		}
		return lbServer;
	}
}
