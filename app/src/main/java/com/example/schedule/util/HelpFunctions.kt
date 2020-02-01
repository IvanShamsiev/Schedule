package com.example.schedule.util

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*


fun daysBetween(d1: Date, d2: Date): Int {
    return ((d2.time - d1.time) / (1000 * 60 * 60 * 24)).toInt()
}

fun <T> Observable<T>.buildSubscribe(onNext: ((item: T) -> Unit)? = null,
                                     onError: ((t: Throwable) -> Unit)? = null,
                                     onComplete: (() -> Unit)? = null,
                                     onSubscribe: ((s: Disposable) -> Unit)? = null): Disposable {
    return this.subscribe(
            { onNext?.invoke(it) },
            { onError?.invoke(it) },
            { onComplete?.invoke() },
            { onSubscribe?.invoke(it) }
    )
}