package com.example.nocap.domain.history.service;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.history.dto.HistoryDetailDto;
import com.example.nocap.domain.history.dto.HistoryRequestDto;
import com.example.nocap.domain.history.dto.HistorySummaryDto;
import com.example.nocap.domain.history.entity.History;
import com.example.nocap.domain.history.mapper.HistoryMapper;
import com.example.nocap.domain.history.repository.HistoryRepository;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final HistoryMapper historyMapper;

    private static final int MAX_HISTORY_COUNT = 10; // 최대 히스토리 개수 상수로 관리


    @Transactional
    public HistoryRequestDto saveHistory(HistoryRequestDto historyRequestDto, UserDetail userDetail) {

        Long id = userDetail.getId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        History history = historyMapper.toHistory(historyRequestDto);
        history.setUser(user);
        historyRepository.save(history);

        // 해당 사용자의 히스토리 총 개수 확인
        long historyCount = historyRepository.countByUser(user);

        // 히스토리 개수가 최대 개수를 초과하면
        if (historyCount > MAX_HISTORY_COUNT) {
            // 가장 오래된 히스토리 1개를 찾아서
            // 5. 삭제
            historyRepository.findFirstByUserOrderByCreatedAtAsc(user)
                .ifPresent(historyRepository::delete);
        }

        return historyRequestDto;
    }

    public List<HistorySummaryDto> getAllHistory(UserDetail userDetail) {
        Long id = userDetail.getId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        List<History> historyList = historyRepository.findAllByUser(user);
        return historyList.stream()
            .map(historyMapper::toHistorySummary)
            .toList();
    }

    public HistoryDetailDto getHistory(Long historyId, UserDetail userDetail) {
        Long id = userDetail.getId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        History history = historyRepository.findById(historyId)
            .orElseThrow(() -> new CustomException(ErrorCode.HISTORY_NOT_FOUND));

        if (!Objects.equals(history.getUser().getId(), id)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return historyMapper.toHistoryDetailDto(history);
    }
}
