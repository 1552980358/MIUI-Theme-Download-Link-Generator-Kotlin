import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL

class Download {
    private var thread: Thread
    private lateinit var link: String
    private lateinit var fileLocation: File
    private var size: Long = -1
    private lateinit var listener: DownloadListener

    constructor(link: String, fileLocation: File, size: Long, listener: DownloadListener) : this() {
        this.link = link
        this.fileLocation = fileLocation
        this.size = size
        this.listener = listener
    }

    constructor() : super() {
        thread = Thread {
            try {
                if (!fileLocation.parentFile.exists()) {
                    fileLocation.parentFile.mkdirs()    // 创建文件
                }

                val openStream = URL(link).openStream()
                val fileOutputStream = FileOutputStream(fileLocation)

                val bytes = ByteArray(102400)

                var len: Int
                var per = 0L
                var sum = 0L
                len = openStream.read(bytes)
                do {
                    fileOutputStream.write(bytes, 0, len)
                    sum += len
                    val current = sum / size
                    if (current != per) {
                        // 更新下自百分比
                        listener.onProcUpdate(current)
                        per = current
                    }
                    len = openStream.read(bytes)
                } while (len != -1)
                fileOutputStream.flush()
                fileOutputStream.close()
                openStream.close()
                listener.onFinish()
            } catch (e: Exception) {
                listener.onStop(e)
            }
        }
    }

    fun setLink(link: String): Download {
        this.link = link
        return this
    }

    fun setFileLocation(fileLocation: File): Download {
        this.fileLocation = fileLocation
        return this
    }

    fun setListener(listener: DownloadListener) {
        this.listener = listener
    }

    fun stop() {
        thread.interrupt()
    }

    fun start() {
        thread.start()
        listener.onStart()
    }
    interface DownloadListener {
        fun onStart() {

        }

        fun onStop(e: Exception) {

        }

        fun onProcUpdate(p: Long) {

        }

        fun onFinish() {

        }
    }
}

