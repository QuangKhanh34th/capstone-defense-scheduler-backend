package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.response.DefenseDayResponse;
import com.capstone.scheduler.entity.DefenseDay;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.repository.DefenseDayRepository;
import com.capstone.scheduler.repository.DefenseRoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefenseDayService {

    private final DefenseDayRepository defenseDayRepository;
    private final DefenseRoundRepository defenseRoundRepository;

    @Transactional(readOnly = true)
    public List<DefenseDayResponse> getAllDefenseDays(Integer roundId) {
        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Round not found with ID: " + roundId));

        List<DefenseDay> defenseDays = defenseDayRepository.findByDefenseRound_RoundIdOrderByDefenseDateAsc(roundId);

        return defenseDays.stream()
                .map(day -> DefenseDayResponse.builder()
                        .dayId(day.getDayId())
                        .defenseDate(day.getDefenseDate())
                        .roundId(round.getRoundId())
                        .roundName(round.getRoundName())
                        .build())
                .collect(Collectors.toList());
    }
}