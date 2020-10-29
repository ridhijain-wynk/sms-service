package in.wynk.sms.constants;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public enum Country {

    SRILANKA("+94", "SL", "SRILANKA"), INDIA("+91", "IN", "INDIA");

    public static HashMap<String, String> countryNameToCode = new HashMap<>();
    public static HashMap<String, Country> countryCodeToCountry = new HashMap<>();
    public static HashMap<String, Country> countryIdToCountry = new HashMap<>();

    static {
        for (Country country : Country.values()) {
            countryNameToCode.put(country.getCountryName(), country.getCountryCode());
            countryCodeToCountry.put(country.getCountryCode(), country);
            countryIdToCountry.put(country.getCountryId(), country);
        }
    }

    private String countryCode;
    private String countryId;
    private String countryName;

    Country(String countryCode, String countryId, String countryName) {
        this.countryCode = countryCode;
        this.countryId = countryId;
        this.countryName = countryName;
    }

    public static Country getCountryByCountryId(String countryId) {
        if (StringUtils.isNotEmpty(countryId)) {
            Country country = countryIdToCountry.get(countryId);
            if (country != null) {
                return country;
            }

        }
        return Country.INDIA;
    }

    public static Country getCountryByCountryCode(String countryCode) {
        if (StringUtils.isNotEmpty(countryCode)) {
            Country country = countryCodeToCountry.get(countryCode);
            if (country != null) {
                return country;
            }
        }
        return Country.INDIA;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}


