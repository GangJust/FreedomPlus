package com.freegang.xpler.loader

import com.freegang.xpler.HookInit
import java.net.URL

class XplerClassloader(
    private val app: ClassLoader,
    private val module: ClassLoader,
    private val parent: ClassLoader,
) : ClassLoader() {

    /**
     * 需要跳过加载的类放这里
     * @param name 类名
     */
    private fun needSkipLoader(name: String?): Boolean {
        val skipPrefix = mutableListOf(
            "kotlinx.",
            "kotlin.",
            "androidx.",
        )
        return skipPrefix.any { name?.startsWith(it) ?: false }
    }

    /**
     * 需要模块加载的类放这里
     * @param name 类名
     */
    private fun moduleLoader(name: String?): Boolean {
        val modulePrefix = mutableListOf(
            "com.freegang.",
        )
        return modulePrefix.any { name?.startsWith(it) ?: false }
    }

    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String?): Class<*> {
        if (needSkipLoader(name)) throw ClassNotFoundException(name)

        if (moduleLoader(name)) {
            try {
                return module.loadClass(name)
            } catch (_: ClassNotFoundException) {
                //module not found
            }
        } else {
            try {
                return app.loadClass(name)
            } catch (_: ClassNotFoundException) {
                //target not found
            }
        }

        try {
            return parent.loadClass(name)
        } catch (_: ClassNotFoundException) {
            //parent not found
        }

        throw ClassNotFoundException(name)
    }

    override fun getResource(name: String?): URL {
        return app.getResource(name) ?: parent.getResource(name)
    }
}

fun injectClassLoader(loader: ClassLoader) {
    val fParent = ClassLoader::class.java.declaredFields.first { it.name == "parent" }.apply { isAccessible = true }
    val mine = HookInit::class.java.classLoader
    val parent = fParent.get(mine) as ClassLoader
    if (parent::class.java != XplerClassloader::class.java) {
        hostClassloader = loader
        moduleClassloader = mine
        xplerClassloader = XplerClassloader(loader, mine!!, parent)
        fParent.set(mine, xplerClassloader)
    }
}

var hostClassloader: ClassLoader? = null

var moduleClassloader: ClassLoader? = null

var xplerClassloader: XplerClassloader? = null