package com.wenchanter.solr.platform.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Config {

	/**
	 * 需要同步的article的信息， 开始同步的时间starttime， 是否按时间从早到晚同步reverse， 同步的间隔（以天为单位）
	 * interval，同步的时候每次取的数据条数size
	 */

	private static Map<String, Object> solrConf;

	static {
		try {
			init();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Map<String, Object> getSolrConfig() {
		return solrConf;
	}

	private static void init() throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		String file = "config.json";
		ClassLoader loader = Config.class.getClassLoader();
		InputStream in = loader.getResourceAsStream(file);

		try {
			@SuppressWarnings("unchecked")
			Map<String, Map<String, Object>> confs = objectMapper.readValue(in, Map.class);
			solrConf = Collections.unmodifiableMap(confs.get("solrConfig"));
		} finally {
			in.close();
		}

	}

//	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
//	}
}
