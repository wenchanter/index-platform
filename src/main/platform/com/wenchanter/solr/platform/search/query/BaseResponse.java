package com.wenchanter.solr.platform.search.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.GroupResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Collation;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Correction;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;

public final class BaseResponse {

	private QueryResponse response;

	private static final String facet_range_other_attr = "_other";

	public BaseResponse(QueryResponse response) {
		this.response = response;
	}

	/**
	 * 获取普通查询结果
	 *
	 * @return
	 */

	public Map<String, Object> getResults() {
		if (response == null) {
			return null;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		SolrDocumentList sdoclist = response.getResults();
		if (sdoclist != null) {
			result.put("total", sdoclist.getNumFound());
			ArrayList<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
			result.put("result", resultList);
			// 增加cursorMark
			result.put("nextCursorMark", response.getNextCursorMark());
			for (int i = 0; i < sdoclist.size(); i++) {
				Map<String, Object> fieldMap = new HashMap<String, Object>();
				SolrDocument sdoc = sdoclist.get(i);
				Collection<?> names = sdoc.getFieldNames();
				Iterator<?> itr = names.iterator();
				while (itr.hasNext()) {
					String name = (String) itr.next();
					Object value = sdoc.getFieldValue(name);
					fieldMap.put(name, value);
				}
				resultList.add(fieldMap);
			}
		}
		return result;
	}


	public Map<String, Object> getResultsWithSimpleGroup() {
		if (response == null) {
			return null;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		GroupResponse rsp = response.getGroupResponse();
		List<GroupCommand> lst = rsp.getValues();
		for (int i = 0; null != lst && i < lst.size(); i++) {
			GroupCommand groupCommand = lst.get(i);
			List<Group> gst = groupCommand.getValues();
			result.put("total", groupCommand.getNGroups());
			for (int j = 0; null != gst && j < gst.size(); j++) {
				Group g = gst.get(j);
				SolrDocumentList sdoclist = g.getResult();
				if (sdoclist != null) {
					List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
					result.put(g.getGroupValue() + "_result", resultList);
					for (int k = 0; k < sdoclist.size(); k++) {
						Map fieldMap = new HashMap();
						SolrDocument sdoc = sdoclist.get(k);
						Collection names = sdoc.getFieldNames();
						Iterator itr = names.iterator();
						while (itr.hasNext()) {
							String name = (String) itr.next();
							Object value = sdoc.getFieldValue(name);
							fieldMap.put(name, value);
						}
						resultList.add(fieldMap);
					}
				}
			}
		}

		return result;
	}


	public Map<String, Object> getResultsWithGroupedGroup(String groupfield) {
		if (response == null) {
			return null;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		GroupResponse rsp = response.getGroupResponse();
		List<GroupCommand> lst = rsp.getValues();
		for (int i = 0; null != lst && i < lst.size(); i++) {
			GroupCommand groupCommand = lst.get(i);
			List<Group> gst = groupCommand.getValues();
			if (groupfield.equals(groupCommand.getName())) {
				result.put("total", groupCommand.getNGroups());
				if (gst == null || gst.size() == 0) {
					result.put("result", new ArrayList<Map<String, Object>>());
				} else {
					for (int j = 0; j < gst.size(); j++) {
						Group g = gst.get(j);
						SolrDocumentList sdoclist = g.getResult();
						if (sdoclist != null) {
							List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
							for (int k = 0; k < sdoclist.size(); k++) {
								Map fieldMap = new HashMap();
								SolrDocument sdoc = sdoclist.get(k);
								Collection names = sdoc.getFieldNames();
								Iterator itr = names.iterator();
								while (itr.hasNext()) {
									String name = (String) itr.next();
									Object value = sdoc.getFieldValue(name);
									fieldMap.put(name, value);
								}
								resultList.add(fieldMap);
							}

							Object temp = result.get("result");
							if (null == temp) {
								result.put("result", resultList);
							} else {
								((List<Map<String, Object>>) temp).addAll(resultList);
								result.put("result", temp);
							}
						}
					}
				}
			}
		}
		return result;
	}


	public Map<String, Object> getResultsWithSimpleGroup(String field) {
		if (response == null) {
			return null;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		GroupResponse rsp = response.getGroupResponse();
		List<GroupCommand> lst = rsp.getValues();
		for (int i = 0; null != lst && i < lst.size(); i++) {
			GroupCommand groupCommand = lst.get(i);
			List<Group> gst = groupCommand.getValues();
			Integer groupnum = groupCommand.getNGroups();
			if (groupnum == null) {
				result.put("total", groupCommand.getMatches());// 由于每个group可以有多个记录，用这个数字代表匹配数是不精确的
			} else {
				result.put("total", groupCommand.getNGroups());// 在用solrcloud的时候，只有保证同一个group在一个shard才正确使用group
			}
			for (int j = 0; null != gst && j < gst.size(); j++) {
				Group g = gst.get(j);
				if (field.equals(g.getGroupValue())) {
					SolrDocumentList sdoclist = g.getResult();
					if (sdoclist != null) {
						List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
						result.put("result", resultList);
						for (int k = 0; k < sdoclist.size(); k++) {
							Map fieldMap = new HashMap();
							SolrDocument sdoc = sdoclist.get(k);
							Collection names = sdoc.getFieldNames();
							Iterator itr = names.iterator();
							while (itr.hasNext()) {
								String name = (String) itr.next();
								Object value = sdoc.getFieldValue(name);
								fieldMap.put(name, value);
							}
							resultList.add(fieldMap);
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * 获取带高亮字段查询结果
	 *
	 * @return
	 */

	public Map<String, Object> getResultsWithHightlight() {
		if (response == null) {
			return null;
		}

		SolrDocumentList doclist = response.getResults();

		if (doclist == null) {
			return null;
		}

		Map<String, Map<String, List<String>>> hlmap = response.getHighlighting();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("total", hlmap.size());
		ArrayList<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		result.put("result", resultList);

		Iterator<SolrDocument> docit = doclist.iterator();
		while (docit.hasNext()) {
			SolrDocument doc = docit.next();
			Map<String, Object> m = new HashMap<String, Object>();
			resultList.add(m);
			String id = (String) doc.getFieldValue("id");
			m.put("id", id);
			Map<String, List<String>> value = hlmap.get(id);
			List<String> ts = value.get("title");
			if (ts == null) {
				ts = value.get("title_maxword");
			}
			if (ts != null) {
				m.put("title", ts.get(0));
			}
			List<String> ds = value.get("dkeys");
			if (ds != null) {
				m.put("dkeys", ds.get(0));
			}
		}

		return result;
	}


	public HashMap<String, Object> getFieldFacetResults() {
		return getFieldFacetResults(null);
	}

	/**
	 * 获取FieldFacet分组结果
	 *
	 * @param countLimits
	 *            对每组结果的count值做过滤，获取小于limit值得count列表。 格式: fieldname:countlimit,
	 *            例如 "setid:900", 表示在setid分组结果中， 只有count值小于900的，才会列出。
	 *            该参数置空或者未设置，表示无限制。
	 * @return
	 */

	public HashMap<String, Object> getFieldFacetResults(HashMap countLimits) {
		if (response == null) {
			return null;
		}

		HashMap<String, Object> facetResultMap = new HashMap<String, Object>();
		List<FacetField> facetList = response.getFacetFields();
		if (facetList != null) {
			for (FacetField facetField : facetList) {
				String fieldname = facetField.getName();
				if (countLimits != null) {
					try {
						Object countLimit = countLimits.get(fieldname);
						if (countLimit != null) {
							facetField = facetField.getLimitingFields((Integer) countLimit);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				List<FacetField.Count> counts = facetField.getValues();
				if (counts != null) {
					List<HashMap> countList = new ArrayList<HashMap>();
					for (FacetField.Count count : counts) {
						long countnum = count.getCount();
						String countName = count.getName();
						HashMap countmap = new HashMap();
						countmap.put("countNum", countnum);
						countmap.put("countName", countName);
						countList.add(countmap);
					}
					facetResultMap.put(fieldname, countList);
				}
			}
		}

		return facetResultMap;
	}

	/**
	 * 获取面向区间的分组查询结果 setid属性表示常规分组结果，setid_other表示边缘结果 例如： {
	 * setid_other={BEFORE=23, AFTER=2593, BETWEEN=2593},
	 * setid=[{countName=39127, countNum=383},{countName=39133, countNum=26}] }
	 */


	public HashMap getRangeFacetResults() {
		if (response == null) {
			return null;
		}
		List<RangeFacet> facetList = response.getFacetRanges();
		if (facetList != null) {
			HashMap<String, Object> facetResultMap = new HashMap<String, Object>();
			for (RangeFacet rangeFacet : facetList) {
				Number after = rangeFacet.getAfter();
				Number before = rangeFacet.getBefore();
				String facetName = rangeFacet.getName();
				HashMap facetOtherMap = new HashMap();
				facetOtherMap.put(FacetParams.FacetRangeOther.BEFORE.name(), before);
				facetOtherMap.put(FacetParams.FacetRangeOther.AFTER.name(), after);
				facetOtherMap.put(FacetParams.FacetRangeOther.BETWEEN.name(), after);

				List<RangeFacet.Count> counts = rangeFacet.getCounts();
				if (counts != null) {
					List<HashMap> countList = new ArrayList<HashMap>();

					for (RangeFacet.Count count : counts) {
						String countName = count.getValue();
						int countNum = count.getCount();

						HashMap countmap = new HashMap();
						countmap.put("countNum", countNum);
						countmap.put("countName", countName);
						countList.add(countmap);

						// System.out.println(" name=" + facetName + ", "
						// + "before=" + before + ", " + "after=" + after
						// + ", count.getValue()=" + count.getValue()
						// + ", " + " count.getCount()="
						// + count.getCount() + ", between=");

					}

					facetResultMap.put(facetName + facet_range_other_attr, facetOtherMap);
					facetResultMap.put(facetName, countList);
				}

			}
			return facetResultMap;
		}
		return null;
	}

	/**
	 * 获取自定义查询分组的结果
	 *
	 * @return
	 */

	public Map<String, Integer> getQueryFacetResults() {
		if (response == null) {
			return null;
		}

		Map<String, Integer> queryFacetMap = response.getFacetQuery();

		return queryFacetMap;
	}

	public HashMap getTermResults() {
		if (response == null) {
			return null;
		}

		TermsResponse termsResponse = response.getTermsResponse();
		if (termsResponse != null) {

			Map<String, List<Term>> termMap = termsResponse.getTermMap();

			if (termMap != null) {
				HashMap<String, Object> facetResultMap = new HashMap<String, Object>();
				Iterator<String> itr = termMap.keySet().iterator();
				while (itr.hasNext()) {
					String termField = itr.next();
					List<Term> termsList = termMap.get(termField);
					if (termsList != null) {
						List<HashMap> terms = new ArrayList<HashMap>();
						for (Term term : termsList) {
							HashMap map = new HashMap();
							map.put("term", term.getTerm());
							// map.put("freq", term.getFrequency());
							terms.add(map);
							// System.out.println("--> term=" + term.getTerm() +
							// ", freq=" + term.getFrequency());
						}
						facetResultMap.put(termField, terms);
					}
				}

				return facetResultMap;
			}
		}

		return null;
	}

	/**
	 * 获得spellcheck查询结果
	 */

	public HashMap<String, Object> getSpellCheckResults(boolean needShowSuggestion) {
		if (response == null) {
			return null;
		}

		SpellCheckResponse spellcheckRespnse = response.getSpellCheckResponse();

		if (spellcheckRespnse != null) {

			// 获取suggestion
			List<Suggestion> suggestions = spellcheckRespnse.getSuggestions();
			boolean correct = spellcheckRespnse.isCorrectlySpelled();
			// System.out.println("correct=" + correct);

			if (correct == true) {
				return null;
			}

			HashMap<String, Object> spellcheckResultMap = new HashMap<String, Object>();

			if (needShowSuggestion && suggestions != null && suggestions.size() > 0) {
				List<HashMap> suggestionResultList = new ArrayList<HashMap>();
				Iterator<Suggestion> itr = suggestions.iterator();
				while (itr.hasNext()) {
					Suggestion suggestion = itr.next();
					String token = suggestion.getToken();
					List alters = suggestion.getAlternatives();
					int numFound = suggestion.getNumFound();
					HashMap suggestionResult = new HashMap();
					suggestionResult.put("token", token);
					suggestionResult.put("numFound", numFound);
					suggestionResult.put("alters", alters);
					suggestionResultList.add(suggestionResult);
					// System.out.println("token=" + token + ", alters =" +
					// alters + ", numfound=" + numFound);
				}
				spellcheckResultMap.put("suggestions", suggestionResultList);
			}

			// //获取collation
			List<Collation> collations = spellcheckRespnse.getCollatedResults();
			if (collations != null && collations.size() > 0) {

				List<HashMap> collationResultList = new ArrayList<HashMap>();

				Iterator<Collation> itr = collations.iterator();
				while (itr.hasNext()) {
					Collation collation = itr.next();
					HashMap collationResult = new HashMap();
					String collationQueryString = collation.getCollationQueryString();
					long hits = collation.getNumberOfHits();
					collationResult.put("collationQueryString", collationQueryString);
					collationResult.put("hits", hits);

					List<Correction> corrections = collation.getMisspellingsAndCorrections();
					if (corrections != null && corrections.size() > 0) {
						List correctionsList = new ArrayList();
						Iterator<Correction> itr2 = corrections.iterator();
						while (itr2.hasNext()) {
							Correction correction = itr2.next();
							String correctString = correction.getCorrection();
							String OrigString = correction.getOriginal();
							HashMap correctionMap = new HashMap();
							correctionMap.put("correct", correctString);
							correctionMap.put("original", OrigString);
							correctionsList.add(correctionMap);
						}
						collationResult.put("corrections", correctionsList);
					}

					collationResultList.add(collationResult);

					// System.out.println("collationQueryString=" +
					// collationQueryString);
				}
				spellcheckResultMap.put("collations", collationResultList);
			}
			// System.out.println("collations=" + collations);
			return spellcheckResultMap;
		}
		return null;
	}

}
