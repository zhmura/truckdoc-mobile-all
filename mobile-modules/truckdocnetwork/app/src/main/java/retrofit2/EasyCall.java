package retrofit2;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;

/**
 * Wraps Retrofit's/HttpOk's Call object to
 * 1) Automatically execute success checks on result
 * 2)
 *
 * @param <T>
 */
public interface EasyCall<T> extends Cloneable {
    Response<T> execute() throws IOException;

    Response<T> executeChecked() throws IOException, HttpException;

    Response<T> executeUnchecked() throws IOException;

    void enqueue(Callback<T> callback);

    void cancel();

    EasyCall<T> clone();
}
