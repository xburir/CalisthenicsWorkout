package com.example.calisthenicsworkout.database

import android.content.Context
import androidx.room.*
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef

@Database(entities = [
    Skill::class,
    SkillAndSkillCrossRef::class
                     ],
            version = 5 ,
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

