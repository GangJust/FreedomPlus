package io.github.xpler.core

import com.freegang.ktutils.reflect.KReflectUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.xpler.core.bridge.MethodHookImpl
import io.github.xpler.core.interfaces.CallConstructors
import io.github.xpler.core.interfaces.CallMethods
import io.github.xpler.core.log.XplerLog
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/// 使用案例, 详见: https://github.com/GangJust/Xpler/blob/main/docs/readme.md

/**
 * 该注解适用于方法, 将作用于Hook目标的成员方法
 *
 * 被该注解标注的方法将会在Hook目标的成员方法执行开始时执行逻辑
 *
 * 等价于: [XC_MethodReplacement.beforeHookedMethod]
 *
 * 须知: 注解只是为了勾住方法, 被该注解标注的方法,
 * 参数名需要与Hook目标方法的参数名一致,
 * 并且还需在首位增加一个 [XC_MethodHook.MethodHookParam] 的方法参数
 * 这是必要的
 *
 * @param name Hook目标方法名, 当多个方法名时, 同一逻辑将作用在多个方法上, 需参数一致
 */
@Target(AnnotationTarget.FUNCTION)
annotation class OnBefore(vararg val name: String)

/**
 * 该注解适用于方法, 将作用于Hook目标的成员方法
 *
 * 被该注解标注的方法将会在Hook目标的成员方法执行结束后执行逻辑
 *
 * 等价于: [XC_MethodReplacement.afterHookedMethod]
 *
 * 须知: 注解只是为了勾住方法, 被该注解标注的方法,
 * 参数名需要与Hook目标方法的参数名一致,
 * 并且还需在首位增加一个 [XC_MethodHook.MethodHookParam] 的方法参数
 * 这是必要的
 *
 * @param name Hook目标方法名, 当多个方法名时, 同一逻辑将作用在多个方法上, 需参数一致
 */
@Target(AnnotationTarget.FUNCTION)
annotation class OnAfter(vararg val name: String)

/**
 * 该注解适用于方法, 将作用于Hook目标的成员方法
 *
 * 被该注解标注的方法将会替换Hook目标的成员方法逻辑
 *
 * 等价于: [XC_MethodReplacement.replaceHookedMethod]
 *
 * 须知: 注解只是为了勾住方法, 被该注解标注的方法,
 * 参数名需要与Hook目标方法的参数名一致,
 * 并且还需在首位增加一个 [XC_MethodHook.MethodHookParam] 的方法参数
 * 这是必要的
 *
 * @param name Hook目标方法名, 当多个方法名时, 同一逻辑将作用在多个方法上, 需参数一致
 */
@Target(AnnotationTarget.FUNCTION)
annotation class OnReplace(vararg val name: String)

/**
 * 该注解适用于方法, 将作用于Hook目标的构造方法
 *
 * 被该注解标注的方法将会在Hook目标的构造方法执行结束开始时执行逻辑
 *
 * 等价于: [XC_MethodReplacement.beforeHookedMethod]
 *
 * 须知: 注解只是为了勾住方法, 被该注解标注的方法,
 * 参数名需要与Hook目标方法的参数名一致,
 * 并且还需在首位增加一个 [XC_MethodHook.MethodHookParam] 的方法参数
 * 这是必要的
 *
 */
@Target(AnnotationTarget.FUNCTION)
annotation class OnConstructorBefore()

/**
 * 该注解适用于方法, 将作用于Hook目标的构造方法
 *
 * 被该注解标注的方法将会在Hook目标的构造方法执行结束后执行逻辑
 *
 * 等价于: [XC_MethodReplacement.afterHookedMethod]
 * 须知: 注解只是为了勾住方法, 被该注解标注的方法,
 * 参数名需要与Hook目标方法的参数名一致,
 * 并且还需在首位增加一个 [XC_MethodHook.MethodHookParam] 的方法参数
 * 这是必要的
 */
