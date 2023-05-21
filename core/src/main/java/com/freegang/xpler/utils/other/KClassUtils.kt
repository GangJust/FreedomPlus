import androidx.annotation.experimental.Experimental
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.util.Locale

@ExperimentalStdlibApi
object KClassUtils {
    private val bytesClassLoader = BytesClassLoader()
    private val imports: MutableSet<String> = mutableSetOf()

    /**
     * 将类转换为字符串表示形式。
     * @param classFile 类文件对象 [.class] 的完整路径
     * @param className 类名, 需要是 [.class] 中的完整类名(含包名)
     * @sample
     * ```
     * 类:
     * package com.sample.demo
     * class Demo(val name: String)
     *--------------------------------
     * 编译后的路径:
     * D:/demo/out/Demo.class
     *--------------------------------
     * 调用:
     * KClassUtils.classToString("D:/demo/out/Demo.class", "com.sample.demo.Demo")
     * ```
     * @return 类的字符串表示形式
     */
    fun classToString(classFile: File, className: String): String {
        val readBytes = classFile.readBytes()
        val clazz = bytesClassLoader.loadClass(className, readBytes) ?: ""
        return classToString(clazz as Class<*>)
    }

    /**
     * 将类转换为字符串表示形式，包括包名、导入语句和类的详细内容。
     *
     * @param clazz 要转换的类对象
     * @return 类的字符串表示形式
     */
    fun classToString(clazz: Class<*>): String {
        val pkg = "package ${clazz.`package`.name};"

        val deepClass = processClass(clazz)

        return "\n$pkg\n\n${imports.joinToString("\n")}\n$deepClass"
    }

    /**
     * 处理类对象，生成类的字符串表示形式。
     *
     * @param clazz 要处理的类对象
     * @return 类的字符串表示形式
     */
    private fun processClass(clazz: Class<*>): String {
        val modifiers: String = Modifier.toString(clazz.modifiers)
        val name: String = clazz.simpleName
        var classStr = "$modifiers class $name {\n"

        //获取方法
        val methods = clazz.declaredMethods
        for (method in methods) {
            classStr = "$classStr\t${processMethod(method)}"
        }

        //获取内部类
        classStr = "\n$classStr${processInnerClasses(clazz)}"

        classStr = "$classStr}\n"

        return classStr
    }

    /**
     * 处理方法对象，生成方法的字符串表示形式。
     *
     * @param method 要处理的方法对象
     * @return 方法的字符串表示形式
     */
    private fun processMethod(method: Method): String {
        val modifiers = method.modifiers
        val returnType = method.returnType
        imports.add("import java.lang.RuntimeException;")
        addImport(returnType)
        val name = method.name
        var methodStr = "${Modifier.toString(modifiers)} ${returnType.simpleName} $name ("
        val params = mutableListOf<String>()
        method.parameters.forEachIndexed { index, parameter ->
            params.add(processParameter(parameter, "${index + 1}"))
        }
        methodStr = "$methodStr${params.joinToString(", ")}"
        methodStr = "$methodStr) {\n"
        methodStr = "$methodStr\t\tthrow new RuntimeException(\"sub!!\");"
        methodStr = "$methodStr\n\t}\n"
        return methodStr
    }

    /**
     * 处理参数对象，生成参数的字符串表示形式。
     *
     * @param parameter 要处理的参数对象
     * @param needName 是否需要参数名称的标识（用于生成唯一的参数名）
     * @return 参数的字符串表示形式
     */
    private fun processParameter(parameter: Parameter?, needName: String): String {
        parameter ?: return ""
        val type = parameter.type
        addImport(type)
        val name = parameter.name
        return "${type.simpleName} ${type.simpleName.substring(0, 1).lowercase(Locale.getDefault())}$needName"
    }

    /**
     * 处理内部类对象，生成内部类的字符串表示形式。
     *
     * @param clazz 父类的类对象，用于获取内部类
     * @return 内部类的字符串表示形式
     */
    private fun processInnerClasses(clazz: Class<*>): String {
        val childClasses = clazz.declaredClasses
        val innerClasses = childClasses.filter { it.isMemberClass && it.declaringClass == clazz }
        val childClassStrings = innerClasses.map { processClass(it) }
        return childClassStrings.joinToString("\n")
    }

    /**
     * 添加导入语句。
     *
     * @param clazz 要导入的类对象
     */
    private fun addImport(clazz: Class<*>) {
        if (clazz.`package` == null) return
        if (clazz.isPrimitive) return
        if (clazz.name == "java.lang.String") return
        imports.add("import ${clazz.name.replace("\$", ".")};")
    }

    private class BytesClassLoader() : ClassLoader() {

        fun loadClass(name: String, bytes: ByteArray): Class<*>? {
            return defineClass(name, bytes, 0, bytes.size)
        }
    }
}

