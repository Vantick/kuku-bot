package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.logic.ArkNightsLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArkNightsLogicImpl implements ArkNightsLogic {

	private final String url = "https://ak.hypergryph.com";

	@Override
	public Result<List<Map<String, String>>> rechargeRecord(String cookie) throws IOException {
		Response response = OkHttpUtils.get(url + "/user/inquiryOrder", OkHttpUtils.addCookie(cookie));
		if (response.code() == 302) return Result.failure("cookie已失效！！", null);
		String html = OkHttpUtils.getStr(response);
		String jsonStr = BotUtils.regex("window.__INITIAL_DATA__ =", "</script>", html);
		JSONObject jsonObject = JSON.parseObject(jsonStr);
		JSONArray jsonArray = jsonObject.getJSONObject("inquiryOrder").getJSONArray("data");
		List<Map<String, String>> list = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++){
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			Map<String, String> map = new HashMap<>();
			map.put("amount", singleJsonObject.getString("amount"));
			map.put("productName", singleJsonObject.getString("productName"));
			map.put("payTime", singleJsonObject.getString("payTime") + "000");
			list.add(map);
		}
		return Result.success(list);
	}

	@Override
	public Result<List<Map<String, String>>> searchRecord(String cookie, Integer page) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson(url + "/user/api/inquiry/gacha?page=" + page, OkHttpUtils.addCookie(cookie));
		Integer code = jsonObject.getInteger("code");
		if (code == 0){
			List<Map<String, String>> list = new ArrayList<>();
			JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
			for (int i = 0; i < jsonArray.size(); i++){
				JSONObject singleJsonObject = jsonArray.getJSONObject(i);
				Map<String, String> map = new HashMap<>();
				map.put("ts", singleJsonObject.getString("ts") + "000");
				JSONArray charsJsonArray = singleJsonObject.getJSONArray("chars");
				StringBuilder result = new StringBuilder();
				for (int j = 0; j < charsJsonArray.size(); j++){
					JSONObject charJsonObject = charsJsonArray.getJSONObject(j);
					result.append(charJsonObject.getString("name")).append("--").append(charJsonObject.getInteger("rarity") + 1).append("星").append("/");
				}
				map.put("result", result.toString());
				list.add(map);
			}
			return Result.success(list);
		}else return Result.failure(jsonObject.getString("msg"), null);
	}

	@Override
	public Result<List<Map<String, String>>> sourceRecord(String cookie, Integer page) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson(url + "/user/api/inquiry/diamond?page=" + page, OkHttpUtils.addCookie(cookie));
		Integer code = jsonObject.getInteger("code");
		if (code == 0){
			List<Map<String, String>> list = new ArrayList<>();
			JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
			for (int i = 0; i < jsonArray.size(); i++){
				JSONObject singleJsonObject = jsonArray.getJSONObject(i);
				Map<String, String> map = new HashMap<>();
				map.put("ts", singleJsonObject.getString("ts") + "000");
				map.put("operation", singleJsonObject.getString("operation"));
				JSONObject changeJsonObject = singleJsonObject.getJSONArray("changes").getJSONObject(0);
				String coin = changeJsonObject.getString("before") + "->" + changeJsonObject.getString("after");
				map.put("coin", coin);
				list.add(map);
			}
			return Result.success(list);
		}else return Result.failure(jsonObject.getString("msg"), null);
	}
}
