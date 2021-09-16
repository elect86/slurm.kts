package slurm

import java.lang.StringBuilder
import java.time.Duration

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

fun Partition(line: List<String>): Partition {
    var name = line[0]
    val default = when {
        name.last() == '*' -> true.also { name = name.dropLast(1) }
        else -> false
    }
    val availability = line[1] == "up"
    var x = line[2].split('-')
    val day = if (x.size > 1) x[0] else ""
    x = x[1].split('-')
    val (hour, minute, second) = x
    val timeLimit = Duration.parse("p${day}T${hour}h${minute}m${second}s")
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

val partitions: List<Partition> by lazy {
    "sinfo -l"()
        .lines()
        .drop(1) // titles
        .map { Partition(it.split(Regex("\\s+"))) }
        .toList()
}
//val sinfo: String by lazy { "sinfo"() }

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
        .drop(1) // titles
        .map { NodeList(it.split(Regex("\\s+"))) }
        .toList()
}

//val nodes

operator fun String.invoke(): String {
    val process = ProcessBuilder(*split(" ").toTypedArray())
        //        .directory(workingDir)
        //        .redirectOutput(Redirect.INHERIT)
        //        .redirectError(Redirect.INHERIT)
        .start()
    //        .waitFor(60, TimeUnit.MINUTES)
    return String(process.inputStream.readAllBytes())
}


inline fun sinfo(block: SinfoBuilder.() -> Unit) {
    val sinfo = Sinfo()
    SinfoBuilder(sinfo).block()
    sinfo()
}

@SlurmMarker
class Sinfo {
    var all = false
    var dead = false
    var exact = false
    var federation = false
    var noHeader = false
    var help = false
    var hide = false
    var iterate = Second(0)
    var json = false
    var local = false
    var long = false
    val clusters = ArrayList<String>()
    val nodes = ArrayList<String>()
    var dontConvert = false
    var node = false
    val format = ArrayList<String>()
    val partitions = ArrayList<String>()
    var responding = false
    var listReasons = false
    var summarize = false
    var order = ""
    val states = ArrayList<State>()
    var reservation = false

    //    var usage = false
    var verbose = false
    var version = false
    var yaml = false

    operator fun invoke() {
        var cmd = StringBuilder("sinfo")
        if (all) cmd += " -a"
        if (dead) cmd += " -d"
        if (exact) cmd += " -e"
        if (federation) cmd += " --federation"
        if (noHeader) cmd += " -h"
        if (help) cmd += " --help"
        if (hide) cmd += " --hide"
        if (iterate != 0.s) cmd += " -i ${iterate.value}"
        if (json) cmd += " --json"
        if (local) cmd += " --local"
        if (long) cmd += " -l"
        if (clusters.isNotEmpty()) cmd += " -M ${clusters.joinToString(",")}"
        if (nodes.isNotEmpty()) cmd += " -n ${nodes.joinToString(",")}"
        if (dontConvert) cmd += " --noconvert"
        if (node) cmd += " -N"
        if (format.isNotEmpty()) cmd += when {
            format.any { '%' in it } -> " -o \"${format.joinToString(" ")}\""
            else -> " -O \"${format.joinToString(",")}\""
        }
        if (partitions.isNotEmpty()) cmd += " -p ${partitions.joinToString(",")}"
        if (responding) cmd += " -r"
        if (listReasons) cmd += " -R"
        if (summarize) cmd += " -s"
        if (order.isNotEmpty()) cmd += " -S $order"
        if (states.isNotEmpty()) cmd += " -t ${states.joinToString(",")}"
        if (reservation) cmd += " -T"
        println("running`$cmd`")
        cmd.toString().invoke()
    }
}

@SlurmMarker
class SinfoBuilder(val sinfo: Sinfo = Sinfo()) {

    /** Display information about all partitions. This causes information to be displayed about partitions that are
     *  configured as hidden and partitions that are unavailable to the user's group. */
    fun all() {
        sinfo.all = true
    }

    /** If set, only report state information for non-responding (dead) nodes. */
    fun dead() {
        sinfo.dead = true
    }

