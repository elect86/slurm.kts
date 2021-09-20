package slurm


fun main() {
    squeue {
//        partitions("intel")
//        states(Squeue.JobStateCode.PENDING)
//        format("%.6i", "%p")

        steps()
        partitions("intel")
        sort("u")
    }
}

inline fun squeue(block: SqueueBuilder.() -> Unit) {
    val squeue = Squeue()
    SqueueBuilder(squeue).block()
    squeue()
}

@SlurmMarker
class Squeue {
    val accounts = ArrayList<String>()
    var all = false
    var array = false
    var arrayUnique = false
    var federation = false
    var noHeader = false
    var help = false
    var hide = false
    var iterate = Second(0)
    val jobs = ArrayList<String>()
    var local = false
    var long = false
    val licenses = ArrayList<String>()
    val clusters = ArrayList<String>()
    val names = ArrayList<String>()
    var dontConvert = false
    val format = ArrayList<String>()
    val partitions = ArrayList<String>()
    var priority = false
    val qos = ArrayList<String>()
    var reservation = ""
    val steps = ArrayList<String>()
    var sibling = false
    var order = ""
    var start = false
    val states = ArrayList<JobStateCode>()
    val users = ArrayList<String>()
    var verbose = false
    var version = false
    var nodelist = ""

    operator fun invoke() {
        val cmd = buildString {
            append("squeue")
            if (accounts.isNotEmpty()) append(" -A ${accounts.joinToString(",")}")
            if (all) append(" -a")
            if (array) append(" -r")
            if (arrayUnique) append(" --array-unique")
            if (federation) append(" --federation")
            if (noHeader) append(" -h")
            if (help) append(" --help")
            if (hide) append(" --hide")
            if (iterate != Second(0)) append(" -i $iterate")
            if (jobs.isNotEmpty()) append(" -j${jobs.joinToString(",")}")
            if (local) append(" --local")
            if (long) append(" --l")
            if (licenses.isNotEmpty()) append(" -L${licenses.joinToString(",")}")
            if (clusters.isNotEmpty()) append(" -M ${clusters.joinToString(",")}")
            if (names.isNotEmpty()) append(" -n ${names.joinToString(",")}")
            if (dontConvert) append(" --noconvert")
            if (format.isNotEmpty()) append(when {
                                                '%' in format[0] -> " -o \"${format.joinToString(" ")}\""
                                                else -> " -O \"${format.joinToString(",")}\""
                                            })
            if (partitions.isNotEmpty()) append(" -p ${partitions.joinToString(",")}")
            if (priority) append(" -P")
            if (qos.isNotEmpty()) append(" -q ${qos.joinToString(",")}")
            if (reservation.isNotEmpty()) append(" -R $reservation")
            if (steps.isNotEmpty()) append(" -s${steps.joinToString(",")}")
            if (sibling) append(" --sibling")
            if (order.isNotEmpty()) append(" -S $order")
            if (start) append(" --start")
            if (states.isNotEmpty()) append(" -t ${states.joinToString(",")}")
            if (users.isNotEmpty()) append(" -u ${users.joinToString(",")}")
            if (verbose) append(" -v")
            if (version) append(" -V")
            if (nodelist.isNotEmpty()) append(" -w $nodelist")
        }
        println("running `$cmd`")
        println(cmd.invoke())
    }

    @SlurmMarker
    class Format(val type: Type) {
        var size = 20
        var rightJustified = false
        var suffix = ""

        enum class Type {
            /** Print the account associated with the job. (Valid for jobs only) */
            Account,

            /** Print the accrue time associated with the job. (Valid for jobs only) */
            AccrueTime,

            /** Administrator comment associated with the job. (Valid for jobs only) */
            admin_comment,

            /** Print the nodes allocated to the job. (Valid for jobs only) */
            AllocNodes,

            /** Print the session ID used to submit the job. (Valid for jobs only) */
            AllocSID,

            /** Prints the job ID of the job array. (Valid for jobs and job steps) */
            ArrayJobID,

            /** Prints the task ID of the job array. (Valid for jobs and job steps) */
            ArrayTaskID,

            /** Prints the ID of the job association. (Valid for jobs only) */
            AssocID,

            /** Prints whether the batch flag has been set. (Valid for jobs only) */
            BatchFlag,

            /** Executing (batch) host. For an allocated session, this is the host on which the session is executing
             *  (i.e. the node from which the srun or the salloc command was executed). For a batch job, this is the
             *  node executing the batch script. In the case of a typical Linux cluster, this would be the compute node
             *  zero of the allocation. In the case of a Cray ALPS system, this would be the front-end host whose slurmd
             *  daemon executes the job script. (Valid for jobs only) */
            BatchHost,

            /** Prints the number of boards per node allocated to the job. (Valid for jobs only) */
            BoardsPerNode,

            /** Burst Buffer specification (Valid for jobs only) */
            BurstBuffer,

