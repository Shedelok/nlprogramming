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

private val CACHE_DIRECTORY = File(System.getProperty("java.io.tmpdir"), "nlprogramming_v1")

internal object FileCacheManager : Closeable {
    private var cacheManager: CacheManager? = null

    private fun getCacheManager(sizeLimitKB: Long, ttlHours: Long): CacheManager {
        if (cacheManager == null) {
            val cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String::class.java,
                String::class.java,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(100, org.ehcache.config.units.EntryUnit.ENTRIES)
                    .disk(sizeLimitKB, MemoryUnit.KB, true) // true for persistent
                    .build()
            )
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(ttlHours)))
                .build()

            cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(CACHE_DIRECTORY))
                .withCache("llm-responses", cacheConfig)
                .build(true)

        }
        return cacheManager!!
    }

    fun getCache(
        cacheName: String,
        sizeLimitKB: Long,
        ttlHours: Long
    ): Cache<String, String> {
        val manager = getCacheManager(sizeLimitKB, ttlHours)
        val existingCache = manager.getCache(cacheName, String::class.java, String::class.java)
        return existingCache ?: throw IllegalStateException("Cache should have been pre-configured")
    }

    override fun close() {
        cacheManager?.close()
    }
}