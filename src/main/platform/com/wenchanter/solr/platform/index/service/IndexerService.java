package com.wenchanter.solr.platform.index.service;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;

public interface IndexerService {

	/**
	 * 插入一条记录
	 */
	public int addDocument(SolrInputDocument doc) throws Exception;

	/**
	 * 插入一组记录
	 */
	public int addDocuments(List<SolrInputDocument> docList)
			throws Exception;

	/**
	 * 添加记录并立即提交
	 */
	public int addDocumentAndCommit(SolrInputDocument doc) throws Exception;

	/**
	 * 添加记录并延时提交
	 */
	public int addDocumentAndCommitLater(SolrInputDocument doc, int commitWithin)
			throws Exception;

	/**
	 * 添加一组记录并立即提交
	 */
	public int addDocumentsAndCommit(List<SolrInputDocument> docList)
			throws Exception;

	/**
	 * 添加一组记录并延时提交
	 */
	public int addDocumentsAndCommitLater(List<SolrInputDocument> docList,
			int commitWithin) throws Exception;

	/**
	 * 按主键删除
	 *
	 * @param docid
	 * @return
	 */
	public int deleteDocumentAndCommit(String docid) throws Exception;

	/**
	 * 按主键延时删除
	 *
	 * @param docid
	 * @param commitWithin
	 * @return
	 */
	public int deleteDocumentAndCommitLater(String docid, int commitWithin)
			throws Exception;

	/**
	 * 按条件删除
	 *
	 * @param query
	 * @return
	 */
	public int deleteDocumentByQueryAndCommit(String query) throws Exception;

	/**
	 * 按条件延时删除
	 *
	 * @param query
	 * @param commitWithin
	 * @return
	 */
	public int deleteDocumentByQueryAndCommitLater(String query,
			int commitWithin) throws Exception;

	/**
	 * 提交
	 */
	public int commit() throws Exception;

	/**
	 * 关闭server
	 */
	public void shutdownUpdateServer();

	/**
	 * optmize索引
	 */
	public void optmize() throws Exception;
}
