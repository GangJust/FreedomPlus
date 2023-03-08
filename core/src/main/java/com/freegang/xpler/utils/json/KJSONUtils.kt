package com.freegang.xpler.utils.json

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

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
    fun isNull(json: JSONObject, key: String): Boolean {
        return json.isNull(key)
    }

    @JvmStatic
    fun hasKey(json: JSONObject, key: String): Boolean {
        return json.has(key)
    }

    @JvmStatic
    fun isEmpty(json: JSONObject): Boolean {
        return json.toString() == "{}" || json.toString() == ""
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
    fun isNull(array: JSONArray, index: Int): Boolean {
        return array.isNull(index)
    }

    @JvmStatic
    fun isEmpty(json: JSONArray): Boolean {
        return json.toString() == "[]" || json.toString() == ""
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

    val JSONObject.isEmpties: Boolean
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

    val JSONArray.isEmpties: Boolean
        get() = KJSONUtils.isEmpty(this)

    fun JSONArray.firstJsonObject(default: JSONObject = JSONObject()): JSONObject {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getJSONObject(this, 0)
    }

    fun JSONArray.firstStringOrDefault(default: String = ""): String {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getString(this, 0, default)
    }

    fun JSONArray.firstIntOrDefault(default: Int = 0): Int {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getInt(this, 0, default)
    }

    fun JSONArray.firstLongOrDefault(default: Long = 0L): Long {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getLong(this, 0, default)
    }

    fun JSONArray.firstDoubleOrDefault(default: Double = 0.0): Double {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getDouble(this, 0, default)
    }

    fun JSONArray.firstBooleanOrDefault(default: Boolean = false): Boolean {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getBoolean(this, 0, default)
    }

    fun JSONArray.lastJsonObject(default: JSONObject = JSONObject()): JSONObject {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getJSONObject(this, this.length() - 1)
    }

    fun JSONArray.lastStringOrDefault(default: String = ""): String {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getString(this, this.length() - 1, default)
    }

    fun JSONArray.lastIntOrDefault(default: Int = 0): Int {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getInt(this, this.length() - 1, default)
    }

    fun JSONArray.lastLongOrDefault(default: Long = 0L): Long {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getLong(this, this.length() - 1, default)
    }

    fun JSONArray.lastDoubleOrDefault(default: Double = 0.0): Double {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getDouble(this, this.length() - 1, default)
    }

    fun JSONArray.lastBooleanOrDefault(default: Boolean = false): Boolean {
        if (this.length() == 0 || this.isEmpties) return default
        return KJSONUtils.getBoolean(this, this.length() - 1, default)
    }

    fun JSONArray.toJSONObjectArray(): Array<JSONObject> {
        if (this.length() == 0 || this.isEmpties) return emptyArray()

        return Array(this.length()) {
            this.getJSONObject(it)
        }
    }
}