    /** If set, do not group node information on multiple nodes unless their configurations to be reported are identical.
     *  Otherwise cpu count, memory size, and disk space for nodes will be listed with the minimum value followed by a
     *  "+" for nodes with the same partition and state (e.g. "250+"). */
    fun exact() {
        sinfo.exact = true
    }

    /** Show all partitions from the federation if a member of one. */
    fun federation() {
        if (sinfo.local)
            println("`federation` is overridden by `local`")
        sinfo.federation = true
    }

    /** Do not print a header on the output. */
    fun noHeader() {
        sinfo.noHeader = true
    }

    /** Print a message describing all sinfo options. */
    fun help() {
        sinfo.help
    }

    /** Do not display information about hidden partitions. Partitions that are configured as hidden or are not
     *  available to the user's group will not be displayed. This is the default behavior. */
    fun hide() {
        sinfo.hide = true
    }

    /** Print the state on a periodic basis. Sleep for the indicated number of seconds between reports. By default
     *  prints a time stamp with the header. */
    fun iterate(period: Second) {
        sinfo.iterate = period
    }

    /** Dump node information as JSON. All other formating and filtering arugments will be ignored. */
    fun json() {
        sinfo.json = true
    }

    /** Show only jobs local to this cluster. Ignore other clusters in this federation (if any). Overrides `federation`. */
    fun local() {
        if (sinfo.federation)
            println("`federation` is overridden")
        sinfo.local = true
    }

    /** Print more detailed information. This is ignored if the `format` option is specified. */
    fun long() {
        if (sinfo.format.isNotEmpty())
            println("`long` is ignored because `format` is specified")
        else sinfo.long = true
    }

    /** Clusters to issue commands to. Multiple cluster names may be comma separated. A value of `all` will query all
     *  clusters. Note that the SlurmDBD must be up for this option to work properly. This option implicitly sets the
     *  `local` option. */
    fun clusters(vararg names: String) {
        sinfo.clusters += names
    }

    /** Print information about the specified node(s). Multiple nodes may be comma separated or expressed using a node
     *  range expression (e.g. "linux[00-17]") Limiting the query to just the relevant nodes can measurably improve the
     *  performance of the command for large clusters. */
    fun nodes(vararg names: String) {
        sinfo.nodes += names
    }

    /** Don't convert units from their original type (e.g. 2048M won't be converted to 2G). */
    fun dontConvert() {
        sinfo.dontConvert = true
    }

    /** Print information in a node-oriented format with one line per node and partition. That is, if a node belongs to
     *  more than one partition, then one line for each node-partition pair will be shown. If `partition` is also
     *  specified, then only one line per node in this partition is shown. The default is to print information in a
     *  partition-oriented format. This is ignored if the `format` option is specified. */
    fun node() {
        if (sinfo.format.isNotEmpty())
            println("`node` is ignored because `format` is specified")
        else sinfo.node = true
    }

