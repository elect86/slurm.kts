package slurm

import java.io.File
import java.time.LocalDateTime

class SbatchBuilder(val sbatch: Sbatch = Sbatch()) {

    var mutuallyExclusive0: MutuallyExclusive0? = null

    enum class MutuallyExclusive0 { extraNodeInfo, hint, threadsPerCore, tasksPerCore }

    fun mutuallyExclusive0(excl: MutuallyExclusive0) {
        if (mutuallyExclusive0 != null) {
            if (mutuallyExclusive0 != excl)
                error("$excl is mutually exclusive with ::extraNodeInfo, ::hint, ::threadsPerCore and ::tasksPerCore")
        } else mutuallyExclusive0 = excl
    }

    /** Submit a job array, multiple jobs to be executed with identical parameters. The indexes specification identifies
     *  what array index values should be used. Multiple values may be specified using a comma separated list and/or an
     *  IntRange.
     *  @sample
     *      array(0..15)
     *      array(0, 6, 16..32)
     *      array(0..15 step 4)
     *  TODO: A maximum number of simultaneously running tasks from the job array may be specified using a "%" separator.
     *  For example "--array=0-15%4" will limit the number of simultaneously running tasks from this job array to 4.
     *  The minimum index value is 0. the maximum value is one less than the configuration parameter MaxArraySize.
     *  NOTE: currently, federated job arrays only run on the local cluster.     */
    fun array(vararg indexes: Any) {
        for (index in indexes)
            when (index) {
                is Int -> sbatch.array += index
                is IntRange -> for (i in index) sbatch.array += i
                else -> error("$index type is invalid, it must be either an Int or an IntRange")
            }
    }

    /** Charge resources used by this job to specified account. The account is an arbitrary string. The account name may
     *  be changed after job submission using the scontrol command.     */
    var account: String by sbatch::account

    /** Define the job accounting and profiling sampling intervals. This can be used to override the
     *  JobAcctGatherFrequency parameter in Slurm's configuration file, slurm.conf. */
    fun acctgFreq(vararg dataType: DataType) {
        sbatch.dataTypes += dataType
    }

    /** Restrict node selection to nodes with at least the specified number of sockets, cores per socket and/or threads
     *  per core.
     *  NOTE: These options do not specify the resource allocation size. Each value specified is considered a minimum.
     *  An asterisk (*) can be used as a placeholder indicating that all available resources of that type are to be
     *  utilized.
     *  If task/affinity plugin is enabled, then specifying an allocation in this manner also results in subsequently
     *  launched tasks being bound to threads if the -B option specifies a thread count, otherwise an option of cores
     *  if a core count is specified, otherwise an option of sockets. If SelectType is configured to select/cons_res,
     *  it must have a parameter of CR_Core, CR_Core_Memory, CR_Socket, or CR_Socket_Memory for this option to be honored.
     *  If not specified, the scontrol show job will display 'ReqS:C:T=*:*:*'. This option applies to job allocations.
     *  NOTE: This option is mutually exclusive with --hint, --threads-per-core and --ntasks-per-core.
     *  NOTE: This option may implicitly set the number of tasks (if -n was not specified) as one task per requested thread. */
    fun extraNodeInfo(sockets: Int, cores: Int? = null, threads: Int? = null) {
        mutuallyExclusive0(MutuallyExclusive0.extraNodeInfo)
        sbatch.extraNodeInfo = "$sockets"
        cores?.let { sbatch.extraNodeInfo += it }
        threads?.let { sbatch.extraNodeInfo += it }
    }

