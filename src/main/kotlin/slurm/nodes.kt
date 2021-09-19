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
            row("NodeList", "Nodes", "Partition", "State", "CPUs", "S:C:T", "Mem(GB)", "TmpDisk", "Weight", "AvailFe", "Reason")
        }
        column(1) { align = TextAlign.RIGHT }
        column(3) { align = TextAlign.CENTER }
        column(4) { align = TextAlign.RIGHT }
        column(7) { align = TextAlign.RIGHT }
        column(8) { align = TextAlign.RIGHT }
        body {
            for (p in this@print.dropLast(size - head))
                row(p.name, p.nodes, p.partition, p.state, p.cpus, p.socketCoreThread, p.memory.GB.value, p.tmpDisk,
                    p.weight, p.availFe, p.reason)
        }
    })
}