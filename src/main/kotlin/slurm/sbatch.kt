package slurm

import java.io.File
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
inline fun sbatch(block: SbatchBuilder.() -> Unit) = SbatchBuilder().block()

@ExperimentalTime
class Sbatch {
    val array = ArrayList<Int>()
    var account = ""
    val dataTypes = ArrayList<DataType>()
    var extraNodeInfo = ""
    var batch = ""
    var bb = ""
    var bbf = ""
    var begin: LocalDateTime = LocalDateTime.MIN
    var checkpoint: Duration = Duration.INFINITE
    var clusterConstraint: String = ""
    var comment: String = ""
    var constraint: String = ""
    var container: File? = null
    var contiguous = false
    var coresPerSocket = -1
    var cpuFreq = ""
    var cpusPerGpu = -1
    var cpusPerTask: Int? = null
    var deadline: LocalDateTime = LocalDateTime.MIN
    var delayBoot: Minute? = null

    var dependencyAfter = ""
    var dependencyAfterAny = ""
    var dependencyBurstBuffer = ""
    var dependencyCorr = ""
    var dependencyNotOk = ""
    var dependencyOk = ""
    var dependencyExpand = ""
    var dependencySingleton = false

    var chDir: File? = null
    var error: File? = null
    var exclusive: Exclusive? = null
    val export = ArrayList<String>()
    var exportFile = ""
    var nodeFile: File? = null
    var getUserEnv: String? = null
    var groupID = ""
    val gpus = ArrayList<String>()
    var gpuBind = ""
    var gpuFreq = ""
    val gpusPerNode = ArrayList<String>()
    val gpusPerSocket = ArrayList<String>()
    val gpusPerTask = ArrayList<String>()
    val gres = ArrayList<String>()
    var gresFlags: GresFlag? = null
    var hold = false
    var help = false
    var hint: Hint? = null
    var ignorePBS = false
    var input = ""
    var jobName = ""
    var dontKill = ""
    var killOnInvalidDep = ""
    val licenses = ArrayList<String>()
    val clusters = ArrayList<String>()
    var distribution = ""
    val mailTypes = ArrayList<MailType>()
    var mailUser = ""
    var mcsLabel = ""
    var mem = ""
    var memPerCpu = ""
    var memPerGpu = ""
    val memBind = ArrayList<String>()
    var minCpus = -1
    var nodes: IntRange? = null
    var ntasks = -1
    var network = ""
    var nice = ""
    var noRequeue = false
    var nTasksPerCore = -1
    var nTasksPerNode = -1
    var nTasksPerSocket = -1
    var overcommit = false
    var output = ""
    var openMode: OpenMode? = null
    var parsable = false
    val partition = ArrayList<String>()
    val power = ArrayList<String>()
    var priority = ""
    val profiles = ArrayList<Profile>()
    val propagate = ArrayList<Propagate>()
    var qos = ""
    var quiet = false
    var reboot = false
    var requeue = false
    var reservation = ""
    var oversubscribe = false
    var coreSpec = -1
    var signal = ""
    var socketsPerNode = -1
    var spreadJob = false
    val switches = ArrayList<String>()
    var time = Duration.INFINITE
    var testOnly = false
    var threadSpec = -1
    var threadsPerCore = -1
    var timeMin = Duration.INFINITE
    var tmp = ""
    var usage = false
    var uid = ""
    var useMinNodes = false
    var version = false
    var verbose = false
    val nodeList = ArrayList<String>()
    var wait = false
    var waitAllNodes: Boolean? = null
    var wckey = ""
    var wrap = ""
    val exclude = ArrayList<String>()