            /** Burst Buffer state (Valid for jobs only) */
            BurstBufferState,

            /** Name of the cluster that is running the job or job step. */
            Cluster,

            /** Cluster features required by the job. (Valid for jobs only) */
            ClusterFeature,

            /** The command to be executed. (Valid for jobs only) */
            Command,

            /** Comment associated with the job. (Valid for jobs only) */
            Comment,

            /** Are contiguous nodes requested by the job. (Valid for jobs only) */
            Contiguous,

            /** OCI container bundle path. */
            Container,

            /** Number of cores per socket requested by the job. This reports the value of the srun `coresPerSocket`
             *  option. When `coresPerSocket` has not been set, "*" is displayed. (Valid for jobs only) */
            Cores,

            /** Count of cores reserved on each node for system use (core specialization). (Valid for jobs only) */
            CoreSpec,

            /** Prints the frequency of the allocated CPUs. (Valid for job steps only) */
            CPUFreq,

            /** Prints the number of CPUs per tasks allocated to the job. (Valid for jobs only) */
            `cpus-per-task`,

            /** Print the memory required per trackable resources allocated to the job or job step. */
            `cpus-per-tres`,

            /** Prints the deadline affected to the job (Valid for jobs only) */
            Deadline,

            /** Delay boot time. (Valid for jobs only) */
            DelayBoot,

            /** Job dependencies remaining. This job will not begin execution until these dependent jobs complete. In
             *  the case of a job that can not run due to job dependencies never being satisfied, the full original job
             *  dependency specification will be reported. A value of NULL implies this job has no dependencies.
             *  (Valid for jobs only) */
            Dependency,

            /** Derived exit code for the job, which is the highest exit code of any job step. (Valid for jobs only) */
            DerivedEC,

            /** Time the job is eligible for running. (Valid for jobs only) */
            EligibleTime,

            /** The time of job termination, actual or expected. (Valid for jobs only) */
            EndTime,

            /** The exit code for the job. (Valid for jobs only) */
            exit_code,

            /** Features required by the job. (Valid for jobs only) */
            Feature,

            /** Group ID of the job. (Valid for jobs only) */
            GroupID,

            /** Group name of the job. (Valid for jobs only) */
            GroupName,

            /** Job ID of the heterogeneous job leader. */
            HetJobID,

            /** Expression identifying all components job IDs within a heterogeneous job. */
            HetJobIDSet,

            /** Zero origin offset within a collection of heterogeneous job components. */
            HetJobOffset,

            /** Job array's job ID. This is the base job ID. For non-array jobs, this is the job ID. (Valid for jobs only) */
            JobArrayID,

            /** Job ID. This will have a unique value for each element of job arrays and each component of heterogeneous
             *  jobs. (Valid for jobs only) */
            JobID,

            /** Prints the last time the job was evaluated for scheduling. (Valid for jobs only) */
            LastSchedEval,

            /** Licenses reserved for the job. (Valid for jobs only) */
            Licenses,

            /** Prints the max number of CPUs allocated to the job. (Valid for jobs only) */
            MaxCPUs,

            /** Prints the max number of nodes allocated to the job. (Valid for jobs only) */
            MaxNodes,

            /** Prints the MCS_label of the job. (Valid for jobs only) */
            MCSLabel,

            /** Print the memory (in MB) required per trackable resources allocated to the job or job step. */
            `mem-per-tres`,

            /** Minimum number of CPUs (processors) per node requested by the job. This reports the value of the srun
             *  `mincpus` option with a default value of zero. (Valid for jobs only) */
            MinCpus,

            /** Minimum size of memory (in MB) requested by the job. (Valid for jobs only) */
            MinMemory,

            /** Minimum time limit of the job (Valid for jobs only) */
            MinTime,

            /** Minimum size of temporary disk space (in MB) requested by the job. (Valid for jobs only) */
            MinTmpDisk,

            /** Job or job step name. (Valid for jobs and job steps) */
            Name,

            /** The network that the job is running on. (Valid for jobs and job steps) */
            Network,

            /** Nice value (adjustment to a job's scheduling priority). (Valid for jobs only) */
            Nice,

            /** List of nodes allocated to the job or job step. In the case of a COMPLETING job, the list of nodes will
             *  comprise only those nodes that have not yet been returned to service. (Valid for jobs only) */
            NodeList,

            /** List of nodes allocated to the job or job step. In the case of a COMPLETING job, the list of nodes will
             *  comprise only those nodes that have not yet been returned to service. (Valid job steps only) */
            Nodes,

            /** The number of tasks per board allocated to the job. (Valid for jobs only) */
            NTPerBoard,

            /** The number of tasks per core allocated to the job. (Valid for jobs only) */
            NTPerCore,

            /** The number of tasks per node allocated to the job. (Valid for jobs only) */
            NTPerNode,

