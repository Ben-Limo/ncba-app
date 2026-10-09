package com.ncba.controller;

import com.ncba.api.CountryInfoRequest;
import com.ncba.api.UpdateCountryRequest;
import com.ncba.cis.soap.client.TCountryInfo;
import com.ncba.dto.CountryInfoDto;
import com.ncba.services.CountryInfoServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/country")
public class CountryInfoServiceController {
    private final CountryInfoServiceImpl service;

    public CountryInfoServiceController(CountryInfoServiceImpl service) {
        this.service = service;
    }
    @PostMapping(value = "/info", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TCountryInfo> get(@Valid @RequestBody CountryInfoRequest request) {
        return ResponseEntity.ok(service.processCountryName(request.name()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryInfoDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CountryInfoDto> updateById(@PathVariable Long id, @Valid @RequestBody UpdateCountryRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