    /** Specify the information to be displayed using an sinfo format string. If the command is executed in a federated
     *  cluster environment and information about more than one cluster is to be displayed and the `noheader` option is
     *  used, then the cluster name will be displayed before the default output formats shown below. Format strings
     *  transparently used by sinfo when running with various options are:
     *      default
     *          "%#P %.5a %.10l %.6D %.6t %N"
     *      summarize()
     *          "%#P %.5a %.10l %.16F %N"
     *      long()
     *          "%#P %.5a %.10l %.10s %.4r %.8h %.10g %.6D %.11T %N"
     *      node()
     *          "%#N %.6D %#P %6t"
     *      long() node()
     *          "%#N %.6D %#P %.11T %.4c %.8z %.6m %.8d %.6w %.8f %20E"
     *      listReasons()
     *          "%20E %9u %19H %N"
     *      long() listReasons()
     *          "%20E %12U %19H %6t %N"
     *  In the above format strings, the use of "#" represents the maximum length of any partition name or node list to
     *  be printed. A pass is made over the records to be printed to establish the size in order to align the sinfo
     *  output, then a second pass is made over the records to print them. Note that the literal character "#" itself is
     *  not a valid field length specification, but is only used to document this behaviour.
     *  The format of each field is "%[[.]size]type[suffix]"
     *      size
     *          Minimum field size. If no size is specified, whatever is needed to print the information will be used.
     *      .
     *          Indicates the output should be right justified and size must be specified. By default output is left
     *          justified.
     *      suffix
     *          Arbitrary string to append to the end of the field.
     *  Valid type specifications include:
     *      %all
     *          Print all fields available for this data type with a vertical bar separating each field.
     *      %a
     *          State/availability of a partition.
     *      %A
     *          Number of nodes by state in the format "allocated/idle". Do not use this with a node state option
     *          ("%t" or "%T") or the different node states will be placed on separate lines.
     *      %b
     *          Features currently active on the nodes, also see %f.
     *      %B
     *          The max number of CPUs per node available to jobs in the partition.
     *      %c
     *          Number of CPUs per node.
     *      %C
     *          Number of CPUs by state in the format "allocated/idle/other/total". Do not use this with a node state
     *          option ("%t" or "%T") or the different node states will be placed on separate lines.
     *      %d
     *          Size of temporary disk space per node in megabytes.
     *      %D
     *          Number of nodes.
     *      %e
     *          Free memory of a node.
     *      %E
     *          The reason a node is unavailable (down, drained, or draining states).
     *      %f
     *          Features available the nodes, also see %b.
     *      %F
     *          Number of nodes by state in the format "allocated/idle/other/total". Note the use of this format option
     *          with a node state format option ("%t" or "%T") will result in the different node states being reported
     *          on separate lines.
     *      %g
     *          Groups which may use the nodes.
     *      %G
     *          Generic resources (gres) associated with the nodes.
     *      %h
     *          Print the OverSubscribe setting for the partition.
     *      %H
     *          Print the timestamp of the reason a node is unavailable.
     *      %I
     *          Partition job priority weighting factor.
     *      %l
     *          Maximum time for any job in the format "days-hours:minutes:seconds"
     *      %L
     *          Default time for any job in the format "days-hours:minutes:seconds"
     *      %m
     *          Size of memory per node in megabytes.
     *      %M
     *          PreemptionMode.
     *      %n
     *          List of node hostnames.
     *      %N
     *          List of node names.
     *      %o
     *          List of node communication addresses.
     *      %O
     *          CPU load of a node.
     *      %p
     *          Partition scheduling tier priority.
     *      %P
     *          Partition name followed by "*" for the default partition, also see %R.
     *      %r
     *          Only user root may initiate jobs, "yes" or "no".
     *      %R
     *          Partition name, also see %P.
     *      %s
     *          Maximum job size in nodes.
     *      %S
     *          Allowed allocating nodes.
     *      %t
     *          State of nodes, compact form.
     *      %T
     *          State of nodes, extended form.
     *      %u
     *          Print the user name of who set the reason a node is unavailable.
     *      %U
     *          Print the user name and uid of who set the reason a node is unavailable.
     *      %v
     *          Print the version of the running slurmd daemon.
     *      %V
     *          Print the cluster name if running in a federation.
     *      %w
     *          Scheduling weight of the nodes.
     *      %X
     *          Number of sockets per node.
     *      %Y
     *          Number of cores per socket.
     *      %Z
     *          Number of threads per core.
     *      %z
     *          Extended processor information: number of sockets, cores, threads (S:C:T) per node. */
    fun format(vararg values: String) {
        sinfo.format += values
    }

    /** Specify the information to be displayed. Also see the `format()` option (which supports greater flexibility in
     *  formatting, but does not support access to all fields because we ran out of letters). Requests a comma separated
     *  list of job information to be displayed.
     *  The format of each field is "type[:[.][size][suffix]]"
     *      size
     *          The minimum field size. If no size is specified, 20 characters will be allocated to print the
     *          information.
     *      .
     *          Indicates the output should be right justified and size must be specified. By default, output is left
     *          justified.
     *      suffix
     *          Arbitrary string to append to the end of the field. */
    fun format(block: FormatBuilder.() -> Unit) = FormatBuilder(sinfo).block()

