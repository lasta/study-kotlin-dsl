package lasta.page.service.workflow

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SequentialTest {

    @Nested
    inner class WhenGivenNoTask {

        @Test
        fun `then runs successfully`() {
            val workflow = Sequential()
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

        @Test
        fun `then runs the task`() {
            val workflow = Sequential(task)
            workflow.run()
            verify(exactly = 1) { task.run() }
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

        @Test
        fun `then runs tasks sequentially`() {
            val workflow = Sequential(task1, task2, task3)
            workflow.run()
            verifyOrder {
                task1.run()
                task2.run()
                task3.run()
            }
        }
    }
}
