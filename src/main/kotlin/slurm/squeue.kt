package slurm


fun main() {
    squeue {
        partitions("intel")
        states(Squeue.JobStateCode.PENDING)
        format("%.6i", "%p")

        //        steps()
        //        partitions("intel")
        //        sort("u")

        //        jobs(3684390..3684392)
        //        steps("3684390.1")
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
    val jobs = ArrayList<Int>()
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
    var steps: ArrayList<String>? = null
    var sibling = false
    var order = ""
    var start = false
    val states = ArrayList<JobStateCode>()
    val users = ArrayList<String>()
    var verbose = false
    var version = false
    var nodelist = ""

    operator fun invoke(): String {
        val cmd = "squeue"
        val args = ArrayList<String>()

        if (accounts.isNotEmpty()) args.add("-A", accounts.joinToString(","))
        if (all) args += "-a"
        if (array) args += "-r"
        if (arrayUnique) args += "--array-unique"
        if (federation) args += "--federation"
        if (noHeader) args += "-h"
        if (help) args += "--help"
        if (hide) args += "--hide"
        if (iterate != Second(0)) args.add("-i", iterate)
        if (jobs.isNotEmpty()) args += "-j${jobs.joinToString(",")}"
        if (local) args += "--local"
        if (long) args += "--l"
        if (licenses.isNotEmpty()) args.add("-L", licenses.joinToString(","))
        if (clusters.isNotEmpty()) args.add("-M", clusters.joinToString(","))
        if (names.isNotEmpty()) args.add("-n", names.joinToString(","))
        if (dontConvert) args += "--noconvert"
        if (format.isNotEmpty()) when {
            '%' in format[0] -> args.add("-o", "\"${format.joinToString(" ")}\"")
            else -> args.add("-O", "\"${format.joinToString(",")}\"")
        }
        if (partitions.isNotEmpty()) args.add("-p", partitions.joinToString(","))
        if (priority) args += "-P"
        if (qos.isNotEmpty()) args.add("-q", qos.joinToString(","))
        if (reservation.isNotEmpty()) args.add("-R", reservation)
        steps?.run { args.add("-s${joinToString(",")}") }
        if (sibling) args += "--sibling"
        if (order.isNotEmpty()) args.add("-S", order)
        if (start) args += "--start"
        if (states.isNotEmpty()) args.add("-t", states.joinToString(","))
        if (users.isNotEmpty()) args.add("-u", users.joinToString(","))
        if (verbose) args += "-v"
        if (version) args += "-V"
        if (nodelist.isNotEmpty()) args.add("-w", nodelist)

        //        println("running `$cmd`")
        return cmd(args)
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

