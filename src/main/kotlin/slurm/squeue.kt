package slurm

inline fun squeue(block: SqueueBuilder.() -> Unit) = SqueueBuilder().block()

@SlurmMarker
class Squeue {
    val accounts = ArrayList<String>()
    var all = false
    var array = false
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
     *  using a regular expression). */
}