package com.ncba.services;

import com.ncba.api.UpdateCountryRequest;
import com.ncba.api.UserResponse;
import com.ncba.cis.soap.client.CountryISOCodeResponse;
import com.ncba.cis.soap.client.FullCountryInfo;
import com.ncba.cis.soap.client.FullCountryInfoResponse;
import com.ncba.cis.soap.client.TCountryInfo;
import com.ncba.configs.SoapClient;
import com.ncba.dto.CountryInfoDto;
import com.ncba.exception.CountryNotFoundException;
import com.ncba.model.CountryInfo;
import com.ncba.model.Language;
import com.ncba.repository.CountryInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;

@Service
public class CountryInfoServiceImpl {
    private final SoapClient soapClient;
    private final CountryInfoRepository repository;
    public CountryInfoServiceImpl(SoapClient soapClient, CountryInfoRepository repository) {
        this.soapClient = soapClient;
        this.repository = repository;
    }
    public TCountryInfo processCountryName(String country) {
        CountryISOCodeResponse soap = soapClient.getCountryISOCode(StringUtils.capitalize(country));
        FullCountryInfoResponse countryInfo = processCountryCode(soap.getCountryISOCodeResult());
        TCountryInfo t = countryInfo.getFullCountryInfoResult();
        CountryInfo c = new CountryInfo();
        c.setIsoCode(t.getSISOCode());
        c.setName(t.getSName());
        c.setCapitalCity(t.getSCapitalCity());
        c.setPhoneCode(Integer.parseInt(t.getSPhoneCode()));
        c.setContinentCode(t.getSContinentCode());
        c.setCurrencyIsoCode(t.getSCurrencyISOCode());
        c.setCountryFlag(t.getSCountryFlag());
        t.getLanguages().getTLanguage().forEach(lang -> {
            Language l = new Language();
            l.setIsoCode(lang.getSISOCode());
            l.setName(lang.getSName());
            c.addLanguage(l);
        });
        CountryInfo saved = repository.save(c);
        System.out.println("country id " + saved.getId());
        return t;
    }

    public FullCountryInfoResponse processCountryCode(String isoCode) {
        return soapClient.getFullCountryInfo(isoCode);
    }

    @Transactional(readOnly = true)
    public CountryInfoDto findById(Long id) {
        return repository.findById(id)
                .map(CountryInfoDto::from)
                .orElseThrow(() -> new CountryNotFoundException(id));
    }

    @Transactional
    public CountryInfoDto update(Long id, UpdateCountryRequest req) {
        CountryInfo country = repository.findById(id)
                .orElseThrow(() -> new CountryNotFoundException(id));

        if (!req.name().isBlank()) {country.setName(req.name());}
        if (req.phoneCode() != null && req.phoneCode() != country.getPhoneCode()) {country.setPhoneCode(req.phoneCode());}
        if (req.capitalCity() != null && !req.capitalCity().isBlank()) {country.setCapitalCity(req.capitalCity());}
        if (req.continentCode() != null && !req.continentCode().isBlank()) {country.setContinentCode(req.continentCode());}
        if (req.currencyIsoCode() != null && !req.currencyIsoCode().isBlank()) {country.setCurrencyIsoCode(req.currencyIsoCode());}
        if (req.countryFlag() != null && !req.countryFlag().isBlank()) {country.setCountryFlag(req.countryFlag());}

        if (req.languages() != null && req.languages().size() > 0) {
            country.getLanguages().clear();

            req.languages().forEach(l -> {
                Language lang = new Language();
                lang.setIsoCode(l.isoCode());
                lang.setName(l.name());
                country.addLanguage(lang);
            });
        }

        repository.save(country);
        return CountryInfoDto.from(country);   // managed entity: changes are flushed on commit
    }

    @Transactional
    public void delete(Long id) {
        CountryInfo country = repository.findById(id)
                .orElseThrow(() -> new CountryNotFoundException(id));
        repository.delete(country);
    }

}