@Target(AnnotationTarget.FUNCTION)
annotation class OnConstructorAfter()

/**
 * 该注解适用于方法, 将作用于Hook目标的构造方法
 *
 * 被该注解标注的方法将会替换Hook目标的构造方法逻辑
 *
 * 等价于: [XC_MethodReplacement.replaceHookedMethod]
 * 须知: 注解只是为了勾住方法, 被该注解标注的方法,
 * 参数名需要与Hook目标方法的参数名一致,
 * 并且还需在首位增加一个 [XC_MethodHook.MethodHookParam] 的方法参数
 * 这是必要的
 */
@Target(AnnotationTarget.FUNCTION)
annotation class OnConstructorReplace()

/**
 * 该注解适用于方法参数, 将作用于Hook目标成员方法的方法参数，被该注解标注的方法参数将被指定 [Class] 类型
 *
 * 须知: 注解只是为了勾住方法, 被该注解标注的方法,
 * 参数名需要与Hook目标方法的参数名一致,
 * 并且还需在首位增加一个 [XC_MethodHook.MethodHookParam] 的方法参数
 * 这是必要的
 *
 * @param name 应该是一个完整的类名, 如: com.sample.User
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Param(val name: String)

/**
 * 配合 [Param] 注解使用, 一般用在参数类型为 [Any] 的时候。
 *
 * 增加它的原因见 [KtOnHook.getTargetMethodParamTypesOnlyAnnotations] 的注释。
 *
 * 实际上它只是为了给注解二维数组占位，并未使用。
 *
 * 当然你也可以使用其他任意非 [Param] 的参数注解给 [Any] 占位。
 * 或者你不嫌麻烦，可以为每一个原始类型这样注解：
 * ```
 * @OnBefore
 * fun test(
 *      //@KeepParam any: Any?,
 *      @Param("java.lang.Object") any: Any?,
 *      @Param("com.test.User") user: Any?,
 * ){
 *      //hook logic
 * }
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class KeepParam()

/**
 * 该注解适用于方法, 将作用于Hook目标的成员方法
 *
 * 被该注解标注的方法将会在执行一次Hook之后立即解开Hook
 * (即Hook逻辑只会执行一次)
 * 需要搭配 [OnBefore] [OnAfter] [OnReplace] 等方法使用
 *
 * 须知: 注解只是为了勾住方法, 被该注解标注的方法,
 * 参数名需要与Hook目标方法的参数名一致,
 * 并且还需在首位增加一个 [XC_MethodHook.MethodHookParam] 的方法参数
 * 这是必要的
 */
@Target(AnnotationTarget.FUNCTION)
annotation class HookOnce()

/**
 * 该注解作用于Hook目标的成员方法(考虑到构造方法多数情况下都会自调用，该注解在构造方法上并没有多大意义)。
 *
 * 对部分将来会出现的方法Hook操作, 场景如下:
 * 某些方法在低版本未出现, 而却在新版本出现了, 这时才会对目标方法Hook; 同理, 未来方法如果被删除, Hook逻辑也不被执行
 * 需要搭配 [OnBefore] [OnAfter] [OnReplace] 等注解使用
 */
@Target(AnnotationTarget.FUNCTION)
annotation class FutureHook()

/**
 * 该类一般用在泛型, 当某个Hook目标类未知时, 可以通过泛型该类进行占位
 */
class EmptyHook {}

/**
 * 该类一般用在 [KtOnHook.setTargetClass] 时, 将不会执行该Hook目标的逻辑
 */
class NoneHook {}

abstract class KtOnHook<T>(protected val lpparam: XC_LoadPackage.LoadPackageParam) {
    private lateinit var mTargetClazz: Class<*>
    protected val targetClazz get() = mTargetClazz
    private val targetMethods = mutableSetOf<Method>()

    private var hookHelper: KtXposedHelpers? = null
    private val mineMethods = mutableSetOf<Method>()

