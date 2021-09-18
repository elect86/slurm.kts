package slurm

import com.github.ajalt.mordant.rendering.BorderStyle
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.table.table
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

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

@ExperimentalTime
fun List<Partition>.print(head: Int = size) {
    terminal.print(table {
        borderStyle = BorderStyle.SQUARE_DOUBLE_SECTION_SEPARATOR
        align = TextAlign.LEFT
        outerBorder = false
        header {
            row("Partition", "Av", "TimeLimit", "JS", "Rt", "OS", "Groups", "Nodes", "State", "NodeList")
        }
        column(2) { align = TextAlign.RIGHT }
        column(6) { align = TextAlign.CENTER }
        column(7) { align = TextAlign.RIGHT }
        body {
            for (p in this@print.dropLast(size - head)) {
                var name = p.name
                if (p.default)
                    name += '*'
                val availability = if (p.availability) "✅" else "❌"
                val timelimit = p.timeLimit.toComponents { days, hours, min, sec, _ ->
                    buildString {
                        var something = false
                        if (days != 0) {
                            append("$days-")
                            something = true
                        }
                        if (hours != 0 || something) {
                            append("%02d:".format(hours))
                            something = true
                        }
                        if (min != 0 || something)
                            append("%02d:".format(min))
                        append("%02d".format(sec))
                    }
                }
                val last = when (p.jobSize.last) {
                    Int.MAX_VALUE -> '\u221E'.toString()
                    else -> p.jobSize.last.toString()
                }
                val jobSize = "${p.jobSize.first}-$last"
                val root = if (p.root) "yes" else "no"
                val oversubs = if (p.oversubs) "yes" else "no"
                row(name, availability, timelimit, jobSize, root, oversubs, p.groups, p.nodes, p.state, p.nodeList.joinToString(","))
            }
        }
    })
}