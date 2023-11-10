package crichton.application.exceptions.code;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FailedErrorCode implements ErrorCode {
    NOT_EXIST_DIRECTORY("F001", "Not Exist Directory"),
    UPLOAD_FAILED("F002", "Upload Failed"),
    LOG_READ_FAILED("F003", "Log Read Failed");

    private final String code;
    private final String message;
}

