package slurm

import com.github.ajalt.mordant.rendering.BorderStyle
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

operator fun StringBuilder.plus(string: String): StringBuilder = append(string)
operator fun StringBuilder.plus(char: Char): StringBuilder = append(char)

@ExperimentalTime
fun main() {
    //    val t = Terminal()
    //    t.println(table {
    //        header { row("CJK", "Emojis") }
    //        body { row("모ㄹ단ㅌ", "🙊🙉🙈") }
    //    })
    partitions.print()
}

val terminal = Terminal(width = 300)



data class NodeList(
    val name: String,
    val nodes: Int,
    val partition: String,
    val state: State,
    val cpus: Int,
    val socketCoreThread: String,
    val memory: MB,
    val tmpDisk: Int,
    val weight: Int,
    val availFe: String,
    val reason: String)

fun NodeList(line: List<String>): NodeList {
    val name = line[0]
    val nodes = line[1].toInt()
    val partition = line[2]
    var x = line[3]
    if (x.last() == '*')
        x = x.dropLast(1)
    val state = State.valueOf(x)
    val cpus = line[4].toInt()
    val sct = line[5]
    val memory = MB(line[6].toInt())
    val tmpDisk = line[7].toInt()
    val weight = line[8].toInt()
    val availFe = line[9]
    val reason = line[10]
    return NodeList(name, nodes, partition, state, cpus, sct, memory, tmpDisk, weight, availFe, reason)
}

@JvmInline
value class MB(val value: Int)

val nodelists: List<NodeList> by lazy {
    "sinfo -Nel"()
        .lines()
        .drop(2) // date and titles
        .dropLast(1) // last one
        .map { NodeList(it.split(Regex("\\s+"))) }
}

operator fun String.invoke(): String {
    val process = ProcessBuilder(*split(" ").toTypedArray())
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