    operator fun invoke(): String {
        val cmd = buildString {
            append("sbatch")
            if (array.isNotEmpty()) append(" -a=${array.joinToString(",")}")
            if (account.isNotEmpty()) append(" -A=$account")
            //            if (acctgFreq)
            if (extraNodeInfo.isNotEmpty()) append(" -B=$extraNodeInfo")
            if (batch.isNotEmpty()) append(" --batch=$batch")
            if (bb.isNotEmpty()) append(" --bb=$bb")
            if (bbf.isNotEmpty()) append(" --bbf=$bbf")
            if (begin != LocalDateTime.MIN) append(" -b=$begin")
            if (checkpoint != Duration.INFINITE) append(" --checkpoint=${checkpoint.toIsoString()}")
            if (clusterConstraint.isNotEmpty()) append(" --cluster-constraint=$clusterConstraint")
            if (contiguous) append(" --contiguous")
            if (coresPerSocket != -1) append(" --cores-per-socket=$coresPerSocket")
            if (cpuFreq.isNotEmpty()) append(" --cpu-freq=$cpuFreq")
            if (cpusPerGpu != -1) append(" --cpus-per-gpu=$cpusPerGpu")
            if (deadline != LocalDateTime.MIN) append(" --deadline=$deadline")
            delayBoot?.let { append(" --delay-boot=${it.value}") }
            var dep = ""
            if (dependencyAfter.isNotEmpty()) dep += dependencyAfter
            if (dependencyAfterAny.isNotEmpty()) dep += dependencyAfterAny
            if (dependencyBurstBuffer.isNotEmpty()) dep += dependencyBurstBuffer
            if (dependencyCorr.isNotEmpty()) dep += dependencyCorr
            if (dependencyNotOk.isNotEmpty()) dep += dependencyNotOk
            if (dependencyOk.isNotEmpty()) dep += dependencyOk
            if (dependencyExpand.isNotEmpty()) dep += dependencyExpand
            if (dependencySingleton) dep += "singleton"
            if (dep.isNotEmpty()) append(" -d=$dep")
            chDir?.let { append(" -D=${it.absolutePath}") }
            error?.let { append(" -e=${it.absolutePath}") }
            exclusive?.let {
                append(" --exclusive")
                if (it != Exclusive.otherRunningJobs)
                    append(it)
            }
            if (export.isNotEmpty()) append(" --export=${export.joinToString(",")}")
            if (exportFile.isNotEmpty()) append(" --export-file=$exportFile")
            nodeFile?.let { append(" -F=${it.absolutePath}") }
            getUserEnv?.let {
                append(" --get-user-env")
                if (it.isNotEmpty())
                    append("=$it")
            }
            if (groupID.isNotEmpty()) append(" --gid=$groupID")
            if (gpus.isNotEmpty()) append(" -G=${gpus.joinToString(",")}")
            if (gpuBind.isNotEmpty()) append(" --gpu-bind=$gpuBind")
            if (gpuFreq.isNotEmpty()) append(" --gpu-freq=$gpuFreq")
            if (gpusPerNode.isNotEmpty()) append(" --gpu-per-node=${gpusPerNode.joinToString(",")}")
            if (gpusPerSocket.isNotEmpty()) append(" --gpu-per-socket=${gpusPerSocket.joinToString(",")}")
            if (gpusPerTask.isNotEmpty()) append(" --gpu-per-task=${gpusPerTask.joinToString(",")}")
            if (gres.isNotEmpty()) append(" --gres=${gres.joinToString(",")}")
            gresFlags?.let { append(" --gres-flags=$it") }
            if (hold) append(" -H")
            if (help) append(" -h")
            hint?.let { append(" --hint=$it") }
            if (ignorePBS) append(" --ignore-pbs")
            if (input.isNotEmpty()) append(" -i=$input")
            if (jobName.isNotEmpty()) append(" -J=$jobName")
            if (dontKill.isNotEmpty()) append(" -k=$dontKill")
            if (killOnInvalidDep.isNotEmpty()) append(" --kill-on-invalid-dep=$killOnInvalidDep")
            if (licenses.isNotEmpty()) append(" -L=${licenses.joinToString(",")}")
            if (clusters.isNotEmpty()) append(" -M=${clusters.joinToString(",")}")
            if (distribution.isNotEmpty()) append(" -m=$distribution")
            if (mailTypes.isNotEmpty()) append(" --mail-type=${mailTypes.joinToString(",")}")
            if (mailUser.isNotEmpty()) append(" --mail-user=$mailUser")
            if (mcsLabel.isNotEmpty()) append(" --mcs-label=$mcsLabel")
            if (mem.isNotEmpty()) append(" --mem=$mem")
            if (memPerCpu.isNotEmpty()) append(" --mem-per-cpu=${memPerCpu}")
            if (memPerGpu.isNotEmpty()) append(" --mem-per-gpu=${memPerGpu}")
            if (memBind.isNotEmpty()) append(" --mem-bind=${memBind.joinToString(",")}")
            if (minCpus != -1) append(" --mincpus=$minCpus")
            nodes?.let {
                append(" -N ${it.first}")
                if (it.first != it.last)
                    append("-${it.last}")
            }
            if (ntasks != -1) append(" -n=$ntasks")
            if (network.isNotEmpty()) append(" --network=$network")
            if (nice.isNotEmpty()) append(" --nice=$nice")
            if (noRequeue) append(" --no-requeue")
            if (nTasksPerCore != -1) append(" --ntasks-per-core=$nTasksPerCore")
            if (nTasksPerNode != -1) append(" --ntasks-per-node=$nTasksPerNode")
            if (nTasksPerSocket != -1) append(" --ntasks-per-socket=$nTasksPerSocket")
            if (overcommit) append(" -O")
            if (output.isNotEmpty()) append(" -o=$output")
            openMode?.let { append(" --open-mode=$it") }
            if (parsable) append(" --parsable")
            if (partition.isNotEmpty()) append(" -p=${partition.joinToString(",")}")
            if (power.isNotEmpty()) append(" --power=${power.joinToString(",")}")
            if (priority.isNotEmpty()) append(" --priority=$priority")
            if (profiles.isNotEmpty()) append(" --profile=$profiles")
            if (propagate.isNotEmpty()) append(" --propagate=${propagate.joinToString(",")}")
            if (qos.isNotEmpty()) append(" --q=$qos")
            if (quiet) append(" -Q")
            if (reboot) append(" --reboot")
            if (requeue) append(" --requeue")
            if (reservation.isNotEmpty()) append(" --reservation=$reservation")
            if (oversubscribe) append(" -s")
            if (coreSpec != -1) append(" -S=$coreSpec")
            if (signal.isNotEmpty()) append(" --signal=$signal")
            if (socketsPerNode != -1) append(" --sockets-per-node=$socketsPerNode")
            if (switches.isNotEmpty()) append(" --switches=${switches.joinToString(",")}")
            if (time != Duration.INFINITE) append(" -t=$time")
            if (testOnly) append(" --test-only")
            if (threadSpec != -1) append(" --thread-spec=$threadSpec")
            if (threadsPerCore != -1) append(" --threads-per-core=$threadsPerCore")
            if (timeMin != Duration.INFINITE) append(" --time-min=$timeMin")
            if (tmp.isNotEmpty()) append(" --tmp$tmp")
            if (usage) append(" --usage")
            if (uid.isNotEmpty()) append(" --uid=$uid")
            if (useMinNodes) append(" --use-min-nodes")
            if (version) append(" -V")
            if (verbose) append(" -v")
            if (nodeList.isNotEmpty()) append(" -w=${nodeList.joinToString(",")}")
            if (wait) append(" -W")
            waitAllNodes?.let { append(" --wait-all-nodes=$it") }
            if (wckey.isNotEmpty()) append(" --wckey")
            if (wrap.isNotEmpty()) append(" --wrap")
            if (exclude.isNotEmpty()) append(" -x=${exclude.joinToString(",")}")
        }
        println("running `$cmd`")
        return cmd()
    }