            /** The number of tasks per socket allocated to the job. (Valid for jobs only) */
            NTPerSocket,

            /** Number of CPUs (processors) requested by the job or allocated to it if already running. As a job is
             *  completing, this number will reflect the current number of CPUs allocated. (Valid for jobs and job steps) */
            NumCPUs,

            /** Number of nodes allocated to the job or the minimum number of nodes required by a pending job. The
             *  actual number of nodes allocated to a pending job may exceed this number if the job specified a node
             *  range count (e.g. minimum and maximum node counts) or the job specifies a processor count instead of a
             *  node count. As a job is completing this number will reflect the current number of nodes allocated.
             *  (Valid for jobs only) */
            NumNodes,

            /** Number of tasks requested by a job or job step. This reports the value of the `ntasks` option.
             *  (Valid for jobs and job steps) */
            NumTasks,

            /** Cluster name where federated job originated from. (Valid for federated jobs only) */
            Origin,

            /** Cluster ID where federated job originated from. (Valid for federated jobs only) */
            OriginRaw,

            /** Can the compute resources allocated to the job be over subscribed by other jobs. The resources to be
             *  over subscribed can be nodes, sockets, cores, or hyperthreads depending upon configuration. The value
             *  will be "YES" if the job was submitted with the oversubscribe option or the partition is configured with
             *  OverSubscribe=Force, "NO" if the job requires exclusive node access, "USER" if the allocated compute
             *  nodes are dedicated to a single user, "MCS" if the allocated compute nodes are dedicated to a single
             *  security class (See MCSPlugin and MCSParameters configuration parameters for more information), "OK"
             *  otherwise (typically allocated dedicated CPUs), (Valid for jobs only) */
            OverSubscribe,

            /** Partition of the job or job step. (Valid for jobs and job steps) */
            Partition,

            /** The preempt time for the job. (Valid for jobs only) */
            PreemptTime,

            /** The time (in seconds) between start time and submit time of the job. If the job has not started yet,
             *  then the time (in seconds) between now and the submit time of the job. (Valid for jobs only) */
            PendingTime,

            /** Priority of the job (converted to a floating point number between 0.0 and 1.0). Also @see prioritylong.
             * (Valid for jobs only) */
            Priority,

            /** Priority of the job (generally a very large unsigned integer). Also see priority. (Valid for jobs only) */
            PriorityLong,

            /** Profile of the job. (Valid for jobs only) */
            Profile,

            /** Quality of service associated with the job. (Valid for jobs only) */
            QOS,

            /** The reason a job is in its current state. See the JOB REASON CODES section below for more information.
             * (Valid for jobs only) */
            Reason,

            /** For pending jobs: the reason a job is waiting for execution is printed within parenthesis. For
             *  terminated jobs with failure: an explanation as to why the job failed is printed within parenthesis.
             *  For all other job states: the list of allocate nodes. See the JOB REASON CODES section below for more
             *  information. (Valid for jobs only) */
            ReasonList,

            /** Indicates if the allocated nodes should be rebooted before starting the job. (Valid on jobs only) */
            Reboot,

            /** List of node names explicitly requested by the job. (Valid for jobs only) */
            ReqNodes,

            /** The max number of requested switches by for the job. (Valid for jobs only) */
            ReqSwitch,

            /** Prints whether the job will be requeued on failure. (Valid for jobs only) */
            Requeue,

            /** Reservation for the job. (Valid for jobs only) */
            Reservation,

            /** The amount of time changed for the job to run. (Valid for jobs only) */
            ResizeTime,

            /** The number of restarts for the job. (Valid for jobs only) */
            RestartCnt,

            /** Reserved ports of the job. (Valid for job steps only) */
            ResvPort,

            /** For pending jobs, a list of the nodes expected to be used when the job is started. (Valid for jobs only) */
            SchedNodes,

            /** Number of requested sockets, cores, and threads (S:C:T) per node for the job. When (S:C:T) has not been
             *  set, "*" is displayed. (Valid for jobs only) */
            SCT,

            /** Node selection plugin specific data for a job. Possible data includes: Geometry requirement of resource
             * allocation (X,Y,Z dimensions), Connection type (TORUS, MESH, or NAV == torus else mesh), Permit rotation
             * of geometry (yes or no), Node use (VIRTUAL or COPROCESSOR), etc. (Valid for jobs only) */
            SelectJobInfo,

            /** Cluster names of where federated sibling jobs exist. (Valid for federated jobs only) */
            SiblingsActive,

            /** Cluster IDs of where federated sibling jobs exist. (Valid for federated jobs only) */
            SiblingsActiveRaw,

            /** Cluster names of where federated sibling jobs are viable to run. (Valid for federated jobs only) */
            SiblingsViable,

            /** Cluster IDs of where federated sibling jobs viable to run. (Valid for federated jobs only) */
            SiblingsViableRaw,

