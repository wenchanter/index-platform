package com.wenchanter.solr.platform.index.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.UpdateParams;

import com.wenchanter.solr.platform.factory.SolrFactory;
import com.wenchanter.solr.platform.index.service.IndexerService;

public class IndexerServiceImpl implements IndexerService {

	private String collection = "unknown";

	public IndexerServiceImpl(String collection) {
		this.collection = collection;
	}

	Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * 插入一条记录
	 */
	@Override
	public int addDocument(SolrInputDocument doc) throws Exception {
		try {
			UpdateResponse res = SolrFactory.getUpdateCloudServer(collection).add(doc);
			int status = res.getStatus();
			return status;
		} catch (Exception e) {
			logger.error("add Document error", e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

	/**
	 * 插入一组记录
	 */
	@Override
	public int addDocuments(List<SolrInputDocument> docList) throws Exception {
		try {
			UpdateResponse res = SolrFactory.getUpdateCloudServer(collection).add(docList);
			int status = res.getStatus();
			return status;
		} catch (Exception e) {
			logger.error("add Documents err...", e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

	/**
	 * 添加记录并立即提交
	 */
	@Override
	public int addDocumentAndCommit(SolrInputDocument doc) throws Exception {
		int status = addDocumentAndCommitLater(doc, 1);
		return status;
	}

	/**
	 * 添加记录并延时提交
	 */
	@Override
	public int addDocumentAndCommitLater(SolrInputDocument doc, int commitWithin) throws Exception {
		try {
			UpdateRequest req = new UpdateRequest();
			//			req.setAction(UpdateRequest.ACTION.COMMIT, false, false);
			req.setCommitWithin(commitWithin);
			req.add(doc);
			UpdateResponse res = req.process(SolrFactory.getUpdateCloudServer(collection));
			int status = res.getStatus();
			return status;
		} catch (Exception e) {
			logger.error("addDocumentAndCommitLater err...", e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

	/**
	 * 添加一组记录并立即提交
	 */
	@Override
	public int addDocumentsAndCommit(List<SolrInputDocument> docList) throws Exception {
		int status = addDocumentsAndCommitLater(docList, 1);
		return status;
	}

	/**
	 * 添加一组记录并延时提交
	 */
	@Override
	public int addDocumentsAndCommitLater(List<SolrInputDocument> docList, int commitWithin) throws Exception {
		if (docList == null || docList.size() == 0) {
			return 0;
		}

		try {
			UpdateRequest req = new UpdateRequest();
			// req.setAction(UpdateRequest.ACTION.COMMIT, false, false);
			req.setCommitWithin(commitWithin);
			req.add(docList);
			UpdateResponse res = req.process(SolrFactory.getUpdateCloudServer(collection));
			int status = res.getStatus();
			return status;
		} catch (Exception e) {
			logger.error("addDocumentsAndCommitLater err...", e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

	/**
	 * 按主键删除
	 *
	 * @param docid
	 * @return
	 */
	@Override
	public int deleteDocumentAndCommit(String docid) throws Exception {
		int status = deleteDocumentAndCommitLater(docid, 1);
		return status;
	}

	/**
	 * 按主键延时删除
	 *
	 * @param docid
	 * @param commitWithin
	 * @return
	 */
	@Override
	public int deleteDocumentAndCommitLater(String docid, int commitWithin) throws Exception {

		try {
			UpdateRequest req = new UpdateRequest();
			if (commitWithin > 0) {
				// https://issues.apache.org/jira/browse/SOLR-3498    这个问题，好像只改了add，没改deleteById
				//				req.setCommitWithin(commitWithin);
				req.setParam(UpdateParams.COMMIT_WITHIN, String.valueOf(commitWithin));
			}
			//			req.setAction(UpdateRequest.ACTION.COMMIT, , true, false);
			req.deleteById(docid);
			UpdateResponse res = req.process(SolrFactory.getUpdateCloudServer(collection));
			int status = res.getStatus();
			return status;
		} catch (Exception e) {
			logger.error("deleteDocumentAndCommitLater err...", e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

	/**
	 * 按条件删除
	 *
	 * @param query
	 * @return
	 */
	@Override
	public int deleteDocumentByQueryAndCommit(String query) throws Exception {
		int status = deleteDocumentByQueryAndCommitLater(query, 1);
		return status;
	}

	/**
	 * 按条件延时删除
	 *
	 * @param query
	 * @param commitWithin
	 * @return
	 */
	@Override
	public int deleteDocumentByQueryAndCommitLater(String query, int commitWithin) throws Exception {
		UpdateRequest req = new UpdateRequest();
		req.setAction(UpdateRequest.ACTION.COMMIT, false, false);
		req.deleteByQuery(query);

		if (commitWithin > 0) {
			// TODO 有时间看源码是为什么
			// 对于deleteByQuery，setCommitWithin和setParam都不好使，只能setAction马上提交
			req.setCommitWithin(commitWithin);
			//			req.setParam(UpdateParams.COMMIT_WITHIN, String.valueOf(commitWithin));
		}

		try {
			UpdateResponse res = req.process(SolrFactory.getUpdateCloudServer(collection));
			int status = res.getStatus();
			return status;
		} catch (Exception e) {
			logger.error("deleteDocumentByQueryAndCommitLater err...", e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

	/**
	 * 提交
	 */
	@Override
	public int commit() throws Exception {
		try {
			UpdateResponse res = SolrFactory.getUpdateCloudServer(collection).commit();
			int status = res.getStatus();
			return status;
		} catch (Exception e) {
			logger.error("commit err...", e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

	@Override
	public void shutdownUpdateServer() {
		SolrFactory.getUpdateCloudServer(collection).shutdown();
	}

	@Override
	public void optmize() throws Exception {
		try {
			SolrFactory.getUpdateCloudServer(collection).optimize();
		} catch (Exception e) {
			logger.error("optmize err...", e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

}