    /** Restrict node selection to nodes with at least the specified number of sockets, cores per socket and/or threads
     *  per core.
     *  NOTE: These options do not specify the resource allocation size. Each value specified is considered a minimum.
     *  An asterisk (*) can be used as a placeholder indicating that all available resources of that type are to be
     *  utilized. Values can also be specified as min-max.
     *  If task/affinity plugin is enabled, then specifying an allocation in this manner also results in subsequently
     *  launched tasks being bound to threads if the -B option specifies a thread count, otherwise an option of cores
     *  if a core count is specified, otherwise an option of sockets. If SelectType is configured to select/cons_res,
     *  it must have a parameter of CR_Core, CR_Core_Memory, CR_Socket, or CR_Socket_Memory for this option to be honored.
     *  If not specified, the scontrol show job will display 'ReqS:C:T=*:*:*'. This option applies to job allocations.
     *  NOTE: This option is mutually exclusive with --hint, --threads-per-core and --ntasks-per-core.
     *  NOTE: This option may implicitly set the number of tasks (if -n was not specified) as one task per requested thread. */
    fun extraNodeInfo(socketsCoresThreads: String) {
        mutuallyExclusive0(MutuallyExclusive0.extraNodeInfo)
        sbatch.extraNodeInfo = socketsCoresThreads
    }

    /** Nodes can have features assigned to them by the Slurm administrator. Users can specify which of these features
     *  are required by their batch script using this options. For example a job's allocation may include both Intel
     *  Haswell and KNL nodes with features "haswell" and "knl" respectively. On such a configuration the batch script
     *  would normally benefit by executing on a faster Haswell node. This would be specified using the option
     *  `batch("haswell")`. The specification can include AND and OR operators using the ampersand and vertical bar
     *  separators. For example: `batch("haswell|broadwell")` or `batch("haswell|big_memory")`.
     *  The `::batch` argument must be a subset of the job's `::constraint` argument (i.e. the job can not request only
     *  KNL nodes, but require the script to execute on a Haswell node). If the request can not be satisfied from the
     *  resources allocated to the job, the batch script will execute on the first node of the job allocation. */
    var batch: String by sbatch::batch

    /** Burst buffer specification. The form of the specification is system dependent. Also see `::bbf`. When the `::bb`
     *  option is used, Slurm parses this option and creates a temporary burst buffer script file that is used
     *  internally by the burst buffer plugins. See Slurm's burst buffer guide for more information and examples:
     *  https://slurm.schedmd.com/burst_buffer.html     */
    var bb: String by sbatch::bb

    /** Path of file containing burst buffer specification. The form of the specification is system dependent.
     *  These burst buffer directives will be inserted into the submitted batch script. See Slurm's burst buffer guide
     *  for more information and examples: https://slurm.schedmd.com/burst_buffer.html */
    var bbf: String by sbatch::bbf

    /** Submit the batch script to the Slurm controller immediately, like normal, but tell the controller to defer the
     *  allocation of the job until the specified time.
     *  (If that time is already past, the next day is assumed.) TODO You may also specify midnight, noon, fika (3 PM)
     *  or teatime (4 PM) and you can have a time-of-day suffixed with AM or PM for running in the morning or the evening.
     *  You can also say what day the job will be run, by specifying a date of the form MMDDYY or MM/DD/YY YYYY-MM-DD.
     *  Combine date and time using the following format YYYY-MM-DD[THH:MM[:SS]]. TODO You can also give times like
     *  now + count time-units, where the time-units can be seconds (default), minutes, hours, days, or weeks and you
     *  can tell Slurm to run the job today with the keyword today and to run the job tomorrow with the keyword tomorrow.
     *  The value may be changed after job submission using the scontrol command. For example: TODO
     *     --begin=16:00
     *     --begin=now+1hour
     *     --begin=now+60           (seconds by default)
     *     --begin=2010-01-20T12:34:00
     *  Notes on date/time specifications:
    - Although the 'seconds' field of the HH:MM:SS time specification is allowed by the code, note that the poll time of the Slurm scheduler is not precise enough to guarantee dispatch of the job on the exact second. The job will be eligible to start on the next poll following the specified time. The exact poll interval depends on the Slurm scheduler (e.g., 60 seconds with the default sched/builtin).
    - If no time (HH:MM:SS) is specified, the default is (00:00:00).
    - If a date is specified without a year (e.g., MM/DD) then the current year is assumed, unless the combination of MM/DD and HH:MM:SS has already passed for that year, in which case the next year is used.
     *  */
    var begin: LocalDateTime by sbatch::begin

