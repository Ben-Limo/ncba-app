package com.ncba.api;

import com.ncba.dto.LanguageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateCountryRequest(
        @NotBlank(message = "name is required")
        String name,
        String capitalCity, Integer phoneCode, String continentCode,
        String currencyIsoCode, String countryFlag,
        List<@Valid LanguageDto> languages
) {
}
