package com.library.repository;

import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByMemberId(Long memberId);

    long countByMemberIdAndStatus(Long memberId, BorrowStatus status);

    Optional<BorrowRecord> findByBookIdAndStatus(Long bookId, BorrowStatus status);
}