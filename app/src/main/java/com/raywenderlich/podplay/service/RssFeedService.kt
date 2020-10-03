package com.raywenderlich.podplay.service

import com.raywenderlich.podplay.util.DateUtils
import okhttp3.*
import org.w3c.dom.Node
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory

class RssFeedService: FeedService {
    override fun getFeed(xmlFileURL: String,
                         callBack: (RssFeedResponse?) -> Unit) {
        // 1
        val client = OkHttpClient()
// 2
        val request = Request.Builder()
            .url(xmlFileURL)
            .build()
// 3
        client.newCall(request).enqueue(object : Callback {
            // 4
            override fun onFailure(call: Call, e: IOException) {
                callBack(null)
            }
            // 5
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
// 6
                if (response.isSuccessful) {
// 7
                    response.body()?.let { responseBody ->
// 8
                        val dbFactory = DocumentBuilderFactory.newInstance()
                        val dBuilder = dbFactory.newDocumentBuilder()
                        val doc = dBuilder.parse(responseBody.byteStream())
                        return
                    }
                }
// 9
                callBack(null)
            }
        })
    }

    private fun domToRssFeedResponse(node: Node,
                                     rssFeedResponse: RssFeedResponse) {
// 1
        if (node.nodeType == Node.ELEMENT_NODE) {
// 2
            val nodeName = node.nodeName
            val parentName = node.parentNode.nodeName
// 3
            if (parentName == "channel") {
// 4
                when (nodeName) {
                    "title" -> rssFeedResponse.title = node.textContent
                    "description" -> rssFeedResponse.description =
                        node.textContent
                    "itunes:summary" -> rssFeedResponse.summary =
                        node.textContent
                    "item" -> rssFeedResponse.episodes?.
                    add(RssFeedResponse.EpisodeResponse())
                    "pubDate" -> rssFeedResponse.lastUpdated =
                        DateUtils.xmlDateToDate(node.textContent)
                }
            }
        }
// 5
        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val childNode = nodeList.item(i)
// 6
            domToRssFeedResponse(childNode, rssFeedResponse)
        }
    }
}

interface FeedService {
    // 1
    fun getFeed(xmlFileURL: String,
                callBack: (RssFeedResponse?) -> Unit)
    // 2
    companion object {
        val instance: FeedService by lazy {
            RssFeedService()
        }
    }
}