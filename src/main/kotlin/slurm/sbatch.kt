package slurm

import java.io.File
import java.time.LocalDateTime

inline fun sbatch(block: SbatchBuilder.() -> Unit) = SbatchBuilder().block()

class Sbatch {
    val array = ArrayList<Int>()
    var account = ""
    val dataTypes = ArrayList<DataType>()
    var extraNodeInfo = ""
    var batch = ""
    var bb = ""
    var bbf = ""
    var begin: LocalDateTime = LocalDateTime.MIN
    var clusterConstraint: String = ""
    var comment: String = ""
    var constraint: String = ""
    var container: File? = null
    var contiguous: Boolean? = null
    var coresPerSocket: Int? = null
    var cpuFreq: String? = null
    var cpusPerGpu: Int? = null
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
    var exportFile: String? = null
    var nodeFile: File? = null
    var getUserEnv: String? = null
    var groupID: String? = null
    val gpus = ArrayList<String>()
    var gpuBind = ""
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