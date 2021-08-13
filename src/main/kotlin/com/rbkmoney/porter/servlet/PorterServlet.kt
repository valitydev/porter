package com.rbkmoney.porter.servlet

import com.rbkmoney.notification.NotificationServiceSrv
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder
import javax.servlet.GenericServlet
import javax.servlet.Servlet
import javax.servlet.ServletConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebServlet

@WebServlet("/porter/v1")
class PorterServlet(
    private val porterHandler: NotificationServiceSrv.Iface,
) : GenericServlet() {

    private lateinit var thriftServlet: Servlet

    override fun init(config: ServletConfig) {
        super.init(config)
        thriftServlet = THServiceBuilder().build(NotificationServiceSrv.Iface::class.java, porterHandler)
    }

    override fun service(req: ServletRequest, response: ServletResponse) {
        thriftServlet.service(req, response)
    }
}
