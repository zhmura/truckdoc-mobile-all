package com.sanda.truckdoc.network.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
    private final RxJava3CallAdapterFactory original;

    private RxErrorHandlingCallAdapterFactory() {
        original = RxJava3CallAdapterFactory.create();
    }

    public static CallAdapter.Factory create() {
        return new RxErrorHandlingCallAdapterFactory();
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return new RxCallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit));
    }

    private static class RxCallAdapterWrapper<R> implements CallAdapter<R, Observable<?>> {
        private final Retrofit retrofit;
        private final CallAdapter<R, Observable<?>> wrapped;

        public RxCallAdapterWrapper(Retrofit retrofit, CallAdapter<R, Observable<?>> wrapped) {
            this.retrofit = retrofit;
            this.wrapped = wrapped;
        }

        @Override
        public Type responseType() {
            return wrapped.responseType();
        }

        @Override
        public Observable<?> adapt(Call<R> call) {
            return ((Observable) wrapped.adapt(call)).onErrorResumeNext(new Function<Throwable, Observable>() {
                @Override
                public Observable apply(Throwable throwable) {
                    return Observable.error(asRetrofitException(throwable));
                }
            });
        }

        private RetrofitException asRetrofitException(Throwable throwable) {
            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;
                return RetrofitException.httpError(httpException.response().raw().request().url().toString(), httpException.response().raw(), retrofit);
            }
            if (throwable instanceof java.net.SocketTimeoutException) {
                return RetrofitException.networkError((java.io.IOException) throwable);
            }
            if (throwable instanceof java.io.IOException) {
                return RetrofitException.networkError((java.io.IOException) throwable);
            }
            return RetrofitException.unexpectedError(throwable);
        }
    }
}