    init {
        runCatching {
            mTargetClazz = this.setTargetClass()
            if (targetClazz == NoneHook::class.java) return@runCatching
            if (targetClazz != EmptyHook::class.java) {
                if (targetClazz == Any::class.java) throw ClassFormatError("Please override the `setTargetClass()` to specify the hook target class!")
                hookHelper = KtXposedHelpers.hookClass(targetClazz)

                getMineAllMethods()
                getHookTargetAllMethods()

                invOnBefore()
                invOnAfter()
                invOnReplace()

                invOnConstructorBefore()
                invOnConstructorAfter()
                invOnConstructorReplace()

                defaultHookAllMethod()
                defaultHookAllConstructor()
            }
            this.onInit()
        }.onFailure {
            XplerLog.xposedLog(it)
        }
    }

    /**
     * 相当于每个Hook逻辑类的入口方法
     */
    open fun onInit() {}

    /**
     * 手动设置目标类, 通常在泛型 <T> 为 Any 时做替换, 常见情况是未对目标app类做只读引入,
     * 则需要通过: XposedHelpers.findClass("类名", lpparam.classLoader)
     */
    open fun setTargetClass(): Class<*> = getHookTargetClass()

    /**
     * 查找某类
     */
    open fun findClass(className: String, classLoader: ClassLoader? = null): Class<*> {
        return try {
            XposedHelpers.findClass(className, classLoader ?: lpparam.classLoader)
        } catch (e: Exception) {
            XplerLog.tagE(this.javaClass.simpleName, e)
            NoneHook::class.java
        }
    }

    /**
     * 获取子类泛型中的Hook目标类, 如果泛型类是 Any, 则需要通过 [setTargetClass] 对指定类进行设置
     */
    @Throws
    private fun getHookTargetClass(): Class<*> {
        val type = this::class.java.genericSuperclass as ParameterizedType
        return type.actualTypeArguments[0] as Class<*>
    }

    /**
     * 获取Hook目标类中的所有方法
     */
    private fun getHookTargetAllMethods() {
        targetMethods.addAll(targetClazz.declaredMethods)
    }

    /**
     * 获取子类所有方法
     */
    private fun getMineAllMethods() {
        mineMethods.addAll(this::class.java.declaredMethods)
    }

