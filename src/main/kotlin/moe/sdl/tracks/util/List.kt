package moe.sdl.tracks.util

inline fun <T1, T2> List<T1>.firstOrNullMatched(
    priorityList: List<T2>,
    predicate: (i: T1, priority: T2) -> Boolean,
): T1? {
    priorityList.forEach { t2 ->
        val t1Matched = this.firstOrNull { t1 ->
            predicate(t1, t2)
        }
        if (t1Matched != null) return t1Matched
    }
    return null
}