            /** Number of sockets per node requested by the job. This reports the value of the srun `socketsPerNode`
             *  option. When `socketsPerNode` has not been set, "*" is displayed. (Valid for jobs only) */
            Sockets,

            /** Number of sockets per board allocated to the job. (Valid for jobs only) */
            SPerBoard,

            /** Actual or expected start time of the job or job step. (Valid for jobs and job steps) */
            StartTime,

            /** Job state in extended form. See the JOB STATE CODES section below for a list of possible states.
             *  (Valid for jobs only) */
            State,

            /** Job state in compact form. See the JOB STATE CODES section below for a list of possible states.
             *  (Valid for jobs only) */
            StateCompact,

            /** The directory for standard error to output to. (Valid for jobs only) */
            STDERR,

            /** The directory for standard in. (Valid for jobs only) */
            STDIN,

            /** The directory for standard out to output to. (Valid for jobs only) */
            STDOUT,

            /** Job or job step ID. In the case of job arrays, the job ID format will be of the form
             *  "<base_job_id>_<index>". (Valid forjob steps only) */
            StepID,

            /** Job step name. (Valid for job steps only) */
            StepName,

            /** The state of the job step. (Valid for job steps only) */
            StepState,

            /** The time that the job was submitted at. (Valid for jobs only) */
            SubmitTime,

            /** System comment associated with the job. (Valid for jobs only) */
            system_comment,

            /** Number of threads per core requested by the job. This reports the value of the srun `threadsPerCore`
             *  option. When `threadsPerCore` has not been set, "*" is displayed. (Valid for jobs only) */
            Threads,

            /** Time left for the job to execute in days-hours:minutes:seconds. This value is calculated by subtracting
             *  the job's time used from its time limit. The value may be "NOT_SET" if not yet established or
             *  "UNLIMITED" for no limit. (Valid for jobs only) */
            TimeLeft,

            /** Timelimit for the job or job step. (Valid for jobs and job steps) */
            TimeLimit,

            /** Time used by the job or job step in days-hours:minutes:seconds. The days and hours are printed only as
             *  needed. For job steps this field shows the elapsed time since execution began and thus will be
             *  inaccurate for job steps which have been suspended. Clock skew between nodes in the cluster will cause
             *  the time to be inaccurate. If the time is obviously wrong (e.g. negative), it displays as "INVALID".
             *  (Valid for jobs and job steps) */
            TimeUsed,

            /** Print the trackable resources allocated to the job if running. If not running, then print the trackable
             *  resources requested by the job. */
            `tres-alloc`,

            /** Print the trackable resources task binding requested by the job or job step. */
            `tres-bind`,

            /** Print the trackable resources frequencies requested by the job or job step. */
            `tres-freq`,

            /** Print the trackable resources requested by the job. */
            `tres-per-job`,

            /** Print the trackable resources per node requested by the job or job step. */
            `tres-per-node`,

            /** Print the trackable resources per socket requested by the job or job step. */
            `tres-per-socket`,

            /** Print the trackable resources requested by the job step. */
            `tres-per-step`,

            /** Print the trackable resources per task requested by the job or job step. */
            `tres-per-task`,

            /** User ID for a job or job step. (Valid for jobs and job steps) */
            UserID,

            /** User name for a job or job step. (Valid for jobs and job steps) */
            UserName,

            /** The amount of time to wait for the desired number of switches. (Valid for jobs only) */
            Wait4Switch,

            /** Workload Characterization Key (wckey). (Valid for jobs only) */
            WCKey,

            /** The job's working directory. (Valid for jobs only) */
            WorkDir
        }
    }

    @SlurmMarker
    inner class FormatBuilder {

        operator fun Format.Type.invoke(block: Format.() -> Unit) {
            this@Squeue.format += Format(this).run {
                block()
                val type = type.name
                val justified = if (rightJustified) "." else ""
                "$type:$justified$size$suffix"
            }
        }
    }

    /** Jobs typically pass through several states in the course of their execution. The typical states are PENDING,
     *  RUNNING, SUSPENDED, COMPLETING, and COMPLETED. An explanation of each state follows. */
    enum class JobStateCode {
        /** Job terminated due to launch failure, typically due to a hardware failure (e.g. unable to boot the node or block and the job can not be requeued). */
        BOOT_FAIL,

        /** Job was explicitly cancelled by the user or system administrator. The job may or may not have been initiated. */
        CANCELLED,

        /** Job has terminated all processes on all nodes with an exit code of zero. */
        COMPLETED,

        /** Job has been allocated resources, but are waiting for them to become ready for use (e.g. booting). */
        CONFIGURING,

        /** Job is in the process of completing. Some processes on some nodes may still be active. */
        COMPLETING,

        /** Job terminated on deadline. */
        DEADLINE,

        /** Job terminated with non-zero exit code or other failure condition. */
        FAILED,

        /** Job terminated due to failure of one or more allocated nodes. */
        NODE_FAIL,

