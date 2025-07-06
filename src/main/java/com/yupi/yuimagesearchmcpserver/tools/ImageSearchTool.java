package com.yupi.yuimagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageSearchTool {

    // 替换为你的 Pexels API 密钥（需从官网申请）
    private static final String API_KEY = "1nt17u964gBlPcjWd0Laprf00CcsSqjSgwxVs255yVPcI4K4SZZNdc05";

    // Pexels 常规搜索接口（请以文档为准）
    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        try {
            return String.join(",\n", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    /**
     * 搜索中等尺寸的图片列表
     *
     * @param query
     * @return
     */
    public List<String> searchMediumImages(String query) {
        // 设置请求头（包含API密钥）
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        // 设置请求参数（仅包含query，可根据文档补充page、per_page等参数）
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);

        // 发送 GET 请求
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        // 解析响应JSON（假设响应结构包含"photos"数组，每个元素包含"medium"字段）
        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                // 因为 Hutool 的 JSONArray 是一个普通的 Java List，元素默认是 Object 类型：
                //   photoObj 是一个 JSON 对象（其实是 Map）
                //   {
                //      "id": 123,
                //      "src": { "medium": "..." }
                //   }
                //   你需要强转成 JSONObject 才能继续拿字段
                .map(photoObj -> (JSONObject) photoObj)
                // 也就是转换为jsonobject这里才能调用getJSONObject方法
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());

        /**
         * {
         *   "total_results": 10000,
         *   "page": 1,
         *   "per_page": 1,
         *   "photos": [
         *     {
         *       "id": 3573351,
         *       "width": 3066,
         *       "height": 3968,
         *       "url": "https://www.pexels.com/photo/trees-during-day-3573351/",
         *       "photographer": "Lukas Rodriguez",
         *       "photographer_url": "https://www.pexels.com/@lukas-rodriguez-1845331",
         *       "photographer_id": 1845331,
         *       "avg_color": "#374824",
         *       "src": {
         *         "original": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png",
         *         "large2x": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
         *         "large": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&h=650&w=940",
         *         "medium": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&h=350",
         *         "small": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&h=130",
         *         "portrait": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&fit=crop&h=1200&w=800",
         *         "landscape": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&fit=crop&h=627&w=1200",
         *         "tiny": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&dpr=1&fit=crop&h=200&w=280"
         *       },
         *       "liked": false,
         *       "alt": "Brown Rocks During Golden Hour"
         *     }
         *   ],
         *   "next_page": "https://api.pexels.com/v1/search/?page=2&per_page=1&query=nature"
         * }
         */
    }
}
