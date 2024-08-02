// Copyright 2018-2019 Sigmob, Inc.
// Licensed under the Sigmob SDK License Agreement
// http://www.sigmob.com/legal/sdk-license-agreement/

package com.sigmob.sdk.mraid;

public class MraidCommandException extends Exception {
    public MraidCommandException() {
        super();
    }

    public MraidCommandException(String detailMessage) {
        super(detailMessage);
    }

    public MraidCommandException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MraidCommandException(Throwable throwable) {
        super(throwable);
    }
}
