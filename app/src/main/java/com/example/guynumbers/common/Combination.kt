package com.example.guynumbers.common

class Combination {

    private var result = 0

    private fun combinationUtil(
        arr: IntArray, n: Int, r: Int, index: Int,
        data: IntArray, i: Int
    ): Int {
        if (index == r) {
            var multiplication = 1
            for (j in 0 until r) {
                multiplication *= data[j]
                print(data[j].toString() + " ")
            }
            println("...")
            result += multiplication
            return result
        }

        if (i >= n) return result

        data[index] = arr[i]
        combinationUtil(arr, n, r, index + 1, data, i + 1)

        combinationUtil(arr, n, r, index, data, i + 1)
        return result
    }

    fun combination(arr: IntArray, n: Int, r: Int): Int {
        val data = IntArray(r)
        result = 0
        return combinationUtil(arr, n, r, 0, data, 0)
    }
}