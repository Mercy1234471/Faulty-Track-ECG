package com.example.util

import android.content.Context
import android.net.wifi.WifiManager
import com.example.data.repository.EcgRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections

class EcgHttpServer(
    private val context: Context,
    private val repository: EcgRepository,
    private val scope: CoroutineScope,
    val port: Int = 8080
) {
    private var serverSocket: ServerSocket? = null
    var isRunning: Boolean = false
        private set

    fun getDeviceIpAddress(): String {
        try {
            // First check Wi-Fi manager
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val wifiIpInt = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (wifiIpInt != 0) {
                val ip = String.format(
                    java.util.Locale.US,
                    "%d.%d.%d.%d",
                    wifiIpInt and 0xff,
                    wifiIpInt shr 8 and 0xff,
                    wifiIpInt shr 16 and 0xff,
                    wifiIpInt shr 24 and 0xff
                )
                if (ip != "0.0.0.0") return ip
            }

            // Fallback to active network interfaces
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val hostAddress = addr.hostAddress ?: ""
                        // Check if IPv4
                        val isIPv4 = hostAddress.indexOf(':') < 0
                        if (isIPv4 && hostAddress.isNotEmpty() && !hostAddress.startsWith("127.")) {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback
        }
        return "127.0.0.1"
    }

    fun getServerUrl(): String {
        val ip = getDeviceIpAddress()
        return "http://$ip:$port"
    }

    fun start() {
        if (isRunning) return
        scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                while (isActive && isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        launch(Dispatchers.IO) {
                            handleClient(clientSocket)
                        }
                    } catch (e: Exception) {
                        if (!isRunning) break
                    }
                }
            } catch (_: Exception) {
                isRunning = false
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
    }

    private suspend fun handleClient(socket: Socket) {
        withContext(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val output: OutputStream = socket.getOutputStream()

                val requestLine = reader.readLine() ?: return@withContext
                val parts = requestLine.split(" ")
                if (parts.size < 2) return@withContext

                val method = parts[0]
                val path = parts[1]

                when {
                    path == "/export/faults.csv" -> {
                        val faults = repository.allFaults.first()
                        val csv = ExcelExportHelper.generateFaultsCsv(faults)
                        val bytes = csv.toByteArray(Charsets.UTF_8)
                        val headers = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/csv; charset=UTF-8\r\n" +
                                "Content-Disposition: attachment; filename=\"ECG_Faults_Report.csv\"\r\n" +
                                "Content-Length: ${bytes.size}\r\n" +
                                "Connection: close\r\n\r\n"
                        output.write(headers.toByteArray(Charsets.UTF_8))
                        output.write(bytes)
                        output.flush()
                    }
                    path == "/export/meter_requests.csv" -> {
                        val requests = repository.allMeterRequests.first()
                        val csv = ExcelExportHelper.generateMeterRequestsCsv(requests)
                        val bytes = csv.toByteArray(Charsets.UTF_8)
                        val headers = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/csv; charset=UTF-8\r\n" +
                                "Content-Disposition: attachment; filename=\"ECG_Meter_Requests.csv\"\r\n" +
                                "Content-Length: ${bytes.size}\r\n" +
                                "Connection: close\r\n\r\n"
                        output.write(headers.toByteArray(Charsets.UTF_8))
                        output.write(bytes)
                        output.flush()
                    }
                    else -> {
                        val faults = repository.allFaults.first()
                        val requests = repository.allMeterRequests.first()
                        val techs = repository.allTechnicians.first()
                        val html = buildWebDashboardHtml(faults, requests, techs)
                        val bytes = html.toByteArray(Charsets.UTF_8)
                        val headers = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/html; charset=UTF-8\r\n" +
                                "Content-Length: ${bytes.size}\r\n" +
                                "Connection: close\r\n\r\n"
                        output.write(headers.toByteArray(Charsets.UTF_8))
                        output.write(bytes)
                        output.flush()
                    }
                }
                socket.close()
            } catch (_: Exception) {
                try { socket.close() } catch (_: Exception) {}
            }
        }
    }

    private fun buildWebDashboardHtml(
        faults: List<com.example.data.model.FaultEntity>,
        meterRequests: List<com.example.data.model.MeterRequestEntity>,
        technicians: List<com.example.data.model.TechnicianEntity>
    ): String {
        val totalFaults = faults.size
        val pendingFaults = faults.count { it.status.equals("Pending", ignoreCase = true) }
        val inProgressFaults = faults.count { it.status.equals("In Progress", ignoreCase = true) }
        val resolvedFaults = faults.count { it.status.equals("Resolved", ignoreCase = true) }

        val faultsRows = faults.joinToString("") { f ->
            val statusColor = when (f.status.lowercase()) {
                "resolved" -> "#00E676"
                "in progress" -> "#29B6F6"
                else -> "#FFB300"
            }
            """
            <tr>
              <td><strong>${f.faultReference}</strong></td>
              <td>${f.faultType}</td>
              <td>${f.region} (${f.district})</td>
              <td>${f.locationAddress}</td>
              <td><span style="background:${statusColor}22; color:$statusColor; border:1px solid $statusColor; padding:4px 8px; border-radius:12px; font-weight:bold; font-size:12px;">${f.status}</span></td>
              <td>${f.assignedTechnicianName ?: "<span style='color:#888'>Unassigned</span>"}</td>
              <td>${f.reportedDate} ${f.reportedTime}</td>
            </tr>
            """.trimIndent()
        }

        val meterRows = meterRequests.joinToString("") { m ->
            val statusColor = when (m.status.lowercase()) {
                "completed" -> "#00E676"
                "in progress" -> "#29B6F6"
                "rejected" -> "#FF5252"
                "reviewed" -> "#AB47BC"
                else -> "#FFB300"
            }
            """
            <tr>
              <td><strong>${m.requestReference}</strong></td>
              <td>${m.fullName}</td>
              <td>${m.phoneNumber}</td>
              <td>${m.gpsAddress}</td>
              <td>${m.category}</td>
              <td><span style="background:${statusColor}22; color:$statusColor; border:1px solid $statusColor; padding:4px 8px; border-radius:12px; font-weight:bold; font-size:12px;">${m.status}</span></td>
              <td>${m.submittedDate} ${m.submittedTime}</td>
            </tr>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>ECG Faulty Track & Meter Portal</title>
          <style>
            :root {
              --ecg-yellow: #FFCC00;
              --ecg-black: #121212;
              --ecg-card: #1E1E1E;
              --ecg-border: #333333;
              --ecg-text: #F5F5F5;
              --ecg-muted: #A0A0A0;
            }
            * { box-sizing: border-box; margin:0; padding:0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
            body { background: var(--ecg-black); color: var(--ecg-text); padding: 24px; }
            .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid var(--ecg-yellow); padding-bottom: 16px; margin-bottom: 24px; }
            .logo-title { display: flex; align-items: center; gap: 14px; }
            .bolt { background: var(--ecg-yellow); color: #000; font-weight: 900; font-size: 24px; width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; border-radius: 8px; }
            .title { font-size: 24px; font-weight: 800; color: #FFF; letter-spacing: -0.5px; }
            .subtitle { font-size: 13px; color: var(--ecg-yellow); text-transform: uppercase; font-weight: 600; }
            .badge-offline { background: #222; border: 1px solid #444; color: #00E676; padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; display: flex; align-items: center; gap: 6px; }
            .dot { width: 8px; height: 8px; background: #00E676; border-radius: 50%; }
            .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 28px; }
            .stat-card { background: var(--ecg-card); border: 1px solid var(--ecg-border); border-radius: 12px; padding: 18px; border-left: 5px solid var(--ecg-yellow); }
            .stat-val { font-size: 32px; font-weight: 800; color: #FFF; margin-top: 4px; }
            .stat-label { font-size: 13px; color: var(--ecg-muted); text-transform: uppercase; letter-spacing: 0.5px; }
            .actions-bar { display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }
            .btn { background: var(--ecg-yellow); color: #000; font-weight: 700; text-decoration: none; padding: 10px 18px; border-radius: 8px; display: inline-flex; align-items: center; gap: 8px; font-size: 14px; border: none; cursor: pointer; transition: 0.2s; }
            .btn:hover { background: #E5B800; }
            .btn-outline { background: transparent; border: 1px solid var(--ecg-yellow); color: var(--ecg-yellow); }
            .btn-outline:hover { background: var(--ecg-yellow); color: #000; }
            .section { background: var(--ecg-card); border: 1px solid var(--ecg-border); border-radius: 12px; padding: 20px; margin-bottom: 28px; }
            .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; border-bottom: 1px solid var(--ecg-border); padding-bottom: 12px; }
            .section-title { font-size: 18px; font-weight: 700; color: var(--ecg-yellow); }
            table { width: 100%; border-collapse: collapse; font-size: 14px; text-align: left; }
            th { padding: 12px 14px; background: #262626; color: var(--ecg-muted); font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; }
            td { padding: 14px; border-bottom: 1px solid var(--ecg-border); }
            tr:hover td { background: #242424; }
            .footer { text-align: center; color: var(--ecg-muted); font-size: 12px; margin-top: 40px; border-top: 1px solid var(--ecg-border); padding-top: 16px; }
          </style>
        </head>
        <body>
          <div class="header">
            <div class="logo-title">
              <div class="bolt">⚡</div>
              <div>
                <div class="title">ECG FAULT TRACK REPORT</div>
                <div class="subtitle">Electricity Company of Ghana • Local Web Gateway</div>
              </div>
            </div>
            <div class="badge-offline">
              <span class="dot"></span> Online & Local Offline Mode
            </div>
          </div>

          <div class="stats-grid">
            <div class="stat-card" style="border-left-color: #FFCC00;">
              <div class="stat-label">Total Faults</div>
              <div class="stat-val">$totalFaults</div>
            </div>
            <div class="stat-card" style="border-left-color: #FFB300;">
              <div class="stat-label">Pending Action</div>
              <div class="stat-val">$pendingFaults</div>
            </div>
            <div class="stat-card" style="border-left-color: #29B6F6;">
              <div class="stat-label">In Progress</div>
              <div class="stat-val">$inProgressFaults</div>
            </div>
            <div class="stat-card" style="border-left-color: #00E676;">
              <div class="stat-label">Resolved</div>
              <div class="stat-val">$resolvedFaults</div>
            </div>
          </div>

          <div class="actions-bar">
            <a href="/export/faults.csv" class="btn" download="ECG_Faults_Report.csv">
              📥 Download Faults Excel (CSV)
            </a>
            <a href="/export/meter_requests.csv" class="btn btn-outline" download="ECG_Meter_Requests.csv">
              📥 Download Meter Requests Excel (CSV)
            </a>
          </div>

          <div class="section">
            <div class="section-header">
              <span class="section-title">⚡ Real-time Faults Log</span>
              <span style="font-size:12px; color:var(--ecg-muted);">${faults.size} records registered</span>
            </div>
            <div style="overflow-x: auto;">
              <table>
                <thead>
                  <tr>
                    <th>Ref ID</th>
                    <th>Type</th>
                    <th>Region / District</th>
                    <th>Address</th>
                    <th>Status</th>
                    <th>Technician</th>
                    <th>Reported</th>
                  </tr>
                </thead>
                <tbody>
                  $faultsRows
                </tbody>
              </table>
            </div>
          </div>

          <div class="section">
            <div class="section-header">
              <span class="section-title">📋 Meter Requests Management</span>
              <span style="font-size:12px; color:var(--ecg-muted);">${meterRequests.size} applications</span>
            </div>
            <div style="overflow-x: auto;">
              <table>
                <thead>
                  <tr>
                    <th>Request ID</th>
                    <th>Applicant</th>
                    <th>Phone</th>
                    <th>GPS Address</th>
                    <th>Category</th>
                    <th>Status</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  $meterRows
                </tbody>
              </table>
            </div>
          </div>

          <div class="footer">
            Electricity Company of Ghana (ECG) • Enterprise Operational Portal • Server listening on port $port
          </div>
        </body>
        </html>
        """.trimIndent()
    }
}