    /** Print information only about the specified partition(s). Multiple partitions are separated by commas. */
    fun partition(vararg partitions: String) {
        sinfo.partitions += partitions
    }

    /** If set only report state information for responding nodes. */
    fun responding() {
        sinfo.responding = true
    }

    /** List reasons nodes are in the down, drained, fail or failing state. When nodes are in these states Slurm
     *  supports the inclusion of a "reason" string by an administrator. This option will display the first 20
     *  characters of the reason field and list of nodes with that reason for all nodes that are, by default, down,
     *  drained, draining or failing. This option may be used with other node filtering options (e.g. `responding`,
     *  `dead`, `states`, `nodes`), however, combinations of these options that result in a list of nodes that are not
     *  down or drained or failing will not produce any output. When used with `long` the output additionally includes
     *  the current node state. */
    fun listReasons() {
        sinfo.listReasons = true
    }

    /** List only a partition state summary with no node state details. This is ignored if the `format` option is
     *  specified. */
    fun summarize() {
        if (sinfo.format.isNotEmpty())
            println("`summarize` is ignored because `format` is specified")
        else sinfo.summarize = true
    }

    /** Specification of the order in which records should be reported. This uses the same field specification as the
     *  <output_format>. Multiple sorts may be performed by listing multiple sort fields separated by commas. The field
     *  specifications may be preceded by "+" or "-" for ascending (default) and descending order respectively.
     *  The partition field specification, "P", may be preceded by a "#" to report partitions in the same order that
     *  they appear in Slurm's configuration file, slurm.conf. For example, a sort value of "+P,-m" requests that
     *  records be printed in order of increasing partition name and within a partition by decreasing memory size.
     *  The default value of sort is "#P,-t" (partitions ordered as configured then decreasing node state).
     *  If the `node` option is selected, the default sort value is "N" (increasing node name). */
    fun sort(order: String) {
        sinfo.order = order
    }

    /** List nodes only having the given state(s). Multiple states may be comma separated and the comparison is case
     *  insensitive. If the states are separated by '&', then the nodes must be in all states. Possible values include
     *  (case insensitive): ALLOC, ALLOCATED, CLOUD, COMP, COMPLETING, DOWN, DRAIN (for node in DRAINING or DRAINED
     *  states), DRAINED, DRAINING, FAIL, FUTURE, FUTR, IDLE, MAINT, MIX, MIXED, NO_RESPOND, NPC, PERFCTRS, PLANNED,
     *  POWER_DOWN, POWERING_DOWN, POWERED_DOWN, POWERING_UP, REBOOT_ISSUED, REBOOT_REQUESTED, RESV, RESERVED, UNK, and
     *  UNKNOWN. By default nodes in the specified state are reported whether they are responding or not. The `dead` and
     *  `responding` options may be used to filter nodes by the corresponding flag. */
    fun states(vararg states: State) {
        sinfo.states += states
    }

    //    --usage
    //    Print a brief message listing the sinfo options.

    /** Only display information about Slurm reservations.
     *  NOTE: This option causes sinfo to ignore most other options, which are focused on partition and node information. */
    fun reservation() {
        sinfo.reservation = true
    }

    /** Provide detailed event logging through program execution. */
    fun verbose() {
        sinfo.verbose = true
    }

    /** Print version information and exit. */
    fun version() {
        sinfo.version = true
    }

    /** Dump node information as YAML. All other formating and filtering arugments will be ignored. */
    fun yaml() {
        sinfo.yaml = true
    }
}

@SlurmMarker
class Format(val type: Type) {
    var size = 20
    var rightJustified = false
    var suffix = ""

    enum class Type {
        /** Print all fields available in the -o format for this data type with a vertical bar separating each field. */
        All,

        /** Prints the amount of allocated memory on a node. */
        AllocMem,

        /** Allowed allocating nodes. */
        AllocNodes,

        /** State/availability of a partition. */
        Available,

        /** Print the cluster name if running in a federation. */
        Cluster,

        /** Comment. (Arbitrary descriptive string) */
        Comment,

        /** Number of cores per socket. */
        Cores,

        /** Number of CPUs per node. */
        CPUs,

