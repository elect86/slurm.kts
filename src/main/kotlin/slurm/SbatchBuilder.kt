package slurm

import java.io.File
import kotlin.time.Duration
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
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
    var contiguous: Boolean by sbatch::contiguous

    /** Restrict node selection to nodes with at least the specified number of cores per socket. See additional
     *  information under -B option above when task/affinity plugin is enabled.
     *  NOTE: This option may implicitly set the number of tasks (if -n was not specified) as one task per requested
     *  thread. */
    var coresPerSocket: Int by sbatch::coresPerSocket

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
    var cpuPerGput: Int by sbatch::cpusPerGpu

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
    fun exportFile(file: String) {
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

    /** Bind tasks to specific GPUs. By default every spawned task can access every GPU allocated to the job. */
    inline fun gpuBind(block: GpuBindBuilder.() -> Unit) = GpuBindBuilder().block()

    inner class GpuBindBuilder {
        /** Bind each task to the GPU(s) which are closest. In a NUMA environment, each task may be bound to more than
         *  one GPU (i.e. all GPUs in that NUMA environment). */
        fun closest() {
            sbatch.gpuBind = "closest"
        }

        /** Bind by setting GPU masks on tasks (or ranks) as specified where <list> is
         *  <gpu_id_for_task_0>,<gpu_id_for_task_1>,... GPU IDs are interpreted as decimal values unless they are
         *  preceded with '0x' in which case they interpreted as hexadecimal values. If the number of tasks (or ranks)
         *  exceeds the number of elements in this list, elements in the list will be reused as needed starting from the
         *  beginning of the list. To simplify support for large task counts, the lists may follow a map with an
         *  asterisk and repetition count. For example "map_cpu:0*4,1*4". Not supported unless the entire node is
         *  allocated to the job. */
        fun mapGpu(vararg gpuId: Int) {
            sbatch.gpuBind = "map_gpu:${gpuId.joinToString(",")}"
        }

        /** Bind by setting GPU masks on tasks (or ranks) as specified where <list> is
         *  <gpu_mask_for_task_0>,<gpu_mask_for_task_1>,... The mapping is specified for a node and identical mapping is
         *  applied to the tasks on every node (i.e. the lowest task ID on each node is mapped to the first mask
         *  specified in the list, etc.). GPU masks are always interpreted as hexadecimal values but can be preceded
         *  with an optional '0x'. Not supported unless the entire node is allocated to the job. To simplify support for
         *  large task counts, the lists may follow a map with an asterisk and repetition count. For example
         *  "mask_gpu:0x0f*4,0xf0*4". Not supported unless the entire node is allocated to the job. */
        fun maskGpu(vararg gpuMask: Int) {
            sbatch.gpuBind = "mask_gpu:${gpuMask.joinToString(",")}"
        }
    }

    /** Request that GPUs allocated to the job are configured with specific frequency values. This option can be
     *  used to independently configure the GPU and its memory frequencies. After the job is completed, the
     *  frequencies of all affected GPUs will be reset to the highest possible values. In some cases, system power
     *  caps may override the requested values. The field type can be "memory". If type is not specified, the GPU
     *  frequency is implied. The value field can either be "low", "medium", "high", "highm1" or a numeric value in
     *  megahertz (MHz). If the specified numeric value is not possible, a value as close as possible will be used.
     *  See below for definition of the values. The verbose option causes current GPU frequency information to be
     *  logged. Examples of use include "--gpu-freq=medium,memory=high" and "--gpu-freq=450". */
    fun gpuFreq(block: GpuFreqBuilder.() -> Unit) = GpuFreqBuilder().block()

    inner class GpuFreqBuilder {

        /** a numeric value in megahertz (MHz) */
        fun freq(mhz: MHz) {
            sbatch.gpuFreq += mhz.value.toString()
        }

        /** the lowest available frequency. */
        fun low() {
            sbatch.gpuFreq += "low"
        }

        /** attempts to set a frequency in the middle of the available range. */
        fun medium() {
            sbatch.gpuFreq += "medium"
        }

        /** the highest available frequency. */
        fun high() {
            sbatch.gpuFreq += "high"
        }

        /** (high minus one) will select the next highest available frequency. */
        fun highm1() {
            sbatch.gpuFreq += "highm1"
        }
    }

    /** Specify the number of GPUs required for the job on each node included in the job's resource allocation. An
     *  optional GPU type specification can be supplied. For example "--gpus-per-node=volta:3". Multiple options can
     *  be requested in a comma separated list, for example: "--gpus-per-node=volta:3,kepler:1". See also the --gpus,
     *  --gpus-per-socket and --gpus-per-task options. */
    fun gpusPerNode(vararg gpus: String) {
        sbatch.gpusPerNode += gpus
    }

    /** Specify the number of GPUs required for the job on each socket included in the job's resource allocation. An
     *  optional GPU type specification can be supplied. For example "--gpus-per-socket=volta:3". Multiple options can
     *  be requested in a comma separated list, for example: "--gpus-per-socket=volta:3,kepler:1". Requires job to
     *  specify a sockets per node count ( --sockets-per-node). See also the --gpus, --gpus-per-node and
     *  --gpus-per-task options. */
    fun gpusPerSocket(vararg gpus: String) {
        sbatch.gpusPerSocket += gpus
    }

    /** Specify the number of GPUs required for the job on each task to be spawned in the job's resource allocation. An
     *  optional GPU type specification can be supplied. This option requires the specification of a task count. For
     *  example "--gpus-per-task=volta:1". Multiple options can be requested in a comma separated list, for example:
     *  "--gpus-per-task=volta:3,kepler:1". Requires job to specify a task count (--nodes). See also the --gpus,
     *  --gpus-per-socket and --gpus-per-node options. */
    fun gpusPerTask(vararg gpus: String) {
        sbatch.gpusPerTask += gpus
    }

    /** Specifies a comma delimited list of generic consumable resources. The format of each entry on the list is
     *  "name[[:type]:count]". The name is that of the consumable resource. The count is the number of those resources
     *  with a default value of 1. The count can have a suffix of "k" or "K" (multiple of 1024), "m" or "M"
     *  (multiple of 1024 x 1024), "g" or "G" (multiple of 1024 x 1024 x 1024), "t" or "T" (multiple of 1024 x 1024 x
     *  1024 x 1024), "p" or "P" (multiple of 1024 x 1024 x 1024 x 1024 x 1024). The specified resources will be
     *  allocated to the job on each node. The available generic consumable resources is configurable by the system
     *  administrator. A list of available generic consumable resources will be printed and the command will exit if the
     *  option argument is "help". Examples of use include "--gres=gpu:2,mic:1", "--gres=gpu:kepler:2", and "--gres=help". */
    fun gres(vararg gres: String) {
        sbatch.gres += gres
    }

    /** Specify generic resource task binding options. */
    var gresFlags: Sbatch.GresFlag? by sbatch::gresFlags

    /** Specify the job is to be submitted in a held state (priority of zero). A held job can now be released using
     *  scontrol to reset its priority (e.g. "scontrol release <job_id>"). */
    fun hold() {
        sbatch.hold = true
    }

    /** Display help information and exit. */
    fun help() {
        sbatch.help = true
    }

    /** Bind tasks according to application hints. */
    var hint: Sbatch.Hint? by sbatch::hint

    /** Ignore any "#PBS" options specified in the batch script. */
    fun ignorePBS() {
        sbatch.ignorePBS = true
    }

    /** Instruct Slurm to connect the batch script's standard input directly to the file name specified in the
     *  "filename pattern".
     *  By default, "/dev/null" is open on the batch script's standard input and both standard output and standard error
     *  are directed to a file of the name "slurm-%j.out", where the "%j" is replaced with the job allocation number, as
     *  described below in the filename pattern section. */
    var input: String by sbatch::input

    /** Specify a name for the job allocation. The specified name will appear along with the job id number when querying
     *  running jobs on the system. The default is the name of the batch script, or just "sbatch" if the script is read
     *  on sbatch's standard input. */
    var jobName: String by sbatch::jobName

    /** Do not automatically terminate a job if one of the nodes it has been allocated fails. The user will assume the
     *  responsibilities for fault-tolerance should a node fail. When there is a node failure, any active job steps
     *  (usually MPI jobs) on that node will almost certainly suffer a fatal error, but with --no-kill, the job
     *  allocation will not be revoked so the user may launch new job steps on the remaining nodes in their allocation.
     *  Specify an optional argument of "off" disable the effect of the SBATCH_NO_KILL environment variable.
     *
     *  By default Slurm terminates the entire job allocation if any node fails in its range of allocated nodes. */
    fun dontKill(off: Boolean = false) {
        sbatch.dontKill = if (off) "off" else "true"
    }

    /** If a job has an invalid dependency and it can never run this parameter tells Slurm to terminate it or not.
     *  A terminated job state will be JOB_CANCELLED. If this option is not specified the system wide behavior applies.
     *  By default the job stays pending with reason DependencyNeverSatisfied or if the kill_invalid_depend is specified
     *  in slurm.conf the job is terminated. */
    fun killOnInvalidDep(boolean: Boolean) {
        sbatch.killOnInvalidDep = boolean.toString()
    }

    /** Specification of licenses (or other resources available on all nodes of the cluster) which must be allocated to
     *  this job. License names can be followed by a colon and count (the default count is one). Multiple license names
     *  should be comma separated (e.g. "--licenses=foo:4,bar"). To submit jobs using remote licenses, those served by
     *  the slurmdbd, specify the name of the server providing the licenses. For example "--license=nastran@slurmdb:12". */
    fun licenses(vararg licenses: String) {
        sbatch.licenses += licenses
    }

    /** Clusters to issue commands to. Multiple cluster names may be comma separated. The job will be submitted to the
     *  one cluster providing the earliest expected job initiation time. The default value is the current cluster. A
     *  value of 'all' will query to run on all clusters. Note the --export option to control environment variables
     *  exported between clusters. Note that the SlurmDBD must be up for this option to work properly. */
    fun clusters(vararg clusters: String) {
        sbatch.clusters += clusters
    }

    //    TODO arbitrary|<block|cyclic|plane=<options>[:block|cyclic|fcyclic]>
    //
    //    Specify alternate distribution methods for remote processes. In sbatch, this only sets environment variables that will be used by subsequent srun requests. This option controls the assignment of tasks to the nodes on which resources have been allocated, and the distribution of those resources to tasks for binding (task affinity). The first distribution method (before the ":") controls the distribution of resources across nodes. The optional second distribution method (after the ":") controls the distribution of resources across sockets within a node. Note that with select/cons_res, the number of cpus allocated on each socket and node may be different. Refer to the mc_support document for more information on resource allocation, assignment of tasks to nodes, and binding of tasks to CPUs.
    //
    //    First distribution method:
    //
    //    block
    //    The block distribution method will distribute tasks to a node such that consecutive tasks share a node. For example, consider an allocation of three nodes each with two cpus. A four-task block distribution request will distribute those tasks to the nodes with tasks one and two on the first node, task three on the second node, and task four on the third node. Block distribution is the default behavior if the number of tasks exceeds the number of allocated nodes.
    //    cyclic
    //    The cyclic distribution method will distribute tasks to a node such that consecutive tasks are distributed over consecutive nodes (in a round-robin fashion). For example, consider an allocation of three nodes each with two cpus. A four-task cyclic distribution request will distribute those tasks to the nodes with tasks one and four on the first node, task two on the second node, and task three on the third node. Note that when SelectType is select/cons_res, the same number of CPUs may not be allocated on each node. Task distribution will be round-robin among all the nodes with CPUs yet to be assigned to tasks. Cyclic distribution is the default behavior if the number of tasks is no larger than the number of allocated nodes.
    //    plane
    //    The tasks are distributed in blocks of a specified size. The options include a number representing the size of the task block. This is followed by an optional specification of the task distribution scheme within a block of tasks and between the blocks of tasks. The number of tasks distributed to each node is the same as for cyclic distribution, but the taskids assigned to each node depend on the plane size. For more details (including examples and diagrams), please see
    //    the mc_support document
    //    and
    //    https://slurm.schedmd.com/dist_plane.html
    //    arbitrary
    //    The arbitrary method of distribution will allocate processes in-order as listed in file designated by the environment variable SLURM_HOSTFILE. If this variable is listed it will override any other method specified. If not set the method will default to block. Inside the hostfile must contain at minimum the number of hosts requested and be one per line or comma separated. If specifying a task count (-n, --ntasks=<number>), your tasks will be laid out on the nodes in the order of the file.
    //    NOTE: The arbitrary distribution option on a job allocation only controls the nodes to be allocated to the job and not the allocation of CPUs on those nodes. This option is meant primarily to control a job step's task layout in an existing job allocation for the srun command.
    //
    //    Second distribution method:
    //    block
    //    The block distribution method will distribute tasks to sockets such that consecutive tasks share a socket.
    //    cyclic
    //    The cyclic distribution method will distribute tasks to sockets such that consecutive tasks are distributed over consecutive sockets (in a round-robin fashion). Tasks requiring more than one CPU will have all of those CPUs allocated on a single socket if possible.
    //    fcyclic
    //    The fcyclic distribution method will distribute tasks to sockets such that consecutive tasks are distributed over consecutive sockets (in a round-robin fashion). Tasks requiring more than one CPU will have each CPUs allocated in a cyclic fashion across sockets.
    fun distribution(distribution: String) {
        sbatch.distribution = distribution
    }

    /** Notify user by email when certain event types occur. Multiple type values may be specified in a comma separated
     *  list. The user to be notified is indicated with --mail-user. Unless the ARRAY_TASKS option is specified, mail
     *  notifications on job BEGIN, END and FAIL apply to a job array as a whole rather than generating individual email
     *  messages for each task in the job array. */
    fun mailTypes(vararg mailTypes: Sbatch.MailType) {
        sbatch.mailTypes += mailTypes
    }

    /** User to receive email notification of state changes as defined by --mail-type. The default value is the
     *  submitting user. */
    var mailUser: String by sbatch::mailUser

    /** Used only when the mcs/group plugin is enabled. This parameter is a group among the groups of the user. Default
     *  value is calculated by the Plugin mcs if it's enabled. */
    var mcsLabel: String by sbatch::mcsLabel

    /** Specify the real memory required per node. Default units are megabytes unless the SchedulerParameters
     *  configuration parameter includes the "default_gbytes" option for gigabytes. Different units can be specified
     *  using the suffix [K|M|G|T]. Default value is DefMemPerNode and the maximum value is MaxMemPerNode. If configured,
     *  both parameters can be seen using the scontrol show config command. This parameter would generally be used if
     *  whole nodes are allocated to jobs (SelectType=select/linear). Also see --mem-per-cpu and --mem-per-gpu.
     *  The --mem, --mem-per-cpu and --mem-per-gpu options are mutually exclusive. If --mem, --mem-per-cpu or
     *  --mem-per-gpu are specified as command line arguments, then they will take precedence over the environment.
     *  NOTE: A memory size specification of zero is treated as a special case and grants the job access to all of the
     *  memory on each node. If the job is allocated multiple nodes in a heterogeneous cluster, the memory limit on each
     *  node will be that of the node in the allocation with the smallest memory size (same limit will apply to every
     *  node in the job's allocation).
     *
     *  NOTE: Enforcement of memory limits currently relies upon the task/cgroup plugin or enabling of accounting, which
     *  samples memory use on a periodic basis (data need not be stored, just collected). In both cases memory use is
     *  based upon the job's Resident Set Size (RSS). A task may exceed the memory limit until the next periodic
     *  accounting sample. */
    var mem: String by sbatch::mem

    /** Minimum memory required per allocated CPU. Default units are megabytes unless the SchedulerParameters
     *  configuration parameter includes the "default_gbytes" option for gigabytes. Default value is DefMemPerCPU and
     *  the maximum value is MaxMemPerCPU (see exception below). If configured, both parameters can be seen using the
     *  scontrol show config command. Note that if the job's --mem-per-cpu value exceeds the configured MaxMemPerCPU,
     *  then the user's limit will be treated as a memory limit per task; --mem-per-cpu will be reduced to a value no
     *  larger than MaxMemPerCPU; --cpus-per-task will be set and the value of --cpus-per-task multiplied by the new
     *  --mem-per-cpu value will equal the original --mem-per-cpu value specified by the user. This parameter would
     *  generally be used if individual processors are allocated to jobs (SelectType=select/cons_res). If resources are
     *  allocated by the core, socket or whole nodes; the number of CPUs allocated to a job may be higher than the task
     *  count and the value of --mem-per-cpu should be adjusted accordingly. Also see --mem and --mem-per-gpu.
     *  The --mem, --mem-per-cpu and --mem-per-gpu options are mutually exclusive.
     *  NOTE:If the final amount of memory requested by job (eg.: when --mem-per-cpu use with --exclusive option) can't
     *  be satisfied by any of nodes configured in the partition, the job will be rejected. */
    var memPerCpu: String by sbatch::memPerCpu

    /** Minimum memory required per allocated GPU. Default units are megabytes unless the SchedulerParameters
     *  configuration parameter includes the "default_gbytes" option for gigabytes. Different units can be specified
     *  using the suffix [K|M|G|T]. Default value is DefMemPerGPU and is available on both a global and per partition
     *  basis. If configured, the parameters can be seen using the scontrol show config and scontrol show partition
     *  commands. Also see --mem. The --mem, --mem-per-cpu and --mem-per-gpu options are mutually exclusive. */
    var memPerGpu: String by sbatch::memPerGpu

    /** Bind tasks to memory. Used only when the task/affinity plugin is enabled and the NUMA memory functions are
     *  available. Note that the resolution of CPU and memory binding may differ on some architectures. For example, CPU
     *  binding may be performed at the level of the cores within a processor while memory binding will be performed at
     *  the level of nodes, where the definition of "nodes" may differ from system to system. By default no memory
     *  binding is performed; any task using any CPU can use any memory. This option is typically used to ensure that
     *  each task is bound to the memory closest to it's assigned CPU. The use of any type other than "none" or "local"
     *  is not recommended. If you want greater control, try running a simple test code with the options
     *  "--cpu-bind=verbose,none --mem-bind=verbose,none" to determine the specific configuration.
     *  NOTE: To have Slurm always report on the selected memory binding for all commands executed in a shell, you can
     *  enable verbose mode by setting the SLURM_MEM_BIND environment variable value to "verbose".
     *
     *  The following informational environment variables are set when --mem-bind is in use:
     *      SLURM_MEM_BIND_LIST
     *      SLURM_MEM_BIND_PREFER
     *      SLURM_MEM_BIND_SORT
     *      SLURM_MEM_BIND_TYPE
     *      SLURM_MEM_BIND_VERBOSE
     *  See the ENVIRONMENT VARIABLES section for a more detailed description of the individual SLURM_MEM_BIND* variables. */
    fun memBind(block: MemBindBuilder.() -> Unit) = MemBindBuilder().block()

    inner class MemBindBuilder {
        /** show this help message */
        fun help() {
            sbatch.memBind += "help"
        }

        /** Use memory local to the processor in use */
        fun local() {
            sbatch.memBind += "local"
        }

        /** Bind by setting memory masks on tasks (or ranks) as specified where <list> is <numa_id_for_task_0>,
         *  <numa_id_for_task_1>,... The mapping is specified for a node and identical mapping is applied to the tasks
         *  on every node (i.e. the lowest task ID on each node is mapped to the first ID specified in the list, etc.).
         *  NUMA IDs are interpreted as decimal values unless they are preceded with '0x' in which case they interpreted
         *  as hexadecimal values. If the number of tasks (or ranks) exceeds the number of elements in this list,
         *  elements in the list will be reused as needed starting from the beginning of the list. To simplify support
         *  for large task counts, the lists may follow a map with an asterisk and repetition count For example
         *  "map_mem:0x0f*4,0xf0*4". Not supported unless the entire node is allocated to the job. */
        fun mapMem(vararg idTasks: Int) {
            sbatch.memBind += idTasks.joinToString(",")
        }

        /** Bind by setting memory masks on tasks (or ranks) as specified where <list> is <numa_mask_for_task_0>,
         *  <numa_mask_for_task_1>,... The mapping is specified for a node and identical mapping is applied to the tasks
         *  on every node (i.e. the lowest task ID on each node is mapped to the first mask specified in the list, etc.).
         *  NUMA masks are always interpreted as hexadecimal values. Note that masks must be preceded with a '0x' if
         *  they don't begin with [0-9] so they are seen as numerical values. If the number of tasks (or ranks) exceeds
         *  the number of elements in this list, elements in the list will be reused as needed starting from the
         *  beginning of the list. To simplify support for large task counts, the lists may follow a mask with an
         *  asterisk and repetition count For example "mask_mem:0*4,1*4". Not supported unless the entire node is
         *  allocated to the job. */
        fun maskMem(vararg masks: Int) {
            sbatch.memBind += masks.joinToString(",")
        }

        /** don't bind tasks to memory (default) */
        fun none() {
            sbatch.memBind += "no"
        }

        /** Prefer use of first specified NUMA node, but permit use of other available NUMA nodes. */
        fun prefer() {
            sbatch.memBind += "p"
        }

        /** quietly bind before task runs (default) */
        fun quiet() {
            sbatch.memBind += "q"
        }

        /** bind by task rank (not recommended) */
        fun rank() {
            sbatch.memBind += "rank"
        }

        /** sort free cache pages (run zonesort on Intel KNL nodes) */
        fun sort() {
            sbatch.memBind += "sort"
        }

        /** verbosely report binding before task runs */
        fun verbose() {
            sbatch.memBind += "v"
        }
    }

    /** Specify a minimum number of logical cpus/processors per node. */
    var minCpus: Int by sbatch::minCpus

    /** Request that a minimum of minnodes nodes be allocated to this job. A maximum node count may also be specified
     *  with maxnodes. If only one number is specified, this is used as both the minimum and maximum node count. The
     *  partition's node limits supersede those of the job. If a job's node limits are outside of the range permitted
     *  for its associated partition, the job will be left in a PENDING state. This permits possible execution at a
     *  later time, when the partition limit is changed. If a job node limit exceeds the number of nodes configured in
     *  the partition, the job will be rejected. Note that the environment variable SLURM_JOB_NODES will be set to the
     *  count of nodes actually allocated to the job. See the ENVIRONMENT VARIABLES section for more information. If -N
     *  is not specified, the default behavior is to allocate enough nodes to satisfy the requirements of the -n and -c
     *  options. The job will be allocated as many nodes as possible within the range specified and without delaying the
     *  initiation of the job. The node count specification may include a numeric value followed by a suffix of "k"
     *  (multiplies numeric value by 1,024) or "m" (multiplies numeric value by 1,048,576). */
    fun nodes(minNodes: Int) {
        sbatch.nodes = minNodes..minNodes
    }

    /** Request that a minimum of minnodes nodes be allocated to this job. A maximum node count may also be specified
     *  with maxnodes. If only one number is specified, this is used as both the minimum and maximum node count. The
     *  partition's node limits supersede those of the job. If a job's node limits are outside of the range permitted
     *  for its associated partition, the job will be left in a PENDING state. This permits possible execution at a
     *  later time, when the partition limit is changed. If a job node limit exceeds the number of nodes configured in
     *  the partition, the job will be rejected. Note that the environment variable SLURM_JOB_NODES will be set to the
     *  count of nodes actually allocated to the job. See the ENVIRONMENT VARIABLES section for more information. If -N
     *  is not specified, the default behavior is to allocate enough nodes to satisfy the requirements of the -n and -c
     *  options. The job will be allocated as many nodes as possible within the range specified and without delaying the
     *  initiation of the job. The node count specification may include a numeric value followed by a suffix of "k"
     *  (multiplies numeric value by 1,024) or "m" (multiplies numeric value by 1,048,576). */
    var nodes: IntRange? by sbatch::nodes

    /** sbatch does not launch tasks, it requests an allocation of resources and submits a batch script. This option
     *  advises the Slurm controller that job steps run within the allocation will launch a maximum of number tasks and
     *  to provide for sufficient resources. The default is one task per node, but note that the --cpus-per-task option
     *  will change this default. */
    var nTasks: Int by sbatch::ntasks

    /** Specify information pertaining to the switch or network. The interpretation of type is system dependent. This
     *  option is supported when running Slurm on a Cray natively. It is used to request using Network Performance
     *  Counters. Only one value per request is valid. All options are case in-sensitive. In this configuration
     *  supported values include:
     *      system
     *          Use the system-wide network performance counters. Only nodes requested will be marked in use for the job
     *          allocation. If the job does not fill up the entire system the rest of the nodes are not able to be used
     *          by other jobs using NPC, if idle their state will appear as PerfCnts. These nodes are still available
     *          for other jobs not using NPC.
     *      blade
     *          Use the blade network performance counters. Only nodes requested will be marked in use for the job
     *          allocation. If the job does not fill up the entire blade(s) allocated to the job those blade(s) are not
     *          able to be used by other jobs using NPC, if idle their state will appear as PerfCnts. These nodes are
     *          still available for other jobs not using NPC.
     *
     *  In all cases the job allocation request must specify the --exclusive option. Otherwise the request will be
     *  denied.
     *
     *  Also with any of these options steps are not allowed to share blades, so resources would remain idle inside an
     *  allocation if the step running on a blade does not take up all the nodes on the blade.
     *
     *  The network option is also supported on systems with IBM's Parallel Environment (PE). See IBM's LoadLeveler job
     *  command keyword documentation about the keyword "network" for more information. Multiple values may be specified
     *  in a comma separated list. All options are case in-sensitive. Supported values include:
     *      BULK_XFER[=<resources>]
     *          Enable bulk transfer of data using Remote Direct-Memory Access (RDMA). The optional resources
     *          specification is a numeric value which can have a suffix of "k", "K", "m", "M", "g" or "G" for
     *          kilobytes, megabytes or gigabytes. NOTE: The resources specification is not supported by the underlying
     *          IBM infrastructure as of Parallel Environment version 2.2 and no value should be specified at this time.
     *      CAU=<count>
     *          Number of Collective Acceleration Units (CAU) required. Applies only to IBM Power7-IH processors.
     *          Default value is zero. Independent CAU will be allocated for each programming interface (MPI, LAPI, etc.)
     *      DEVNAME=<name>
     *          Specify the device name to use for communications (e.g. "eth0" or "mlx4_0").
     *      DEVTYPE=<type>
     *          Specify the device type to use for communications. The supported values of type are: "IB" (InfiniBand),
     *          "HFI" (P7 Host Fabric Interface), "IPONLY" (IP-Only interfaces), "HPCE" (HPC Ethernet), and "KMUX"
     *          (Kernel Emulation of HPCE). The devices allocated to a job must all be of the same type. The default
     *          value depends upon depends upon what hardware is available and in order of preferences is IPONLY (which
     *          is not considered in User Space mode), HFI, IB, HPCE, and KMUX.
     *      IMMED =<count>
     *          Number of immediate send slots per window required. Applies only to IBM Power7-IH processors. Default
     *          value is zero.
     *      INSTANCES =<count>
     *          Specify number of network connections for each task on each network connection. The default instance
     *          count is 1.
     *      IPV4
     *          Use Internet Protocol (IP) version 4 communications (default).
     *      IPV6
     *          Use Internet Protocol (IP) version 6 communications.
     *      LAPI
     *          Use the LAPI programming interface.
     *      MPI
     *          Use the MPI programming interface. MPI is the default interface.
     *      PAMI
     *          Use the PAMI programming interface.
     *      SHMEM
     *          Use the OpenSHMEM programming interface.
     *      SN_ALL
     *          Use all available switch networks (default).
     *      SN_SINGLE
     *          Use one available switch network.
     *      UPC
     *          Use the UPC programming interface.
     *      US
     *          Use User Space communications.
     *  Some examples of network specifications:
     *      Instances=2,US,MPI,SN_ALL
     *          Create two user space connections for MPI communications on every switch network for each task.
     *      US,MPI,Instances=3,Devtype=IB
     *          Create three user space connections for MPI communications on every InfiniBand network for each task.
     *      IPV4,LAPI,SN_Single
     *          Create a IP version 4 connection for LAPI communications on one switch network for each task.
     *      Instances=2,US,LAPI,MPI
     *          Create two user space connections each for LAPI and MPI communications on every switch network for each
     *          task. Note that SN_ALL is the default option so every switch network is used. Also note that Instances=2
     *          specifies that two connections are established for each protocol (LAPI and MPI) and each task. If there
     *          are two networks and four tasks on the node then a total of 32 connections are established (2 instances
     *          x 2 protocols x 2 networks x 4 tasks). */
    var network: String by sbatch::network

    /** Run the job with an adjusted scheduling priority within Slurm. With no adjustment value the scheduling priority
     *  is decreased by 100. A negative nice value increases the priority, otherwise decreases it. The adjustment range
     *  is +/- 2147483645. Only privileged users can specify a negative adjustment. */
    fun nice(adjustament: Int) {
        sbatch.nice = adjustament.toString()
    }

    /** Specifies that the batch job should never be requeued under any circumstances. Setting this option will prevent
     *  system administrators from being able to restart the job (for example, after a scheduled downtime), recover from
     *  a node failure, or be requeued upon preemption by a higher priority job. When a job is requeued, the batch
     *  script is initiated from its beginning. Also see the --requeue option. The JobRequeue configuration parameter
     *  controls the default behavior on the cluster. */
    fun noRequeue() {
        sbatch.noRequeue = true
    }

    /** Request the maximum ntasks be invoked on each core. Meant to be used with the --ntasks option. Related to
     *  --ntasks-per-node except at the core level instead of the node level. NOTE: This option is not supported unless
     *  SelectType=cons_res is configured (either directly or indirectly on Cray systems) along with the node's core
     *  count. */
    var nTasksPerCore: Int by sbatch::nTasksPerCore

    /** Request that ntasks be invoked on each node. If used with the --ntasks option, the --ntasks option will take
     *  precedence and the --ntasks-per-node will be treated as a maximum count of tasks per node. Meant to be used with
     *  the --nodes option. This is related to --cpus-per-task=ncpus, but does not require knowledge of the actual
     *  number of cpus on each node. In some cases, it is more convenient to be able to request that no more than a
     *  specific number of tasks be invoked on each node. Examples of this include submitting a hybrid MPI/OpenMP app
     *  where only one MPI "task/rank" should be assigned to each node while allowing the OpenMP portion to utilize all
     *  of the parallelism present in the node, or submitting a single setup/cleanup/monitoring job to each node of a
     *  pre-existing allocation as one step in a larger job script. */
    var nTasksPerNode: Int by sbatch::nTasksPerNode

    /** Request the maximum ntasks be invoked on each socket. Meant to be used with the --ntasks option. Related to
     *  --ntasks-per-node except at the socket level instead of the node level. NOTE: This option is not supported
     *  unless SelectType=cons_res is configured (either directly or indirectly on Cray systems) along with the node's
     *  socket count. */
    var nTasksPerSocket: Int by sbatch::nTasksPerSocket

    /** Overcommit resources. When applied to job allocation, only one CPU is allocated to the job per node and options
     *  used to specify the number of tasks per node, socket, core, etc. are ignored. When applied to job step
     *  allocations (the srun command when executed within an existing job allocation), this option can be used to
     *  launch more than one task per CPU. Normally, srun will not allocate more than one process per CPU. By specifying
     *  --overcommit you are explicitly allowing more than one process per CPU. However no more than MAX_TASKS_PER_NODE
     *  tasks are permitted to execute per node. NOTE: MAX_TASKS_PER_NODE is defined in the file slurm.h and is not a
     *  variable, it is set at Slurm build time. */
    fun overcommit() {
        sbatch.overcommit = true
    }

    /** Instruct Slurm to connect the batch script's standard output directly to the file name specified in the
     *  "filename pattern". By default both standard output and standard error are directed to the same file. For job
     *  arrays, the default file name is "slurm-%A_%a.out", "%A" is replaced by the job ID and "%a" with the array
     *  index. For other jobs, the default file name is "slurm-%j.out", where the "%j" is replaced by the job ID. See
     *  the filename pattern section below for filename specification options. */
    var output: String by sbatch::output

    /** Open the output and error files using append or truncate mode as specified. The default value is specified by
     *  the system configuration parameter JobFileAppend. */
    var openMode: Sbatch.OpenMode? by sbatch::openMode

    /** Outputs only the job id number and the cluster name if present. The values are separated by a semicolon. Errors
     *  will still be displayed. */
    var parsable: Boolean by sbatch::parsable

    /** Request a specific partition for the resource allocation. If not specified, the default behavior is to allow the
     *  slurm controller to select the default partition as designated by the system administrator. If the job can use
     *  more than one partition, specify their names in a comma separate list and the one offering earliest initiation
     *  will be used with no regard given to the partition name ordering (although higher priority partitions will be
     *  considered first). When the job is initiated, the name of the partition used will be placed first in the job
     *  record partition string. */
    fun partition(vararg partitions: String) {
        sbatch.partition += partitions
    }

    /** Comma separated list of power management plugin options. Currently available flags include: level (all nodes
     *  allocated to the job should have identical power caps, may be disabled by the Slurm configuration option
     *  PowerParameters=job_no_level). */
    fun power(flags: String) {
        sbatch.power += flags
    }

    /** Request a specific job priority. May be subject to configuration specific constraints. value should either be a
     *  numeric value or "TOP" (for highest possible value). Only Slurm operators and administrators can set the
     *  priority of a job. */
    var priority: String by sbatch::priority

    /** enables detailed data collection by the acct_gather_profile plugin. Detailed data are typically time-series that
     *  are stored in an HDF5 file for the job or an InfluxDB database depending on the configured plugin. */
    fun profiles(vararg profile: Sbatch.Profile) {
        sbatch.profiles += profile
    }

    /** Allows users to specify which of the modifiable (soft) resource limits to propagate to the compute nodes and
     *  apply to their jobs. If no rlimit is specified, then all resource limits will be propagated. */
    fun propagate(vararg propagate: Sbatch.Propagate) {
        sbatch.propagate += propagate
    }

    /** Request a quality of service for the job. QOS values can be defined for each user/cluster/account association in
     *  the Slurm database. Users will be limited to their association's defined set of qos's when the Slurm
     *  configuration parameter, AccountingStorageEnforce, includes "qos" in it's definition. */
    var qos: String by sbatch::qos

    /** Suppress informational messages from sbatch such as Job ID. Only errors will still be displayed. */
    var quite: Boolean by sbatch::quiet

    /** Force the allocated nodes to reboot before starting the job. This is only supported with some system
     *  configurations and will otherwise be silently ignored. */
    var reboot: Boolean by sbatch::reboot

    /** Specifies that the batch job should eligible to being requeue. The job may be requeued explicitly by a system
     *  administrator, after node failure, or upon preemption by a higher priority job. When a job is requeued, the
     *  batch script is initiated from its beginning. Also see the --no-requeue option. The JobRequeue configuration
     *  parameter controls the default behavior on the cluster. */
    var requeue: Boolean by sbatch::requeue

    /** Allocate resources for the job from the named reservation. */
    var reservation: String by sbatch::reservation

    /** The job allocation can over-subscribe resources with other running jobs. The resources to be over-subscribed can
     *  be nodes, sockets, cores, and/or hyperthreads depending upon configuration. The default over-subscribe behavior
     *  depends on system configuration and the partition's OverSubscribe option takes precedence over the job's option.
     *  This option may result in the allocation being granted sooner than if the --oversubscribe option was not set and
     *  allow higher system utilization, but application performance will likely suffer due to competition for resources.
     *  Also see the --exclusive option. */
    var oversubscribe: Boolean by sbatch::oversubscribe

    /** Count of specialized cores per node reserved by the job for system operations and not used by the application.
     *  The application will not use these cores, but will be charged for their allocation. Default value is dependent
     *  upon the node's configured CoreSpecCount value. If a value of zero is designated and the Slurm configuration
     *  option AllowSpecResourcesUsage is enabled, the job will be allowed to override CoreSpecCount and use the
     *  specialized resources on nodes it is allocated. This option can not be used with the --thread-spec option. */
    var coreSpec: Int by sbatch::coreSpec

    /** When a job is within sig_time seconds of its end time, send it the signal sig_num. Due to the resolution of
     *  event handling by Slurm, the signal may be sent up to 60 seconds earlier than specified. sig_num may either be a
     *  signal number or name (e.g. "10" or "USR1"). sig_time must have an integer value between 0 and 65535. By default,
     *  no signal is sent before the job's end time. If a sig_num is specified without any sig_time, the default time
     *  will be 60 seconds. Use the "B:" option to signal only the batch shell, none of the other processes will be
     *  signaled. By default all job steps will be signaled, but not the batch shell itself. To have the signal sent at
     *  preemption time see the preempt_send_user_signal SlurmctldParameter. */
    var signal: String by sbatch::signal

    /** Restrict node selection to nodes with at least the specified number of sockets. See additional information under
     * -B option above when task/affinity plugin is enabled. */
    var socketsPerNode: Int by sbatch::socketsPerNode

    /** Spread the job allocation over as many nodes as possible and attempt to evenly distribute tasks across the
     *  allocated nodes. This option disables the topology/tree plugin. */
    var spreadJob: Boolean by sbatch::spreadJob

    /** When a tree topology is used, this defines the maximum count of switches desired for the job allocation and
     *  optionally the maximum time to wait for that number of switches. If Slurm finds an allocation containing more
     *  switches than the count specified, the job remains pending until it either finds an allocation with desired
     *  switch count or the time limit expires. It there is no switch count limit, there is no delay in starting the
     *  job. Acceptable time formats include "minutes", "minutes:seconds", "hours:minutes:seconds", "days-hours",
     *  "days-hours:minutes" and "days-hours:minutes:seconds". The job's maximum time delay may be limited by the system
     *  administrator using the SchedulerParameters configuration parameter with the max_switch_wait parameter option.
     *  On a dragonfly network the only switch count supported is 1 since communication performance will be highest when
     *  a job is allocate resources on one leaf switch or more than 2 leaf switches. The default max-time is the
     *  max_switch_wait SchedulerParameters. */
    var time: Duration by sbatch::time

    /** Validate the batch script and return an estimate of when a job would be scheduled to run given the current job
     *  queue and all the other arguments specifying the job requirements. No job is actually submitted. */
    var testOnly: Boolean by sbatch::testOnly

    /** Count of specialized threads per node reserved by the job for system operations and not used by the application.
     *  The application will not use these threads, but will be charged for their allocation. This option can not be
     *  used with the --core-spec option. */
    var threadSpec: Int by sbatch::threadSpec

    /** Restrict node selection to nodes with at least the specified number of threads per core. NOTE: "Threads" refers
     *  to the number of processing units on each core rather than the number of application tasks to be launched per
     *  core. See additional information under -B option above when task/affinity plugin is enabled. */
    var threadsPerCore: Int by sbatch::threadsPerCore

    /** Set a minimum time limit on the job allocation. If specified, the job may have it's --time limit lowered to a
     *  value no lower than --time-min if doing so permits the job to begin execution earlier than otherwise possible.
     *  The job's time limit will not be changed after the job is allocated resources. This is performed by a backfill
     *  scheduling algorithm to allocate resources otherwise reserved for higher priority jobs. Acceptable time formats
     *  include "minutes", "minutes:seconds", "hours:minutes:seconds", "days-hours", "days-hours:minutes" and
     *  "days-hours:minutes:seconds". */
    var timeMin: Duration by sbatch::timeMin

    /** Specify a minimum amount of temporary disk space per node. Default units are megabytes unless the
     *  SchedulerParameters configuration parameter includes the "default_gbytes" option for gigabytes. Different units
     *  can be specified using the suffix [K|M|G|T]. */
    var tmp: String by sbatch::tmp

    /** Display brief help message and exit. */
    fun usage() {
        sbatch.usage = true
    }

    /** Attempt to submit and/or run a job as user instead of the invoking user id. The invoking user's credentials will
     *  be used to check access permissions for the target partition. User root may use this option to run jobs as a
     *  normal user in a RootOnly partition for example. If run as root, sbatch will drop its permissions to the uid
     *  specified after node allocation is successful. user may be the user name or numerical user ID. */
    val uid: String by sbatch::uid

    /** If a range of node counts is given, prefer the smaller count. */
    fun useMinNodes() {
        sbatch.useMinNodes = true
    }

    /** Display version information and exit. */
    fun version() {
        sbatch.version
    }

    /** Increase the verbosity of sbatch's informational messages. Multiple -v's will further increase sbatch's
     *  verbosity. By default only errors will be displayed. */
    fun verbose() {
        sbatch.verbose
    }

    /** Request a specific list of hosts. The job will contain all of these hosts and possibly additional hosts as
     *  needed to satisfy resource requirements. The list may be specified as a comma-separated list of hosts, a range
     *  of hosts (host[1-5,7,...] for example), or a filename. The host list will be assumed to be a filename if it
     *  contains a "/" character. If you specify a minimum node or processor count larger than can be satisfied by the
     *  supplied host list, additional resources will be allocated on other nodes as needed. Duplicate node names in
     *  the list will be ignored. The order of the node names in the list is not important; the node names will be
     *  sorted by Slurm. */
    fun nodeList(vararg hosts: String) {
        sbatch.nodeList += hosts
    }

    /** Do not exit until the submitted job terminates. The exit code of the sbatch command will be the same as the exit
     *  code of the submitted job. If the job terminated due to a signal rather than a normal exit, the exit code will
     *  be set to 1. In the case of a job array, the exit code recorded will be the highest value for any task in the
     *  job array. */
    fun wait_() {
        sbatch.wait = true
    }

    /** Controls when the execution of the command begins. By default the job will begin execution as soon as the
     *  allocation is made.
     *      false
     *          Begin execution as soon as allocation can be made. Do not wait for all nodes to be ready for use
     *          (i.e. booted).
     *      true
     *          Do not begin execution until all nodes are ready for use. */
    fun waitAllNodes(value: Boolean) {
        sbatch.waitAllNodes = value
    }

    /** Specify wckey to be used with job. If TrackWCKey=no (default) in the slurm.conf this value is ignored. */
    var wcKey: String by sbatch::wckey

    /** Sbatch will wrap the specified command string in a simple "sh" shell script, and submit that script to the slurm
     *  controller. When --wrap is used, a script name and arguments may not be specified on the command line; instead
     *  the sbatch-generated wrapper script is used. */
    var wrap: String by sbatch::wrap

    /** Explicitly exclude certain nodes from the resources granted to the job. */
    fun exclude(vararg nodes: String) {
        sbatch.exclude += nodes
    }
}
































