package com.sejong.userservice.support.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class HtmlImageHandler {

    // 1. temp 이미지 URL만 추출 (외부 이미지는 제외)
    public List<String> extractTempImageUrls(String html, String tempPrefix) {
        List<String> tempUrls = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements images = doc.select("img[src]");

        for (Element img : images) {
            String src = img.attr("src");
            if (src.contains(tempPrefix)) {  // "temp/" 포함된 것만
                tempUrls.add(src);
            }
        }
        return tempUrls;
    }

    // 2. URL 교체 (temp → final)
    public String replaceImageUrls(String html, Map<String, String> oldToNewUrlMap) {
        String result = html;
        for (Map.Entry<String, String> entry : oldToNewUrlMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
