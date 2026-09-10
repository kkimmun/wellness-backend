package com.kh.wellness.sensor.model.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.sensor.model.dao.SensorMapper;
import com.kh.wellness.sensor.model.dto.SensorRequestDto;
import com.kh.wellness.sensor.model.dto.SensorResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SensorService {
	private final SensorMapper sensorMapper;
	
	public List<SensorResponseDto> sensorInfoRequest(Long memberNo) {
		List<SensorResponseDto> sensorInfoList = sensorMapper.sensorInfoRequest(memberNo);
		
		if(sensorInfoList == null) {
			throw new BadRequestException("센서 정보 조회에 실패하였습니다, 잠시후 다시 시도해주세요.");
		}
		
		return sensorInfoList;
	}

	public void insertSensorData(SensorRequestDto sensor) {
		int result = sensorMapper.insertSensorData(sensor);
		
		if(result < 1) {
			throw new BadRequestException("센서와 통신에 실패하였습니다, 잠시후 다시 시도해주세요.");
		}
		
	}
	
	//00시 도래 시 전일 만보기 기록은 파기
	@Transactional
    @Scheduled(cron = "0 00 00 * * *")
    public void sensorCleanup() {

        int result = sensorMapper.sensorCleanup();

        if (result > 0) {
            log.info("만료된 인증 이메일 정리 완료: {}건 삭제됨", result);
        } else {
            log.info("정리할 만료 이메일이 없습니다.");
        }
    }

}
