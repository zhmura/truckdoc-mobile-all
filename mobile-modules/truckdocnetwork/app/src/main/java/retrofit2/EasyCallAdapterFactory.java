package retrofit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.lang.reflect.ParameterizedType;
import retrofit2.Utils;

/**
 * Converts Retrofit's response to EasyCall object.
 */
public class EasyCallAdapterFactory extends CallAdapter.Factory {

    private final Executor callbackExecutor;

    public static EasyCallAdapterFactory create() {
        return new EasyCallAdapterFactory();
    }

    EasyCallAdapterFactory() {
        this.callbackExecutor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
    }

    @Override
    public CallAdapter<?, EasyCall<?>> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (Utils.getRawType(returnType) != EasyCall.class) {
            return null;
        }
        final Type responseType = Utils.getParameterUpperBound(0, (ParameterizedType) returnType);
        return new CallAdapter<Object, EasyCall<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public EasyCall<Object> adapt(Call<Object> call) {
                return new ExecutorCallbackCall<>(callbackExecutor, call);
            }
        };
    }

    private static final class ExecutorCallbackCall<T> implements EasyCall<T> {

        private final Executor callbackExecutor;
        private final Call<T> delegate;

        ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public void enqueue(Callback<T> callback) {
            delegate.enqueue(new ExecutorCallback<>(callbackExecutor, callback));
        }

        @Override
        public Response<T> execute() throws IOException {
            Response<T> response = delegate.execute();
            if (response.isSuccessful()) {
                return response;
            } else {
                throw new RuntimeException(new HttpException(response));
            }
        }

        public Response<T> executeUnchecked() throws IOException {
            return delegate.execute();
        }

        @Override
        public Response<T> executeChecked() throws IOException, HttpException {
            Response<T> response = delegate.execute();
            if (response.isSuccessful()) {
                return response;
            } else {
                throw new HttpException(response);
            }
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
        @Override
        public EasyCall<T> clone() {
            return new ExecutorCallbackCall<>(callbackExecutor, delegate.clone());
        }
    }

    private static final class ExecutorCallback<T> implements Callback<T> {

        private final Executor callbackExecutor;
        private final Callback<T> delegate;

        ExecutorCallback(Executor callbackExecutor, Callback<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public void onResponse(Call<T> call, final Response<T> response) {
            callbackExecutor.execute(() -> {
                if (response.isSuccessful()) {
                    delegate.onResponse(call, response);
                } else {
                    delegate.onFailure(call, new HttpException(response));
                }
            });
        }

        @Override
        public void onFailure(Call<T> call, final Throwable t) {
            callbackExecutor.execute(() -> delegate.onFailure(call, t));
        }
    }
}