    /**
     * 查找子类方法[mineMethods]中被 [@OnBefore] 标注的所有方法, 并将其Hook
     */
    @Throws
    private fun invOnBefore() {
        val methodMap = getAnnotationMethod(OnBefore::class.java)
        for ((key, value) in methodMap) {
            if (value.getAnnotation(OnReplace::class.java) != null) continue
            value.isAccessible = true

            val isFutureHook = value.getAnnotation(FutureHook::class.java) != null
            val names = key.name
            val paramTypes = getTargetMethodParamTypes(value)

            // 目标方法名, 参数名同时为空
            if (names.isEmpty() && paramTypes.isEmpty()) {
                continue
            }

            // 目标方法名为空, 对同类型的所有参数做Hook
            if (names.isEmpty()) {
                val finds = KReflectUtils.findMethods(
                    methods = targetMethods,
                    paramTypes = paramTypes,
                )
                for (method in finds) {
                    // 跳过抽象方法
                    if (Modifier.isAbstract(method.modifiers)) {
                        continue
                    }

                    // 开启Hook
                    MethodHookImpl(method).apply {
                        onBefore {
                            val invArgs = arrayOf(this, *argsOrEmpty)
                            value.invoke(this@KtOnHook, *invArgs)
                        }
                        if (value.getAnnotation(HookOnce::class.java) != null) {
                            onUnhook { _, _ -> }
                        }
                    }.startHook()
                }

                continue
            }

            // 目标方法名不为空, 但参数为空
            if (paramTypes.isEmpty()) {
                names.forEach {
                    val finds = KReflectUtils.findMethods(
                        methods = targetMethods,
                        name = it,
                    )
                    for (method in finds) {
                        // 跳过抽象方法
                        if (Modifier.isAbstract(method.modifiers)) {
                            continue
                        }

                        // 开启Hook
                        MethodHookImpl(method).apply {
                            onBefore {
                                value.invoke(this@KtOnHook, *arrayOf(this)) // 没有方法参数, 直接回调默认参数
                            }
                            if (value.getAnnotation(HookOnce::class.java) != null) {
                                onUnhook { _, _ -> }
                            }
                        }.startHook()
                    }
                }

                continue
            }

            // 基本Hook
            names.forEach { name ->
                var methodHookImpl: MethodHookImpl? = null
                // 如果属于 FutureHook 则对目标Class进行搜索, 未搜索到则不Hook
                if (isFutureHook) {
                    val method = KReflectUtils.findMethodFirst(
                        methods = targetMethods,
                        name = name,
                        paramTypes = paramTypes,
                    )
                    if (method != null && !Modifier.isAbstract(method.modifiers)) {
                        methodHookImpl = MethodHookImpl(method)
                    }
                } else {
                    methodHookImpl = MethodHookImpl(targetClazz, name, *paramTypes)
                }

                // 开启Hook
                methodHookImpl?.apply {
                    onBefore {
                        val invArgs = arrayOf(this, *argsOrEmpty)
                        value.invoke(this@KtOnHook, *invArgs)
                    }
                    if (value.getAnnotation(HookOnce::class.java) != null) {
                        onUnhook { _, _ -> }
                    }
                }?.startHook()
            }
        }
    }

    /**
     * 查找子类方法[mineMethods]中被 [@OnAfter] 标注的所有方法, 并将其Hook
     */
    @Throws
    private fun invOnAfter() {
        val methodMap = getAnnotationMethod(OnAfter::class.java)
        for ((key, value) in methodMap) {
            if (value.getAnnotation(OnReplace::class.java) != null) continue
            value.isAccessible = true

            val isFutureHook = value.getAnnotation(FutureHook::class.java) != null
            val names = key.name
            val paramTypes = getTargetMethodParamTypes(value)

            // 目标方法名, 参数名同时为空
            if (names.isEmpty() && paramTypes.isEmpty()) {
                continue
            }

            // 目标方法名为空, 对同类型的所有参数做Hook
            if (names.isEmpty()) {
                val finds = KReflectUtils.findMethods(
                    methods = targetMethods,
                    paramTypes = paramTypes,
                )
                for (method in finds) {
                    // 跳过抽象方法
                    if (Modifier.isAbstract(method.modifiers)) {
                        continue
                    }

                    // 开启Hook
                    MethodHookImpl(method).apply {
                        onAfter {
                            val invArgs = arrayOf(this, *argsOrEmpty)
                            value.invoke(this@KtOnHook, *invArgs)
                        }
                        if (value.getAnnotation(HookOnce::class.java) != null) {
                            onUnhook { _, _ -> }
                        }
                    }.startHook()
                }

                continue
            }

            // 目标方法名不为空, 但参数为空
            if (paramTypes.isEmpty()) {
                names.forEach { name ->
                    val finds = KReflectUtils.findMethods(
                        methods = targetMethods,
                        name = name,
                    )
                    for (method in finds) {
                        // 跳过抽象方法
                        if (Modifier.isAbstract(method.modifiers)) {
                            continue
                        }

                        // 开启Hook
                        MethodHookImpl(method).apply {
                            onAfter {
                                value.invoke(this@KtOnHook, *arrayOf(this)) // 没有方法参数, 直接回调默认参数
                            }
                            if (value.getAnnotation(HookOnce::class.java) != null) {
                                onUnhook { _, _ -> }
                            }
                        }.startHook()
                    }
                }

                continue
            }

            // 基本Hook
            names.forEach { name ->
                var methodHookImpl: MethodHookImpl? = null
                // 如果属于 FutureHook 则对目标Class进行搜索, 未搜索到则不Hook
                if (isFutureHook) {
                    val method = KReflectUtils.findMethodFirst(
                        methods = targetMethods,
                        name = name,
                        paramTypes = paramTypes,
                    )
                    if (method != null && !Modifier.isAbstract(method.modifiers)) {
                        methodHookImpl = MethodHookImpl(method)
                    }
                } else {
                    methodHookImpl = MethodHookImpl(targetClazz, name, *paramTypes)
                }

                // 开启Hook
                methodHookImpl?.apply {
                    onAfter {
                        val invArgs = arrayOf(this, *argsOrEmpty)
                        value.invoke(this@KtOnHook, *invArgs)
                    }
                    if (value.getAnnotation(HookOnce::class.java) != null) {
                        onUnhook { _, _ -> }
                    }
                }?.startHook()
            }
        }
    }

