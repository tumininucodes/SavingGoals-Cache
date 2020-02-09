package com.sample.android.qapital.goals

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sample.android.qapital.api.QapitalApi
import com.sample.android.qapital.data.SavingsGoal
import com.sample.android.qapital.data.source.GoalsRepository
import com.sample.android.qapital.data.source.local.GoalsDao
import com.sample.android.qapital.data.source.local.QapitalLocalDataSource
import com.sample.android.qapital.data.source.remote.SavingsGoalsRemoteDataSource
import com.sample.android.qapital.usecase.SavingsGoalsUseCase
import com.sample.android.qapital.viewmodels.SavingsGoalsViewModel
import com.sample.android.qapital.util.DiskIOThreadExecutor
import com.sample.android.qapital.util.Resource
import com.sample.android.qapital.util.schedulers.BaseSchedulerProvider
import com.sample.android.qapital.util.schedulers.ImmediateSchedulerProvider
import io.reactivex.Observable
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class SavingsGoalsViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var api: QapitalApi
    @Mock
    private lateinit var dao: GoalsDao
    private lateinit var repository: GoalsRepository
    private lateinit var schedulerProvider: BaseSchedulerProvider
    private lateinit var savingsGoal: SavingsGoal

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        // Make the sure that all schedulers are immediate.
        schedulerProvider = ImmediateSchedulerProvider()

        val executor = DiskIOThreadExecutor()
        val remoteDataSource = SavingsGoalsRemoteDataSource(api)
        val localDataSource = QapitalLocalDataSource(executor, dao)
        repository = GoalsRepository(remoteDataSource, localDataSource, executor)

        savingsGoal = SavingsGoal("", .0f, 12f, "name", 1)
    }

    @Test
    fun loadSavingsGoal() {
        val observableResponse = Observable.just(QapitalApi.SavingsGoalWrapper(listOf(savingsGoal)))
        `when`(api.requestSavingGoals()).thenReturn(observableResponse)

        val useCase = SavingsGoalsUseCase(schedulerProvider, repository)
        val viewModel = SavingsGoalsViewModel(repository, useCase)

        with(viewModel.liveData.value) {
            if (this is Resource.Success) {
                assertFalse(data!!.isEmpty())
                assertTrue(data?.size == 1)
            }
        }
    }
}