        /** CPU load of a node. */
        CPUsLoad,

        /** Number of CPUs by state in the format "allocated/idle/other/total". Do not use this with a node state option ("%t" or "%T") or the different node states will be placed on separate lines. */
        CPUsState,

        /** Default time for any job in the format "days-hours:minutes:seconds". */
        DefaultTime,

        /** Size of temporary disk space per node in megabytes. */
        Disk,

        /** Arbitray string on the node. */
        Extra,

        /** Features available on the nodes. Also see features_act. */
        Features,

        /** Features currently active on the nodes. Also see features. */
        features_act,

        /** Free memory of a node. */
        FreeMem,

        /** Generic resources (gres) associated with the nodes. */
        Gres,

        /** Generic resources (gres) currently in use on the nodes. */
        GresUsed,

        /** Groups which may use the nodes. */
        Groups,

        /** The max number of CPUs per node available to jobs in the partition. */
        MaxCPUsPerNode,

        /** Size of memory per node in megabytes. */
        Memory,

        /** List of node communication addresses. */
        NodeAddr,

        /** Number of nodes by state in the format "allocated/idle". Do not use this with a node state option ("%t" or "%T") or the different node states will be placed on separate lines. */
        NodeAI,

        /** Number of nodes by state in the format "allocated/idle/other/total". Do not use this with a node state option ("%t" or "%T") or the different node states will be placed on separate lines. */
        NodeAIOT,

        /** List of node hostnames. */
        NodeHost,

        /** List of node names. */
        NodeList,

        /** Number of nodes. */
        Nodes,

        /** Whether jobs may oversubscribe compute resources (e.g. CPUs). */
        OverSubscribe,

        /** Partition name followed by "*" for the default partition, also see %R. */
        Partition,

        /** Partition name, also see %P. */
        PartitionName,

        /** Node TCP port. */
        Port,

        /** Preemption mode. */
        PreemptMode,

        /** Partition factor used by priority/multifactor plugin in calculating job priority. */
        PriorityJobFactor,

        /** Partition scheduling tier priority. */
        PriorityTier,

        /** The reason a node is unavailable (down, drained, or draining states). */
        Reason,

        /** Only user root may initiate jobs, "yes" or "no". */
        Root,

        /** Maximum job size in nodes. */
        Size,

        /** Extended processor information: number of sockets, cores, threads (S:C:T) per node. */
        SocketCoreThread,

        /** Number of sockets per node. */
        Sockets,

        /** State of nodes, compact form. */
        StateCompact,

        /** State of nodes, extended form. */
        StateLong,

        /** State of nodes, including all node state flags. eg. "idle+cloud+power" */
        StateComplete,

        /** Number of threads per core. */
        Threads,

        /** Maximum time for any job in the format "days-hours:minutes:seconds". */
        Time,

        /** Print the timestamp of the reason a node is unavailable. */
        TimeStamp,

        /** Print the user name of who set the reason a node is unavailable. */
        User,

        /** Print the user name and uid of who set the reason a node is unavailable. */
        UserLong,

        /** Print the version of the running slurmd daemon. */
        Version,

        /** Scheduling weight of the nodes. */
        Weight
    }
}

@SlurmMarker
class FormatBuilder(val sinfo: Sinfo) {

    operator fun Format.Type.invoke(block: Format.() -> Unit) {
        sinfo.format += Format(this).run {
            block()
            val type = type.name
            val justified = if (rightJustified) "." else ""
            "$type:$justified$size$suffix"
        }
    }
}

enum class State {
    ALLOC, ALLOCATED, CLOUD, COMP, COMPLETING, DOWN,

    /** for node in DRAINING or DRAINED states */
    DRAIN,
    DRAINED, DRAINING, FAIL, FUTURE, FUTR, IDLE, MAINT, MIX, MIXED, NO_RESPOND, NPC, PERFCTRS, PLANNED, POWER_DOWN,
    POWERING_DOWN, POWERED_DOWN, POWERING_UP, REBOOT_ISSUED, REBOOT_REQUESTED, RESV, RESERVED, UNK, UNKNOWN
}