package org.digit.sdcrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Location - Geographic location details for a dog report.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @JsonProperty("id")
    private String id;

    @NotNull
    @JsonProperty("latitude")
    private BigDecimal latitude;

    @NotNull
    @JsonProperty("longitude")
    private BigDecimal longitude;

    @Size(max = 512)
    @JsonProperty("address")
    private String address;

    @Size(max = 64)
    @JsonProperty("locality")
    private String locality;

    @Size(max = 64)
    @JsonProperty("localityCode")
    private String localityCode;

    @Size(max = 64)
    @JsonProperty("ward")
    private String ward;

    @Size(max = 64)
    @JsonProperty("wardCode")
    private String wardCode;

    @Size(max = 64)
    @JsonProperty("district")
    private String district;

    @Size(max = 64)
    @JsonProperty("districtCode")
    private String districtCode;

    @Size(max = 64)
    @JsonProperty("city")
    private String city;

    @Size(max = 10)
    @JsonProperty("pincode")
    private String pincode;

    // GPS metadata from EXIF
    @JsonProperty("gpsLatitude")
    private BigDecimal gpsLatitude;

    @JsonProperty("gpsLongitude")
    private BigDecimal gpsLongitude;

    @JsonProperty("gpsAccuracy")
    private BigDecimal gpsAccuracy;

    @JsonProperty("gpsTimestamp")
    private Long gpsTimestamp;

    // Flag indicating if GPS was validated
    @JsonProperty("gpsValidated")
    private Boolean gpsValidated;

    @JsonProperty("gpsValidationResult")
    private String gpsValidationResult;  // MATCH, MISMATCH, NOT_AVAILABLE
}
