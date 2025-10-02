package com.example.nocap.domain.analysis.service;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.analysis.dto.AnalysisViewDto;
import com.example.nocap.domain.analysis.mapper.AnalysisMapper;
import com.example.nocap.domain.analysis.dto.AnalysisDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.analysis.repository.AnalysisRepository;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.domain.useranalysis.entity.UserAnalysis;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final AnalysisMapper analysisMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AnalysisDto> getAllAnalysis() {
        return analysisRepository.findAll().stream()
            .map(analysisMapper::toAnalysisDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public AnalysisViewDto getAnalysisById(Long id) {
        Analysis analysis = analysisRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.ANALYSIS_NOT_FOUND));
        analysis.setView(analysis.getView() + 1);
        return analysisMapper.toAnalysisViewDto(analysis);
    }

    @Transactional(readOnly = true)
    public List<AnalysisDto> getAnalysisByCategory(String category) {
        return analysisRepository.findAllByCategory(category).stream()
            .map(analysisMapper::toAnalysisDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalysisDto> getAnalysisByUserId(UserDetail userDetail) {

        User user = userRepository.findById(userDetail.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return user.getUserAnalyses().stream()
            .map(UserAnalysis::getAnalysis)      // 각 UserAnalysis 객체에서 Analysis 엔티티를 추출
            .map(analysisMapper::toAnalysisDto)  // 각 Analysis 엔티티를 AnalysisDto로 변환
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAnalysisById(Long id) {
        analysisRepository.deleteById(id);
    }

}