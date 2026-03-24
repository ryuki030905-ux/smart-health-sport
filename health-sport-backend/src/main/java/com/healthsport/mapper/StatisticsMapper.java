package com.healthsport.mapper;

import com.healthsport.mapper.model.DateDecimalItem;
import com.healthsport.mapper.model.DateIntegerItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
// 图表统计专门用的 Mapper。
// 和普通增删改查不太一样，这里更多是在查按天汇总好的结果。
public interface StatisticsMapper {

    @Select("""
            SELECT record_date AS statDate, weight AS value
            FROM health_record
            WHERE user_id = #{userId}
              AND record_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)
            ORDER BY record_date ASC
            """)
    List<DateDecimalItem> selectWeightTrend(@Param("userId") Long userId, @Param("days") Integer days);

    @Select("""
            SELECT diet_date AS statDate, SUM(calories) AS value
            FROM diet_record
            WHERE user_id = #{userId}
              AND diet_date BETWEEN #{startDate} AND #{endDate}
            GROUP BY diet_date
            ORDER BY diet_date ASC
            """)
    List<DateDecimalItem> selectIntakeByDate(@Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT exercise_date AS statDate, SUM(calories_burned) AS value
            FROM exercise_record
            WHERE user_id = #{userId}
              AND exercise_date BETWEEN #{startDate} AND #{endDate}
            GROUP BY exercise_date
            ORDER BY exercise_date ASC
            """)
    List<DateDecimalItem> selectBurnedByDate(@Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT exercise_date AS statDate, SUM(duration_min) AS value
            FROM exercise_record
            WHERE user_id = #{userId}
              AND exercise_date BETWEEN #{startDate} AND DATE_ADD(#{startDate}, INTERVAL 6 DAY)
            GROUP BY exercise_date
            ORDER BY exercise_date ASC
            """)
    List<DateIntegerItem> selectWeeklyExerciseDuration(@Param("userId") Long userId,
                                                       @Param("startDate") LocalDate startDate);
}