        /** Job experienced out of memory error. */
        OUT_OF_MEMORY,

        /** Job is awaiting resource allocation. */
        PENDING,

        /** Job terminated due to preemption. */
        PREEMPTED,

        /** Job currently has an allocation. */
        RUNNING,

        /** Job is being held after requested reservation was deleted. */
        RESV_DEL_HOLD,

        /** Job is being requeued by a federation. */
        REQUEUE_FED,

        /** Held job is being requeued. */
        REQUEUE_HOLD,

        /** Completing job is being requeued. */
        REQUEUED,

        /** Job is about to change size. */
        RESIZING,

        /** Sibling was removed from cluster due to other cluster starting the job. */
        REVOKED,

        /** Job is being signaled. */
        SIGNALING,

        /** The job was requeued in a special state. This state can be set by users, typically in EpilogSlurmctld, if the job has terminated with a particular exit value. */
        SPECIAL_EXIT,

        /** Job is staging out files. */
        STAGE_OUT,

        /** Job has an allocation, but execution has been stopped with SIGSTOP signal. CPUS have been retained by this job. */
        STOPPED,

        /** Job has an allocation, but execution has been suspended and CPUs have been released for other jobs. */
        SUSPENDED,

        /** Job terminated upon reaching its time limit. */
        TIMEOUT
    }
}

@SlurmMarker
class SqueueBuilder(val squeue: Squeue = Squeue()) {

    /** Specify the accounts of the jobs to view. Accepts a comma separated list of account names. This has no effect
     *  when listing job steps. */
    fun accounts(vararg accounts: String) {
        squeue.accounts += accounts
    }

    /** Display information about jobs and job steps in all partitions. This causes information to be displayed about
     *  partitions that are configured as hidden, partitions that are unavailable to a user's group, and federated jobs
     *  that are in a "revoked" state. */
    fun all() {
        squeue.all = true
    }

    /** Display one job array element per line. Without this option, the display will be optimized for use with job
     *  arrays (pending job array elements will be combined on one line of output with the array index values printed
     *  using a regular expression).
     *
     *  @param unique Display one unique pending job array element per line. Without this option, the pending job array
     *  elements will be grouped into the master array job to optimize the display. This can also be set with the
     *  environment variable SQUEUE_ARRAY_UNIQUE.*/
    fun array(unique: Boolean = false) {
        if (unique)
            squeue.arrayUnique = true
        else
            squeue.array = true
    }

    /** Show jobs from the federation if a member of one. */
    fun federation() {
        squeue.federation = true
    }

    /** Do not print a header on the output. */
    fun noHeader() {
        squeue.noHeader = true
    }

    //    --help
    //    Print a help message describing all options squeue.

    /** Do not display information about jobs and job steps in all partitions. By default, information about partitions
     *  that are configured as hidden or are not available to the user's group will not be displayed (i.e. this is the
     *  default behavior). */
    fun hide() {
        squeue.hide = true
    }

    /** Repeatedly gather and report the requested information at the interval specified (in seconds). By default,
     *  prints a time stamp with the header. */
    fun iterate(seconds: Second) {
        squeue.iterate = seconds
    }

    /** Requests a comma separated list of job IDs to display. Defaults to all jobs. The `jobs(<job_id_list>)` option
     *  may be used in conjunction with the `steps` option to print step information about specific jobs. Note: If a
     *  list of job IDs is provided, the jobs are displayed even if they are on hidden partitions. Since this option's
     *  argument is optional, for proper parsing the single letter option must be followed immediately with the value
     *  and not include a space between them. For example "-j1008" and not "-j 1008". The job ID format is
     *  "job_id[_array_id]". Performance of the command can be measurably improved for systems with large numbers of
     *  jobs when a single job ID is specified. By default, this field size will be limited to 64 bytes. Use the
     *  environment variable SLURM_BITSTR_LEN to specify larger field sizes. */
    fun jobs(vararg jobs: String) {
        squeue.jobs += jobs
    }

    /** Show only jobs local to this cluster. Ignore other clusters in this federation (if any). Overrides `federation`. */
    fun local() {
        if (squeue.federation)
            println("`local` overrides `federation`")
        squeue.local = true
    }

    /** Report more of the available information for the selected jobs or job steps, subject to any constraints specified. */
    fun long() {
        squeue.long = true
    }

    /** Request jobs requesting or using one or more of the named licenses. The license list consists of a comma
     *  separated list of license names. */
    fun licenses(vararg licenses: String) {
        squeue.licenses += licenses
    }

    /** Clusters to issue commands to. Multiple cluster names may be comma separated. A value of `all` will query to run
     *  on all clusters. This option implicitly sets the `local` option. */
    fun clusters(vararg clusters: String) {
        squeue.clusters += clusters
    }

