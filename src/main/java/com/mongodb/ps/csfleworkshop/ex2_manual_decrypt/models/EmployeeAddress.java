package com.mongodb.ps.csfleworkshop.ex2_manual_decrypt.models;

public class EmployeeAddress {
    private String streetAddress;
    private String suburbCounty;
    private String zipPostcode;
    private String stateProvince;
    private String country;

    public EmployeeAddress() {
    }

    public EmployeeAddress(String streetAddress, String suburbCounty, String zipPostcode, String stateProvince,
            String country) {
        this.streetAddress = streetAddress;
        this.suburbCounty = suburbCounty;
        this.zipPostcode = zipPostcode;
        this.stateProvince = stateProvince;
        this.country = country;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getSuburbCounty() {
        return suburbCounty;
    }

    public void setSuburbCounty(String suburbCounty) {
        this.suburbCounty = suburbCounty;
    }

    public String getZipPostcode() {
        return zipPostcode;
    }

    public void setZipPostcode(String zipPostcode) {
        this.zipPostcode = zipPostcode;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "EmployeeAddress [streetAddress=" + streetAddress + ", suburbCounty=" + suburbCounty + ", zipPostcode="
                + zipPostcode + ", stateProvince=" + stateProvince + ", country=" + country + "]";
    }
}
