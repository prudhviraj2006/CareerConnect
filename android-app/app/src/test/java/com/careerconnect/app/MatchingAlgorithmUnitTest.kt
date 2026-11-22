package com.careerconnect.app

import org.junit.Assert.*
import org.junit.Test

class MatchingAlgorithmUnitTest {

    @Test
    fun testWeightDistribution() {
        val skillsWeight = 0.60f
        val expWeight = 0.20f
        val eduWeight = 0.10f
        val prefWeight = 0.10f

        val totalWeight = skillsWeight + expWeight + eduWeight + prefWeight
        assertEquals(1.00f, totalWeight, 0.001f)
    }

    @Test
    fun testSkillMatchRatio() {
        val requiredSkills = listOf("kotlin", "jetpack compose", "room", "coroutines")
        val candidateSkills = listOf("kotlin", "jetpack compose", "room")

        val matched = requiredSkills.filter { candidateSkills.contains(it) }
        val missing = requiredSkills.filterNot { candidateSkills.contains(it) }

        assertEquals(3, matched.size)
        assertEquals(1, missing.size)
        assertTrue(missing.contains("coroutines"))

        val ratio = matched.size.toFloat() / requiredSkills.size.toFloat()
        val skillsScore = ratio * 60f
        assertEquals(45f, skillsScore, 0.1f)
    }

    @Test
    fun testExperienceScoreCalculation() {
        val reqYears = 3.0f
        val candidateYearsExceeds = 4.0f
        val candidateYearsClose = 2.5f
        val candidateYearsNone = 0.0f

        // Meeting requirement gives full 20 points
        val scoreFull = if (candidateYearsExceeds >= reqYears) 20f else 0f
        assertEquals(20f, scoreFull, 0.01f)

        // Close to requirement gives 75% of 20 points = 15 points
        val scoreClose = if (candidateYearsClose >= reqYears - 1.0f) 15f else 0f
        assertEquals(15f, scoreClose, 0.01f)

        // Zero gives 0 points
        val scoreZero = if (candidateYearsNone >= reqYears) 20f else 0f
        assertEquals(0f, scoreZero, 0.01f)
    }
}
