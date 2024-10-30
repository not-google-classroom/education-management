package com.org.education_management.repository;

import com.org.education_management.model.OrgDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgDetailsRepository extends JpaRepository<OrgDetails, Long> {
    @Override
    OrgDetails getReferenceById(Long aLong);
}

