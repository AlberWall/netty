/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.ssl;

import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLContext;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static io.netty.util.internal.ObjectUtil.*;

/**
 * A server-side {@link SslContext} which uses OpenSSL's SSL/TLS implementation.
 */
public final class OpenSslServerContext extends OpenSslContext {
    private static final byte[] ID = new byte[] {'n', 'e', 't', 't', 'y'};
    private final OpenSslServerSessionContext sessionContext;

    /**
     * Creates a new instance.
     *
     * @param certChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @deprecated use {@link SslContextBuilder}
     */
    @Deprecated
    public OpenSslServerContext(File certChainFile, File keyFile) throws SSLException {
        this(certChainFile, keyFile, null);
    }

    /**
     * Creates a new instance.
     *
     * @param certChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}.
     *                    {@code null} if it's not password-protected.
     * @deprecated use {@link SslContextBuilder}
     */
    @Deprecated
    public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword) throws SSLException {
        this(certChainFile, keyFile, keyPassword, null, IdentityCipherSuiteFilter.INSTANCE,
             ApplicationProtocolConfig.DISABLED, 0, 0);
    }

    /**
     * Creates a new instance.
     *
     * @param certChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}.
     *                    {@code null} if it's not password-protected.
     * @param ciphers the cipher suites to enable, in the order of preference.
     *                {@code null} to use the default cipher suites.
     * @param apn Provides a means to configure parameters related to application protocol negotiation.
     * @param sessionCacheSize the size of the cache used for storing SSL session objects.
     *                         {@code 0} to use the default value.
     * @param sessionTimeout the timeout for the cached SSL session objects, in seconds.
     *                       {@code 0} to use the default value.
     * @deprecated use {@link SslContextBuilder}
     */
    @Deprecated
    public OpenSslServerContext(
            File certChainFile, File keyFile, String keyPassword,
            Iterable<String> ciphers, ApplicationProtocolConfig apn,
            long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(certChainFile, keyFile, keyPassword, ciphers, IdentityCipherSuiteFilter.INSTANCE,
             apn, sessionCacheSize, sessionTimeout);
    }

    /**
     * Creates a new instance.
     *
     * @param certChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}.
     *                    {@code null} if it's not password-protected.
     * @param ciphers the cipher suites to enable, in the order of preference.
     *                {@code null} to use the default cipher suites.
     * @param nextProtocols the application layer protocols to accept, in the order of preference.
     *                      {@code null} to disable TLS NPN/ALPN extension.
     * @param sessionCacheSize the size of the cache used for storing SSL session objects.
     *                         {@code 0} to use the default value.
     * @param sessionTimeout the timeout for the cached SSL session objects, in seconds.
     *                       {@code 0} to use the default value.
     * @deprecated use {@link SslContextBuilder}
     */
    @Deprecated
    public OpenSslServerContext(
            File certChainFile, File keyFile, String keyPassword,
            Iterable<String> ciphers, Iterable<String> nextProtocols,
            long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(certChainFile, keyFile, keyPassword, ciphers,
            toApplicationProtocolConfig(nextProtocols), sessionCacheSize, sessionTimeout);
    }

    /**
     * Creates a new instance.
     *
     * @param certChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}.
     *                    {@code null} if it's not password-protected.
     * @param ciphers the cipher suites to enable, in the order of preference.
     *                {@code null} to use the default cipher suites.
     * @param config Application protocol config.
     * @param sessionCacheSize the size of the cache used for storing SSL session objects.
     *                         {@code 0} to use the default value.
     * @param sessionTimeout the timeout for the cached SSL session objects, in seconds.
     *                       {@code 0} to use the default value.
     * @deprecated use {@link SslContextBuilder}
     */
    @Deprecated
    public OpenSslServerContext(
            File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory,
            Iterable<String> ciphers, ApplicationProtocolConfig config,
            long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(certChainFile, keyFile, keyPassword, trustManagerFactory, ciphers,
                toNegotiator(config), sessionCacheSize, sessionTimeout);
    }

    /**
     * Creates a new instance.
     *
     * @param certChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}.
     *                    {@code null} if it's not password-protected.
     * @param ciphers the cipher suites to enable, in the order of preference.
     *                {@code null} to use the default cipher suites.
     * @param apn Application protocol negotiator.
     * @param sessionCacheSize the size of the cache used for storing SSL session objects.
     *                         {@code 0} to use the default value.
     * @param sessionTimeout the timeout for the cached SSL session objects, in seconds.
     *                       {@code 0} to use the default value.
     * @deprecated use {@link SslContextBuilder}
     */
    @Deprecated
    public OpenSslServerContext(
            File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory,
            Iterable<String> ciphers, OpenSslApplicationProtocolNegotiator apn,
            long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(null, trustManagerFactory, certChainFile, keyFile, keyPassword, null,
             ciphers, null, apn, sessionCacheSize, sessionTimeout);
    }

    /**
     * Creates a new instance.
     *
     * @param certChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}.
     *                    {@code null} if it's not password-protected.
     * @param ciphers the cipher suites to enable, in the order of preference.
     *                {@code null} to use the default cipher suites.
     * @param cipherFilter a filter to apply over the supplied list of ciphers
     * @param apn Provides a means to configure parameters related to application protocol negotiation.
     * @param sessionCacheSize the size of the cache used for storing SSL session objects.
     *                         {@code 0} to use the default value.
     * @param sessionTimeout the timeout for the cached SSL session objects, in seconds.
     *                       {@code 0} to use the default value.
     * @deprecated use {@link SslContextBuilder}
     */
    @Deprecated
    public OpenSslServerContext(
            File certChainFile, File keyFile, String keyPassword,
            Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn,
            long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(null, null, certChainFile, keyFile, keyPassword, null,
             ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
    }

    /**
     * Creates a new instance.
     *
     * @param trustCertCollectionFile an X.509 certificate collection file in PEM format.
     *                      This provides the certificate collection used for mutual authentication.
     *                      {@code null} to use the system default
     * @param trustManagerFactory the {@link TrustManagerFactory} that provides the {@link TrustManager}s
     *                            that verifies the certificates sent from clients.
     *                            {@code null} to use the default or the results of parsing
     *                            {@code trustCertCollectionFile}.
     * @param keyCertChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}.
     *                    {@code null} if it's not password-protected.
     * @param keyManagerFactory the {@link KeyManagerFactory} that provides the {@link KeyManager}s
     *                          that is used to encrypt data being sent to clients.
     *                          {@code null} to use the default or the results of parsing
     *                          {@code keyCertChainFile} and {@code keyFile}.
     * @param ciphers the cipher suites to enable, in the order of preference.
     *                {@code null} to use the default cipher suites.
     * @param cipherFilter a filter to apply over the supplied list of ciphers
     *                Only required if {@code provider} is {@link SslProvider#JDK}
     * @param config Provides a means to configure parameters related to application protocol negotiation.
     * @param sessionCacheSize the size of the cache used for storing SSL session objects.
     *                         {@code 0} to use the default value.
     * @param sessionTimeout the timeout for the cached SSL session objects, in seconds.
     *                       {@code 0} to use the default value.
     * @deprecated use {@link SslContextBuilder}
     */
    @Deprecated
    public OpenSslServerContext(
            File trustCertCollectionFile, TrustManagerFactory trustManagerFactory,
            File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory,
            Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig config,
            long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(trustCertCollectionFile, trustManagerFactory, keyCertChainFile, keyFile, keyPassword, keyManagerFactory,
             ciphers, cipherFilter, toNegotiator(config), sessionCacheSize, sessionTimeout);
    }

    /**
     * Creates a new instance.
     *
     * @param certChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}.
     *                    {@code null} if it's not password-protected.
     * @param ciphers the cipher suites to enable, in the order of preference.
     *                {@code null} to use the default cipher suites.
     * @param cipherFilter a filter to apply over the supplied list of ciphers
     * @param config Application protocol config.
     * @param sessionCacheSize the size of the cache used for storing SSL session objects.
     *                         {@code 0} to use the default value.
     * @param sessionTimeout the timeout for the cached SSL session objects, in seconds.
     *                       {@code 0} to use the default value.
     * @deprecated use {@link SslContextBuilder}
     */
    @Deprecated
    public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword,
                                TrustManagerFactory trustManagerFactory, Iterable<String> ciphers,
                                CipherSuiteFilter cipherFilter, ApplicationProtocolConfig config,
                                long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(null, trustManagerFactory, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter,
                      toNegotiator(config), sessionCacheSize, sessionTimeout);
    }

    /**
     * Creates a new instance.
     *
     * @param certChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}.
     *                    {@code null} if it's not password-protected.
     * @param ciphers the cipher suites to enable, in the order of preference.
     *                {@code null} to use the default cipher suites.
     * @param cipherFilter a filter to apply over the supplied list of ciphers
     * @param apn Application protocol negotiator.
     * @param sessionCacheSize the size of the cache used for storing SSL session objects.
     *                         {@code 0} to use the default value.
     * @param sessionTimeout the timeout for the cached SSL session objects, in seconds.
     *                       {@code 0} to use the default value.
     * @deprecated use {@link SslContextBuilder}}
     */
    @Deprecated
    public OpenSslServerContext(
            File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory,
            Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn,
            long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(null, trustManagerFactory, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter,
             apn, sessionCacheSize, sessionTimeout);
    }

    /**
     * Creates a new instance.
     *
     *
     * @param trustCertCollectionFile an X.509 certificate collection file in PEM format.
     *                      This provides the certificate collection used for mutual authentication.
     *                      {@code null} to use the system default
     * @param trustManagerFactory the {@link TrustManagerFactory} that provides the {@link TrustManager}s
     *                            that verifies the certificates sent from clients.
     *                            {@code null} to use the default or the results of parsing
     *                            {@code trustCertCollectionFile}.
     * @param keyCertChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}.
     *                    {@code null} if it's not password-protected.
     * @param keyManagerFactory the {@link KeyManagerFactory} that provides the {@link KeyManager}s
     *                          that is used to encrypt data being sent to clients.
     *                          {@code null} to use the default or the results of parsing
     *                          {@code keyCertChainFile} and {@code keyFile}.
     * @param ciphers the cipher suites to enable, in the order of preference.
     *                {@code null} to use the default cipher suites.
     * @param cipherFilter a filter to apply over the supplied list of ciphers
     *                Only required if {@code provider} is {@link SslProvider#JDK}
     * @param apn Application Protocol Negotiator object
     * @param sessionCacheSize the size of the cache used for storing SSL session objects.
     *                         {@code 0} to use the default value.
     * @param sessionTimeout the timeout for the cached SSL session objects, in seconds.
     *                       {@code 0} to use the default value.
     * @deprecated use {@link SslContextBuilder}
     */
    @Deprecated
    public OpenSslServerContext(
            File trustCertCollectionFile, TrustManagerFactory trustManagerFactory,
            File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory,
            Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn,
            long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(toX509CertificatesInternal(trustCertCollectionFile), trustManagerFactory,
                toX509CertificatesInternal(keyCertChainFile), toPrivateKeyInternal(keyFile, keyPassword),
                keyPassword, keyManagerFactory, ciphers, cipherFilter,
                apn, sessionCacheSize, sessionTimeout, ClientAuth.NONE);
    }

    OpenSslServerContext(
            X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory,
            X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory,
            Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn,
            long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth) throws SSLException {
        this(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers,
                cipherFilter, toNegotiator(apn), sessionCacheSize, sessionTimeout, clientAuth);
    }

    @SuppressWarnings("deprecation")
    private OpenSslServerContext(
            X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory,
            X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory,
            Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn,
            long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth) throws SSLException {
        super(ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, SSL.SSL_MODE_SERVER, keyCertChain,
                clientAuth);
        // Create a new SSL_CTX and configure it.
        boolean success = false;
        try {
            checkKeyManagerFactory(keyManagerFactory);
            checkNotNull(keyCertChain, "keyCertChainFile");
            checkNotNull(key, "keyFile");

            if (keyPassword == null) {
                keyPassword = "";
            }

            synchronized (OpenSslContext.class) {
                /* Set certificate verification policy. */
                SSLContext.setVerify(ctx, SSL.SSL_CVERIFY_NONE, VERIFY_DEPTH);
                long keyCertChainBio = 0;
                try {
                    keyCertChainBio = toBIO(keyCertChain);
                    /* Load the certificate chain. We must skip the first cert when server mode */
                    if (!SSLContext.setCertificateChainBio(ctx, keyCertChainBio, true)) {
                        long error = SSL.getLastErrorNumber();
                        if (OpenSsl.isError(error)) {
                            String err = SSL.getErrorString(error);
                            throw new SSLException(
                                    "failed to set certificate chain: " + err);
                        }
                    }
                } catch (Exception e) {
                    throw new SSLException(
                            "failed to set certificate chain", e);
                } finally {
                    if (keyCertChainBio != 0) {
                        SSL.freeBIO(keyCertChainBio);
                    }
                }

                /* Load the certificate file and private key. */
                long keyBio = 0;
                keyCertChainBio = 0;
                try {
                    keyBio = toBIO(key);
                    keyCertChainBio = toBIO(keyCertChain);
                    if (!SSLContext.setCertificateBio(
                            ctx, keyCertChainBio, keyBio, keyPassword, SSL.SSL_AIDX_RSA)) {
                        long error = SSL.getLastErrorNumber();
                        if (OpenSsl.isError(error)) {
                            String err = SSL.getErrorString(error);
                            throw new SSLException("failed to set certificate and key: " + err);
                        }
                    }
                } catch (SSLException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SSLException("failed to set certificate and key", e);
                } finally {
                    if (keyBio != 0) {
                        SSL.freeBIO(keyBio);
                    }
                    if (keyCertChainBio != 0) {
                        SSL.freeBIO(keyCertChainBio);
                    }
                }
                try {
                    if (trustCertCollection != null) {
                        trustManagerFactory = buildTrustManagerFactory(trustCertCollection, trustManagerFactory);
                    } else if (trustManagerFactory == null) {
                        // Mimic the way SSLContext.getInstance(KeyManager[], null, null) works
                        trustManagerFactory = TrustManagerFactory.getInstance(
                                TrustManagerFactory.getDefaultAlgorithm());
                        trustManagerFactory.init((KeyStore) null);
                    }

                    final X509TrustManager manager = chooseTrustManager(trustManagerFactory.getTrustManagers());

                    // IMPORTANT: The callbacks set for verification must be static to prevent memory leak as
                    //            otherwise the context can never be collected. This is because the JNI code holds
                    //            a global reference to the callbacks.
                    //
                    //            See https://github.com/netty/netty/issues/5372

                    // Use this to prevent an error when running on java < 7
                    if (useExtendedTrustManager(manager)) {
                        SSLContext.setCertVerifyCallback(ctx,
                                new ExtendedTrustManagerVerifyCallback(engineMap, (X509ExtendedTrustManager) manager));
                    } else {
                        SSLContext.setCertVerifyCallback(ctx, new TrustManagerVerifyCallback(engineMap, manager));
                    }
                } catch (Exception e) {
                    throw new SSLException("unable to setup trustmanager", e);
                }
            }
            sessionContext = new OpenSslServerSessionContext(this);
            sessionContext.setSessionIdContext(ID);
            success = true;
        } finally {
            if (!success) {
                destroy();
            }
        }
    }

    @Override
    public OpenSslServerSessionContext sessionContext() {
        return sessionContext;
    }

    private static final class TrustManagerVerifyCallback extends AbstractCertificateVerifier {
        private final X509TrustManager manager;

        TrustManagerVerifyCallback(OpenSslEngineMap engineMap, X509TrustManager manager) {
            super(engineMap);
            this.manager = manager;
        }

        @Override
        void verify(OpenSslEngine engine, X509Certificate[] peerCerts, String auth)
                throws Exception {
            manager.checkClientTrusted(peerCerts, auth);
        }
    }

    private static final class ExtendedTrustManagerVerifyCallback extends AbstractCertificateVerifier {
        private final X509ExtendedTrustManager manager;

        ExtendedTrustManagerVerifyCallback(OpenSslEngineMap engineMap, X509ExtendedTrustManager manager) {
            super(engineMap);
            this.manager = manager;
        }

        @Override
        void verify(OpenSslEngine engine, X509Certificate[] peerCerts, String auth)
                throws Exception {
            manager.checkClientTrusted(peerCerts, auth, engine);
        }
    }
}
