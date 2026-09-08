package com.kh.wellness.place.model.dto;

import lombok.Data;

@Data
public class PlaceImageLicenseDto {
    private String sourceName;
    private String sourcePageUrl;
    private String authorName;
    private String licenseCode;
    private String licenseUrl;
    private String attributionText;
}
