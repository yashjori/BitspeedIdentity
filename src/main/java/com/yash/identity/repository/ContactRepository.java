package com.yash.identity.repository;

import com.yash.identity.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByEmailOrPhoneNumberAndDeletedAtIsNull(String email, String phoneNumber);

    List<Contact> findByLinkedIdAndDeletedAtIsNull(Long linkedId);
}