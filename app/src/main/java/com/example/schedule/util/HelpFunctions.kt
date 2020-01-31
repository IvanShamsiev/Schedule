package com.example.schedule.util

import java.util.*


fun daysBetween(d1: Date, d2: Date): Int {
    return ((d2.time - d1.time) / (1000 * 60 * 60 * 24)).toInt()
}