    /** Specifies features that a federated cluster must have to have a sibling job submitted to it. Slurm will attempt
     *  to submit a sibling job to a cluster if it has at least one of the specified features. If the "!" option is
     *  included, Slurm will attempt to submit a sibling job to a cluster that has none of the specified features. */
    var clusterConstraint: String by sbatch::clusterConstraint

    /**  An arbitrary comment enclosed in double quotes if using spaces or some special characters. */
    var comment: String by sbatch::comment

    /** Nodes can have features assigned to them by the Slurm administrator. Users can specify which of these features
     *  are required by their job using the constraint option. Only nodes having features matching the job constraints
     *  will be used to satisfy the request. Multiple constraints may be specified with AND, OR, matching OR, resource
     *  counts, etc. (some operators are not supported on all system types). Supported constraint options include:
     *  Single Name
     *      Only nodes which have the specified feature will be used. For example, `constraint = "intel"`
     *  Node Count
     *      A request can specify the number of nodes needed with some feature by appending an asterisk and count after
     *      the feature name. For example, `nodes=16; constraint="graphics*4 ..."` indicates that the job requires 16
     *      nodes and that at least four of those nodes must have the feature "graphics."
     *  AND
     *      If only nodes with all of specified features will be used. The ampersand is used for an AND operator.
     *      For example, `constraint="intel&gpu"`
     *  OR
     *      If only nodes with at least one of specified features will be used. The vertical bar is used for an OR
     *      operator. For example, `constraint="intel|amd"`
     *  Matching OR
     *      If only one of a set of possible options should be used for all allocated nodes, then use the OR operator
     *      and enclose the options within square brackets. For example, `constraint="[rack1|rack2|rack3|rack4]"` might
     *      be used to specify that all nodes must be allocated on a single rack of the cluster, but any of those four
     *      racks can be used.
     *  Multiple Counts
     *      Specific counts of multiple resources may be specified by using the AND operator and enclosing the options
     *      within square brackets. For example, `constraint="[rack1*2&rack2*4]"` might be used to specify that two
     *      nodes must be allocated from nodes with the feature of "rack1" and four nodes must be allocated from nodes
     *      with the feature "rack2".
     *      NOTE: This construct does not support multiple Intel KNL NUMA or MCDRAM modes. For example, while
     *      `constraint="[(knl&quad)*2&(knl&hemi)*4]"` is not supported, `constraint="[haswell*2&(knl&hemi)*4]"` is
     *      supported. Specification of multiple KNL modes requires the use of a heterogeneous job.
     *  Brackets
     *      Brackets can be used to indicate that you are looking for a set of nodes with the different requirements
     *      contained within the brackets. For example, `constraint="[(rack1|rack2)*1&(rack3)*2]"` will get you one node
     *      with either the "rack1" or "rack2" features and two nodes with the "rack3" feature. The same request without
     *      the brackets will try to find a single node that meets those requirements.
     *  Parenthesis
     *      Parenthesis can be used to group like node features together. For example,
     *      `constraint="[(knl&snc4&flat)*4&haswell*1]"` might be used to specify that four nodes with the features
     *      `knl`, `snc4` and `flat` plus one node with the feature `haswell` are required. All options within
     *      parenthesis should be grouped with AND (e.g. "&") operands. */
    var constraint: String by sbatch::constraint

    /**  Path to OCI container bundle. */
    var container: File? by sbatch::container

    /** If set, then the allocated nodes must form a contiguous set.
     *  NOTE: If SelectPlugin=cons_res this option won't be honored with the topology/tree or topology/3d_torus plugins,
     *  both of which can modify the node ordering. */
    var contiguous: Boolean? by sbatch::contiguous

    /** Restrict node selection to nodes with at least the specified number of cores per socket. See additional
     *  information under -B option above when task/affinity plugin is enabled.
     *  NOTE: This option may implicitly set the number of tasks (if -n was not specified) as one task per requested
     *  thread. */
    var coresPerSocket: Int? by sbatch::coresPerSocket

    // TODO check docs p1, p2 and p3
    /** Request that job steps initiated by srun commands inside this sbatch script be run at some requested frequency
     *  if possible, on the CPUs selected for the step on the compute node(s). */
    fun cpuFreq(freq: kHz) {
        sbatch.cpuFreq = freq.toString()
    }