    /**
     * 查找子类方法[mineMethods]中被 [@OnReplace] 标注的所有方法,并将其Hook
     * 值得注意的是, 某个方法一旦标注了 [@OnReplace] 如果该方法又同时
     * 被[@OnBefore]或[@OnAfter]标注, 该方法跳过它们, 只对 [@OnReplace] 生效
     */
    @Throws
    private fun invOnReplace() {
        val methodMap = getAnnotationMethod(OnReplace::class.java)
        for ((key, value) in methodMap) {
            value.isAccessible = true

            val isFutureHook = value.getAnnotation(FutureHook::class.java) != null
            val names = key.name
            val paramTypes = getTargetMethodParamTypes(value)

            // 目标方法名, 参数名同时为空
            if (names.isEmpty() && paramTypes.isEmpty()) {
                continue
            }

            // 目标方法名为空, 对同类型的所有参数做Hook
            if (names.isEmpty()) {
                val finds = KReflectUtils.findMethods(
                    methods = targetMethods,
                    paramTypes = paramTypes,
                )
                for (method in finds) {
                    // 跳过抽象方法
                    if (Modifier.isAbstract(method.modifiers)) {
                        continue
                    }

                    // 开启Hook
                    MethodHookImpl(method).apply {
                        onReplace {
                            val invArgs = arrayOf(this, *argsOrEmpty)
                            value.invoke(this@KtOnHook, *invArgs) ?: Unit
                        }
                        if (value.getAnnotation(HookOnce::class.java) != null) {
                            onUnhook { _, _ -> }
                        }
                    }.startHook()
                }

                continue
            }

            // 目标方法名不为空, 但参数为空
            if (paramTypes.isEmpty()) {
                names.forEach {
                    val finds = KReflectUtils.findMethods(
                        methods = targetMethods,
                        name = it,
                    )
                    for (method in finds) {
                        // 跳过抽象方法
                        if (Modifier.isAbstract(method.modifiers)) {
                            continue
                        }

                        // 开启Hook
                        MethodHookImpl(method).apply {
                            onReplace {
                                value.invoke(this@KtOnHook, *arrayOf(this)) ?: Unit // 没有方法参数, 直接回调默认参数
                            }
                            if (value.getAnnotation(HookOnce::class.java) != null) {
                                onUnhook { _, _ -> }
                            }
                        }.startHook()
                    }
                }

                continue
            }

            // 基本Hook
            names.forEach { name ->
                var methodHookImpl: MethodHookImpl? = null
                // 如果属于 FutureHook 则对目标Class进行搜索, 未搜索到则不Hook
                if (isFutureHook) {
                    val method = KReflectUtils.findMethodFirst(
                        methods = targetMethods,
                        name = name,
                        paramTypes = paramTypes,
                    )
                    if (method != null && !Modifier.isAbstract(method.modifiers)) {
                        methodHookImpl = MethodHookImpl(method)
                    }
                } else {
                    methodHookImpl = MethodHookImpl(targetClazz, name, *paramTypes)
                }

                // 开启Hook
                methodHookImpl?.apply {
                    onReplace {
                        val invArgs = arrayOf(this, *argsOrEmpty)
                        value.invoke(this@KtOnHook, *invArgs) ?: Unit
                    }
                    if (value.getAnnotation(HookOnce::class.java) != null) {
                        onUnhook { _, _ -> }
                    }
                }?.startHook()
            }
        }
    }