    /** Request jobs or job steps having one of the specified names. The list consists of a comma separated list of job
     *  names. */
    fun names(vararg names: String) {
        squeue.names += names
    }

    /** Don't convert units from their original type (e.g. 2048M won't be converted to 2G). */
    fun dontConvert() {
        squeue.dontConvert = true
    }

    /** Specify the information to be displayed, its size and position (right or left justified). Also see the -O
     *  <output_format>, --Format=<output_format> option described below (which supports less flexibility in formatting,
     *  but supports access to all fields). The default formats with various options are:
     *      default
     *          "%.18i %.9P %.8j %.8u %.2t %.10M %.6D %R"
     *      long
     *          "%.18i %.9P %.8j %.8u %.8T %.10M %.9l %.6D %R"
     *      steps
     *          "%.15i %.8j %.9P %.8u %.9M %N"
     *  The format of each field is "%[[.]size]type".
     *      size
     *          is the minimum field size. If no size is specified, whatever is needed to print the information will be
     *          used.
     *      .
     *          indicates the output should be right justified and size must be specified. By default, output is left
     *          justified.
     *  Note that many of these type specifications are valid only for jobs while others are valid only for job steps.
     *  Valid type specifications include:
     *      %all
     *          Print all fields available for this data type with a vertical bar separating each field.
     *      %a
     *          Account associated with the job. (Valid for jobs only)
     *      %A
     *          Number of tasks created by a job step. This reports the value of the srun `ntasks` option.
     *          (Valid for job steps only)
     *      %A
     *          Job id. This will have a unique value for each element of job arrays. (Valid for jobs only)
     *      %B
     *          Executing (batch) host. For an allocated session, this is the host on which the session is executing
     *          (i.e. the node from which the srun or the salloc command was executed). For a batch job, this is the
     *          node executing the batch script. In the case of a typical Linux cluster, this would be the compute node
     *          zero of the allocation. In the case of a Cray ALPS system, this would be the front-end host whose slurmd
     *          daemon executes the job script.
     *      %c
     *          Minimum number of CPUs (processors) per node requested by the job. This reports the value of the srun
     *          `mincpus` option with a default value of zero. (Valid for jobs only)
     *      %C
     *          Number of CPUs (processors) requested by the job or allocated to it if already running. As a job is
     *          completing this number will reflect the current number of CPUs allocated. (Valid for jobs only)
     *      %d
     *          Minimum size of temporary disk space (in MB) requested by the job. (Valid for jobs only)
     *      %D
     *          Number of nodes allocated to the job or the minimum number of nodes required by a pending job. The
     *          actual number of nodes allocated to a pending job may exceed this number if the job specified a node
     *          range count (e.g. minimum and maximum node counts) or the job specifies a processor count instead of a
     *          node count and the cluster contains nodes with varying processor counts. As a job is completing this
     *          number will reflect the current number of nodes allocated. (Valid for jobs only)
     *      %e
     *          Time at which the job ended or is expected to end (based upon its time limit). (Valid for jobs only)
     *      %E
     *          Job dependencies remaining. This job will not begin execution until these dependent jobs complete. In
     *          the case of a job that can not run due to job dependencies never being satisfied, the full original job
     *          dependency specification will be reported. A value of NULL implies this job has no dependencies.
     *          (Valid for jobs only)
     *      %f
     *          Features required by the job. (Valid for jobs only)
     *      %F
     *          Job array's job ID. This is the base job ID. For non-array jobs, this is the job ID. (Valid for jobs only)
     *      %g
     *          Group name of the job. (Valid for jobs only)
     *      %G
     *          Group ID of the job. (Valid for jobs only)
     *      %h
     *          Can the compute resources allocated to the job be over subscribed by other jobs. The resources to be
     *          over subscribed can be nodes, sockets, cores, or hyperthreads depending upon configuration. The value
     *          will be "YES" if the job was submitted with the oversubscribe option or the partition is configured with
     *          OverSubscribe=Force, "NO" if the job requires exclusive node access, "USER" if the allocated compute
     *          nodes are dedicated to a single user, "MCS" if the allocated compute nodes are dedicated to a single
     *          security class (See MCSPlugin and MCSParameters configuration parameters for more information), "OK"
     *          otherwise (typically allocated dedicated CPUs), (Valid for jobs only)
     *      %H
     *          Number of sockets per node requested by the job. This reports the value of the srun `socketsPerNode`
     *          option. When `socketsPerNode` has not been set, "*" is displayed. (Valid for jobs only)
     *      %i
     *          Job or job step id. In the case of job arrays, the job ID format will be of the form
     *          "<base_job_id>_<index>". By default, the job array index field size will be limited to 64 bytes. Use the
     *          environment variable SLURM_BITSTR_LEN to specify larger field sizes. (Valid for jobs and job steps) In
     *          the case of heterogeneous job allocations, the job ID format will be of the form "#+#" where the first
     *          number is the "heterogeneous job leader" and the second number the zero origin offset for each component
     *          of the job.
     *      %I
     *          Number of cores per socket requested by the job. This reports the value of the srun `coresPerSocket`
     *          option. When `coresPerSocket` has not been set, "*" is displayed. (Valid for jobs only)
     *      %j
     *          Job or job step name. (Valid for jobs and job steps)
     *      %J
     *          Number of threads per core requested by the job. This reports the value of the srun `threadsPerCore`
     *          option. When `threadsPerCore` has not been set, "*" is displayed. (Valid for jobs only)
     *      %k
     *          Comment associated with the job. (Valid for jobs only)
     *      %K
     *          Job array index. By default, this field size will be limited to 64 bytes. Use the environment variable
     *          SLURM_BITSTR_LEN to specify larger field sizes. (Valid for jobs only)
     *      %l
     *          Time limit of the job or job step in days-hours:minutes:seconds. The value may be "NOT_SET" if not yet
     *          established or "UNLIMITED" for no limit. (Valid for jobs and job steps)
     *      %L
     *          Time left for the job to execute in days-hours:minutes:seconds. This value is calculated by subtracting
     *          the job's time used from its time limit. The value may be "NOT_SET" if not yet established or
     *          "UNLIMITED" for no limit. (Valid for jobs only)
     *      %m
     *          Minimum size of memory (in MB) requested by the job. (Valid for jobs only)
     *      %M
     *          Time used by the job or job step in days-hours:minutes:seconds. The days and hours are printed only as
     *          needed. For job steps this field shows the elapsed time since execution began and thus will be
     *          inaccurate for job steps which have been suspended. Clock skew between nodes in the cluster will cause
     *          the time to be inaccurate. If the time is obviously wrong (e.g. negative), it displays as "INVALID".
     *          (Valid for jobs and job steps)
     *      %n
     *          List of node names explicitly requested by the job. (Valid for jobs only)
     *      %N
     *          List of nodes allocated to the job or job step. In the case of a COMPLETING job, the list of nodes will
     *          comprise only those nodes that have not yet been returned to service. (Valid for jobs and job steps)
     *      %o
     *          The command to be executed.
     *      %O
     *          Are contiguous nodes requested by the job. (Valid for jobs only)
     *      %p
     *          Priority of the job (converted to a floating point number between 0.0 and 1.0). Also see %Q. (Valid for
     *          jobs only)
     *      %P
     *          Partition of the job or job step. (Valid for jobs and job steps)
     *      %q
     *          Quality of service associated with the job. (Valid for jobs only)
     *      %Q
     *          Priority of the job (generally a very large unsigned integer). Also see %p. (Valid for jobs only)
     *      %r
     *          The reason a job is in its current state. See the JOB REASON CODES section below for more information.
     *          (Valid for jobs only)
     *      %R
     *          For pending jobs: the reason a job is waiting for execution is printed within parenthesis. For
     *          terminated jobs with failure: an explanation as to why the job failed is printed within parenthesis. For
     *          all other job states: the list of allocate nodes. See the JOB REASON CODES section below for more
     *          information. (Valid for jobs only)
     *      %s
     *          Node selection plugin specific data for a job. Possible data includes: Geometry requirement of resource
     *          allocation (X,Y,Z dimensions), Connection type (TORUS, MESH, or NAV == torus else mesh), Permit rotation
     *          of geometry (yes or no), Node use (VIRTUAL or COPROCESSOR), etc. (Valid for jobs only)
     *      %S
     *          Actual or expected start time of the job or job step. (Valid for jobs and job steps)
     *      %t
     *          Job state in compact form. See the JOB STATE CODES section below for a list of possible states. (Valid
     *          for jobs only)
     *      %T
     *          Job state in extended form. See the JOB STATE CODES section below for a list of possible states.
     *          (Valid for jobs only)
     *      %u
     *          User name for a job or job step. (Valid for jobs and job steps)
     *      %U
     *          User ID for a job or job step. (Valid for jobs and job steps)
     *      %v
     *          Reservation for the job. (Valid for jobs only)
     *      %V
     *          The job's submission time.
     *      %w
     *          Workload Characterization Key (wckey). (Valid for jobs only)
     *      %W
     *          Licenses reserved for the job. (Valid for jobs only)
     *      %x
     *          List of node names explicitly excluded by the job. (Valid for jobs only)
     *      %X
     *          Count of cores reserved on each node for system use (core specialization). (Valid for jobs only)
     *      %y
     *          Nice value (adjustment to a job's scheduling priority). (Valid for jobs only)
     *      %Y
     *          For pending jobs, a list of the nodes expected to be used when the job is started.
     *      %z
     *          Number of requested sockets, cores, and threads (S:C:T) per node for the job. When (S:C:T) has not been
     *          set, "*" is displayed. (Valid for jobs only)
     *      %Z
     *          The job's working directory. */
    fun format(vararg values: String) {
        squeue.format += values
    }

