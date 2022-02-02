package moe.sdl.tracks

import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.consts.getDefaultHttpClient
import moe.sdl.yabapi.storage.FileCookieStorage
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath

internal val client = BiliClient(
    getDefaultHttpClient(FileCookieStorage(SYSTEM, "./cookies.json".toPath()))
)
