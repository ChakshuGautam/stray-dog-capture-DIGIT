package org.digit.sdcrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Evidence - Photo/document evidence attached to a dog report.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evidence {

    @JsonProperty("id")
    private String id;

    @NotNull
    @Size(max = 64)
    @JsonProperty("fileStoreId")
    private String fileStoreId;

    @NotNull
    @JsonProperty("evidenceType")
    private EvidenceType evidenceType;

    @Size(max = 256)
    @JsonProperty("fileName")
    private String fileName;

    @Size(max = 64)
    @JsonProperty("mimeType")
    private String mimeType;

    @JsonProperty("fileSize")
    private Long fileSize;

    // Image hash for duplicate detection (pHash)
    @Size(max = 64)
    @JsonProperty("imageHash")
    private String imageHash;

    // EXIF metadata
    @JsonProperty("captureTime")
    private Long captureTime;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @Size(max = 128)
    @JsonProperty("deviceInfo")
    private String deviceInfo;

    @JsonProperty("uploadedAt")
    private Long uploadedAt;

    @Size(max = 64)
    @JsonProperty("uploadedBy")
    private String uploadedBy;

    public enum EvidenceType {
        DOG_PHOTO,       // Photo of the stray dog
        REPORTER_SELFIE, // Selfie of reporter at location
        CAPTURE_PHOTO,   // Photo after capture by MC officer
        OTHER
    }
}
