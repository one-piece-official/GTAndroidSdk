package com.gt.sdk.base.videocache.file;

import java.io.File;
import java.io.IOException;

/**
 * Declares how {@link FileCache} will use disc space.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public interface DiskUsage {

    void touch(File file) throws IOException;

}
