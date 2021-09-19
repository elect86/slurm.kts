package slurm

import com.github.ajalt.mordant.rendering.BorderStyle
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.table.table
import kotlin.time.ExperimentalTime

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

@ExperimentalTime
fun main() {
    nodelists.print()
}

val nodelists: List<NodeList> by lazy {
    "sinfo -Nel"()
        .lines()
        .drop(2) // date and titles
        .dropLast(1) // last one
        .map { NodeList(it.split(Regex("\\s+"))) }
}

@ExperimentalTime
fun List<NodeList>.print(head: Int = size) {
    terminal.print(table {
        borderStyle = BorderStyle.SQUARE_DOUBLE_SECTION_SEPARATOR
        align = TextAlign.LEFT
        outerBorder = false
        header {
            row("NodeList", "Nodes", "Partition", "State", "CPUs"/*, "OS", "Gr", "Nd", "State", "NodeList"*/)
        }
//        column(2) { align = TextAlign.RIGHT }
//        column(6) { align = TextAlign.CENTER }
//        column(7) { align = TextAlign.RIGHT }
//        column(8) { align = TextAlign.CENTER }
        body {
            for (p in this@print.dropLast(size - head)) {
//                val timelimit = p.timeLimit.toComponents { days, hours, min, sec, _ ->
//                    buildString {
//                        var something = false
//                        if (days != 0) {
//                            append("$days-")
//                            something = true
//                        }
//                        if (hours != 0 || something) {
//                            append("%02d:".format(hours))
//                            something = true
//                        }
//                        if (min != 0 || something)
//                            append("%02d:".format(min))
//                        append("%02d".format(sec))
//                    }
//                }
//                val last = when (p.jobSize.last) {
//                    Int.MAX_VALUE -> '\u221E'.toString()
//                    else -> p.jobSize.last.toString()
//                }
//                val jobSize = "${p.jobSize.first}-$last"
//                val root = if (p.root) "yes" else "no"
//                val oversubs = if (p.oversubs) "yes" else "no"
                row(p.name, p.nodes, p.partition, p.state, p.cpus/*, oversubs, p.groups, p.nodes, p.state, p.nodeList.joinToString(",")*/)
            }
        }
    })
}