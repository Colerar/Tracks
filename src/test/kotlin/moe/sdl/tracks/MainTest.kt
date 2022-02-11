package moe.sdl.tracks

fun main() {
    while (true) {
        // JVM lack of fine-grained `System.exit()` control,
        // cannot execute like REPL, may need restart program
        try {
            val command = readln().split(' ').toTypedArray()
            main(command)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: Error) {
            e.printStackTrace()
        }
    }
}
