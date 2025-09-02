package sharepa.nlprogramming.llm

import org.ehcache.Cache
import sharepa.nlprogramming.cache.FileCacheManager
import java.security.MessageDigest

internal class CachingLLMClient(
    private val delegate: LLMClient,
    sizeLimitKB: Long,
    ttlHours: Long
) : LLMClient {
    private val cache: Cache<String, String> = FileCacheManager.getCache("llm-responses", sizeLimitKB, ttlHours)
    private val delegateDescription = delegate.describeModel()

    override fun generateText(systemPrompt: String, userMessage: String): String {
        val cacheKey = generateCacheKey(systemPrompt, userMessage)

        cache.get(cacheKey)?.let { return it }

        val response = delegate.generateText(systemPrompt, userMessage)
        cache.put(cacheKey, response)
        return response
    }

    override fun describeModel(): String {
        return "Cache over delegate=(${delegate.describeModel()})"
    }

    override fun close() {
        FileCacheManager.close()
        delegate.close()
    }

    private fun generateCacheKey(systemPrompt: String, userMessage: String): String {
        return MessageDigest.getInstance("SHA-256").apply {
            update(delegateDescription.toByteArray())
            update(systemPrompt.toByteArray())
            update(userMessage.toByteArray())
        }.digest().joinToString("") { "%02x".format(it) }
    }
}