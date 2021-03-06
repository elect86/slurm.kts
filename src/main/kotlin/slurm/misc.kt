package slurm

import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.ExperimentalTime

operator fun StringBuilder.plus(string: String): StringBuilder = append(string)
operator fun StringBuilder.plus(char: Char): StringBuilder = append(char)

@ExperimentalTime
fun main() {
    //    val t = Terminal()
    //    t.println(table {
    //        header { row("CJK", "Emojis") }
    //        body { row("λͺ¨γΉλ¨γ", "πππ") }
    //    })
    partitions.print()
}

val terminal = Terminal(width = 300)

@JvmInline
value class MB(val value: Int) {
    val GB
        get() = GB(value / 1_000)
}

@JvmInline
value class GB(val value: Int)


operator fun String.invoke(arg: String): String = invoke(arrayListOf(arg))

operator fun String.invoke(args: ArrayList<String>): String {
    args.add(0, this)
    val process = ProcessBuilder(args)
        //        .directory(workingDir)
        //        .redirectOutput(Redirect.INHERIT)
        //        .redirectError(Redirect.INHERIT)
        .start()
    //        .waitFor(60, TimeUnit.MINUTES)
    return String(process.inputStream.readAllBytes())
}

//fun main() {
//    //    sinfo { states(State.alloc) }
//}

val Int.s
    get() = Second(this)

@JvmInline
value class Second(val value: Int)

val Int.minute
    get() = Minute(this)

@JvmInline
value class Minute(val value: Int)

val Int.kHz
    get() = kHz(this)

@JvmInline
value class kHz(val value: Int)

val Int.MHz
    get() = MHz(this)

@JvmInline
value class MHz(val value: Int)

@DslMarker
annotation class SlurmMarker

fun ArrayList<String>.add(key: String, value: Any) {
    add(key)
    add(value.toString())
}