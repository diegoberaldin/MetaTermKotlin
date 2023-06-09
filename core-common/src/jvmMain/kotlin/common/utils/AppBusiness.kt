package common.utils

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner

object AppBusiness : InstanceKeeperOwner {

    override val instanceKeeper: InstanceKeeper by lazy {
        InstanceKeeperDispatcher()
    }
}
