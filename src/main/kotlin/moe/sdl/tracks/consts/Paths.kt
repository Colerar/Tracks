package moe.sdl.tracks.consts

import dev.dirs.ProjectDirectories
import moe.sdl.tracks.util.io.getJarLocation

internal val JAR_DIR = getJarLocation()

internal val CONFIG_DIR = ProjectDirectories.from("moe", "sdl", "tracks").configDir

internal val TRACKS_CONFIG_FILE = "$CONFIG_DIR/tracks.json"

internal val YABAPI_COOKIE_STORAGE_FILE = "$CONFIG_DIR/cookies.json"
