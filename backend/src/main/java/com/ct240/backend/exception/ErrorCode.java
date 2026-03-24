package com.ct240.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {
    UNAUTHENTICATED(1003, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "Unauthorized", HttpStatus.FORBIDDEN),

    USER_EXISTED(1100, "User Existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1101, "User Not Found", HttpStatus.NOT_FOUND),

    SPACE_NOT_FOUND(1201, "Space Not Found", HttpStatus.NOT_FOUND),
    USER_EXISTED_IN_SPACE(1202, "User Existed In The Space", HttpStatus.CONFLICT),
    USER_NOT_EXIST_IN_SPACE(1203, "User Not Exist In The Space" , HttpStatus.NOT_FOUND),
    OWNER_CANNOT_LEAVE_SPACE(1204, "Owner Cannot Leave The Space", HttpStatus.FORBIDDEN),

    BOARD_NOT_FOUND(1301, "Board Not Found", HttpStatus.NOT_FOUND),
    USER_EXISTED_IN_BOARD(1302, "User Existed In The Board", HttpStatus.CONFLICT),
    USER_NOT_EXIST_IN_BOARD(1303, "User Not Exist In The Board" , HttpStatus.NOT_FOUND),
    OWNER_CANNOT_LEAVE_BOARD(1304, "Owner Cannot Leave The Board", HttpStatus.FORBIDDEN),

    CARD_NOT_FOUND(1401, "Card Not Found", HttpStatus.NOT_FOUND),
    CARD_POSITION_CONFLICT(1405, "Position Card Conflict Detected", HttpStatus.BAD_REQUEST),

    TASK_NOT_FOUND(1501, "Task Not Found", HttpStatus.NOT_FOUND),
    USER_NOT_ASSIGNED_TO_TASK(1503, "User Not Assigned To The Task", HttpStatus.NOT_FOUND),
    TASK_POSITION_CONFLICT(1405, "Position Task Conflict Detected", HttpStatus.BAD_REQUEST),

    COMMENT_NOT_FOUND(1601, "Comment Not Found", HttpStatus.NOT_FOUND),

    NOTIFICATION_NOT_FOUND(1701,  "Notification Not Found", HttpStatus.NOT_FOUND),

    FILE_EMPTY(1801, "File must not be empty", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(1802, "Fill is invalid", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    FILE_TOO_LARGE(1803, "File loaded is too large", HttpStatus.PAYLOAD_TOO_LARGE),
    UPLOAD_FAILED(1804, "The file is not uploaded", HttpStatus.INTERNAL_SERVER_ERROR),
    CANNOT_DELETE_DEFAULT_AVATAR(1805, "Cannot delete default avatar", HttpStatus.BAD_REQUEST);

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
