package moe.sdl.tracks.enums

/**
 * @property EXACT 精确匹配清晰度/码率
 * @property NEAR_UP 优先精确匹配, 无时使用临近中质量较高的, 若仍无, 选择所有轨道中质量最高的
 * @property NEAR_DOWN 优先精确匹配, 无时使用临近中质量较低的, 若仍无, 选择所有轨道中质量最高的
 */
enum class QualityStrategy {
    EXACT,
    NEAR_UP,
    NEAR_DOWN,
    ;
}
