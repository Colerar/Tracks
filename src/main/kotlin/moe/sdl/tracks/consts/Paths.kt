package moe.sdl.tracks.consts

import moe.sdl.tracks.util.io.getJarLocation

internal val JAR_DIR by lazy {
    getJarLocation()
}

internal val CONFIG_DIR = "$JAR_DIR/config"

internal val STORAGE_DIR = "$JAR_DIR/storage"

internal val CACHE_DIR = "$JAR_DIR/cache"

internal val TRACKS_CONFIG_FILE = "$CONFIG_DIR/tracks.json"

internal val YABAPI_COOKIE_STORAGE_FILE = "$STORAGE_DIR/cookies.json"

internal val BILI_IMG_CACHE_DIR = "$CACHE_DIR/bili_img"
