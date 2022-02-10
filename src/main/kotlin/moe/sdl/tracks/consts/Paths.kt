package moe.sdl.tracks.consts

import moe.sdl.tracks.util.io.getJarLocation

internal val JAR_DIR by lazy { getJarLocation() }

internal val CONFIG_DIR by lazy { "$JAR_DIR/config" }

internal val STORAGE_DIR by lazy { "$JAR_DIR/storage" }

internal val TRACKS_CONFIG_FILE by lazy { "$CONFIG_DIR/tracks.json" }

internal val YABAPI_COOKIE_STORAGE_FILE by lazy { "$STORAGE_DIR/cookies.json" }
