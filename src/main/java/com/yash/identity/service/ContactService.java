package com.yash.identity.service;

import com.yash.identity.dto.*;
import com.yash.identity.entity.Contact;
import com.yash.identity.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service

public class ContactService {

    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
		super();
		this.repository = repository;
	}

	@Transactional
    public IdentifyResponse identify(IdentifyRequest request) {

        String email = request.getEmail();
        String phone = request.getPhoneNumber();

        List<Contact> matched =
                repository.findByEmailOrPhoneNumberAndDeletedAtIsNull(email, phone);

        if (matched.isEmpty()) {
            Contact newContact = repository.save(
                    Contact.builder()
                            .email(email)
                            .phoneNumber(phone)
                            .linkPrecedence("primary")
                            .build()
            );

            return buildResponse(newContact, List.of(newContact));
        }

        Set<Contact> allContacts = new HashSet<>(matched);

        for (Contact c : matched) {
            if (c.getLinkedId() != null) {
                repository.findById(c.getLinkedId()).ifPresent(allContacts::add);
            }
            allContacts.addAll(repository.findByLinkedIdAndDeletedAtIsNull(c.getId()));
        }

        Contact primary = allContacts.stream()
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow();

        for (Contact c : allContacts) {
            if ("primary".equals(c.getLinkPrecedence())
                    && !c.getId().equals(primary.getId())) {

                c.setLinkPrecedence("secondary");
                c.setLinkedId(primary.getId());
                repository.save(c);
            }
        }

        Set<String> emails = allContacts.stream()
                .map(Contact::getEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> phones = allContacts.stream()
                .map(Contact::getPhoneNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if ((email != null && !emails.contains(email))
                || (phone != null && !phones.contains(phone))) {

            Contact secondary = repository.save(
                    Contact.builder()
                            .email(email)
                            .phoneNumber(phone)
                            .linkedId(primary.getId())
                            .linkPrecedence("secondary")
                            .build()
            );

            allContacts.add(secondary);
        }

        return buildResponse(primary, new ArrayList<>(allContacts));
    }

    private IdentifyResponse buildResponse(Contact primary, List<Contact> contacts) {

        List<String> emails = contacts.stream()
                .map(Contact::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> phones = contacts.stream()
                .map(Contact::getPhoneNumber)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> secondaryIds = contacts.stream()
                .filter(c -> "secondary".equals(c.getLinkPrecedence()))
                .map(Contact::getId)
                .collect(Collectors.toList());

        return IdentifyResponse.builder()
                .contact(
                        IdentifyResponse.ContactData.builder()
                                .primaryContactId(primary.getId())
                                .emails(emails)
                                .phoneNumbers(phones)
                                .secondaryContactIds(secondaryIds)
                                .build()
                )
                .build();
    }
}