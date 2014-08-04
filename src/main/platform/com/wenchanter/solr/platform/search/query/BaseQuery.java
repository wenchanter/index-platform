package com.wenchanter.solr.platform.search.query;

import java.util.Date;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.GroupParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SpellingParams;

import com.wenchanter.solr.platform.factory.SolrFactory;

public final class BaseQuery {

	private String collection = "unknown";
	// private static final String orderby = "score desc,ptime desc";// 排序
	private static final String fl = "id";// 返回字段
	//	private static final String functionQuery = "{!boost b=recip(ms(NOW/HOUR,ptime),3.16e-11,1,1)}";// 按时间缓慢衰减的相关度
//	private static final String functionQuery = "{!boost b=dateDeboost(ptime)}";// 按时间急剧衰减的相关度
	private static final String facet_prefix = "f.";

	private final SolrQuery query = new SolrQuery();

	//	public BaseQuery(String collection) {
	//		this.setCollection(collection);
	//	}

	SolrQuery getSolrQuery() {
		return query;
	}

	/**
	 * 执行查询动作
	 *
	 * @throws Exception
	 */
	//	public void search() throws Exception {
	//		try {
	//			response = SolrFactory.getLBServer(collection).query(query);
	//		} catch (Exception e) {
	//			String Qstr = query.getQuery().replace("&&", "AND").replace("||", "OR");
	//			String alarm = DateUtils.getNowStr() + "\t" + Qstr + "\t" + e.getMessage();
	//			System.err.println(alarm);
	//			throw e;
	//		}
	//	}

	/**
	 * 高级查询条件
	 *
	 * @param q
	 *            : 查询条件
	 * @param fq
	 *            : 过滤条件
	 * @param fl
	 *            : 指定返回字段，否则返回全部字段, 如 photoid, title
	 * @param orderby
	 *            ： 排序，例如 photoid asc, topicid desc
	 * @param start
	 *            ： 开始位置
	 * @param limit
	 *            ：查询条数
	 * @return
	 */
	public void setCommonCondition(String q, String fq, String fl, String orderby, int start, int limit) {
//		if (StringUtils.isBlank(orderby)) {//不指定排序，则用相关度-时间的综合排序
//			q = functionQuery + q;
//		}
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set(CommonParams.Q, q); // 基础查询条件
		params.set(CommonParams.FL, fl); // 指定返回字段
		params.set(CommonParams.FQ, fq); // 过滤查询条件
		params.set(CommonParams.SORT, orderby);// 排序
		params.set(CommonParams.START, start);// 偏移量offset
		params.set(CommonParams.ROWS, limit);// 个数
		this.query.add(params);
		query.set(ShardParams.SHARDS_TOLERANT, "true");// 防止一个shard挂掉，导致所有查询有问题。
	}

