package com.healthsport.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.healthsport.dto.HealthRecordSaveDTO;
import com.healthsport.entity.HealthRecord;

// HealthRecordService：服务接口，先把能力定义清楚
public interface HealthRecordService {

    HealthRecord save(HealthRecordSaveDTO dto);

    IPage<HealthRecord> pageCurrentUser(long current, long size);

    HealthRecord latest();

    HealthRecord update(Long id, HealthRecordSaveDTO dto);

    void delete(Long id);
}

