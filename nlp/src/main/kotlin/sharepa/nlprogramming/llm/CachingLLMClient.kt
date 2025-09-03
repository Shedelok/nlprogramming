package sharepa.nlprogramming.llm

import org.ehcache.Cache
import org.json.JSONObject
import sharepa.nlprogramming.cache.FileCacheManager
import sharepa.nlprogramming.cache.LLM_JSON_RESPONSES_CACHE_NAME
import sharepa.nlprogramming.cache.LLM_TEXT_RESPONSES_CACHE_NAME
import java.security.MessageDigest

internal class CachingLLMClient(private val delegate: LLMClient, sizeLimitKB: Long, ttlHours: Long) : LLMClient {
    private val cacheManager: FileCacheManager = FileCacheManager(sizeLimitKB, ttlHours)
    private val textCache: Cache<String, String> = cacheManager.getCache(LLM_TEXT_RESPONSES_CACHE_NAME)
    private val jsonCache: Cache<String, String> = cacheManager.getCache(LLM_JSON_RESPONSES_CACHE_NAME)

    private val delegateDescription = delegate.describeModel()

    override fun generateText(systemPrompt: String, userMessage: String): String {
        val cacheKey = generateCacheKey(systemPrompt, userMessage, "text")

        textCache.get(cacheKey)?.let { return it }

        val response = delegate.generateText(systemPrompt, userMessage)
        textCache.put(cacheKey, response)
        return response
    }

    override fun generateJson(systemPrompt: String, userMessage: String): JSONObject {
        val cacheKey = generateCacheKey(systemPrompt, userMessage, "json")

        jsonCache.get(cacheKey)?.let { return JSONObject(it) }

        val response = delegate.generateJson(systemPrompt, userMessage)
        jsonCache.put(cacheKey, response.toString())
        return response
    }

    override fun describeModel(): String {
        return "Cache over delegate=(${delegate.describeModel()})"
    }

    private fun generateCacheKey(systemPrompt: String, userMessage: String, method: String): String {
        return MessageDigest.getInstance("SHA-256").apply {
            update(delegateDescription.toByteArray())
            update(systemPrompt.toByteArray())
            update(userMessage.toByteArray())
        }.digest().joinToString("") { "%02x".format(it) }
    }

    override fun close() {
        cacheManager.close()
        delegate.close()
    }
}