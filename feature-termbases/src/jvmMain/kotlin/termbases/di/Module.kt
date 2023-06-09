package termbases.di

import dialogcreate.di.dialogCreateModule
import dialogcreate.stepone.di.createTermbaseStepOneModule
import dialogcreate.stepthree.di.createTermbaseStepThreeModule
import dialogcreate.steptwo.di.createTermbaseStepTwoModule
import dialogedit.di.dialogEditModule
import dialogmanage.di.dialogManageModule
import dialogstatistics.di.dialogStatisticsModule
import org.koin.dsl.module

val termbasesModule = module {
    includes(
        dialogStatisticsModule,
        dialogManageModule,
        dialogCreateModule,
        dialogEditModule,
        createTermbaseStepOneModule,
        createTermbaseStepTwoModule,
        createTermbaseStepThreeModule,
    )
}