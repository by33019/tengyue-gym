package com.gym.coze;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.entity.CheckIn;
import com.gym.entity.User;
import com.gym.mapper.CheckInMapper;
import com.gym.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContextBuilderImpl implements ContextBuilder {

    private final UserMapper userMapper;
    private final CheckInMapper checkInMapper;

    @Override
    public String buildContext(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("【用户身体数据】\n");
        sb.append("- 身高：").append(user.getHeight() != null ? user.getHeight() + "cm" : "未填写").append("\n");
        sb.append("- 体重：").append(user.getWeight() != null ? user.getWeight() + "kg" : "未填写").append("\n");
        sb.append("- 健身目标：").append(user.getFitnessGoal() != null ? user.getFitnessGoal() : "未填写").append("\n");
        sb.append("- 运动等级：").append(user.getFitnessLevel() != null ? user.getFitnessLevel() : "未填写").append("\n");

        List<CheckIn> recentCheckIns = checkInMapper.selectList(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .ge(CheckIn::getCheckInTime, LocalDate.now().minusDays(7).atStartOfDay())
                        .orderByDesc(CheckIn::getCheckInTime));

        sb.append("\n【近7日运动记录】\n");
        if (recentCheckIns.isEmpty()) {
            sb.append("暂无运动记录\n");
        } else {
            var grouped = recentCheckIns.stream()
                    .collect(Collectors.groupingBy(
                            c -> c.getCheckInTime().toLocalDate(),
                            Collectors.toList()));
            grouped.forEach((date, list) -> {
                String items = list.stream()
                        .map(c -> c.getExerciseType() + " " + c.getDurationMinutes() + "分钟 " + c.getCalories() + "kcal")
                        .collect(Collectors.joining("; "));
                sb.append("- ").append(date).append("：").append(items).append("\n");
            });
        }

        return sb.toString();
    }
}
