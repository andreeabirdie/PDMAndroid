package ro.ubbcluj.ro.birdie.myapp.songs.data

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SongRepoWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        when (inputData.getString("operation")) {
            "save" -> SongRepoHelper.save()
            "update" -> SongRepoHelper.update()
            "delete" -> SongRepoHelper.delete()
            else -> return Result.failure()
        }
        return Result.success()
    }
}