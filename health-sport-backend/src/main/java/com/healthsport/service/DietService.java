package com.healthsport.service;

import com.healthsport.dto.DietRecordSaveDTO;
import com.healthsport.entity.DietRecord;
import com.healthsport.entity.FoodDict;

import java.time.LocalDate;
import java.util.List;

// DietService：服务接口，先把能力定义清楚
public interface DietService {

    DietRecord saveRecord(DietRecordSaveDTO dto);

    List<DietRecord> listByDate(LocalDate date);

    List<FoodDict> searchFood(String keyword);
}

