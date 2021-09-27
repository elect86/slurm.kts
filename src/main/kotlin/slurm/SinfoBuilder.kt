package slurm

@SlurmMarker
class SinfoBuilder(val sinfo: Sinfo = Sinfo()) {

    /** Display information about all partitions. This causes information to be displayed about partitions that are
     *  configured as hidden and partitions that are unavailable to the user's group. */
    val all: Unit
        get() {
            sinfo.all = true
        }

    /** If set, only report state information for non-responding (dead) nodes. */
    val dead: Unit
        get() {
            sinfo.dead = true
        }

    /** If set, do not group node information on multiple nodes unless their configurations to be reported are identical.
     *  Otherwise cpu count, memory size, and disk space for nodes will be listed with the minimum value followed by a
     *  "+" for nodes with the same partition and state (e.g. "250+"). */
    val exact: Unit
        get() {
            sinfo.exact = true
        }

    /** Show all partitions from the federation if a member of one. */
    val federation: Unit
        get() {
            if (sinfo.local)
                println("`federation` is overridden by `local`")
            sinfo.federation = true
        }

    /** Do not print a header on the output. */
    val noHeader: Unit
        get() {
            sinfo.noHeader = true
        }

    /** Print a message describing all sinfo options. */
    val help: Unit
        get() {
            sinfo.help
        }

    /** Print the state on a periodic basis. Sleep for the indicated number of seconds between reports. By default
     *  prints a time stamp with the header. */
    var iterate: Second
        @Deprecated(message = "Write only property", level = DeprecationLevel.HIDDEN) get() = error("")
        set(value) {
            sinfo.iterate = value
        }

    /** Show only jobs local to this cluster. Ignore other clusters in this federation (if any). Overrides `federation`. */
    val local: Unit
        get() {
            if (sinfo.federation)
                println("`federation` is overridden")
            sinfo.local = true
        }

    /** Print more detailed information. This is ignored if the `format` option is specified. */
    val long: Unit
        get() {
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
    val dontConvert: Unit
        get() {
            sinfo.dontConvert = true
        }

    /** Print information in a node-oriented format with one line per node and partition. That is, if a node belongs to
     *  more than one partition, then one line for each node-partition pair will be shown. If `partition` is also
     *  specified, then only one line per node in this partition is shown. The default is to print information in a
     *  partition-oriented format. This is ignored if the `format` option is specified. */
    val node: Unit
        get() = when {
            sinfo.format.isNotEmpty() -> println("`node` is ignored because `format` is specified")
            else -> sinfo.node = true
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
    fun format(block: Sinfo.FormatBuilder.() -> Unit) = sinfo.FormatBuilder().block()

    /** Print information only about the specified partition(s). Multiple partitions are separated by commas. */
    fun partition(vararg partitions: String) {
        sinfo.partitions += partitions
    }

    /** If set only report state information for responding nodes. */
    val responding: Unit
        get() {
            sinfo.responding = true
        }

    /** List reasons nodes are in the down, drained, fail or failing state. When nodes are in these states Slurm
     *  supports the inclusion of a "reason" string by an administrator. This option will display the first 20
     *  characters of the reason field and list of nodes with that reason for all nodes that are, by default, down,
     *  drained, draining or failing. This option may be used with other node filtering options (e.g. `responding`,
     *  `dead`, `states`, `nodes`), however, combinations of these options that result in a list of nodes that are not
     *  down or drained or failing will not produce any output. When used with `long` the output additionally includes
     *  the current node state. */
    val listReasons: Unit
        get() {
            sinfo.listReasons = true
        }

    /** List only a partition state summary with no node state details. This is ignored if the `format` option is
     *  specified. */
    val summarize: Unit
        get() = when {
            sinfo.format.isNotEmpty() -> println("`summarize` is ignored because `format` is specified")
            else -> sinfo.summarize = true
        }

    /** Specification of the order in which records should be reported. This uses the same field specification as the
     *  <output_format>. Multiple sorts may be performed by listing multiple sort fields separated by commas. The field
     *  specifications may be preceded by "+" or "-" for ascending (default) and descending order respectively.
     *  The partition field specification, "P", may be preceded by a "#" to report partitions in the same order that
     *  they appear in Slurm's configuration file, slurm.conf. For example, a sort value of "+P,-m" requests that
     *  records be printed in order of increasing partition name and within a partition by decreasing memory size.
     *  The default value of sort is "#P,-t" (partitions ordered as configured then decreasing node state).
     *  If the `node` option is selected, the default sort value is "N" (increasing node name). */
    var sort: String
        @Deprecated(message = "Write only property", level = DeprecationLevel.HIDDEN) get() = error("")
        set(value) {
            sinfo.order = value
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
    val reservation: Unit
        get() {
            sinfo.reservation = true
        }

    /** Provide detailed event logging through program execution. */
    val verbose: Unit
        get() {
            sinfo.verbose = true
        }

    /** Print version information and exit. */
    val version: Unit
        get() {
            sinfo.version = true
        }
}