    /**
     * 查找子类方法[mineMethods]中被 [@OnConstructorBefore] 标注的所有方法, 并将其Hook
     */
    @Throws
    private fun invOnConstructorBefore() {
        val methodMap = getAnnotationMethod(OnConstructorBefore::class.java)
        for ((_, value) in methodMap) {
            if (value.getAnnotation(OnConstructorReplace::class.java) != null) continue
            value.isAccessible = true

            hookHelper?.constructor(*getTargetMethodParamTypes(value)) {
                onBefore {
                    val invArgs = arrayOf(this, *argsOrEmpty)
                    value.invoke(this@KtOnHook, *invArgs)
                }
                if (value.getAnnotation(HookOnce::class.java) != null) {
                    onUnhook { _, _ -> }
                }
            }
        }
    }

    /**
     * 查找子类方法[mineMethods]中被 [@OnConstructorAfter] 标注的所有方法, 并将其Hook
     */
    @Throws
    private fun invOnConstructorAfter() {
        val methodMap = getAnnotationMethod(OnConstructorAfter::class.java)
        for ((_, value) in methodMap) {
            if (value.getAnnotation(OnConstructorReplace::class.java) != null) continue
            value.isAccessible = true

            hookHelper?.constructor(*getTargetMethodParamTypes(value)) {
                onAfter {
                    val invArgs = arrayOf(this, *argsOrEmpty)
                    value.invoke(this@KtOnHook, *invArgs)
                }
                if (value.getAnnotation(HookOnce::class.java) != null) {
                    onUnhook { _, _ -> }
                }
            }
        }
    }

    /**
     * 查找子类方法[mineMethods]中被 [@OnConstructorReplace] 标注的所有方法, 并将其Hook
     */
    @Throws
    private fun invOnConstructorReplace() {
        val methodMap = getAnnotationMethod(OnConstructorReplace::class.java)
        for ((_, value) in methodMap) {
            value.isAccessible = true
            hookHelper?.constructor(*getTargetMethodParamTypes(value)) {
                onReplace {
                    val invArgs = arrayOf(this, *argsOrEmpty)
                    value.invoke(this@KtOnHook, *invArgs)
                    thisObject
                }
                if (value.getAnnotation(HookOnce::class.java) != null) {
                    onUnhook { _, _ -> }
                }
            }
        }
    }

    /**
     * 获取被指定注解标注的方法集合
     * @param a  a extends Annotation
     * @return Map
     */
    private fun <A : Annotation> getAnnotationMethod(a: Class<A>): Map<A, Method> {
        val map = mutableMapOf<A, Method>()
        for (method in mineMethods) {
            val annotation = method.getAnnotation(a) ?: continue

            val mineParamsTypes = method.parameterTypes
            if (mineParamsTypes.isEmpty()) {
                throw IllegalArgumentException("parameterTypes empty.")
            }
            if (mineParamsTypes.first() != XC_MethodHook.MethodHookParam::class.java) {
                throw IllegalArgumentException("parameterTypes[0] should be XC_MethodHook.MethodHookParam.")
            }

            map[annotation] = method
        }
        return map
    }

