package com.ncba.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "languages")
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String isoCode;
    @Column(nullable = false)
    private String name;

    // INVERSE side: mappedBy points to the field name in Country
    @ManyToMany(mappedBy = "languages")
    private Set<CountryInfo> countries = new HashSet<>();

    public Long getId() { return id; }

    public String getIsoCode() { return isoCode; }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Language other && isoCode != null && isoCode.equals(other.isoCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isoCode);
    }

    public Set<CountryInfo> getCountries() {
        return countries;
    }
}
