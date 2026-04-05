package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.FinancialRecordRequest;
import com.zorvyn.finance.dto.FinancialRecordResponse;
import com.zorvyn.finance.exception.ResourceNotFoundException;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.TransactionType;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import com.zorvyn.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getAllRecords(
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return recordRepository.findAllWithFilters(type, category, startDate, endDate, pageable)
                .map(FinancialRecordResponse::fromRecord);
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecordById(Long id) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record", id));
        return FinancialRecordResponse.fromRecord(record);
    }

    @Transactional
    public FinancialRecordResponse createRecord(FinancialRecordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .build();

        record = recordRepository.save(record);
        log.info("Financial record created: id={}, type={}, amount={}, by={}",
                record.getId(), record.getType(), record.getAmount(), email);
        return FinancialRecordResponse.fromRecord(record);
    }

    @Transactional
    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record", id));

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());

        record = recordRepository.save(record);
        log.info("Financial record updated: id={}", id);
        return FinancialRecordResponse.fromRecord(record);
    }

    @Transactional
    public void deleteRecord(Long id) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record", id));
        // Soft delete
        record.setDeleted(true);
        recordRepository.save(record);
        log.info("Financial record soft-deleted: id={}", id);
    }
}
