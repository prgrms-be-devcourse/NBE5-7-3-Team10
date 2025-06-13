package kr.co.programmers.collabond.shared.exception

import kr.co.programmers.collabond.shared.exception.AbstractCustomException
import kr.co.programmers.collabond.shared.exception.ErrorCode

class DuplicatedException : AbstractCustomException {

    constructor() : super(ErrorCode.DUPLICATED_DATA)

    constructor(errorCode: ErrorCode) : super(errorCode)
}

class ExpiredException : AbstractCustomException {

    constructor() : super(ErrorCode.EXPIRED_DATA)

    constructor(errorCode: ErrorCode) : super(errorCode)
}

class ForbiddenException : AbstractCustomException {

    constructor() : super(ErrorCode.FORBIDDEN_REQUEST)

    constructor(errorCode: ErrorCode) : super(errorCode)
}

class InternalException : AbstractCustomException {

    constructor() : super(ErrorCode.INTERNAL_SERVER_ERROR)

    constructor(errorCode: ErrorCode) : super(errorCode)
}

class InvalidException : AbstractCustomException {

    constructor() : super(ErrorCode.INVALID_REQUEST)

    constructor(errorCode: ErrorCode) : super(errorCode)
}

class NotFoundException : AbstractCustomException {

    constructor() : super(ErrorCode.NOT_FOUND)

    constructor(errorCode: ErrorCode) : super(errorCode)
}