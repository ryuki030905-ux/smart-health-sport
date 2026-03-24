package com.healthsport.service;

import com.healthsport.dto.ExercisePlanSaveDTO;
import com.healthsport.dto.ExerciseRecordSaveDTO;
import com.healthsport.entity.ExerciseDict;
import com.healthsport.entity.ExercisePlan;
import com.healthsport.entity.ExerciseRecord;
import com.healthsport.vo.ExercisePlanVO;

import java.time.LocalDate;
import java.util.List;

// ExerciseService：服务接口，先把能力定义清楚
public interface ExerciseService {

    ExerciseRecord saveRecord(ExerciseRecordSaveDTO dto);

    List<ExerciseRecord> listRecordsByDate(LocalDate date);

    List<ExerciseDict> listExerciseDict();

    ExercisePlan createPlan(ExercisePlanSaveDTO dto);

    List<ExercisePlanVO> listPlans();
}

