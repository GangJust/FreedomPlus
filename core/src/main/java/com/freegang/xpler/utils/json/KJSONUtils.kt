package com.freegang.xpler.utils.json

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface JSONObjectMapFunction {
    fun invoke(entry: Map.Entry<String, Any>)
}

object KJSONUtils {

    /// Json ///
    @JvmStatic
    fun parse(json: String): JSONObject {
        return try {
            JSONObject(json)
        } catch (e: JSONException) {
            JSONObject()
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getString(json: JSONObject, key: String, default: String = ""): String {
        return try {
            json.getString(key)
        } catch (e: JSONException) {
            default
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getInt(json: JSONObject, key: String, default: Int = 0): Int {
        return try {
            json.getInt(key)
        } catch (e: JSONException) {
            default
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getLong(json: JSONObject, key: String, default: Long = 0L): Long {
        return try {
            json.getLong(key)
        } catch (e: JSONException) {
            default
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getDouble(json: JSONObject, key: String, default: Double = 0.0): Double {
        return try {
            json.getDouble(key)
        } catch (e: JSONException) {
            default
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getBoolean(json: JSONObject, key: String, default: Boolean = false): Boolean {
        return try {
            json.getBoolean(key)
        } catch (e: JSONException) {
            default
        }
    }

    @JvmStatic
    fun getJSONObject(json: JSONObject, key: String): JSONObject {
        return try {
            json.getJSONObject(key)
        } catch (e: JSONException) {
            JSONObject()
        }
    }

    @JvmStatic
    fun getJSONArray(json: JSONObject, key: String): JSONArray {
        return try {
            json.getJSONArray(key)
        } catch (e: JSONException) {
            JSONArray()
        }
    }

    @JvmStatic
    fun getJSONArrays(json: JSONObject, key: String): Array<JSONObject> {
        val array = getJSONArray(json, key)
        if (array.length() == 0) return emptyArray()
        return Array(array.length()) { array.getJSONObject(it) }
    }

    @JvmStatic
    fun toMap(json: JSONObject): Map<String, Any> {
        val iterator = json.keys().iterator()
        val map = mutableMapOf<String, Any>()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = json.get(key)
        }
        return map
    }

    @JvmStatic
    fun map(json: JSONObject, block: JSONObjectMapFunction) {
        val iterator = json.keys().iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            block.invoke(object : Map.Entry<String, Any> {
                override val key: String
                    get() = key
                override val value: Any
                    get() = json.get(key)
            })
        }
    }

    @JvmStatic
    fun isNull(json: JSONObject, key: String): Boolean {
        return json.isNull(key)
    }

    @JvmStatic
    fun hasKey(json: JSONObject, key: String): Boolean {
        return json.has(key)
    }

    @JvmStatic
    fun isEmpty(json: JSONObject): Boolean {
        val jsonStr = json.toString()
        return jsonStr == "{}" || jsonStr == ""
    }

    /// Json Array ///
    @JvmStatic
    fun parseArray(json: String): JSONArray {
        return try {
            JSONArray(json)
        } catch (e: JSONException) {
            JSONArray()
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getString(array: JSONArray, index: Int, default: String = ""): String {
        return try {
            array.getString(index)
        } catch (e: JSONException) {
            default
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getInt(array: JSONArray, index: Int, default: Int = 0): Int {
        return try {
            array.getInt(index)
        } catch (e: JSONException) {
            default
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getLong(array: JSONArray, index: Int, default: Long = 0): Long {
        return try {
            array.getLong(index)
        } catch (e: JSONException) {
            default
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getDouble(array: JSONArray, index: Int, default: Double = 0.0): Double {
        return try {
            array.getDouble(index)
        } catch (e: JSONException) {
            default
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getBoolean(array: JSONArray, index: Int, default: Boolean = false): Boolean {
        return try {
            array.getBoolean(index)
        } catch (e: JSONException) {
            default
        }
    }

    @JvmStatic
    fun getJSONObject(array: JSONArray, index: Int): JSONObject {
        return try {
            array.getJSONObject(index)
        } catch (e: JSONException) {
            JSONObject()
        }
    }

    @JvmStatic
    fun getJSONArray(array: JSONArray, index: Int): JSONArray {
        return try {
            array.getJSONArray(index)
        } catch (e: JSONException) {
            JSONArray()
        }
    }

    @JvmStatic
    fun toJSONObjects(array: JSONArray): Array<JSONObject> {
        if (array.length() == 0) return emptyArray()
        return Array(array.length()) { array.getJSONObject(it) }
    }

    @JvmStatic
    fun toMaps(array: JSONArray): List<Map<String, Any>> {
        if (array.length() == 0) return emptyList()
        val list = mutableListOf<Map<String, Any>>()
        for (i in 0 until array.length()) {
            list.add(toMap(getJSONObject(array, i)))
        }
        return list
    }

    @JvmStatic
    fun isNull(array: JSONArray, index: Int): Boolean {
        return array.isNull(index)
    }

    @JvmStatic
    fun isEmpty(array: JSONArray): Boolean {
        if (array.length() == 0) return true

        val jsonStr = array.toString()
        return jsonStr == "[]" || jsonStr == ""
    }


    /// factory ///
    class Factory(json: String) {
        private var mJsonObject: JSONObject? = null
        private var mJsonArray: JSONArray? = null

        constructor(jsonObject: JSONObject) : this("") {
            mJsonObject = jsonObject
        }

        constructor(jsonArray: JSONArray) : this("") {
            mJsonArray = jsonArray
        }


        init {
            if (json.isNotBlank()) {
                if (json[0] == '{') {
                    mJsonObject = parse(json)
                } else {
                    mJsonArray = parseArray(json)
                }
            }
        }

        fun next(key: String): Factory {
            val jsonObject = getJSONObject(mJsonObject!!, key)
            if (isEmpty(jsonObject)) {
                mJsonArray = getJSONArray(mJsonObject!!, key)
            } else {
                mJsonObject = jsonObject
            }
            return this
        }

        fun next(index: Int): Factory {
            val jsonArray = getJSONArray(mJsonArray!!, index)
            if (isEmpty(jsonArray)) {
                mJsonObject = getJSONObject(mJsonArray!!, index)
            } else {
                mJsonArray = jsonArray
            }
            return this
        }

        val jsonObject: JSONObject get() = mJsonObject!!

        val jsonArray: JSONArray get() = mJsonArray!!
    }
}


/// Extended Json ///
fun String.parseJSON(): JSONObject {
    return KJSONUtils.parse(this)
}

fun JSONObject.getStringOrDefault(key: String, default: String = ""): String {
    return KJSONUtils.getString(this, key, default)
}

fun JSONObject.getIntOrDefault(key: String, default: Int = 0): Int {
    return KJSONUtils.getInt(this, key, default)
}

fun JSONObject.getLongOrDefault(key: String, default: Long = 0L): Long {
    return KJSONUtils.getLong(this, key, default)
}

fun JSONObject.getDoubleOrDefault(key: String, default: Double = 0.0): Double {
    return KJSONUtils.getDouble(this, key, default)
}

fun JSONObject.getBooleanOrDefault(key: String, default: Boolean = false): Boolean {
    return KJSONUtils.getBoolean(this, key, default)
}

fun JSONObject.getJSONObjectOrDefault(key: String, default: JSONObject = JSONObject()): JSONObject {
    return KJSONUtils.getJSONObject(this, key)
}

fun JSONObject.getJSONArrayOrDefault(key: String, default: JSONArray = JSONArray()): JSONArray {
    return KJSONUtils.getJSONArray(this, key)
}

fun JSONObject.toMap(): Map<String, Any> {
    return KJSONUtils.toMap(this)
}

fun JSONObject.map(block: JSONObjectMapFunction) {
    KJSONUtils.map(this, block)
}

val JSONObject.isEmpty: Boolean
    get() = KJSONUtils.isEmpty(this)

/// Extended Json Array ///
fun String.parseJSONArray(): JSONArray {
    return KJSONUtils.parseArray(this)
}

fun JSONArray.getStringOrDefault(index: Int, default: String = ""): String {
    return KJSONUtils.getString(this, index, default)
}

fun JSONArray.getIntOrDefault(index: Int, default: Int = 0): Int {
    return KJSONUtils.getInt(this, index, default)
}

fun JSONArray.getLongOrDefault(index: Int, default: Long = 0L): Long {
    return KJSONUtils.getLong(this, index, default)
}

fun JSONArray.getDoubleOrDefault(index: Int, default: Double = 0.0): Double {
    return KJSONUtils.getDouble(this, index, default)
}

fun JSONArray.getBooleanOrDefault(index: Int, default: Boolean = false): Boolean {
    return KJSONUtils.getBoolean(this, index, default)
}

fun JSONArray.getJSONObjectOrDefault(index: Int, default: JSONObject = JSONObject()): JSONObject {
    return KJSONUtils.getJSONObject(this, index)
}

fun JSONArray.getJSONArrayOrDefault(index: Int, default: JSONArray = JSONArray()): JSONArray {
    return KJSONUtils.getJSONArray(this, index)
}

fun JSONArray.toMaps(): List<Map<String, Any>> {
    return KJSONUtils.toMaps(this)
}

val JSONArray.isEmpty: Boolean
    get() = KJSONUtils.isEmpty(this)

fun JSONArray.firstJsonObject(default: JSONObject = JSONObject()): JSONObject {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getJSONObject(this, 0)
}

fun JSONArray.firstStringOrDefault(default: String = ""): String {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getString(this, 0, default)
}

fun JSONArray.firstIntOrDefault(default: Int = 0): Int {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getInt(this, 0, default)
}

fun JSONArray.firstLongOrDefault(default: Long = 0L): Long {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getLong(this, 0, default)
}

fun JSONArray.firstDoubleOrDefault(default: Double = 0.0): Double {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getDouble(this, 0, default)
}

fun JSONArray.firstBooleanOrDefault(default: Boolean = false): Boolean {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getBoolean(this, 0, default)
}

fun JSONArray.lastJsonObject(default: JSONObject = JSONObject()): JSONObject {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getJSONObject(this, this.length() - 1)
}

fun JSONArray.lastStringOrDefault(default: String = ""): String {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getString(this, this.length() - 1, default)
}

fun JSONArray.lastIntOrDefault(default: Int = 0): Int {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getInt(this, this.length() - 1, default)
}

fun JSONArray.lastLongOrDefault(default: Long = 0L): Long {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getLong(this, this.length() - 1, default)
}

fun JSONArray.lastDoubleOrDefault(default: Double = 0.0): Double {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getDouble(this, this.length() - 1, default)
}

fun JSONArray.lastBooleanOrDefault(default: Boolean = false): Boolean {
    if (this.length() == 0 || this.isEmpty) return default
    return KJSONUtils.getBoolean(this, this.length() - 1, default)
}

fun JSONArray.toJSONObjectArray(): Array<JSONObject> {
    return KJSONUtils.toJSONObjects(this)
}