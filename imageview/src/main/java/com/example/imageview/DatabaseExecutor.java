package com.example.imageview;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DatabaseExecutor {

    public interface Callback<T> {
        void onComplete(T result);
    }

    public interface Operation<T> {
        T run();
    }

    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private DatabaseExecutor() {
    }

    public static void execute(Runnable runnable) {
        IO_EXECUTOR.execute(runnable);
    }

    public static <T> void execute(Operation<T> operation, Callback<T> callback) {
        IO_EXECUTOR.execute(() -> {
            T result = operation.run();
            if (callback != null) {
                MAIN_HANDLER.post(() -> callback.onComplete(result));
            }
        });
    }

    public static void runOnMainThread(Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }
}
