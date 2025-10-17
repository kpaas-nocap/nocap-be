package com.example.nocap.domain.question.service;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.question.dto.QuestionAnswerDto;
import com.example.nocap.domain.question.dto.QuestionDetailDto;
import com.example.nocap.domain.question.dto.QuestionRequestDto;
import com.example.nocap.domain.question.dto.QuestionSummaryDto;
import com.example.nocap.domain.question.entity.Question;
import com.example.nocap.domain.question.mapper.QuestionMapper;
import com.example.nocap.domain.question.repository.QuestionRepository;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;

    @Transactional
    public QuestionRequestDto saveQuestion(QuestionRequestDto questionRequestDto, UserDetail userDetail) {

        Long id = userDetail.getId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Question question = questionMapper.toQuestion(questionRequestDto);
        question.setUser(user);
        question.setCreatedAt(LocalDateTime.now());
        questionRepository.save(question);
        return questionRequestDto;
    }

    @Transactional(readOnly = true)
    public List<QuestionSummaryDto> getAllQuestion(UserDetail userDetail) {

        Long id = userDetail.getId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Question> questionList = user.getQuestions();

        return getQuestionSummaryDtoList(questionList);
    }

    @Transactional(readOnly = true)
    public QuestionDetailDto getQuestion(Long questionId, UserDetail userDetail) {

        Long id = userDetail.getId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));

        if (!Objects.equals(user.getId(), question.getUser().getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return questionMapper.toQuestionDetailDto(question);
    }

    @Transactional(readOnly = true)
    public List<QuestionSummaryDto> getAllQuestionByAdmin(UserDetail userDetail) {
        Long id = userDetail.getId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!user.getRole().equals("ROLE_ADMIN")) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        List<Question> questionList = questionRepository.findAll();
        return getQuestionSummaryDtoList(questionList);
    }

    @Transactional(readOnly = true)
    public QuestionDetailDto getQuestionByAdmin(Long questionId, UserDetail userDetail) {

        Long id = userDetail.getId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));

        if (!user.getRole().equals("ROLE_ADMIN")) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return questionMapper.toQuestionDetailDto(question);
    }

    @Transactional
    public QuestionDetailDto answerQuestionByAdmin(QuestionAnswerDto questionAnswerDto, UserDetail userDetail) {

        Long id = userDetail.getId();
        Long questionId = questionAnswerDto.getQuestionId();
        String answer = questionAnswerDto.getAnswer();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));

        if (!user.getRole().equals("ROLE_ADMIN")) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        question.setAnswer(answer);
        question.setAnsweredAt(LocalDateTime.now());
        questionRepository.save(question);
        return questionMapper.toQuestionDetailDto(question);

    }

    @NotNull
    private List<QuestionSummaryDto> getQuestionSummaryDtoList(List<Question> questionList) {
        return questionList.stream()
            .map(question -> {
                QuestionSummaryDto dto = new QuestionSummaryDto();
                dto.setQuestionId(question.getQuestionId());
                dto.setCategory(question.getCategory());
                dto.setContent(question.getContent());
                dto.setCreatedAt(question.getCreatedAt());

                // answer 필드가 null이 아니고 비어있지도 않으면 "Done", 그렇지 않으면 "Waiting"
                String status = (question.getAnswer() != null && !question.getAnswer().isBlank())
                    ? "Done"
                    : "Waiting";
                dto.setStatus(status);

                return dto;
            })
            .collect(Collectors.toList());
    }
}
