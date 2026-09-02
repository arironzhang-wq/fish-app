package com.yunao.fishing.data

/** 影响个性化预测的单个因子，及其对命中率的历史贡献 */
data class Factor(
    val name: String,
    val currentValue: String,
    val idealRange: String,
    val contributionPercent: Int, // 正数=正向拉高命中率，负数=拉低
    val isFavorable: Boolean
)

data class PersonalForecast(
    val spotName: String,
    val score: Int, // 0-100 专属命中率
    val baselineScore: Int, // 该水域历史平均命中率，用于对比
    val summary: String,
    val factors: List<Factor>
)

data class FishSpecies(
    val name: String,
    val latinName: String
)

data class Gear(
    val category: String, // 竿/线组/饵料/钓法
    val name: String,
    val reason: String
)

data class GearPlan(
    val targetSpecies: String,
    val waterType: String,
    val season: String,
    val items: List<Gear>
)

data class CatchLog(
    val id: Int,
    val date: String,
    val spotName: String,
    val species: String,
    val countAndSize: String,
    val weatherSnapshot: String, // 出钓时天气快照
    val gearUsed: String,
    val note: String
)

data class FishingTrip(
    val id: Int,
    val title: String,
    val spotName: String,
    val dateTime: String,
    val organizer: String,
    val joined: Int,
    val capacity: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val nickname: String,
    val totalWeightKg: Double,
    val badge: String
)

data class Achievement(
    val name: String,
    val description: String,
    val unlocked: Boolean
)

object MockData {

    val personalForecasts = listOf(
        PersonalForecast(
            spotName = "东湖野钓点·3号桩",
            score = 82,
            baselineScore = 58,
            summary = "比通用指数高出 24 分：这类气压+风向组合，是你历史上中鱼率最高的条件之一",
            factors = listOf(
                Factor("气压趋势", "1017 hPa，缓慢上升", "你的最佳区间 1014-1019 hPa", 18, true),
                Factor("风向风力", "东北风 2 级", "你 76% 的好渔获都发生在东北风", 14, true),
                Factor("水温", "21℃", "你的历史最佳区间 19-23℃", 9, true),
                Factor("光照/时段", "阴天，上午 6-9 点", "你在阴天早晨的中鱼率比晴天高 31%", 7, true),
                Factor("近期用饵", "谷物颗粒+腥味小药", "过去 5 次同配方 4 次上鱼", 6, true),
            )
        ),
        PersonalForecast(
            spotName = "南渠黑坑·老坑",
            score = 47,
            baselineScore = 61,
            summary = "低于通用指数：正午强光+无风，历史上不是你在这个坑位的强势时段",
            factors = listOf(
                Factor("光照/时段", "晴天正午", "你在这个坑更适合傍晚", -16, false),
                Factor("风力", "无风", "你偏好 1-2 级微风", -8, false),
                Factor("水情", "刚放鱼 2 天", "通常第 4-6 天开口更好", -5, false),
            )
        )
    )

    val speciesCatalog = listOf(
        FishSpecies("鲫鱼", "Carassius auratus"),
        FishSpecies("鲤鱼", "Cyprinus carpio"),
        FishSpecies("草鱼", "Ctenopharyngodon idella"),
        FishSpecies("翘嘴鲌", "Culter alburnus"),
        FishSpecies("鳜鱼", "Siniperca chuatsi"),
    )

    val gearPlans = listOf(
        GearPlan(
            targetSpecies = "翘嘴鲌",
            waterType = "开阔水库",
            season = "秋季",
            items = listOf(
                Gear("路亚竿", "ML 调 2.1m 快调竿", "适合抛投轻质亮片，手感灵敏能感知截口"),
                Gear("线组", "PE 1.0 + 氟碳前导 20lb", "水库能见度高，氟碳前导更隐蔽"),
                Gear("饵/拟饵", "70mm 沉水铅笔", "模拟受伤小鱼，秋季翘嘴追口积极"),
                Gear("钓法", "抽停+暴力顿口", "间歇性抽停制造逃窜假象，触发攻击反射"),
            )
        ),
        GearPlan(
            targetSpecies = "鲫鱼",
            waterType = "黑坑/静水坑塘",
            season = "冬春季",
            items = listOf(
                Gear("台钓竿", "3.6m 28调硬软",  "坑塘钓鲫兼顾灵敏与控鱼"),
                Gear("线组", "主线1.0+子线0.4", "低温鱼口轻，细线组提高中鱼率"),
                Gear("饵料", "腥香型雾化饵+蚯蚓", "低温期腥味更能刺激开口"),
                Gear("钓法", "钓灵不钓钝，勤逗鱼", "冬春鱼口弱，小幅逗引更易触发"),
            )
        ),
    )

    val recentLogs = listOf(
        CatchLog(1, "09-01", "东湖野钓点·3号桩", "鲫鱼", "6 尾，最大 0.4kg", "阴，21℃，东北风2级，气压1017", "谷物颗粒+腥味小药", "开阔水域下竿，前2小时无口，日出后连口"),
        CatchLog(2, "08-27", "南渠黑坑·老坑", "鲤鱼", "1 尾，1.8kg", "晴，28℃，无风，气压1009", "商品饵·四季诱", "正午强光，鱼口很轻，差点脱钩"),
        CatchLog(3, "08-20", "水库大坝西侧", "翘嘴鲌", "3 尾，合计1.6kg", "多云，24℃，西南风3级", "路亚·沉水铅笔", "傍晚抽停手法，连续两口都在停顿瞬间"),
    )

    val trips = listOf(
        FishingTrip(1, "周六清晨约钓·东湖", "东湖野钓点·3号桩", "本周六 05:30", "老张", 3, 6),
        FishingTrip(2, "路亚水库夜钓", "水库大坝西侧", "本周五 18:00", "阿凯", 2, 4),
        FishingTrip(3, "新手陪钓·黑坑体验", "南渠黑坑·老坑", "本周日 09:00", "渔脑教练", 5, 8),
    )

    val leaderboard = listOf(
        LeaderboardEntry(1, "老张", 18.6, "本周渔获王"),
        LeaderboardEntry(2, "阿凯", 14.2, "路亚达人"),
        LeaderboardEntry(3, "你", 11.8, "稳步上升"),
        LeaderboardEntry(4, "小渔", 9.4, ""),
        LeaderboardEntry(5, "风筝", 7.1, ""),
    )

    val achievements = listOf(
        Achievement("初次出钓", "完成第一次出钓记录", true),
        Achievement("数据积累者", "累计记录 10 次出钓", true),
        Achievement("五连胜", "连续 5 次出钓均有渔获", false),
        Achievement("大物猎人", "单尾渔获超过 2kg", false),
    )
}