    enum class GresFlag {
        /** Disable filtering of CPUs with respect to generic resource locality. This option is currently required to
         *  use more CPUs than are bound to a GRES (i.e. if a GPU is bound to the CPUs on one socket, but resources on
         *  more than one socket are required to run the job). This option may permit a job to be allocated resources
         *  sooner than otherwise possible, but may result in lower job performance. */
        disableBinding,

        /** The only CPUs available to the job will be those bound to the selected GRES (i.e. the CPUs identified in the
         *  gres.conf file will be strictly enforced). This option may result in delayed initiation of a job. For
         *  example a job requiring two GPUs and one CPU will be delayed until both GPUs on a single socket are
         *  available rather than using GPUs bound to separate sockets, however the application performance may be
         *  improved due to improved communication speed. Requires the node to be configured with more than one socket
         *  and resource filtering will be performed on a per-socket basis. */
        enforceBinding
    }

    enum class Hint {
        /** Select settings for compute bound applications: use all cores in each socket, one thread per core. */
        compute_bound,

        /** Select settings for memory bound applications: use only one core in each socket, one thread per core. */
        memory_bound,

        /** use extra threads with in-core multi-threading which can benefit communication intensive applications. Only supported with the task/affinity plugin. */
        multithread,

        /** don't use extra threads with in-core multi-threading which can benefit communication intensive applications. Only supported with the task/affinity plugin. */
        nomultithread,

        /** show this help message */
        help
    }

    enum class MailType {
        NONE, BEGIN, END, FAIL, REQUEUE,

        /** equivalent to BEGIN, END, FAIL, REQUEUE, and STAGE_OUT */
        ALL,

        /** burst buffer stage out and teardown completed */
        STAGE_OUT, TIME_LIMIT,

        /** reached 90 percent of time limit */
        TIME_LIMIT_90,

        /** reached 80 percent of time limit */
        TIME_LIMIT_80,

        /** reached 50 percent of time limit */
        TIME_LIMIT_50,

        /** send emails for each array task */
        ARRAY_TASKS
    }

    enum class OpenMode { append, truncate }

    enum class Profile {
        /** All data types are collected. (Cannot be combined with other values.) */
        All,

        /** No data types are collected. This is the default. (Cannot be combined with other values.) */
        None,

        /** Energy data is collected. */
        Energy,

        /** Task (I/O, Memory, ...) data is collected. */
        Task,

        /** Lustre data is collected. */
        Lustre,

        /** Network (InfiniBand) data is collected. */
        Network
    }

    enum class Propagate {
        /** All limits listed below (default) */
        ALL,

        /** No limits listed below */
        NONE,

        /** The maximum address space for a process */
        AS,

        /** The maximum size of core file */
        CORE,

        /** The maximum amount of CPU time */
        CPU,

