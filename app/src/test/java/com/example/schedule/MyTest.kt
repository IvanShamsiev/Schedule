package com.example.schedule

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.reactivestreams.Publisher
import java.util.concurrent.TimeUnit


class MyTest {

    @Test
    fun testFlatMap() {
        println("Привет, Мир!")

        Thread {
            Flowable.just("123")
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap(Function<String, Publisher<Int>> { str ->
                        return@Function Publisher {
                            if (str.length == 3) it.onNext(3)
                            else it.onError(Throwable("Размер не равен 3"))
                        }
                    })
                    .subscribe(
                            {i -> print("onNext: $i")},
                            { t -> "onError: " + print(t.message)},
                            { print("onComplete!") },
                            { print("onSubscribe!") }
                    )
        }.run()

        for (i in 0..10000) { val b = i * i }
        TimeUnit.MILLISECONDS.sleep(2000)
    }

}