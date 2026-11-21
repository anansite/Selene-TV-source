package org.moontechlab.selenetv.service

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.serialization.json.Json
import okhttp3.Request
import org.moontechlab.selenetv.model.GithubRelease
import org.moontechlab.selenetv.model.UpdateInfo
import java.io.File

/**
 * 应用更新服务。阻塞方法，必须在 Dispatchers.IO 调用；失败抛 ApiException。
 * 复用 MediaHttp.httpClient（trust-all），与项目网络约定一致。
 */
object UpdateService {
    private const val TAG = "UpdateService"
    private const val REPO_NAME = "anansite/Selene-TV-source"
    private const val LATEST_URL =
        "https://api.github.com/repos/$REPO_NAME/releases/latest"

    /** API 检查地址列表（路由器局域网 CDN 优先，国内加速代理与官方直连兜底） */
    private val RELEASE_URLS = listOf(
        "https://cdn.205002.xyz/selene-tv/latest.json",
        "https://ghproxy.net/https://api.github.com/repos/$REPO_NAME/releases/latest",
        LATEST_URL
    )

    /** 国内镜像加速前缀列表（按优先级依次尝试，空串表示回退官方直连） */
    private val DOWNLOAD_MIRRORS = listOf(
        "https://ghfast.top/",
        "https://ghproxy.net/",
        "",
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** 下载专用客户端：APK 较大，放宽 readTimeout 防止慢速/卡顿网络中途超时。 */
    private val downloadClient by lazy {
        MediaHttp.httpClient.newBuilder()
            .readTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
            .build()
    }

    /** 当前 versionName（与 SettingsTab.VersionColophon 一致，不依赖 BuildConfig）。 */
    fun currentVersionName(context: Context): String =
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: ""

    /**
     * 检查更新。返回 UpdateInfo，或 null（无更新 / CI 构建中无资产 / 无匹配架构）。
     * 网络或解析失败抛 ApiException。
     */
    fun checkForUpdate(context: Context): UpdateInfo? {
        val release = fetchLatestRelease()
        val current = currentVersionName(context)
        val abis = Build.SUPPORTED_ABIS?.toList() ?: emptyList()
        return UpdateLogic.buildUpdateInfo(release, current, abis)
    }

    private fun fetchLatestRelease(): GithubRelease {
        var lastException: Exception? = null
        for (apiUrl in RELEASE_URLS) {
            val request = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "SeleneTV")
                .build()
            try {
                MediaHttp.httpClient.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) throw ApiException("检查更新失败 (${resp.code})", resp.code)
                    val body = resp.body?.string() ?: throw ApiException("检查更新失败：空响应")
                    return json.decodeFromString<GithubRelease>(body)
                }
            } catch (e: Exception) {
                Log.w(TAG, "fetchLatestRelease failed via [$apiUrl]: ${e.message}")
                lastException = e
            }
        }
        if (lastException is ApiException) throw lastException
        throw ApiException("检查更新失败：${lastException?.message ?: "无法连接更新服务器"}")
    }

    /**
     * 下载 APK 到 cacheDir/update/。下载前清空该目录。
     * onProgress 回调 0f..1f；总长未知时回调 -1f。失败抛 ApiException 并删除残文件。
     * 调用方应自行节流（如按百分比变化）以免过频回调。
     */
    fun downloadApk(context: Context, info: UpdateInfo, onProgress: (Float) -> Unit): File {
        val dir = File(context.cacheDir, "update").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val outFile = File(dir, "SeleneTV-${info.version}-${info.abi}.apk")

        var lastException: Exception? = null
        val isCdnUrl = info.apkUrl.startsWith("https://cdn.205002.xyz")
        val mirrors = if (isCdnUrl) listOf("") else DOWNLOAD_MIRRORS
        for (prefix in mirrors) {
            val targetUrl = if (prefix.isEmpty()) info.apkUrl else "$prefix${info.apkUrl}"
            val request = Request.Builder().url(targetUrl).header("User-Agent", "SeleneTV").build()
            try {
                downloadClient.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) throw ApiException("下载失败 (${resp.code})", resp.code)
                    val body = resp.body ?: throw ApiException("下载失败：空响应")
                    val total = body.contentLength()
                    body.byteStream().use { input ->
                        outFile.outputStream().use { output ->
                            val buf = ByteArray(8 * 1024)
                            var downloaded = 0L
                            var read = input.read(buf)
                            while (read != -1) {
                                output.write(buf, 0, read)
                                downloaded += read
                                onProgress(if (total > 0) downloaded.toFloat() / total else -1f)
                                read = input.read(buf)
                            }
                        }
                    }
                }
                // 下载成功
                return outFile
            } catch (e: Exception) {
                outFile.delete()
                Log.w(TAG, "downloadApk failed via [$targetUrl]: ${e.message}")
                lastException = e
            }
        }

        if (lastException is ApiException) throw lastException
        throw ApiException("下载失败：${lastException?.message ?: "所有下载节点均失败"}")
    }
}
