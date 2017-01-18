package com.godinsec.privacy.net;

import com.godinsec.privacy.utils.RxJavaHelper;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

/**
 * Created by Seeker on 2016/9/13.
 */

public abstract class RetrofitBase {

    private static final String BASEURL = "https://10.0.5.23:7773/";

    private static final long TIMEOUT = 5 * 1000;

    protected Retrofit retrofit;

    protected RxJavaHelper rxJavaHelper;

    protected RetrofitBase(){
        rxJavaHelper = RxJavaHelper.getInstance();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASEURL)
                .client(client())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    private OkHttpClient client(){

        LauncherTrust launcherTrust = new LauncherTrust();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT,TimeUnit.MILLISECONDS)
                .sslSocketFactory(sslSocketFactory(launcherTrust),launcherTrust)
                .hostnameVerifier(new AllowAllHostnameVerifier())
                .build();
        return okHttpClient;
    }

    private SSLSocketFactory sslSocketFactory(LauncherTrust launcherTrust){

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null,new TrustManager[]{launcherTrust},new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    static class LauncherTrust implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}