        /** The maximum size of a process's data segment */
        DATA,

        /** The maximum size of files created. Note that if the user sets FSIZE to less than the current size of the
         *  slurmd.log, job launches will fail with a 'File size limit exceeded' error. */
        FSIZE,

        /** The maximum size that may be locked into memory */
        MEMLOCK,

        /** The maximum number of open files */
        NOFILE,

        /** The maximum number of processes available */
        NPROC,

        /** The maximum resident set size */
        RSS,

        /** The maximum stack size */
        STACK
    }
}

/** The default value for the task sampling interval is 30 seconds. The default value for all other intervals is 0.
 *  An interval of 0 disables sampling of the specified type. If the task sampling interval is 0, accounting information
 *  is collected only at job termination (reducing Slurm interference with the job).
 *  Smaller (non-zero) values have a greater impact upon job performance, but a value of 30 seconds is not likely to be
 *  noticeable for applications having less than 10,000 tasks. */
sealed class DataType(val interval: Second) {

    /** @param interval is the task sampling interval in seconds for the jobacct_gather plugins and for task
     *  profiling by the acct_gather_profile plugin.
     *  NOTE: This frequency is used to monitor memory usage. If memory limits are enforced the highest frequency a user
     *  can request is what is configured in the slurm.conf file. They can not turn it off (=0) either. */
    class task(interval: Second = 30.s) : DataType(interval) {
        init {
            if (interval == 0.s) error("You need to define an interval != 0 seconds")
        }
    }

    /** @param interval is the sampling interval in seconds for energy profiling using the acct_gather_energy plugin */
    class energy(interval: Second = 0.s) : DataType(interval)

    /** @param interval is the sampling interval in seconds for infiniband profiling using the acct_gather_interconnect plugin.  */
    class network(interval: Second = 0.s) : DataType(interval)

    /** @param interval is the sampling interval in seconds for filesystem profiling using the acct_gather_filesystem plugin. */
    class filesystem(interval: Second = 0.s) : DataType(interval)
}

enum class FrequencyPolicy { low, medium, high, highm1, Conservative, OnDemand, Performance, PowerSave }

@ExperimentalTime
class DependencyBuilder(val sbatch: Sbatch = Sbatch()) {

    /** After the specified jobs start or are cancelled and 'time' in minutes from job start or cancellation happens,
     *  this job can begin execution. If no 'time' is given then there is no delay after start or cancellation. */
    fun after(vararg jobs: Job) {
        for (job in jobs) {
            sbatch.dependencyAfter += ":${job.id}"
            if (job is JobTime)
                sbatch.dependencyAfter += "+${job.time}"
        }
    }

    /** This job can begin execution after the specified jobs have terminated. */
    fun afterAny(vararg jobs: Job) {
        for (job in jobs) sbatch.dependencyAfterAny += ":${job.id}"
    }

    /** This job can begin execution after the specified jobs have terminated and any associated burst buffer stage out
     *  operations have completed. */
    fun afterBurstBuffer(vararg jobs: Job) {
        for (job in jobs) sbatch.dependencyBurstBuffer += ":${job.id}"
    }

    /** A task of this job array can begin execution after the corresponding task ID in the specified job has completed
     *  successfully (ran to completion with an exit code of zero). */
    fun afterCorr(vararg jobs: Job) {
        for (job in jobs) sbatch.dependencyCorr += ":${job.id}"
    }

    /** This job can begin execution after the specified jobs have terminated in some failed state (non-zero exit code,
     *  node failure, timed out, etc). */
    fun afterNotOk(vararg jobs: Job) {
        for (job in jobs) sbatch.dependencyNotOk += ":${job.id}"
    }

    /** This job can begin execution after the specified jobs have successfully executed (ran to completion with an exit
     *  code of zero). */
    fun afterOk(vararg jobs: Job) {
        for (job in jobs) sbatch.dependencyOk += ":${job.id}"
    }

    /** Resources allocated to this job should be used to expand the specified job. The job to expand must share the
     *  same QOS (Quality of Service) and partition. Gang scheduling of resources in the partition is also not supported.
     *  "expand" is not allowed for jobs that didn't originate on the same cluster as the submitted job. */
    fun expand(job: Job) {
        sbatch.dependencyExpand = job.id.toString()
    }

    /** This job can begin execution after any previously launched jobs sharing the same job name and user have
     *  terminated. In other words, only one job by that name and owned by that user can be running or suspended at any
     *  point in time. In a federation, a singleton dependency must be fulfilled on all clusters unless
     *  `DependencyParameters=disable_remote_singleton` is used in slurm.conf. */
    fun singleton() {
        sbatch.dependencySingleton = true
    }
}

open class Job(val id: Int)

class JobTime(id: Int, val time: Minute) : Job(id)

enum class Exclusive { otherRunningJobs, user, mcs }