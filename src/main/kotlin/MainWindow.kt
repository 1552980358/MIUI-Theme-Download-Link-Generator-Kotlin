import java.awt.*
import javax.swing.*
import java.lang.Exception
import java.net.URL
import com.google.gson.JsonParser

import Constants.Companion.VERSION
import java.io.File

/* 主程序启动 */
fun main(@Suppress("UnusedMainParameter") args: Array<String>) {
    createMainWindow()
}

/* 创建基本窗口 */
fun createMainWindow() {
    val jFrame = JFrame("小米主题MTZ下载")
    jFrame.isVisible = true
    jFrame.setSize(750, 350)
    jFrame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

    val panel = JPanel()
    panel.isVisible = false
    panel.layout = null
    panel.setSize(750, 350)
    jFrame.add(panel)

    Thread {
        val head = JLabel("主题链接: ")
        head.isVisible = false
        head.foreground = Color.black
        head.setBounds(10, 0, 80, 25)
        panel.add(head)
        head.isVisible = true
    }.start()

    val originLink = JTextField()
    Thread {
        originLink.isVisible = false
        originLink.setBounds(10, 25, 715, 25)
        originLink.background = Color.white
        panel.add(originLink)
        originLink.isVisible = true
    }.start()

    var version = VERSION[0]
    Thread {
        val label = JLabel("选择版本: ")
        label.isVisible = false
        label.foreground = Color.black
        label.setBounds(10, 50, 100, 25)

        val v10 = JRadioButton("V10", true)
        val v89 = JRadioButton("V8/V9", false)
        val v67 = JRadioButton("V6/V7", false)
        val v5 = JRadioButton("V5", false)
        val v4 = JRadioButton("V4", false)

        v10.isVisible = false
        v89.isVisible = false
        v67.isVisible = false
        v5.isVisible = false
        v4.isVisible = false

        v10.setBounds(110, 50, 70, 25)
        v89.setBounds(180, 50, 70, 25)
        v67.setBounds(250, 50, 70, 25)
        v5.setBounds(320, 50, 70, 25)
        v4.setBounds(390, 50, 70, 25)

        val buttonGroup = ButtonGroup()
        buttonGroup.add(v10)
        buttonGroup.add(v89)
        buttonGroup.add(v67)
        buttonGroup.add(v5)
        buttonGroup.add(v4)

        v10.addItemListener {
            if (v10.isSelected) {
                version = VERSION[0]
            }
        }
        v89.addItemListener {
            if (v89.isSelected) {
                version = VERSION[1]
            }
        }
        v67.addItemListener {
            if (v67.isSelected) {
                version = VERSION[2]
            }
        }
        v5.addItemListener {
            if (v5.isSelected) {
                version = VERSION[3]
            }
        }
        v4.addItemListener {
            if (v4.isSelected) {
                version = VERSION[4]
            }
        }

        panel.add(label)
        panel.add(v10)
        panel.add(v89)
        panel.add(v67)
        panel.add(v5)
        panel.add(v4)

        label.isVisible = true
        v10.isVisible = true
        v89.isVisible = true
        v67.isVisible = true
        v5.isVisible = true
        v4.isVisible = true
    }.start()

    val resultOut = JTextField()
    Thread {
        resultOut.isVisible = false
        resultOut.setBounds(10, 75, 715, 25)
        panel.add(resultOut)
        resultOut.isVisible = true
    }.start()

    Thread {
        val getLink = JButton("获取")
        getLink.isVisible = false
        getLink.setBounds(10, 260, 355, 50)
        getLink.addActionListener {
            Thread {
                if (!originLink.text.isEmpty()) {
                    resultOut.text = getLink(originLink.text, version)
                } else {
                    resultOut.text = "请输入链接"
                }
            }.start()
        }
        panel.add(getLink)
        getLink.isVisible = true
    }.start()

    Thread {
        val download = JButton("下载")
        download.isVisible = false
        download.setBounds(365, 260, 360, 50)
        download.addActionListener {
            Thread {
                if (!originLink.text.isEmpty()) {
                    createDialog(getRawJson(originLink.text, version))

                } else {
                    resultOut.text = "请输入链接"
                }
            }.start()
        }
        panel.add(download)
        download.isVisible = true
    }.start()

    panel.isVisible = true
}

fun createDialog(rawJson: String) {
    val jFrame = JFrame("准备下载")
    jFrame.setSize(375, 175)
    jFrame.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
    jFrame.isVisible = true

    val panel = Panel()
    panel.isVisible = false
    panel.layout = null
    panel.setSize(375, 175)
    jFrame.add(panel)

    val jProgressBar = JProgressBar(0, 100)
    jProgressBar.isVisible = false
    jProgressBar.value = 0
    jProgressBar.setBounds(10, 0, 345, 25)
    panel.add(jProgressBar)
    jProgressBar.isVisible = true
    panel.isVisible = true

    val json = JsonParser().parse(rawJson).asJsonObject
    val apiData = json.get("apiData").asJsonObject
    val url = apiData.get("downloadUrl").asString   // 链接
    val size = apiData.get("fileSize").asLong       // 大小
    val home = System.getProperties().getProperty("user.home")

    val tmp = url.replace("http://f8.market.xiaomi.com/download/ThemeMarket/", "")
    val fileName = tmp.substring(tmp.indexOf("/") + 1)
    print(url + "\n")
    print(fileName + "\n")
    print(home)

    Download(url, File("$home/Downloads", fileName), size, object : Download.DownloadListener {
        override fun onFinish() {
            super.onFinish()
            jFrame.isVisible = false
        }

        override fun onProcUpdate(p: Long) {
            super.onProcUpdate(p)
            jProgressBar.value = p.toInt()
        }

        override fun onStart() {
            super.onStart()
        }

        override fun onStop(e: Exception) {
            super.onStop(e)
            e.printStackTrace()
        }
    }).start()
}

fun getRawJson(origin: String, version: String): String {
    if (!origin.contains("http://zhuti.xiaomi.com/detail/")) {
        return "获取失败: 请输入正确的主题链接"
    }
    val jsonPage = origin.replace(
        "http://zhuti.xiaomi.com/detail/",
        "https://thm.market.xiaomi.com/thm/download/v2/"
    ) + version

    return try {
        val url = URL(jsonPage)

        // 获取
        return url.openStream().bufferedReader().readText()
    } catch (e: Exception) {
        e.printStackTrace()
        "获取失败: 请查看堆栈"
    }
}

fun getLink(origin: String, version: String): String {
    val json = JsonParser().parse(getRawJson(origin, version)).asJsonObject
    val apiData = json.get("apiData").asJsonObject
    return apiData.get("downloadUrl").asString
}