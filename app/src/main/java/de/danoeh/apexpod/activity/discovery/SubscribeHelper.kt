package de.danoeh.apexpod.activity.discovery

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import de.danoeh.apexpod.activity.OnlineFeedViewActivity
import de.danoeh.apexpod.core.service.download.DownloadStatus
import de.danoeh.apexpod.core.service.download.Downloader
import de.danoeh.apexpod.core.service.download.HttpDownloader
import de.danoeh.apexpod.core.storage.DBReader
import de.danoeh.apexpod.core.util.DownloadError
import de.danoeh.apexpod.core.util.URLChecker
import de.danoeh.apexpod.discovery.PodcastSearcherRegistry
import de.danoeh.apexpod.model.feed.Feed
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class SubscribeHelper(val activity : AppCompatActivity) {
    private val TAG = "SubscribeHelper"
    private var downloader: Downloader? = null
    private var download: Disposable? = null
    var feeds : List<Feed>? = null
    var feed : Feed? = null
    fun lookupUrl(url: String, username: String?, password: String?,
                  checkDownloadResult: (DownloadStatus?) -> Unit,
                  showNoPodcastFoundError : () -> Unit
    ) {
        download = PodcastSearcherRegistry.lookupUrl(url)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(
                { lookedUpUrl: String? ->
                    startFeedDownload(
                        lookedUpUrl,
                        username,
                        password,
                        checkDownloadResult
                    )
                }
            ) { error: Throwable? ->
                showNoPodcastFoundError()
                Log.e(
                    TAG,
                    Log.getStackTraceString(error)
                )
            }
    }

    fun download() {

    }

    fun startFeedDownload(
        url: String?,
        username: String?,
        password: String?,
        checkDownloadResult : (DownloadStatus?) -> Unit
                ) {
        var url: String? = url
        Log.d(TAG, "Starting feed download")
        url = URLChecker.prepareURL(url!!)
        val downlaodRequestFactory = DownlaodRequestFactory()
        val request = activity.getExternalCacheDir()?.let {
            downlaodRequestFactory.create(
                url, it, username, password
            )
        }
        this.feed = downlaodRequestFactory.feed
        download = Observable.fromCallable {
            feeds = DBReader.getFeedList()
            downloader = HttpDownloader(request!!)
            (downloader as HttpDownloader).call()
            (downloader as HttpDownloader).getResult()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { status: DownloadStatus? ->
                    checkDownloadResult(
                        status
                    )
                }
            ) { error: Throwable? ->
                Log.e(
                    TAG,
                    Log.getStackTraceString(error)
                )
            }
    }
}