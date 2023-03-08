package com.freegang.xpler.utils.other


/// 简化循环
fun Int.forTo(to: Int, both: Boolean = false, block: (index: Int) -> Unit) {
    //10 -> 0
    if (to < this) {
        val finalTo = if (both) to else to + 1
        for (i in this downTo finalTo) block.invoke(i)
    } else { // 0 -> 10
        val finalTo = if (both) to else to - 1
        for (i in this..finalTo) block.invoke(i)
    }
}

/// 简化计算
fun <T> Int.forCalc(from: Int, initValue: T, both: Boolean = false, block: (previous: T) -> T): T {
    var result: T = initValue

    if (both) {
        for (i in from..this) {
            result = block.invoke(result)
        }
        return result
    }

    for (i in from until this) {
        result = block.invoke(result)
    }
    return result
}

