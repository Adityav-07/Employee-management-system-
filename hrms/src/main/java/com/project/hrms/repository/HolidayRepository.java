package com.project.hrms.repository;

import com.project.hrms.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/*public interface HolidayRepository extends JpaRepository<Holiday, Integer> {

    boolean existsByDate(LocalDate date);

    long countByDateBetween(LocalDate start, LocalDate end);
}*/


public interface HolidayRepository extends JpaRepository<Holiday, Integer> {

    boolean existsByDate(LocalDate date);

    long countByDateBetween(LocalDate start, LocalDate end);

    List<Holiday> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);
}