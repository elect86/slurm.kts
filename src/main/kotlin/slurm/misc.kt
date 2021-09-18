package slurm

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

@ExperimentalTime
data class Partition(
    val name: String,
    val default: Boolean,
    val availability: Boolean,
    val timeLimit: Duration,
    val jobSize: IntRange,
    val root: Boolean,
    val oversubs: Boolean,
    val groups: String,
    val nodes: Int,
    val state: State,
    val nodeList: List<String>)

@ExperimentalTime
fun Partition(line: List<String>): Partition {
    var name = line[0]
    val default = when {
        name.last() == '*' -> true.also { name = name.dropLast(1) }
        else -> false
    }
    val availability = line[1] == "up"
    var x = line[2].split('-')
    val day = if (x.size > 1) "${x[0]}d" else ""
    x = x.getOrElse(1) { x[0] }.split(':')
    var hour = 0
    var min = 0
    val sec = when (x.size) {
        3 -> {
            hour = x[0].toInt()
            min = x[1].toInt()
            x[2].toInt()
        }
        2 -> {
            min = x[0].toInt()
            x[1].toInt()
        }
        else -> x[0].toInt()
    }
    val timeLimit = java.time.Duration.parse("p${day}T${hour}h${min}m${sec}s").toKotlinDuration()
    x = line[3].split('-')
    val (start, end) = x[0].toInt() to x.getOrElse(1) { x[0] }
    val jobSize = start..if (end == "infinite") Int.MAX_VALUE else end.toInt()
    val root = line[4].equals("yes", ignoreCase = true)
    val oversubs = line[5].equals("yes", ignoreCase = true)
    val groups = line[6]
    val nodes = line[7].toInt()
    var y = line[8]
    if (y.last() == '*')
        y = y.dropLast(1)
    val state = State.valueOf(y)
    val nodeList = line[9].split(Regex(",(?![^\\[]*[]])"))
    return Partition(name, default, availability, timeLimit, jobSize, root, oversubs, groups, nodes, state, nodeList)
}

@ExperimentalTime
val partitions: List<Partition> by lazy {
    "sinfo -l"()
        .lines()
        .drop(2) // date and titles
        .dropLast(1) // last one
        .map { Partition(it.split(Regex("\\s+"))) }
}

val terminal = Terminal()

@ExperimentalTime
fun List<Partition>.print() {
    terminal.print(table {
        header {
            row("Partition", "Avail", "TimeLimit", "JobSize", "Root", "Oversubs"/*, "Groups", "Nodes", "State", "NodeList"*/)
        }
        body {
            for (p in this@print) {
                var name = p.name
                if (p.default)
                    name += '*'
                val availability = if (p.availability) "✅" else "❌"
                val days = p.timeLimit.inWholeDays
                val hours = p.timeLimit.inWholeHours
                val min = p.timeLimit.inWholeMinutes
                val sec = p.timeLimit.inWholeSeconds
                val timelimit = "$days-$hours:$min:$sec"
                val last = when (p.jobSize.last) {
                    Int.MAX_VALUE -> '\u221E'.toString()
                    else -> p.jobSize.last.toString()
                }
                val jobSize = "${p.jobSize.first}-$last"
                val root = if (p.root) "yes" else "no"
                val oversubs = if (p.oversubs) "yes" else "no"
                row(name, availability, timelimit, jobSize, root, oversubs/*, p.groups, p.nodes, p.state, p.nodeList.joinToString(",")*/)
            }
        }
    })
}

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