package com.example.nocap.domain.analysis.service.parser;

import com.example.nocap.domain.mainnews.entity.MainNews;
import org.jsoup.nodes.Document;

public interface SiteSpecificParser {

    // 이 파서가 해당 URL을 처리할 수 있는지 여부를 반환
    boolean canParse(String url);

    // ✨ Document와 url을 모두 받아서 MainNews를 반환하도록 수정
    MainNews extract(Document doc, String url);
}
