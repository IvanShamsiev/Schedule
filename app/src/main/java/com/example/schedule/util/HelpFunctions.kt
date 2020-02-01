package com.example.schedule.util

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*


fun daysBetween(d1: Date, d2: Date): Int {
    return ((d2.time - d1.time) / (1000 * 60 * 60 * 24)).toInt()
}

fun <T> Observable<T>.subs(onNext: (item: T) -> Unit, onError: (t: Throwable) -> Unit, onComplete: () -> Unit, onSubscribe: (s: Disposable) -> Unit): Disposable {
    return this.subscribe(onNext, onError, onComplete, onSubscribe)
}