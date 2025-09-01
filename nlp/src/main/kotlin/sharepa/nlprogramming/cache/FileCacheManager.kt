package sharepa.nlprogramming.cache

import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import java.io.File
import java.time.Duration

private val CACHE_DIRECTORY = File(System.getProperty("java.io.tmpdir"), "nlprogramming_v1")

internal object FileCacheManager {
    private val cacheManager: CacheManager by lazy {
        CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence(CACHE_DIRECTORY))
            .build(true)
    }

    fun getCache(
        cacheName: String,
        sizeLimitKB: Long,
        ttlHours: Long
    ): Cache<String, String> {
        return cacheManager.getCache(cacheName, String::class.java, String::class.java)
            ?: createCache(cacheName, sizeLimitKB, ttlHours)
    }

    private fun createCache(name: String, sizeLimitKB: Long, ttlHours: Long): Cache<String, String> {
        val cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String::class.java,
            String::class.java,
            ResourcePoolsBuilder.newResourcePoolsBuilder()
                .disk(sizeLimitKB, MemoryUnit.KB)
                .build()
        )
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(ttlHours)))
            .build()

        return cacheManager.createCache(name, cacheConfig)
    }
}