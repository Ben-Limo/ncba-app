package com.ncba.api;

import jakarta.validation.constraints.NotBlank;

public record CountryInfoRequest(
        @NotBlank(message = "name is required")
        String name
) {
}
