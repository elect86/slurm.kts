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
        val cmd = "sbatch"
        var args = ArrayList<String>()
        if (array.isNotEmpty()) args += "-a=${array.joinToString(",")}"
        if (account.isNotEmpty()) args += "-A=$account"
        //            if (acctgFreq)
        if (extraNodeInfo.isNotEmpty()) args += "-B=$extraNodeInfo"
        if (batch.isNotEmpty()) args += "--batch=$batch"
        if (bb.isNotEmpty()) args += "--bb=$bb"
        if (bbf.isNotEmpty()) args += "--bbf=$bbf"
        if (begin != LocalDateTime.MIN) args += "-b=$begin"
        if (checkpoint != Duration.INFINITE) args += "--checkpoint=${checkpoint.toIsoString()}"
        if (clusterConstraint.isNotEmpty()) args += "--cluster-constraint=$clusterConstraint"
        if (contiguous) args += "--contiguous"
        if (coresPerSocket != -1) args += "--cores-per-socket=$coresPerSocket"
        if (cpuFreq.isNotEmpty()) args += "--cpu-freq=$cpuFreq"
        if (cpusPerGpu != -1) args += "--cpus-per-gpu=$cpusPerGpu"
        if (deadline != LocalDateTime.MIN) args += "--deadline=$deadline"
        delayBoot?.let { args += "--delay-boot=${it.value}" }
        var dep = ""
        if (dependencyAfter.isNotEmpty()) dep += dependencyAfter
        if (dependencyAfterAny.isNotEmpty()) dep += dependencyAfterAny
        if (dependencyBurstBuffer.isNotEmpty()) dep += dependencyBurstBuffer
        if (dependencyCorr.isNotEmpty()) dep += dependencyCorr
        if (dependencyNotOk.isNotEmpty()) dep += dependencyNotOk
        if (dependencyOk.isNotEmpty()) dep += dependencyOk
        if (dependencyExpand.isNotEmpty()) dep += dependencyExpand
        if (dependencySingleton) dep += "singleton"
        if (dep.isNotEmpty()) args += "-d=$dep"
        chDir?.let { args += "-D=${it.absolutePath}" }
        error?.let { args += "-e=${it.absolutePath}" }
        exclusive?.let {
            var arg = "--exclusive"
            if (it != Exclusive.otherRunningJobs)
                arg += "=$it"
            args += arg
        }
        if (export.isNotEmpty()) args += "--export=${export.joinToString(",")}"
        if (exportFile.isNotEmpty()) args += "--export-file=$exportFile"
        nodeFile?.let { args += "-F=${it.absolutePath}" }
        getUserEnv?.let {
            var arg = "--get-user-env"
            if (it.isNotEmpty())
                arg += "=$it"
            args += arg
        }
        if (groupID.isNotEmpty()) args += "--gid=$groupID"
        if (gpus.isNotEmpty()) args += "-G=${gpus.joinToString(",")}"
        if (gpuBind.isNotEmpty()) args += "--gpu-bind=$gpuBind"
        if (gpuFreq.isNotEmpty()) args += "--gpu-freq=$gpuFreq"
        if (gpusPerNode.isNotEmpty()) args += "--gpu-per-node=${gpusPerNode.joinToString(",")}"
        if (gpusPerSocket.isNotEmpty()) args += "--gpu-per-socket=${gpusPerSocket.joinToString(",")}"
        if (gpusPerTask.isNotEmpty()) args += "--gpu-per-task=${gpusPerTask.joinToString(",")}"
        if (gres.isNotEmpty()) args += "--gres=${gres.joinToString(",")}"
        gresFlags?.let { args += "--gres-flags=$it" }
        if (hold) args += "-H"
        if (help) args += "-h"
        hint?.let { args += "--hint=$it" }
        if (ignorePBS) args += "--ignore-pbs"
        if (input.isNotEmpty()) args += "-i=$input"
        if (jobName.isNotEmpty()) args += "-J=$jobName"
        if (dontKill.isNotEmpty()) args += "-k=$dontKill"
        if (killOnInvalidDep.isNotEmpty()) args += "--kill-on-invalid-dep=$killOnInvalidDep"
        if (licenses.isNotEmpty()) args += "-L=${licenses.joinToString(",")}"
        if (clusters.isNotEmpty()) args += "-M=${clusters.joinToString(",")}"
        if (distribution.isNotEmpty()) args += "-m=$distribution"
        if (mailTypes.isNotEmpty()) args += "--mail-type=${mailTypes.joinToString(",")}"
        if (mailUser.isNotEmpty()) args += "--mail-user=$mailUser"
        if (mcsLabel.isNotEmpty()) args += "--mcs-label=$mcsLabel"
        if (mem.isNotEmpty()) args += "--mem=$mem"
        if (memPerCpu.isNotEmpty()) args += "--mem-per-cpu=${memPerCpu}"
        if (memPerGpu.isNotEmpty()) args += "--mem-per-gpu=${memPerGpu}"
        if (memBind.isNotEmpty()) args += "--mem-bind=${memBind.joinToString(",")}"
        if (minCpus != -1) args += "--mincpus=$minCpus"
        nodes?.let {
            args += "-N"
            var arg = it.first.toString()
            if (it.first != it.last)
                arg += "-${it.last}"
            args += arg
        }
        if (ntasks != -1) args += "-n=$ntasks"
        if (network.isNotEmpty()) args += "--network=$network"
        if (nice.isNotEmpty()) args += "--nice=$nice"
        if (noRequeue) args += "--no-requeue"
        if (nTasksPerCore != -1) args += "--ntasks-per-core=$nTasksPerCore"
        if (nTasksPerNode != -1) args += "--ntasks-per-node=$nTasksPerNode"
        if (nTasksPerSocket != -1) args += "--ntasks-per-socket=$nTasksPerSocket"
        if (overcommit) args += "-O"
        if (output.isNotEmpty()) args += "-o=$output"
        openMode?.let { args += "--open-mode=$it" }
        if (parsable) args += "--parsable"
        if (partition.isNotEmpty()) args += "-p=${partition.joinToString(",")}"
        if (power.isNotEmpty()) args += "--power=${power.joinToString(",")}"
        if (priority.isNotEmpty()) args += "--priority=$priority"
        if (profiles.isNotEmpty()) args += "--profile=$profiles"
        if (propagate.isNotEmpty()) args += "--propagate=${propagate.joinToString(",")}"
        if (qos.isNotEmpty()) args += "--q=$qos"
        if (quiet) args += "-Q"
        if (reboot) args += "--reboot"
        if (requeue) args += "--requeue"
        if (reservation.isNotEmpty()) args += "--reservation=$reservation"
        if (oversubscribe) args += "-s"
        if (coreSpec != -1) args += "-S=$coreSpec"
        if (signal.isNotEmpty()) args += "--signal=$signal"
        if (socketsPerNode != -1) args += "--sockets-per-node=$socketsPerNode"
        if (switches.isNotEmpty()) args += "--switches=${switches.joinToString(",")}"
        if (time != Duration.INFINITE) args += "-t=$time"
        if (testOnly) args += "--test-only"
        if (threadSpec != -1) args += "--thread-spec=$threadSpec"
        if (threadsPerCore != -1) args += "--threads-per-core=$threadsPerCore"
        if (timeMin != Duration.INFINITE) args += "--time-min=$timeMin"
        if (tmp.isNotEmpty()) args += "--tmp$tmp"
        if (usage) args += "--usage"
        if (uid.isNotEmpty()) args += "--uid=$uid"
        if (useMinNodes) args += "--use-min-nodes"
        if (version) args += "-V"
        if (verbose) args += "-v"
        if (nodeList.isNotEmpty()) args += "-w=${nodeList.joinToString(",")}"
        if (wait) args += "-W"
        waitAllNodes?.let { args += " --wait-all-nodes=$it" }
        if (wckey.isNotEmpty()) args += "--wckey"
        if (wrap.isNotEmpty()) args += "--wrap"
        if (exclude.isNotEmpty()) args += "-x=${exclude.joinToString(",")}"

        println("running `$cmd`")
        return cmd(args)
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