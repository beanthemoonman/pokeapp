package io.beanthemoonman.pokeapp.tv

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class PokedexApplication : Application(), ImageLoaderFactory {

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }

  /**
   * App-wide Coil loader. Sprites are large and effectively immutable, so we keep a
   * persistent disk cache and ignore server cache headers — once a sprite is fetched it
   * is reused across sessions without another network hit.
   */
  override fun newImageLoader(): ImageLoader =
    ImageLoader.Builder(this)
      .memoryCache {
        MemoryCache.Builder(this)
          .maxSizePercent(0.25)
          .build()
      }
      .diskCache {
        DiskCache.Builder()
          .directory(cacheDir.resolve("sprite_cache"))
          .maxSizeBytes(SPRITE_CACHE_BYTES)
          .build()
      }
      .respectCacheHeaders(false)
      .build()

  private companion object {
    const val SPRITE_CACHE_BYTES = 256L * 1024 * 1024 // 256 MB
  }
}