    /** Specify the information to be displayed. Also see the -o <output_format>, --format=<output_format> option
     *  described below (which supports greater flexibility in formatting, but does not support access to all fields
     *  because we ran out of letters). Requests a comma separated list of job information to be displayed.
     *  The format of each field is "type[:[.][size][suffix]]"
     *      size
     *          Minimum field size. If no size is specified, 20 characters will be allocated to print the information.
     *      .
     *          Indicates the output should be right justified and size must be specified. By default output is left
     *          justified.
     *      suffix
     *          Arbitrary string to append to the end of the field.
     *  Note that many of these type specifications are valid only for jobs while others are valid only for job steps. */
    fun format(block: Squeue.FormatBuilder.() -> Unit) = squeue.FormatBuilder().block()

    /** Specify the partitions of the jobs or steps to view. Accepts a comma separated list of partition names. */
    fun partitions(vararg partitions: String) {
        squeue.partitions += partitions
    }

    /** For pending jobs submitted to multiple partitions, list the job once per partition. In addition, if jobs are
     *  sorted by priority, consider both the partition and job priority. This option can be used to produce a list of
     *  pending jobs in the same order considered for scheduling by Slurm with appropriate additional options
     *  (e.g. "--sort=-p,i --states=PD"). */
    fun priority() {
        squeue.priority = true
    }

