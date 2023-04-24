package socket

import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * @author Fedotov Yakov
 */
abstract class BaseSocket {


    private val coroutineContext =
        SupervisorJob() + Dispatchers.IO /*+ CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable
        }*/
    protected val job = Job()
    private val socketScope = CoroutineScope(coroutineContext + job)

    protected val gson = Gson()

    protected fun <P> doWorkInMainThread(doOnAsyncBlock: () -> P) {
        //doCoroutineWork(doOnAsyncBlock, socketScope, Dispatchers.Main)
        doOnAsyncBlock()
    }

    protected fun <P> doWork(doOnAsyncBlock: suspend CoroutineScope.() -> P) =
        doCoroutineWork(doOnAsyncBlock, socketScope, Dispatchers.IO)

    private inline fun <P> doCoroutineWork(
        crossinline doOnAsyncBlock: suspend CoroutineScope.() -> P,
        coroutineScope: CoroutineScope,
        context: CoroutineContext
    ) = coroutineScope.launch {
            withContext(context) {
                doOnAsyncBlock.invoke(this)
            }
        }
}