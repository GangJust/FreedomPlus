package com.freegang.xpler.utils.collection

object CollectionUtils {

}

//某个数字是否在集合索引范围内
fun Collection<*>.inIndex(index: Int): Boolean {
    return (index >= 0) and (index < this.size)
}

//某个数字是否在数组索引范围内
fun Array<*>.inIndex(index: Int): Boolean {
    return (index >= 0) and (index < this.size)
}