package com.healthsport.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 一周运动时长图返回的数据。
// 主要是一周 7 天和每天对应的运动分钟数。
public class WeeklyExerciseVO {

    private List<String> xAxis;

    private List<Integer> series;
}

