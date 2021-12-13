
package com.example.calisthenicsworkout.test

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.Skill
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class SleepDatabaseTest {

    private lateinit var skillDao: SkillDatabaseDao
    private lateinit var db: SkillDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, SkillDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        skillDao = db.skillDatabaseDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetSkill() {
//        val skill = Skill(0,"Dip","")
//        skillDao.insert(skill)
//        val chosenSkill = Skill(0,"Dip","")
//        assertEquals("Dip", chosenSkill.value?.skillName)
    }
}
