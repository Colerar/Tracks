package moe.sdl.tracks.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Provide a common [CoroutineScope] for module
 * with common [CoroutineExceptionHandler] and [Dispatchers.Default]
 *
 * In general, you should add a [ModuleScope] as a class member field
 * or object member field with some `init(parentCoroutineContext)` method.
 * And launch or dispatch jobs coroutines by [ModuleScope]
 *
 * @property parentJob specified [Job] with parent coroutine context [Job]
 *
 * @param moduleName coroutine name, and name also would appear
 * @param parentContext parent scope [CoroutineContext]
 * @param dispatcher custom [CoroutineDispatcher]
 * with [CoroutineContext], [Throwable] the specified logger, [String] module name
 */
open class ModuleScope(
    private val moduleName: String = "UnnamedModule",
    parentContext: CoroutineContext = EmptyCoroutineContext,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineScope {

    val parentJob = SupervisorJob(parentContext[Job])

    override val coroutineContext: CoroutineContext =
        parentContext + parentJob + CoroutineName(moduleName) + dispatcher +
            CoroutineExceptionHandler { context, e ->
                val coroutineName = context[CoroutineName]
                val name = if (coroutineName == null) "" else " [${coroutineName.name}]"
                println("Caught Exception on $moduleName${name}:")
                e.printStackTrace()
            }

    fun dispose() {
        parentJob.cancel()
        onClosed()
    }

    open fun onClosed() {
    }
}
