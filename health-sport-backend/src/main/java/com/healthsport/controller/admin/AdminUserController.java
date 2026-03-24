package com.healthsport.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthsport.dto.AdminUserAiLimitUpdateDTO;
import com.healthsport.dto.AdminUserStatusUpdateDTO;
import com.healthsport.entity.User;
import com.healthsport.exception.BusinessException;
import com.healthsport.mapper.UserMapper;
import com.healthsport.utils.Result;
import com.healthsport.vo.AdminUserVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
// AdminUserController：管理端接口，处理后台相关请求
public class AdminUserController {

    private final UserMapper userMapper;

    // 构造方法：把依赖先注入进来
    public AdminUserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping
    public Result<IPage<AdminUserVO>> pageUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page必须>=1") long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size必须>=1") long size) {

        Page<User> pageParam = new Page<>(page, size);
        IPage<User> userPage = userMapper.selectPage(pageParam,
                new LambdaQueryWrapper<User>()
                        .like(keyword != null && !keyword.isBlank(), User::getUsername, keyword)
                        .orderByDesc(User::getCreateTime)
                        .orderByDesc(User::getId));

        IPage<AdminUserVO> voPage = userPage.convert(user -> AdminUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .age(user.getAge())
                .role(user.getRole())
                .status(user.getStatus())
                .aiDailyLimit(user.getAiDailyLimit())
                .createTime(user.getCreateTime())
                .build());

        return Result.success(voPage);
    }

    @PutMapping("/{id}/status")
    // 更新Status，先校验再落库
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminUserStatusUpdateDTO dto) {
        User user = userMapper.selectById(id);
        // 这里判断一下空值，避免后面空指针
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(dto.getStatus());
        userMapper.updateById(user);
        return Result.success(null);
    }

    @PutMapping("/{id}/ai-limit")
    public Result<Void> updateAiLimit(@PathVariable Long id, @Valid @RequestBody AdminUserAiLimitUpdateDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BusinessException(400, "管理员用户默认无限次，无需单独设置 AI 次数");
        }

        user.setAiDailyLimit(dto.getAiDailyLimit());
        userMapper.updateById(user);
        return Result.success(null);
    }
}

