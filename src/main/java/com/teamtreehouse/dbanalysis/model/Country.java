package com.teamtreehouse.dbanalysis.model;

import javax.persistence.*;

@Entity
public class Country {
    @Id
    private String code;

    @Column
    private String name;

    @Column
    private Double internetUsers;

    @Column
    private Double adultLiteracyRate;

    public Country() {}

    public Country(CountryBuilder builder) {
        this.code = builder.code;
        this.name = builder.name;
        this.internetUsers = builder.internetUsers;
        this.adultLiteracyRate = builder.adultLiteracyRate;
    }

    @Override
    public String toString() {
        return "Country{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", internetUsers=" + internetUsers +
                ", adultLiteracyRate=" + adultLiteracyRate +
                '}';
    }

    public String getCode() {
        return code;
    }


    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getInternetUsers() {
        return internetUsers;
    }

    public void setInternetUsers(Double internetUsers) {
        this.internetUsers = internetUsers;
    }

    public Double getAdultLiteracyRate() {
        return adultLiteracyRate;
    }

    public void setAdultLiteracyRate(Double adultLiteracyRate) {
        this.adultLiteracyRate = adultLiteracyRate;
    }

    public static class CountryBuilder {
        private String code;
        private String name;
        private double internetUsers;
        private double adultLiteracyRate;

        public CountryBuilder(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public CountryBuilder withInternetUsers(double internetUsers) {
            this.internetUsers = internetUsers;
            return this;
        }

        public CountryBuilder withAdultLiteracyRate(double adultLiteracyRate) {
            this.adultLiteracyRate = adultLiteracyRate;
            return this;
        }

        public Country build() {
            return new Country(this);
        }
    }
}
