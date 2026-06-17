package com.back.baton.domain.talent.repository;

import com.back.baton.domain.talent.entity.Talent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TalentRepository extends JpaRepository<Talent, Long> {}