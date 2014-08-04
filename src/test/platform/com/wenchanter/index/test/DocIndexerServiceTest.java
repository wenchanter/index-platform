package com.wenchanter.index.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;

import com.wenchanter.solr.platform.index.service.IndexerService;
import com.wenchanter.solr.platform.index.service.impl.IndexerServiceImpl;

public class DocIndexerServiceTest {

	public static ArrayList<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

	IndexerService docIndexer = new IndexerServiceImpl("docCollection");
	IndexerService autoIndexer = new IndexerServiceImpl("autocpltCollection");

	@Before
	public void setUp() throws Exception {
//		BeanFactory factory = new ClassPathXmlApplicationContext(new String[] { "applicationContext-all-test.xml" });
//		docIndexer = (IndexerService) factory.getBean("docIndexerService");
	}

	@Test
	public void indexOne() throws Exception {

		SolrInputDocument sd = new SolrInputDocument();
		sd.addField("id", "addone");
		sd.addField("channelid", "9999");
		sd.addField("topictree", "tptree");
		sd.addField("topicid", "tpid");
		sd.addField("dkeys", "测试");
		sd.addField("title", "junit 标题");
		sd.addField("ptime", new Date());
		sd.addField("url", "/junit/test/com");
		docIndexer.addDocument(sd);
	}

	@Test
	public void commit() throws Exception {
		docIndexer.commit();
	}


	@Test
	public void indexOneLater() throws Exception {

//		ArrayList<SolrInputDocument> buffer = new ArrayList<SolrInputDocument>();
		SolrInputDocument sd = new SolrInputDocument();
		sd.addField("id", "addone");
		sd.addField("channelid", "9999");
		sd.addField("topictree", "tptree");
		sd.addField("topicid", "tpid");
		sd.addField("dkeys", "测试");
		sd.addField("title", "junit 标题");
		sd.addField("ptime", new Date());
		sd.addField("url", "/junit/test/com");
//			System.out.println(doc);
//		buffer.add(sd);
		docIndexer.addDocumentAndCommitLater(sd, 1);
	}

	@Test
	public void indexListLater() throws Exception {

		ArrayList<SolrInputDocument> buffer = new ArrayList<SolrInputDocument>();
		SolrInputDocument sd = new SolrInputDocument();
		sd.addField("id", "addone");
		sd.addField("channelid", "9999");
		sd.addField("topictree", "tptree");
		sd.addField("topicid", "tpid");
		sd.addField("dkeys", "测试");
		sd.addField("title", "junit 标题");
		sd.addField("ptime", new Date());
		sd.addField("url", "/junit/test/com");
//			System.out.println(doc);
		buffer.add(sd);
		docIndexer.addDocumentsAndCommitLater(buffer, 1);

	}

	@Test
	public void updatePartial() throws Exception {
		SolrInputDocument sd = new SolrInputDocument();
		sd.addField("id", "addone");
		Map<String, Integer> partial = new HashMap<String, Integer>();
		partial.put("inc", 6);
		sd.addField("frequency", partial);
		sd.addField("word", "测试ceshi");
		sd.addField("quanpin", "suningceshiceshi");
		sd.addField("jianpin", "sncsceshi");
		sd.addField("type", "doc");

		autoIndexer.addDocumentAndCommitLater(sd, 1);
	}

	@Test
	public void deleteById() throws Exception {
		String docid = "addone";
		docIndexer.deleteDocumentByQueryAndCommitLater("id:*" + docid, 1);
	}

	@Test
	public void delById() throws Exception {
		String docid = "addone";
		docIndexer.deleteDocumentAndCommitLater(docid, 1);
	}

	@Test
	public void deleteAll() throws Exception {
//		docIndexer.deleteDocumentByQueryAndCommitLater("*:*", 10000);
	}
}
