package moe.sdl.tracks.enums

/**
 * @property EXACT 精确匹配清晰度/码率
 * @property NEAR_UP 优先精确匹配, 无时使用临近中质量较高的
  * @property NEAR_DOWN 优先精确匹配, 无时使用临近中质量较低的
 */
enum class QualityStrategy {
    EXACT,
    NEAR_UP,
    NEAR_DOWN,
    ;
}
