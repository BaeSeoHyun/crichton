package crichton.security;

import crichton.application.exceptions.CustomException;
import crichton.application.exceptions.code.TokenErrorCode;
import crichton.domain.dtos.PayloadDTO;
import crichton.domain.services.AccessTokenService;
import crichton.domain.services.RefreshTokenService;
import crichton.util.ObjectMapperUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Base64;

@Component("TokenInterceptor")
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final static String HEADER_AUTH = "Authorization";
    private final static String REFRESH_TOKEN = "RefreshToken";
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;

    public String decodeBase64(String base64) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        return new String(decodedBytes);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws CustomException {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/v1/crichton/auth/token")) {
            return true;
        }
        try {
            String accessToken = request.getHeader(HEADER_AUTH);
            String refreshToken = request.getHeader(REFRESH_TOKEN);
            String[] tokenParts = accessToken.split("\\.");
            String payload = decodeBase64(tokenParts[1]);
            PayloadDTO payloadDTO = ObjectMapperUtils.convertJsonStringToObject(payload, PayloadDTO.class);
            // 1. accessToken 무결성 검사 및 만료날짜 검사
            if (accessTokenService.validateAccessToken(accessToken, payloadDTO) && !accessTokenService.isAccessTokenExpired(payloadDTO)){
                return true;
            }
            //2. refreshToken 값이 header에 포함되서 날라오고 refreshToken 기간이 만료되지않았을때
            if (refreshToken != null && refreshTokenService.validateRefreshToken(payloadDTO.getSub(),refreshToken)) {
                String newAccessToken = accessTokenService.refreshAccessToken(accessToken, payloadDTO);
                //3. 새로운 accessToken 이 성공적으로 발급되었을 때 헤더에 포함해서 전달
                response.setHeader("Authorization", newAccessToken);
                return true;
            }
            //4. refreshToken 값이 header에 포함되지않거나 refreshToken 기간이 만료되었을때
            else {
                // 5. refreshToken 값이 header에 포함되지않았는지 확인
                if (refreshToken != null){
                    // 해당 분기는 refreshToken 기간이 만료되었을 때
                    throw new CustomException(TokenErrorCode.INVALID_REFRESH_TOKEN);
                }
                else {
                    // 해당 분기는 accessToken 기간이 만료되었을때
                    throw new CustomException(TokenErrorCode.INVALID_ACCESS_TOKEN);
                }
            }
        }
        catch (CustomException customException) {
            throw customException;
        }
        catch (Exception e){
            throw new CustomException(TokenErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }
}
