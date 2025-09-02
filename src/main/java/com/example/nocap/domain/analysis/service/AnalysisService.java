package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.AnalysisViewDto;
import com.example.nocap.domain.analysis.mapper.AnalysisMapper;
import com.example.nocap.domain.analysis.dto.AnalysisDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.analysis.repository.AnalysisRepository;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final AnalysisMapper analysisMapper;
    private final UserRepository userRepository;

    public List<AnalysisDto> getAllAnalysis() {
        return analysisRepository.findAll().stream()
            .map(analysisMapper::toAnalysisDto)
            .collect(Collectors.toList());
    }

    public AnalysisViewDto getAnalysisById(Long id) {
        Analysis analysis = analysisRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.ANALYSIS_NOT_FOUND));
        return analysisMapper.toAnalysisViewDto(analysis);
    }

    public List<AnalysisDto> getAnalysisByCategory(String category) {
        return analysisRepository.findAllByCategory(category).stream()
            .map(analysisMapper::toAnalysisDto)
            .collect(Collectors.toList());
    }

    public List<AnalysisDto> getAnalysisByUserId(Long id) {

//        String userId = customUserDetails.getId();
//        System.out.println("customUserDetails에서 가져온 id : " + userId);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Analysis> analyses = analysisRepository.findAllByUserId(user.getId());
        return analyses.stream()
            .map(analysisMapper::toAnalysisDto)
            .collect(Collectors.toList());
    }

    public void deleteAnalysisById(Long id) {
        analysisRepository.deleteById(id);
    }
}