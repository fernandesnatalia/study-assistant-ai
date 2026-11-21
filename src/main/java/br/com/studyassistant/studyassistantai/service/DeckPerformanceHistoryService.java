package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.DeckPerformanceHistoryResponse;
import br.com.studyassistant.studyassistantai.enums.DeckMode;
import br.com.studyassistant.studyassistantai.model.DeckPerformanceHistory;
import br.com.studyassistant.studyassistantai.repository.DeckPerformanceHistoryRepository;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class DeckPerformanceHistoryService {

    private final DeckPerformanceHistoryRepository historyRepository;
    private final DeckRepository deckRepository;

    public DeckPerformanceHistoryService(
            DeckPerformanceHistoryRepository historyRepository,
            DeckRepository deckRepository
    ) {
        this.historyRepository = historyRepository;
        this.deckRepository = deckRepository;
    }

    @Transactional(readOnly = true)
    public List<DeckPerformanceHistoryResponse> findAll(Long userId) {
        return historyRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeckPerformanceHistoryResponse findById(Long id, Long userId) {
        return toResponse(getExistingHistory(id, userId));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        DeckPerformanceHistory history = getExistingHistory(id, userId);

        deckRepository.findByIdAndUserId(
                history.getOriginalDeckId(),
                userId
        ).ifPresent(deck -> {
            if (deck.getMode() == DeckMode.TRAINING) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Cannot delete performance history of an active TRAINING deck"
                );
            }
        });

        historyRepository.delete(history);
    }

    private DeckPerformanceHistory getExistingHistory(Long id, Long userId
    ) {
        return historyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performance history not found"));
    }

    private DeckPerformanceHistoryResponse toResponse(DeckPerformanceHistory history) {
        return new DeckPerformanceHistoryResponse(
                history.getId(),
                history.getOriginalDeckId(),
                history.getTheme(),
                history.getTotalAttempts(),
                history.getCorrectAnswers(),
                history.getIncorrectAnswers(),
                history.getAccuracyPercentage()
        );
    }
}