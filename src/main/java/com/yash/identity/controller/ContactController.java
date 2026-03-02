package com.yash.identity.controller;

import com.yash.identity.dto.*;
import com.yash.identity.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/identify")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService service;

    @PostMapping
    public IdentifyResponse identify(@RequestBody IdentifyRequest request) {
        return service.identify(request);
    }
}