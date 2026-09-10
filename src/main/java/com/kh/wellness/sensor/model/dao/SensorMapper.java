package com.kh.wellness.sensor.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kh.wellness.sensor.model.dto.SensorRequestDto;
import com.kh.wellness.sensor.model.dto.SensorResponseDto;

@Mapper
public interface SensorMapper {
	
	List<SensorResponseDto> sensorInfoRequest(Long memberNo);

	int insertSensorData(SensorRequestDto sensor);

	int sensorCleanup();

}