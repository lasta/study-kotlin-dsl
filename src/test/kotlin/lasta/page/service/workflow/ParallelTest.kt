package lasta.page.service.workflow

import assertk.assertFailure
import assertk.assertions.hasMessage
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import io.mockk.verifyAll
import io.mockk.verifyOrder
import io.mockk.verifySequence
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource

class ParallelTest {

    @Nested
    inner class WhenGivenNoTask {
        @Test
        fun `then runs successfully`() {
            val workflow = Parallel()
            workflow.run()
        }
    }

    @Nested
    inner class WhenGivenOneTask {

        @MockK
        private lateinit var task: Task

        @BeforeEach
        fun setUp() {
            MockKAnnotations.init(this)
            every { task.run() } just Runs
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2])
        @NullSource
        @DisplayName("When given maxConcurrency is positive or not given")
        fun `then runs successfully`(maxConcurrency: Int?) {
            val workflow = Parallel(task, maxConcurrency = maxConcurrency)
            workflow.run()
            verify(exactly = 1) { task.run() }
        }

        @ParameterizedTest
        @ValueSource(ints = [-1, 0])
        @DisplayName("When given maxConcurrency is negative")
        fun `then throws IllegalArgumentException`(maxConcurrency: Int) {
            val workflow = Parallel(task, maxConcurrency = maxConcurrency)
            assertFailure {
                workflow.run()
            }.hasMessage("maxConcurrency must be positive (got: $maxConcurrency)")
            verify {
                task wasNot Called
            }
        }
    }

    @Nested
    inner class WhenGivenPluralTasks {

        @MockK
        private lateinit var task1: Task

        @MockK
        private lateinit var task2: Task

        @MockK
        private lateinit var task3: Task

        @BeforeEach
        fun setUp() {
            MockKAnnotations.init(this)
            every { task1.run() } just Runs
            every { task2.run() } just Runs
            every { task3.run() } just Runs
        }

        @AfterEach
        fun tearDown() {
            confirmVerified(task1, task2, task3)
        }

        @Nested
        inner class WhenNotGivenConcurrency {

            @Test
            fun `then runs all tasks without ordering`() {
                val workflow = Parallel(task1, task2, task3)
                workflow.run()
                verifyAll {
                    task1.run()
                    task2.run()
                    task3.run()
                }
            }
        }

        @Nested
        inner class WhenGivenConcurrencyIs1 {

            @Test
            fun `then runs sequentially`() {
                val workflow = Parallel(task1, task2, task3, maxConcurrency = 1)
                workflow.run()
                verifySequence {
                    task1.run()
                    task2.run()
                    task3.run()
                }
            }
        }

        @Nested
        inner class WhenGivenConcurrencyIsLessThanTaskSize {

            @Test
            fun `then runs later if tasks overflowing from maxConcurrency`() {
                val workflow = Parallel(task1, task2, task3, maxConcurrency = 2)
                workflow.run()
                verifyOrder {
                    task1.run()
                    task3.run()
                }
                verifyOrder {
                    task2.run()
                    task3.run()
                }
            }
        }

        @Nested
        inner class WhenGivenConcurrencyIsMoreThanOrEqualToTaskSize {

            @ParameterizedTest
            @ValueSource(ints = [3, 4])
            fun `then runs all tasks concurrently`(maxConcurrency: Int) {
                val workflow = Parallel(task1, task2, task3, maxConcurrency = maxConcurrency)
                workflow.run()
                verifyAll {
                    task1.run()
                    task2.run()
                    task3.run()
                }
            }
        }
    }
}
