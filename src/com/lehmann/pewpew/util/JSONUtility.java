package com.lehmann.pewpew.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtility {

	public static List listFromJSONArray(final JSONArray jsonArray) {
		final List result = new ArrayList();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				final Object obj = JSONUtility.objectFromJSONItem(jsonArray
						.get(i));
				result.add(obj);
			}
		} catch (final JSONException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	public static List listFromJSONString(final String jsonString) {
		try {
			return JSONUtility.listFromJSONArray(new JSONArray(jsonString));
		} catch (final JSONException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Map mapFromJSONObject(final JSONObject jsonObject) {
		final Map result = new HashMap();
		try {
			for (final Iterator ki = jsonObject.keys(); ki.hasNext();) {
				final String key = (String) ki.next();
				final Object value = JSONUtility.objectFromJSONItem(jsonObject
						.get(key));
				result.put(key, value);
			}
		} catch (final JSONException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	public static Map<String, Object> mapFromJSONString(final String jsonString) {
		try {
			return JSONUtility.mapFromJSONObject(new JSONObject(jsonString));
		} catch (final JSONException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Object objectFromJSONItem(final Object jsonItem) {
		if (jsonItem == JSONObject.NULL) {
			return null;
		}
		if (jsonItem instanceof JSONArray) {
			return JSONUtility.listFromJSONArray((JSONArray) jsonItem);
		}
		if (jsonItem instanceof JSONObject) {
			return JSONUtility.mapFromJSONObject((JSONObject) jsonItem);
		}
		return jsonItem;
	}

}
