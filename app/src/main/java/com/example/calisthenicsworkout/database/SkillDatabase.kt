package com.example.calisthenicsworkout.database

import android.content.Context
import androidx.room.*
import com.example.calisthenicsworkout.database.entities.*

@Database(entities = [
    Skill::class,
    SkillAndSkillCrossRef::class,
    User::class,
    UserAndSkillCrossRef::class,
    Exercise::class,
    Training::class
                     ],
            version = 20 ,
            exportSchema = false)
@TypeConverters(Converters::class)
abstract class SkillDatabase : RoomDatabase() {

    abstract fun skillDatabaseDao(): SkillDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: SkillDatabase? = null

        fun getInstance(context: Context): SkillDatabase{
            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,SkillDatabase::class.java,"Database")
                        .fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }


    }
}

