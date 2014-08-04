package com.wenchanter.solr.platform.search.query;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.wenchanter.solr.platform.factory.SolrFactory;
import com.wenchanter.solr.platform.util.DateUtils;

public class IndexSearcher {

	static Logger logger = Logger.getLogger(IndexSearcher.class);
	
	public static BaseResponse search(BaseQuery query, String collection) throws Exception {
		try {
			QueryResponse response = SolrFactory.getLBServer(collection).query(query.getSolrQuery());
			return new BaseResponse(response);
		} catch (Exception e) {
			String Qstr = query.getSolrQuery().getQuery().replace("&&", "AND").replace("||", "OR");
			String alarm = DateUtils.getNowStr() + "\t" + Qstr + "\t" + e.getMessage();
			logger.error("commit err...", e);
			System.err.println(alarm);
			throw e;
		}
	}

}
