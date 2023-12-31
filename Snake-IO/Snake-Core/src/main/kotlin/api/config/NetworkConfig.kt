package api.config

import java.net.InetSocketAddress
import java.net.NetworkInterface

data class NetworkConfig(
    val groupAddress: InetSocketAddress = groupAddress(),
    val localInterface: NetworkInterface = localInterface()
) {
    companion object Defaults {
        // TODO: fixed hardcode
        private fun groupAddress(): InetSocketAddress = InetSocketAddress("239.192.0.4", 9192)
        private fun localInterface(): NetworkInterface = NetworkInterface.getByName("wlp3s0")
    }
}