    /** Specify the qos(s) of the jobs or steps to view. Accepts a comma separated list of qos's. */
    fun qos(vararg qos: String) {
        squeue.qos += qos
    }

    /** Specify the reservation of the jobs to view. */
    val reservation: String by squeue::reservation

    /** Specify the job steps to view. This flag indicates that a comma separated list of job steps to view follows
     *  without an equal sign (see examples). The job step format is "job_id[_array_id].step_id". Defaults to all job
     *  steps. Since this option's argument is optional, for proper parsing the single letter option must be followed
     *  immediately with the value and not include a space between them. For example "-s1008.0" and not "-s 1008.0". */
    fun steps(vararg steps: String) {
        squeue.steps += steps
    }

    /** Show all sibling jobs on a federated cluster. Implies `federation`. */
    fun sibling() {
        squeue.sibling = true
    }

    /** Specification of the order in which records should be reported. This uses the same field specification as the
     *  <output_format>. The long format option "cluster" can also be used to sort jobs or job steps by cluster name
     *  (e.g. federated jobs). Multiple sorts may be performed by listing multiple sort fields separated by commas.
     *  The field specifications may be preceded by "+" or "-" for ascending (default) and descending order respectively.
     *  For example, a sort value of "P,U" will sort the records by partition name then by user id. The default value of
     *  sort for jobs is "P,t,-p" (increasing partition name then within a given partition by increasing job state and
     *  then decreasing priority). The default value of sort for job steps is "P,i" (increasing partition name then
     *  within a given partition by increasing step id). */
    fun sort(order: String) {
        squeue.order = order
    }

    /** Report the expected start time and resources to be allocated for pending jobs in order of increasing start time.
     *  This is equivalent to the following options:
     *      format("%.18i %.9P %.8j %.8u %.2t %.19S %.6D %20Y %R")
     *      sort("S")
     *      states(PENDING)
     *  Any of these options may be explicitly changed as desired by combining the `start` option with other option
     *  values (e.g. to use a different output format). The expected start time of pending jobs is only available if the
     *  Slurm is configured to use the backfill scheduling plugin. */
    fun start() {
        squeue.start = true
    }

    /** Specify the states of jobs to view. Accepts a comma separated list of state names or "all". If "all" is
     *  specified then jobs of all states will be reported. If no state is specified then pending, running, and
     *  completing jobs are reported. See the JOB STATE CODES section below for a list of valid states. Both extended
     *  and compact forms are valid. Note the <state_list> supplied is case insensitive ("pd" and "PD" are equivalent). */
    fun states(vararg states: Squeue.JobStateCode) {
        squeue.states += states
    }

    /** Request jobs or job steps from a comma separated list of users. The list can consist of user names or user id
     *  numbers. Performance of the command can be measurably improved for systems with large numbers of jobs when a
     *  single user is specified. */
    fun users(vararg users: String) {
        squeue.users += users
    }

    //    /** Print a brief help message listing the squeue options. */
    //    fun usage() {
    //        squeue.usage = true
    //    }

    /** Report details of squeues actions. */
    fun verbose() {
        squeue.verbose = true
    }

    /** Print version information and exit. */
    fun version() {
        squeue.version
    }

    /** Report only on jobs allocated to the specified node or list of nodes. This may either be the NodeName or
     *  NodeHostname as defined in slurm.conf(5) in the event that they differ. A node_name of localhost is mapped to
     *  the current host name. */
    fun nodelist(nodelist: String) {
        squeue.nodelist = nodelist
    }
}