    /**
     * 替换@Param注解, 获取目标方法中的的真实参数列表
     * @param method 目标方法
     * @return Array
     */
    private fun getTargetMethodParamTypes(method: Method): Array<Class<*>> {
        val parameterAnnotations = method.parameterAnnotations
        val parameterTypes = method.parameterTypes

        // 只含注解参数的情况下
        return if (parameterAnnotations.size < parameterTypes.size) {
            getTargetMethodParamTypesOnlyAnnotations(parameterAnnotations, parameterTypes)
        } else {
            getTargetMethodParamTypesNormal(parameterAnnotations, parameterTypes)
        }
    }

    /**
     * 部分情况下 [parameterAnnotations] 只会返回含有注解的参数
     * ```
     * val targetMethodParamTypes = method.parameterTypes
     * val parameterAnnotations = method.parameterAnnotations
     *
     * // 通常情况下 targetMethodParamTypes.size == parameterAnnotations.size 的结果应该是 `true`
     * // 而某些情况下 targetMethodParamTypes.size > parameterAnnotations.size 的结果才是 `true`
     * // 因为 `parameterAnnotations.size == 被注解标注了的参数的数量` 而那些未被注解的参数本来应该是空数组的，但是它却没了。
     * ```
     * @param parameterAnnotations 注解数组
     * @param parameterTypes 参数数组
     */
    private fun getTargetMethodParamTypesOnlyAnnotations(
        parameterAnnotations: Array<Array<Annotation>>,
        parameterTypes: Array<Class<*>>
    ): Array<Class<*>> {
        // 整理参数, 将第一个参数`XC_MethodHook.MethodHookParam`移除
        val paramTypes = parameterTypes.toMutableList().apply { removeFirst() }

        var index = 0
        return paramTypes.map { clazz ->
            if (clazz == Any::class.java) {
                val param = parameterAnnotations[index++].filterIsInstance<Param>()
                    .ifEmpty { return@map clazz } // 如果某个参数没有@Param注解, 直接return
                findClass(param[0].name)// 寻找注解类
            } else {
                clazz
            }
        }.toTypedArray()
    }

    /**
     * 正常情况下 [parameterAnnotations] 能获取到全部参数的注解, 未被注解标注的参数是一个空注解数组
     *
     * @param parameterAnnotations 注解数组
     * @param parameterTypes 参数数组
     */
    private fun getTargetMethodParamTypesNormal(
        parameterAnnotations: Array<Array<Annotation>>,
        parameterTypes: Array<Class<*>>
    ): Array<Class<*>> {
        // 整理参数、参数注解列表, 将第一个参数`XC_MethodHook.MethodHookParam`移除
        val paramAnnotations = parameterAnnotations.toMutableList().apply { removeFirst() }
        val paramTypes = parameterTypes.toMutableList().apply { removeFirst() }

        // 替换 @Param 指定的参数类型
        val finalParamTypes = paramTypes.mapIndexed { index, clazz ->
            if (paramAnnotations[index].isEmpty()) return@mapIndexed clazz // 如果某个参数没有注解
            val param = paramAnnotations[index].filterIsInstance<Param>()
                .ifEmpty { return@mapIndexed clazz } // 如果某个参数有注解, 但没有@Param注解, 直接return
            findClass(param[0].name) // 寻找注解类
        }.toTypedArray()

        return finalParamTypes
    }

    /**
     * 勾住所有普通方法
     */
    private fun defaultHookAllMethod() {
        // 所有普通方法
        if (this@KtOnHook is CallMethods) {
            hookHelper?.methodAll {
                onBefore {
                    callOnBeforeMethods(this)
                }
                onAfter {
                    thisObject ?: return@onAfter
                    callOnAfterMethods(this)
                }
            }
        }
    }

    /**
     * 勾住所有构造方法
     */
    private fun defaultHookAllConstructor() {
        // 所有构造方法
        if (this@KtOnHook is CallConstructors) {
            hookHelper?.constructorsAll {
                onBefore {
                    callOnBeforeConstructors(this)
                }
                onAfter {
                    thisObject ?: return@onAfter
                    callOnAfterConstructors(this)
                }
            }
        }
    }
}