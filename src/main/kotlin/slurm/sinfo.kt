package slurm


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
    var iterate = Second(0)
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
    var usage = false
    var verbose = false
    var version = false

    operator fun invoke(): String {
        val cmd = buildString {
            append("sinfo")
            if (all) append(" -a")
            if (dead) append(" -d")
            if (exact) append(" -e")
            if (federation) append(" --federation")
            if (noHeader) append(" -h")
            if (help) append(" --help")
            if (iterate != 0.s) append(" -i ${iterate.value}")
            if (local) append(" --local")
            if (long) append(" -l")
            if (clusters.isNotEmpty()) append(" -M ${clusters.joinToString(",")}")
            if (nodes.isNotEmpty()) append(" -n ${nodes.joinToString(",")}")
            if (dontConvert) append(" --noconvert")
            if (node) append(" -N")
            if (format.isNotEmpty()) append(when {
                                                '%' in format[0] -> " -o \"${format.joinToString(" ")}\""
                                                else -> " -O \"${format.joinToString(",")}\""
                                            })
            if (partitions.isNotEmpty()) append(" -p ${partitions.joinToString(",")}")
            if (responding) append(" -r")
            if (listReasons) append(" -R")
            if (summarize) append(" -s")
            if (order.isNotEmpty()) append(" -S $order")
            if (states.isNotEmpty()) append(" -t ${states.joinToString(",")}")
            if (reservation) append(" -T")
        }
        println("running `$cmd`")
        return cmd()
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
    inner class FormatBuilder {

        operator fun Format.Type.invoke(block: Format.() -> Unit) {
            this@Sinfo.format += Format(this).run {
                block()
                val type = type.name
                val justified = if (rightJustified) "." else ""
                "$type:$justified$size$suffix"
            }
        }
    }
}

enum class State {
    alloc, allocated, cloud, comp, completing, down,

    /** for node in DRAINING or DRAINED states */
    drain,
    drained, draining, fail, future, futr, idle, maint, mix, mixed, no_respond, npc, perfctrs, planned, power_down,
    powering_down, powered_down, powering_up, reboot_issued, reboot_requested, resv, reserved, unk, unknown
}