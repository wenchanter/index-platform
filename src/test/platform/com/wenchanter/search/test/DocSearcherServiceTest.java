package com.wenchanter.search.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.wenchanter.solr.platform.search.query.BaseQuery;
import com.wenchanter.solr.platform.search.query.BaseResponse;
import com.wenchanter.solr.platform.search.query.IndexSearcher;
import com.wenchanter.solr.platform.util.DateUtils;

public class DocSearcherServiceTest {

//	SearcherService docSearcher;

	@Before
	public void setUp() throws Exception {
//		BeanFactory factory = new ClassPathXmlApplicationContext(
//				new String[] { "applicationContext-all-test.xml" });
//		docSearcher = (SearcherService) factory.getBean("docSearcherService");
		// System.out.println(factory.getBean("docSearcherService"));
		// System.out.println(factory.getBean("docSearcherService"));
		// System.out.println(factory.getBean("docSearcherService"));
	}

	@Test
	public void testSimple() throws Exception {
		String q = "title:阿森纳 OR dkeys:阿森纳";
//		docSearcher.setCommonCondition(q, null, "id", null, 0, 5);
//		String groupfield = "signiture";
//		docSearcher.addQueryGroupCondition(groupfield, 1, false, true);
//		docSearcher.search();
//
//		System.out.println(docSearcher.getResultsWithGroupedGroup(groupfield));

		BaseQuery query = new BaseQuery();
//		query.setCommonCondition(q, 0, 5);
		query.setCommonCondition(q, null, "title,score", "score desc, id desc", 0, 3);
		query.setCursorMarkCondition("AoIIQCXc1zA5UlA1TFZBQzAwMDk0T0RW");
		BaseResponse response = IndexSearcher.search(query, "docCollection");
		Map<String,Object> result = response.getResults();
		System.out.println(result);

	}

	@Test
	public void testHightLight() throws Exception {
		String q = "title:上古卷轴 OR dkeys:上古卷轴";
		BaseQuery query = new BaseQuery();
		query.setCommonCondition(q, null, "*,score", "score desc, id desc", 0, 3);
		query.setHighlightCondition("title, dkeys", "<en>", "</em>", 1);
		BaseResponse response = IndexSearcher.search(query, "docCollection");
		System.out.println(response.getResultsWithHightlight());
	}

	@Test
	public void testDateRange() throws Exception {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date start = DateUtils.toDate("2013-03-01 00:00:00", format);
		Date end = DateUtils.toDate("2013-03-028 00:00:00", format);

//		docSearcher.addDateRangeFacetCondition("ptime", start, end, "+7DAY");
//		docSearcher.search();
//		System.out.println(docSearcher.getRangeFacetResults());
	}

	@Test
	public void testNumRange() throws Exception {

	}

}
