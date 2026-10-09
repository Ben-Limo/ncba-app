package com.ncba.dto;

import com.ncba.model.CountryInfo;
import com.ncba.model.Language;

import java.util.Comparator;
import java.util.List;

public record CountryInfoDto (
        String isoCode, String name, String capitalCity, Integer phoneCode, String continentCode,
        String currencyIsoCode, String countryFlag, List<LanguageDto> languages
){
    public static CountryInfoDto from(CountryInfo c) {
        return new CountryInfoDto(c.getIsoCode(), c.getName(), c.getCapitalCity(), c.getPhoneCode(), c.getContinentCode(),
                c.getCurrencyIsoCode(), c.getCountryFlag(),
                c.getLanguages().stream().map(LanguageDto::from).sorted(Comparator.comparing(LanguageDto::name)).toList());
    }

    public record LanguageDto(String isoCode, String name) {
        public static LanguageDto from(Language l) {
            return new LanguageDto(l.getIsoCode(), l.getName());
        }
    }
}
