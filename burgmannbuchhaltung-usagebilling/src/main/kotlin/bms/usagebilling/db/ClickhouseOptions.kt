package bms.usagebilling.db

object ClickhouseOptions {
    const val ASYNC_INSERT = "async_insert=1"
    const val ASYNC_WAIT = "wait_for_async_insert=1"
    const val ASYNC_NOWAIT = "wait_for_async_insert=0"

    val asyncInsertWaitOptions = arrayOf(ASYNC_INSERT, ASYNC_WAIT)
    val asyncInsertNoWaitOptions = arrayOf(ASYNC_INSERT, ASYNC_NOWAIT)
}