    /** Request that job steps initiated by srun commands inside this sbatch script be run at some requested frequency
     *  if possible, on the CPUs selected for the step on the compute node(s). */
    fun cpuFreq(policy: FrequencyPolicy) {
        sbatch.cpuFreq = policy.toString()
    }

    /** Advise Slurm that ensuing job steps will require ncpus processors per allocated GPU. Not compatible with the
     *  `cpusPerTask` option. */
    var cpuPerGput: Int? by sbatch::cpusPerGpu

    /** Advise the Slurm controller that ensuing job steps will require ncpus number of processors per task. Without
     *  this option, the controller will just try to allocate one processor per task.
     *  For instance, consider an application that has 4 tasks, each requiring 3 processors. If our cluster is comprised
     *  of quad-processors nodes and we simply ask for 12 processors, the controller might give us only 3 nodes.
     *  However, by using the `cpusPerTask=3` options, the controller knows that each task requires 3 processors on the
     *  same node, and the controller will grant an allocation of 4 nodes, one for each of the 4 tasks. */
    var cpuPerTask: Int? by sbatch::cpusPerTask

    /** Remove the job if no ending is possible before this deadline (start > (deadline - time[-min])).
     *  Default is no deadline */
    var deadline: LocalDateTime by sbatch::deadline

    /** Do not reboot nodes in order to satisfied this job's feature specification if the job has been eligible to run
     *  for less than this time period. If the job has waited for less than the specified period, it will use only nodes
     *  which already have the specified features. The argument is in units of minutes. A default value may be set by a
     *  system administrator using the delay_boot option of the SchedulerParameters configuration parameter in the
     *  slurm.conf file, otherwise the default value is zero (no delay). */
    var delayBoot: Minute? by sbatch::delayBoot

    /** Defer the start of this job until the specified dependencies have been satisfied completed.
     *  TODO ? Any dependency may be satisfied if the "?" separator is used.
     *  Many jobs can share the same dependency and these jobs may even belong to different users. The value may be
     *  changed after job submission using the scontrol command. Dependencies on remote jobs are allowed in a federation.
     *  Once a job dependency fails due to the termination state of a preceding job, the dependent job will never be run,
     *  even if the preceding job is requeued and has a different termination state in a subsequent execution. */
    inline fun dependency(block: DependencyBuilder.() -> Unit) = DependencyBuilder(sbatch).block()

    /** Set the working directory of the batch script to directory before it is executed. The path can be specified as
     *  full path or relative path to the directory where the command is executed. */
    var chdir: File? by sbatch::chDir

    /** Instruct Slurm to connect the batch script's standard error directly to the file name specified in the
     *  "filename pattern". By default both standard output and standard error are directed to the same file. For job
     *  arrays, the default file name is "slurm-%A_%a.out", "%A" is replaced by the job ID and "%a" with the array index.
     *  For other jobs, the default file name is "slurm-%j.out", where the "%j" is replaced by the job ID. See the
     *  filename pattern section below for filename specification options. */
    var error: File? by sbatch::error

    /** The job allocation can not share nodes with other running jobs (or just other users with the "=user" option or
     *  with the "=mcs" option). The default shared/exclusive behavior depends on system configuration and the
     *  partition's OverSubscribe option takes precedence over the job's option. */
    fun exclusive(with: Exclusive = Exclusive.otherRunningJobs) {
        sbatch.exclusive = with
    }