	public void addCondition(String param, String... querys) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add(param, querys);
		this.query.add(params);
	}

	public void setCommonCondition(String q, int start, int limit) {
		// 设置索引读取的服务器们
		// Map collectionConfig = (Map) SolrFactory.config.get(collection);
		// String READ_SERVERS = (String) collectionConfig
		// .get("ShardParams.SHARDS");
		// if (StringUtils.isNotBlank(READ_SERVERS)) {
		// query.set(ShardParams.SHARDS, READ_SERVERS);
		// }
		// q=q+"-status:99";//去掉特定状态的文章
		setCommonCondition(q, null, fl, null, start, limit);
	}

	public void setCommonConditionReturn(String q, String fl, int start, int limit) {
		// 设置索引读取的服务器们
		// Map collectionConfig = (Map) SolrFactory.config.get(collection);
		// String READ_SERVERS = (String) collectionConfig
		// .get("ShardParams.SHARDS");
		// if (StringUtils.isNotBlank(READ_SERVERS)) {
		// query.set(ShardParams.SHARDS, READ_SERVERS);
		// }
		// q=q+"-status:99";//去掉特定状态的文章
		setCommonCondition(q, null, fl, null, start, limit);
	}

	/**
	 * 带游标的常用属性
	 *
	 * @param q
	 * @param fq
	 * @param fl
	 * @param orderby
	 * @param cursorMark
	 * @param limit
	 */
	public void setCommonCursorMarkCondition(String q, String fq, String fl, String orderby, String cursorMark,
			int limit) {
		setCommonCondition(q, fq, fl, orderby, 0, limit);
		setCursorMarkCondition(cursorMark);
	}

	public void setCommonCursorMarkCondition(String q, String fl, String cursorMark,
			int limit) {
		setCommonCursorMarkCondition(q, null, fl, "score desc, id asc", cursorMark, limit);
	}

	public void setCursorMarkCondition(String cursorMark) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark); // 基础查询条件
		this.query.add(params);
	}

	/**
	 * 高级查询条件
	 *
	 * @param hls
	 *            : 指定高亮字段
	 * @return
	 */
	public void setHighlightCondition(String hls, String prefix, String postfix, int snippets) {

		ModifiableSolrParams params = new ModifiableSolrParams();

		// 设定高亮字段
		if (hls != null && hls.length() > 0) {
			params.add(HighlightParams.FIELDS, hls);
			params.set(HighlightParams.HIGHLIGHT, true);
			params.set(HighlightParams.SIMPLE_PRE, prefix);
			params.set(HighlightParams.SIMPLE_POST, postfix);
			params.set(HighlightParams.SNIPPETS, snippets);
		}

		this.query.add(params);

	}

	/**
	 * 设置通用FieldFacet参数
	 *
	 * @param facetFields
	 *            ：需要分组查询的字段
	 * @param facetSort
	 *            : 分组结果排序策略,默认是count，即按照每组结果数量，亦可选择按照字典序 index
	 * @param facetLimit
	 *            : 返回分组的组数量，默认是100组, -1表示不限制
	 * @param facrtOffset
	 *            : 分组结果的起始偏移量，默认是0.
	 * @param facetMincount
	 *            : 分组结果值的最小值，小于这个限制的分组结果不返回，默认是0.
	 */
	public void setFieldFacetCondition(String[] facetFields, String facetSort, int facetLimit, int facetOffset,
			int facetMincount) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set(FacetParams.FACET, true);
		params.add(FacetParams.FACET_FIELD, facetFields);
		params.set(FacetParams.FACET_SORT, facetSort);
		params.set(FacetParams.FACET_LIMIT, facetLimit);
		params.set(FacetParams.FACET_OFFSET, facetOffset);
		params.set(FacetParams.FACET_MINCOUNT, facetMincount);
		this.query.add(params);
	}

	/**
	 * 设置某个字段的FieldFacet参数
	 *
	 * @param facetFields
	 *            ：需要分组查询的字段
	 * @param facetSort
	 *            : 分组结果排序策略,默认是count，即按照每组结果数量，亦可选择按照字典序 index
	 * @param facetLimit
	 *            : 返回分组的组数量，默认是100组, -1表示不限制
	 * @param facrtOffset
	 *            : 分组结果的起始偏移量，默认是0.
	 * @param facetMincount
	 *            : 分组结果值的最小值，小于这个限制的分组结果不返回，默认是0.
	 */
	public void addSingleFieldFacetCondition(String facetField, String facetSort, int facetLimit, int facetOffset,
			int facetMincount) {

		String prefix = facet_prefix + facetField + ".";
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set(prefix + FacetParams.FACET_SORT, facetSort);
		params.set(prefix + FacetParams.FACET_LIMIT, facetLimit);
		params.set(prefix + FacetParams.FACET_OFFSET, facetOffset);
		params.set(prefix + FacetParams.FACET_MINCOUNT, facetMincount);
		this.query.add(params);
		this.query.add(FacetParams.FACET, "true");
		this.query.add(FacetParams.FACET_FIELD, facetField);
	}

	/**
	 * 设置某个字段的FieldFacet参数
	 *
	 * @param facetFields
	 *            ：需要分组查询的字段
	 * @param facetSort
	 *            : 分组结果排序策略,默认是count，即按照每组结果数量，亦可选择按照字典序 index
	 * @param facetLimit
	 *            : 返回分组的组数量，默认是100组, -1表示不限制
	 * @param facrtOffset
	 *            : 分组结果的起始偏移量，默认是0.
	 * @param facetMincount
	 *            : 分组结果值的最小值，小于这个限制的分组结果不返回，默认是0.
	 */
	public void addSinglePrefixFacetCondition(String facetField, String facetPrefix, String facetSort, int facetLimit,
			int facetOffset, int facetMincount) {

		String prefix = facet_prefix + facetField + ".";
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set(prefix + FacetParams.FACET_SORT, facetSort);
		params.set(prefix + FacetParams.FACET_LIMIT, facetLimit);
		params.set(prefix + FacetParams.FACET_OFFSET, facetOffset);
		params.set(prefix + FacetParams.FACET_MINCOUNT, facetMincount);
		params.set(prefix + FacetParams.FACET_PREFIX, facetPrefix);
		this.query.add(params);
		this.query.add(FacetParams.FACET, "true");
		this.query.add(FacetParams.FACET_FIELD, facetField);

	}

	/**
	 * 设置字段的范围查询条件
	 */
	public void addSingleRangeFacetCondition(String rangeField, String rangeStart, String rangeEnd, String rangeGap) {
		this.addSingleRangeFacetCondition(rangeField, rangeStart, rangeEnd, rangeGap, false, null, null);
	}

	/**
	 * 设置字段的范围查询条件
	 */
	public void addDateRangeFacetCondition(String rangeField, Date rangeStart, Date rangeEnd, String rangeGap) {

		this.query.addDateRangeFacet(rangeField, rangeStart, rangeEnd, rangeGap);
		this.query.add(FacetParams.FACET, "true");
	}

	/**
	 * 设置字段的范围查询条件
	 *
	 * @param rangeField
	 *            : 指定范围查询的字段
	 * @param rangeStart
	 *            : 范围的开始边界
	 * @param rangeEnd
	 *            : 范围的结束边界
	 * @param rangeGap
	 *            : 范围的区间间距（等距）
	 * @param rangeHardEnd
	 *            : 上界是否限制，默认是false，表示最后一段区间长度，如果小于间距gap，则以gap作为最后的上界；
	 *            如果设置为true，表示以rangeEnd作为最后的上届。
	 * @param rangeOther
	 *            : [before] all records with field values lower then lower
	 *            bound of the first range [after] all records with field values
	 *            greater then the upper bound of the last range [between] all
	 *            records with field values between the start and end bounds of
	 *            all ranges [none] compute none of this information [all]
	 *            shortcut for before, between, and after
	 * @param rangeInclude
	 *            : [lower] = all gap based ranges include their lower bound
	 *            [upper] = all gap based ranges include their upper bound
	 *            [edge] = the first and last gap ranges include their edge
	 *            bounds (ie: lower for the first one, upper for the last one)
	 *            even if the corresponding upper/lower option is not specified
	 *            [outer] = the "before" and "after" ranges will be inclusive of
	 *            their bounds, even if the first or last ranges already include
	 *            those boundaries. [all] = shorthand for lower, upper, edge,
	 *            outer
	 */
	public void addSingleRangeFacetCondition(String rangeField, String rangeStart, String rangeEnd, String rangeGap,
			boolean rangeHardEnd, String rangeOther, String rangeInclude) {
		String prefix = facet_prefix + rangeField + ".";
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add(prefix + FacetParams.FACET_RANGE_START, rangeStart);
		params.add(prefix + FacetParams.FACET_RANGE_END, rangeEnd);
		params.add(prefix + FacetParams.FACET_RANGE_GAP, rangeGap);
		params.add(prefix + FacetParams.FACET_RANGE_HARD_END, Boolean.toString(rangeHardEnd));
		// 获取区间之外的数据, before 和 after .
		params.add(prefix + FacetParams.FACET_RANGE_OTHER, rangeOther);
		params.add(prefix + FacetParams.FACET_RANGE_INCLUDE, rangeInclude);

		this.query.add(params);
		this.query.add(FacetParams.FACET, "true");
		this.query.add(FacetParams.FACET_RANGE, rangeField);
	}

	/**
	 * 设置自定义分组条件（逐次累加条件）
	 */
	public void addQueryFacetCondition(String rangeQuery) {
		this.query.addFacetQuery(rangeQuery);
	}

	public void addQueryGroupCondition(String fields, int grouplimit, boolean useSimpleFormat, boolean showGroupNum) {
		query.add(GroupParams.GROUP, "true");
		String[] qq = fields.split(",");
		for (int i = 0; null != qq && i < qq.length; i++) {
			query.add(GroupParams.GROUP_FIELD, qq[i]);
		}
		query.add(GroupParams.GROUP_LIMIT, "" + grouplimit);
		if (useSimpleFormat) {
			query.add(GroupParams.GROUP_FORMAT, "simple");
		}
		if (showGroupNum) {
			query.add(GroupParams.GROUP_TOTAL_COUNT, "true");
		}
	}

	/**
	 * 设置自定义分组条件（一次性设定多个条件）
	 */
	public void setQueryFacetCondition(String[] rangeQuery) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set(FacetParams.FACET, true);
		params.add(FacetParams.FACET_QUERY, rangeQuery);
		this.query.add(params);
	}

	/**
	 * 添加按照prefix Term 查询参数
	 *
	 * @param termField
	 *            : term字段
	 * @param termPrefix
	 *            ： 查询前缀
	 * @param lower
	 *            ：查询的区间最小值
	 * @param upper
	 *            ：查询的区间最大值
	 * @param limit
	 *            ：查询结果返回数量
	 */
	public void addTermPrefixCondition(String termField, String termPrefix, String lower, String upper, int limit) {
		this.addTermCondition(termField, termPrefix, null, null, lower, true, upper, true, limit, 0, -1);
	}

	/**
	 * 添加按照 regex Term 查询参数
	 *
	 * @param termField
	 *            :term字段
	 * @param termRegex
	 *            :查询正则表达式
	 * @param termRegexFlag
	 *            ：正则查询的一些选项
	 * @param lower
	 *            ：查询的区间最小值
	 * @param upper
	 *            ：查询的区间最大值
	 * @param limit
	 *            ：查询结果返回数量
	 */
	public void addTermRegexCondition(String termField, String termRegex, String termRegexFlag, String lower,
			String upper, int limit) {
		this.addTermCondition(termField, null, termRegex, termRegexFlag, lower, true, upper, true, limit, 0, -1);
	}

	/**
	 * 设置Term查询的参数
	 *
	 * @param termField
	 *            : term字段
	 * @param termPrefix
	 *            : 查询前缀
	 * @param termRegex
	 *            ： 正则表达式
	 * @param termRegexFlag
	 *            ： 正则查询的一些选项
	 * @param lower
	 *            ： 查询的区间最小值
	 * @param lowerInc
	 *            ： 是否包括最小值边界，默认是true。
	 * @param upper
	 *            ： 查询的区间最大值
	 * @param upperInc
	 *            ：是否包含最大值边界，默认是false.
	 * @param limit
	 *            ： 查询结果返回数量
	 * @param minCount
	 *            : 返回结果中frequency必须满足的最小值。
	 * @param maxCount
	 *            ： 返回结果中frequency必须满足的最大值。
	 */
	public void addTermCondition(String termField, String termPrefix, String termRegex, String termRegexFlag,
			String lower, boolean lowerInc, String upper, boolean upperInc, int limit, int minCount, int maxCount) {
		this.query.setTerms(true);
		this.query.addTermsField(termField);
		this.query.setTermsPrefix(termPrefix);
		this.query.setTermsRegex(termRegex);
		this.query.setTermsRegexFlag(termRegexFlag);
		this.query.setTermsLower(lower);
		this.query.setTermsLowerInclusive(lowerInc);
		this.query.setTermsUpper(upper);
		this.query.setTermsUpperInclusive(upperInc);
		this.query.setTermsLimit(limit);
		this.query.setTermsMinCount(minCount);
		this.query.setTermsMaxCount(maxCount);
		this.query.set("qt", "/terms");
	}

	/**
	 * 添加spellcheck查询条件
	 *
	 * @param q
	 *            :关键词
	 * @param dict
	 *            ：词典名
	 * @param suggestionCount
	 *            ：返回的suggestions数量
	 * @param maxCollations
	 *            : 返回collations的最大数量上限, 实际返回collations的数量取决于 spellCheckCount和
	 *            maxCollations 的较小值。
	 * @param accuracy
	 *            : 相似度，超过该相似度的token才会被返回，一般为小于1的小数，数字越小，表示相似度限制越低，默认是0.5，
	 *            当取0的时候，表示无限制。
	 * @param extendResults
	 *            : 是否提供额外的suggestions返回参数。
	 * @param onlyMorePopular
	 *            : Only return suggestions that result in more hits for the
	 *            query than the existing query。
	 * @param collateExtendResults
	 *            : 是否提供额外的collation返回参数, 如correction信息。
	 * @param maxResultForSuggest
	 *            : 设置强制spellcheck的阀值，如果查询的结果数小于阀值，则会触发spellcheck，设置为0，
	 *            表示只有返回结果为空的时候，才会触发。 设置为无穷大的时候，表示一定会触发。
	 */
	public void addSpellCheckCondition(String q, String dict, String accuracy, String suggestionCount,
			String maxCollations, String maxResultForSuggest, boolean extendResults, boolean onlyMorePopular,
			boolean collateExtendResults) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add(SpellingParams.SPELLCHECK_COUNT, suggestionCount + 1);
		params.add(SpellingParams.SPELLCHECK_DICT, dict);
		params.add(SpellingParams.SPELLCHECK_Q, q);
		params.add(SpellingParams.SPELLCHECK_ACCURACY, accuracy);
		params.add(SpellingParams.SPELLCHECK_EXTENDED_RESULTS, String.valueOf(extendResults));
		params.add(SpellingParams.SPELLCHECK_ONLY_MORE_POPULAR, String.valueOf(onlyMorePopular));
		params.add(SpellingParams.SPELLCHECK_COLLATE_EXTENDED_RESULTS, String.valueOf(collateExtendResults));
		params.add(SpellingParams.SPELLCHECK_MAX_RESULTS_FOR_SUGGEST, maxResultForSuggest);
		params.add(SpellingParams.SPELLCHECK_MAX_COLLATIONS, maxCollations);
		params.add(SpellingParams.SPELLCHECK_MAX_COLLATION_TRIES, maxCollations);
		params.add(SpellingParams.SPELLCHECK_ALTERNATIVE_TERM_COUNT, suggestionCount);
		// 由于solr的版本bug，使用collate功能，有可能出现异常，所以去掉改功能
		params.add(SpellingParams.SPELLCHECK_COLLATE, "false");

		this.query.set("qt", "/suggest");
		this.query.add(params);
	}

	// TODO
	/**
	 * 查询个数
	 *
	 * @param where
	 * @return
	 */
	public long count(String where) {
		SolrQuery q = new SolrQuery();
		q.setQuery(where);
		q.setRows(0);

		QueryResponse rsp;
		try {
			rsp = SolrFactory.getLBServer(collection).query(q);
			SolrDocumentList docs = rsp.getResults();
			return docs.getNumFound();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return 0;
	}

	//	public String getCollection() {
	//		return collection;
	//	}
	//
	//	public void setCollection(String collection) {
	//		this.collection = collection;
	//	}

}
