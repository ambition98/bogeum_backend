package xyz.bogeum.auth

import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import xyz.bogeum.util.logger
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthEntryPoint(
    val jwtProvider: JwtProvider
) : AuthenticationEntryPoint {

    val log = logger()

    override fun commence(
        req: HttpServletRequest,
        resp: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val out = resp.writer
        resp.setHeader("Content-Type", "application/json")
        val token = jwtProvider.getTokenFromCookie(req)
//        log.info("[EntryPoint] token: $token")
//        log.info("[EntryPoint] auth: ${SecurityContextHolder.getContext().authentication}")

        val response = when(jwtProvider.getState(token)) {
            JwtState.EXPIRED -> {
                resp.status = HttpStatus.UNAUTHORIZED.value()
                log.info("Expired Jwt")
                "{httpStatus: ${HttpStatus.UNAUTHORIZED}, msg: Expired access token"
            }
            else -> {
                resp.status = HttpStatus.BAD_REQUEST.value()
                log.warn("Invalid Jwt")
                "{httpStatus: ${HttpStatus.BAD_REQUEST}, msg: Need Login"
            }
        }
        out.print(response)
        out.flush()
        out.close()
    }

//    private fun makeResponseJson(httpStatus: HttpStatus, msg: String): String {
//        return "{httpStatus: $httpStatus, msg: $msg}"
//    }
}