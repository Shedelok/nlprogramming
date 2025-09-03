package sharepa.nlprogramming.cache

import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import java.io.Closeable
import java.io.File
import java.time.Duration

internal const val LLM_TEXT_RESPONSES_CACHE_NAME = "llm-text-responses"
internal const val LLM_JSON_RESPONSES_CACHE_NAME = "llm-json-responses"

private val cacheAliases = listOf(LLM_TEXT_RESPONSES_CACHE_NAME, LLM_JSON_RESPONSES_CACHE_NAME)

private val CACHE_DIRECTORY = File(System.getProperty("java.io.tmpdir"), "nlprogramming_v1")

internal class FileCacheManager(sizeLimitKB: Long, ttlHours: Long) : Closeable {
    private val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerBuilder.persistence(CACHE_DIRECTORY))
        .let { builder ->
            val sizeLimitPerCacheKB = sizeLimitKB / cacheAliases.size

            var builderWithCaches = builder
            for (alias in cacheAliases) {
                builderWithCaches = builderWithCaches.withCache(
                    alias, CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        String::class.java,
                        String::class.java,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                            .heap(100, org.ehcache.config.units.EntryUnit.ENTRIES)
                            .disk(sizeLimitPerCacheKB, MemoryUnit.KB, true) // true for persistent
                            .build()
                    ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(ttlHours)))
                        .build()
                )
            }

            builderWithCaches
        }
        .build(true)

    fun getCache(cacheName: String): Cache<String, String> {
        val existingCache = cacheManager.getCache(cacheName, String::class.java, String::class.java)
        return existingCache ?: throw IllegalStateException("Cache should have been pre-configured")
    }

    override fun close() {
        cacheManager.close()
    }
}