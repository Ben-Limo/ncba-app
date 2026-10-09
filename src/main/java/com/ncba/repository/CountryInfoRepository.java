package com.ncba.repository;

import com.ncba.model.CountryInfo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface CountryInfoRepository extends JpaRepository<CountryInfo, Long> {
    @EntityGraph(attributePaths = "languages")
    Optional<CountryInfo> findById(Long Id);

    @EntityGraph(attributePaths = "languages")
    List<CountryInfo> findAll();
}
