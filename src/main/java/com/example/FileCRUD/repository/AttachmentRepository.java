package com.example.FileCRUD.repository;

import com.example.FileCRUD.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
    Optional<Attachment> findByNameAndSizeAndContextType(String originalFilename, long size, String contentType);

    List<Attachment> findAllByNameContaining(String name);

    List<Attachment> findAllBySizeLessThan(long size);

    List<Attachment> findAllBySizeGreaterThan(long size);

}
