/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package main;

import android.util.Log;

public class Fibonacci {
    private static long fibonacci(int n) {
        if (n <= 1) return n;
        else return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        for (int i = 1; i <= n; i++)
            Log.d("Fibonacci", i + " : " + fibonacci(i));
    }
}