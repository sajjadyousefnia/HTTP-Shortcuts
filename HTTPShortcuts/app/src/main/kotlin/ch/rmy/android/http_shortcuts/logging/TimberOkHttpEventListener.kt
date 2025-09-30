package ch.rmy.android.http_shortcuts.logging

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.HttpUrl
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

object TimberOkHttpEventListener : EventListener.Factory {

    private const val TAG = "OkHttp"

    override fun create(call: Call): EventListener =
        LoggingEventListener(
            requestId = Integer.toHexString(System.identityHashCode(call)),
            request = call.request(),
        )

    private class LoggingEventListener(
        private val requestId: String,
        private val request: Request,
    ) : EventListener() {

        override fun callStart(call: Call) {
            logDebug("callStart (timeout=%dms)", call.timeout().timeoutNanos() / 1_000_000)
        }

        override fun proxySelectStart(call: Call, url: HttpUrl) {
            logDebug("proxySelectStart (%s)", url)
        }

        override fun proxySelectEnd(call: Call, url: HttpUrl, proxies: List<Proxy>) {
            logDebug(
                "proxySelectEnd -> %s",
                proxies.joinToString { proxy ->
                    "${proxy.type()} ${proxy.address()}"
                },
            )
        }

        override fun dnsStart(call: Call, domainName: String) {
            logDebug("dnsStart (%s)", domainName)
        }

        override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
            logDebug(
                "dnsEnd -> %s",
                inetAddressList.joinToString { it.hostAddress ?: it.toString() },
            )
        }

        override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
            logDebug("connectStart (%s via %s)", inetSocketAddress, proxy.type())
        }

        override fun secureConnectStart(call: Call) {
            logDebug("secureConnectStart")
        }

        override fun secureConnectEnd(call: Call, handshake: Handshake?) {
            logDebug("secureConnectEnd (cipherSuite=%s)", handshake?.cipherSuite())
        }

        override fun connectEnd(
            call: Call,
            inetSocketAddress: InetSocketAddress,
            proxy: Proxy,
            protocol: Protocol?,
        ) {
            logDebug("connectEnd (protocol=%s)", protocol)
        }

        override fun connectFailed(
            call: Call,
            inetSocketAddress: InetSocketAddress,
            proxy: Proxy,
            protocol: Protocol?,
            ioe: IOException,
        ) {
            logError(ioe, "connectFailed (protocol=%s)", protocol)
        }

        override fun connectionAcquired(call: Call, connection: okhttp3.Connection) {
            logDebug("connectionAcquired (%s)", connection.route())
        }

        override fun connectionReleased(call: Call, connection: okhttp3.Connection) {
            logDebug("connectionReleased")
        }

        override fun requestHeadersStart(call: Call) {
            logDebug(
                "requestHeadersStart -> %s",
                request.headers.toMultimap().entries.joinToString { (name, values) ->
                    "$name=${values.joinToString()}"
                },
            )
        }

        override fun requestHeadersEnd(call: Call, request: Request) {
            logDebug("requestHeadersEnd (contentLength=%d)", request.body?.contentLength())
        }

        override fun requestBodyStart(call: Call) {
            logDebug("requestBodyStart")
        }

        override fun requestBodyEnd(call: Call, byteCount: Long) {
            logDebug("requestBodyEnd (%d bytes)", byteCount)
        }

        override fun responseHeadersStart(call: Call) {
            logDebug("responseHeadersStart")
        }

        override fun responseHeadersEnd(call: Call, response: Response) {
            logDebug(
                "responseHeadersEnd (code=%d, message=%s)",
                response.code,
                response.message,
            )
        }

        override fun responseBodyStart(call: Call) {
            logDebug("responseBodyStart")
        }

        override fun responseBodyEnd(call: Call, byteCount: Long) {
            logDebug("responseBodyEnd (%d bytes)", byteCount)
        }

        override fun requestFailed(call: Call, ioe: IOException) {
            logError(ioe, "requestFailed")
        }

        override fun responseFailed(call: Call, ioe: IOException) {
            logError(ioe, "responseFailed")
        }

        override fun callEnd(call: Call) {
            logDebug("callEnd")
        }

        override fun callFailed(call: Call, ioe: IOException) {
            logError(ioe, "callFailed")
        }

        override fun canceled(call: Call) {
            logDebug("canceled")
        }

        private fun logDebug(message: String, vararg args: Any?) {
            Timber.tag(TAG).d(formatMessage(message), *formatArgs(*args))
        }

        private fun logError(throwable: Throwable, message: String, vararg args: Any?) {
            Timber.tag(TAG).e(throwable, formatMessage(message), *formatArgs(*args))
        }

        private fun formatMessage(message: String): String =
            "[%s] %s %s - $message"

        private fun formatArgs(vararg args: Any?): Array<Any?> =
            arrayOf(requestId, request.method, request.url.toString(), *args)
    }
}