    /** Identify which environment variables from the submission environment are propagated to the launched application.
     *  Note that SLURM_* variables are always propagated.
     *
     *  export("ALL")
     *      Default mode if `::export` is not specified. All of the user's environment will be loaded (either from the
     *      caller's environment or from a clean environment if `::getUserEnv` is specified).
     *  export("NONE")
     *      Only SLURM_* variables from the user environment will be defined. User must use absolute path to the binary
     *      to be executed that will define the environment. User can not specify explicit environment variables with
     *      NONE. `::getUserEnv` will be ignored. This option is particularly important for jobs that are submitted on
     *      one cluster and execute on a different cluster (e.g. with different paths). To avoid steps inheriting
     *      environment export settings (e.g. NONE) from sbatch command, the environment variable SLURM_EXPORT_ENV
     *      should be set to ALL in the job script.
     *  export(["ALL",]environment variables)
     *      Exports all SLURM_* environment variables along with explicitly defined variables. Multiple environment
     *      variable names should be comma separated. Environment variable names may be specified to propagate the
     *      current value (e.g. `export("EDITOR")`) or specific values may be exported (e.g.
     *      `export("EDITOR=/bin/emacs`). If `"ALL"` is specified, then all user environment variables will be loaded
     *      and will take precedence over any explicitly given environment variables.
     *
     *  Example: `export("EDITOR","ARG1=test")`
     *      In this example, the propagated environment will only contain the variable `EDITOR` from the user's
     *      environment, `SLURM_*` environment variables, and `ARG1=test`.
     *  Example: `export("ALL","EDITOR=/bin/emacs")`
     *      There are two possible outcomes for this example. If the caller has the EDITOR environment variable defined,
     *      then the job's environment will inherit the variable from the caller's environment. If the caller doesn't
     *      have an environment variable defined for EDITOR, then the job's environment will use the value given by
     *      `::export`. */
    fun export(export: String) {
        sbatch.export += export
    }

    /** If a number between 3 and OPEN_MAX is specified as the argument to this option, a readable file descriptor will
     *  be assumed (STDIN and STDOUT are not supported as valid arguments). Otherwise a filename is assumed. Export
     *  environment variables defined in <filename> or read from <fd> to the job's execution environment. The content is
     *  one or more environment variable definitions of the form NAME=value, each separated by a null character. This
     *  allows the use of special characters in environment definitions. */
    fun exportFile(file: String?) {
        sbatch.exportFile = file
    }

    /** Much like `::nodelist`, but the list is contained in a file of name node file. The node names of the list may
     *  also span multiple lines in the file. Duplicate node names in the file will be ignored. The order of the node
     *  names in the list is not important; the node names will be sorted by Slurm. */
    fun nodeFile(file: File?) {
        sbatch.nodeFile = file
    }

    /** This option will tell sbatch to retrieve the login environment variables for the user specified in the --uid
     *  option. The environment variables are retrieved by running something of this sort
     *  `su - <username> -c /usr/bin/env` and parsing the output. Be aware that any environment variables already set in
     *  sbatch's environment will take precedence over any environment variables in the user's login environment.
     *  Clear any environment variables before calling sbatch that you do not want propagated to the spawned program.
     *  The optional timeout value is in seconds. Default value is 8 seconds. The optional mode value control the "su"
     *  options. With a mode value of "S", "su" is executed without the "-" option. With a mode value of "L", "su" is
     *  executed with the "-" option, replicating the login environment. If mode not specified, the mode established at
     *  Slurm build time is used. Example of use include `getUserEnv()`, `getUserEnv("10")`, `getUserEnv("10L")` and
     *  `getUserEnv("S")`. */
    fun getUserEnv(timeoutMode: String? = "") {
        sbatch.getUserEnv = timeoutMode
    }

    /** If sbatch is run as root, and the --gid option is used, submit the job with group's group access permissions.
     *  `group` may be the group name or the numerical group ID. */
    fun groupID(groupID: String) {
        sbatch.groupID = groupID
    }

    /** Specify the total number of GPUs required for the job. An optional GPU type specification can be supplied.
     *  For example `gpus("volta:3")`. Multiple options can be requested in a comma separated list, for example:
     *  `gpus("volta:3", "kepler:1")`. See also the `::gpusPerNode`, `::gpusPerSocket` and `gpusPerTask` options. */
    fun gpus(vararg typeNumbers: String) {
        for (typeNumber in typeNumbers)
            sbatch.gpus += typeNumber
    }

    inline fun gpuBind(block: GpuBindBuilder.() -> Unit) {

    }

    inner class GpuBindBuilder {
        fun closest() {
            sbatch.gpuBind = "closest"
        }
        fun mapGpu(vararg gpuId: Int) {
            sbatch.gpuBind = "map_gpu:${gpuId.joinToString(",")}"
        }
//        fun maskGpu(vararg gpuMask)
    }
}































