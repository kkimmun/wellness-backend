package com.kh.wellness.sensor.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SensorResponseDto {
	private Long sensorNo;
	private Long infoNo;
	private Long stepCount;
	private LocalDateTime collectTime;
}
