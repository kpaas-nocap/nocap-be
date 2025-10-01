package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.TitleCategoryDto;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService {

    private final OpenAiChatModel openAiChatModel;

    public OpenAIService(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    public TitleCategoryDto generate(String text) {

        ChatClient chatClient = ChatClient.create(openAiChatModel);

        try {
            // 이스케이프된 큰따옴표를 실제 큰따옴표로
            text = text.replace("\\\"", "\"");

            // 메시지
//            SystemMessage systemMessage = new SystemMessage(
//                "다음 뉴스 기사 본문을 바탕으로, 핵심 명사만 추출해 공백으로 구분된 검색어 형태로 만들어주세요. " +
//                    "조사·접속사·구두점 제거, 중복 없이 5~8개 주요 키워드 포함, 30자 이내 작성 예: '네이버 21대 대선 정치 댓글 감소'" +
//                    "입력된 기사의 내용을 통해 다음 category 중 하나로 분류를 해주세요: '정치', '경제', '사회', '생활문화', 'IT과학', '세계', '기타'"
//            );
            SystemMessage systemMessage = new SystemMessage(
                "You are a helpful assistant who is a professional news editor. " +
                    "Based on the following news article, please perform two tasks: " +
                    "1. Summarize the core event of the article into a single, natural-sounding search query of about 5 to 7 keywords. This query will be used to find similar news articles. " +
                    "2. Classify the article into one of the following categories: '정치', '경제', '사회', '생활문화', 'IT과학', '세계', '기타'. " +
                    "The final output MUST be a JSON object with exactly two keys: a 'title' key for the search query, and a 'category' key for the classification."
            );
            UserMessage userMessage = new UserMessage(text);
            AssistantMessage assistantMessage = new AssistantMessage("");

            // 옵션
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.1)
                .build();

            // 프롬프트
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);

            // 요청 및 응답
            return chatClient.prompt(prompt).call().entity(TitleCategoryDto.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OPEN_API_ERROR);
        }

    }

}
