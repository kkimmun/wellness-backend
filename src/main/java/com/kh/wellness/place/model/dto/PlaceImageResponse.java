package com.kh.wellness.place.model.dto;

import org.apache.ibatis.type.Alias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Alias("PublicPlaceImageResponse")
public class PlaceImageResponse {

    private Integer imgOrder;
    private String imageUrl;
}
