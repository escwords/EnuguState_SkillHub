package com.words.storageapp.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WokrdataDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var workDataDao: AllSkillsDbDao

//    private val plantA = WokrData("1", "A", "B", "", "", "",
//        null,null,null,null,null,null,
//        null,null,null,null,null,null,
//        null,null,null,null)
//
//    private val plantB = WokrData("1", "A", "B", "", "", "",
//        null,null,null,null,null,null,
//        null,null,null,null,null,null,
//        null,null,null,null)
//
//    private val plantC = WokrData("1", "A", "B", "", "", "",
//        null,null,null,null,null,null,
//        null,null,null,null,null,null,
//        null,null,null,null)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        workDataDao = database.allSkillsDbDao()

        // Insert plants in non-alphabetical order to test that results are sorted by name
        //workDataDao.insertAll(listOf(plantB, plantC, plantA))
    }

    @After
    fun closeDb() {
        database.close()
    }

//    @Test
//    fun testGetPlants() {
//        val plantList = getValue(workDataDao.getAllSkills())
//        assertThat(plantList.size, equalTo(3))

    // Ensure plant list is sorted by name

}
/*
    @Test fun testGetPlantsWithGrowZoneNumber() {
        val plantList = getValue(plantDao.getPlantsWithGrowZoneNumber(1))
        assertThat(plantList.size, equalTo(2))
        assertThat(getValue(plantDao.getPlantsWithGrowZoneNumber(2)).size, equalTo(1))
        assertThat(getValue(plantDao.getPlantsWithGrowZoneNumber(3)).size, equalTo(0))

        // Ensure plant list is sorted by name
        assertThat(plantList[0], equalTo(plantA))
        assertThat(plantList[1], equalTo(plantB))
    }*/

@Test
fun testGetPlant() {
    // assertThat(getValue(plantDao.getPlant(plantA.plantId)), equalTo(plantA))
}
